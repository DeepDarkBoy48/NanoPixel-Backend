package robin.discordbot.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.github.cdimascio.dotenv.Dotenv;
import robin.discordbot.utils.ai.geminiModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WebAgent {
    Dotenv dotenv = Dotenv.load();

    // 1) 工具：搜索 + 抓取（你用 HTTP 调 Brave/自建搜索都行）
    class WebTools {

        private static final String TAVILY_SEARCH_ENDPOINT = "https://api.tavily.com/search";
        private final String tavilyApiKey = dotenv.get("tavilyApiKey");

        @Tool("Full fill the search parameters and this tool will return a list of search results and relevant image urls")
        public String search(@P(value = "search query", required = true) String q,
                @P(value = "max_results", required = true) int max_results,
                @P(value = "start_date") String start_date,
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
            // TavilyResponse response = JSONUtil.toBean(body, TavilyResponse.class);
            return body;
        }

        @Tool("before search, please get localtime")
        public String getLocalTime() {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

    }

    // 2) 研究员 Agent：按需调用工具，最后写到 'answer'
    public interface ResearchAgent {
        @UserMessage("""
                  You are a writer.
                  You need to write an article based on the search results.
                  notice! before search, please use tool 'get localtime' to get the current local time.
                  If needed, use tools 'search' to gather evidence.
                  For query: {{q}}
                  Output an article with origin web urls and image urls, the style is html.
                """)
        @Agent(outputName = "article", description = "Writes an article based on the search results")
        String research(@V("q") String q);
    }

    // 3) 评审 Agent：给当前 answer 打分写到 'score'
    public interface Judge {
        @UserMessage("""
                  Score from 0.0 to 1.0 how factually supported and complete the current answer is.
                  Return ONLY the number.
                  Query: {{q}}
                  Answer: {{article}}
                """)
        @Agent(outputName = "score", description = "Judges the factuality and completeness of the current article")
        double score(@V("q") String q, @V("article") String article);
    }

    public String webSearch(String query) {

        ResearchAgent researchAgent = AgenticServices.agentBuilder(ResearchAgent.class)
                .chatModel(geminiModel.getGeminiModel("gemini-2.5-flash"))
                .tools(new WebTools())
                .outputName("article")
                .build();

        Judge judge = AgenticServices
                .agentBuilder(Judge.class)
                .chatModel(geminiModel.getGeminiModel("gemini-2.5-flash"))
                .outputName("score")
                .build();

        UntypedAgent searchLoop = AgenticServices.loopBuilder()
                .subAgents(researchAgent, judge)
                .maxIterations(1)
                .exitCondition(scope -> {
                    double score = scope.readState("score", 0.0);
                    System.out.println("Judge score: " + score);
                    return score >= 0.8;
                }) // 从 AgenticScope 读 'score' 退出
                .outputName("article")
                .build(); // 顺序/循环工作流写法见文档示例。
        ResultWithAgenticScope<String> resultWithAgenticScope = searchLoop.invokeWithAgenticScope(Map.of("q", query));
        String result = resultWithAgenticScope.result();
        AgenticScope scope = resultWithAgenticScope.agenticScope();
        if (scope != null) {
            System.out.println("Loop state: " + scope.state());
        }
        if (result != null) {
            // System.out.println(result);
            return result;
        }
        return "web search failed";
    }

    public static void main(String[] args) {

        WebAgent webAgent = new WebAgent();
        String result = webAgent.webSearch("张雪峰怎么了");
    }
}
