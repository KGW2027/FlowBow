package ac.jnu.flowbot.functions;

import ac.jnu.flowbot.data.EnvironmentData;
import ac.jnu.flowbot.data.database.College;
import net.dv8tion.jda.api.entities.Role;

public class RoleManager {

    enum Roles {
        VERIFYED(1038059280340881409L),

        ENGINEERING(1038107560022118491L),
        MULTIPLE(1038107619761598484L),
        OTHER(1038107709901385768L),

        YEAR23(1038110191142576279L),
        YEAR22(1038107765685620767L),
        YEAR21(1038107768504201246L),
        YEAR20(1038107770777514196L),
        YEAR19(1038107778687959121L),
        YEAR18(1038107795913982012L),
        YEAR17(1038108061170151424L),
        YEAR16(1038108063724482662L),
        YEAR15(1038108065339297802L),
        YEAR14(1038108067490971769L)
        ;

        long id;
        Roles(long id) {
            this.id = id;
        }
    }

    public static Role getVerified() {
        return EnvironmentData.getInstance().getMainGuild().getRoleById(Roles.VERIFYED.id);
    }

    public static Role getCollege(College.Colleges college) {
        Role r = EnvironmentData.getInstance().getMainGuild().getRoleById(Roles.OTHER.id);
        switch (college){
            case ENGINEERING -> r = EnvironmentData.getInstance().getMainGuild().getRoleById(Roles.ENGINEERING.id);
            case MULTIPLE -> r = EnvironmentData.getInstance().getMainGuild().getRoleById(Roles.MULTIPLE.id);
        }
        return r;
    }

    public static Role getYear(String year) {
        return EnvironmentData.getInstance().getMainGuild().getRoleById(Roles.valueOf("YEAR"+year.substring(0, 2)).id);
    }
}
