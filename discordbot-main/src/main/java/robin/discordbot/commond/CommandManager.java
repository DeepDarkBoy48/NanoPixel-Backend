package robin.discordbot.commond;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import robin.discordbot.config.RegularConfig;
import robin.discordbot.pojo.entity.aiEntity.AiMessageFormat;
import robin.discordbot.service.LangChain4jService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Registers and manages slash commands.
 *
 * @author TechnoVision
 */

public class CommandManager extends ListenerAdapter {

    /**
     * Listens for slash commands and responds accordingly
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        String id = event.getUser().getId();
        if (command.equals("welcome")) {
            // Run the 'ping' command
            String userGlobalName = event.getUser().getGlobalName();
            //setEphemeral设置个人可见
            event.reply("欢迎来到discord **" + userGlobalName + "**!").setEphemeral(false).queue();
        } else if (command.equals("roles")) {
            // run the 'roles' command
            event.deferReply().setEphemeral(false).queue();
            String response = "";
            for (Role role : event.getGuild().getRoles()) {
                response += role.getAsMention() + "\n";
            }
            event.getHook().sendMessage(response).queue();
        } else if (command.equals("ai文字聊天")) {
            event.deferReply().setEphemeral(false).queue();
            OptionMapping option = event.getOption("输入提示词");
            String message = option.getAsString();
            //ai
            AiMessageFormat aiMessageFormat = new AiMessageFormat();
            aiMessageFormat.setMessage(message);
            LangChain4jService langchain4jservice = RegularConfig.getLangchain4jservice();
//            String deepDarkResult = langchain4jservice.deepdarkaiText(id, aiMessageFormat);
            String deepDarkResult = null;

            // 通过 sendFiles() 上传文件
            event.getHook().sendMessage(deepDarkResult).queue();

        } else if (event.getName().equals("ai生成图片") && event.getMember().getId() == "1121767044715651122") {
            System.out.println(event.getMember().getId());
            event.deferReply().setEphemeral(false).queue();
            OptionMapping option = event.getOption("输入提示词");
            String message = option.getAsString();

            //ai
            AiMessageFormat aiMessageFormat = new AiMessageFormat();
            aiMessageFormat.setMessage(message);
            LangChain4jService langchain4jservice = RegularConfig.getLangchain4jservice();
//            String imageUrl = langchain4jservice.deepdarkaiImage(id, aiMessageFormat);
            String imageUrl = null;
            event.getHook().sendMessage(imageUrl).queue();
        }
    }
    // 用户输入消息时可以提示
//    @Override
//    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
//
//        if (event.getName().equals("chatgpt-4o-mini") && event.getFocusedOption().getName().equals("输入问题与ai对话")&& event.getFocusedOption().getValue()!=null) {
//            String message = event.getFocusedOption().getValue();
//            String id = event.getUser().getId();
//
//            //ai
//            AiMessageFormat aiMessageFormat = new AiMessageFormat();
//            aiMessageFormat.setMessage(message);
//            LangChain4jService langchain4jservice = RegularConfig.getLangchain4jservice();
//            byte[] deepDarkResult = langchain4jservice.deepdarkai(id,aiMessageFormat);
//
//            List<Command.Choice> choices = new ArrayList<>();
//            Command.Choice choice = new Command.Choice(deepDarkResult, deepDarkResult);
//            choices.add(choice);
//            event.replyChoices(choices).queue();
//        }
//    }

    /**
     * Registers slash commands as GUILD commands (max 100).
     * These commands will update instantly and are great for testing.
     */

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("welcome", "Get welcomed by the bot"));
        commandData.add(Commands.slash("roles", "Display all roles on the server"));
        commandData.add(Commands.slash("ai文字聊天", "chatgpt-4o-mini-text").addOption(OptionType.STRING, "输入提示词", "chatgpt-4o-mini", true, false));
        commandData.add(Commands.slash("ai文字聊天html转图片", "chatgpt-4o-mini-figure").addOption(OptionType.STRING, "输入提示词", "chatgpt-4o-mini", true, false));
        commandData.add(Commands.slash("ai生成图片", "chatgpt-4o-mini-image").addOption(OptionType.STRING, "输入提示词", "chatgpt-4o-mini", true, false));

        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    /**
     * Registers slash commands as GLOBAL commands (unlimited).
     * These commands may take up to an hour to update.
     */
//    @Override
//    public void onReady(@NotNull ReadyEvent event) {
//        List<CommandData> commandData = new ArrayList<>();
//        commandData.add(Commands.slash("welcome", "Get welcomed by the bot"));
//        commandData.add(Commands.slash("roles", "Display all roles on the server"));
//        event.getJDA().updateCommands().addCommands(commandData).queue();
//    }
}

