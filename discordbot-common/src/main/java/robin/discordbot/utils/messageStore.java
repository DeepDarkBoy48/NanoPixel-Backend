package robin.discordbot.utils;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.util.List;

public class messageStore implements ChatMemoryStore {
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        System.out.println("MYSQL");
        return List.of();
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        System.out.println("MYSQL");
    }

    @Override
    public void deleteMessages(Object memoryId) {

    }
}
