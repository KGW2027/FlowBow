package ac.jnu.flowbot.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class CommandAdapter extends ListenerAdapter {

    private final long whitelistChannel = 1038408973986054164L;

    public CommandAdapter(JDA jda){
        jda.upsertCommand("hello", "Haha").queue();
        jda.upsertCommand("programmers", "프로그래머즈 레벨별 문제 추천을 받습니다.")
                .addOption(OptionType.INTEGER, "level", "추천을 받고싶은 레벨을 입력해주세요. (1~5)")
                .queue();
        jda.upsertCommand("solved", "Solved.ac 레벨별 문제 추천을 받습니다.")
                .addOption(OptionType.STRING, "level", "추천을 받고싶은 레벨을 입력해주세요. (브론즈, 실버, 골드, 플래티넘, 다이아몬드, 루비)")
                .queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(event.getChannel().getIdLong() != whitelistChannel) return;

        switch (event.getName().toLowerCase()) {
            case "hello":
                event.reply("yay").queue();
                break;
            case "programmers":
                int plevel = event.getOption("level").getAsInt();
                event.reply(String.format("프로그래머즈 레벨 %d 문제를 추천합니다.", plevel)).queue();
                break;
            case "solved":
                String slevel = event.getOption("level").getAsString();
                event.reply("아직 Solved.ac 기능은 추가되지 않았습니다. TnT").queue();
                break;
        }
    }
}
