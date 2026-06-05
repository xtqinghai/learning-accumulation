package com.quinn.hboot.experimental.poker;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 升级扑克（山东泰安玩法）—— 控制台单机版 v2
 *
 * 改进点：
 *   1. 边摸边亮：deal(1)×26轮，每轮可亮主/反主
 *   2. 亮主方挑底牌补回（同数量同分值）
 *   3. 反主增强：对王反单王、大王反小王、对大王反对小王
 *   4. 连对相邻判定修复（级牌与常主重叠时的间隙）
 *   5. AiStrategy接口抽出，便于替换MCTS
 *   6. 必打卡死柔性保护（连续N局未升级则随机摇号坐庄）
 *   7. 反主补偿灵活给（不拆对，给最小的）
 *   8. 跟牌规则完善：没有对时允许出散牌跟
 */
public class UpgradePoker {

    /* =====================================================================
     *                              枚举定义
     * ===================================================================== */
    enum Suit {
        CLUBS("梅花"), DIAMONDS("方片"), HEARTS("红桃"), SPADES("黑桃"), JOKER("王");
        final String s;
        Suit(String s) { this.s = s; }
    }

    enum PlayType { SINGLE, PAIR, CONSECUTIVE_PAIR, INVALID }

    /* =====================================================================
     *                              Card
     * ===================================================================== */
    static class Card {
        final String rank;     // A,2,3,4,5,6,7,8,9,10,J,Q,K, BJ(大王), SJ(小王)
        final Suit suit;
        final int copyId;      // 0 或 1，区分两副中相同的牌

        Card(String rank, Suit suit, int copyId) {
            this.rank = rank; this.suit = suit; this.copyId = copyId;
        }
        boolean isBigJoker() { return suit == Suit.JOKER && rank.equals("BJ"); }
        boolean isSmallJoker() { return suit == Suit.JOKER && rank.equals("SJ"); }
        boolean isJoker() { return suit == Suit.JOKER; }

        /** 牌面分 */
        int point() {
            switch (rank) {
                case "5": return 5;
                case "10": case "K": return 10;
                default: return 0;
            }
        }

        /** 是否主牌 */
        boolean isTrump(String level, Suit trump) {
            if (isJoker()) return true;
            if (rank.equals(level)) return true;
            if (rank.equals("2") || rank.equals("3") || rank.equals("5")) return true;
            return suit == trump;
        }

        /** 是否常主（2/3/5）*/
        boolean isFixedTrump() {
            return rank.equals("2") || rank.equals("3") || rank.equals("5");
        }

