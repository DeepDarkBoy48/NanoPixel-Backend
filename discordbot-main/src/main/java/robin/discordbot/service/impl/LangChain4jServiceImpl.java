package robin.discordbot.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.output.Response;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import robin.discordbot.config.Langchain4j;
import robin.discordbot.pojo.entity.aiEntity.*;
import robin.discordbot.service.LangChain4jService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LangChain4jServiceImpl implements LangChain4jService {
//    @Autowired
//    private Langchain4j.AiAssistantFigure deepDarkAiHTMLFigure;
//    @Autowired
//    private Langchain4j.AiAssistantText deepDarkAiText;
//    @Autowired
//    private Langchain4j.AiAssistantThreadText deepDarkThreadAi;
//    @Autowired
//    private Langchain4j.embed embed;
    @Autowired
    private Langchain4j.aiSearchTavily aiSearchTavily;
//    @Autowired
//    private Langchain4j.AiAssistantGemini gemini;
//    @Resource
//    private Langchain4j.AiAssistantGeminiTranslateEN2CN geminiTranslateEN2CN;
//    @Resource
//    private Langchain4j.AiAssistantGeminiTranslateCN2EN geminiTranslateCN2EN;
//    @Resource
//    private Langchain4j.AiAssistantPlayground aiPlayground;

    private final Dotenv dotenv = Dotenv.load();

    public String getOpenaiToken() {
        return dotenv.get("openaiToken");
    }

    public String getTavilyToken() {
        return dotenv.get("tavilyToken");
    }
    public String getDiscordToken() {
        return dotenv.get("discordToken");
    }

    public String getGrokToken() {
        return dotenv.get("grokToken");
    }

//    public ImageModel chatLanguageModelImage = OpenAiImageModel.builder()
//            .apiKey(getOpenaiToken())
//            .modelName("DALL·E 2")
//            .build();

//    @Override
//    public byte[] deepDarkAiHTMLFigure(String id, AiMessageFormat aiMessageFormat) {
//        String deepDarkResult = deepDarkAiHTMLFigure.chat(id, aiMessageFormat);
//        // html2picture.htmlTransferImage(deepDarkResult);
//        HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
//        // 加载 HTML 内容
//        imageGenerator.setSize(new Dimension(800, 600));
//        imageGenerator.loadHtml(deepDarkResult);
//        // 保存图片到指定路径
//        // imageGenerator.saveAsImage("output.png");
//        // 生成 BufferedImage
//        BufferedImage bufferedImage = imageGenerator.getBufferedImage();
//        // 将 BufferedImage 转换为 byte[]
//        // 使用 ByteArrayOutputStream 将 BufferedImage 转换为 byte[]
//        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//            ImageIO.write(bufferedImage, "png", baos); // 将图片写入字节流，格式为 PNG
//            return baos.toByteArray(); // 返回字节数组
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null; // 处理异常，返回 null
//        }
//    }

//    @Override
//    public String deepdarkaiText(String id, AiMessageFormat aiMessageFormat) {
//        String deepDarkResult = deepDarkAiText.chat(id, aiMessageFormat);
//        return deepDarkResult;
//    }

//    @Override
//    public String deepdarkaiImage(String id, AiMessageFormat aiMessageFormat) {
//        String message = aiMessageFormat.getMessage();
//        Response<Image> response = chatLanguageModelImage.generate(message);
//        return response.content().url().toString();
//    }

//    @Override
//    public String deepDarkTreadAiChat(String id, AiMessageFormat aiMessageFormat) {
//        String deepDarkResult = deepDarkThreadAi.chat(id, aiMessageFormat);
//        return deepDarkResult;
//    }

//    @Override
//    public String embediframe(String id, AiMessageFormat aiMessageFormat) {
//        String embedResult = embed.chat(id, aiMessageFormat);
//        return embedResult;
//    }

    @Override
    public aiSearchFinalEntity aisearch(String id, AiMessageFormat aiMessageFormat) {
        String url = "https://api.tavily.com/search";
        String apiKey = getTavilyToken();
        List<String> domains = new ArrayList<>();
        boolean include_images = true;
        boolean include_images_descriptions = false;
        String search_depth = "advanced";
        String query = aiMessageFormat.getMessage();
        Boolean include_raw_content = false;
        // 构建请求数据
        JSONObject data = new JSONObject();
        data.put("api_key", apiKey);
        data.put("query", query);
        data.put("include_images", include_images);
        data.put("include_images_descriptions", include_images_descriptions);
        data.put("search_depth", search_depth);
        data.put("include_raw_content",include_raw_content);
//        data.put("include_domains", domains);
        // 发送 POST 请求
        String response = HttpUtil.post(url, data.toString());
        JSONObject jsonObject = JSONUtil.parseObj(response);
        //json转为java实体类
        aiSearchEntity aiSearchEntity = jsonObject.toBean(aiSearchEntity.class);

        //ai搜索的图片
        List<String> images = aiSearchEntity.getImages();
        StringBuilder stringBuilder = new StringBuilder();
        for (String image : images){
             stringBuilder.append(image).append("\n");
        }
        String imagesInfo = stringBuilder.toString();

        //ai搜索的内容
        List<aiSearchResultEntity> results = aiSearchEntity.getResults();
        StringBuilder stringBuilder2 = new StringBuilder();
        for (aiSearchResultEntity result : results){
            stringBuilder2.append(result.toString()).append("\n");
        }
        String resultInfo = stringBuilder2.toString();

        //resultInfo交给ai重构优化
//        AiMessageFormat aiMessageFormat1 = new AiMessageFormat();
//        aiMessageFormat1.setMessage(resultInfo);
        aiSearchOutputEntity aiSearchOutputEntity = aiSearchTavily.chat(id, resultInfo);

        aiSearchFinalEntity aiSearchFinalEntity = new aiSearchFinalEntity();
        aiSearchFinalEntity.setImagesInfo(imagesInfo);
        aiSearchFinalEntity.setAiSearchOutputEntity(aiSearchOutputEntity);

        return aiSearchFinalEntity;
    }

    @Override
    public String grok(String message) {
        // 设置 URL 和请求头
        String url = "https://api.x.ai/v1/chat/completions";
        // 构建请求体数据
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("model", "grok-beta");
        requestData.put("stream", false);
        requestData.put("temperature", 0);
        // 构建消息数组
        List<Map<String, String>> messages = new ArrayList<>();
        // 系统消息
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are Grok, a chatbot inspired by the Hitchhikers Guide to the Galaxy.");
        messages.add(systemMessage);
        // 用户消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messages.add(userMessage);
        requestData.put("messages", messages);
        try {
            // 发送POST请求
            HttpResponse response = HttpRequest.post(url)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + getGrokToken())
                    .body(JSONUtil.toJsonStr(requestData))
                    .execute();
            // 解析响应
            String body = response.body();
            JSONObject jsonObject = JSONUtil.parseObj(body);
            String grokResult = jsonObject.getByPath("choices[0].message.content", String.class);
            return grokResult;
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, there was an error processing your request: " + e.getMessage();
        }
    }



    @Override
    public aiSearchFinalEntity aisearchNSFW(String id, AiMessageFormat aiMessageFormat) {
        return null;
    }


}
