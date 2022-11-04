package ac.jnu.flowbot;

import net.dv8tion.jda.api.JDABuilder;

import java.io.File;
import java.io.IOException;

public class LifeCycle {

    JDABuilder builder;

    private LifeCycle() {

    }

    public static void main(String[] args) throws IOException {
        File file = new File("./EnvironmentDatas");
        System.out.println(file.getCanonicalFile() + " > " + file.exists());
    }
}
