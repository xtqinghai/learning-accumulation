package com.quinn.hboot.js;

import com.quinn.hboot.js.UpgradePokerV3.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * 离线规则单元测试：覆盖 v3 关键修复点
 *  1. ScoreEngine.settle 区间表
 *  2. 主连对必须同花色（红桃A+黑桃K 不能拼连对）
 *  3. 必打级 mustPlayClamp 行为
 *  4. sortHand 段优先级
 */
public class RuleTest {

    static int passed = 0, failed = 0;

    static void check(String name, boolean cond) {
        if (cond) { passed++; System.out.println("✅ " + name); }
        else      { failed++; System.out.println("❌ " + name); }
    }

    public static void main(String[] args) throws Exception {
        testSettleTable();
        testTrumpConsecutivePairSuit();
        testMustPlayClamp();
        testSortHand();
        System.out.println("\n=== 测试结果：通过 " + passed + " / 失败 " + failed + " ===");
        if (failed > 0) System.exit(1);
    }

    // ---------------- §3.3 升降级区间表 ----------------
    static void testSettleTable() throws Exception {
        System.out.println("\n[Test] §3.3 升降级区间表");
        Class<?> seCls = Class.forName("com.quinn.hboot.js.UpgradePokerV3$ScoreEngine");
        Method settle = seCls.getDeclaredMethod("settle", int.class);
        settle.setAccessible(true);

        int[][] cases = {
            // {farmerScore, expectedDealerHold, expectedUp, expectedSwap}
            {  0, 1, 3, 0}, // 大光，庄升 3
            { 20, 1, 3, 0}, // 5-35 小光，庄升 3 (v3 修复)
            { 35, 1, 3, 0},
            { 40, 1, 1, 0},
            { 75, 1, 1, 0},
            { 80, 0, 0, 1}, // 换庄不升
            {115, 0, 0, 1},
            {120, 0, 1, 1}, // 闲升 1
            {155, 0, 1, 1},
            {160, 0, 2, 1}, // 闲升 2
            {195, 0, 2, 1},
            {200, 0, 3, 1}, // 大倒，闲升 3
        };
        for (int[] c : cases) {
            int[] r = (int[]) settle.invoke(null, c[0]);
            check("score=" + c[0] + " → hold=" + r[0] + " up=" + r[1] + " swap=" + r[2],
                    r[0] == c[1] && r[1] == c[2] && r[2] == c[3]);
        }
    }

    // ---------------- §5.3 主连对必须同花色 ----------------
    static void testTrumpConsecutivePairSuit() throws Exception {
        System.out.println("\n[Test] §5.3 主连对必须同花色（红桃A+黑桃K 不可拼连对）");
        Class<?> cardCls = Class.forName("com.quinn.hboot.js.UpgradePokerV3$Card");
        Class<?> suitCls = Class.forName("com.quinn.hboot.js.UpgradePokerV3$Suit");
        Constructor<?> ctor = cardCls.getDeclaredConstructor(String.class, suitCls, int.class);
        ctor.setAccessible(true);

        Object SPADES  = Enum.valueOf((Class<Enum>) suitCls, "SPADES");
        Object HEARTS  = Enum.valueOf((Class<Enum>) suitCls, "HEARTS");

        // 当前 level=2, trump=红桃；红桃 A、K 都是主花色普通牌
        // 红桃A + 红桃A + 红桃K + 红桃K → 应是合法主连对
        Object hA1 = ctor.newInstance("A", HEARTS, 0);
        Object hA2 = ctor.newInstance("A", HEARTS, 1);
        Object hK1 = ctor.newInstance("K", HEARTS, 0);
        Object hK2 = ctor.newInstance("K", HEARTS, 1);
        // 红桃A + 红桃A + 黑桃K + 黑桃K → 不能（黑桃K 是副牌；即使是主，跨花色也不行）
        // 为构造"全主"情况，使用 trump=红桃，level=2，黑桃K 是副牌——所以这例不进入主连对路径
        // 改用：红桃A + 红桃A + 黑桃 2 + 黑桃 2（黑桃2 是常主），这时全主，但跨花色应 false
        Object s2_1 = ctor.newInstance("2", SPADES, 0);
        Object s2_2 = ctor.newInstance("2", SPADES, 1);

        Class<?> reCls = Class.forName("com.quinn.hboot.js.UpgradePokerV3$RuleEngine");
        Method typeOf = reCls.getDeclaredMethod("typeOf", List.class, String.class, suitCls);
        typeOf.setAccessible(true);

        List<Object> good = Arrays.asList(hA1, hA2, hK1, hK2);
        Object t1 = typeOf.invoke(null, good, "2", HEARTS);
        check("红桃AA+红桃KK = 主连对", t1.toString().equals("CONSECUTIVE_PAIR"));

        List<Object> bad = Arrays.asList(hA1, hA2, s2_1, s2_2);
        Object t2 = typeOf.invoke(null, bad, "2", HEARTS);
        check("红桃AA + 黑桃22（跨花色） ≠ 主连对", !t2.toString().equals("CONSECUTIVE_PAIR"));
    }

