package robin.discordbot.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.googleai.GeminiThinkingConfig;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.service.*;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import robin.discordbot.mapper.AiMapper;
import robin.discordbot.mapper.MainChannelServiceImplTestMapper;
import robin.discordbot.pojo.entity.User;
import robin.discordbot.pojo.entity.aiEntity.aiPrompt;
import robin.discordbot.pojo.http.tavily.Image;
import robin.discordbot.pojo.http.tavily.TavilyResponse;
import robin.discordbot.pojo.http.tavily.TavilyResponseVo;
import robin.discordbot.service.CloudflareR2Service;
import robin.discordbot.service.MainChannelAIService;
import robin.discordbot.service.UserService;
import robin.discordbot.utils.GeminiFactory;
import robin.discordbot.utils.ThreadLocalUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.chroma.ChromaApiVersion.V2;
import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Service
@RequiredArgsConstructor
public class MainWebChannelAIServiceImplAGENT implements MainChannelAIService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final UserService userService;

    Dotenv dotenv = Dotenv.load();

    @Value("${chromaBaseUrl}")
    private String chromaBaseUrl;

    @Autowired
    private AiMapper aiMapper;

    @Autowired
    private CloudflareR2Service cloudflareR2Service;

    @Autowired
    private MainChannelServiceImplTestMapper mainChannelServiceImplTestMapper;

    // 每个用户独立的 ChatMemory 映射，按 memoryId(sid) 隔离
    private final Map<Object, ChatMemory> memories = new ConcurrentHashMap<>();
    private final ChatMemoryProvider chatMemoryProvider = memoryId -> memories.computeIfAbsent(memoryId,
            id -> MessageWindowChatMemory.builder()
                    .id(id)
                    .maxMessages(50)
                    .build());

    interface AiPlayGroundTest {
        Result<String> chat(@MemoryId String memoryId, @UserMessage String message) throws NoSuchMethodException;
    }

    interface translator {
        @SystemMessage("你是一个翻译工具,你翻译任何语言到中文，只输出中文翻译结果，不能输出任何其他内容，需要保留原文的格式")
        String translate(@UserMessage String message) throws NoSuchMethodException;
    }

    // 移除共享 sid，改为在工具实例中携带当前请求的 sid

    @Override
    public String aiWebAGENT(String sid, User user, String message) {
        String contentRaw = message;
        String UserName = user.getNickname();
        String systemMessage = "";
        String modelName = "";
        int thinkingBudget = 0;
        boolean isThink = false;

        // 过五关,斩六将
        // 1. 获取系统提示词
        // 2. 获取模型名称
        // 3. 获取AI实体
        // 4. 如果AI实体不存在,则创建一个AI实体，并设置模型名称和系统提示词
        // 5. 如果AI实体存在,则更新AI实体
        // 6. 如果AI实体的模型名称不存在,则创建一个AI实体，并设置模型名称
        // 7. 如果AI实体的系统提示词不存在,则创建一个AI实体，并设置系统提示词

        aiPrompt aiEntity = aiMapper.getAiByCategory("AGENT");

        modelName = aiEntity.getModelName();
        systemMessage = aiEntity.getPrompt();
        thinkingBudget = aiEntity.getThinkBudget();
        if (thinkingBudget != 0) {
            isThink = true;
        }

        if (contentRaw != null && contentRaw.startsWith("@M ")) {
            modelName = contentRaw.substring(3);
            String globalName = UserName;
            LocalDateTime now = LocalDateTime.now();
            Integer id = aiEntity.getId();
            aiPrompt aiPrompt = new aiPrompt(1, globalName, now, modelName);
            aiMapper.updateAI(aiPrompt, id);
            return "ModelName updated: " + modelName;
        }
        if (contentRaw != null && contentRaw.startsWith("@S ")) {
            systemMessage = contentRaw.substring(3);
            String globalName = UserName;
            LocalDateTime now = LocalDateTime.now();
            aiPrompt aiPrompt = new aiPrompt(systemMessage, globalName, now, "gemini-2.5-flash", "AGENT", 1);
            // 所有agent都关闭
            aiMapper.unenableALL("AGENT");
            aiMapper.addAi(aiPrompt);
            // 仅清空当前会话(sid)的记忆
            ChatMemory cm = memories.get(sid);
            if (cm != null)
                cm.clear();
            return "SystemPrompt updated: " + systemMessage;
        }

        // 获取 Gemini API 密钥 轮询
        String apiKey = GeminiFactory.getGeminiToken();

        // 创建web search agent模型实例
        GeminiThinkingConfig thinkingConfig = GeminiThinkingConfig.builder()
                .includeThoughts(isThink)
                .thinkingBudget(thinkingBudget)
                .build();
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey) // 需要替换为你的 Gemini API 密钥 [2]
                .modelName(modelName) // 模型名称 [2]
                .thinkingConfig(thinkingConfig)
                .returnThinking(true)
                .build();
        String finalSystemMessage = systemMessage;
        AiPlayGroundTest aiPlayGroundTest = AiServices
                .builder(AiPlayGroundTest.class)
                .chatMemoryProvider(chatMemoryProvider)
                .chatModel(model)
                .systemMessageProvider(a -> finalSystemMessage)
                .tools(new toolbox(sid))
                .build();
        System.out.println("contentRaw: " + contentRaw);

        // 创建翻译模型实例
        ChatModel translatorModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey) // 需要替换为你的 Gemini API 密钥 [2]
                .modelName("gemini-flash-lite-latest") // 模型名称 [2]
                .build();
        translator translator = AiServices
                .builder(translator.class)
                .chatModel(translatorModel)
                .build();

        try {
            Result<String> result = aiPlayGroundTest.chat(sid,
                    "{User:" + UserName + ",\n UserMessage:" + contentRaw + "}");

            String answer = result.content();
            // 模型思考
            String thinking = result.finalResponse().aiMessage().thinking();
            System.out.println("thinking: " + thinking);
            if (thinking != null && !thinking.isEmpty()) {
                // 翻译模型思考
                String translateThinking = translator.translate(thinking);
                translateThinking = "<think>" + translateThinking + "<think>";
                System.out.println("translateThinking: " + translateThinking);
                answer = translateThinking + "\n" + answer;
            }

            mainChannelServiceImplTestMapper.updateGeminiApiKeyUsageCount(apiKey, LocalDateTime.now());
            return answer;
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
            return "error: " + e.getMessage();
        } finally {
        }
    }

    class toolbox {
        private final String sid;

        toolbox(String sid) {
            this.sid = sid;
        }

        private static final String TAVILY_SEARCH_ENDPOINT = "https://api.tavily.com/search";
        private final String tavilyApiKey = dotenv.get("tavilyApiKey");

        @Tool("If user ask for web search, use getLocalTime tool to get the current local time, then fill the search parameters and search tool will return a list of search results and relevant image urls")
        public String search(@P(value = "search query", required = true) String q,
                @P(value = "start_date") String start_date,
                @P(value = "max_results") Integer max_results,
                @P(value = "end_date") String end_date,
                @P(value = "Use the full country name in lowercase letters, such as \"china\" or \"united states\".", required = true) String country) {
            JSONObject requestBody = new JSONObject();
            requestBody.set("query", q);
            requestBody.set("include_answer", "basic");
            requestBody.set("max_results", max_results);
            requestBody.set("start_date", start_date);
            requestBody.set("end_date", end_date);
            requestBody.set("country", country);
            requestBody.set("include_images", true);
            requestBody.set("include_image_descriptions", true);
            // requestBody.set("include_raw_content", "markdown");
            HttpResponse request = HttpRequest.post(TAVILY_SEARCH_ENDPOINT)
                    .header("Authorization", "Bearer " + tavilyApiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .execute();

            String body = request.body();
            TavilyResponse response = JSONUtil.toBean(body, TavilyResponse.class);
            // 替换图片url
            List<Image> images = response.getImages();
            // 并行处理图片，每个图片上传到R2并更新URL
            images.parallelStream().forEach(image -> {
                try {
                    // 核心逻辑保持不变
                    byte[] bytes = HttpUtil.downloadBytes(image.getUrl());
                    String imageUrl = cloudflareR2Service.uploadBytes(bytes, image.getTitle() + UUID.randomUUID(),
                            "image/jpeg", "tavily");
                    imageUrl = cloudflareR2Service.handleUrl(imageUrl);
                    image.setUrl(imageUrl);
                } catch (Exception e) {
                    // 确保处理异常，避免一个任务失败导致整个流中断
                    e.printStackTrace();
                }
            });
            TavilyResponseVo tavilyResponseVo = new TavilyResponseVo();
            BeanUtils.copyProperties(response, tavilyResponseVo);
            return tavilyResponseVo.toString();
        }

        @Tool("getLocalTime tool will return the current local time in the format of yyyy-MM-dd")
        public String getLocalTime() {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        @Tool("if user ask for searching in specific file with id, then use this vector search tool to find the most similar content."
                +
                " If a user requests a file with a specific ID, use this vector search tool to find the most similar content."
                +
                " For broad user queries, adjust the minScore and maxResults parameters to obtain optimal results. " +
                "For ambiguous questions like “Summarize the entire text,” reduce minScore and increase maxResults. " +
                "For precise queries such as “What is the largest lake in Kunming,” increase minScore and decrease maxResults.")
        public String vectorSearch(@P(value = "search query", required = true) String q,
                @P(value = "fileId list", required = true) Collection<String> fileIds,
                @P(value = "minScore, default is 0.6, min is 0.1, max is 0.9") Double minScore,
                @P(value = "maxResults, default is 8, max is 20, min is 3") Integer maxResults) {
            EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                    .apiVersion(V2)
                    .baseUrl(chromaBaseUrl)
                    .collectionName("xuzi")
                    .logRequests(true)
                    .logResponses(true)
                    .timeout(Duration.ofMinutes(1))
                    .build();

            Double defaultMinScore = 0.6;
            if (minScore != null) {
                defaultMinScore = minScore;
            }
            Integer defaultMaxResults = 7;
            if (maxResults != null) {
                defaultMaxResults = maxResults;
            }

            ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(embeddingStore)
                    .embeddingModel(embeddingModel)
                    .maxResults(defaultMaxResults)
                    .filter(metadataKey("fileId").isIn(fileIds).and(metadataKey("userId").isEqualTo(sid)))
                    .minScore(defaultMinScore)
                    .build();
            List<Content> contents = contentRetriever.retrieve(new Query(q));
            if (contents == null || contents.isEmpty()) {
                return "decrease minScore or increase maxResults to get more results.";
            }

            System.out.println(contents);
            JSONArray jsonArray = new JSONArray();
            String url = null;
            for (Content content : contents) {
                JSONObject jsonObject = new JSONObject();
                // 从 content 对象中获取数据
                String text = content.textSegment().text();
                Double score = (Double) content.metadata().get(ContentMetadata.SCORE);

                // 注意：根据您第一张截图的结构，url 的正确获取方式应该是这样的
                // 您原始代码中的路径 content.textSegment().metadata()... 可能有误
                url = content.textSegment().metadata().getString("url");

                // 将数据放入 JSON 对象
                jsonObject.set("text", text);
                jsonObject.set("score", score);

                // 将这个 JSON 对象添加到数组中
                jsonArray.add(jsonObject);
            }
            jsonArray.add(new JSONObject().set("url", url));
            return jsonArray.toString();
        }

        @Tool("clear the chat memory for current user")
        String clearChatMemory() {
            ChatMemory cm = memories.get(sid);
            if (cm != null) {
                cm.clear();
                return "Chat memory cleared for sid=" + sid + ".";
            }
            return "No chat memory to clear for sid=" + sid + ".";
        }

    }
    @Override
    public String clearChatMemory(String sid) {
        try {
            ChatMemory cm = memories.get(sid);
            if (cm != null) {
                cm.clear();
                return "Chat memory cleared for sid=" + sid + ".";
            }
            return "No chat memory to clear for sid=" + sid + ".";
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
            return "error: " + e.getMessage();
        }
    }

    @Override
    public String aiPlayGroundAGENT(MessageReceivedEvent event) {
        return "";
    }

    @Override
    public String aiPlayGroundMCP(MessageReceivedEvent event) {
        return null;
    }

}
