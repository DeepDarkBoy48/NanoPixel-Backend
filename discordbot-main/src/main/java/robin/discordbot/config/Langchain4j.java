package robin.discordbot.config;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.github.cdimascio.dotenv.Dotenv;
import robin.discordbot.pojo.entity.aiEntity.aiSearchOutputEntity;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static cn.hutool.core.util.IdUtil.randomUUID;
import static dev.langchain4j.store.embedding.chroma.ChromaApiVersion.V2;

@Configuration
public class Langchain4j {
    private static final Dotenv dotenv = Dotenv.load();

    public static String getGoogleToken() {
        return dotenv.get("geminiTier1Token");
    }
    /**
     * chat memory provider
     *
     * @return
     */
    @Bean
    public static ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(30)
                .build();

    }

    @Bean
    public ChatModel chatLanguageModelGeminiFlash() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(getGoogleToken())
                .modelName("gemini-2.5-flash")
                .build();
    }

    /**
     * AiService - - aiSearchTavily
     *
     * @return
     */

    public interface aiSearchTavily {
        @SystemMessage("请根据 resultInfo 中提供的信息同时结合自己本身的知识，resultInfo可能是英文内容，但也要重新整理并编辑成一篇中文文章。文章需满足以下要求：\n" +
                "1. **内容完整性**：尽可能涵盖 resultInfo 中的所有信息同时结合自己本身的知识。\n" +
                "2. **链接整合**：按照示例处理resultInfo 中所有的网页链接，示例：请访问 [OpenAI](https://www.openai.com) 获取更多信息。\n" +
                "3. **格式优化**：使用markdown语法包裹文章，包括大标题，粗体，斜体，代码块，有序列表，无序列表。但是要尽量让文章紧凑，不要空行\n" +
                "4. **署名**：文章末尾添加斜体加粗文字：_**AI服务由 @Crispy Frog 提供**_ \n" +
                "\n" +
                "此外，根据 resultInfo 提供的信息生成 3 个相关追问问题,字数控制在20个字符以内，并将这些问题填入 aiSearchOutputEntity 的 List<String> buttonInfo 中。\n")
        aiSearchOutputEntity chat(@MemoryId Object userId, @UserMessage String resultInfo);
    }

    @Bean
    public aiSearchTavily aiSearchTavily() {
        return AiServices.builder(aiSearchTavily.class)
                .chatModel(chatLanguageModelGeminiFlash())
                .chatMemoryProvider(chatMemoryProvider())
                .build();
    }

    @Bean
    public EmbeddingModel qwenEmbeddingModel() {
        return QwenEmbeddingModel.builder()
                .dimension(1024)
                .apiKey(dotenv.get("qwen"))
                .modelName("text-embedding-v4")
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> chromaEmbeddingStore() {
        return ChromaEmbeddingStore.builder()
                .apiVersion(V2)
                .collectionName("xuzi")
                .baseUrl(dotenv.get("chromaBaseUrl"))
                .logRequests(true)
                .logResponses(true)
                .timeout(Duration.ofMinutes(5))
                .build();
    }
}
