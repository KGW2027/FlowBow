package ac.jnu.flowbot.data;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.*;

public class EnvironmentData {

    boolean isLocal = true;

    private static EnvironmentData instance;
    public static Logger logger;

    public static EnvironmentData getInstance() {
        if(instance == null) {
            try {
                instance = new EnvironmentData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private String BOT_ID;
    private String SECRET_TOKEN;
    private String PUBLIC_KEY;
    private String BOT_TOKEN;
    private long MAIN_GUILD;

    private JDA jda;
    private Guild mainGuild;

    private EnvironmentData() throws IOException {
        if(isLocal) {
            File env = new File("./EnvironmentDatas");
            if(!env.exists()) throw new FileNotFoundException("Set isLocal to False");

            BufferedReader reader = new BufferedReader(new FileReader(env));
            String line;
            while((line = reader.readLine()) != null)
                updateData(line);
        } else {

        }
        logger = new Logger();
    }

    private void updateData(String line) {
        int indexOf = line.indexOf('=');
        String value = line.substring(indexOf+1);
        switch (line.substring(0, indexOf)) {
            case "APPLICATION_ID" -> BOT_ID = value;
            case "CLIENT_SECRET" -> SECRET_TOKEN = value;
            case "PUBLIC_KEY" -> PUBLIC_KEY = value;
            case "BOT_TOKEN" -> BOT_TOKEN = value;
            case "MAIN_GUILD" -> MAIN_GUILD = Long.parseLong(value);
        }
    }

    /**
     * 세팅된 JDA를 반환합니다.
     *
     * @return 초기 설정된 JDA
     */
    public JDA getJDA() {
        if(jda == null) {
            JDABuilder builder = JDABuilder.createDefault(BOT_TOKEN)
                    // 길드 멤버 전체에게 접근하기 위한 권한
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    // 상태 표시
                    .setStatus(OnlineStatus.IDLE)
                    .setActivity(Activity.playing("IDE"))
                    ;

            jda = builder.build();
        }
        return jda;
    }

    /**
     * 작동을 위한 디스코드 채널을 반환합니다.
     * 
     * @return 디스코드 채널
     */
    public Guild getMainGuild() {
        if(mainGuild == null) mainGuild = jda.getGuildById(MAIN_GUILD);

        return mainGuild;
    }



}
