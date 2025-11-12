package robin.discordbot.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.langchain4j.agent.tool.*;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.tool.*;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import robin.discordbot.mapper.AiMapper;
import robin.discordbot.mapper.MainChannelServiceImplTestMapper;
import robin.discordbot.pojo.entity.User;
import robin.discordbot.pojo.entity.aiEntity.aiPrompt;
import robin.discordbot.pojo.entity.aiEntity.gemini_api_key_entity;
import robin.discordbot.service.ApiService;
import robin.discordbot.service.MainChannelAIService;
import robin.discordbot.utils.GeminiFactory;

@Service
public class MainChannelAIServiceImplMCP implements MainChannelAIService {

    @Autowired
    private AiMapper aiMapper;

    @Autowired
    private MainChannelServiceImplTestMapper mainChannelServiceImplTestMapper;

    private Dotenv dotenv = Dotenv.load();

    @Resource
    private ApiService apiService;

    //  创建一个全局共享的 ChatMemory 实例
    private final ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(100).build();
    private final ExecutorService executorService = Executors.newCachedThreadPool(); // 用于异步执行

    interface AiPlayGroundTest {
        String chat(@UserMessage String message) throws NoSuchMethodException;
    }

    @Override
    public String aiPlayGroundMCP(MessageReceivedEvent event) throws Exception {

        String contentRaw = event.getMessage().getContentRaw();
        String UserName = event.getAuthor().getGlobalName();
        String systemMessage = "";
        String modelName = "";

        //过五关,斩六将
        // 1. 获取系统提示词
        // 2. 获取模型名称
        // 3. 获取AI实体
        // 4. 如果AI实体不存在,则创建一个AI实体，并设置模型名称和系统提示词
        // 5. 如果AI实体存在,则更新AI实体
        // 6. 如果AI实体的模型名称不存在,则创建一个AI实体，并设置模型名称
        // 7. 如果AI实体的系统提示词不存在,则创建一个AI实体，并设置系统提示词

        aiPrompt aiEntity = aiMapper.getAiByCategory("MCP");

//        if (aiEntity.getModelName().isEmpty() || aiEntity.getPrompt().isEmpty()) {
//            aiPrompt aiPrompt = new aiPrompt();
//            aiPrompt.setModelName("gemini-2.0-flash");
//            aiPrompt.setPrompt("Hello, I am Gemini. How can I help you?");
//            aiPrompt.setCreationTime(LocalDateTime.now());
//            aiMapper.updateAI(aiPrompt, 1);
//            aiEntity = aiMapper.getAiByCategory(1);
//        }


        // FIXME: Ensure aiEntity has getModelName() and getPrompt() methods
        // For now, assuming they exist or will be added.
        // If aiEntity can be null, add null checks.
        if (aiEntity != null) {
            modelName = aiEntity.getModelName(); // Placeholder if getter doesn't exist
            systemMessage = aiEntity.getPrompt(); // Placeholder if getter doesn't exist
        } else {
            // Handle case where aiEntity might be null, e.g., set default values or throw an error
            modelName = "gemini-2.5-pro-exp-03-25"; // Default model
            systemMessage = "You are a helpful assistant."; // Default prompt
            // Optionally, log a warning or error here
            System.err.println("Warning: aiEntity for MCP is null. Using default model and prompt.");
        }


        if (contentRaw != null && contentRaw.startsWith("@M ")) {
            modelName = contentRaw.substring(3);
            String globalName = event.getAuthor().getGlobalName();
            LocalDateTime now = LocalDateTime.now();
            // FIXME: Ensure aiPrompt constructor and aiEntity.getId() are correct
            aiPrompt aiPromptToUpdate = new aiPrompt(1, globalName, now, modelName);
            Integer id = (aiEntity != null) ? aiEntity.getId() : null; // Placeholder if getter doesn't exist
            if (id != null) {
                aiMapper.updateAI(aiPromptToUpdate, id);
            } else {
                // Handle case where id is null (e.g. aiEntity was null or had no id)
                System.err.println("Error: Cannot update model name, AI entity ID is null.");
                return "Error: AI entity not found for updating model name.";
            }
            return "ModelName updated: " + modelName;
        }
        if (contentRaw != null && contentRaw.startsWith("@S ")) {
            systemMessage = contentRaw.substring(3);
            String globalName = event.getAuthor().getGlobalName();
            LocalDateTime now = LocalDateTime.now();
            aiPrompt aiPromptToSet = new aiPrompt(systemMessage, globalName, now, "gemini-2.5-pro-exp-03-25", "MCP", 1);
            // 所有agent都关闭
            aiMapper.unenableALL("MCP");
            aiMapper.addAi(aiPromptToSet);
            chatMemory.clear();
            return "SystemPrompt updated: " + systemMessage;
        }

        // 获取 Gemini API 密钥 轮询
        String apiKey = GeminiFactory.getGeminiToken();

        // 创建 Gemini 模型实例
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey) // 需要替换为你的 Gemini API 密钥 [2]
                .modelName(modelName) //  模型名称 [2]
                .build();

