package ac.jnu.flowbot.data.database;

import java.util.Locale;

public class College {

    public enum Colleges {
        ENGINEERING,
        MULTIPLE,
        OTHER
    }

    public static Colleges parseCollegeByName(String name) {
        switch(name.toLowerCase(Locale.ROOT)) {
            case "공과대학":
            case "공대":
            case "공대생":
            case "engineering":
                return Colleges.ENGINEERING;
            case "복수전공":
            case "복전":
            case "복전생":
            case "부전공":
                return Colleges.MULTIPLE;
        }
        return Colleges.OTHER;
    }
}
