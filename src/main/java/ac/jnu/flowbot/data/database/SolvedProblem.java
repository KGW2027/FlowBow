package ac.jnu.flowbot.data.database;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class SolvedProblem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1000L;

    int problemId;
    String titleKo;
    long acceptUserCount;
    float avgTries;
    List<String> tags;

    public void setAcceptUserCount(long acceptUserCount) {
        this.acceptUserCount = acceptUserCount;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public void setAvgTries(float avgTries) {
        this.avgTries = avgTries;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setTitleKo(String titleKo) {
        this.titleKo = titleKo;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getTitleKo() {
        return titleKo;
    }

    public int getProblemId() {
        return problemId;
    }

    public long getAcceptUserCount() {
        return acceptUserCount;
    }

    public float getAvgTries() {
        return avgTries;
    }

    @Override
    public String toString() {
        return String.format("[문제 %d] %s \t 푼 사람 : %d명\t평균 시도 횟수 : %.3f회\t태그 : %s", problemId, titleKo, acceptUserCount, avgTries, Arrays.toString(tags.toArray()));
    }

}