        /**
         * tavily-mcp@0.1.4
         */
        McpTransport transport1 = new StdioMcpTransport.Builder()
                .command(List.of(
                        "npx",
                        "-y",
                        "tavily-mcp@0.1.4"
                ))
                .environment(Map.of("TAVILY_API_KEY", envOrDefault("", "TAVILY_API_KEY", "tavilyApiKey")))
                .logEvents(true)
                .build();
        McpClient mcpClient1 = new DefaultMcpClient.Builder()
                .transport(transport1)
                .toolExecutionTimeout(Duration.ofSeconds(160)) // Set a timeout for tool execution
                .build();

        /**
         * Sequential Thinking MCP Server
         */
        McpTransport SequentialThinkingTransport = new StdioMcpTransport.Builder()
                .command(List.of( // Assuming 'npx' is in the system PATH
                        "npx",
                        "-y",
                        "@modelcontextprotocol/server-sequential-thinking"
                ))
                // No environment variables specified in the JSON
                .logEvents(true)
                .build();
        McpClient mcpClient2 = new DefaultMcpClient.Builder()
                .transport(SequentialThinkingTransport)
                .toolExecutionTimeout(Duration.ofSeconds(160)) // Set a timeout for tool execution
                .build();


//        // Desktop Commander 服务器: 用于执行桌面相关的命令或操作
//        McpTransport transport3 = new StdioMcpTransport.Builder()
//                .command(List.of("npx",
//                        "-y",
//                        "@wonderwhy-er/desktop-commander"
//                ))
//                // .environment(Map.of(...)) // 此配置中没有环境变量
//                .logEvents(true) // 根据需要添加日志记录
//                .build();
//        McpClient mcpClient3 = new DefaultMcpClient.Builder()
//                .transport(transport3)
//                .build();

        // Amap SSE 服务器: 通过 Server-Sent Events (SSE) 连接到高德地图相关服务
//        McpTransport transport4 = new HttpMcpTransport.Builder()
//                .sseUrl("https://mcp.amap.com/sse?key=YOUR_AMAP_API_KEY")
//                // .environment(Map.of("AMAP_API_KEY", envOrDefault("", "AMAP_API_KEY")))
//                .logRequests(true) // 根据需要启用请求日志
//                .logResponses(true) // 根据需要启用响应日志
//                .build();
//        McpClient mcpClient4 = new DefaultMcpClient.Builder()
//                .transport(transport4)
//                .build();

//        // Google Maps 服务器: 用于与 Google Maps API 进行交互，提供地图相关功能
//        McpTransport transport5 = new StdioMcpTransport.Builder()
//                .command(List.of("npx",
//                        "-y",
//                        "@modelcontextprotocol/server-google-maps"
//                ))
//                .environment(Map.of("GOOGLE_MAPS_API_KEY", "<YOUR_API_KEY>")) // 替换 <YOUR_API_KEY> 为你的实际 API 密钥
//                .logEvents(true) // 根据需要添加日志记录
//                .build();
//        McpClient mcpClient5 = new DefaultMcpClient.Builder()
//                .transport(transport5)
//                .build();

//        // Filesystem 服务器: 用于与本地文件系统交互，允许访问指定的目录
//        McpTransport transport6 = new StdioMcpTransport.Builder()
//                .command(List.of("npx",
//                        "-y",
//                        "@modelcontextprotocol/server-filesystem",
//                        "/Users/robinmacmini/Desktop",
//                        "/path/to/other/allowed/dir"
//                ))
//                // .environment(Map.of(...)) // 此配置中没有环境变量
//                .logEvents(true) // 根据需要添加日志记录
//                .build();
//        McpClient mcpClient6 = new DefaultMcpClient.Builder()
//                .transport(transport6)
//                .build();

