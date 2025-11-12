package robin.discordbot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import robin.discordbot.mapper.ChatMessageMapper;
import robin.discordbot.pojo.entity.ChatMessage;
import robin.discordbot.service.ChatMessageService;

import java.util.List;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {
    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    public void saveMessage(ChatMessage message) {
        chatMessageMapper.insert(message);
    }

    @Override
    public List<ChatMessage> getRecentMessages(String roomId, int limit) {
        return chatMessageMapper.findRecentMessages(roomId, limit);
    }
}
