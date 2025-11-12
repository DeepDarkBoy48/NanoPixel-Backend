package robin.discordbot.service.impl;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import robin.discordbot.pojo.vo.LLMResult;
import robin.discordbot.mapper.AiMapper;
import robin.discordbot.mapper.UserMapper;
import robin.discordbot.service.ApiService;
import robin.discordbot.utils.GeminiFactory;

import java.util.function.Function;

@Service
public class ApiServiceImpl implements ApiService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private AiMapper aiMapper;

//    @Override
//    public User getUserById(Integer id) {
//        User user = userMapper.getUserById(id);
//        return user;
//    }

    private String systemprompt = "你是一个翻译专家，幽默搞笑地翻译接受到的原文，只输出译文，不输出任何其他信息";

    @Override
    public LLMResult getTranslate(String text) {

        System.out.println("系统提示词"+systemprompt);
        // 获取 Gemini API 密钥 轮询
        String apiKey = GeminiFactory.getGeminiToken();

        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash-preview-04-17")
                .build();
        Translator translator = AiServices.builder(Translator.class)
                .chatModel(model)
                .systemMessageProvider(new Function<Object, String>() {
                    @Override
                    public String apply(Object o) {
                        return systemprompt;
                    }
                })
                .build();
        String result = "";
        try {
            result = translator.chat(text);
            LLMResult translate = new LLMResult();
            translate.setResult(result);
            return translate;
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void setSystemprompt(String systemprompt) {
        this.systemprompt = systemprompt;
        System.out.println("systemprompt: " + this.systemprompt);
    }


    interface Translator {
        String chat(@UserMessage String message) throws NoSuchMethodException;
    }
}
