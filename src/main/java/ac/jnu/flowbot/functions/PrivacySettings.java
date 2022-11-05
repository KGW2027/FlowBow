package ac.jnu.flowbot.functions;

import ac.jnu.flowbot.data.EnvironmentData;
import ac.jnu.flowbot.data.database.DatabaseIntegration;
import ac.jnu.flowbot.data.database.MemberData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PrivacySettings implements Runnable {

    public static final long channel = 1038365824764870676L;

    public static MessageEmbed getEmbed() {
        EmbedBuilder builder = new EmbedBuilder();

        builder.setColor(new java.awt.Color(0xD32E47));

        builder.setTitle("정보 공개 여부를 설정할 수 있습니다.");
        builder.setDescription("""
                아래의 버튼을 눌러서 학번과 학과 역할을 숨기거나 표시할 수 있습니다.
                """);

        return builder.build();
    }

    private Member member;
    private InteractionHook hook;

    public PrivacySettings(Member member, InteractionHook hook) {
        this.member = member;
        this.hook = hook;
    }

    @Override
    public void run() {
        MemberData md = DatabaseIntegration.getInstance().getMemberData(member.getIdLong());
        if(md == null) {
            hook.sendMessage("인증 정보가 없어 정보 공개여부를 변경할 수 없습니다.").complete().delete().queueAfter(5, TimeUnit.SECONDS);
            return;
        }

        List<Role> roleList = new ArrayList<>(member.getRoles());
        Role[] colleges = RoleManager.getColleges();
        Role year = RoleManager.getYear(md.id);
        Role college = RoleManager.getCollege(md.college);
        if(!roleList.contains(colleges[0]) && !roleList.contains(colleges[1]) && !roleList.contains(colleges[2])) { // 정보 공개
            roleList.add(year);
            roleList.add(college);
        } else { // 정보 숨기기
            roleList.remove(year);
            roleList.remove(college);
        }
        EnvironmentData.getInstance().getMainGuild().modifyMemberRoles(member, roleList).queue();
        hook.sendMessage("적용되었습니다!").complete().delete().queueAfter(5, TimeUnit.SECONDS);
    }
}
