package ac.jnu.flowbot.data.database;

import java.io.Serial;

public enum SolvedTier {
    BRONZE_V(1),
    BRONZE_IV(2),
    BRONZE_III(3),
    BRONZE_II(4),
    BRONZE_I(5),
    SILVER_V(6),
    SILVER_IV(7),
    SILVER_III(8),
    SILVER_II(9),
    SILVER_I(10),
    GOLD_V(11),
    GOLD_IV(12),
    GOLD_III(13),
    GOLD_II(14),
    GOLD_I(15),
    PLATINUM_V(16),
    PLATINUM_IV(17),
    PLATINUM_III(18),
    PLATINUM_II(19),
    PLATINUM_I(20),
    DIAMOND_V(21),
    DIAMOND_IV(22),
    DIAMOND_III(23),
    DIAMOND_II(24),
    DIAMOND_I(25),
    RUBY_V(26),
    RUBY_IV(27),
    RUBY_III(28),
    RUBY_II(29),
    RUBY_I(30),
    ;

    @Serial private static final long serialVersionUID = 2000L;

    int tierNum;

    SolvedTier(int i) { tierNum = i; }

    public String getURL() {
        return String.format("https://solved.ac/api/v3/search/problem?query=solvable:true+tier:%d", tierNum);
    }

    public int getTier() {
        return tierNum;
    }

    public static SolvedTier[] getTierList(String name) {
        switch(name.toLowerCase()) {
            case "bronze":
                return new SolvedTier[]{BRONZE_I, BRONZE_II, BRONZE_III, BRONZE_IV, BRONZE_V};
            case "silver":
                return new SolvedTier[]{SILVER_I, SILVER_II, SILVER_III, SILVER_IV, SILVER_V};
            case "gold":
                return new SolvedTier[]{GOLD_I, GOLD_II, GOLD_III, GOLD_IV, GOLD_V};
            case "platinum":
                return new SolvedTier[]{PLATINUM_I, PLATINUM_II, PLATINUM_III, PLATINUM_IV, PLATINUM_V};
            case "diamond":
                return new SolvedTier[]{DIAMOND_I, DIAMOND_II, DIAMOND_III, DIAMOND_IV, DIAMOND_V};
            case "ruby":
                return new SolvedTier[]{RUBY_I, RUBY_II, RUBY_III, RUBY_IV, RUBY_V};
        }
        return new SolvedTier[0];
    }
}