        // Brave Search MCP 服务器，用于执行 Brave 搜索查询
        McpTransport transport7 = new StdioMcpTransport.Builder()
                .command(List.of("npx",
                        "-y",
                        "@modelcontextprotocol/server-brave-search"
                ))
                .environment(Map.of("BRAVE_API_KEY", envOrDefault("", "BRAVE_API_KEY", "braveApiKey")))
                .logEvents(true) // 根据需要启用或禁用日志记录
                .build();
        McpClient mcpClient7 = new DefaultMcpClient.Builder()
                .transport(transport7)
                .toolExecutionTimeout(Duration.ofSeconds(160)) // 设置工具执行超时时间
                .build();

//        // YouTube Transcript 服务器: 用于获取 YouTube 视频的文字记录
//        McpTransport transport8 = new StdioMcpTransport.Builder()
//                .command(List.of("npx",
//                        "-y",
//                        "@sinco-lab/mcp-youtube-transcript"
//                ))
//                // .environment(Map.of(...)) // 此配置中没有环境变量
//                .logEvents(true) // 根据需要添加日志记录
//                .build();
//        McpClient mcpClient8 = new DefaultMcpClient.Builder()
//                .transport(transport8)
//                .build();

//        // Feishu MCP 服务器: 用于与飞书（Lark）平台进行交互，例如发送消息、获取文档等
//        McpTransport transport9 = new StdioMcpTransport.Builder()
//                .command(List.of("npx",
//                        "-y",
//                        "feishu-mcp",
//                        "--stdio"
//                ))
//                .environment(Map.of(
//                        "FEISHU_APP_ID", envOrDefault("", "FEISHU_APP_ID"),
//                        "FEISHU_APP_SECRET", envOrDefault("", "FEISHU_APP_SECRET")
//                ))
//                .logEvents(true) // 根据需要添加日志记录
//                .build();
//        McpClient mcpClient9 = new DefaultMcpClient.Builder()
//                .transport(transport9)
//                .build();

        // YouTube 服务器: 用于与 YouTube Data API 交互，获取视频信息、搜索视频、获取字幕等
        String youtubeApiKey = envOrDefault("", "YOUTUBE_API_KEY_S", "YOUTUBE_API_KEY");
        String youtubeTranscriptLang = envOrDefault("ko", "YOUTUBE_TRANSCRIPT_LANG");

