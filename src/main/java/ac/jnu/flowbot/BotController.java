package ac.jnu.flowbot;

import ac.jnu.flowbot.data.EnvironmentData;
import ac.jnu.flowbot.events.EventManager;
import ac.jnu.flowbot.functions.Authorization;
import ac.jnu.flowbot.functions.FunctionManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Objects;

public class BotController {

    private static BotController instance;
    public static BotController getInstance() {
        if(instance == null)
            instance = new BotController();
        return instance;
    }

    private EnvironmentData ed;
    private JDA jda;
    private Guild guild;
    private EventManager eventManager;

    private BotController() {
        ed = EnvironmentData.getInstance();
        jda = ed.getJDA();
        eventManager = new EventManager();
    }

    public void run() throws InterruptedException {
        eventManager.registerEvents(jda);
        jda.awaitReady();
        guild = ed.getMainGuild();
        validUtilities();
    }

    private void validUtilities() {
        FunctionManager.checkAuthorizationInfoMessage(Objects.requireNonNull(guild.getTextChannelById(Authorization.channel)));
    }
}
