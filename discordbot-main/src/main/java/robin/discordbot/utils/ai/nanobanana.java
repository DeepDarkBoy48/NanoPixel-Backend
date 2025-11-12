package robin.discordbot.utils.ai;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.web.multipart.MultipartFile;

public class nanobanana {

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent";
    private static final Dotenv dotenv = Dotenv.load();

    public static byte[] process(MultipartFile file, String prompt) {
        return generateNanoBananaCatImage(file, prompt);
    }

    public static byte[] generateNanoBananaCatImage(MultipartFile imageFile, String prompt) {
        try {
            String imageBase64 = Base64.encode(imageFile.getBytes());
            String contentType = imageFile.getContentType() != null ? imageFile.getContentType() : "image/png";

            JSONObject requestBody = createRequestBody(imageBase64, prompt, contentType);

            HttpResponse response = HttpRequest.post(GEMINI_API_URL)
                    .header("x-goog-api-key", dotenv.get("geminiTier1Token"))
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(requestBody))
                    .execute();

            if (response.isOk()) {
                String responseBody = response.body();
                return processResponse(responseBody);
            } else {
                System.err.println("请求失败: " + response.getStatus() + " - " + response.body());
                return null;
            }

        } catch (Exception e) {
            System.err.println("发生错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static JSONObject createRequestBody(String imageBase64, String prompt, String mimeType) {
        try {
            JSONObject inlineData = new JSONObject()
                    .set("mimeType", mimeType)
                    .set("data", imageBase64);

            JSONArray parts = new JSONArray()
                    .put(new JSONObject().set("text", prompt == null ? "" : prompt))
                    .put(new JSONObject().set("inlineData", inlineData));

            JSONObject content = new JSONObject().set("parts", parts);
            JSONArray contents = new JSONArray().put(content);

            return new JSONObject().set("contents", contents);
        } catch (Exception e) {
            System.err.println("构建请求体时发生错误: " + e.getMessage());
            throw e;
        }
    }

    private static byte[] processResponse(String responseBody) {
        try {
            JSONObject responseJson = JSONUtil.parseObj(responseBody);
            JSONArray candidates = responseJson.getJSONArray("candidates");

            if (candidates != null && !candidates.isEmpty()) {
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject content = candidate.getJSONObject("content");

                if (content != null) {
                    JSONArray parts = content.getJSONArray("parts");
                    if (parts != null && !parts.isEmpty()) {

                        // 检查所有parts，寻找图片数据
                        for (int i = 0; i < parts.size(); i++) {
                            JSONObject part = parts.getJSONObject(i);

                            // 如果包含inlineData，说明是图片数据
                            if (part.containsKey("inlineData")) {
                                JSONObject inlineData = part.getJSONObject("inlineData");
                                if (inlineData != null && inlineData.containsKey("data")) {
                                    String base64Data = inlineData.getStr("data");
                                    return Base64.decode(base64Data);
                                }
                            }

                            // 如果只有text，说明Gemini返回的是文本而不是图片
                            if (part.containsKey("text") && !part.containsKey("inlineData")) {
                                String textResponse = part.getStr("text");
                                System.err.println("Gemini返回文本而非图片: " + textResponse);
                            }
                        }

                        // 如果没有找到图片数据
                        System.err.println("响应中未找到图片数据，可能需要调整prompt或模型参数");
                        System.err.println("完整响应: " + responseBody);
                    }
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("处理响应时发生错误: " + e.getMessage());
            System.err.println("响应内容: " + responseBody);
            e.printStackTrace();
            return null;
        }
    }

}