        McpTransport transport10 = new StdioMcpTransport.Builder()
                .command(List.of("npx",
                        "-y",
                        "youtube-data-mcp-server"
                ))
                .environment(Map.of(
                        "YOUTUBE_API_KEY", youtubeApiKey,
                        "YOUTUBE_TRANSCRIPT_LANG", youtubeTranscriptLang       // 指定字幕语言（默认为韩语，可通过环境变量覆盖）
                ))
                .logEvents(true) // 根据需要添加日志记录
                .build();
        McpClient mcpClient10 = new DefaultMcpClient.Builder()
                .transport(transport10)
                .toolExecutionTimeout(Duration.ofSeconds(160)) // 设置工具执行超时时间
                .build();

//        // 创建 MCP 传输实例
//        // Gitingest MCP 服务器: 用于通过 Git 仓库摄取数据
//        // 创建 MCP 传输实例
//        // Gitingest MCP 服务器: 用于通过 Git 仓库摄取数据 (使用 uvx 运行)
//        McpTransport transport11 = new StdioMcpTransport.Builder()
//                .command(List.of("/Users/robinmacmini/.local/bin/uvx", // 替换为你的 uvx 实际路径
//                        "--from",
//                        "git+https://github.com/puravparab/gitingest-mcp",
//                        "gitingest-mcp"
//                ))
//                // .environment(Map.of(...)) // 此配置中没有环境变量
//                .logEvents(true) // 根据需要添加日志记录
//                .build();
//        McpClient mcpClient11 = new DefaultMcpClient.Builder()
//                .transport(transport11)
//                .build();


        // 创建工具提供者
        // Register all MCP clients that you want to use
        List<McpClient> mcpClients = new ArrayList<>();
        mcpClients.add(mcpClient1);
        mcpClients.add(mcpClient2);
        // mcpClients.add(mcpClient3); // Uncomment if Desktop Commander is used
        // mcpClients.add(mcpClient4); // Uncomment if Amap SSE is used
        // mcpClients.add(mcpClient5); // Uncomment if Google Maps is used
        // mcpClients.add(mcpClient6); // Uncomment if Filesystem is used
        mcpClients.add(mcpClient7); // Brave Search
        // mcpClients.add(mcpClient8); // Uncomment if YouTube Transcript is used
        // mcpClients.add(mcpClient9); // Uncomment if Feishu is used
        mcpClients.add(mcpClient10); // YouTube Data
        // mcpClients.add(mcpClient11); // Uncomment if Gitingest is used

        ToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(mcpClients) // Pass the list of all active MCP clients
                .failIfOneServerFails(false)
                .build();


        String finalSystemMessage = systemMessage;
        System.out.println("finalSystemMessage: " + finalSystemMessage);
        System.out.println("modelName: " + modelName);
        AiPlayGroundTest aiPlayGroundTest = AiServices.builder(AiPlayGroundTest.class)
                .chatMemory(chatMemory)
                .chatModel(model)
                .systemMessageProvider(id -> finalSystemMessage)
                .toolProvider(toolProvider)
                .tools(new toolbox())
                .build();
        System.out.println("contentRaw: " + contentRaw);

