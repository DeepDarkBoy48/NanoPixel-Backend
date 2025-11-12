//package robin.discordbot.tutorial;
//
//import dev.langchain4j.memory.ChatMemory;
//import dev.langchain4j.memory.chat.MessageWindowChatMemory;
//import dev.langchain4j.model.chat.ChatModel;
//import dev.langchain4j.model.openai.OpenAiChatModel;
//import dev.langchain4j.service.SystemMessage;
//import dev.langchain4j.service.UserMessage;
//import io.github.cdimascio.dotenv.Dotenv;
//
//public class reasoning {
//
//    public static Dotenv dotenv = Dotenv.load();
//
//    private static final ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(100).build();
//
//    public static String getDeepSeekToken() {
//        return dotenv.get("deepseekToken");
//    }
//
//    interface AiPlayGroundTest {
//        @SystemMessage("你是个坏人")
//        String chat(@UserMessage String message) throws NoSuchMethodException;
//    }
//    public static void main(String[] args) throws NoSuchMethodException {
//        String modelname = "deepseek-reasoner";
//        String url = "https://api.deepseek.com";
//
//        ChatModel model = OpenAiChatModel.builder()
//                .apiKey(getDeepSeekToken())
//                .baseUrl(url)
//                .modelName(modelname)
//                .build();
//
//        String generate = model.chat("今天昆明天气怎么样");
//        System.out.println(generate);
////        AiPlayGroundTest aiPlayGroundTest = AiServices.builder(AiPlayGroundTest.class)
////                .chatLanguageModel(model)
////                .build();
////        String chat = aiPlayGroundTest.chat("今天昆明天气怎么样");
////        System.out.println(chat);
//    }
//}
