package ac.jnu.flowbot.functions;

import ac.jnu.flowbot.data.EnvironmentData;
import ac.jnu.flowbot.data.database.Languages;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;

public class FunctionManager {

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
    }

    public static void checkFavoriteLanguages(Guild guild, TextChannel langChannel) {
        List<Message> history = langChannel.getHistory().retrievePast(10).complete();

        if(history.size() == 0)
        {
            langChannel.sendMessageEmbeds(FavoriteLanguages.getEmbed()).queue(msg-> {
                for(Languages lang : Languages.values()) {
                    Emoji emoji = guild.getEmojiById(lang.getId());
                    if(emoji != null)
                        msg.addReaction(emoji).queue();
                }
            });
        }
    }

    public static void checkPrivacySettings(TextChannel privacyChannel){
        List<Message> history = privacyChannel.getHistory().retrievePast(10).complete();

        if(history.size() == 0)
        {
            privacyChannel.sendMessageEmbeds(PrivacySettings.getEmbed())
                    .addActionRow(
                            Button.primary("TogglePrivacy", "정보 표시/숨기기")
                    ).complete();
        }
    }

    public static void updateMemberCount(int count) {
        VoiceChannel channel = EnvironmentData.getInstance().getMainGuild().getVoiceChannelById(1038370762043699220L);
        channel.getManager().setName(String.format("멤버 수 : %d", count)).queue();
    }
}
