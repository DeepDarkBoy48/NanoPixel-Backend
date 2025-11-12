//package robin.discordbot.tutorial;
//
//import dev.langchain4j.model.image.ImageModel;
//import io.github.cdimascio.dotenv.Dotenv;
//import robin.discordbot.record.llmModel;
//
//public class imageModle {
//    public static Dotenv dotenv = Dotenv.load();
//
//    public static String getGeminiToken() {
//        return dotenv.get("GEMINI2");
//    }
//
//    public static void main(String[] args) {
//
//        ImageModel model = OpenAiChatModel
//                .apiKey(getGeminiToken())
//                .modelName(llmModel.GEMINI_FLASH.getModle())
//                .build();
//    }
//}
