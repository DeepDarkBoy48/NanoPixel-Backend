package robin.discordbot;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONArray;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class nanobanana {

    private static final Dotenv dotenv = Dotenv.load();
    
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image-preview:generateContent";
    private static final String GEMINI_API_KEY = dotenv.get("geminiTier1Token");
    
    public static void generateNanoBananaCatImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.err.println("图片文件不存在: " + imagePath);
                return;
            }
            
            String imageBase64 = Base64.encode(FileUtil.readBytes(imageFile));
            
            JSONObject requestBody = createRequestBody(imageBase64);
            
            HttpResponse response = HttpRequest.post(GEMINI_API_URL)
                    .header("x-goog-api-key", GEMINI_API_KEY)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(requestBody))
                    .execute();
                    
            if (response.isOk()) {
                String responseBody = response.body();
                System.out.println(responseBody);
                processResponse(responseBody);
            } else {
                System.err.println("请求失败: " + response.getStatus() + " - " + response.body());
            }
            
        } catch (Exception e) {
            System.err.println("发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static JSONObject createRequestBody(String imageBase64) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入提示词:");
        String prompt = scanner.nextLine();
        String jsonTemplate = """
        {
          "contents": [{
            "parts": [
              {"text": "%s"},
              {
                "inline_data": {
                  "mime_type": "image/jpeg",
                  "data": "%s"
                }
              }
            ]
          }]
        }
        """;
//        这里%s会被imageBase64的值替换，生成完整的JSON字符串。
//        然后使用JSONUtil.parseObj()方法将字符串转换为JSONObject对象。
        return JSONUtil.parseObj(String.format(jsonTemplate,prompt,imageBase64));
    }
    
    private static void processResponse(String responseBody) {
        try {
            JSONObject responseJson = JSONUtil.parseObj(responseBody);
            JSONArray candidates = responseJson.getJSONArray("candidates");
            
            if (candidates != null && !candidates.isEmpty()) {
                JSONObject candidate = candidates.getJSONObject(0);

                JSONObject content = candidate.getJSONObject("content");
                if (content != null) {
                    JSONArray parts = content.getJSONArray("parts");
                    if (parts != null && !parts.isEmpty()) {
                        JSONObject part = parts.getJSONObject(1);
                        JSONObject inlineData = part.getJSONObject("inlineData");

                        if (inlineData != null && inlineData.containsKey("data")) {
                            String base64Data = inlineData.getStr("data");
                        byte[] imageData = Base64.decode(base64Data);
                            Path outputPath = Paths.get("gemini-edited-image.png");
                            Files.write(outputPath, imageData);
                            System.out.println("生成的图片已保存到: " + outputPath.toAbsolutePath());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("处理响应时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {

        
        String imagePath = "/Users/robinmacmini/Programming/littleWebsite/DiscordAiBot/images/text.png";
        generateNanoBananaCatImage(imagePath);
    }
}
