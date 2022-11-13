package ac.jnu.flowbot.functions;

import ac.jnu.flowbot.data.EnvironmentData;
import ac.jnu.flowbot.data.SolvedRecommender;
import ac.jnu.flowbot.data.database.SolvedCache;
import ac.jnu.flowbot.data.database.SolvedProblem;
import ac.jnu.flowbot.data.database.SolvedTier;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolvedProblemParser {

    private static final String COUNT_PATTERN = "(?<=(\"count\":))(.*?)(?=,)";
    private static final String PROB_LINE_PATTERN = "(?<=(\\{\"problemId\":))(.*?)(?=]}]})";
    private static final String TITLE_KO_PATTERN = "(?<=(\"titleKo\":\"))(.*?)(?=\",)";
    private static final String ACCEPT_USER_COUNT_PATTERN = "(?<=(\"acceptedUserCount\":))(.*?)(?=,)";
    private static final String AVG_TRIES_PATTERN = "(?<=(\"averageTries\":))(.*?)(?=,)";
    private static final String TAG_KO_PATTERN ="(?<=(\"ko\",\"name\":\"))(.*?)(?=\")";

    private static final long dayProblemChannel = 1041293339183566868L;


    public static void parseSolvedData() {
        new Thread(() -> {
            for (SolvedTier tier : SolvedTier.values()) {
                if(tier.getTier() < 18) continue;
                try {
                    parseSolvedData(tier);
                    Thread.sleep(20 * 1000);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void parseSolvedData(SolvedTier tier) throws IOException {

        String json = getJSONData(tier, 1);

        Pattern countPattern = Pattern.compile(COUNT_PATTERN);
        Pattern elementPattern = Pattern.compile(PROB_LINE_PATTERN);

        Matcher matcher;
        int maxPage = 1;
        int curPage = 1;

        List<SolvedProblem> problems = new ArrayList<>();
        do{

            if(curPage == 1) {
                matcher = countPattern.matcher(json);
                if(!matcher.find()) {
                    EnvironmentData.logger.failedParseSolvedProblem(SolvedTier.BRONZE_V);
                    return;
                }
                maxPage = (int) Math.ceil(Integer.parseInt(matcher.group()) / 50.0f);
            } else {
                json = getJSONData(tier, curPage);
            }

            matcher = elementPattern.matcher(json);
            while(matcher.find()) {
                String element = matcher.group();
                SolvedProblem sp = parseProblem(element);
                problems.add(sp);
            }

        }while(++curPage <= maxPage);

        SolvedRecommender.getInstance().setCache(tier, problems);
        System.out.println("Solved Tier " + tier.toString() + " 에 대한 parse 완료, Count : " + problems.size());
    }

    private static SolvedProblem parseProblem(String element) {

        SolvedProblem sp = new SolvedProblem();

        Pattern titlePattern = Pattern.compile(TITLE_KO_PATTERN);
        Pattern acceptCountPattern = Pattern.compile(ACCEPT_USER_COUNT_PATTERN);
        Pattern avgTriesPattern = Pattern.compile(AVG_TRIES_PATTERN);
        Pattern tagPattern = Pattern.compile(TAG_KO_PATTERN);

        Matcher innerMatcher;

        String id = element.split(",", 2)[0];
        sp.setProblemId(Integer.parseInt(id));

        innerMatcher = titlePattern.matcher(element);
        innerMatcher.find();
        sp.setTitleKo(innerMatcher.group());

        innerMatcher = acceptCountPattern.matcher(element);
        innerMatcher.find();
        sp.setAcceptUserCount(Long.parseLong(innerMatcher.group()));

        innerMatcher = avgTriesPattern.matcher(element);
        innerMatcher.find();
        sp.setAvgTries(Float.parseFloat(innerMatcher.group()));

        innerMatcher = tagPattern.matcher(element);
        List<String> tags = new ArrayList<>();
        while (innerMatcher.find()) {
            tags.add(innerMatcher.group());
        }
        sp.setTags(tags);

        return sp;
    }

    private static String getJSONData(SolvedTier tier, int page) throws IOException {
        String path = tier.getURL().concat(String.format("&page=%d", page));
        EnvironmentData.logger.sendHTTPRequest(path);

        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if(conn.getResponseCode() != 200) {
            EnvironmentData.logger.responseHTTPRequest(path, conn.getResponseCode());
            System.out.println(path +" 와의 연결에 실패했습니다. ResponseCode : " + conn.getResponseCode());
            return null;
        }
        EnvironmentData.logger.responseHTTPRequest(path, conn.getResponseCode());

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
            builder.append(line);
        }
        br.close();

        return builder.toString();
    }

    public static void sendRecommendDayProblems() {
        TextChannel tc = EnvironmentData.getInstance().getMainGuild().getTextChannelById(dayProblemChannel);
        List<MessageEmbed> recommends = new ArrayList<>();
        recommends.add(SolvedRecommender.getInstance().getDayRandomProblem("bronze"));
        recommends.add(SolvedRecommender.getInstance().getDayRandomProblem("silver"));
        recommends.add(SolvedRecommender.getInstance().getDayRandomProblem("gold"));
        recommends.add(SolvedRecommender.getInstance().getDayRandomProblem("platinum"));

        tc.sendMessageEmbeds(recommends).complete();
    }
}
