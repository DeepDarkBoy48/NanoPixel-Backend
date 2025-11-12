package robin.discordbot.controller;

import dev.langchain4j.agent.tool.Tool;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import robin.discordbot.config.RegularConfig;
import robin.discordbot.pojo.entity.Result;
import robin.discordbot.service.MainChannelAIService;
import robin.discordbot.websocket.WebSocketServer;

@RestController
@RequestMapping("/chatroom")
@RequiredArgsConstructor
public class ChatRoomController {
    @PostMapping("/clear")
    public Result<String> clearChatMemory(@RequestParam("sid") String sid) {
        String msg = RegularConfig.getMainWebChannelAIServiceImplAGENT().clearChatMemory(sid);
        return Result.success(msg);
    }
}
