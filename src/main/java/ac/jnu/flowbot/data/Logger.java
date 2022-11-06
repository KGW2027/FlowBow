package ac.jnu.flowbot.data;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public enum LogType {
        NORMAL,
        EXCEPTION,
        JOIN,
        LEAVE,
        MESSAGE
    }

    File logFile;
    final BufferedWriter writer;

    public Logger() throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        int fileNum = 1;
        do{
            String fileName = String.format("floatbow-log-%s-%d.log", sdf.format(new Date()), fileNum++);
            logFile = new File("./logs/".concat(fileName));
        }while(logFile.exists());
        logFile.getParentFile().mkdirs();
        logFile.createNewFile();
        writer = new BufferedWriter(new FileWriter(logFile));
    }

    private String createLogString(LogType type, String log) {
        String time = new SimpleDateFormat("[HH:mm:ss]").format(new Date());
        return String.format("%s [%s] [%s]: %s\r\n", time, type, Thread.currentThread(), log);
    }

    private boolean addLog(LogType type, String log) {
        try{
            synchronized (writer) {
                writer.write(createLogString(type, log));
                writer.flush();
            }
            return true;
        }catch(Exception ex) {
            return false;
        }
    }

    public boolean newAuthorization(Long userId) {
        return addLog(LogType.NORMAL, String.format("%d님이 인증 절차를 시작했습니다.", userId));
    }

    public boolean completeAuthorization(Long userId) {
        return addLog(LogType.NORMAL, String.format("%d님이 인증 절차를 완료했습니다.", userId));
    }

    public boolean timeoutAuthorization(Long userId) {
        return addLog(LogType.NORMAL, String.format("%d님의 인증 절차가 시간 초과로 만료되었습니다.", userId));
    }

    public boolean newMemberJoin(Long userId) {
        return addLog(LogType.JOIN, String.format("%d님이 채널에 입장했습니다.", userId));
    }

    public boolean leaveMember(Long userId) {
        return addLog(LogType.LEAVE, String.format("%d님이 채널에서 퇴장했습니다.", userId));
    }

    public boolean editPrivacySetting(Long userId, boolean hide) {
        if(hide) return addLog(LogType.NORMAL, String.format("%d님이 정보를 비공개하였습니다.", userId));
        return addLog(LogType.NORMAL, String.format("%d님이 정보를 공개하였습니다.", userId));
    }

    public boolean logChattedMessage(Long userId, String raw) {
        return addLog(LogType.MESSAGE, String.format("%d -> %s", userId, raw));
    }

    public boolean sendException(Exception exception) {
        boolean success = true;
        if(!addLog(LogType.EXCEPTION, exception.getMessage())) success = false;
        for(StackTraceElement ste : exception.getStackTrace())
            if(!addLog(LogType.EXCEPTION, ste.toString())) success = false;
        return success;
    }

    public boolean rateProgrammersProblem(long userId, long probId) {
        return addLog(LogType.NORMAL, String.format("%d님이 프로그래머즈 문제 %d를 평가했습니다.", userId, probId));
    }

    public boolean infoProgrammersProblem(long userId, String probId) {
        return addLog(LogType.NORMAL, String.format("%d님이 프로그래머즈 문제 %s를 조회했습니다.", userId, probId));
    }

    public boolean listProgrammersProblem(long userId, int level) {
        return addLog(LogType.NORMAL, String.format("%d님이 프로그래머즈 문제 레벨 %d 목록을 조회했습니다.", userId, level));
    }

    public boolean randomProgrammersProblem(long userId, int level) {
        return addLog(LogType.NORMAL, String.format("%d님이 프로그래머즈 문제 레벨 %d에서 랜덤으로 문제를 받았습니다.", userId, level));
    }

    public boolean sendHTTPRequest(String url) {
        return addLog(LogType.NORMAL, String.format("%s 에 HTTP Request를 전송했습니다.", url));
    }

    public boolean responseHTTPRequest(String url, int responseCode) {
        return addLog(LogType.NORMAL, String.format("%s 에서 받은 응답 : %d", url, responseCode));
    }

}