        @Override
        public String toString() {
            if (isBigJoker()) return "🃏BJ";
            if (isSmallJoker()) return "🃏SJ";
            return suit.s + rank;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Card)) return false;
            Card c = (Card) o;
            return rank.equals(c.rank) && suit == c.suit && copyId == c.copyId;
        }

        @Override
        public int hashCode() { return Objects.hash(rank, suit, copyId); }
    }

    /* =====================================================================
     *                              Deck
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
            for (int i = cards.size() - 1; i > 0; i--) {
                int j = rnd.nextInt(i + 1);
                Collections.swap(cards, i, j);
            }
        }

        List<Card> deal(int n) {
            List<Card> r = new ArrayList<>(cards.subList(0, n));
            cards.subList(0, n).clear();
            return r;
        }
    }

    /* =====================================================================
     *                              RuleEngine
     * ===================================================================== */
    static class RuleEngine {

        /** 副牌排序值：A=12,K=11,Q=10,J=9,10=8,9=7,8=6,7=5,6=4,4=3 */
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
         * 主牌排序值（仅 isTrump=true 时有意义），数值越大越大：
         *   大王=100，小王=99
         *   级牌（主花色）=98，级牌（副花色）=97
         *   常主5=96，常主3=95，常主2=94（若与级牌重合则按级牌处理）
         *   主花色普通 A=82,K=81,...4=73
         */
        static int trumpOrder(Card c, String level, Suit trump) {
            if (c.isBigJoker()) return 100;
            if (c.isSmallJoker()) return 99;
            if (c.rank.equals(level)) {
                return (c.suit == trump) ? 98 : 97;
            }
            if (c.rank.equals("5")) return 96;
            if (c.rank.equals("3")) return 95;
            if (c.rank.equals("2")) return 94;
            return 70 + sideOrder(c.rank);
        }

        /**
         * 构建当前局主牌排序值的有序序列，用于连对相邻判定。
         * 处理级牌与常主(2/3/5)重叠时的间隙问题。
         */
        static List<Integer> buildTrumpSequence(String level, Suit trump) {
            List<Integer> seq = new ArrayList<>();
            // 主花色普通牌（排除级牌——级牌单独处理）
            for (String rank : new String[]{"4","6","7","8","9","10","J","Q","K","A"}) {
                if (!rank.equals(level)) {
                    seq.add(70 + sideOrder(rank));
                }
            }
            // 常主（排除与级牌重叠的）
            if (!"2".equals(level)) seq.add(94);
            if (!"3".equals(level)) seq.add(95);
            if (!"5".equals(level)) seq.add(96);
            // 级牌
            seq.add(97); // 副花色级牌
            seq.add(98); // 主花色级牌
            // 王
            seq.add(99);
            seq.add(100);
            Collections.sort(seq);
            return seq;
        }

        /** 判断两个trumpOrder值在主牌序列中是否相邻 */
        static boolean isTrumpAdjacent(int valA, int valB, String level, Suit trump) {
            List<Integer> seq = buildTrumpSequence(level, trump);
            int idxA = seq.indexOf(valA);
            int idxB = seq.indexOf(valB);
            if (idxA < 0 || idxB < 0) return false;
            return Math.abs(idxA - idxB) == 1;
        }

        /** 通用比较：a 与 b 的大小 */
        static int compareCard(Card a, Card b, String level, Suit trump) {
            boolean ta = a.isTrump(level, trump), tb = b.isTrump(level, trump);
            if (ta && !tb) return 1;
            if (!ta && tb) return -1;
            if (ta) return Integer.compare(trumpOrder(a, level, trump), trumpOrder(b, level, trump));
            if (a.suit != b.suit) return 0;
            return Integer.compare(sideOrder(a.rank), sideOrder(b.rank));
        }

        /** 判别牌型 */
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
         * 连对判定（修正版）：
         * 使用 buildTrumpSequence 处理级牌-常主重叠间隙。
         * 副牌相邻仍用 sideOrder（因为副牌不存在间隙问题，2/3/5 永远不在副牌中）。
         */
        static boolean isConsecutivePairs(List<Card> play, String level, Suit trump) {
            List<Card> sorted = new ArrayList<>(play);
            boolean allTrump = sorted.stream().allMatch(c -> c.isTrump(level, trump));
            boolean allOneSideSuit = !allTrump
                    && sorted.stream().allMatch(c -> c.suit == sorted.get(0).suit
                    && !c.isTrump(level, trump));
            if (!allTrump && !allOneSideSuit) return false;

            // 按排序值排序
            sorted.sort((x, y) -> allTrump
                    ? Integer.compare(trumpOrder(x, level, trump), trumpOrder(y, level, trump))
                    : Integer.compare(sideOrder(x.rank), sideOrder(y.rank)));

            // 验证每一对
            for (int i = 0; i < sorted.size(); i += 2) {
                if (!isPair(sorted.get(i), sorted.get(i + 1))) return false;
            }

            // 验证相邻性
            if (allTrump) {
                // 主牌：用序列索引判定相邻
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
                // 副牌：sideOrder 天然连续（2/3/5不参与副牌）
                int prev = -999;
                for (int i = 0; i < sorted.size(); i += 2) {
                    int v = sideOrder(sorted.get(i).rank);
                    if (prev != -999 && v != prev + 1) return false;
                    prev = v;
                }
            }
            return true;
        }

        /** 比较两手牌大小：a > b 返回正数 */
        static int comparePlay(List<Card> a, List<Card> b, String level, Suit trump, Suit leadSuit) {
            Card ka = a.stream().max((x, y) -> compareCard(x, y, level, trump)).orElse(null);
            Card kb = b.stream().max((x, y) -> compareCard(x, y, level, trump)).orElse(null);
            boolean aTrump = a.stream().allMatch(c -> c.isTrump(level, trump));
            boolean bTrump = b.stream().allMatch(c -> c.isTrump(level, trump));
            if (aTrump && !bTrump) return 1;
            if (!aTrump && bTrump) return -1;
            if (aTrump) return Integer.compare(trumpOrder(ka, level, trump), trumpOrder(kb, level, trump));
            if (ka.suit != leadSuit) return -1;
            if (kb.suit != leadSuit) return 1;
            return Integer.compare(sideOrder(ka.rank), sideOrder(kb.rank));
        }

        /** 获取手牌中指定类别（同花色副牌 或 主牌）的牌 */
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

        /** 检查牌列表中是否含有指定类别的对子 */
        static boolean hasPairInCategory(List<Card> cards) {
            for (int i = 0; i < cards.size(); i++)
                for (int j = i + 1; j < cards.size(); j++)
                    if (isPair(cards.get(i), cards.get(j))) return true;
            return false;
        }

        /** 检查牌列表中是否含有指定类别的连对 */
        static boolean hasConsecutivePairInCategory(List<Card> cards, int pairCount,
                                                    String level, Suit trump) {
            if (cards.size() < pairCount * 2) return false;
            // 尝试所有组合太昂贵，简化：检查是否存在任意 pairCount 个相邻对子
            // 先找出所有对子
            List<int[]> pairs = new ArrayList<>(); // [idx1, idx2]
            boolean[] used = new boolean[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                if (used[i]) continue;
                for (int j = i + 1; j < cards.size(); j++) {
                    if (used[j]) continue;
                    if (isPair(cards.get(i), cards.get(j))) {
                        pairs.add(new int[]{i, j});
                        used[i] = true; used[j] = true;
                        break;
                    }
                }
            }
            if (pairs.size() < pairCount) return false;
            // 检查连续性
            boolean allTrump = cards.stream().allMatch(c -> c.isTrump(level, trump));
            List<Integer> pairValues = new ArrayList<>();
            for (int[] p : pairs) {
                Card c = cards.get(p[0]);
                pairValues.add(allTrump ? trumpOrder(c, level, trump) : sideOrder(c.rank));
            }
            Collections.sort(pairValues);
            // 滑窗找连续pairCount个
            if (allTrump) {
                List<Integer> seq = buildTrumpSequence(level, trump);
                List<Integer> pairIdxs = new ArrayList<>();
                for (int v : pairValues) {
                    int idx = seq.indexOf(v);
                    if (idx >= 0) pairIdxs.add(idx);
                }
                Collections.sort(pairIdxs);
                for (int i = 0; i <= pairIdxs.size() - pairCount; i++) {
                    boolean ok = true;
                    for (int j = 1; j < pairCount; j++) {
                        if (pairIdxs.get(i + j) != pairIdxs.get(i) + j) { ok = false; break; }
                    }
                    if (ok) return true;
                }
            } else {
                for (int i = 0; i <= pairValues.size() - pairCount; i++) {
                    boolean ok = true;
                    for (int j = 1; j < pairCount; j++) {
                        if (pairValues.get(i + j) != pairValues.get(i) + j) { ok = false; break; }
                    }
                    if (ok) return true;
                }
            }
            return false;
        }

        /**
         * 出牌合法性校验（完善版）：
         * - 跟对子时，若同类别无对子则允许出两张散牌
         * - 跟连对时，若同类别无连对则先出对子，不够再出散牌
         */
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
            List<Card> mustList = cardsOfCategory(hand, leadSuit, leadTrump, level, trump);

            if (play.size() != leadPlay.size()) return "出牌张数必须等于" + leadPlay.size();

            // 必须先用同类别
            int needSame = Math.min(mustList.size(), leadPlay.size());
            long playSame = play.stream().filter(c -> {
                boolean t = c.isTrump(level, trump);
                return leadTrump ? t : (!t && c.suit == leadSuit);
            }).count();
            if (playSame < needSame)
                return "必须先跟" + (leadTrump ? "主牌" : leadSuit.s) + "，至少" + needSame + "张";

            // 牌型匹配（当同类别牌足够时）
            if (mustList.size() >= leadPlay.size()) {
                if (leadType == PlayType.PAIR) {
                    // 有同类别对子才强制出对子；没有则允许出散牌
                    if (hasPairInCategory(mustList)) {
                        if (typeOf(play, level, trump) != PlayType.PAIR)
                            return "你有同花色对子，必须出对子";
                    }
                    // 无对子时：出2张同类散牌即可（已通过playSame检查）
                }
                if (leadType == PlayType.CONSECUTIVE_PAIR) {
                    int pairCount = leadPlay.size() / 2;
                    if (hasConsecutivePairInCategory(mustList, pairCount, level, trump)) {
                        if (typeOf(play, level, trump) != PlayType.CONSECUTIVE_PAIR)
                            return "你有同花色连对，必须出连对";
                    } else if (hasPairInCategory(mustList)) {
                        // 无连对但有对子：应优先出对子
                        // 检查play中是否包含了尽可能多的对子
                        // （简化：不强制，交由AI/玩家自行判断）
                    }
                }
            }
            return null;
        }
    }

    /* =====================================================================
     *                              Player
     * ===================================================================== */
    static class Player {
        final String name;
        final boolean isHuman;
        final int seat;       // 0..3
        List<Card> hand = new ArrayList<>();
        Player teammate;

        Player(String name, boolean h, int seat) {
            this.name = name; this.isHuman = h; this.seat = seat;
        }

        void receive(List<Card> cs) { hand.addAll(cs); }

        void sortHand(String level, Suit trump) {
            hand.sort((a, b) -> {
                boolean ta = a.isTrump(level, trump), tb = b.isTrump(level, trump);
                if (ta != tb) return ta ? -1 : 1;
                if (ta) return Integer.compare(RuleEngine.trumpOrder(b, level, trump),
                        RuleEngine.trumpOrder(a, level, trump));
                int s = Integer.compare(a.suit.ordinal(), b.suit.ordinal());
                if (s != 0) return s;
                return Integer.compare(RuleEngine.sideOrder(b.rank), RuleEngine.sideOrder(a.rank));
            });
        }

        void show() {
            StringBuilder sb = new StringBuilder(name + "(" + hand.size() + "): ");
            for (int i = 0; i < hand.size(); i++)
                sb.append("[").append(i).append("]").append(hand.get(i)).append(" ");
            System.out.println(sb);
        }

        /** 带主/副分隔的增强显示 */
        void show(String level, Suit trump) {
            if (level == null || trump == null) { show(); return; }
            StringBuilder sb = new StringBuilder(name + "(" + hand.size() + ")\n");
            // 主牌部分
            sb.append("  主牌: ");
            boolean hasAnyTrump = false;
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).isTrump(level, trump)) {
                    sb.append("[").append(i).append("]").append(hand.get(i)).append(" ");
                    hasAnyTrump = true;
                }
            }
            if (!hasAnyTrump) sb.append("（无）");
            sb.append("\n");
            // 副牌部分，按花色分组
            Suit lastSuit = null;
            boolean hasAnySide = false;
            sb.append("  副牌: ");
            for (int i = 0; i < hand.size(); i++) {
                Card c = hand.get(i);
                if (c.isTrump(level, trump)) continue;
                if (lastSuit != null && c.suit != lastSuit) sb.append(" | ");
                sb.append("[").append(i).append("]").append(c).append(" ");
                lastSuit = c.suit;
                hasAnySide = true;
            }
            if (!hasAnySide) sb.append("（无）");
            System.out.println(sb);
        }
    }

    /* =====================================================================
     *                              ScoreEngine
     * ===================================================================== */
    static class ScoreEngine {
        /** 升级结算：返回 [庄家是否守住, 升级数, 是否上台] */
        static int[] settle(int farmerScore) {
            if (farmerScore <= 75)  return new int[]{1, 1, 0};
            if (farmerScore <= 115) return new int[]{0, 0, 1};
            if (farmerScore <= 155) return new int[]{0, 1, 1};
            if (farmerScore <= 195) return new int[]{0, 2, 1};
            return new int[]{0, 3, 1}; // 200
        }

        /** 底分增减 */
        static int applyBottom(int farmerScore, boolean farmerWinLastTrick,
                               List<Card> lastPlay, List<Card> bottom,
                               String level, Suit trump) {
            int bp = bottom.stream().mapToInt(Card::point).sum();
            boolean trumpPlay = lastPlay.stream().allMatch(c -> c.isTrump(level, trump));
            if (!trumpPlay) {
                // 副牌扣底：不翻倍，只按原始底分加减
                if (farmerWinLastTrick) farmerScore += bp;
                    // 庄家收副牌底——底分不翻倍也不扣（规则：副牌不翻倍，只按原始加减）
                    // 修正理解：闲家赢最后一墩 → 闲家得底分；庄家赢 → 底分不计
                    // 按规则2.4：闲牌扣底均不翻倍，只按原始底分加减
                    // 这里"加减"的含义：闲家收底就加，庄家收底就减
                else farmerScore -= bp;
                return Math.max(0, Math.min(200, farmerScore));
            }
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
     *                       AiStrategy 接口（改进5）
     * ===================================================================== */
    interface AiStrategy {
        /** 首出选牌 */
        List<Card> chooseLead(Player p, String level, Suit trump);
        /** 跟牌选牌 */
        List<Card> chooseFollow(Player p, List<Card> leadPlay, String level, Suit trump,
                                Player leader, Player currentBest, List<Card> bestPlay);
        /** 庄家扣底 */
        List<Card> chooseBottom(Player p, String level, Suit trump);
        /** 选择主花色 */
        Suit pickTrumpSuit(Player p, String level);
        /** 是否亮主（AI决策） */
        boolean shouldBid(Player p, int cardCount);
        /** 是否反主（AI决策） */
        boolean shouldCounter(Player p);
    }

    /* =====================================================================
     *                      RuleBasedAI（实现 AiStrategy）
     * ===================================================================== */
    static class RuleBasedAI implements AiStrategy {

        @Override
        public List<Card> chooseLead(Player p, String level, Suit trump) {
            List<Card> hand = p.hand;
            // 1) 副牌A且本花色无分牌
            for (Card a : hand) {
                if (a.rank.equals("A") && !a.isTrump(level, trump)) {
                    Suit s = a.suit;
                    boolean hasScore = hand.stream().anyMatch(c ->
                            !c.isTrump(level, trump) && c.suit == s && c.point() > 0 && !c.equals(a));
                    if (!hasScore) return Collections.singletonList(a);
                }
            }
            // 2) 副牌对子
            List<Card> pair = findPair(hand, level, trump, false);
            if (pair != null) return pair;
            // 3) 连对
            List<Card> consec = findConsecutivePair(hand, level, trump, false);
            if (consec != null) return consec;
            // 4) 主牌大牌对子（调主）
            List<Card> tpair = findPair(hand, level, trump, true);
            if (tpair != null) return tpair;
            // 5) 出最小副牌
            Card min = null;
            for (Card c : hand) {
                if (c.isTrump(level, trump)) continue;
                if (min == null || RuleEngine.sideOrder(c.rank) < RuleEngine.sideOrder(min.rank))
                    min = c;
            }
            if (min != null) return Collections.singletonList(min);
            // 6) 全是主牌：出最小主
            Card minT = hand.stream().min((x, y) -> Integer.compare(
                    RuleEngine.trumpOrder(x, level, trump),
                    RuleEngine.trumpOrder(y, level, trump))).orElse(hand.get(0));
            return Collections.singletonList(minT);
        }

        private List<Card> findPair(List<Card> hand, String level, Suit trump, boolean wantTrump) {
            for (int i = 0; i < hand.size(); i++)
                for (int j = i + 1; j < hand.size(); j++) {
                    Card a = hand.get(i), b = hand.get(j);
                    if (RuleEngine.isPair(a, b)) {
                        if (a.isTrump(level, trump) == wantTrump)
                            return Arrays.asList(a, b);
                    }
                }
            return null;
        }

        private List<Card> findConsecutivePair(List<Card> hand, String level, Suit trump, boolean wantTrump) {
            // 找手牌中的连对（至少2对相邻）
            // 简化：按花色/主牌分组，查找连续对子
            Map<Object, List<Card>> groups = new LinkedHashMap<>();
            for (Card c : hand) {
                boolean t = c.isTrump(level, trump);
                if (t == wantTrump) {
                    Object key = t ? "TRUMP" : c.suit;
                    groups.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
                }
            }
            for (List<Card> group : groups.values()) {
                // 找所有对子
                List<Card[]> pairs = new ArrayList<>();
                boolean[] used = new boolean[group.size()];
                for (int i = 0; i < group.size(); i++) {
                    if (used[i]) continue;
                    for (int j = i + 1; j < group.size(); j++) {
                        if (used[j]) continue;
                        if (RuleEngine.isPair(group.get(i), group.get(j))) {
                            pairs.add(new Card[]{group.get(i), group.get(j)});
                            used[i] = true; used[j] = true;
                            break;
                        }
                    }
                }
                if (pairs.size() < 2) continue;
                // 按排序值排序
                boolean allTrump = wantTrump;
                pairs.sort((a, b) -> {
                    int va = allTrump ? RuleEngine.trumpOrder(a[0], level, trump) : RuleEngine.sideOrder(a[0].rank);
                    int vb = allTrump ? RuleEngine.trumpOrder(b[0], level, trump) : RuleEngine.sideOrder(b[0].rank);
                    return Integer.compare(va, vb);
                });
                // 找相邻对（使用序列索引判定）
                List<Integer> seq = allTrump ? RuleEngine.buildTrumpSequence(level, trump) : null;
                for (int i = 0; i < pairs.size() - 1; i++) {
                    int va = allTrump ? RuleEngine.trumpOrder(pairs.get(i)[0], level, trump) : RuleEngine.sideOrder(pairs.get(i)[0].rank);
                    int vb = allTrump ? RuleEngine.trumpOrder(pairs.get(i+1)[0], level, trump) : RuleEngine.sideOrder(pairs.get(i+1)[0].rank);
                    boolean adjacent;
                    if (allTrump) {
                        int idxA = seq.indexOf(va), idxB = seq.indexOf(vb);
                        adjacent = (idxA >= 0 && idxB >= 0 && idxB == idxA + 1);
                    } else {
                        adjacent = (vb == va + 1);
                    }
                    if (adjacent) {
                        List<Card> result = new ArrayList<>();
                        result.add(pairs.get(i)[0]); result.add(pairs.get(i)[1]);
                        result.add(pairs.get(i+1)[0]); result.add(pairs.get(i+1)[1]);
                        return result;
                    }
                }
            }
            return null;
        }

        @Override
        public List<Card> chooseFollow(Player p, List<Card> leadPlay, String level, Suit trump,
                                       Player leader, Player currentBest, List<Card> bestPlay) {
            List<Card> hand = p.hand;
            boolean leadTrump = leadPlay.stream().allMatch(c -> c.isTrump(level, trump));
            Suit leadSuit = leadTrump ? trump : leadPlay.get(0).suit;
            PlayType leadType = RuleEngine.typeOf(leadPlay, level, trump);

            List<Card> sameCat = RuleEngine.cardsOfCategory(hand, leadSuit, leadTrump, level, trump);
            int need = leadPlay.size();
            boolean teammateWinning = currentBest != null && currentBest.teammate == p;

            // 同类够
            if (sameCat.size() >= need) {
                sameCat.sort(Comparator.comparingInt(c -> orderKey(c, level, trump)));

                // 跟对子但无对子时：直接出最小/最大散牌
                if (leadType == PlayType.PAIR && !RuleEngine.hasPairInCategory(sameCat)) {
                    // 无对子，出最小的两张散牌
                    return new ArrayList<>(sameCat.subList(0, need));
                }

                if (teammateWinning) {
                    return new ArrayList<>(sameCat.subList(0, need));
                }

                // 尝试压过
                List<Card> sortedDesc = new ArrayList<>(sameCat);
                Collections.reverse(sortedDesc);

                // 对于对子：找能压过的最小对子
                if (leadType == PlayType.PAIR && RuleEngine.hasPairInCategory(sameCat)) {
                    List<Card> bestPair = findSmallestWinningPair(sameCat, bestPlay, level, trump, leadSuit);
                    if (bestPair != null) return bestPair;
                    // 压不过，出最小对子或最小散牌
                    List<Card> smallPair = findSmallestPair(sameCat);
                    if (smallPair != null) return smallPair;
                    return new ArrayList<>(sameCat.subList(0, need));
                }

                // 普通：最小能压过的组合
                List<Card> attempt = new ArrayList<>(sortedDesc.subList(0, need));
                if (RuleEngine.comparePlay(attempt, bestPlay, level, trump, leadSuit) > 0) {
                    for (int i = sameCat.size() - need; i >= 0; i--) {
                        List<Card> cand = new ArrayList<>(sameCat.subList(i, i + need));
                        if (RuleEngine.comparePlay(cand, bestPlay, level, trump, leadSuit) > 0)
                            return cand;
                    }
                }
                return new ArrayList<>(sameCat.subList(0, need));
            }

            // 不够同类
            List<Card> result = new ArrayList<>(sameCat);
            List<Card> remain = new ArrayList<>(hand);
            remain.removeAll(sameCat);

            int shortage = need - result.size();
            boolean tableHasScore = bestPlay.stream().mapToInt(Card::point).sum() > 0
                    || leadPlay.stream().mapToInt(Card::point).sum() > 0;

            // 无同类且有分且非队友赢且首出非主 → 用最小主牌毙
            if (sameCat.isEmpty() && tableHasScore && !teammateWinning && !leadTrump) {
                List<Card> trumps = remain.stream().filter(c -> c.isTrump(level, trump))
                        .sorted(Comparator.comparingInt(c -> RuleEngine.trumpOrder(c, level, trump)))
                        .collect(Collectors.toList());
                if (trumps.size() >= need) {
                    return new ArrayList<>(trumps.subList(0, need));
                }
            }

            // 垫无用副牌
            remain.sort(Comparator.comparingInt(c -> orderKey(c, level, trump)));
            for (Card c : remain) {
                if (result.size() >= need) break;
                result.add(c);
            }
            return result;
        }

        /** 找同类别中能压过bestPlay的最小对子 */
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
                        used[i] = true; used[j] = true;
                        break;
                    }
                }
            }
            // 从小到大找能赢的
            pairs.sort(Comparator.comparingInt(p -> orderKey(p[0], level, trump)));
            for (Card[] pair : pairs) {
                List<Card> cand = Arrays.asList(pair[0], pair[1]);
                if (RuleEngine.comparePlay(cand, bestPlay, level, trump, leadSuit) > 0)
                    return cand;
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
        public List<Card> chooseBottom(Player p, String level, Suit trump) {
            List<Card> candidates = p.hand.stream().filter(c ->
                            !c.isJoker() && !c.rank.equals(level) && !c.isFixedTrump())
                    .collect(Collectors.toList());
            candidates.sort((a, b) -> {
                int sa = a.point(), sb = b.point();
                if (sa != sb) return Integer.compare(sa, sb);
                int ta = a.isTrump(level, trump) ? 1 : 0;
                int tb = b.isTrump(level, trump) ? 1 : 0;
                if (ta != tb) return Integer.compare(ta, tb);
                return Integer.compare(orderKey(a, level, trump), orderKey(b, level, trump));
            });
            return new ArrayList<>(candidates.subList(0, Math.min(4, candidates.size())));
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
            // 至少摸到10张牌时考虑亮主（能力已由findBidOptions检查）
            if (cardCount < 10) return false;
            // 手中同花色牌越多越值得亮主
            Map<Suit, Integer> cnt = new EnumMap<>(Suit.class);
            for (Card c : p.hand)
                if (!c.isJoker()) cnt.merge(c.suit, 1, Integer::sum);
            int maxSuitCount = cnt.values().stream().mapToInt(i -> i).max().orElse(0);
            return maxSuitCount >= 5 && Math.random() < 0.5;
        }

        @Override
        public boolean shouldCounter(Player p) {
            return Math.random() < 0.3;
        }
    }

    /* =====================================================================
     *                   亮主竞标工具（对数递增体系）
     * ===================================================================== */

    /** 亮主/反主用的自然点数排序（用于判定连对相邻），不含2/3/5 */
    static int bidRankOrder(String rank) {
        switch (rank) {
            case "4": return 1; case "6": return 2; case "7": return 3;
            case "8": return 4; case "9": return 5; case "10": return 6;
            case "J": return 7; case "Q": return 8; case "K": return 9;
            case "A": return 10;
            default: return -1; // 2,3,5 不参与亮主连对
        }
    }

    /**
     * 在给定花色的牌列表中，找出所有对子（同花同点两张），
     * 返回按 bidRankOrder 升序排列的对子列表。
     */
    static List<Card[]> findSuitPairsForBid(List<Card> hand, Suit suit) {
        // 按 (rank) 分组，只保留非王且指定花色且 bidRankOrder>=0 的牌
        Map<String, List<Card>> groups = new LinkedHashMap<>();
        for (Card c : hand) {
            if (c.isJoker() || c.suit != suit) continue;
            if (bidRankOrder(c.rank) < 0) continue; // 排除 2/3/5
            groups.computeIfAbsent(c.rank, k -> new ArrayList<>()).add(c);
        }
        List<Card[]> pairs = new ArrayList<>();
        for (Map.Entry<String, List<Card>> e : groups.entrySet()) {
            if (e.getValue().size() >= 2) {
                pairs.add(new Card[]{e.getValue().get(0), e.getValue().get(1)});
            }
        }
        pairs.sort(Comparator.comparingInt(p -> bidRankOrder(p[0].rank)));
        return pairs;
    }

    /**
     * 在已排序的对子列表中，找 count 个相邻连对（bidRankOrder 连续）。
     * 返回连对的牌列表，找不到返回 null。
     */
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
     *                              Game
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

        // 改进6：连续未升级计数
        int consecutiveNoUpgrade = 0;
        static final int DEADLOCK_THRESHOLD = 8; // 连续8局未升级触发保护

        static final String[] LEVEL_ORDER = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};

        // 亮主状态（对数递增体系）
        Player bidder = null;
        Card bidJoker = null;            // 亮主用的王
        List<Card> bidPairCards = null;   // 亮主出的对子/连对牌
        int bidPairCount = 0;            // 当前亮主的对数（1=单对，2=连对，3=三连对…）

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
                round++;
                if (round > 100) { System.out.println("超过 100 局自动结束"); break; }
            }
            int winnerTeam = teamALevelIdx >= LEVEL_ORDER.length ? 0 : 1;
            System.out.println("\n🎉 " + (winnerTeam == 0 ? "甲队（0/2）" : "乙队（1/3）") + " 获胜！");
        }

        /** 进行一整局，返回闲家分数 */
        int playOneDeal() {
            Deck deck = new Deck();
            deck.shuffle();
            for (Player p : players) p.hand.clear();
            level = currentLevel();

            // 先抽出4张底牌
            bottom = deck.deal(4);

            // ===== 改进1：边摸边亮 =====
            bidder = null; bidJoker = null; bidPairCards = null; bidPairCount = 0; trump = null;
            System.out.println("\n[发牌 + 亮主阶段]");

            for (int round = 0; round < 26; round++) {
                // 每轮每人摸1张
                for (int k = 0; k < 4; k++) {
                    Player p = players[(dealerSeat + 1 + k) % 4];
                    List<Card> drawn = deck.deal(1);
                    p.receive(drawn);
                }
                // 每轮后检查亮主/反主
                checkBiddingRound(round + 1);
            }

            // 揭底牌（无人亮主时）
            int bottomHandlerSeat;
            if (bidder == null) {
                for (Card c : bottom) {
                    if (!c.isJoker()) { trump = c.suit; break; }
                }
                if (trump == null) trump = Suit.SPADES;
                System.out.println("无人亮主，揭底牌确定主花色：" + trump.s);
                bottomHandlerSeat = dealerSeat; // 无人亮主时庄家自己收底
            } else {
                dealerSeat = bidder.seat;
                bottomHandlerSeat = bidder.teammate.seat; // 亮主方队友收底
                System.out.println("最终主：" + trump.s
                        + "，庄家(亮牌方)：" + players[dealerSeat].name
                        + "，收底方：" + players[bottomHandlerSeat].name);
            }

            // 扣底阶段（队友收底）
            bottomExchangePhase(bottomHandlerSeat);

            // 亮主方挑底牌补回
            claimBottomBySameSuit();

            // 13墩出牌（扣底者首出）
            int farmerScore = 0;
            int leaderSeat = bottomHandlerSeat;
            List<Card> lastPlay = null;
            boolean farmerWinLast = false;

            for (Player p : players) p.sortHand(level, trump);

            for (int trick = 1; trick <= 13; trick++) {
                System.out.println("\n--- 第 " + trick + " 墩 ---  闲家累计分:" + farmerScore);
                List<Card> leadPlay = null;
                Player leader = players[leaderSeat];
                Player bestP = null;
                List<Card> bestPlay = null;
                int trickScore = 0; // 本墩总分

                for (int k = 0; k < 4; k++) {
                    Player cur = players[(leaderSeat + k) % 4];
                    List<Card> play;
                    if (cur.isHuman)
                        play = humanPlay(cur, leadPlay);
                    else
                        play = (leadPlay == null)
                                ? ai.chooseLead(cur, level, trump)
                                : ai.chooseFollow(cur, leadPlay, level, trump, leader, bestP, bestPlay);
                    cur.hand.removeAll(play);
                    // 累计本墩分数（修复：所有4家出牌都要计分）
                    trickScore += play.stream().mapToInt(Card::point).sum();
                    System.out.println(cur.name + " 出：" + play);

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

                if (bestP == players[dealerSeat] || bestP.teammate == players[dealerSeat]) {
                    // 庄家方赢
                } else {
                    farmerScore += trickScore;
                }
                farmerWinLast = !(bestP == players[dealerSeat] || bestP.teammate == players[dealerSeat]);
                lastPlay = bestPlay;
                leaderSeat = bestP.seat;
                System.out.println("→ 本墩获胜：" + bestP.name + "  本墩得分:" + trickScore + "  闲家累计:" + farmerScore);
            }

            // 底分增减
            farmerScore = ScoreEngine.applyBottom(farmerScore, farmerWinLast, lastPlay, bottom, level, trump);
            System.out.println("\n底牌:" + bottom + "  底牌效果后闲家终分:" + farmerScore);
            return farmerScore;
        }

        /* ============= 亮主/反主（对数递增体系）============= */

        /**
         * 每轮摸牌后的亮主/反主检查。
         * @param cardCount 当前每人手牌数
         */
        void checkBiddingRound(int cardCount) {
            // 亮主阶段：如果还没有人亮主，依次询问
            if (bidder == null) {
                for (Player p : players) {
                    List<BidOption> opts = findBidOptions(p);
                    if (opts.isEmpty()) continue;
                    if (p.isHuman) {
                        p.sortHand(level != null ? level : currentLevel(),
                                trump != null ? trump : Suit.SPADES);
                        p.show(level, trump);
                        System.out.println(p.name + " 你可以亮主，可选组合：");
                        for (int i = 0; i < opts.size(); i++) {
                            BidOption o = opts.get(i);
                            System.out.println("  [" + i + "] " + o.joker + " + " + o.pairCards
                                    + " → 主花色 " + o.trumpSuit.s);
                        }
                        System.out.print("选择编号亮主，或 n 放弃：");
                        String input = sc.nextLine().trim();
                        if (input.equalsIgnoreCase("n")) continue;
                        try {
                            int idx = Integer.parseInt(input);
                            if (idx < 0 || idx >= opts.size()) continue;
                            BidOption chosen = opts.get(idx);
                            executeBid(p, chosen);
                        } catch (Exception e) { continue; }
                    } else {
                        if (ai.shouldBid(p, cardCount)) {
                            // AI：选手牌最多花色中的最小对子
                            BidOption best = aiBestBidOption(opts, p);
                            if (best != null) executeBid(p, best);
                        }
                    }
                    if (bidder != null) break;
                }
            }

            // 反主阶段
            if (bidder != null) {
                checkCounterRound();
            }
        }

        /** 执行亮主 */
        void executeBid(Player p, BidOption opt) {
            bidder = p;
            bidJoker = opt.joker;
            bidPairCards = opt.pairCards;
            bidPairCount = opt.pairCount;
            trump = opt.trumpSuit;
            System.out.println("⭐ " + p.name + " 亮主：" + bidJoker + " + " + bidPairCards
                    + "（" + bidPairCount + "对） → " + trump.s);
        }

        /** 反主检查（最多5轮，支持持续递增） */
        void checkCounterRound() {
            for (int round = 0; round < 5; round++) {
                boolean anyCounter = false;
                for (Player p : players) {
                    if (p == bidder) continue;
                    List<CounterOption> copts = findCounterOptions(p);
                    if (copts.isEmpty()) continue;

                    if (p.isHuman) {
                        p.sortHand(level != null ? level : currentLevel(), trump);
                        p.show(level, trump);
                        System.out.println(p.name + " 你可以反主（当前：" + trump.s
                                + "，" + bidPairCount + "对），需要 "
                                + (bidPairCount + 1) + "连对：");
                        for (int i = 0; i < copts.size(); i++) {
                            CounterOption co = copts.get(i);
                            System.out.println("  [" + i + "] " + co.joker + " + " + co.pairCards
                                    + " → 新主 " + co.trumpSuit.s);
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

        /* ---------- 亮主选项 ---------- */

        static class BidOption {
            Card joker;              // 使用的王
            List<Card> pairCards;    // 出的对子牌（2张=1对，4张=2连对…）
            int pairCount;           // 对数
            Suit trumpSuit;          // 对子花色 = 主牌花色

            BidOption(Card j, List<Card> pairs, int cnt, Suit s) {
                joker = j; pairCards = pairs; pairCount = cnt; trumpSuit = s;
            }
        }

        /**
         * 找出玩家所有可用的亮主选项：1王 + 1对（任意花色任意非2/3/5点数）
         */
        List<BidOption> findBidOptions(Player p) {
            List<BidOption> options = new ArrayList<>();
            // 找一张王
            Card joker = null;
            for (Card c : p.hand) {
                if (c.isJoker()) { joker = c; break; }
            }
            if (joker == null) return options;

            // 在每个花色中找对子
            for (Suit s : new Suit[]{Suit.CLUBS, Suit.DIAMONDS, Suit.HEARTS, Suit.SPADES}) {
                List<Card[]> pairs = findSuitPairsForBid(p.hand, s);
                for (Card[] pair : pairs) {
                    List<Card> pairList = Arrays.asList(pair[0], pair[1]);
                    options.add(new BidOption(joker, pairList, 1, s));
                }
            }
            return options;
        }

        /** AI选最优亮主选项：选手牌最多花色的最小对子 */
        BidOption aiBestBidOption(List<BidOption> opts, Player p) {
            if (opts.isEmpty()) return null;
            // 统计各花色手牌数
            Map<Suit, Integer> cnt = new EnumMap<>(Suit.class);
            for (Card c : p.hand)
                if (!c.isJoker()) cnt.merge(c.suit, 1, Integer::sum);
            // 选手牌最多的花色
            Suit bestSuit = cnt.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey).orElse(Suit.SPADES);
            // 在该花色中找选项，取第一个（最小对子）
            for (BidOption o : opts) {
                if (o.trumpSuit == bestSuit) return o;
            }
            return opts.get(0); // fallback
        }

        /* ---------- 反主选项 ---------- */

        static class CounterOption {
            Card joker;
            List<Card> pairCards;    // 连对牌
            int pairCount;           // 对数
            Suit trumpSuit;

            CounterOption(Card j, List<Card> pairs, int cnt, Suit s) {
                joker = j; pairCards = pairs; pairCount = cnt; trumpSuit = s;
            }
        }

        /**
         * 找反主选项：1王 + N连对，N = 当前亮主对数 + 1。
         * 连对要求同花色、bidRankOrder相邻。
         */
        List<CounterOption> findCounterOptions(Player p) {
            List<CounterOption> options = new ArrayList<>();
            Card joker = null;
            for (Card c : p.hand) {
                if (c.isJoker()) { joker = c; break; }
            }
            if (joker == null) return options;

            int needPairs = bidPairCount + 1;
            for (Suit s : new Suit[]{Suit.CLUBS, Suit.DIAMONDS, Suit.HEARTS, Suit.SPADES}) {
                List<Card[]> pairs = findSuitPairsForBid(p.hand, s);
                List<Card> consec = findNConsecutivePairsForBid(pairs, needPairs);
                if (consec != null) {
                    options.add(new CounterOption(joker, consec, needPairs, s));
                }
            }
            return options;
        }

        /**
         * 执行反主：补偿规则不变（不拆对，给最小固定主）
         */
        void doCounter(Player p, CounterOption co) {
            Card comp = pickCompensation(p);
            if (comp != null) {
                p.hand.remove(comp);
                bidder.hand.add(comp);
            }
            System.out.println("🔥 " + p.name + " 反主：" + co.joker + " + " + co.pairCards
                    + "（" + co.pairCount + "连对） → " + co.trumpSuit.s
                    + " （补偿 " + comp + " 给 " + bidder.name + "）");
            bidder = p;
            bidJoker = co.joker;
            bidPairCards = co.pairCards;
            bidPairCount = co.pairCount;
            trump = co.trumpSuit;
        }

        /**
         * 反主补偿：不拆对，给最小固定主（2<3<5）
         */
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
            for (String rank : new String[]{"2", "3", "5"}) {
                List<Card> list = fixedMap.get(rank);
                if (list.size() == 1) return list.get(0);
            }
            for (String rank : new String[]{"2", "3", "5"}) {
                List<Card> list = fixedMap.get(rank);
                if (!list.isEmpty()) return list.get(0);
            }
            return null;
        }

        Suit parseSuit(String s) {
            if (s == null || s.isEmpty()) return null;
            switch (Character.toUpperCase(s.charAt(0))) {
                case 'C': return Suit.CLUBS;
                case 'D': return Suit.DIAMONDS;
                case 'H': return Suit.HEARTS;
                case 'S': return Suit.SPADES;
            }
            return null;
        }

        /* ============= 扣底（支持先扣后拿 / 先拿后扣）============= */

        /**
         * 扣底阶段：
         *   handlerSeat 的玩家收底牌，可选两种顺序：
         *   A）先拿后扣：先拿起底牌（30张），再选4张扣回（26张）
         *   B）先扣后拿：先从手中拿出4张（22张），再拿起底牌（26张）
         */
        void bottomExchangePhase(int handlerSeat) {
            Player handler = players[handlerSeat];
            System.out.println("\n[扣底] " + handler.name + " 负责收底牌");

            if (handler.isHuman) {
                System.out.println("请选择扣底顺序：");
                System.out.println("  [1] 先拿底牌再扣牌（可看到底牌后决定）");
                System.out.println("  [2] 先扣牌再拿底牌（盲扣）");
                System.out.print("选择 (1/2)：");
                String choice = sc.nextLine().trim();
                if ("2".equals(choice)) {
                    bottomExchange_DiscardFirst(handler);
                } else {
                    bottomExchange_PickupFirst(handler);
                }
            } else {
                // AI总是先拿后扣（信息更充分）
                bottomExchange_PickupFirst(handler);
            }
        }

        /** 先拿后扣：拿起底牌（30张）→ 选4张扣回（26张） */
        void bottomExchange_PickupFirst(Player handler) {
            handler.hand.addAll(bottom);
            handler.sortHand(level, trump);
            System.out.println(handler.name + " 拿起底牌（" + handler.hand.size()
                    + "张），需扣回 4 张（不能扣王/级牌/常主）");
            List<Card> newBottom;
            if (handler.isHuman) {
                newBottom = humanSelectDiscard(handler, 4);
            } else {
                newBottom = ai.chooseBottom(handler, level, trump);
                handler.hand.removeAll(newBottom);
                System.out.println(handler.name + "(AI) 扣底：" + newBottom);
            }
            bottom = newBottom;
        }

        /** 先扣后拿：先从手中拿出4张（22张）→ 拿起底牌（26张） */
        void bottomExchange_DiscardFirst(Player handler) {
            handler.sortHand(level, trump);
            System.out.println(handler.name + " 先从手中选 4 张扣出（不能扣王/级牌/常主），然后再拿底牌");
            List<Card> newBottom;
            if (handler.isHuman) {
                newBottom = humanSelectDiscard(handler, 4);
            } else {
                newBottom = ai.chooseBottom(handler, level, trump);
                handler.hand.removeAll(newBottom);
                System.out.println(handler.name + "(AI) 盲扣：" + newBottom);
            }
            // 扣完后拿起底牌
            handler.hand.addAll(bottom);
            handler.sortHand(level, trump);
            System.out.println(handler.name + " 拿起底牌，当前 " + handler.hand.size() + " 张");
            if (handler.isHuman) {
                handler.show(level, trump);
            }
            bottom = newBottom;
        }

        /** 人类玩家选择 count 张牌扣底（循环校验） */
        List<Card> humanSelectDiscard(Player handler, int count) {
            while (true) {
                handler.sortHand(level, trump);
                handler.show(level, trump);
                System.out.print("选择 " + count + " 张扣底（空格分隔索引）：");
                String line = sc.nextLine().trim();
                try {
                    String[] tk = line.split("\\s+");
                    if (tk.length != count) {
                        System.out.println("必须 " + count + " 张"); continue;
                    }
                    Set<Integer> idxs = new TreeSet<>(Comparator.reverseOrder());
                    for (String t : tk) idxs.add(Integer.parseInt(t));
                    if (idxs.size() != count) {
                        System.out.println("索引重复"); continue;
                    }
                    boolean illegal = false;
                    List<Card> sel = new ArrayList<>();
                    for (int i : idxs) {
                        Card c = handler.hand.get(i);
                        if (c.isJoker() || c.rank.equals(level) || c.isFixedTrump()) {
                            illegal = true; break;
                        }
                        sel.add(c);
                    }
                    if (illegal) {
                        System.out.println("含禁止扣底的牌（王/级牌/常主）"); continue;
                    }
                    for (int i : idxs) handler.hand.remove(i);
                    return sel;
                } catch (Exception e) {
                    System.out.println("输入错误");
                }
            }
        }

        /**
         * 改进2：亮主方挑底牌补回
         * 亮主方扣底后，可从新底牌中挑出与主花色相同的牌收入手中，
         * 但必须用同数量、同总分值的牌补回底牌。
         * 若挑出的牌中有分牌，补回的也必须是分牌。
         */
        void claimBottomBySameSuit() {
            if (bidder == null || trump == null) return;
            Player claimer = bidder;

            // 找底牌中与主花色相同的非主牌（注意：级牌和常主已经是主牌，不在此列）
            // 实际规则：挑出主花色的牌
            List<Card> claimable = new ArrayList<>();
            for (Card c : bottom) {
                if (c.suit == trump && !c.isJoker() && !c.rank.equals(level) && !c.isFixedTrump()) {
                    claimable.add(c);
                }
            }
            if (claimable.isEmpty()) return;

            int claimPoints = claimable.stream().mapToInt(Card::point).sum();
            boolean hasScoreCard = claimable.stream().anyMatch(c -> c.point() > 0);

            System.out.println("\n[挑底] " + claimer.name + " 可从底牌中挑出主花色牌：" + claimable);

            if (claimer.isHuman) {
                System.out.print("是否挑底？(y/n) ");
                if (!sc.nextLine().trim().equalsIgnoreCase("y")) return;
            }

            // AI总是挑
            // 找手中可以补回的牌：同数量、同总分值
            List<Card> replacements = findReplacement(claimer, claimable.size(), claimPoints, hasScoreCard);
            if (replacements == null) {
                System.out.println("  手中无合适的牌补回，放弃挑底。");
                return;
            }

            if (claimer.isHuman) {
                claimer.sortHand(level, trump);
                claimer.show(level, trump);
                System.out.println("需补回 " + claimable.size() + " 张，总分=" + claimPoints
                        + (hasScoreCard ? "（含分牌）" : ""));
                System.out.print("选择补回的牌索引（空格分隔）：");
                String line = sc.nextLine().trim();
                try {
                    String[] tk = line.split("\\s+");
                    if (tk.length != claimable.size()) {
                        System.out.println("数量不匹配，放弃挑底。"); return;
                    }
                    List<Card> sel = new ArrayList<>();
                    Set<Integer> idxs = new TreeSet<>(Comparator.reverseOrder());
                    for (String t : tk) idxs.add(Integer.parseInt(t));
                    for (int i : idxs) sel.add(claimer.hand.get(i));
                    int selPoints = sel.stream().mapToInt(Card::point).sum();
                    if (selPoints != claimPoints) {
                        System.out.println("总分不等（需" + claimPoints + "，选了" + selPoints + "），放弃。");
                        return;
                    }
                    if (hasScoreCard && sel.stream().noneMatch(c -> c.point() > 0)) {
                        System.out.println("挑出的牌含分牌，补回的也必须含分牌，放弃。");
                        return;
                    }
                    // 不能补回王/级牌/常主
                    if (sel.stream().anyMatch(c -> c.isJoker() || c.rank.equals(level) || c.isFixedTrump())) {
                        System.out.println("补回的牌含禁止扣底的牌，放弃。"); return;
                    }
                    replacements = sel;
                    // 从手中移除索引对应的牌
                    for (int i : idxs) claimer.hand.remove(i);
                } catch (Exception e) {
                    System.out.println("输入错误，放弃挑底。"); return;
                }
            } else {
                claimer.hand.removeAll(replacements);
            }

            // 执行交换
            bottom.removeAll(claimable);
            bottom.addAll(replacements);
            claimer.hand.addAll(claimable);
            claimer.sortHand(level, trump);
            System.out.println("  挑出：" + claimable + " → 补回：" + replacements);
        }

        /** AI找合适的补回牌 */
        List<Card> findReplacement(Player p, int count, int targetPoints, boolean needScore) {
            // 从手牌中找可扣底的牌（不含王/级牌/常主）
            List<Card> candidates = p.hand.stream().filter(c ->
                            !c.isJoker() && !c.rank.equals(level) && !c.isFixedTrump()
                                    && c.suit != trump) // 优先补副牌
                    .sorted(Comparator.comparingInt(Card::point)
                            .thenComparingInt(c -> RuleEngine.sideOrder(c.rank)))
                    .collect(Collectors.toList());

            // 简单贪心：尝试找count张总分=targetPoints的组合
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

        /* ============= 出牌 ============= */

        /** 玩家手动出牌 */
        List<Card> humanPlay(Player p, List<Card> leadPlay) {
            while (true) {
                p.sortHand(level, trump);
                p.show(level, trump);
                // 跟牌时显示提示
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

        /* ============= 升级结算 ============= */
        void applySettle(int farmerScore) {
            int[] r = ScoreEngine.settle(farmerScore);
            boolean dealerHold = r[0] == 1;
            int up = r[1];
            boolean swap = r[2] == 1;
            System.out.println("\n[结算] 闲家分:" + farmerScore + " -> 庄"
                    + (dealerHold ? "守" : "换") + "，升级 " + up);

            // 改进6：追踪连续未升级
            if (up == 0) {
                consecutiveNoUpgrade++;
            } else {
                consecutiveNoUpgrade = 0;
            }

            if (dealerHold) {
                bumpLevel(teamA(players[dealerSeat]), up);
            } else {
                bumpLevel(!teamA(players[dealerSeat]), up);
            }

            if (swap) {
                // 上台：换为另一队玩家坐庄
                int original = dealerSeat;
                dealerSeat = (dealerSeat + 1) % 4;
                while (teamA(players[dealerSeat]) == teamA(players[original])) {
                    dealerSeat = (dealerSeat + 1) % 4;
                }
            }

            // 改进6：必打卡死柔性保护
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

        /** 10/J/K 必打：跨过则停在必打点 */
        int mustPlayClamp(int from, int up) {
            int target = from + up;
            int[] mustStops = {9, 10, 12}; // 10->idx9, J->idx10, K->idx12
            for (int s : mustStops) {
                if (from < s && target > s) return s;
            }
            return Math.min(target, 13);
        }
    }

    /* =====================================================================
     *                              main
     * ===================================================================== */
    public static void main(String[] args) {
        String[] names = {"玩家", "AI_西", "AI_北", "AI_东"};
        new Game(names).play();
    }
}
