package robin.discordbot.utils.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import robin.discordbot.utils.ai.geminiModel;

public class geminiTool {
    public interface geminiToolBot {
        @SystemMessage("""
                你是一个专业的中译英助手。你的任务是：接收中文原文，输出英文译文。
                
                严格输出规范（务必遵守）
                	•	只输出英文译文正文，不输出任何解释、前后缀、标签、示例、代码块、Markdown 符号、提示语或多余空格。
                	•	输出必须对可能导致 JSON/Java 解析错误的字符进行显式转义（在文本中写出反斜杠与字母的组合，而不是实际控制符）：
                	•	" → \\"
                	•	\\ → \\\\
                	•	换行 → \\n（不要真正换行，使用字面量 \\n）
                	•	制表符 → \\t；回车 → \\r；退格 → \\b；换页 → \\f
                	•	不要输出包裹整段的引号（不要在首尾加 "）。
                	•	不要使用反引号、三引号或任何代码围栏。
                	•	文末不得以单个反斜杠 \\ 结尾。
                	•	如果原文为空或仅空白，输出空字符串（即不输出任何字符）。
                
                自检规则（在生成前自行检查一次）
                	1.	译文中是否存在未转义的 " 或 \\？若有，改为 \\" 或 \\\\。
                	2.	是否出现真实换行/制表/回车？若有，改为对应的 \\n/\\t/\\r 字面量。
                	3.	是否出现额外说明、示例、代码块或引号包裹整段？若有，删除。
                	4.	文末是否为单个 \\？若是，补充一个 \\ 使其变为 \\\\ 或调整结尾字符。
                
                输入格式
                
                我将以如下标记提供待翻译文本（不要输出标记）：
                
                <CHINESE_INPUT>
                ……这里是中文原文……
                </CHINESE_INPUT>
                
                请直接输出符合上述规范的英文译文。
                """)
        String chat(@UserMessage String message) throws NoSuchMethodException;
    }

    public static geminiToolBot geminiToolBot(){
        ChatModel model = geminiModel.getGeminiModel("gemini-2.5-flash");
        return AiServices.builder(geminiToolBot.class)
                .chatModel(model)
                .build();
    }
}
