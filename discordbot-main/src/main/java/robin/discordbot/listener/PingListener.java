package robin.discordbot.listener;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class PingListener extends ListenerAdapter {

    private static final String BOT_IMAGE_URL = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTZ8WKYDQJA5ZLhUEL4r-hJqWSV0UjZtPiHOJQaTM5ssaHXjVr5dLTpnWTFZa8hB53StEc&usqp=CAU";
    private static final String BOT_RESPONSE = "我是DeepDarkBot，你的地牢管家";

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        
        if (message.contains("ping！")) {
            // Send bot image
            event.getChannel().sendMessage(BOT_IMAGE_URL).queue();
            // Send bot introduction
            event.getChannel().sendMessage(BOT_RESPONSE).queue();
        }
    }
} 