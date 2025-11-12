package robin.discordbot.listener;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class ReactionListener extends ListenerAdapter {
    private static final String GENERAL_CHANNEL = "常规";
    private static final Emoji EMOJI_ONE = Emoji.fromUnicode("😅");
    private static final Emoji EMOJI_TWO = Emoji.fromUnicode("😢");

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // 检查是否是在常规频道
        if (!event.getChannel().getName().equals(GENERAL_CHANNEL))
            return;

        // 检查是否是特定消息（这里假设消息内容为"选择表情"）
        if (event.getMessage().getContentRaw().equals("选择表情")) {
            // 发送消息并添加表情
            event.getChannel().sendMessage("请选择一个表情：\n" +
                            EMOJI_ONE + " - 你好\n" +
                            EMOJI_TWO + " - 你不好")
                    .queue(new Consumer<Message>() {
                        @Override
                        public void accept(Message message) {
                            message.addReaction(EMOJI_ONE).queue();
                            message.addReaction(EMOJI_TWO).queue();
                        }
                    });
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        // 忽略机器人的反应
        if (event.getUser().isBot())
            return;

        // 检查是否在常规频道
        if (!event.getChannel().getName().equals(GENERAL_CHANNEL))
            return;

        String emoji = event.getReaction().getEmoji().getAsReactionCode();
        // 根据不同的表情发送不同的消息
        if (emoji.equals(EMOJI_ONE.getAsReactionCode())) {
            event.getChannel().sendMessage("你好！").queue();
        } else if (emoji.equals(EMOJI_TWO.getAsReactionCode())) {
            event.getChannel().sendMessage("你不好").queue();
        }
        // 当前频道
        MessageChannelUnion channel = event.getChannel();
        // 获取用户标签
        String userTag = event.getUser().getGlobalName();
        // 获取消息跳转链接
        String jumpLink = event.getJumpUrl();
        // 获取频道提及
        String channelMention = event.getChannel().getAsMention();
        if (channel != null) {
            channel.sendMessage("用户 " + userTag + " 反应了 " + emoji + " in " + channelMention + ".\nJump to message: " + jumpLink);
        }
    }
}
