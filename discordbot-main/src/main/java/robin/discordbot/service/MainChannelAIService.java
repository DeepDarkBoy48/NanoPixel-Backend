package robin.discordbot.service;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import robin.discordbot.pojo.entity.User;

public interface MainChannelAIService {

    String clearChatMemory(String sid);

    String aiPlayGroundAGENT(MessageReceivedEvent event);

    String aiPlayGroundMCP(MessageReceivedEvent event) throws Exception;

    String aiWebAGENT(String sid, User user, String message);
}
