package robin.discordbot.tutorial;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import io.github.cdimascio.dotenv.Dotenv;
import robin.discordbot.record.llmModel;

import java.util.concurrent.TimeUnit;

public class Streaming {
    public static Dotenv dotenv = Dotenv.load();

    public static String getGeminiToken() {
        return dotenv.get("GEMINI2");
    }
    public static void main(String[] args) {

        GoogleAiGeminiStreamingChatModel model = GoogleAiGeminiStreamingChatModel.builder()
                .apiKey(getGeminiToken())
                .modelName(llmModel.GEMINI_FLASH.getModle())
                .build();

        model.chat("你写一个故事", new StreamingChatResponseHandler() {


            @Override
            public void onPartialResponse(String token) {
                System.out.println(token);
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse chatResponse) {
                System.out.println(chatResponse);
            }


            @Override
            public void onError(Throwable throwable) {
                System.out.println(throwable);
            }
        });




    }
}
