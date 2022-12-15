package ac.jnu.flowbot;

import ac.jnu.flowbot.data.EnvironmentData;
import ac.jnu.flowbot.events.EventManager;
import ac.jnu.flowbot.functions.*;
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

    private final EnvironmentData ed;
    private final JDA jda;
    private Guild guild;
    private final EventManager eventManager;
    private final WebHTTPRequester webHTTPRequester;

    private BotController() {
        ed = EnvironmentData.getInstance();
        jda = ed.getJDA();
        eventManager = new EventManager();
        webHTTPRequester = new WebHTTPRequester();
    }

    public void run() throws InterruptedException {
        eventManager.registerEvents(jda);
        jda.awaitReady();
        guild = ed.getMainGuild();
        validUtilities();
    }

    private void validUtilities() {
        new Thread(webHTTPRequester).start();
        FunctionManager.checkAuthorizationInfoMessage(Objects.requireNonNull(guild.getTextChannelById(Authorization.channel)));
        FunctionManager.checkFavoriteLanguages(guild, Objects.requireNonNull(guild.getTextChannelById(FavoriteLanguages.channel)));
        FunctionManager.checkPrivacySettings(Objects.requireNonNull(guild.getTextChannelById(PrivacySettings.channel)));
        DayUpdater.initInstance();
    }
}
