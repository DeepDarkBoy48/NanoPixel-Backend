package robin.discordbot.tutorial;


import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.ChatModel;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import io.github.cdimascio.dotenv.Dotenv;

public class qwen {
    public static Dotenv dotenv = Dotenv.load();

    public static String getQwenToken() {
        return dotenv.get("qwen");
    }
    public static void main(String[] args) {
        ChatModel qwenChatModel = QwenChatModel.builder()
                .apiKey(getQwenToken())
                .modelName("deepseek-r1")
                .build();
        EmbeddingModel embeddingModel  = QwenEmbeddingModel.builder()
                .apiKey(getQwenToken())
                .modelName("text-embedding-v3")
                .build();
        Response<Embedding> embed = embeddingModel.embed("你好，你叫什么名字？");
        String string = embed.content().toString();
        System.out.println(string);
        String generate = qwenChatModel.chat("如何评价马斯克，从马克思的角度，输出思考内容");
        System.out.println(generate);
    }
}
