package robin.discordbot.utils;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;

public class discordWordsLimit {
    public static void splitParagraphWithActionRow(String report, ButtonInteractionEvent event, ActionRow actionRow) {
        if (report.length() > 2000) {
            // 计算应该分成几个message
            int msgNum = 0;
            if (report.length() % 2000 == 0) {
                msgNum = report.length() / 2000;
            } else {
                msgNum = report.length() / 2000 + 1;
            }
            // 将每个message分别发出
            int start = 0;
            int end = 2000;
            for (int i = 1; i <= msgNum; i++) {
                String subreport = report.substring(start, end);
                if (i == msgNum) {
                    event.getChannel().asThreadChannel().sendMessage(subreport).setComponents(actionRow).queue();
                } else {
                    event.getChannel().asThreadChannel().sendMessage(subreport).queue();
                }
                start = end;
                end = Math.min(start + 2000, report.length());
            }
        } else {
            // 发送ai报告和按钮
            event.getChannel().asThreadChannel().sendMessage(report).setComponents(actionRow).queue();
        }
    }

    public static void splitParagraphWithActionRow2(String report, MessageReceivedEvent event, ActionRow actionRow) {
        if (report.length() > 2000) {
            // 计算应该分成几个message
            int msgNum = 0;
            if (report.length() % 2000 == 0) {
                msgNum = report.length() / 2000;
            } else {
                msgNum = report.length() / 2000 + 1;
            }
            // 将每个message分别发出
            int start = 0;
            int end = 2000;
            for (int i = 1; i <= msgNum; i++) {
                String subreport = report.substring(start, end);
                if (i == msgNum) {
                    event.getChannel().asThreadChannel().sendMessage(subreport).setComponents(actionRow).queue();
                } else {
                    event.getChannel().asThreadChannel().sendMessage(subreport).queue();
                }
                start = end;
                end = Math.min(start + 2000, report.length());
            }
        } else {
            // 发送ai报告和按钮
            event.getChannel().asThreadChannel().sendMessage(report).setComponents(actionRow).queue();
        }
    }

    // 无 ActionRow 的分割方法
    public static void splitParagraph(String report, MessageReceivedEvent event) {
        if (report.length() > 2000) {
            // 计算应该分成几个message
            int msgNum = (report.length() + 1999) / 2000; // Ceiling division
            for (int i = 0; i < msgNum; i++) {
                int start = i * 2000;
                int end = Math.min(start + 2000, report.length());
                String subreport = report.substring(start, end);
                event.getChannel().sendMessage(subreport).queue();
            }
        } else {
            // 发送ai报告
            event.getChannel().sendMessage(report).queue();
        }
    }
}
