package ac.jnu.flowbot.events;

import ac.jnu.flowbot.data.EnvironmentData;
import ac.jnu.flowbot.data.database.Languages;
import ac.jnu.flowbot.functions.Authorization;
import ac.jnu.flowbot.functions.FavoriteLanguages;
import ac.jnu.flowbot.functions.FunctionManager;
import ac.jnu.flowbot.functions.RoleManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 멤버의 입장, 퇴장, 메세지 등 멤버와 관련된 이벤트를 처리하기 위해 존재합니다.
 *
 * @link https://javadoc.io/doc/net.dv8tion/JDA/latest/net/dv8tion/jda/api/hooks/ListenerAdapter.html
 */
public class InteractionAdapter extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        EnvironmentData.logger.newMemberJoin(event.getUser().getIdLong());
        FunctionManager.updateMemberCount(event.getGuild().getMemberCount());
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        EnvironmentData.logger.leaveMember(event.getUser().getIdLong());
        FunctionManager.updateMemberCount(event.getGuild().getMemberCount());
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        switch(event.getComponentId()) {
            case "StartAuthorization": // 인증 시작
                new Thread(new Authorization(event.getUser())).start();
                event.deferReply().queue();
                event.getHook().sendMessage("1대1 메세지로 인증 절차에 대해서 알려드릴게요!").complete().delete().queueAfter(10L, TimeUnit.SECONDS);
                break;
            case "TogglePrivacy":

        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot()) return;

        // DM 데이터 인증절차 전송
        if(event.isFromType(ChannelType.PRIVATE)) {
            Authorization.sendData(event.getAuthor().getIdLong(), event.getMessage().getContentRaw());
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if(Objects.requireNonNull(event.getUser()).isBot()) return;

        // 선호 언어 선택
        if(event.getChannel().asTextChannel().getIdLong() == FavoriteLanguages.channel) {
            Languages lang = Languages.getEnumByLong(event.getEmoji().asCustom().getIdLong());
            if(lang == null) return;
            Role role = RoleManager.getLanguage(lang);
            if(role == null) return;

            event.getGuild().addRoleToMember(event.getUser(), role).queue();
        }
    }
    
    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if(Objects.requireNonNull(event.getUser()).isBot()) return;

        // 선호 언어 선택
        if(event.getChannel().asTextChannel().getIdLong() == FavoriteLanguages.channel) {
            Languages lang = Languages.getEnumByLong(event.getEmoji().asCustom().getIdLong());
            if(lang == null) return;
            Role role = RoleManager.getLanguage(lang);
            if(role == null) return;

            Member m = event.getGuild().getMember(event.getUser());
            List<Role> roles = new ArrayList<>(m.getRoles());
            if(roles.contains(role)) {
                roles.remove(role);
                event.getGuild().modifyMemberRoles(m, roles).queue();
            }
        }
    }
}
