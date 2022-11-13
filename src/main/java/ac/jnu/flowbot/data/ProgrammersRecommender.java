package ac.jnu.flowbot.data;

import ac.jnu.flowbot.data.database.Programmers;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.io.*;

public class ProgrammersRecommender {

    File file;
    Programmers manager;

    private static ProgrammersRecommender instance;
    public static ProgrammersRecommender getInstance() {
        if(instance == null)
            instance = new ProgrammersRecommender();
        return instance;
    }

    private ProgrammersRecommender() {
        try {
            file = new File("./datas/programmeres.dat");
            file.getParentFile().mkdirs();
            if (!file.exists()) {
                file.createNewFile();
                manager = new Programmers();
            } else {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                manager = (Programmers) ois.readObject();
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
            oos.writeObject(manager);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            EnvironmentData.logger.sendException(e);
            e.printStackTrace();
        }
    }

    /**
     * 프로그래머즈 문제 평가를 데이터베이스에 추가합니다.
     * @param uid 평가자
     * @param id 문제의 ID
     * @param level 문제의 레벨
     * @param name 문제의 이름
     * @param difficult 난이도 적절성
     * @param hasEff 문제에 효율성 검사가 있는지
     * @param good 문제의 완성도 / 재미
     */
    public void addProblem(long uid, long id, int level, String name, int difficult, int good, boolean hasEff) {
        addProblem(uid, id, level, name, difficult, good, hasEff, "");
    }
    public void addProblem(long uid, long id, int level, String name, int difficult, int good, boolean hasEff, String comment) {
        manager.addProblem(uid, id, level, name, difficult, good, hasEff, comment);
        syncFile();
    }

    /**
     * 프로그래머즈 문제에 대해 평가합니다.
     * @param uid 평가자
     * @param id 문제의 ID
     * @param difficult 문제의 난이도 적절성
     * @param good 문제의 완성도 / 재미
     */
    public boolean rateProblem(long uid, long id, int difficult, int good) {
        return rateProblem(uid, id, difficult, good, "");
    }

    public boolean rateProblem(long uid, long id, int difficult, int good, String comment) {
        if(!manager.rateProblem(uid, id, difficult, good, comment)) return false;
        syncFile();
        return true;
    }

    /**
     * 프로그래머즈 문제 평가 정보를 가져옵니다.
     * @param id 문제 ID 
     * @return 문제 정보 Embed
     */
    public MessageEmbed getProblemInfo(long id) {
        return manager.getProblemInfo(id);
    }

    /**
     * 프로그래머즈 문제 평가 정보를 가져옵니다.
     * @param name 문제 이름
     * @return 문제 정보 Embed
     */
    public MessageEmbed getProblemInfo(String name) {
        return manager.getProblemInfo(name);
    }


    public Object[][] getProblemListByLevel(int level) {
        return manager.getProblemByLevel(level);
    }

    /**
     * 프로그래머즈의 레벨별 평가 기록을 가져옵니다.
     * @param level 문제 레벨
     * @param hasEff 효율성 검사가 있는 문제들만 가져올지 여부
     * @return 정렬된 데이터 Embed
     */
    public Object[][] getProblemListByLevel(int level, boolean hasEff) {
        return manager.getProblemByLevel(level, hasEff);
    }

    public MessageEmbed getRandomByLevel(int level) {
        return getRandomByLevel(level, false);
    }

    public MessageEmbed getRandomByLevel(int level, boolean hasEff) {
        return manager.getRandomProblem(level, hasEff);
    }

}
