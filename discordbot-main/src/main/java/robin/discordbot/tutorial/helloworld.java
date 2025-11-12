package robin.discordbot.tutorial;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import io.github.cdimascio.dotenv.Dotenv;
import robin.discordbot.record.llmModel;

public class helloworld {

    public static Dotenv dotenv = Dotenv.load();

    public static String getGeminiToken() {
        return dotenv.get("GEMINI2");
    }

    public static void main(String[] args) {
        String model = llmModel.GEMINI_FLASH_LITE.getModle();
        ChatModel modle = GoogleAiGeminiChatModel.builder()
                .apiKey(getGeminiToken())
                .modelName(model)
                .build();

        modle.chat("你写一个故事");
    }
}
