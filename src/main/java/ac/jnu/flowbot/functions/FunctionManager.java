package ac.jnu.flowbot.functions;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;

public class FunctionManager {

    /**
     * #어서와요 채널에 인증 절차 안내를 위한 메세지가 유지되고 있는지 검사합니다.
     *
     * @param welcomeChannel #어서와요 채널의 ID
     */
    public static void checkAuthorizationInfoMessage(TextChannel welcomeChannel){
        List<Message> history = welcomeChannel.getHistory().retrievePast(10).complete();

        // restore welcome message
        if(history.size() == 0)
        {
            welcomeChannel.sendMessageEmbeds(Authorization.getAuthorizationEmbed())
                    .addActionRow(
                            Button.success("StartAuthorization", "인증 절차 시작")
                    ).complete();
        }
        // remove other message
        else if (history.size() > 1)
        {
            for(int msg = 0 ; msg < history.size()-1 ; msg++)
                history.get(msg).delete().queue();
        }
    }

}
