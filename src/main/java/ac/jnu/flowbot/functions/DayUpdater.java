package ac.jnu.flowbot.functions;

import ac.jnu.flowbot.data.EnvironmentData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DayUpdater {

    private static DayUpdater instance;
    public static DayUpdater initInstance() {
        if(instance == null)
            instance = new DayUpdater();
        return instance;
    }

    private final int updateHour = 23;
    private final int updateMin = 60;
    private final int updateSec = 60;

    public DayUpdater() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String[] time = sdf.format(new Date()).split(":");

        int hours = updateHour - Integer.parseInt(time[0]);
        int mins = updateMin - Integer.parseInt(time[1]);
        int secs = updateSec - Integer.parseInt(time[2]);
        EnvironmentData.logger.reserveDayUpdater(hours, mins, secs);

        int waitTime = (hours * 60 * 60) + (mins * 60) + secs;

        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(this::runDayFunctions, waitTime, 24*60*60, TimeUnit.SECONDS);
    }

    private void runDayFunctions() {
         EnvironmentData.logger.runDayUpdater();
         SolvedProblemParser.sendRecommendDayProblems();
         if(Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")).get(Calendar.DAY_OF_WEEK) == 1)
             SolvedProblemParser.parseSolvedData();
    }
}
