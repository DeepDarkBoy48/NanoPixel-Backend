package robin.discordbot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import robin.discordbot.service.*;

@Component
public class RegularConfig {

    private static LangChain4jService langChain4jService;
    private static MainChannelAIService mainChannelAIServiceImplAGENT;
    private static MainChannelAIService mainChannelAIServiceImplMCP;
    private static UserService userService;
    private static MainChannelAIService mainWebChannelAIServiceImplAGENT;
    private static ChatMessageService chatMessageService;

    @Autowired
    public void SetRegularConfig(LangChain4jService langChain4jService, UserService userService,
                                 @Qualifier("mainChannelAIServiceImplAGENT") MainChannelAIService mainChannelAIServiceImplAGENT, @Qualifier("mainChannelAIServiceImplMCP") MainChannelAIService mainChannelAIServiceImplMCP, @Qualifier("mainWebChannelAIServiceImplAGENT") MainChannelAIService mainWebChannelAIServiceImplAGENT, ChatMessageService chatMessageService) {
        RegularConfig.langChain4jService = langChain4jService;
        RegularConfig.mainChannelAIServiceImplAGENT = mainChannelAIServiceImplAGENT;
        RegularConfig.mainChannelAIServiceImplMCP = mainChannelAIServiceImplMCP;
        RegularConfig.mainWebChannelAIServiceImplAGENT = mainWebChannelAIServiceImplAGENT;
        RegularConfig.userService = userService;
        RegularConfig.chatMessageService = chatMessageService;
    }

    public static LangChain4jService getLangchain4jservice() {
        return langChain4jService;
    }


    public static UserService getUserService() {
        return userService;
    }

    public static MainChannelAIService getMainChannelAIServiceImplAGENT() {
        return mainChannelAIServiceImplAGENT;
    }

    public static MainChannelAIService getMainChannelAIServiceImplMCP() {
        return mainChannelAIServiceImplMCP;
    }

    public static MainChannelAIService getMainWebChannelAIServiceImplAGENT() {
        return mainWebChannelAIServiceImplAGENT;
    }

    public static ChatMessageService getChatMessageService() {
        return chatMessageService;
    }
}
