package ac.jnu.flowbot.data.database;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class Programmers implements Serializable {

    @Serial
    private static final long serialVersionUID = 10L;
    private static final String url = "https://school.programmers.co.kr/learn/courses/30/lessons/";

    class ProblemRate implements Serializable, Comparable<ProblemRate>{

        @Serial
        private static final long serialVersionUID = 1L;

        long probId; // 문제 ID
        String name; // 문제 이름
        long good; // 알고리즘이 좋은가
        long difficult; // 체감레벨
        List<Long> ratersId; // 평가자 목록
        long raters; // 평가자 수
        int level; // 실제 프로그래머즈 레벨
        List<String> comments; // 한줄평
        boolean hasEfficiency; //효율성 검사 여부

        @Override
        public int compareTo(@NotNull ProblemRate o) {
            if(good + difficult > o.good + o.difficult) return 1;
            else if(good + difficult < o.good + o.difficult) return -1;
            if(hasEfficiency) return 1;
            return 0;
        }
    }

    HashMap<Integer, List<Long>> levelInfo;
    HashMap<Long, ProblemRate> probInfo;

    public Programmers() {
        levelInfo = new HashMap<>();
        probInfo = new HashMap<>();
    }

    public void addProblem(long uid, long id, int level, String name, int difficult, int good, boolean haseff, String comment) {
        ProblemRate pr = new ProblemRate();
        pr.probId = id;
        pr.ratersId = new ArrayList<>(List.of(uid));
        pr.level = level;
        pr.name = name;
        pr.difficult = difficult;
        pr.good = good;
        pr.hasEfficiency = haseff;
        pr.comments = comment.equals("") ? new ArrayList<>() : new ArrayList<>(List.of(new String[]{comment}));
        pr.raters = 1;

        List<Long> lis = levelInfo.getOrDefault(level, new ArrayList<>());
        lis.add(id);
        levelInfo.put(level, lis);

        probInfo.put(id, pr);
    }

    public boolean rateProblem(long uid, long id, int difficult, int good, String comment) {
        ProblemRate pr = probInfo.get(id);
        if(pr.ratersId.contains(uid)) return false;
        pr.ratersId.add(uid);
        pr.raters++;
        pr.difficult+=difficult;
        pr.good+=good;
        if(!comment.equals("")) pr.comments.add(comment.replace("\r", "").replace("\n", ""));
        return true;
    }

    private long getProblemIdByName(String name) {
        for(Long id : probInfo.keySet()) {
            if(probInfo.get(id).name.equals(name)) return id;
        }
        return -1;
    }


    public MessageEmbed getProblemInfo(String name) {
        long findId = getProblemIdByName(name);
        if(findId == -1) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(new java.awt.Color(0xA41414));
            builder.setTitle(name + " 문제 정보를 찾을 수 없습니다...");
            builder.setDescription("앞 뒤 띄어쓰기 여부등을 확인해주세요.\n혹은 아직 정보입력이 안 된 문제일 수도 있습니다.");
            return builder.build();
        }
        return getProblemInfo(findId);
    }

    public MessageEmbed getProblemInfo(long id) {
        EmbedBuilder builder = new EmbedBuilder();
        if(!probInfo.containsKey(id)) {
            builder.setColor(new java.awt.Color(0xA41414));
            builder.setTitle(id + "번 문제를 찾을 수 없습니다...");
            builder.setDescription("입력을 한 번 더 확인해보세요.\n아니면, 정보입력이 안 된 문제일 수도 있습니다.");
        } else {
            ProblemRate pr = probInfo.get(id);
            builder.setColor(new java.awt.Color(0x68C72B));
            builder.setTitle(String.format("%s (%d) 문제 정보입니다.", pr.name, pr.probId), url.concat(String.valueOf(pr.probId)));
            builder.addField("문제 레벨", String.valueOf(pr.level), false);
            builder.addField("효율성 검사 여부", pr.hasEfficiency ? "있음" : "없음", true);
            builder.addField("평가자 수", String.valueOf(pr.raters), true);
            double diffScore = Math.round( (pr.difficult / (double) pr.raters) * 100 ) / 100.0f;
            builder.addField("체감레벨", diffScore + " 레벨", false);
            double goodScore = Math.round( (pr.good / (double) pr.raters) * 100 ) / 100.0f;
            builder.addField("문제가 좋은가?", goodScore + " / 5", true);

            StringBuilder description = new StringBuilder().append("\n");
            if(pr.comments.size() > 0) {
                for(int key = pr.comments.size()-1 ; key >= 0 ; key++) {
                    description.append(pr.comments.get(key)).append(" - ").append(pr.ratersId.get(key)).append("\n");
                }
            }
            builder.setDescription(description);
        }
        return builder.build();
    }

    public Object[][] getProblemByLevel(int level) {
        return getProblemByLevel(level, false);
    }

    public Object[][] getProblemByLevel(int level, boolean checkEff) {
        List<Object[]> problemList = new ArrayList<>();
        List<Long> levelProblems = levelInfo.getOrDefault(level, new ArrayList<>());

        if(levelProblems.size() > 0) {
            List<ProblemRate> prs = new ArrayList<>();
            for(Long l : levelProblems) prs.add(probInfo.get(l));
            prs.sort(Collections.reverseOrder());
            for(ProblemRate pr : prs) {
                if(checkEff) {
                    if(pr.hasEfficiency) problemList.add(new Object[]{pr.probId, pr.name, pr.raters});
                } else {
                    problemList.add(new Object[]{pr.probId, pr.name, pr.raters});
                }
            }
        }

        return problemList.toArray(new Object[0][0]);
    }

    public MessageEmbed getRandomProblem(int level, boolean checkEff) {
        List<Long> levelProblems = levelInfo.getOrDefault(level, new ArrayList<>());
        EmbedBuilder builder = new EmbedBuilder();
        if(levelProblems.size() <= 0) {
            builder.setColor(new java.awt.Color(0xF54646));
            builder.setTitle(String.format("%d 레벨에 등록된 정보가 없습니다.", level));
            builder.setDescription("열심히 추가하도록 하겠습니다...");
        } else {
            int random = (new Random()).nextInt(levelProblems.size());
            ProblemRate pr = probInfo.get(levelProblems.get(random));
            if(checkEff) {
                int start = random;
                while(!pr.hasEfficiency && start != ++random) {
                    if(random >= probInfo.size()) random = 0;
                    pr = probInfo.get(levelProblems.get(random));
                }
            }
            builder.setColor(new java.awt.Color(0x73FF9E));
            builder.setTitle(String.format("레벨 %d 추천문제는 %s ( %d ) 입니다.", level, pr.name, pr.probId),"https://school.programmers.co.kr/learn/courses/30/lessons/" + pr.probId);
        }
        return builder.build();
    }
}
