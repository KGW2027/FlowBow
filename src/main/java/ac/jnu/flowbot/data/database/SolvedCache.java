package ac.jnu.flowbot.data.database;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Solved.ac의 티어별 문제 목록을 저장하고 직렬화 할 수 있는 캐시 클래스
 *
 */
public class SolvedCache implements Serializable {

    @Serial
    private static final long serialVersionUID = 2000L;

    HashMap<SolvedTier, List<SolvedProblem>> problems;

    public SolvedCache() {
        problems = new HashMap<>();
    }

    public void update(SolvedTier tier, List<SolvedProblem> list) {
        problems.put(tier, list);
    }

    public List<SolvedProblem> get(SolvedTier tier) {
        return problems.getOrDefault(tier, new ArrayList<>());
    }

}
