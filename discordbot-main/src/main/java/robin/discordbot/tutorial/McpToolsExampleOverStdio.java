package robin.discordbot.tutorial;

import java.io.File;
import java.util.List;
import java.util.Map;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import io.github.cdimascio.dotenv.Dotenv;
import robin.discordbot.record.llmModel;

public class McpToolsExampleOverStdio {
    private static Dotenv dotenv = Dotenv.load();

    public static String getApiKey() {
        return dotenv.get("GEMINI2");
    }

    // We will let the AI read the contents of this file
    public static final String FILE_TO_BE_READ = "src/main/resources/file.txt";

    /**
     * This example uses the `server-filesystem` MCP server to showcase how
     * to allow an LLM to interact with the local filesystem.
     * <p>
     * Running this example requires npm to be installed on your machine,
     * because it spawns the `server-filesystem` as a subprocess via npm:
     * `npm exec @modelcontextprotocol/server-filesystem@0.6.2`.
     * <p>
     * Of course, feel free to swap out the server with any other MCP server.
     * <p>
     * The communication with the server is done directly via stdin/stdout.
     * <p>
     * IMPORTANT: when executing this, make sure that the working directory is
     * equal to the root directory of the project
     * (`langchain4j-examples/mcp-example`), otherwise the program won't be able to find
     * the proper file to read. If you're working from another directory,
     * adjust the path inside the StdioMcpTransport.Builder() usage in the main method.
     */
    public static void main(String[] args) throws Exception {


        interface Bot {

            String chat(String prompt);
        }

        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(getApiKey()) // 需要替换为你的 Gemini API 密钥 [2]
                .modelName(llmModel.GEMINI_2_5_PRO.getModle()) //  模型名称 [2]
                .logRequestsAndResponses(true) // 日志请求和响应 [2]
                .build();


        McpTransport transport = new StdioMcpTransport.Builder()
                .command(List.of("/Users/robinmacmini/.nvm/versions/node/v23.5.0/bin/npx",
                        "npx",
                        "-y",
                        "tavily-mcp@0.1.4"
                ))
                .environment(Map.of("TAVILY_API_KEY", "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"))
                .logEvents(true)
                .build();


        McpClient mcpClient = new DefaultMcpClient.Builder()
                .transport(transport)
                .build();

        ToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(List.of(mcpClient))
                .build();

        Bot bot = AiServices.builder(Bot.class)
                .chatModel(model)
                .toolProvider(toolProvider)
                .build();

        try {
//            File file = new File(FILE_TO_BE_READ);
//            String response = bot.chat("Read the contents of the file " + file.getAbsolutePath());

            String response = bot.chat("介绍一下你使用的工具");
            System.out.println("RESPONSE: " + response);
        } finally {
            mcpClient.close();
        }
    }
}