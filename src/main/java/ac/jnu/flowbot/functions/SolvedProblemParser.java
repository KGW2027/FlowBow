package ac.jnu.flowbot.functions;

import ac.jnu.flowbot.data.EnvironmentData;
import ac.jnu.flowbot.data.SolvedRecommender;
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
    private static final String PROB_LINE_PATTERN = "(?<=(\\{\"problemId\":))(.*?)(?=}]}])";

    private static final String TITLE_KO_CONTAINS_PATTERN = "(?<=(\"titles\":\\[))(.*?)(?=])";
    private static final String TITLE_KO_PATTERN = "(?<=(\"titleKo\":\"))(.*?)(?=\",)";
    private static final String ACCEPT_USER_COUNT_PATTERN = "(?<=(\"acceptedUserCount\":))(.*?)(?=,)";
    private static final String AVG_TRIES_PATTERN = "(?<=(\"averageTries\":))(.*?)(?=,)";
    private static final String TAG_KO_PATTERN ="(?<=(\"ko\",\"name\":\"))(.*?)(?=\")";

    private static final long dayProblemChannel = 1041293339183566868L;


    public static void parseSolvedData() {
        new Thread(() -> {
            for (SolvedTier tier : SolvedTier.values()) {
                try {
//                    System.out.printf("%s 티어를 검색 하는 중\n", tier);
                    parseSolvedData(tier);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void parseSolvedData(SolvedTier tier) throws IOException, InterruptedException {

        String json = getJSONData(tier, 1);

        Pattern countPattern = Pattern.compile(COUNT_PATTERN);
        Pattern elementPattern = Pattern.compile(PROB_LINE_PATTERN);

        Matcher matcher;
        int maxPage = 1;
        int curPage = 1;
        int errorCount = 0;

        List<SolvedProblem> problems = new ArrayList<>();
        do{
            if(curPage == 1) {
                matcher = countPattern.matcher(json);
                if(!matcher.find()) {
//                    EnvironmentData.logger.failedParseSolvedProblem(tier);
                    return;
                }
                maxPage = (int) Math.ceil(Integer.parseInt(matcher.group()) / 50.0f);
            } else {
                Thread.sleep(6 * 1000);
                json = getJSONData(tier, curPage);
                if(isNum(json)) {
                    int code = Integer.parseInt(json);
                    if(errorCount++ >= 3) {
//                        System.out.println("3번 에러가 반복되어 검색이 종료됩니다.");
                        return;
                    }

//                    System.out.printf("HTTP RESPONSE CODE : %d, 10분 대기\n", code);
                    Thread.sleep(10 * 60 * 1000);
                    continue;
                }
            }
//            System.out.printf("페이지 %d / %d\n", curPage, maxPage);

            matcher = elementPattern.matcher(json);
            while(matcher.find()) {
                String element = matcher.group();
                SolvedProblem sp = parseProblem(element);
                problems.add(sp);
            }
            errorCount = 0;

        }while(++curPage <= maxPage);

        SolvedRecommender.getInstance().setCache(tier, problems);
//        System.out.println("Solved Tier " + tier + " 에 대한 parse 완료, Count : " + problems.size());
    }

    private static SolvedProblem parseProblem(String element) {

        SolvedProblem sp = new SolvedProblem();

        Pattern titlePattern = Pattern.compile(TITLE_KO_PATTERN);
        Pattern acceptCountPattern = Pattern.compile(ACCEPT_USER_COUNT_PATTERN);
        Pattern avgTriesPattern = Pattern.compile(AVG_TRIES_PATTERN);
        Pattern tagPattern = Pattern.compile(TAG_KO_PATTERN);

        Pattern titleContainsPattern = Pattern.compile(TITLE_KO_CONTAINS_PATTERN);
        final String koCheck = "\"language\":\"ko\"";

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

        innerMatcher = titleContainsPattern.matcher(element);
        innerMatcher.find();
        sp.setIsKorean(innerMatcher.group().contains(koCheck));

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
//        EnvironmentData.logger.sendHTTPRequest(path);

        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if(conn.getResponseCode() != 200) {
            EnvironmentData.logger.responseHTTPRequest(path, conn.getResponseCode());
            System.out.println(path +" 와의 연결에 실패했습니다. ResponseCode : " + conn.getResponseCode());
            return String.valueOf(conn.getResponseCode());
        }
//        EnvironmentData.logger.responseHTTPRequest(path, conn.getResponseCode());

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

    private static boolean isNum(String num) {
        try{
            int i = Integer.parseInt(num);
            return true;
        } catch (Exception failed) { return false; }
    }
}