        try {
            String chat = aiPlayGroundTest.chat(UserName + "(用户)：" + contentRaw);
            mainChannelServiceImplTestMapper.updateGeminiApiKeyUsageCount(apiKey, LocalDateTime.now());
            return chat;
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
            return "error: " + e.getMessage();
        } finally {
            // Gracefully close MCP clients
            mcpClient1.close();
            mcpClient2.close();
            mcpClient7.close();
            mcpClient10.close();
            // Close other mcpClients if they were initialized and added to the list
        }
    }




    class toolbox {
        public record WeatherForecast(
                String location,
                String forecast,
                int temperature) {
        }

        //        @Tool("Get the weather forecast for a location")
//        WeatherForecast getForecast(@P("Location to get the forecast for") String location) {
//            if (location.equals("Paris")) {
//                return new WeatherForecast("Paris", "sunny", 20);
//            } else if (location.equals("London")) {
//                return new WeatherForecast("London", "rainy", 15);
//            } else if (location.equals("Tokyo")) {
//                return new WeatherForecast("Tokyo", "warm", 32);
//            } else {
//                return new WeatherForecast("Unknown", "unknown", 0);
//            }
//        }
//
//
        @Tool("Get the gemini api key usage count")
        String getGeminiApiKeyUsageCount() {
            TreeMap<String, Integer> treeMap = new TreeMap<>();
            String[] geminiAccounts = new String[4];
            geminiAccounts[0] = "zuiquan200818@gmail.com";
            geminiAccounts[1] = "warghost200818@gmail.com";
            geminiAccounts[2] = "xuchenyang36robin@gmail.com";
            geminiAccounts[3] = "essnoto605@gmail.com";
            Integer totalCount = 0;
            List<gemini_api_key_entity> allGeminiApiKeys = mainChannelServiceImplTestMapper.getAllGeminiApiKeys();
            int i = 0;
            for (gemini_api_key_entity geminiApiKeyEntity : allGeminiApiKeys) {
                treeMap.put(geminiAccounts[i], geminiApiKeyEntity.getUsageCount());
                totalCount += geminiApiKeyEntity.getUsageCount();
                i++;
            }
            String usage = treeMap.toString();
            usage = "每个账号剩余使用的次数: " + usage + "\n" + "总剩余次数: " + totalCount;
            return usage;
        }

        @Tool("create a new AGENT with the system prompt and insert it into the database")
        String createAGENT(@P("SYSTEM PROMPT") String systemPrompt, @P("UserName") String UserName) {
            aiPrompt aiPrompt = new aiPrompt(systemPrompt, UserName, LocalDateTime.now(), "gemini-2.5-pro-exp-03-25", "AGENT", 0);
            aiMapper.addAi(aiPrompt);
            chatMemory.clear();
            return "SystemPrompt updated: " + "systemPrompt: " + systemPrompt + " UserName: " + UserName;
        }


        @Tool("Get FULL AGENT LIST INFO,if an AGENT does not enable, it should be also show to the user")
        String getMCPAGENTLIST() {
            List<aiPrompt> agents = aiMapper.getALLAgentByCategory("AGENT");
            StringBuilder agentsInfo = new StringBuilder();
            for (aiPrompt agent : agents) {
                agentsInfo.append(agent.toString()).append("\n");
            }
            System.out.println(agentsInfo.toString());
            return agentsInfo.toString() + "\n" + "用markdown语法合理包裹这些信息，使用一些表情来增强信息的可读性";
        }

        @Tool("update selected AGENT SYSTEM PROMPT,if has no system prompt,then use default system prompt")
        String updateAGENTSYSTEMPROMPT(@P("AGENT ID") Integer id, @P(value = "SYSTEM PROMPT", required = false) String systemPrompt, @P("UserName") String UserName) {
            aiPrompt aiPrompt = new aiPrompt(systemPrompt, UserName, LocalDateTime.now(), "AGENT", 1);
            aiMapper.unenableALL("AGENT");
            aiMapper.updateAI(aiPrompt, id);
            return "update success" + " id: " + id + " systemPrompt: " + systemPrompt;
        }

        @Tool("enable selected AGENT , the other AGENT will be disabled")
        String enableAGENTSYSTEMPROMPT(@P("AGENT ID") Integer id, @P("UserName") String UserName) {
            aiPrompt aiPrompt = new aiPrompt(UserName, LocalDateTime.now(), "AGENT", 1);
            aiMapper.unenableALL("AGENT");
            aiMapper.updateAI(aiPrompt, id);
            return "enable success" + " id: " + id;
        }

        @Tool("delete selected AGENT")
        String deleteAGENT(@P("AGENT ID") Integer id) {
            aiPrompt agent = aiMapper.getAiByCategory("AGENT");
            if (agent.getId() == id) {
                return "AGENT cannot be deleted,please select another AGENT id first";
            }
            aiMapper.deleteAiById(id);
            return "delete success" + " id: " + id;
        }

        @Tool("Get FULL MCP LIST INFO, if a mcp does not enable, it should be also show to the user")
        String getMCPAGENTLISTINFO() {
            List<aiPrompt> agents = aiMapper.getALLAgentByCategory("MCP");
            StringBuilder agentsInfo = new StringBuilder();
            for (aiPrompt agent : agents) {
                agentsInfo.append(agent.toString()).append("\n");
            }
            System.out.println(agentsInfo.toString());
            return agentsInfo.toString() + "\n" + "Please output the full info of the MCP, including all the fields";
        }

        @Tool("update selected MCP SYSTEM PROMPT,if has no system prompt,then use default system prompt")
        String updateMCPSYSTEMPROMPT(@P("MCP ID") Integer id, @P(value = "SYSTEM PROMPT", required = false) String systemPrompt, @P("UserName") String UserName) {
            aiPrompt aiPrompt = new aiPrompt(systemPrompt, UserName, LocalDateTime.now(), "MCP", 1);
            aiMapper.unenableALL("MCP");
            aiMapper.updateAI(aiPrompt, id);
            return "update success" + " id: " + id + " systemPrompt: " + systemPrompt;
        }

        @Tool("enable selected MCP, the other MCP will be disabled")
        String enableMCPSYSTEMPROMPT(@P("MCP ID") Integer id, @P("UserName") String UserName) {
            aiPrompt aiPrompt = new aiPrompt(UserName, LocalDateTime.now(), "MCP", 1);
            aiMapper.unenableALL("MCP");
            aiMapper.updateAI(aiPrompt, id);
            return "enable success" + " id: " + id;
        }

        @Tool("delete selected MCP")
        String deleteMCP(@P("MCP ID") Integer id) {
            aiPrompt agent = aiMapper.getAiByCategory("AGENT");
            if (agent.getId() == id) {
                return "MCP cannot be deleted,please select another MCP id first";
            }
            aiMapper.deleteAiById(id);
            return "delete success" + " id: " + id;
        }



        @Tool("Describe the structure and provide example data for the ai_prompts table.")
        String describeAiPromptsTable() {
            String description = """
                    The `ai_prompts` table stores configurations for different AI personalities or agents.
                    
                    Table Schema:
                    CREATE TABLE ai_prompts (
                        id            INT AUTO_INCREMENT PRIMARY KEY, -- Unique identifier for the prompt configuration
                        prompt        TEXT        NULL, -- The system prompt text defining the AI's behavior or persona
                        creator       VARCHAR(30) NULL, -- The user who created this configuration
                        creation_time DATETIME    NULL, -- Timestamp when the configuration was created
                        model_name    VARCHAR(50) NULL, -- The specific AI model used (e.g., gemini-2.5-pro-exp-03-25)
                        category      VARCHAR(30) NULL, -- Category of the prompt (e.g., 'AGENT', 'MCP')
                        is_enable     TINYINT(1)  NULL  -- Flag indicating if this configuration is currently active (1 = enabled, 0 = disabled)
                    );
                    
                    Example Data:
                    | id | prompt                                                      | creator     | creation_time       | model_name                 | category | is_enable |
                    |----|-------------------------------------------------------------|-------------|---------------------|----------------------------|----------|-----------|
                    | 1  | 你是一个强大的助手, 擅长分析用户的问题并使用各种...         | Crispy Frog | 2025-04-11 00:58:14 | gemini-2.5-pro-exp-03-25 | AGENT    | 0         |
                    | 3  | 你是tool user                                               | Crispy Frog | 2025-04-10 00:25:53 | gemini-2.5-pro-exp-03-25 | MCP      | 1         |
                    | 5  | 你是大傻逼                                                  | Crispy Frog | 2025-04-10 00:31:13 | gemini-2.5-pro-exp-03-25 | MCP      | 0         |
                    | 6  | 你擅长调用多个工具链组合完成任务                          | Crispy Frog | 2025-04-10 00:30:03 | gemini-2.5-pro-exp-03-25 | MCP      | 0         |
                    | 8  | 你是一个全能助手                                            | Crispy Frog | 2025-04-10 23:23:06 | gemini-2.5-pro-exp-03-25 | AGENT    | 0         |
                    | 9  | 你是一个攻击性拉满的小学生                                  | Crispy Frog | 2025-04-10 23:37:05 | gemini-2.5-pro-exp-03-25 | AGENT    | 0         |
                    | 12 | 你是一个大聪明                                              | Crispy Frog | 2025-04-11 01:00:07 | gemini-2.5-pro-exp-03-25 | AGENT    | 1         |
                    | 13 | 你是一个拥有替身能力的AI助手, 说话风格模仿《JOJO...         | Crispy Frog | 2025-04-11 21:12:11 | gemini-2.5-pro-exp-03-25 | AGENT    | 0         |
                    
                    noticed: 
                    1. if you enable an agent or a mcp , the other agent or mcp will be disabled, you can only enable one agent and one mcp at a time.
                    Use the `sqlExecute` tool to interact with this table using SQL generated based on this description.
                    """;
            return description;
        }

        @Tool("If there is no tools can implement users thoughts than use this tool to Executes a given SQL statement against the database. Use 'describeAiPromptsTable' to understand the 'ai_prompts' table structure before generating SQL.")
        String sqlExecute(@P("The SQL statement to execute. Should typically target the 'ai_prompts' table based on its description. The sql grammar should fit with mysql") String sql) {
            if (sql == null || sql.trim().isEmpty()) {
                return "Error: SQL statement cannot be empty.";
            }
            // Basic check to differentiate query types (case-insensitive trim)
            String trimmedSql = sql.trim();
            boolean isSelect = trimmedSql.regionMatches(true, 0, "SELECT", 0, "SELECT".length());

            try {
                if (isSelect) {
                    List<Map<String, Object>> results = aiMapper.executeSelect(trimmedSql);
                    if (results == null || results.isEmpty()) {
                        return "Query executed successfully. No results found.";
                    }
                    // Convert results to a readable string format (e.g., JSON)
                    return "Query executed successfully. Results:\n" + cn.hutool.json.JSONUtil.toJsonPrettyStr(results);
                } else {
                    // Assume INSERT, UPDATE, DELETE
                    // Basic validation to prevent accidental execution of clearly invalid non-SELECT statements
                    if (!trimmedSql.regionMatches(true, 0, "INSERT", 0, "INSERT".length()) &&
                            !trimmedSql.regionMatches(true, 0, "UPDATE", 0, "UPDATE".length()) &&
                            !trimmedSql.regionMatches(true, 0, "DELETE", 0, "DELETE".length())) {
                        return "Error: SQL statement does not appear to be a valid SELECT, INSERT, UPDATE, or DELETE command.";
                    }

                    int rowsAffected = aiMapper.executeUpdateOrInsertOrDelete(trimmedSql);
                    return "Update executed successfully. Rows affected: " + rowsAffected;
                }
            } catch (Exception e) {
                // Catch generic Exception, as MyBatis might wrap SQLExceptions
                System.err.println("Error executing SQL: " + sql + "\n" + e.getMessage());
                // Provide a safe error message to the user/LLM
                return "Error executing SQL statement. Please check the syntax and ensure it's valid. Details: " + e.getMessage();
            }
        }

        @Tool("设置小猫翻译的人设")
        String setBrowserExtensionSystemprompt(@P("人设") String systemprompt) {
            try {
                apiService.setSystemprompt(systemprompt);
                return "operation success";
            } catch (Exception e) {
                return "operation failed";
            }
        }


    }

    @Override
    public String clearChatMemory(String sid) {
        try {
            chatMemory.clear();
            return "Chat memory cleared.";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @Override
    public String aiPlayGroundAGENT(MessageReceivedEvent event) {
        // TODO: Implement asynchronous logic if this method also performs long-running AI operations
        return null; // Or supplyAsync if it becomes complex
    }

    @Override
    public String aiWebAGENT(String sid , User user, String message) {
        return null;
    }

    private String envOrDefault(String defaultValue, String... keys) {
        if (keys == null) {
            return defaultValue;
        }
        for (String key : keys) {
            if (key == null || key.isBlank()) {
                continue;
            }
            String value = dotenv.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return defaultValue;
    }
}
