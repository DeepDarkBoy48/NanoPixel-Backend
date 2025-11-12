package robin.discordbot.tutorial;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpUtil;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.github.cdimascio.dotenv.Dotenv;

public class modality {
    private static Dotenv dotenv = Dotenv.load();
    private static String getGeminiToken(){
        return dotenv.get("GEMINI2");
    }

    interface ModalityTest {
        @SystemMessage("将{{content}}翻译为{{language}}，精确翻译，不准漏译少译或者增加内容，只输出翻译结果，不需要其他操作")
        String chat(@UserMessage String content , @V("language") String language) throws NoSuchMethodException;

        @SystemMessage("如下{{audio}}是音频的内容，写一个总结报告")
        String audioAnalyzer(@UserMessage AudioContent audio) throws NoSuchMethodException;
    }

    class toolbox {
        @Tool("解析base64Audio音频")
        public String summarize(String base64Audio){
            return "summarize the given audio";
        }
    }

    public static void main(String[] args) throws NoSuchMethodException {
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(getGeminiToken()) // 需要替换为你的 Gemini API 密钥 [2]
                .modelName("gemini-2.5-flash-preview-05-20") //  模型名称 [2]
                .build();
        ModalityTest modalityTest = AiServices.builder(ModalityTest.class)
                .chatModel(model)
                .build();

        //翻译测试
        String audio = "你是一个工商银行员工,明天参加会议";
        String chat = modalityTest.chat(audio, "en");
        System.out.println(chat);

        //音频分析测试
        try {
            String audioUrl = "https://pub-d2e4cfca78f042f29331f4f9fcf74111.r2.dev/%E5%BD%95%E7%94%A8%E6%B3%A8%E6%84%8F%E4%BA%8B%E9%A1%B9.mp3";
            
            // 使用Hutool下载音频文件
            byte[] audioData = HttpUtil.downloadBytes(audioUrl);
            
            // 转换为Base64字符串
            String base64Audio = Base64.encode(audioData);
            
            // 使用Base64字符串创建AudioContent (指定MIME类型为mp3)
            AudioContent audioContent = AudioContent.from(base64Audio, "audio/mp3");
            ChatMessage chatMessage = dev.langchain4j.data.message.UserMessage.from(audioContent);
            ChatMessage chatMessage2 = dev.langchain4j.data.message.UserMessage.from("请总结以上录音");

            ChatResponse chat1 = model.chat(chatMessage,  chatMessage2);
            String text = chat1.aiMessage().text();

            System.out.println(text);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("处理音频时出错: " + e.getMessage());
        }
    }
}
