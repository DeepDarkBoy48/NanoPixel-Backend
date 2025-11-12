package robin.discordbot.utils.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import io.github.cdimascio.dotenv.Dotenv;
import robin.discordbot.utils.GeminiFactory;

public class geminiModel {
    private static final Dotenv dotenv = Dotenv.load();

    // 获取 Gemini API 密钥 轮询
//    static String apiKey = GeminiFactory.getGeminiToken();
    public static ChatModel getGeminiModel(String modelName) {
        return GoogleAiGeminiChatModel.builder()
                .modelName(modelName)
                .apiKey(dotenv.get("geminiTier1Token"))
                .build();
    }
}
