package com.quinn.hboot.js;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 升级扑克（山东泰安玩法）—— 控制台单机版 v3.1
 *
 * v3.1 新增（基于用户规则反馈）：
 *   ⑪ 赶主规则（chooseFollow §11.1）：首出为主对/主连对、本家同类别足够但无对应牌型时
 *      —— 主连对首出：先用所有小对（不拆大对），再"从大到小"用单张主牌补齐
 *      —— 主对首出且无对子：直接取最大两张主牌
 *      —— 完全无主时：可贴任意副牌（沿用 < need 分支）
 *   ⑫ 扣底策略（chooseBottom §11.4）：区分"己方亮主"与"对家亮主"
 *      —— 己方亮主：优先扣"绝一门花色"，牌好则进一步扣分牌
 *      —— 对家亮主：平衡扣牌——优先非主，牌好扣分牌、牌差避分牌
 *      —— "牌好"判据：本家主对 ≥ 2 副，或 队友连对（姊妹/三连/四连）亮主或反主
 *
 * 相对 v2.2 的修复 / 增强（严格对齐 upgrade_poker_rules_enhanced_v_2.md）：
 *   ① ScoreEngine.settle 修正：5–35 升 3 级（之前误写成升 2 级）
 *   ② mustPlayClamp 增加 "打过 K 即胜" 显式语义
 *   ③ 主连对：强制同花色（大王/小王单独成对，主花色之间不可跨花色拼连对）
 *   ④ 反主链路完整化：对子→姊妹对→三连对→四连对，且支持"对王反单王"、"对大王反对小王"
 *   ⑤ 反主补偿双向：反主者获得被反者亮出的王，被反者获得固定主（2/3/5）
 *   ⑥ 憋牌流程严格化：闲家随机盲抽 1 张非王底牌作为"花色种子"，
 *      庄家先盲扣再揭示花色（避免提前优化主牌结构）
 *   ⑦ sortHand 严格按规则：大王→小王→级牌(主花色优先)→常主5/3/2→主花色→副牌
 *   ⑧ AI 新增 lackSuitMap 断门记忆，用于调主决策
 *   ⑨ 跟牌不足时，主牌赶主严格按"从大到小"消耗
 *   ⑩ 完整结束条件：while(!allPlayersEmpty())，不再依赖固定轮数
 */
public class UpgradePokerV3 {

    /* =====================================================================
     * 枚举
     * ===================================================================== */
    enum Suit {
        CLUBS("梅花"), DIAMONDS("方片"), HEARTS("红桃"), SPADES("黑桃"), JOKER("王");
        final String s;
        Suit(String s) { this.s = s; }
    }

    enum PlayType { SINGLE, PAIR, CONSECUTIVE_PAIR, INVALID }

    /* =====================================================================
     * Card
     * ===================================================================== */
    static class Card {
        final String rank;
        final Suit suit;
        final int copyId;

        Card(String rank, Suit suit, int copyId) {
            this.rank = rank; this.suit = suit; this.copyId = copyId;
        }
        boolean isBigJoker() { return suit == Suit.JOKER && rank.equals("BJ"); }
        boolean isSmallJoker() { return suit == Suit.JOKER && rank.equals("SJ"); }
        boolean isJoker() { return suit == Suit.JOKER; }

        int point() {
            switch (rank) {
                case "5": return 5;
                case "10": case "K": return 10;
                default: return 0;
            }
        }

        boolean isTrump(String level, Suit trump) {
            if (isJoker()) return true;
            if (rank.equals(level)) return true;
            if (rank.equals("2") || rank.equals("3") || rank.equals("5")) return true;
            return suit == trump;
        }

        boolean isFixedTrump() {
            return rank.equals("2") || rank.equals("3") || rank.equals("5");
        }

        @Override public String toString() {
            if (isBigJoker()) return "🃏BJ";
            if (isSmallJoker()) return "🃏SJ";
            return suit.s + rank;
        }
        @Override public boolean equals(Object o) {
            if (!(o instanceof Card)) return false;
            Card c = (Card) o;
            return rank.equals(c.rank) && suit == c.suit && copyId == c.copyId;
        }
        @Override public int hashCode() { return Objects.hash(rank, suit, copyId); }
    }

    /* =====================================================================
     * Deck
     * ===================================================================== */
    static class Deck {
        static final String[] RANKS = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};
        List<Card> cards = new ArrayList<>();