    // ---------------- §3.2 必打级 ----------------
    static void testMustPlayClamp() throws Exception {
        System.out.println("\n[Test] §3.2 必打级（10=idx9, J=idx10, K=idx12）");
        Class<?> gameCls = Class.forName("com.quinn.hboot.js.UpgradePokerV3$Game");
        Constructor<?> gctor = gameCls.getDeclaredConstructor(String[].class);
        gctor.setAccessible(true);
        Object game = gctor.newInstance(new Object[]{new String[]{"P0","P1","P2","P3"}});
        Method clamp = gameCls.getDeclaredMethod("mustPlayClamp", int.class, int.class);
        clamp.setAccessible(true);

        // 8 (9级=idx8) 升 3 → 应卡在 9 (10级)
        check("from=8 up=3 → 卡在必打 10(idx9)", (int) clamp.invoke(game, 8, 3) == 9);
        // 9 (10级) 升 1 → 10 (J)
        check("from=9 up=1 → 直升至 J(idx10)", (int) clamp.invoke(game, 9, 1) == 10);
        // 10 (J) 升 3 → 卡在 12 (K)
        check("from=10 up=3 → 卡在必打 K(idx12)", (int) clamp.invoke(game, 10, 3) == 12);
        // 12 (K) 升 1 → 13 (打过 K，胜利)
        check("from=12(K) up=1 → idx13（打过 K 即胜）", (int) clamp.invoke(game, 12, 1) == 13);
        // 0 (A) 升 3 → 3 (4级，不卡)
        check("from=0 up=3 → 3 (4级)", (int) clamp.invoke(game, 0, 3) == 3);
    }

    // ---------------- §十四 手牌排序 ----------------
    static void testSortHand() throws Exception {
        System.out.println("\n[Test] §十四 手牌排序");
        Class<?> cardCls = Class.forName("com.quinn.hboot.js.UpgradePokerV3$Card");
        Class<?> suitCls = Class.forName("com.quinn.hboot.js.UpgradePokerV3$Suit");
        Class<?> playerCls = Class.forName("com.quinn.hboot.js.UpgradePokerV3$Player");
        Constructor<?> cctor = cardCls.getDeclaredConstructor(String.class, suitCls, int.class);
        cctor.setAccessible(true);
        Constructor<?> pctor = playerCls.getDeclaredConstructor(String.class, boolean.class, int.class);
        pctor.setAccessible(true);

        Object SPADES  = Enum.valueOf((Class<Enum>) suitCls, "SPADES");
        Object HEARTS  = Enum.valueOf((Class<Enum>) suitCls, "HEARTS");
        Object JOKER   = Enum.valueOf((Class<Enum>) suitCls, "JOKER");

        // 构造杂乱手牌：黑桃A(副)、红桃4(主花色)、大王、常主3、级牌5(主花色)
        Object bj  = cctor.newInstance("BJ", JOKER, 0);
        Object lvl = cctor.newInstance("5", HEARTS, 0); // level=5, trump=红桃 → 红桃5是级牌主
        Object ft3 = cctor.newInstance("3", SPADES, 0); // 常主 3
        Object trH = cctor.newInstance("4", HEARTS, 0); // 主花色 4
        Object sdA = cctor.newInstance("A", SPADES, 0); // 副牌 A

        Object player = pctor.newInstance("test", false, 0);
        Field handF = playerCls.getDeclaredField("hand");
        handF.setAccessible(true);
        List<Object> hand = (List<Object>) handF.get(player);
        hand.add(sdA); hand.add(trH); hand.add(ft3); hand.add(bj); hand.add(lvl);

        Method sort = playerCls.getDeclaredMethod("sortHand", String.class, suitCls);
        sort.setAccessible(true);
        sort.invoke(player, "5", HEARTS);

        // 期望顺序：大王 → 红桃5(级牌主) → 常主3 → 红桃4(主花色) → 黑桃A(副)
        check("位置0 = 大王BJ", hand.get(0).toString().contains("BJ"));
        check("位置1 = 红桃5(级牌)", hand.get(1).toString().equals("红桃5"));
        check("位置2 = 常主3", hand.get(2).toString().equals("黑桃3"));
        check("位置3 = 红桃4(主花色)", hand.get(3).toString().equals("红桃4"));
        check("位置4 = 黑桃A(副)", hand.get(4).toString().equals("黑桃A"));
    }
}
