package robin.discordbot.listener;

import org.springframework.stereotype.Service;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import robin.discordbot.config.RegularConfig;
import robin.discordbot.service.MainChannelAIService;
import robin.discordbot.utils.discordWordsLimit;

@Service
public class ChannelListener extends ListenerAdapter {


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        String result = null;
        if (event.getChannel().getName().equals("ai游乐园agent")) {
            MainChannelAIService mainChannelAIService = RegularConfig.getMainChannelAIServiceImplAGENT();
             result = mainChannelAIService.aiPlayGroundAGENT(event);
        } else if (event.getChannel().getName().equals("ai游乐园mcp")) {
            MainChannelAIService mainChannelAIService = RegularConfig.getMainChannelAIServiceImplMCP();
            try {
                result = mainChannelAIService.aiPlayGroundMCP(event);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        discordWordsLimit.splitParagraph(result, event);
    }
}
