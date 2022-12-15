package ac.jnu.flowbot.functions;


import ac.jnu.flowbot.data.database.College;
import ac.jnu.flowbot.data.EnvironmentData;
import ac.jnu.flowbot.data.database.DatabaseIntegration;
import ac.jnu.flowbot.data.database.MemberData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;

import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * 디스코드 방 참여를 위한 인증 절차를 관리합니다.
 */
public class Authorization implements Runnable {

    public static final long channel = 1038035269594648627L;
    private static final HashMap<Long, Authorization> progressAuthorizations = new HashMap<>();

    /**
     * 인증 절차를 안내하는 MessageEmbed를 반환합니다.
     *
     * @return MessageEmbed
     */
    public static MessageEmbed getAuthorizationEmbed() {
        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle("어서오세요! JNU FLOW 입니다.");
        builder.setColor(new Color(0x61F583));
        builder.setDescription(
                        """
                        이 채널은 전남대학생끼리 프로그래밍을 같이 공부해보면 좋겠다해서 만든 방입니다.
                        기본적으로 가벼운 분위기에서 자유롭게 커뮤니티를 즐겨주시면 되겠으나,
                        신천지와 같은 종교 및 정치단체의 활동은 발견 즉시 추방합니다.
                        
                        채널 내에서는 프로그래밍과 관련한 질문, 코드테스트 문제 풀이, 자랑,
                        과제 관련 질문 또는 같이 게임을 즐길 수도 있습니다.
                        
                        간단한 인증절차를 마치면 참여가능하며, 입력하신 정보는 인증 이후 서버 내에서 숨길 수 있습니다.
                        """
        );
        builder.setFooter("전 Java 좋아하는데...");

        return builder.build();
    }

    /**
     * 개인메세지로 오는 입력을 메인스레드에서 받아 전달합니다.
     * @param tc 어떤 유저의 입력인지
     * @param message 입력된 데이터
     */
    public static void sendData(Long tc, String message) {
        if(progressAuthorizations.containsKey(tc)) {
            progressAuthorizations.get(tc).dataInput = message;
        }
    }

    private final User user;
    private int progress;
    private String dataInput;
    private final MemberData memberData;

    public Authorization(User user) {
        this.user = user;
        this.progress = 0;
        dataInput = null;
        progressAuthorizations.put(user.getIdLong(), this);
        memberData = new MemberData();
        memberData.userId = user.getIdLong();
    }

    @Override
    public void run() {
        EnvironmentData.logger.newAuthorization(user.getIdLong());
        PrivateChannel privateChannel = user.openPrivateChannel().complete();
        try {

            privateChannel.sendMessage("""
                    안녕하세요! 인증절차에 참가해주셔서 감사합니다!
                    다음 절차에서 수집되는 정보들은 서버 내 역할 지급에 사용됩니다.
                                                        
                    개인정보를 공개하는 것에 거리낌이 있다면,
                    편하게 익명으로 참가하셔도됩니다!
                                                        
                    인증 절차 시작하겠습니다.
                    """).queue();
            privateChannel.sendMessageEmbeds(getEmbed()).queue();
            waitInput();

            synchronized (this) {
                if(dataInput.equals("익명")) dataInput = "익명".concat(randomNumberGenerator());
                memberData.name = dataInput;
            }

            progress++;
            privateChannel.sendMessageEmbeds(getEmbed()).queue();
            do {
                waitInput();

                synchronized (this) {
                    if (validIdYear(dataInput)){
                        memberData.id = dataInput;
                        break;
                    } else {
                        privateChannel.sendMessageEmbeds(getErrorEmbed()).queue();
                    }
                }
            }while(true);

            progress++;
            privateChannel.sendMessageEmbeds(getEmbed()).queue();
            waitInput();

            synchronized (this) {
                memberData.college = College.parseCollegeByName(dataInput);
            }
            progress++;
            privateChannel.sendMessageEmbeds(getEmbed()).queue();

            complete();

        } catch (InterruptedException ignored) { }
    }

