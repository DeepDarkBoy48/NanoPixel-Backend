package robin.discordbot.service;

import robin.discordbot.pojo.entity.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    void saveMessage(ChatMessage message);
    List<ChatMessage> getRecentMessages(String roomId, int limit);
}
