package robin.discordbot.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpUtil;
import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.web.multipart.MultipartFile;
import robin.discordbot.tutorial.modality;

import robin.discordbot.utils.ai.geminiModel;

public class GeminiAudioUtil {

    private static Dotenv dotenv = Dotenv.load();


    static public String Audio(MultipartFile file, String prompt) {
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(GeminiFactory.getGeminiToken()) // 需要替换为你的 Gemini API 密钥 [2]
                .modelName("gemini-2.5-flash-preview-05-20") //  模型名称 [2]
                .build();

        //音频分析测试
        try {
//            String audioUrl = "https://pub-d2e4cfca78f042f29331f4f9fcf74111.r2.dev/%E5%BD%95%E7%94%A8%E6%B3%A8%E6%84%8F%E4%BA%8B%E9%A1%B9.mp3";
//
//            // 使用Hutool下载音频文件
//            byte[] audioData = HttpUtil.downloadBytes(audioUrl);

            // 转换为Base64字符串
            String base64Audio = Base64.encode(file.getInputStream());

            // 使用Base64字符串创建AudioContent (指定MIME类型为mp3)
            AudioContent audioContent = AudioContent.from(base64Audio, "audio/mp3");
            SystemMessage systemMessage = SystemMessage.from(prompt);
            UserMessage userMessage= UserMessage.from(audioContent);


            ChatResponse chat1 = model.chat(systemMessage, userMessage);
            String text = chat1.aiMessage().text();
            return text;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("处理音频时出错: " + e.getMessage());
            return "处理音频时出错: " + e.getMessage();
        }
    }
}
