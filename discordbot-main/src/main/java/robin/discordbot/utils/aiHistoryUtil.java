package robin.discordbot.utils;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import robin.discordbot.pojo.entity.aiEntity.AiMessageFormat;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class aiHistoryUtil {
    static private Map<String, LinkedList<AiMessageFormat>> channelMessages = new HashMap<>();
    // 每个频道保留的消息数量
    private static final int MAX_MESSAGES_PER_CHANNEL = 30;
    public static void addMessageToHistory(String channelId, MessageReceivedEvent event) {
        //匿名内部类,可替换成lambda表达式,String是输入,LinkedList<AiMessageFormat>是输出
        LinkedList<AiMessageFormat> messages = channelMessages.computeIfAbsent(channelId, new Function<String, LinkedList<AiMessageFormat>>() {
            @Override
            public LinkedList<AiMessageFormat> apply(String s) {
                return new LinkedList<>();
            }
        });
        AiMessageFormat newMessage = new AiMessageFormat();
        newMessage.setMessage(event.getMessage().getContentRaw());
        newMessage.setUsername(event.getAuthor().getGlobalName());
        messages.add(newMessage);
        if (messages.size() > MAX_MESSAGES_PER_CHANNEL) {
            // 如果消息数量超过最大值,则删除最早的消息
            messages.removeFirst();
        }
    }

    public static void addAiMessageToHistory(String channelId, String aiReply) {
        LinkedList<AiMessageFormat> messages = channelMessages.computeIfAbsent(channelId, new Function<String, LinkedList<AiMessageFormat>>() {
            @Override
            public LinkedList<AiMessageFormat> apply(String s) {
                return new LinkedList<>();
            }
        });
        // 将 AI 的回复也添加到历史记录
        AiMessageFormat aiMessage = new AiMessageFormat();
        aiMessage.setMessage(aiReply);
        aiMessage.setUsername("deepdarkbot"); // 使用机器人的名字
        messages.add(aiMessage);
        if (messages.size() > MAX_MESSAGES_PER_CHANNEL) {
            // 如果消息数量超过最大值,则删除最早的消息
            messages.removeFirst();
        }
    }

    public static void addMessageToAi(List<Map<String, String>> messages, String channelId) {
        LinkedList<AiMessageFormat> historyMessages = channelMessages.getOrDefault(channelId, new LinkedList<>());
        for (AiMessageFormat historyMessage : historyMessages) {

            Map<String, String> userHistoryMessage = new HashMap<>();
            if (historyMessage.getUsername().equals("deepdarkbot")){
                String historyMessageReformat = historyMessage.getMessage();
                userHistoryMessage.put("content", historyMessageReformat);
                userHistoryMessage.put("role", "assistant");
            }else{
                String historyMessageReformat = historyMessage.getUsername() + ":" + historyMessage.getMessage();
                userHistoryMessage.put("content", historyMessageReformat);
                userHistoryMessage.put("role", "user");
            }
            messages.add(userHistoryMessage);
        }
    }
}
