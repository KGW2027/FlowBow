package ac.jnu.flowbot.events;

import net.dv8tion.jda.api.JDA;

public class EventManager {

    public EventManager() { }

    public void registerEvents(JDA jda) {
        jda.addEventListener(new InteractionAdapter());
    }

}
