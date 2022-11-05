package ac.jnu.flowbot.functions;

import ac.jnu.flowbot.data.EnvironmentData;
import ac.jnu.flowbot.data.database.College;
import ac.jnu.flowbot.data.database.Languages;
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
        YEAR14(1038108067490971769L),

        JAVA(1038382518954635264L),
        REACT(1038382593613254686L),
        TYPESCRIPT(1038383305889939506L),
        CSHARP(1038382588471029760L),
        CPP(1038382592786956288L),
        LANGC(1038382590966636574L),
        PYTHON(1038382801050931273L),
        LANGDART(1038383519212245002L),
        MATLAB(1038383003832954941L),
        RUST(1038383026083733655L),
        LANGR(1038383019112796220L),
        UNREAL(1038383152143540234L),
        UNITY(1038383022623428658L),
        ASSEMBLY(1038383440988491786L),
        SWIFT(1038383353918926959L)
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

    public static Role getLanguage(Languages languages) {
        return EnvironmentData.getInstance().getMainGuild().getRoleById(Roles.valueOf(languages.name()).id);
    }
}
