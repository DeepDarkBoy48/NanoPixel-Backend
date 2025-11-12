package robin.discordbot.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import robin.discordbot.mapper.AiMapper;
import robin.discordbot.mapper.MainChannelServiceImplTestMapper;
import robin.discordbot.pojo.entity.User;
import robin.discordbot.pojo.entity.aiEntity.aiPrompt;
import robin.discordbot.pojo.entity.aiEntity.gemini_api_key_entity;
import robin.discordbot.service.MainChannelAIService;
import robin.discordbot.utils.GeminiFactory;

@Service
public class MainChannelAIServiceImplAGENT implements MainChannelAIService {

    @Autowired
    private AiMapper aiMapper;

    @Autowired
    private MainChannelServiceImplTestMapper mainChannelServiceImplTestMapper;


    //  创建一个全局共享的 ChatMemory 实例
    private final ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(100).build();


    interface AiPlayGroundTest {
        String chat(@UserMessage String message) throws NoSuchMethodException;
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

        aiPrompt aiEntity = aiMapper.getAiByCategory("AGENT");

//        if (aiEntity.getModelName().isEmpty() || aiEntity.getPrompt().isEmpty()) {
//            aiPrompt aiPrompt = new aiPrompt();
//            aiPrompt.setModelName("gemini-2.0-flash");
//            aiPrompt.setPrompt("Hello, I am Gemini. How can I help you?");
//            aiPrompt.setCreationTime(LocalDateTime.now());
//            aiMapper.updateAI(aiPrompt, 1);
//            aiEntity = aiMapper.getAiById(1);
//        }
        modelName = aiEntity.getModelName();
        systemMessage = aiEntity.getPrompt();

        if (contentRaw != null && contentRaw.startsWith("@M ")) {
            modelName = contentRaw.substring(3);
            String globalName = event.getAuthor().getGlobalName();
            LocalDateTime now = LocalDateTime.now();
            Integer id = aiEntity.getId();
            aiPrompt aiPrompt = new aiPrompt(1, globalName, now, modelName);
            aiMapper.updateAI(aiPrompt, id);
            return "ModelName updated: " + modelName;
        }
        if (contentRaw != null && contentRaw.startsWith("@S ")) {
            systemMessage = contentRaw.substring(3);
            String globalName = event.getAuthor().getGlobalName();
            LocalDateTime now = LocalDateTime.now();
            aiPrompt aiPrompt = new aiPrompt(systemMessage, globalName, now, "gemini-2.5-pro-exp-03-25", "AGENT", 1);
            // 所有agent都关闭
            aiMapper.unenableALL("AGENT");
            aiMapper.addAi(aiPrompt);
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


        //todo  // 创建low level工具
//        //define the toolSpecifications
//        List<ToolSpecification> toolSpecifications = ToolSpecifications.toolSpecificationsFrom(MainChannelAIServiceImplMCP.toolbox.class);
//        // define the ToolExecutor
//        List<ToolExecutor> toolExecutors = new ArrayList<>();
//        toolExecutors.add((ToolExecutionRequest toolExecutionRequest, Object memoryId) -> {
//            Map<String, Object> arguments = JSONUtil.parseObj(toolExecutionRequest.arguments());
//            return new MainChannelAIServiceImplAGENT.toolbox().getForecast(arguments.get("location").toString()).toString();
//        });
//        toolExecutors.add((ToolExecutionRequest toolExecutionRequest, Object memoryId) -> {
//            Map<String, Object> arguments = JSONUtil.parseObj(toolExecutionRequest.arguments());
//            return new MainChannelAIServiceImplAGENT.toolbox().getGeminiApiKeyUsageCount().toString();
//        });
//        ToolProvider toolProvider = (ToolProviderRequest) -> {
//            return ToolProviderResult.builder()
//                    .add(toolSpecifications.get(0), toolExecutors.get(0))
//                    .add(toolSpecifications.get(1), toolExecutors.get(1))
//                    .build();
//        };

        String finalSystemMessage = systemMessage;
        System.out.println("finalSystemMessage: " + finalSystemMessage);
        System.out.println("modelName: " + modelName);
        MainChannelAIServiceImplMCP.AiPlayGroundTest aiPlayGroundTest = AiServices.builder(MainChannelAIServiceImplMCP.AiPlayGroundTest.class)
                .chatMemory(chatMemory)
                .chatModel(model)
                .systemMessageProvider(id -> finalSystemMessage)
                .tools(new toolbox())
//                .toolProvider(toolProvider)
                .build();
        System.out.println("contentRaw: " + contentRaw);

        try {
            String chat = aiPlayGroundTest.chat(UserName +"说"+ contentRaw);
            mainChannelServiceImplTestMapper.updateGeminiApiKeyUsageCount(apiKey, LocalDateTime.now());
            return chat;
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
            return "error: " + e.getMessage();
        } finally {
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
//        @Tool("Get the gemini api key usage count")
//        String getGeminiApiKeyUsageCount() {
//            TreeMap<String, Integer> treeMap = new TreeMap<>();
//            String[] geminiAccounts = new String[4];
//            geminiAccounts[0] = "zuiquan200818@gmail.com";
//            geminiAccounts[1] = "warghost200818@gmail.com";
//            geminiAccounts[2] = "xuchenyang36robin@gmail.com";
//            geminiAccounts[3] = "essnoto605@gmail.com";
//            Integer totalCount = 0;
//            List<gemini_api_key_entity> allGeminiApiKeys = mainChannelServiceImplTestMapper.getAllGeminiApiKeys();
//            int i = 0;
//            for (gemini_api_key_entity geminiApiKeyEntity : allGeminiApiKeys) {
//                treeMap.put(geminiAccounts[i], geminiApiKeyEntity.getUsageCount());
//                totalCount += geminiApiKeyEntity.getUsageCount();
//                i++;
//            }
//            String usage = treeMap.toString();
//            usage = "每个账号剩余使用的次数: " + usage + "\n" + "总剩余次数: " + totalCount;
//            return usage;
//        }

        @Tool("create a new AGENT with the system prompt and insert it into the database")
        String createAGENT(@P("SYSTEM PROMPT") String systemPrompt, @P("UserName") String UserName) {
            aiPrompt aiPrompt = new aiPrompt(systemPrompt, UserName, LocalDateTime.now(), "gemini-2.5-pro-exp-03-25","AGENT", 0);
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
            return agentsInfo.toString()+"\n" + "用markdown语法合理包裹这些信息，使用一些表情来增强信息的可读性";
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
            return agentsInfo.toString()+"\n" + "Please output the full info of the MCP, including all the fields";
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

        @Tool("clear the chat memory")
        String clearChatMemory() {
            chatMemory.clear();
            return "Chat memory cleared.";
        }


        // 刷新gemini轮询池
        @Tool("renew gemini api key pool, return the key with usage times ")
        List<gemini_api_key_entity> renewGeminiApiKeyPool() {
            List<gemini_api_key_entity> geminiApiKeyEntities = GeminiFactory.updateGeminiApiKey();
            return geminiApiKeyEntities;
        }

    }

    @Override
    public String aiPlayGroundMCP(MessageReceivedEvent event) {
        return null;
    }

    @Override
    public String aiWebAGENT(String sid, User user, String message) {
        return null;
    }


}
