package ac.jnu.flowbot.events;

import ac.jnu.flowbot.data.EnvironmentData;
import ac.jnu.flowbot.data.ProgrammersRecommender;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommandAdapter extends ListenerAdapter {

    private final long whitelistChannel = (EnvironmentData.isLocal) ? 1038660018112839771L : 1038408973986054164L;

    private HashMap<Long, Object[][]> messagePages;

    public CommandAdapter(JDA jda) {
        messagePages = new HashMap<>();
        jda.upsertCommand("hello", "Hello").queue();

        List<SubcommandData> pgmsSubcommands = new ArrayList<>();

        // 프로그래머즈 문제 조회 명령어

        pgmsSubcommands.add(new SubcommandData("rate", "문제를 평가합니다.")
                .addOption(OptionType.INTEGER, "id", "문제 풀이 주소창 가장 우측의 숫자를 입력해주세요.", true)
                .addOption(OptionType.INTEGER, "difficult", "체감 레벨 (1레벨~5레벨)", true)
                .addOption(OptionType.INTEGER, "good", "문제가 도움이 되는지 (1~5)", true)
                .addOption(OptionType.STRING, "comment", "코멘트를 남기고싶다면 적어주세요.", false)
                .addOption(OptionType.BOOLEAN, "efficiency", "(최초 평가용) 효율성 검사 여부", false)
                .addOption(OptionType.INTEGER, "level", "(최초 평가용) 문제의 레벨", false)
                .addOption(OptionType.STRING, "name", "(최초 평가용) 문제 이름", false));

        pgmsSubcommands.add(new SubcommandData("info", "문제 평가 정보를 조회합니다.")
                .addOption(OptionType.STRING, "idorname", "문제의 ID나 이름을 적어주세요.", true));

        pgmsSubcommands.add(new SubcommandData("list", "레벨 별 문제 목록을 확인합니다.")
                .addOption(OptionType.INTEGER, "level", "원하는 레벨을 입력해주세요. (1~5)", true)
                .addOption(OptionType.BOOLEAN, "isefficiency", "효율성 검사가 있는 문제만 필터", false));

        pgmsSubcommands.add(new SubcommandData("random", "해당 레벨에서 문제 하나를 추첨합니다.")
                .addOption(OptionType.INTEGER, "level", "원하는 레벨을 입력해주세요. (1~5)", true)
                .addOption(OptionType.BOOLEAN, "isefficiency", "효율성 검사가 있는 문제만 필터", false));

        jda.upsertCommand("programmers", "프로그래머즈 문제 추천을 받습니다.")
                .addSubcommands(pgmsSubcommands)
                .queue();
        jda.upsertCommand("pgms", "프로그래머즈 문제 추천을 받습니다.")
                .addSubcommands(pgmsSubcommands)
                .queue();

        // Solved.ac 문제 조회 명령어

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
            case "pgms":
            case "programmers":
                switch(event.getSubcommandName().toLowerCase()) {
                    case "rate":
                        long id = event.getOption("id").getAsInt();
                        int diff = event.getOption("difficult").getAsInt();
                        if(diff < 1) diff = 1; else if (diff > 5) diff = 5;
                        int good = event.getOption("good").getAsInt();
                        if(good < 1) good = 1; else if (good > 5) good = 5;

                        EnvironmentData.logger.rateProgrammersProblem(event.getUser().getIdLong(), id);

                        boolean success = false;
                        if(event.getOptions().size() == 3) {
                            success = ProgrammersRecommender.getInstance().rateProblem(event.getUser().getIdLong(), id, diff, good);
                        } else if (event.getOptions().size() >= 4) {
                            OptionMapping comment = event.getOption("comment");
                            if(comment != null && event.getOptions().size() == 4) {
                                success = ProgrammersRecommender.getInstance().rateProblem(event.getUser().getIdLong(), id, diff, good, comment.getAsString());
                            } else {
                                OptionMapping level = event.getOption("level");
                                OptionMapping eff = event.getOption("efficiency");
                                OptionMapping name = event.getOption("name");
                                if(level == null || eff == null || name == null) {
                                    event.reply("레벨, 효율성 여부, 이름 중 제대로 입력되지않은 정보가 있습니다.").queue();
                                    return;
                                }
                                ProgrammersRecommender.getInstance().addProblem(event.getUser().getIdLong(), id, level.getAsInt(), name.getAsString(), diff, good, eff.getAsBoolean());
                                success = true;
                            }
                        }
                        if(success) {
                            event.reply("요청이 성공적으로 수행되었습니다.").queue();
                        } else {
                            event.reply("요청이 실패했습니다.\n입력값을 다시 확인하거나, 똑같은 문제를 2번 평가하려고 하는게 아닌지 생각해보세요.").queue();
                        }
                        break;
                    case "info":
                        String s = event.getOption("idorname").getAsString();
                        if(isNum(s)) event.replyEmbeds(ProgrammersRecommender.getInstance().getProblemInfo(Long.parseLong(s))).queue();
                        else event.replyEmbeds(ProgrammersRecommender.getInstance().getProblemInfo(s)).queue();
                        EnvironmentData.logger.infoProgrammersProblem(event.getUser().getIdLong(), s);
                        break;
                    case "list":
                        int level = event.getOption("level").getAsInt();
                        Object[][] list;
                        if(event.getOptions().size() == 2) {
                            boolean isEff = event.getOption("isefficiency").getAsBoolean();
                            list = ProgrammersRecommender.getInstance().getProblemListByLevel(level, isEff);
                        } else {
                            list = ProgrammersRecommender.getInstance().getProblemListByLevel(level);
                        }
                        EnvironmentData.logger.listProgrammersProblem(event.getUser().getIdLong(), level);
                        event.deferReply().queue();
                        viewProgrammersProbList(level, list, event.getHook());
                        break;
                    case "random":
                        int rlevel = event.getOption("level").getAsInt();
                        MessageEmbed embed;
                        if(event.getOptions().size() == 2) {
                            boolean isEff = event.getOption("isefficiency").getAsBoolean();
                            embed = ProgrammersRecommender.getInstance().getRandomByLevel(rlevel, isEff);
                        } else {
                            embed = ProgrammersRecommender.getInstance().getRandomByLevel(rlevel);
                        }
                        EnvironmentData.logger.randomProgrammersProblem(event.getUser().getIdLong(), rlevel);
                        event.replyEmbeds(embed).queue();
                        break;
                }
                break;
            case "solved":
                String slevel = event.getOption("level").getAsString();
                event.reply("아직 Solved.ac 기능은 추가되지 않았습니다. TnT").queue();
                break;
        }
    }

    private boolean isNum(String s) {
        try{
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void viewProgrammersProbList(int level, Object[][] listInfo, InteractionHook hook) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(new java.awt.Color(0x72EAE0));
        builder.setTitle(String.format("프로그래머즈 레벨 %d - Top 10", level));

        for(int i = 0 ; i < Math.min(listInfo.length, 10) ; i++) {
            builder.addField(
                    String.format("Rank %d", i+1),
                    String.format("%s (%d) %d명 성공 - %s", listInfo[i][1], listInfo[i][0], listInfo[i][2], "https://school.programmers.co.kr/learn/courses/30/lessons/" + listInfo[i][0]), false);
        }

        hook.sendMessageEmbeds(builder.build()).complete();

    }
}
