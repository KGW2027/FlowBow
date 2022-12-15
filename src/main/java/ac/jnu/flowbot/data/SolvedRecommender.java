package ac.jnu.flowbot.data;

import ac.jnu.flowbot.data.database.SolvedCache;
import ac.jnu.flowbot.data.database.SolvedProblem;
import ac.jnu.flowbot.data.database.SolvedTier;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class SolvedRecommender {

    File file;
    SolvedCache cache;

    private static final String SOLVED_LINK = "https://www.acmicpc.net/problem/%d";
    Queue<Integer> recent10Days;

    private static SolvedRecommender instance;
    public static SolvedRecommender getInstance() {
        if(instance == null)
            instance = new SolvedRecommender();
        return instance;
    }

    private SolvedRecommender() {
        recent10Days = new LinkedList<>();
        try {
            file = new File("./datas/solvedCache.dat");
            file.getParentFile().mkdirs();
            if (!file.exists()) {
                file.createNewFile();
                cache = new SolvedCache();
            } else {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                cache = (SolvedCache) ois.readObject();
                ois.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            EnvironmentData.logger.sendException(e);
            e.printStackTrace();
        }
    }

    private void syncFile() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(cache);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            EnvironmentData.logger.sendException(e);
            e.printStackTrace();
        }
    }

    public void setCache(SolvedTier tier, List<SolvedProblem> prob) {
        cache.update(tier, prob);
        syncFile();
    }

    public MessageEmbed getRecommendProblem(String level, String grade) {
        return getRecommendProblem(level, grade, new String[0]);
    }

    /**
     * Solved 문제를 랜덤으로 추천받기 위한 함수입니다.
     * @param level 레벨을 선택합니다. ( BRONZE ~ RUBY, UNRATED는 선택할 수 없음 )
     * @param grade 등급을 선택합니다. ( I ~ V )
     * @param tags 필터링할 태그가 있다면 입력합니다.
     * @return 랜덤으로 추천된 문제가 Embed로 출력됩니다.
     */
    public MessageEmbed getRecommendProblem(String level, String grade, String[] tags) {
        String tierStr = level.toUpperCase().concat("_").concat(grade.toUpperCase());
        SolvedTier tier = SolvedTier.valueOf(tierStr);

        List<SolvedProblem> probs = cache.get(tier);
        if(probs.size() == 0) {
            return getErrorEmbed();
        }

        int randomKey;
        SolvedProblem sp;
        int tries = 0;
        do {
            randomKey = (new Random()).nextInt(probs.size());
            sp = probs.get(randomKey);
        }while(tries++ < 25 && !validTags(sp.getTags(), tags));

        if(tries >= 25) {
            return getMaxTries();
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(String.format("%s (문제 %d번) 입니다.", sp.getTitleKo(), sp.getProblemId()), String.format(SOLVED_LINK, sp.getProblemId()));
        eb.setColor(new java.awt.Color(0x5FD046));
        eb.addField("레벨/등급", level.concat(" ").concat(grade), false);
        eb.addField("문제를 푼 사람", String.valueOf(sp.getAcceptUserCount()), false);
        eb.addField("평균 시도 횟수", String.valueOf(sp.getAvgTries()), false);
        eb.addField("태그", Arrays.toString(sp.getTags().toArray(new String[0])).replace("[", "").replace("]", ""), false);
        eb.setFooter("행운을 빕니다!");

        return eb.build();
    }

    /**
     * 매일 한 문제 Solved에 등록된 백준 문제를 반환합니다.
     * @param level 레벨
     * @return 문제 Embed
     */
    public MessageEmbed getDayRandomProblem(String level) {
        SolvedTier[] tiers = SolvedTier.getTierList(level);

        int bound = 0, grade = 0;
        for(SolvedTier tier : tiers) bound += cache.get(tier).size();

        SolvedProblem sp;
        do {
            int randKey = (new Random()).nextInt(bound), subs = 0;
            grade = 0;
            for (; grade < 4; grade++) {
                int size = cache.get(tiers[grade]).size();
                if (randKey < subs + size) break;
                subs += size;
            }

            List<SolvedProblem> list = cache.get(tiers[grade]);
            sp = list.get(randKey - subs);
        } while(!sp.isKoreanTranslated() || sp.getAcceptUserCount() < 100 || recent10Days.contains(sp.getProblemId()));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String[] date = sdf.format(new Date()).split("-");
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(String.format("%s년 %s월 %s일의 %s 문제는 %s (문제 %d번) 입니다.", date[0], date[1], date[2], level.toUpperCase(), sp.getTitleKo(), sp.getProblemId()), String.format(SOLVED_LINK, sp.getProblemId()));
        eb.setColor(new java.awt.Color(0x5FD046));
        eb.addField("레벨/등급", level.concat(" ").concat(parseGradeByInt(grade)), false);
        eb.addField("문제를 푼 사람", String.valueOf(sp.getAcceptUserCount()), false);
        eb.addField("평균 시도 횟수", String.valueOf(sp.getAvgTries()), false);
        eb.addField("태그", "|| " + Arrays.toString(sp.getTags().toArray(new String[0])).replace("[", "").replace("]", "") + " ||", false);
        eb.setFooter("행운을 빕니다!");

        recent10Days.add(sp.getProblemId());
        removeOverTenDayProb();
        return eb.build();
    }

    public void removeOverTenDayProb() {
        if(recent10Days.size() >= 40) recent10Days.poll();
    }

    private String parseGradeByInt(int i) {
        switch(i) {
            case 0 : return "V";
            case 1 : return "IV";
            case 2 : return "III";
            case 3 : return "II";
            case 4 : return "I";
        }
        return "";
    }

    private boolean validTags(List<String> tags, String[] conds) {
        boolean[] valid = new boolean[conds.length];

        String[] condsSpaceConverted = new String[conds.length];
        for(int key = 0 ; key < conds.length ; key++) {
            condsSpaceConverted[key] = conds[key].replace("_", " ");
        }

        for(String t : tags) {
            for(int i = 0 ; i < conds.length ; i++) {
                if(!valid[i] && t.contains(condsSpaceConverted[i])) valid[i] = true;
            }
        }

        for(boolean v : valid) if(!v) return false;
        return true;
    }

    private MessageEmbed getErrorEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("에러가 발생했습니다!");
        eb.setDescription("1. Level과 Grade를 제대로 입력했는지 확인해주세요.\n2.정보를 가져오는 중에 오류가 발생했을 수 있습니다. 다시 시도해보세요!");
        eb.setColor(new java.awt.Color(0xB72A2A));
        return eb.build();
    }

    private MessageEmbed getMaxTries() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("문제를 찾을 수 없습니다.");
        eb.setDescription("태그를 너무 많이 작성했거나, 길게 적은 것 같습니다.\nhttps://www.acmicpc.net/problem/tags 를 참고하시고\n전부 적는 것보다 조금만 적는게 도움됩니다.\n\n예시) 유클리드 호제법 -> 유클리드");
        eb.setColor(new java.awt.Color(0xB72A2A));
        return eb.build();
    }

}
