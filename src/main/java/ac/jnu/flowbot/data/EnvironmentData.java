package ac.jnu.flowbot.data;

import java.io.*;

public class EnvironmentData {

    boolean isLocal = true;

    private static EnvironmentData instance;
    public static EnvironmentData getInstance() throws IOException {
        if(instance == null)
            instance = new EnvironmentData();
        return instance;
    }


    private EnvironmentData() throws IOException {
        if(isLocal) {
            File env = new File("./EnvironmentDatas");
            if(!env.exists()) throw new FileNotFoundException("Set isLocal to False");

            BufferedReader reader = new BufferedReader(new FileReader(env));

        }
    }

    private String botToken;




}
