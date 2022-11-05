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

}
