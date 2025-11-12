package robin.discordbot.service.impl;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import robin.discordbot.pojo.vo.LLMResult;
import robin.discordbot.mapper.AiMapper;
import robin.discordbot.mapper.UserMapper;
import robin.discordbot.service.ShortCutService;
import robin.discordbot.utils.GeminiAudioUtil;
import robin.discordbot.utils.GeminiFactory;

import java.util.function.Function;

@Service
public class ShortcutServiceImpl implements ShortCutService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private AiMapper aiMapper;


    private String systemMessage = "你是善良的同学";
    @Override
    public LLMResult Unhinged(String text) {

        // 获取 Gemini API 密钥 轮询
        String apiKey = GeminiFactory.getGeminiToken();

        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash-preview-05-20")
                .build();
        Unhinged translator = AiServices.builder(Unhinged.class)
                .chatModel(model)
                .systemMessageProvider(new Function<Object, String>() {
                    @Override
                    public String apply(Object o) {
                        return systemMessage;
                    }
                })
                .build();
        String result = "";
        try {
            result = translator.chat(text);
            LLMResult Unhinged = new LLMResult();
            Unhinged.setResult(result);
            return Unhinged;
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void setSystemMessage(String text) {
        systemMessage = text;
    }

    @Override
    public String audioAnalyzer(MultipartFile file, String prompt) {
        String result = GeminiAudioUtil.Audio(file, prompt);
        return result;
    }


    interface Unhinged {
        String chat(@UserMessage String message) throws NoSuchMethodException;
    }
}
