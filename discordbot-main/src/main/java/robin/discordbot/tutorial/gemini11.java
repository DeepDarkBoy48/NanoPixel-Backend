package robin.discordbot.tutorial;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import org.springframework.stereotype.Service;

@Service
public class gemini11 {
    public static void main(String[] args) throws NoSuchMethodException {
        interface AiPlayGroundTest {
            String chat(@UserMessage String message) throws NoSuchMethodException;
        }

        ChatModel build = GoogleAiGeminiChatModel.builder()
                .apiKey("YOUR_GEMINI_API_KEY") // 需要替换为你的 Gemini API 密钥 [2]
                .modelName("gemini-2.0-flash") //  模型名称 [2]
                .build();

        AiPlayGroundTest aiPlayGroundTest = AiServices.builder(AiPlayGroundTest.class)
                .chatModel(build)
                .build();
        System.out.println(aiPlayGroundTest.chat("你写一个jojo的梗"));


    }
}