    private void complete() {
        String sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        memberData.registerDate = sdf;

        Guild guild = EnvironmentData.getInstance().getMainGuild();
        Member member = guild.getMemberById(user.getIdLong());
        if(member == null) {
            user.openPrivateChannel().queue(privateChannel -> {
               privateChannel.sendMessage("디스코드 채널에서 사용자님을 찾을 수 없어서 강제종료되었습니다.").queue();
            });
            return;
        }

        List<Role> roles = new ArrayList<>();
        roles.add(RoleManager.getVerified());
        roles.add(RoleManager.getCollege(memberData.college));
        roles.add(RoleManager.getYear(memberData.id));
        guild.modifyMemberRoles(member, roles).queue();
        guild.modifyNickname(member, memberData.name).queue();

        user.openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("""
                                    역할 지급이 완료되었습니다!
                                    <#%general%>에서 반갑게 인사해요.
                                    학과나 학번 정보를 숨기고 싶다면 <#%privacy%>로 이동해주세요.
                                    선호하는 언어에 대해서 설정하고 싶다면 <#%language%>로 이동해주세요.
                                    """
                    .replace("%general%", "1038035269594648631")
                    .replace("%privacy%", String.valueOf(PrivacySettings.channel))
                    .replace("%language%", String.valueOf(FavoriteLanguages.channel))).queue();
        });

        try {
            DatabaseIntegration.getInstance().insertMemberData(memberData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        EnvironmentData.logger.completeAuthorization(user.getIdLong());
    }

    private String randomNumberGenerator() {
        return String.format("%07d", (int) (Math.random() * Math.pow(10, 7)));
    }

    private boolean validIdYear(String data) {
        String year = data.substring(0, 2);
        try{
            int i = Integer.parseInt(year);
            if(10 <= i && i <= Calendar.getInstance().get(Calendar.YEAR) - 2000) return true;
        } catch (Exception ignored) { }
        return false;
    }

    private void waitInput() throws InterruptedException {
        int request = 0;
        dataInput = null;

        while(dataInput == null && request++ < 60) Thread.sleep(1000);

        if(dataInput == null || request >= 60) {
            EnvironmentData.logger.timeoutAuthorization(user.getIdLong());
            user.openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage("""
                                        요청으로 부터 장시간지나 인증절차가 종료되었습니다.
                                        재시도를 원하시면 "<#%channel%>" 에서 다시 시작해주세요!
                                        """.replace("%channel%", String.valueOf(channel))).queue();
            });
            progressAuthorizations.remove(user.getIdLong());
            Thread.interrupted();
        }
    }

    private MessageEmbed getEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        switch(progress) {
            case 0:
                eb.setTitle("서버 내에서 사용할 이름을 입력해주세요!");
                eb.setDescription("""
                                실명이여도 되고, 별명이여도 상관없습니다!
                                디스코드 서버 내 별명이 이 이름으로 자동 변경될거에요.
                                
                                **익명**으로 입력할 시 '익명(난수)'의 형태로 설정됩니다.
                                예시: 익명23714
                                """);
                eb.setFooter("입력 예시 : 홍길동, 익명, ...");
                eb.setColor(new Color(0x71FFBE));
                break;
            case 1:
                eb.setTitle("학번을 입력해주세요!");
                eb.setDescription("""
                                학번을 풀로 적는 것은 개인정보 노출의 우려가 있습니다.
                                XX학번 할 때 XX만 입력해주세요.
                                """);
                eb.setFooter("입력 예시 : 19, 21학번, ...");
                eb.setColor(new Color(0x71FFBE));
                break;
            case 2:
                eb.setTitle("학과를 적어주세요!");
                eb.setDescription("""
                                 대신, 자세히 적지마시고,
                                 **공대 or 복수전공 or 기타**
                                 로 작성해주세요.
                                 """);
                eb.setFooter("입력 예시 : 공대, 공과대학, 복수전공, 복전, 기타, ...");
                eb.setColor(new Color(0x71FFBE));
                break;
            case 3:
                eb.setTitle("인증 절차가 완료되었습니다!");
                eb.setDescription("""
                        잠시만 기다려주세요!
                        데이터를 모두 처리하고 빠르게 진행해드릴게요!
                        """);
                eb.addField("이름", memberData.name, false);
                eb.addField("학번", memberData.id, false);
                eb.addField("학과", String.valueOf(memberData.college), false);
                eb.setColor(new Color(0x9CFF7C));
                break;
        }
        return eb.build();
    }

    private MessageEmbed getErrorEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        if (progress == 1) {
            eb.setTitle("뭔가 잘못됬습니다...");
            eb.setDescription("""
                    학번 인식에 실패했습니다.
                    앞에 공백이 있었거나, 잘못입력한게 아닌지 확인해주세요.
                    혹시 제대로 입력했는데 안된거라면... 관리자한테 DM주세요.
                    """);
            eb.setFooter("학번은 숨길 수 없습니다.");
            eb.setColor(new Color(0xFE4C1E));
        }
        return eb.build();
    }
}
