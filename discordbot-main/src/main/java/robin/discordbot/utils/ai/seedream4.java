package robin.discordbot.utils.ai;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.List;

public class seedream4 {


    private static final String SEEDREAM_API_URL = "https://ark.cn-beijing.volces.com/api/v3/images/generations";
    private static final Dotenv dotenv = Dotenv.load();

    public static String process(String prompt, List<String> imageUrls) {

        if (imageUrls == null || imageUrls.isEmpty()) {
            System.err.println("seedream4: imageUrls 不能为空");
            return null;
        }

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", "doubao-seedream-4-0-250828");
        requestBody.set("prompt", prompt == null ? "" : prompt);
        JSONArray urls = new JSONArray();
        for (String u : imageUrls) {
            if (u != null && !u.isBlank()) {
                urls.add(u);
            }
        }
        requestBody.set("image", urls);
        requestBody.set("sequential_image_generation", "auto");
        int maxImages = 1;
        requestBody.set("sequential_image_generation_options", new JSONObject().set("max_images", maxImages));
        requestBody.set("size", "2K");

        // 发送请求
        HttpResponse response = HttpRequest.post(SEEDREAM_API_URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + dotenv.get("seedreamToken"))
                .body(JSONUtil.toJsonStr(requestBody))
                .execute();

        // 处理响应
        if (!response.isOk()) {
            System.err.println("seedream4 请求失败: " + response.getStatus() + " - " + response.body());
            return null;
        }
        // 解析响应
        String body = response.body();
        JSONObject jsonObject = JSONUtil.parseObj(body);
        JSONArray data = jsonObject.getJSONArray("data");
        String firstUrl = data.getJSONObject(0).getStr("url");
        return firstUrl;
    }
}