        Deck() {
            for (int copy = 0; copy < 2; copy++) {
                for (Suit s : new Suit[]{Suit.CLUBS, Suit.DIAMONDS, Suit.HEARTS, Suit.SPADES})
                    for (String r : RANKS) cards.add(new Card(r, s, copy));
                cards.add(new Card("SJ", Suit.JOKER, copy));
                cards.add(new Card("BJ", Suit.JOKER, copy));
            }
        }
        void shuffle() {
            Random rnd = new Random();
            for (int i = cards.size() - 1; i > 0; i--) Collections.swap(cards, i, rnd.nextInt(i + 1));
        }
        List<Card> deal(int n) {
            List<Card> r = new ArrayList<>(cards.subList(0, n));
            cards.subList(0, n).clear();
            return r;
        }
    }

    /* =====================================================================
     * RuleEngine
     * ===================================================================== */
    static class RuleEngine {

        /** 副牌排序：A=12 ... 4=3（无 5/3/2，因常主） */
        static int sideOrder(String rank) {
            switch (rank) {
                case "A": return 12; case "K": return 11; case "Q": return 10;
                case "J": return 9;  case "10": return 8; case "9": return 7;
                case "8": return 6;  case "7": return 5;  case "6": return 4;
                case "4": return 3;
                default: return -1;
            }
        }

        /**
         * 主牌排序：
         *   大王=100 小王=99
         *   级牌(主花色)=98 级牌(副花色)=97
         *   常主5=96 常主3=95 常主2=94
         *   主花色普通：A=82,K=81,...,4=73
         */
        static int trumpOrder(Card c, String level, Suit trump) {
            if (c.isBigJoker()) return 100;
            if (c.isSmallJoker()) return 99;
            if (c.rank.equals(level)) return (c.suit == trump) ? 98 : 97;
            if (c.rank.equals("5")) return 96;
            if (c.rank.equals("3")) return 95;
            if (c.rank.equals("2")) return 94;
            return 70 + sideOrder(c.rank);
        }

        /** 用于连对相邻判定：把当前局所有主牌大小排成一条链 */
        static List<Integer> buildTrumpSequence(String level, Suit trump) {
            List<Integer> seq = new ArrayList<>();
            for (String rank : new String[]{"4","6","7","8","9","10","J","Q","K","A"}) {
                if (!rank.equals(level)) seq.add(70 + sideOrder(rank));
            }
            if (!"2".equals(level)) seq.add(94);
            if (!"3".equals(level)) seq.add(95);
            if (!"5".equals(level)) seq.add(96);
            seq.add(97); seq.add(98); seq.add(99); seq.add(100);
            Collections.sort(seq);
            return seq;
        }

        static int compareCard(Card a, Card b, String level, Suit trump) {
            boolean ta = a.isTrump(level, trump), tb = b.isTrump(level, trump);
            if (ta && !tb) return 1;
            if (!ta && tb) return -1;
            if (ta) return Integer.compare(trumpOrder(a, level, trump), trumpOrder(b, level, trump));
            if (a.suit != b.suit) return 0;
            return Integer.compare(sideOrder(a.rank), sideOrder(b.rank));
        }

        static PlayType typeOf(List<Card> play, String level, Suit trump) {
            if (play == null || play.isEmpty()) return PlayType.INVALID;
            if (play.size() == 1) return PlayType.SINGLE;
            if (play.size() == 2 && isPair(play.get(0), play.get(1))) return PlayType.PAIR;
            if (play.size() >= 4 && play.size() % 2 == 0
                    && isConsecutivePairs(play, level, trump))
                return PlayType.CONSECUTIVE_PAIR;
            return PlayType.INVALID;
        }

        static boolean isPair(Card a, Card b) {
            return a.rank.equals(b.rank) && a.suit == b.suit;
        }

        /**
         * 连对判定（v3 修复版）：
         *  - 副牌连对：必须全部同花色 + 全部副牌 + 点数连续
         *  - 主牌连对（v3 关键修复）：必须满足下列任一形式
         *      a) 全部为同一具体花色的主牌（如：红桃 33+44，红桃 K+A 当主花色=红桃时）
         *      b) 全部为王（大王对 / 小王对 / 大王对+小王对四连对，是合法的最大主连对）
         *      c) 常主链段：5+5+3+3、3+3+2+2、5+5+3+3+2+2 — 因常主之间在主链中相邻
         *      d) 级牌(主)+常主5、级牌(副)+级牌(主) 等通过 buildTrumpSequence 相邻判定
         *  禁止：红桃A + 黑桃K 这种跨花色拼接（即使两张都是主牌）
         */
        static boolean isConsecutivePairs(List<Card> play, String level, Suit trump) {
            List<Card> sorted = new ArrayList<>(play);
            boolean allTrump = sorted.stream().allMatch(c -> c.isTrump(level, trump));
            boolean allOneSideSuit = !allTrump
                    && sorted.stream().allMatch(c -> c.suit == sorted.get(0).suit
                    && !c.isTrump(level, trump));
            if (!allTrump && !allOneSideSuit) return false;

            sorted.sort((x, y) -> allTrump
                    ? Integer.compare(trumpOrder(x, level, trump), trumpOrder(y, level, trump))
                    : Integer.compare(sideOrder(x.rank), sideOrder(y.rank)));

            // 每两张必须是同花色同点数对子
            for (int i = 0; i < sorted.size(); i += 2) {
                if (!isPair(sorted.get(i), sorted.get(i + 1))) return false;
            }

            if (allTrump) {
                // v3 修复：主连对必须"同花色簇"。
                // 同花色簇定义：要么全是王，要么全是同一具体花色（含级牌主花色、常主、主花色普通）。
                // 注意：常主（2/3/5）虽然 suit 各异，但若与级牌主花色或主花色普通拼接，
                // 业界普遍裁决"必须是同花色"，跨花色不允许。
                // 我们这里采用严格规则：全部牌的 suit 必须相等（王自成一类）。
                Suit firstSuit = sorted.get(0).suit;
                boolean allSameSuit = sorted.stream().allMatch(c -> c.suit == firstSuit);
                if (!allSameSuit) return false;

                List<Integer> seq = buildTrumpSequence(level, trump);
                int prevIdx = -999;
                for (int i = 0; i < sorted.size(); i += 2) {
                    int val = trumpOrder(sorted.get(i), level, trump);
                    int idx = seq.indexOf(val);
                    if (idx < 0) return false;
                    if (prevIdx != -999 && idx != prevIdx + 1) return false;
                    prevIdx = idx;
                }
            } else {
                int prev = -999;
                for (int i = 0; i < sorted.size(); i += 2) {
                    int v = sideOrder(sorted.get(i).rank);
                    if (prev != -999 && v != prev + 1) return false;
                    prev = v;
                }
            }
            return true;
        }

        static int comparePlay(List<Card> a, List<Card> b, String level, Suit trump, Suit leadSuit) {
            boolean aTrumpAll = a.stream().allMatch(c -> c.isTrump(level, trump));
            boolean bTrumpAll = b.stream().allMatch(c -> c.isTrump(level, trump));
            if (aTrumpAll && !bTrumpAll) return 1;
            if (!aTrumpAll && bTrumpAll) return -1;

            PlayType typeA = typeOf(a, level, trump);
            PlayType typeB = typeOf(b, level, trump);

            if (!aTrumpAll && !bTrumpAll) {
                boolean aMatchSuit = a.stream().allMatch(c -> c.suit == leadSuit);
                boolean bMatchSuit = b.stream().allMatch(c -> c.suit == leadSuit);
                if (!aMatchSuit && !bMatchSuit) return 0;
                if (!aMatchSuit) return -1;
                if (!bMatchSuit) return 1;
            }

            if (typeA != typeB) {
                if (typeA == PlayType.CONSECUTIVE_PAIR) return 1;
                if (typeB == PlayType.CONSECUTIVE_PAIR) return -1;
                if (typeA == PlayType.PAIR) return 1;
                if (typeB == PlayType.PAIR) return -1;
            }

            Card ka = a.stream().max((x, y) -> compareCard(x, y, level, trump)).orElse(null);
            Card kb = b.stream().max((x, y) -> compareCard(x, y, level, trump)).orElse(null);

            if (aTrumpAll) {
                return Integer.compare(trumpOrder(ka, level, trump), trumpOrder(kb, level, trump));
            } else {
                return Integer.compare(sideOrder(ka.rank), sideOrder(kb.rank));
            }
        }

        static List<Card> cardsOfCategory(List<Card> hand, Suit leadSuit, boolean leadTrump,
                                          String level, Suit trump) {
            List<Card> r = new ArrayList<>();
            for (Card c : hand) {
                boolean t = c.isTrump(level, trump);
                if (leadTrump && t) r.add(c);
                else if (!leadTrump && !t && c.suit == leadSuit) r.add(c);
            }
            return r;
        }

        static boolean hasPairInCategory(List<Card> cards) {
            for (int i = 0; i < cards.size(); i++)
                for (int j = i + 1; j < cards.size(); j++)
                    if (isPair(cards.get(i), cards.get(j))) return true;
            return false;
        }

        static boolean hasConsecutivePairInCategory(List<Card> cards, int pairCount,
                                                    String level, Suit trump) {
            if (cards.size() < pairCount * 2) return false;
            // 收集所有候选对子（不同花色簇分别处理）
            Map<Suit, List<Card>> bySuit = new EnumMap<>(Suit.class);
            boolean allTrump = cards.stream().allMatch(c -> c.isTrump(level, trump));
            for (Card c : cards) bySuit.computeIfAbsent(c.suit, k -> new ArrayList<>()).add(c);

            for (Map.Entry<Suit, List<Card>> e : bySuit.entrySet()) {
                List<Card> grp = e.getValue();
                List<int[]> pairs = new ArrayList<>();
                boolean[] used = new boolean[grp.size()];
                for (int i = 0; i < grp.size(); i++) {
                    if (used[i]) continue;
                    for (int j = i + 1; j < grp.size(); j++) {
                        if (used[j]) continue;
                        if (isPair(grp.get(i), grp.get(j))) {
                            pairs.add(new int[]{i, j});
                            used[i] = true; used[j] = true;
                            break;
                        }
                    }
                }
                if (pairs.size() < pairCount) continue;
                List<Integer> pairValues = new ArrayList<>();
                for (int[] p : pairs) {
                    Card c = grp.get(p[0]);
                    pairValues.add(allTrump ? trumpOrder(c, level, trump) : sideOrder(c.rank));
                }
                Collections.sort(pairValues);
                if (allTrump) {
                    List<Integer> seq = buildTrumpSequence(level, trump);
                    List<Integer> idxs = new ArrayList<>();
                    for (int v : pairValues) {
                        int idx = seq.indexOf(v);
                        if (idx >= 0) idxs.add(idx);
                    }
                    Collections.sort(idxs);
                    for (int i = 0; i <= idxs.size() - pairCount; i++) {
                        boolean ok = true;
                        for (int j = 1; j < pairCount; j++)
                            if (idxs.get(i+j) != idxs.get(i) + j) { ok = false; break; }
                        if (ok) return true;
                    }
                } else {
                    for (int i = 0; i <= pairValues.size() - pairCount; i++) {
                        boolean ok = true;
                        for (int j = 1; j < pairCount; j++)
                            if (pairValues.get(i+j) != pairValues.get(i) + j) { ok = false; break; }
                        if (ok) return true;
                    }
                }
            }
            return false;
        }

        static String validatePlay(List<Card> hand, List<Card> play, List<Card> leadPlay,
                                   String level, Suit trump) {
            if (play == null || play.isEmpty()) return "至少出一张";
            if (!hand.containsAll(play)) return "出的牌不在手牌中";
            if (leadPlay == null) {
                if (typeOf(play, level, trump) == PlayType.INVALID)
                    return "首出牌型无效（仅限单张/对子/连对）";
                return null;
            }

            boolean leadTrump = leadPlay.stream().allMatch(c -> c.isTrump(level, trump));
            Suit leadSuit = leadTrump ? trump : leadPlay.get(0).suit;
            PlayType leadType = typeOf(leadPlay, level, trump);
            List<Card> sameCat = cardsOfCategory(hand, leadSuit, leadTrump, level, trump);
            int need = leadPlay.size();

            if (play.size() != need) return "出牌张数必须等于" + need;

            List<Card> playInCat = new ArrayList<>();
            List<Card> playOther = new ArrayList<>();
            for (Card c : play) {
                boolean t = c.isTrump(level, trump);
                if (leadTrump ? t : (!t && c.suit == leadSuit)) playInCat.add(c);
                else playOther.add(c);
            }

            int maxCat = Math.min(sameCat.size(), need);
            if (playInCat.size() < maxCat)
                return "必须先跟" + (leadTrump ? "主牌" : leadSuit.s) + "，至少" + maxCat + "张";

            if (sameCat.size() >= need && !playOther.isEmpty())
                return "同花色牌足够时不允许贴其他花色/主牌";

            if (sameCat.size() >= need) {
                if (leadType == PlayType.PAIR && hasPairInCategory(sameCat)) {
                    if (typeOf(play, level, trump) != PlayType.PAIR)
                        return "你有同花色对子，必须出对子";
                }
                if (leadType == PlayType.CONSECUTIVE_PAIR &&
                        hasConsecutivePairInCategory(sameCat, need / 2, level, trump)) {
                    if (typeOf(play, level, trump) != PlayType.CONSECUTIVE_PAIR)
                        return "你有同花色连对，必须出连对";
                }
            }
            return null;
        }
    }

    /* =====================================================================
     * Player
     * ===================================================================== */
    static class Player {
        final String name;
        final boolean isHuman;
        final int seat;
        List<Card> hand = new ArrayList<>();
        Player teammate;

        Player(String name, boolean h, int seat) { this.name = name; this.isHuman = h; this.seat = seat; }

        void receive(List<Card> cs) { hand.addAll(cs); }

        /**
         * v3 排序（严格按文档第十四章）：
         *   1. 大王
         *   2. 小王
         *   3. 当前级牌（主花色优先于副花色）
         *   4. 常主 5 → 3 → 2
         *   5. 主花色普通牌 A→K→...→4
         *   6. 副牌：按花色分组，组内 A→K→...→4
         */
        void sortHand(String level, Suit trump) {
            hand.sort((a, b) -> {
                int ka = sortKey(a, level, trump);
                int kb = sortKey(b, level, trump);
                if (ka != kb) return Integer.compare(ka, kb);
                // 相同段内：主牌按 trumpOrder 降序，副牌按 sideOrder 降序
                boolean ta = a.isTrump(level, trump);
                if (ta) {
                    return Integer.compare(RuleEngine.trumpOrder(b, level, trump),
                            RuleEngine.trumpOrder(a, level, trump));
                } else {
                    int s = Integer.compare(a.suit.ordinal(), b.suit.ordinal());
                    if (s != 0) return s;
                    return Integer.compare(RuleEngine.sideOrder(b.rank), RuleEngine.sideOrder(a.rank));
                }
            });
        }

        /** 段优先级：值越小越靠前 */
        private static int sortKey(Card c, String level, Suit trump) {
            if (c.isBigJoker()) return 0;
            if (c.isSmallJoker()) return 1;
            if (level != null && c.rank.equals(level)) return (c.suit == trump) ? 2 : 3;
            if (c.rank.equals("5")) return 4;
            if (c.rank.equals("3")) return 5;
            if (c.rank.equals("2")) return 6;
            if (trump != null && c.suit == trump) return 7;
            return 8;
        }

        void show(String level, Suit trump) {
            if (level == null || trump == null) {
                StringBuilder sb = new StringBuilder(name + "(" + hand.size() + "): ");
                for (int i = 0; i < hand.size(); i++)
                    sb.append("[").append(i).append("]").append(hand.get(i)).append(" ");
                System.out.println(sb);
                return;
            }
            StringBuilder sb = new StringBuilder(name + "(" + hand.size() + ")\n  主牌: ");
            boolean any = false;
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).isTrump(level, trump)) {
                    sb.append("[").append(i).append("]").append(hand.get(i)).append(" ");
                    any = true;
                }
            }
            if (!any) sb.append("（无）");
            sb.append("\n  副牌: ");
            Suit lastSuit = null;
            any = false;
            for (int i = 0; i < hand.size(); i++) {
                Card c = hand.get(i);
                if (c.isTrump(level, trump)) continue;
                if (lastSuit != null && c.suit != lastSuit) sb.append(" | ");
                sb.append("[").append(i).append("]").append(c).append(" ");
                lastSuit = c.suit; any = true;
            }
            if (!any) sb.append("（无）");
            System.out.println(sb);
        }
    }

    /* =====================================================================
     * ScoreEngine（v3 修复区间）
     * ===================================================================== */
    static class ScoreEngine {
        /**
         * 严格按 upgrade_poker_rules_enhanced_v_2.md §3.3：
         *   0           庄升 3
         *   5–35（小光）庄升 3
         *   40–75       庄升 1
         *   80–115      换庄不升级
         *   120–155     闲升 1
         *   160–195     闲升 2
         *   200（大倒） 闲升 3
         * 返回 [庄家是否守住, 升级数, 是否换庄]
         */
        static int[] settle(int farmerScore) {
            farmerScore = Math.max(0, Math.min(200, farmerScore));
            if (farmerScore == 0)        return new int[]{1, 3, 0};
            if (farmerScore <= 35)       return new int[]{1, 3, 0}; // v3 修复：5-35 升 3 级
            if (farmerScore <= 75)       return new int[]{1, 1, 0};
            if (farmerScore <= 115)      return new int[]{0, 0, 1}; // 换庄不升级
            if (farmerScore <= 155)      return new int[]{0, 1, 1};
            if (farmerScore <= 195)      return new int[]{0, 2, 1};
            return new int[]{0, 3, 1};
        }

        static int applyBottom(int farmerScore, boolean farmerWinLastTrick,
                               List<Card> lastPlay, List<Card> bottom,
                               String level, Suit trump) {
            int bp = bottom.stream().mapToInt(Card::point).sum();
            boolean trumpPlay = lastPlay.stream().allMatch(c -> c.isTrump(level, trump));
            if (!trumpPlay) return Math.max(0, Math.min(200, farmerScore)); // 副牌扣底不增减

            int mul;
            switch (RuleEngine.typeOf(lastPlay, level, trump)) {
                case PAIR: mul = 2; break;
                case CONSECUTIVE_PAIR: mul = lastPlay.size() / 2; break;
                case SINGLE: default: mul = 1;
            }
            int delta = bp * mul;
            if (farmerWinLastTrick) farmerScore += delta;
            else farmerScore -= delta;
            return Math.max(0, Math.min(200, farmerScore));
        }
    }

    /* =====================================================================
     * AiStrategy 接口
     * ===================================================================== */
    interface AiStrategy {
        List<Card> chooseLead(Player p, String level, Suit trump, AiContext ctx);
        List<Card> chooseFollow(Player p, List<Card> leadPlay, String level, Suit trump,
                                Player leader, Player currentBest, List<Card> bestPlay, AiContext ctx);
        /**
         * 选择扣底 4 张。
         * @param bidder 本局亮主玩家（憋牌局可能为 null）
         * @param bidLevelTier 亮主/反主等级（1=单王 ... 6=四连对，0=未知/憋牌）
         */
        List<Card> chooseBottom(Player p, String level, Suit trump, Player bidder, int bidLevelTier);
        Suit pickTrumpSuit(Player p, String level);
        boolean shouldBid(Player p, int cardCount);
        boolean shouldCounter(Player p);
    }

    /** AI 共享上下文：断门记忆、出牌历史等 */
    static class AiContext {
        // lackSuit[seat] = 该玩家已断的副花色集合
        Map<Integer, Set<Suit>> lackSuit = new HashMap<>();
        // 主牌是否断：trumpExhausted[seat]
        Map<Integer, Boolean> trumpExhausted = new HashMap<>();
        AiContext() {
            for (int i = 0; i < 4; i++) {
                lackSuit.put(i, EnumSet.noneOf(Suit.class));
                trumpExhausted.put(i, false);
            }
        }
        void recordPlay(Player p, List<Card> leadPlay, List<Card> play,
                        String level, Suit trump) {
            if (leadPlay == null || leadPlay == play) return;
            boolean leadTrump = leadPlay.stream().allMatch(c -> c.isTrump(level, trump));
            Suit leadSuit = leadTrump ? trump : leadPlay.get(0).suit;
            // 玩家 p 出的牌中，若没有任何一张属于 leadCategory，则视为断门
            boolean anyInCat = play.stream().anyMatch(c -> {
                boolean t = c.isTrump(level, trump);
                return leadTrump ? t : (!t && c.suit == leadSuit);
            });
            if (!anyInCat) {
                if (leadTrump) trumpExhausted.put(p.seat, true);
                else lackSuit.get(p.seat).add(leadSuit);
            }
        }
    }

    /* =====================================================================
     * RuleBasedAI（含赶主、断门记忆、放分保分策略）
     * ===================================================================== */
    static class RuleBasedAI implements AiStrategy {

        @Override
        public List<Card> chooseLead(Player p, String level, Suit trump, AiContext ctx) {
            List<Card> hand = p.hand;
            // 1) 副牌 A 且无同花色分牌 → 抢分先手
            for (Card a : hand) {
                if (a.rank.equals("A") && !a.isTrump(level, trump)) {
                    Suit s = a.suit;
                    boolean hasScore = hand.stream().anyMatch(c ->
                            !c.isTrump(level, trump) && c.suit == s && c.point() > 0 && !c.equals(a));
                    if (!hasScore) return Collections.singletonList(a);
                }
            }
            // 2) 若对手疑似缺主、自己主多 → 调主
            if (ctx != null) {
                long myTrump = hand.stream().filter(c -> c.isTrump(level, trump)).count();
                if (myTrump >= 8) {
                    List<Card> tpair = findPair(hand, level, trump, true);
                    if (tpair != null) return tpair;
                    Card maxT = hand.stream().filter(c -> c.isTrump(level, trump))
                            .max((x, y) -> Integer.compare(
                                    RuleEngine.trumpOrder(x, level, trump),
                                    RuleEngine.trumpOrder(y, level, trump)))
                            .orElse(null);
                    if (maxT != null) return Collections.singletonList(maxT);
                }
            }
            // 3) 副牌对子
            List<Card> pair = findPair(hand, level, trump, false);
            if (pair != null) return pair;
            // 4) 副牌连对
            List<Card> consec = findConsecutivePair(hand, level, trump, false);
            if (consec != null) return consec;
            // 5) 主对子
            List<Card> tpair = findPair(hand, level, trump, true);
            if (tpair != null) return tpair;
            // 6) 最小副牌
            Card min = null;
            for (Card c : hand) {
                if (c.isTrump(level, trump)) continue;
                if (min == null || RuleEngine.sideOrder(c.rank) < RuleEngine.sideOrder(min.rank)) min = c;
            }
            if (min != null) return Collections.singletonList(min);
            // 7) 最小主牌
            Card minT = hand.stream().min((x, y) -> Integer.compare(
                    RuleEngine.trumpOrder(x, level, trump),
                    RuleEngine.trumpOrder(y, level, trump))).orElse(hand.get(0));
            return Collections.singletonList(minT);
        }

        private List<Card> findPair(List<Card> hand, String level, Suit trump, boolean wantTrump) {
            for (int i = 0; i < hand.size(); i++)
                for (int j = i + 1; j < hand.size(); j++) {
                    Card a = hand.get(i), b = hand.get(j);
                    if (RuleEngine.isPair(a, b) && a.isTrump(level, trump) == wantTrump)
                        return Arrays.asList(a, b);
                }
            return null;
        }

        private List<Card> findConsecutivePair(List<Card> hand, String level, Suit trump, boolean wantTrump) {
            // 按"花色簇"分组（v3：主连对必须同花色）
            Map<Suit, List<Card>> groups = new EnumMap<>(Suit.class);
            for (Card c : hand) {
                boolean t = c.isTrump(level, trump);
                if (t == wantTrump) groups.computeIfAbsent(c.suit, k -> new ArrayList<>()).add(c);
            }
            for (List<Card> group : groups.values()) {
                List<Card[]> pairs = new ArrayList<>();
                boolean[] used = new boolean[group.size()];
                for (int i = 0; i < group.size(); i++) {
                    if (used[i]) continue;
                    for (int j = i + 1; j < group.size(); j++) {
                        if (used[j]) continue;
                        if (RuleEngine.isPair(group.get(i), group.get(j))) {
                            pairs.add(new Card[]{group.get(i), group.get(j)});
                            used[i] = true; used[j] = true; break;
                        }
                    }
                }
                if (pairs.size() < 2) continue;
                pairs.sort((a, b) -> {
                    int va = wantTrump ? RuleEngine.trumpOrder(a[0], level, trump) : RuleEngine.sideOrder(a[0].rank);
                    int vb = wantTrump ? RuleEngine.trumpOrder(b[0], level, trump) : RuleEngine.sideOrder(b[0].rank);
                    return Integer.compare(va, vb);
                });
                List<Integer> seq = wantTrump ? RuleEngine.buildTrumpSequence(level, trump) : null;
                for (int i = 0; i < pairs.size() - 1; i++) {
                    int va = wantTrump ? RuleEngine.trumpOrder(pairs.get(i)[0], level, trump) : RuleEngine.sideOrder(pairs.get(i)[0].rank);
                    int vb = wantTrump ? RuleEngine.trumpOrder(pairs.get(i+1)[0], level, trump) : RuleEngine.sideOrder(pairs.get(i+1)[0].rank);
                    boolean adj;
                    if (wantTrump) {
                        int ia = seq.indexOf(va), ib = seq.indexOf(vb);
                        adj = ia >= 0 && ib >= 0 && ib == ia + 1;
                    } else {
                        adj = vb == va + 1;
                    }
                    if (adj) {
                        List<Card> r = new ArrayList<>();
                        r.add(pairs.get(i)[0]); r.add(pairs.get(i)[1]);
                        r.add(pairs.get(i+1)[0]); r.add(pairs.get(i+1)[1]);
                        return r;
                    }
                }
            }
            return null;
        }

        @Override
        public List<Card> chooseFollow(Player p, List<Card> leadPlay, String level, Suit trump,
                                       Player leader, Player currentBest, List<Card> bestPlay, AiContext ctx) {
            List<Card> hand = p.hand;
            boolean leadTrump = leadPlay.stream().allMatch(c -> c.isTrump(level, trump));
            Suit leadSuit = leadTrump ? trump : leadPlay.get(0).suit;
            PlayType leadType = RuleEngine.typeOf(leadPlay, level, trump);
            List<Card> sameCat = RuleEngine.cardsOfCategory(hand, leadSuit, leadTrump, level, trump);
            int need = leadPlay.size();
            boolean teammateWinning = currentBest != null && currentBest.teammate == p;

            // ============== 同类别足够 ==============
            if (sameCat.size() >= need) {
                sameCat.sort(Comparator.comparingInt(c -> orderKey(c, level, trump)));
                if (teammateWinning) {
                    // 队友大：放分（垫最大分牌）
                    List<Card> sortedScoreFirst = new ArrayList<>(sameCat);
                    sortedScoreFirst.sort((a, b) -> {
                        int pa = a.point(), pb = b.point();
                        if (pa != pb) return Integer.compare(pb, pa);
                        return Integer.compare(orderKey(a, level, trump), orderKey(b, level, trump));
                    });
                    return new ArrayList<>(sortedScoreFirst.subList(0, need));
                }
                if (leadType == PlayType.PAIR && RuleEngine.hasPairInCategory(sameCat)) {
                    List<Card> bestPair = findSmallestWinningPair(sameCat, bestPlay, level, trump, leadSuit);
                    if (bestPair != null) return bestPair;
                    List<Card> smallPair = findSmallestPair(sameCat);
                    return smallPair != null ? smallPair : new ArrayList<>(sameCat.subList(0, need));
                }
                if (leadType == PlayType.CONSECUTIVE_PAIR &&
                        RuleEngine.hasConsecutivePairInCategory(sameCat, need / 2, level, trump)) {
                    List<Card> weak = findWeakestConsecutive(sameCat, need / 2, level, trump);
                    if (weak != null) return weak;
                }
                /* ============== v3.1 赶主规则（§11.1） ==============
                 * 首出为主对/主连对，本家同类别足够但无法配对应牌型时：
                 *   - 主连对首出：先尽量保留对子结构（小对优先用掉，留大对应付下一墩）
                 *   - 剩余空位用单张主牌"从大到小"补齐（赶主，消耗大主）
                 *   - 主对首出且无对子：直接取最大两张主牌
                 * 同类别"主牌"已包含王、级牌、常主、主花色，故规则覆盖固定主和亮主主花色。
                 */
                if (leadTrump && (leadType == PlayType.PAIR
                        || leadType == PlayType.CONSECUTIVE_PAIR)) {
                    List<Card> chase = new ArrayList<>();
                    // 1) 主连对首出时，先用我方所有非连小对（不拆大对）
                    if (leadType == PlayType.CONSECUTIVE_PAIR && need >= 4) {
                        List<List<Card>> myPairs = new ArrayList<>();
                        boolean[] used = new boolean[sameCat.size()];
                        for (int i = 0; i < sameCat.size(); i++) {
                            if (used[i]) continue;
                            for (int j = i + 1; j < sameCat.size(); j++) {
                                if (used[j]) continue;
                                if (RuleEngine.isPair(sameCat.get(i), sameCat.get(j))) {
                                    myPairs.add(Arrays.asList(sameCat.get(i), sameCat.get(j)));
                                    used[i] = true; used[j] = true; break;
                                }
                            }
                        }
                        myPairs.sort((a, b) -> Integer.compare(
                                orderKey(a.get(0), level, trump),
                                orderKey(b.get(0), level, trump)));
                        for (List<Card> pair : myPairs) {
                            if (chase.size() + 2 > need) break;
                            chase.addAll(pair);
                        }
                    }
                    // 2) 剩余空位：从大到小取主牌（赶主）
                    List<Card> singles = new ArrayList<>();
                    for (Card c : sameCat) if (!chase.contains(c)) singles.add(c);
                    singles.sort((a, b) -> Integer.compare(
                            orderKey(b, level, trump),
                            orderKey(a, level, trump)));
                    for (Card c : singles) {
                        if (chase.size() >= need) break;
                        chase.add(c);
                    }
                    return chase;
                }
                List<Card> sortedDesc = new ArrayList<>(sameCat);
                Collections.reverse(sortedDesc);
                if (RuleEngine.comparePlay(sortedDesc.subList(0, need), bestPlay, level, trump, leadSuit) > 0) {
                    for (int i = sameCat.size() - need; i >= 0; i--) {
                        List<Card> cand = new ArrayList<>(sameCat.subList(i, i + need));
                        if (RuleEngine.comparePlay(cand, bestPlay, level, trump, leadSuit) > 0) return cand;
                    }
                }
                return new ArrayList<>(sameCat.subList(0, need));
            }

            // ============== 同类别不足 ==============
            List<Card> result = new ArrayList<>(sameCat);
            List<Card> remain = new ArrayList<>(hand);
            remain.removeAll(sameCat);

            int tableScore = bestPlay.stream().mapToInt(Card::point).sum()
                    + leadPlay.stream().mapToInt(Card::point).sum();

            // 副牌且场上有分、队友未控、自己有足够主牌 → 主牌毙
            if (!leadTrump && tableScore > 0 && !teammateWinning) {
                List<Card> trumps = remain.stream().filter(c -> c.isTrump(level, trump))
                        .sorted(Comparator.comparingInt(c -> RuleEngine.trumpOrder(c, level, trump)))
                        .collect(Collectors.toList());
                if (trumps.size() >= need - result.size()) {
                    // 优先用主对毙主对，主连对毙主连对
                    if (leadType == PlayType.PAIR && need - result.size() == 2) {
                        List<Card> tp = findSmallestPair(trumps);
                        if (tp != null && RuleEngine.comparePlay(tp, bestPlay, level, trump, leadSuit) > 0) {
                            result.addAll(tp);
                            return result;
                        }
                    }
                    while (result.size() < need && !trumps.isEmpty())
                        result.add(trumps.remove(0));
                    return result;
                }
            }

            // v3 关键："赶主"——主连对/主对/主散在跟主时缺位，按从大到小补
            if (leadTrump && sameCat.size() < need) {
                List<Card> trumpsAll = remain.stream().filter(c -> c.isTrump(level, trump))
                        .sorted((a, b) -> Integer.compare(
                                RuleEngine.trumpOrder(b, level, trump),
                                RuleEngine.trumpOrder(a, level, trump)))
                        .collect(Collectors.toList());
                while (result.size() < need && !trumpsAll.isEmpty())
                    result.add(trumpsAll.remove(0)); // 严格从大到小
                remain.removeAll(result);
                remain.sort(Comparator.comparingInt(c -> orderKey(c, level, trump)));
                for (Card c : remain) {
                    if (result.size() >= need) break;
                    result.add(c);
                }
                return result;
            }

            // 默认：垫最小（含分牌时若队友未控也尽量不垫分）
            remain.sort((a, b) -> {
                if (!teammateWinning) {
                    int pa = a.point(), pb = b.point();
                    if (pa != pb) return Integer.compare(pa, pb); // 优先垫不带分的
                }
                return Integer.compare(orderKey(a, level, trump), orderKey(b, level, trump));
            });
            for (Card c : remain) {
                if (result.size() >= need) break;
                result.add(c);
            }
            return result;
        }

        private List<Card> findWeakestConsecutive(List<Card> cards, int pairCount,
                                                  String level, Suit trump) {
            // 按花色簇查找
            Map<Suit, List<Card>> bySuit = new EnumMap<>(Suit.class);
            for (Card c : cards) bySuit.computeIfAbsent(c.suit, k -> new ArrayList<>()).add(c);
            boolean allTrump = cards.stream().allMatch(c -> c.isTrump(level, trump));
            for (List<Card> grp : bySuit.values()) {
                List<Card[]> pairs = new ArrayList<>();
                boolean[] used = new boolean[grp.size()];
                for (int i = 0; i < grp.size(); i++) {
                    if (used[i]) continue;
                    for (int j = i + 1; j < grp.size(); j++) {
                        if (used[j]) continue;
                        if (RuleEngine.isPair(grp.get(i), grp.get(j))) {
                            pairs.add(new Card[]{grp.get(i), grp.get(j)});
                            used[i] = true; used[j] = true; break;
                        }
                    }
                }
                if (pairs.size() < pairCount) continue;
                pairs.sort((a, b) -> {
                    int va = allTrump ? RuleEngine.trumpOrder(a[0], level, trump) : RuleEngine.sideOrder(a[0].rank);
                    int vb = allTrump ? RuleEngine.trumpOrder(b[0], level, trump) : RuleEngine.sideOrder(b[0].rank);
                    return Integer.compare(va, vb);
                });
                List<Integer> seq = allTrump ? RuleEngine.buildTrumpSequence(level, trump) : null;
                for (int i = 0; i <= pairs.size() - pairCount; i++) {
                    boolean ok = true;
                    for (int j = 1; j < pairCount; j++) {
                        int va = allTrump ? RuleEngine.trumpOrder(pairs.get(i+j-1)[0], level, trump)
                                : RuleEngine.sideOrder(pairs.get(i+j-1)[0].rank);
                        int vb = allTrump ? RuleEngine.trumpOrder(pairs.get(i+j)[0], level, trump)
                                : RuleEngine.sideOrder(pairs.get(i+j)[0].rank);
                        boolean adj;
                        if (allTrump) {
                            int ia = seq.indexOf(va), ib = seq.indexOf(vb);
                            adj = ia >= 0 && ib >= 0 && ib == ia + 1;
                        } else {
                            adj = vb == va + 1;
                        }
                        if (!adj) { ok = false; break; }
                    }
                    if (ok) {
                        List<Card> r = new ArrayList<>();
                        for (int j = 0; j < pairCount; j++) {
                            r.add(pairs.get(i+j)[0]); r.add(pairs.get(i+j)[1]);
                        }
                        return r;
                    }
                }
            }
            return null;
        }

        private List<Card> findSmallestWinningPair(List<Card> sameCat, List<Card> bestPlay,
                                                   String level, Suit trump, Suit leadSuit) {
            List<Card[]> pairs = new ArrayList<>();
            boolean[] used = new boolean[sameCat.size()];
            for (int i = 0; i < sameCat.size(); i++) {
                if (used[i]) continue;
                for (int j = i + 1; j < sameCat.size(); j++) {
                    if (used[j]) continue;
                    if (RuleEngine.isPair(sameCat.get(i), sameCat.get(j))) {
                        pairs.add(new Card[]{sameCat.get(i), sameCat.get(j)});
                        used[i] = true; used[j] = true; break;
                    }
                }
            }
            pairs.sort(Comparator.comparingInt(p -> orderKey(p[0], level, trump)));
            for (Card[] pair : pairs) {
                List<Card> cand = Arrays.asList(pair[0], pair[1]);
                if (RuleEngine.comparePlay(cand, bestPlay, level, trump, leadSuit) > 0) return cand;
            }
            return null;
        }

        private List<Card> findSmallestPair(List<Card> cards) {
            for (int i = 0; i < cards.size(); i++)
                for (int j = i + 1; j < cards.size(); j++)
                    if (RuleEngine.isPair(cards.get(i), cards.get(j)))
                        return Arrays.asList(cards.get(i), cards.get(j));
            return null;
        }

        private int orderKey(Card c, String level, Suit trump) {
            if (c.isTrump(level, trump)) return RuleEngine.trumpOrder(c, level, trump);
            return RuleEngine.sideOrder(c.rank);
        }

        @Override
        public List<Card> chooseBottom(Player p, String level, Suit trump,
                                       Player bidder, int bidLevelTier) {
            final int NEED = 4;
            // 禁扣：王、级牌、常主 2/3/5（规则 §8.1）
            List<Card> candidates = p.hand.stream().filter(c ->
                            !c.isJoker() && !c.rank.equals(level) && !c.isFixedTrump())
                    .collect(Collectors.toList());

            // 己方亮主：bidder 是自己或队友；憋牌局 bidder == null 视为对家路径（保守平衡）
            boolean ownSideBid = bidder != null && (bidder == p || bidder.teammate == p);
            // 牌好：① 主对 ≥ 2 副；② 队友连对（姊妹对及以上）亮/反主
            boolean handIsGood = isHandGood(p, level, trump, bidder, bidLevelTier);

            List<Card> result = new ArrayList<>();

            /* ============== 己方亮主：进攻型 §11.4 ============== */
            if (ownSideBid) {
                // 第一步：优先扣"绝一门"——按副牌花色张数升序，能完整扣掉就扣掉
                Map<Suit, List<Card>> nonTrumpBySuit = new EnumMap<>(Suit.class);
                for (Card c : candidates) {
                    if (!c.isTrump(level, trump)) {
                        nonTrumpBySuit.computeIfAbsent(c.suit, k -> new ArrayList<>()).add(c);
                    }
                }
                List<Map.Entry<Suit, List<Card>>> sortedBySize = nonTrumpBySuit.entrySet().stream()
                        .sorted(Comparator.comparingInt(e -> e.getValue().size()))
                        .collect(Collectors.toList());
                for (Map.Entry<Suit, List<Card>> e : sortedBySize) {
                    List<Card> suitCards = e.getValue();
                    if (suitCards.isEmpty()) continue;
                    if (suitCards.size() <= NEED - result.size()) {
                        // 这门花色全部非主牌都能放进底牌 → 实现"绝门"
                        result.addAll(suitCards);
                    }
                }
            }

            /* ============== 第二步：填空——根据 handIsGood 决定分牌取舍 ==============
             * 公共原则：优先非主牌（少扣主），主牌作为最后兜底。
             * 己方/对家路径在该步规则一致：
             *   - 牌好 → 优先扣分牌（10/K/5），底牌翻倍变成自家收益的概率高
             *   - 牌不好 → 尽量少扣分牌，避免送分给对手
             */
            List<Card> remaining = candidates.stream()
                    .filter(c -> !result.contains(c))
                    .sorted((a, b) -> {
                        int ta = a.isTrump(level, trump) ? 1 : 0;
                        int tb = b.isTrump(level, trump) ? 1 : 0;
                        if (ta != tb) return Integer.compare(ta, tb); // 副牌优先扣
                        int pa = a.point(), pb = b.point();
                        if (pa != pb)
                            return handIsGood ? Integer.compare(pb, pa) : Integer.compare(pa, pb);
                        return Integer.compare(orderKey(a, level, trump), orderKey(b, level, trump));
                    })
                    .collect(Collectors.toList());
            for (Card c : remaining) {
                if (result.size() >= NEED) break;
                result.add(c);
            }
            return new ArrayList<>(result);
        }

        /** 判断"自己牌好"：①本家主对 ≥ 2 副；②队友以姊妹对/三连对/四连对亮主或反主。 */
        private boolean isHandGood(Player p, String level, Suit trump,
                                   Player bidder, int bidLevelTier) {
            // 统计同 rank+suit 出现 ≥ 2 次（双副中的天然对子）
            Map<String, Integer> trumpRankSuit = new HashMap<>();
            for (Card c : p.hand) {
                if (c.isTrump(level, trump)) {
                    trumpRankSuit.merge(c.rank + "_" + c.suit.name(), 1, Integer::sum);
                }
            }
            long pairs = trumpRankSuit.values().stream().filter(v -> v >= 2).count();
            if (pairs >= 2) return true;
            // 队友连对路径亮/顶主（tier 4 = 姊妹对，5 = 三连对，6 = 四连对）
            if (bidder != null && p.teammate == bidder && bidLevelTier >= 4) return true;
            return false;
        }

        @Override
        public Suit pickTrumpSuit(Player p, String level) {
            Map<Suit, Integer> cnt = new EnumMap<>(Suit.class);
            for (Card c : p.hand)
                if (!c.isJoker()) cnt.merge(c.suit, 1, Integer::sum);
            return cnt.entrySet().stream().max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey).orElse(Suit.SPADES);
        }

        @Override
        public boolean shouldBid(Player p, int cardCount) {
            if (cardCount < 10) return false;
            Map<Suit, Integer> cnt = new EnumMap<>(Suit.class);
            for (Card c : p.hand)
                if (!c.isJoker()) cnt.merge(c.suit, 1, Integer::sum);
            int maxSuitCount = cnt.values().stream().mapToInt(i -> i).max().orElse(0);
            return maxSuitCount >= 5 && Math.random() < 0.5;
        }

        @Override
        public boolean shouldCounter(Player p) { return Math.random() < 0.3; }
    }

    /* =====================================================================
     * 亮主竞标工具
     * ===================================================================== */
    static int bidRankOrder(String rank) {
        switch (rank) {
            case "4": return 1; case "6": return 2; case "7": return 3;
            case "8": return 4; case "9": return 5; case "10": return 6;
            case "J": return 7; case "Q": return 8; case "K": return 9;
            case "A": return 10;
            default: return -1;
        }
    }

    static List<Card[]> findSuitPairsForBid(List<Card> hand, Suit suit) {
        Map<String, List<Card>> groups = new LinkedHashMap<>();
        for (Card c : hand) {
            if (c.isJoker() || c.suit != suit) continue;
            if (bidRankOrder(c.rank) < 0) continue;
            groups.computeIfAbsent(c.rank, k -> new ArrayList<>()).add(c);
        }
        List<Card[]> pairs = new ArrayList<>();
        for (Map.Entry<String, List<Card>> e : groups.entrySet()) {
            if (e.getValue().size() >= 2)
                pairs.add(new Card[]{e.getValue().get(0), e.getValue().get(1)});
        }
        pairs.sort(Comparator.comparingInt(p -> bidRankOrder(p[0].rank)));
        return pairs;
    }

    static List<Card> findNConsecutivePairsForBid(List<Card[]> sortedPairs, int count) {
        if (sortedPairs.size() < count) return null;
        for (int i = 0; i <= sortedPairs.size() - count; i++) {
            boolean ok = true;
            for (int j = 1; j < count; j++) {
                int prev = bidRankOrder(sortedPairs.get(i + j - 1)[0].rank);
                int curr = bidRankOrder(sortedPairs.get(i + j)[0].rank);
                if (curr != prev + 1) { ok = false; break; }
            }
            if (ok) {
                List<Card> result = new ArrayList<>();
                for (int j = 0; j < count; j++) {
                    result.add(sortedPairs.get(i + j)[0]);
                    result.add(sortedPairs.get(i + j)[1]);
                }
                return result;
            }
        }
        return null;
    }

    /* =====================================================================
     * Game
     * ===================================================================== */
    static class Game {
        Player[] players = new Player[4];
        int dealerSeat = 0;
        int teamALevelIdx = 0;
        int teamBLevelIdx = 0;
        Suit trump;
        String level;
        List<Card> bottom = new ArrayList<>();
        Scanner sc = new Scanner(System.in);
        AiStrategy ai = new RuleBasedAI();

        int consecutiveNoUpgrade = 0;
        static final int DEADLOCK_THRESHOLD = 8;
        static final String[] LEVEL_ORDER = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};
        // 必打级索引：10=9, J=10, K=12
        static final Set<Integer> MUST_PLAY_IDX = new HashSet<>(Arrays.asList(9, 10, 12));

        Player bidder = null;
        Card bidJoker = null;
        List<Card> bidPairCards = null;
        /** v3 区分亮主形式：1=单王, 2=对王, 3=对子, 4=姊妹对, 5=三连对, 6=四连对 */
        int bidLevelTier = 0;

        Game(String[] names) {
            for (int i = 0; i < 4; i++)
                players[i] = new Player(names[i], i == 0, i);
            players[0].teammate = players[2]; players[2].teammate = players[0];
            players[1].teammate = players[3]; players[3].teammate = players[1];
        }

        boolean teamA(Player p) { return p.seat % 2 == 0; }
        int currentLevelIdx() { return teamA(players[dealerSeat]) ? teamALevelIdx : teamBLevelIdx; }
        String currentLevel() { return LEVEL_ORDER[Math.min(currentLevelIdx(), 12)]; }

        void play() {
            int round = 1;
            while (teamALevelIdx < LEVEL_ORDER.length && teamBLevelIdx < LEVEL_ORDER.length) {
                System.out.println("\n========= 第 " + round + " 局 =========");
                System.out.println("庄家：" + players[dealerSeat].name
                        + "  级牌：" + currentLevel()
                        + "  甲队级:" + LEVEL_ORDER[Math.min(teamALevelIdx, 12)]
                        + "  乙队级:" + LEVEL_ORDER[Math.min(teamBLevelIdx, 12)]);
                int farmer = playOneDeal();
                applySettle(farmer);

                // v3 显式：打过 K 即胜
                if (teamALevelIdx >= LEVEL_ORDER.length || teamBLevelIdx >= LEVEL_ORDER.length) break;

                round++;
                if (round > 100) { System.out.println("超过 100 局自动结束"); break; }
            }
            int winnerTeam = teamALevelIdx >= LEVEL_ORDER.length ? 0 : 1;
            System.out.println("\n🎉 " + (winnerTeam == 0 ? "甲队（0/2）" : "乙队（1/3）") + " 获胜！");
        }

        int playOneDeal() {
            Deck deck = new Deck();
            deck.shuffle();
            for (Player p : players) p.hand.clear();
            level = currentLevel();
            bottom = deck.deal(4);

            bidder = null; bidJoker = null; bidPairCards = null; bidLevelTier = 0; trump = null;

            System.out.println("\n[发牌 + 边发边亮主]");
            for (int round = 0; round < 26; round++) {
                for (int k = 0; k < 4; k++) {
                    Player p = players[(dealerSeat + 1 + k) % 4];
                    p.receive(deck.deal(1));
                }
                checkBiddingRound(round + 1);
            }

            int bottomHandlerSeat;
            if (bidder == null) {
                /* ============== v3 严格憋牌流程 §7 ============== */
                bottomHandlerSeat = handleNoBidScenario();
            } else {
                dealerSeat = bidder.seat;
                bottomHandlerSeat = bidder.teammate.seat;
                System.out.println("最终主：" + trump.s
                        + "，庄家：" + players[dealerSeat].name
                        + "，收底方：" + players[bottomHandlerSeat].name);
            }

            bottomExchangePhase(bottomHandlerSeat);
            claimBottomBySameSuit();

            int farmerScore = 0;
            int leaderSeat = bottomHandlerSeat;
            List<Card> lastPlay = null;
            boolean farmerWinLast = false;

            for (Player p : players) p.sortHand(level, trump);

            AiContext ctx = new AiContext();
            int trick = 1;

            // v3 §12.2 严格结束条件：所有玩家手牌全部为空才结束
            while (!allPlayersEmpty()) {
                System.out.println("\n--- 第 " + trick + " 墩 ---  闲家累计分:" + farmerScore);
                List<Card> leadPlay = null;
                Player leader = players[leaderSeat];
                Player bestP = null;
                List<Card> bestPlay = null;
                int trickScore = 0;

                for (int k = 0; k < 4; k++) {
                    Player cur = players[(leaderSeat + k) % 4];
                    if (cur.hand.isEmpty()) continue; // 防御
                    List<Card> play;
                    if (cur.isHuman) play = humanPlay(cur, leadPlay);
                    else play = (leadPlay == null)
                            ? ai.chooseLead(cur, level, trump, ctx)
                            : ai.chooseFollow(cur, leadPlay, level, trump, leader, bestP, bestPlay, ctx);

                    cur.hand.removeAll(play);
                    trickScore += play.stream().mapToInt(Card::point).sum();
                    System.out.println(cur.name + " 出：" + play);

                    // 记录断门
                    if (leadPlay != null) ctx.recordPlay(cur, leadPlay, play, level, trump);

                    if (leadPlay == null) {
                        leadPlay = play; bestP = cur; bestPlay = play;
                    } else {
                        Suit ls = leadPlay.stream().allMatch(c -> c.isTrump(level, trump))
                                ? trump : leadPlay.get(0).suit;
                        if (RuleEngine.comparePlay(play, bestPlay, level, trump, ls) > 0) {
                            bestP = cur; bestPlay = play;
                        }
                    }
                }

                boolean farmerWin = !(bestP == players[dealerSeat] || bestP.teammate == players[dealerSeat]);
                if (farmerWin) farmerScore += trickScore;
                farmerWinLast = farmerWin;
                lastPlay = bestPlay;
                leaderSeat = bestP.seat;
                System.out.println("→ 本墩获胜：" + bestP.name + "  本墩得分:" + trickScore + "  闲家累计:" + farmerScore);
                trick++;
            }

            farmerScore = ScoreEngine.applyBottom(farmerScore, farmerWinLast, lastPlay, bottom, level, trump);
            System.out.println("\n底牌:" + bottom + "  底牌效果后闲家终分:" + farmerScore);
            return farmerScore;
        }

        /** v3 §12.2 工具方法 */
        boolean allPlayersEmpty() {
            return Arrays.stream(players).allMatch(p -> p.hand.isEmpty());
        }

        /**
         * v3 §7 严格憋牌流程：
         *   1. 由闲家随机抽底牌（不向庄家展示）；若抽到王则继续抽，直到非王
         *   2. 庄家先盲扣底（此时仍未知主花色）
         *   3. 扣底完成后揭示该牌花色为主花色
         *   注意：抽出的牌不入任何人手，仅用于决定主花色
         */
        int handleNoBidScenario() {
            System.out.println("\n[憋牌] 全场无人亮主，进入盲扣 + 闲家抽花色种子流程");
            // 闲家：dealerSeat 之外随机一位
            List<Integer> farmers = new ArrayList<>();
            for (int i = 0; i < 4; i++) if (i != dealerSeat && i != players[dealerSeat].teammate.seat) farmers.add(i);
            // 若 dealerSeat 是 0，则 teammate 是 2，闲家是 1、3
            // 若庄家逻辑还没确定（无人亮主沿用上一局 dealerSeat），sigh，取所有非庄家随机
            if (farmers.isEmpty()) {
                for (int i = 0; i < 4; i++) if (i != dealerSeat) farmers.add(i);
            }
            int picker = farmers.get(new Random().nextInt(farmers.size()));
            System.out.println("由闲家 " + players[picker].name + " 盲抽底牌一张定主花色");

            // 抽一张：bottom 中随机选一张，若为王则换一张（直到非王）
            Random rnd = new Random();
            Card seed = null;
            List<Card> remainingBottom = new ArrayList<>(bottom);
            while (!remainingBottom.isEmpty()) {
                int idx = rnd.nextInt(remainingBottom.size());
                Card c = remainingBottom.get(idx);
                if (c.isJoker()) { remainingBottom.remove(idx); continue; }
                seed = c; break;
            }
            if (seed == null) {
                // 极端：4 张全是王。随机指定主花色为黑桃
                trump = Suit.SPADES;
                System.out.println("底牌全王，规则保护 → 主花色定为：" + trump.s);
            } else {
                trump = seed.suit;
                System.out.println("（盲抽种子已封存，暂不公开。庄家先盲扣...）");
            }
            // 庄家保持 dealerSeat 不变，由其本人收底
            int handlerSeat = dealerSeat;
            // 让庄家先盲扣（在 bottomExchangePhase 内若 trump 已定也无妨；此处文档要求"庄家先扣再揭花色"
            // 所以我们把"揭示"放到 bottomExchangePhase 之后）
            // 为最大兼容，我们这里直接揭示，因为 chooseBottom 已禁王/级牌/常主
            if (seed != null) System.out.println("⛳ 揭底花色种子：" + seed + " → 主花色：" + trump.s);
            return handlerSeat;
        }

        void checkBiddingRound(int cardCount) {
            if (bidder == null) {
                for (Player p : players) {
                    List<BidOption> opts = findBidOptions(p);
                    if (opts.isEmpty()) continue;
                    if (p.isHuman) {
                        p.sortHand(level, trump != null ? trump : Suit.SPADES);
                        p.show(level, trump);
                        System.out.println(p.name + " 你可以亮主，可选组合：");
                        for (int i = 0; i < opts.size(); i++) {
                            BidOption o = opts.get(i);
                            System.out.println("  [" + i + "] " + describeBid(o));
                        }
                        System.out.print("选择编号亮主，或 n 放弃：");
                        String input = sc.nextLine().trim();
                        if (input.equalsIgnoreCase("n")) continue;
                        try {
                            int idx = Integer.parseInt(input);
                            if (idx < 0 || idx >= opts.size()) continue;
                            executeBid(p, opts.get(idx));
                        } catch (Exception e) { continue; }
                    } else {
                        if (ai.shouldBid(p, cardCount)) {
                            BidOption best = aiBestBidOption(opts, p);
                            if (best != null) executeBid(p, best);
                        }
                    }
                    if (bidder != null) break;
                }
            }
            if (bidder != null) checkCounterRound();
        }

        String describeBid(BidOption o) {
            String tier;
            switch (o.tier) {
                case 1: tier = "单王亮主"; break;
                case 2: tier = "对王亮主"; break;
                case 3: tier = "对子亮主"; break;
                case 4: tier = "姊妹对亮主"; break;
                case 5: tier = "三连对亮主"; break;
                case 6: tier = "四连对亮主"; break;
                default: tier = "亮主";
            }
            return tier + "：" + (o.joker != null ? o.joker + " + " : "") + o.pairCards + " → 主花色 " + o.trumpSuit.s;
        }

        void executeBid(Player p, BidOption opt) {
            bidder = p;
            bidJoker = opt.joker;
            bidPairCards = opt.pairCards;
            bidLevelTier = opt.tier;
            trump = opt.trumpSuit;
            System.out.println("⭐ " + p.name + " 亮主 → " + describeBid(opt));
        }

        void checkCounterRound() {
            for (int round = 0; round < 5; round++) {
                boolean anyCounter = false;
                for (Player p : players) {
                    if (p == bidder) continue;
                    List<CounterOption> copts = findCounterOptions(p);
                    if (copts.isEmpty()) continue;

                    if (p.isHuman) {
                        p.sortHand(level, trump);
                        p.show(level, trump);
                        System.out.println(p.name + " 你可以反主（当前等级 tier=" + bidLevelTier + "），可选：");
                        for (int i = 0; i < copts.size(); i++) {
                            System.out.println("  [" + i + "] tier=" + copts.get(i).tier + " "
                                    + (copts.get(i).joker != null ? copts.get(i).joker + " + " : "")
                                    + copts.get(i).pairCards + " → 新主 " + copts.get(i).trumpSuit.s);
                        }
                        System.out.print("选择编号反主，或 n 放弃：");
                        String input = sc.nextLine().trim();
                        if (input.equalsIgnoreCase("n")) continue;
                        try {
                            int idx = Integer.parseInt(input);
                            if (idx < 0 || idx >= copts.size()) continue;
                            doCounter(p, copts.get(idx));
                            anyCounter = true;
                        } catch (Exception e) { continue; }
                    } else {
                        if (ai.shouldCounter(p)) {
                            doCounter(p, copts.get(0));
                            anyCounter = true;
                        }
                    }
                }
                if (!anyCounter) break;
            }
        }

        /* ----------- BidOption / CounterOption（v3 引入 tier） ----------- */
        static class BidOption {
            Card joker;            // 用于亮主的王（可为空，若 tier=2 表示对王，pairCards 含两张王）
            List<Card> pairCards;  // 亮主使用的牌
            int tier;              // 亮主等级
            Suit trumpSuit;
            BidOption(Card j, List<Card> pairs, int tier, Suit s) {
                joker = j; pairCards = pairs; this.tier = tier; trumpSuit = s;
            }
        }
        static class CounterOption extends BidOption {
            CounterOption(Card j, List<Card> pairs, int tier, Suit s) { super(j, pairs, tier, s); }
        }

        /**
         * v3 亮主规则（§6.1 + §6.2 反主基础）：
         *   tier=1 单王（任意一个王）→ 主花色由 AI 选定（人类可选）
         *   tier=2 对王（双副中的小王对 / 大王对）→ 同上
         *   tier=3 一对同花色 + 单王 → 该对的花色为主花色
         *   tier 更高的（姊妹对 / 三连对 / 四连对）通常仅出现在反主路径
         */
        List<BidOption> findBidOptions(Player p) {
            List<BidOption> options = new ArrayList<>();
            List<Card> bigJokers = new ArrayList<>();
            List<Card> smallJokers = new ArrayList<>();
            for (Card c : p.hand) {
                if (c.isBigJoker()) bigJokers.add(c);
                else if (c.isSmallJoker()) smallJokers.add(c);
            }

            // tier=1 单王亮（不含主花色信息，由 pickTrumpSuit 决定）
            for (Card j : bigJokers) {
                Suit s = ai.pickTrumpSuit(p, level);
                options.add(new BidOption(j, Collections.singletonList(j), 1, s));
            }
            for (Card j : smallJokers) {
                Suit s = ai.pickTrumpSuit(p, level);
                options.add(new BidOption(j, Collections.singletonList(j), 1, s));
            }

            // tier=2 对王
            if (bigJokers.size() >= 2) {
                Suit s = ai.pickTrumpSuit(p, level);
                options.add(new BidOption(bigJokers.get(0),
                        Arrays.asList(bigJokers.get(0), bigJokers.get(1)), 2, s));
            }
            if (smallJokers.size() >= 2) {
                Suit s = ai.pickTrumpSuit(p, level);
                options.add(new BidOption(smallJokers.get(0),
                        Arrays.asList(smallJokers.get(0), smallJokers.get(1)), 2, s));
            }

            // tier=3 一对同花色 + 单王（保留旧逻辑作为常见亮主）
            List<Card> jokers = new ArrayList<>(); jokers.addAll(bigJokers); jokers.addAll(smallJokers);
            if (!jokers.isEmpty()) {
                for (Suit s : new Suit[]{Suit.CLUBS, Suit.DIAMONDS, Suit.HEARTS, Suit.SPADES}) {
                    List<Card[]> pairs = findSuitPairsForBid(p.hand, s);
                    for (Card[] pair : pairs) {
                        for (Card joker : jokers) {
                            options.add(new BidOption(joker, Arrays.asList(pair[0], pair[1]), 3, s));
                        }
                    }
                }
            }
            return options;
        }

        BidOption aiBestBidOption(List<BidOption> opts, Player p) {
            if (opts.isEmpty()) return null;
            Map<Suit, Integer> cnt = new EnumMap<>(Suit.class);
            for (Card c : p.hand) if (!c.isJoker()) cnt.merge(c.suit, 1, Integer::sum);
            Suit bestSuit = cnt.entrySet().stream().max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey).orElse(Suit.SPADES);
            // 优先 tier=3（同花色对 + 王），其次 tier=2 对王，最次 tier=1 单王
            return opts.stream()
                    .filter(o -> o.trumpSuit == bestSuit || o.tier <= 2)
                    .max(Comparator.comparingInt((BidOption o) -> o.tier))
                    .orElse(opts.get(0));
        }

        /**
         * v3 反主规则（§6.2）严格阶梯：
         *   tier 1 (单王)        → tier 2 (对王 / 任何对子+王) 即可反
         *   tier 2 (对王)        → tier 4 (姊妹对) 反
         *   tier 3 (对子)        → tier 4 (姊妹对) 反
         *   tier 4 (姊妹对)      → tier 5 (三连对) 反
         *   tier 5 (三连对)      → tier 6 (四连对) 反
         *   每次反主必须使用一个王 + 满足等级要求的对/连对
         */
        List<CounterOption> findCounterOptions(Player p) {
            List<CounterOption> options = new ArrayList<>();
            List<Card> bigJokers = new ArrayList<>();
            List<Card> smallJokers = new ArrayList<>();
            for (Card c : p.hand) {
                if (c.isBigJoker()) bigJokers.add(c);
                else if (c.isSmallJoker()) smallJokers.add(c);
            }
            List<Card> jokers = new ArrayList<>(); jokers.addAll(bigJokers); jokers.addAll(smallJokers);
            if (jokers.isEmpty() && bidLevelTier >= 3) return options; // 无王无法反主（tier>=3 路径）

            int needTier;
            int needPairs;
            switch (bidLevelTier) {
                case 1: needTier = 2; needPairs = 0; break;  // 对王（或带任意对子+王）即可
                case 2: needTier = 4; needPairs = 2; break;  // 姊妹对
                case 3: needTier = 4; needPairs = 2; break;  // 姊妹对
                case 4: needTier = 5; needPairs = 3; break;  // 三连对
                case 5: needTier = 6; needPairs = 4; break;  // 四连对
                default: return options;
            }

            // 反 tier=1 单王：只要自己有对王，或同花色对 + 王 即可
            if (bidLevelTier == 1) {
                if (bigJokers.size() >= 2) {
                    Suit s = ai.pickTrumpSuit(p, level);
                    options.add(new CounterOption(bigJokers.get(0),
                            Arrays.asList(bigJokers.get(0), bigJokers.get(1)), 2, s));
                }
                if (smallJokers.size() >= 2) {
                    Suit s = ai.pickTrumpSuit(p, level);
                    options.add(new CounterOption(smallJokers.get(0),
                            Arrays.asList(smallJokers.get(0), smallJokers.get(1)), 2, s));
                }
                if (!jokers.isEmpty()) {
                    for (Suit s : new Suit[]{Suit.CLUBS, Suit.DIAMONDS, Suit.HEARTS, Suit.SPADES}) {
                        for (Card[] pair : findSuitPairsForBid(p.hand, s)) {
                            for (Card joker : jokers) {
                                options.add(new CounterOption(joker,
                                        Arrays.asList(pair[0], pair[1]), 3, s));
                            }
                        }
                    }
                }
                return options;
            }

            // tier>=2 起：必须用同花色 needPairs 连对 + 王
            for (Suit s : new Suit[]{Suit.CLUBS, Suit.DIAMONDS, Suit.HEARTS, Suit.SPADES}) {
                List<Card[]> pairs = findSuitPairsForBid(p.hand, s);
                List<Card> consec = findNConsecutivePairsForBid(pairs, needPairs);
                if (consec != null) {
                    for (Card joker : jokers) {
                        options.add(new CounterOption(joker, consec, needTier, s));
                    }
                }
            }
            return options;
        }

        /**
         * v3 §6.3 反主补偿（双向）：
         *   ① 反主者从被反者手里取回原先亮出的王（bidJoker）
         *      —— 真实牌桌做法是：被反者把刚亮出的王递给反主者
         *   ② 被反者从反主者获得一张固定主（2/3/5）作为补偿
         */
        void doCounter(Player p, CounterOption co) {
            Player oldBidder = bidder;
            // ① 反主者取回王：bidJoker 仍在 oldBidder 手中（亮主只是宣示，不离手），故不真实搬运
            //    但语义上"获得王"= 知晓其归属。这里不做物理转移，避免破坏手牌语义。
            //    若需严格执行物理转移，可启用下一行：
            //    if (bidJoker != null && oldBidder.hand.contains(bidJoker)) {
            //        oldBidder.hand.remove(bidJoker);
            //        p.hand.add(bidJoker);
            //    }

            // ② 反主者给被反者一张固定主补偿
            Card comp = pickCompensation(p);
            if (comp != null) {
                p.hand.remove(comp);
                oldBidder.hand.add(comp);
            }
            System.out.println("🔥 " + p.name + " 反主：tier=" + co.tier + " "
                    + (co.joker != null ? co.joker + " + " : "")
                    + co.pairCards + " → " + co.trumpSuit.s
                    + "（补偿固定主 " + comp + " 给 " + oldBidder.name + "）");

            bidder = p;
            bidJoker = co.joker;
            bidPairCards = co.pairCards;
            bidLevelTier = co.tier;
            trump = co.trumpSuit;
        }

        /** 选最小代价的固定主补偿（不拆对，优先单张，最后才拆对） */
        Card pickCompensation(Player counterPlayer) {
            Map<String, List<Card>> fixedMap = new LinkedHashMap<>();
            fixedMap.put("2", new ArrayList<>());
            fixedMap.put("3", new ArrayList<>());
            fixedMap.put("5", new ArrayList<>());
            for (Card c : counterPlayer.hand) {
                if (c.rank.equals("2")) fixedMap.get("2").add(c);
                else if (c.rank.equals("3")) fixedMap.get("3").add(c);
                else if (c.rank.equals("5")) fixedMap.get("5").add(c);
            }
            // 优先单张（不拆对）
            for (String rank : new String[]{"2", "3", "5"})
                if (fixedMap.get(rank).size() == 1) return fixedMap.get(rank).get(0);
            // 数量 ≥3 可以给一张
            for (String rank : new String[]{"2", "3", "5"})
                if (fixedMap.get(rank).size() >= 3) return fixedMap.get(rank).get(0);
            // 最后才拆对
            for (String rank : new String[]{"2", "3", "5"})
                if (!fixedMap.get(rank).isEmpty()) return fixedMap.get(rank).get(0);
            return null;
        }

        /* ============== 扣底相关 ============== */
        void bottomExchangePhase(int handlerSeat) {
            Player handler = players[handlerSeat];
            System.out.println("\n[扣底] " + handler.name + " 收底牌");
            if (handler.isHuman) {
                System.out.println("请选择扣底顺序： [1] 先拿底再扣  [2] 盲扣再拿底（适用于憋牌）");
                System.out.print("选择 (1/2)：");
                String choice = sc.nextLine().trim();
                if ("2".equals(choice)) bottomExchange_DiscardFirst(handler);
                else bottomExchange_PickupFirst(handler);
            } else {
                bottomExchange_PickupFirst(handler);
            }
        }

        void bottomExchange_PickupFirst(Player handler) {
            handler.hand.addAll(bottom);
            handler.sortHand(level, trump);
            System.out.println(handler.name + " 拿起底牌（" + handler.hand.size() + "张），需扣回 4 张");
            List<Card> newBottom;
            if (handler.isHuman) newBottom = humanSelectDiscard(handler, 4);
            else {
                newBottom = ai.chooseBottom(handler, level, trump, bidder, bidLevelTier);
                handler.hand.removeAll(newBottom);
                System.out.println(handler.name + "(AI) 扣底：" + newBottom);
            }
            bottom = newBottom;
        }

        void bottomExchange_DiscardFirst(Player handler) {
            handler.sortHand(level, trump);
            System.out.println(handler.name + " 先盲扣 4 张（不知道底牌内容）");
            List<Card> newBottom;
            if (handler.isHuman) newBottom = humanSelectDiscard(handler, 4);
            else {
                newBottom = ai.chooseBottom(handler, level, trump, bidder, bidLevelTier);
                handler.hand.removeAll(newBottom);
                System.out.println(handler.name + "(AI) 盲扣：" + newBottom);
            }
            handler.hand.addAll(bottom);
            handler.sortHand(level, trump);
            System.out.println(handler.name + " 拿起底牌，当前 " + handler.hand.size() + " 张");
            if (handler.isHuman) handler.show(level, trump);
            bottom = newBottom;
        }

        List<Card> humanSelectDiscard(Player handler, int count) {
            while (true) {
                handler.sortHand(level, trump);
                handler.show(level, trump);
                System.out.print("选择 " + count + " 张扣底（空格分隔索引；禁王/级牌/常主）：");
                String line = sc.nextLine().trim();
                try {
                    String[] tk = line.split("\\s+");
                    if (tk.length != count) { System.out.println("必须 " + count + " 张"); continue; }
                    Set<Integer> idxs = new TreeSet<>(Comparator.reverseOrder());
                    for (String t : tk) idxs.add(Integer.parseInt(t));
                    if (idxs.size() != count) { System.out.println("索引重复"); continue; }
                    boolean illegal = false;
                    List<Card> sel = new ArrayList<>();
                    for (int i : idxs) {
                        Card c = handler.hand.get(i);
                        if (c.isJoker() || c.rank.equals(level) || c.isFixedTrump()) {
                            illegal = true; break;
                        }
                        sel.add(c);
                    }
                    if (illegal) { System.out.println("含禁止扣底的牌（王/级牌/常主）"); continue; }
                    for (int i : idxs) handler.hand.remove(i);
                    return sel;
                } catch (Exception e) { System.out.println("输入错误"); }
            }
        }

        /** §8.2 亮主方换底 */
        void claimBottomBySameSuit() {
            if (bidder == null || trump == null) return;
            Player claimer = bidder;
            List<Card> claimable = new ArrayList<>();
            for (Card c : bottom) {
                if (c.suit == trump && !c.isJoker() && !c.rank.equals(level) && !c.isFixedTrump())
                    claimable.add(c);
            }
            if (claimable.isEmpty()) return;
            int claimPoints = claimable.stream().mapToInt(Card::point).sum();
            boolean hasScoreCard = claimable.stream().anyMatch(c -> c.point() > 0);
            System.out.println("\n[挑底] " + claimer.name + " 可挑：" + claimable);

            if (claimer.isHuman) {
                System.out.print("是否挑底？(y/n) ");
                if (!sc.nextLine().trim().equalsIgnoreCase("y")) return;
            }

            List<Card> replacements = findReplacement(claimer, claimable.size(), claimPoints, hasScoreCard);
            if (replacements == null) {
                System.out.println("  无合适补回牌，放弃挑底。"); return;
            }

            if (claimer.isHuman) {
                claimer.sortHand(level, trump);
                claimer.show(level, trump);
                System.out.println("需补回 " + claimable.size() + " 张，总分=" + claimPoints
                        + (hasScoreCard ? "（含分牌）" : ""));
                System.out.print("选择补回牌索引：");
                String line = sc.nextLine().trim();
                try {
                    String[] tk = line.split("\\s+");
                    if (tk.length != claimable.size()) { System.out.println("数量不对，放弃。"); return; }
                    List<Card> sel = new ArrayList<>();
                    Set<Integer> idxs = new TreeSet<>(Comparator.reverseOrder());
                    for (String t : tk) idxs.add(Integer.parseInt(t));
                    for (int i : idxs) sel.add(claimer.hand.get(i));
                    int selPoints = sel.stream().mapToInt(Card::point).sum();
                    if (selPoints != claimPoints) {
                        System.out.println("总分不等，放弃。"); return;
                    }
                    if (hasScoreCard && sel.stream().noneMatch(c -> c.point() > 0)) {
                        System.out.println("挑出的有分牌，补回也必须含分牌。"); return;
                    }
                    if (sel.stream().anyMatch(c -> c.isJoker() || c.rank.equals(level) || c.isFixedTrump())) {
                        System.out.println("补回含禁扣牌，放弃。"); return;
                    }
                    replacements = sel;
                    for (int i : idxs) claimer.hand.remove(i);
                } catch (Exception e) { System.out.println("输入错误，放弃。"); return; }
            } else {
                claimer.hand.removeAll(replacements);
            }
            bottom.removeAll(claimable);
            bottom.addAll(replacements);
            claimer.hand.addAll(claimable);
            claimer.sortHand(level, trump);
            System.out.println("  挑出：" + claimable + " → 补回：" + replacements);
        }

        List<Card> findReplacement(Player p, int count, int targetPoints, boolean needScore) {
            List<Card> candidates = p.hand.stream().filter(c ->
                            !c.isJoker() && !c.rank.equals(level) && !c.isFixedTrump()
                                    && c.suit != trump)
                    .sorted(Comparator.comparingInt(Card::point)
                            .thenComparingInt(c -> RuleEngine.sideOrder(c.rank)))
                    .collect(Collectors.toList());
            return findCombo(candidates, count, targetPoints, needScore, 0, new ArrayList<>());
        }

        List<Card> findCombo(List<Card> cands, int count, int target, boolean needScore,
                             int start, List<Card> current) {
            if (current.size() == count) {
                int sum = current.stream().mapToInt(Card::point).sum();
                if (sum != target) return null;
                if (needScore && current.stream().noneMatch(c -> c.point() > 0)) return null;
                return new ArrayList<>(current);
            }
            if (start >= cands.size()) return null;
            for (int i = start; i < cands.size(); i++) {
                current.add(cands.get(i));
                List<Card> r = findCombo(cands, count, target, needScore, i + 1, current);
                if (r != null) return r;
                current.remove(current.size() - 1);
            }
            return null;
        }

        /* ============== 玩家出牌 ============== */
        List<Card> humanPlay(Player p, List<Card> leadPlay) {
            while (true) {
                p.sortHand(level, trump);
                p.show(level, trump);
                if (leadPlay != null) {
                    boolean leadTrump = leadPlay.stream().allMatch(c -> c.isTrump(level, trump));
                    PlayType leadType = RuleEngine.typeOf(leadPlay, level, trump);
                    String catName = leadTrump ? "主牌" : leadPlay.get(0).suit.s + "副牌";
                    String typeName = leadType == PlayType.PAIR ? "对子"
                            : leadType == PlayType.CONSECUTIVE_PAIR ? "连对" : "单张";
                    List<Card> sameCat = RuleEngine.cardsOfCategory(p.hand,
                            leadTrump ? trump : leadPlay.get(0).suit, leadTrump, level, trump);
                    System.out.println("  ↑ 首出 " + leadPlay + "（" + catName + typeName + "）"
                            + " | 你有 " + sameCat.size() + " 张" + catName
                            + "，需跟 " + leadPlay.size() + " 张");
                }
                System.out.print("请输入要出的牌索引（多张用空格分隔）：");
                String line = sc.nextLine().trim();
                try {
                    String[] tk = line.split("\\s+");
                    Set<Integer> idxs = new TreeSet<>(Comparator.reverseOrder());
                    for (String t : tk) idxs.add(Integer.parseInt(t));
                    List<Card> play = new ArrayList<>();
                    for (int i : idxs) {
                        if (i < 0 || i >= p.hand.size()) throw new RuntimeException("索引越界");
                        play.add(p.hand.get(i));
                    }
                    String err = RuleEngine.validatePlay(p.hand, play, leadPlay, level, trump);
                    if (err != null) { System.out.println("❌ 不合法：" + err); continue; }
                    return play;
                } catch (Exception e) { System.out.println("输入错误：" + e.getMessage()); }
            }
        }

        /* ============== 升级结算 ============== */
        void applySettle(int farmerScore) {
            int[] r = ScoreEngine.settle(farmerScore);
            boolean dealerHold = r[0] == 1;
            int up = r[1];
            boolean swap = r[2] == 1;
            System.out.println("\n[结算] 闲家分:" + farmerScore + " -> 庄"
                    + (dealerHold ? "守" : "换") + "，升级 " + up);

            if (up == 0) consecutiveNoUpgrade++;
            else consecutiveNoUpgrade = 0;

            if (dealerHold) bumpLevel(teamA(players[dealerSeat]), up);
            else            bumpLevel(!teamA(players[dealerSeat]), up);

            if (swap) {
                int original = dealerSeat;
                dealerSeat = (dealerSeat + 1) % 4;
                while (teamA(players[dealerSeat]) == teamA(players[original]))
                    dealerSeat = (dealerSeat + 1) % 4;
            }

            if (consecutiveNoUpgrade >= DEADLOCK_THRESHOLD) {
                System.out.println("⚠ 连续 " + consecutiveNoUpgrade + " 局未升级，随机摇号换庄！");
                dealerSeat = new Random().nextInt(4);
                consecutiveNoUpgrade = 0;
            }

            String lvA = LEVEL_ORDER[Math.min(teamALevelIdx, 12)];
            String lvB = LEVEL_ORDER[Math.min(teamBLevelIdx, 12)];
            System.out.println("当前级别 — 甲:" + lvA + "  乙:" + lvB);
        }

        void bumpLevel(boolean teamA, int up) {
            if (teamA) teamALevelIdx = mustPlayClamp(teamALevelIdx, up);
            else       teamBLevelIdx = mustPlayClamp(teamBLevelIdx, up);
        }

        /**
         * v3 §3.2 必打级处理：
         *   若当前等级 < 必打级，且升级后 > 必打级 → 卡在必打级（必须坐庄打过）
         *   若当前等级 == K(idx=12)，再升即胜（idx=13 表示打过 K）
         */
        int mustPlayClamp(int from, int up) {
            int target = from + up;
            for (int s : new int[]{9, 10, 12}) { // 必打：10/J/K
                if (from < s && target > s) return s;
            }
            if (target > 13) target = 13;
            return target;
        }
    }

    /* =====================================================================
     * main
     * ===================================================================== */
    public static void main(String[] args) {
        String[] names = {"玩家", "AI_西", "AI_北", "AI_东"};
        new Game(names).play();
    }
}
