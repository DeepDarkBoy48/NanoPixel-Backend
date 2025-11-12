package robin.discordbot;

import com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.lang.reflect.Method;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * Google Gen AI Java SDK 示例集合
 * 基于Google Gen AI Java SDK 1.15.0版本
 */
public class gemini {
    private static final Dotenv dotenv = Dotenv.load();
    // API密钥 - 在生产环境中应该使用环境变量
    private static final String API_KEY = dotenv.get("geminiTier1Token");



    public static void main(String[] args) {
        System.out.println("\n=== Google Gen AI Java SDK 示例 ===\n");
        System.out.println("输入对应数字运行单个示例，或输入 a 运行全部，输入 0 退出\n");

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                System.out.print("请选择: ");
                String input = scanner.nextLine().trim();

                switch (input) {
                    case "1":
                        basicTextGeneration();
                        break;
                    case "2":
                        streamContentGeneration();
                        break;
                    case "3":
                        asyncContentGeneration();
                        break;
                    case "4":
                        contentGenerationWithConfig();
                        break;
                    case "5":
                        jsonSchemaResponse();
                        break;
                    case "6":
                        countTokensExample();
                        break;
                    case "7":
                        embedContentExample();
                        break;
                    case "8":
                        automaticFunctionCalling();
                        break;
                    case "9":
                        generateImagesExample();
                        break;
                    case "a":
                    case "A":
                        runAllExamples();
                        break;
                    case "0":
                        running = false;
                        System.out.println("已退出。");
                        break;
                    default:
                        System.out.println("无效选择，请重试。");
                }
                if (running) {
                    System.out.println("\n—— 操作完成 ——\n");
                }
            }
        }
    }

    private static void printMenu() {
        System.out.println("菜单:");
        System.out.println(" 1) 基础文本生成");
        System.out.println(" 2) 流式内容生成");
        System.out.println(" 3) 异步内容生成");
        System.out.println(" 4) 带配置的内容生成");
        System.out.println(" 5) JSON格式响应");
        System.out.println(" 6) Token计数");
        System.out.println(" 7) 文本嵌入");
        System.out.println(" 8) 自动函数调用");
        System.out.println(" 9) 图像生成");
        System.out.println(" a) 依次运行全部示例");
        System.out.println(" 0) 退出");
    }

    private static void runAllExamples() {
        // 1. 基础文本生成
        basicTextGeneration();

        // 2. 流式内容生成
        streamContentGeneration();

        // 3. 异步内容生成
        asyncContentGeneration();

        // 4. 带配置的内容生成
        contentGenerationWithConfig();

        // 5. JSON格式响应
        jsonSchemaResponse();

        // 6. Token计数
        countTokensExample();

        // 7. 文本嵌入
        embedContentExample();

        // 8. 自动函数调用
        automaticFunctionCalling();

        // 9. 图像生成
        generateImagesExample();

        System.out.println("\n=== 所有示例执行完成 ===");
    }

    /**
     * 示例1: 基础文本生成
     */
    public static void basicTextGeneration() {
        System.out.println("1. 基础文本生成:");
        try {
            Client client = Client.builder().apiKey(API_KEY).build();
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", "请用中文介绍一下Java编程语言", null);
            System.out.println("回答: " + response.text());
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * 示例2: 流式内容生成
     */
    public static void streamContentGeneration() {
        System.out.println("2. 流式内容生成:");
        try {
            Client client = Client.builder().apiKey(API_KEY).build();
            ResponseStream<GenerateContentResponse> responseStream = 
                client.models.generateContentStream("gemini-2.5-flash", "请写一个关于人工智能的短故事", null);

            System.out.print("流式响应: ");
            for (GenerateContentResponse res : responseStream) {
                System.out.print(res.text());
            }
            responseStream.close();
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
        System.out.println("\n");
    }

    /**
     * 示例3: 异步内容生成
     */
    public static void asyncContentGeneration() {
        System.out.println("3. 异步内容生成:");
        try {
            Client client = Client.builder().apiKey(API_KEY).build();
            CompletableFuture<GenerateContentResponse> responseFuture = 
                client.async.models.generateContent("gemini-2.5-flash", "介绍一下Google AI Studio", null);

            responseFuture
                .thenAccept(response -> System.out.println("异步响应: " + response.text()))
                .join();
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * 示例4: 带配置的内容生成（安全设置、系统指令等）
     */
    public static void contentGenerationWithConfig() {
        System.out.println("4. 带配置的内容生成:");
        try {
            Client client = Client.builder().apiKey(API_KEY).build();

            // 设置安全配置
            ImmutableList<SafetySetting> safetySettings = 
                ImmutableList.of(
                    SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_HATE_SPEECH)
                        .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
                        .build(),
                    SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_DANGEROUS_CONTENT)
                        .threshold(HarmBlockThreshold.Known.BLOCK_LOW_AND_ABOVE)
                        .build());

            // 设置系统指令
            Content systemInstruction = Content.fromParts(Part.fromText("你是一个有用的编程助手"));

            GenerateContentConfig config = 
                GenerateContentConfig.builder()
                    .candidateCount(1)
                    .maxOutputTokens(1024)
                    .safetySettings(safetySettings)
                    .systemInstruction(systemInstruction)
                    .build();

            GenerateContentResponse response = 
                client.models.generateContent("gemini-2.5-flash", "请帮我解释什么是面向对象编程", config);

            System.out.println("配置化响应: " + response.text());
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * 示例5: JSON格式响应
     */
    public static void jsonSchemaResponse() {
        System.out.println("5. JSON格式响应:");
        try {
            Client client = Client.builder().apiKey(API_KEY).build();

            GenerateContentConfig config = 
                GenerateContentConfig.builder()
                    .responseMimeType("application/json")
                    .candidateCount(1)
                    .build();

            GenerateContentResponse response = 
                client.models.generateContent("gemini-2.5-flash", "请介绍Java编程语言的特性", config);

            System.out.println("JSON响应: " + response.text());
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * 示例6: Token计数
     */
    public static void countTokensExample() {
        System.out.println("6. Token计数:");
        try {
            Client client = Client.builder().apiKey(API_KEY).build();
            CountTokensResponse response = 
                client.models.countTokens("gemini-2.5-flash", "这是一个用于测试token计数的文本", null);

            System.out.println("Token计数响应: " + response);
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * 示例7: 文本嵌入
     */
    public static void embedContentExample() {
        System.out.println("7. 文本嵌入:");
        try {
            Client client = Client.builder().apiKey(API_KEY).build();
            EmbedContentResponse response = 
                client.models.embedContent("gemini-embedding-001", "什么是机器学习？", null);

            System.out.println("嵌入响应: " + response);
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * 示例8: 自动函数调用
     * 需要添加Maven编译器插件支持-parameters参数
     */
    public static void automaticFunctionCalling() {
        System.out.println("8. 自动函数调用:");
        try {
            Client client = Client.builder().apiKey(API_KEY).build();

            // 反射获取方法
            Method method = gemini.class.getMethod("getCurrentWeather", String.class, String.class);

            GenerateContentConfig config = 
                GenerateContentConfig.builder()
                    .tools(Tool.builder().functions(method))
                    .build();

            GenerateContentResponse response = 
                client.models.generateContent(
                    "gemini-2.5-flash", 
                    "北京的天气怎么样？", 
                    config);

            System.out.println("函数调用响应: " + response.text());
            if (response.automaticFunctionCallingHistory().isPresent()) {
                System.out.println("函数调用历史: " + response.automaticFunctionCallingHistory().get());
            }
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * 示例9: 图像生成 (需要相应的模型支持)
     */
    public static void generateImagesExample() {
        System.out.println("9. 图像生成:");
        try {
            Client client = Client.builder().apiKey(API_KEY).build();

            GenerateImagesConfig config = 
                GenerateImagesConfig.builder()
                    .numberOfImages(1)
                    .outputMimeType("image/jpeg")
                    .includeSafetyAttributes(true)
                    .build();

            GenerateImagesResponse response = 
                client.models.generateImages(
                    "imagen-3.0-generate-002", "一只可爱的猫咪在花园里玩耍", config);

            response.generatedImages().ifPresent(
                images -> {
                    System.out.println("成功生成 " + images.size() + " 张图像");
                    // 在实际应用中，您可以保存或处理生成的图像
                }
            );
        } catch (Exception e) {
            System.out.println("图像生成错误 (可能需要特定的API权限): " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * 获取天气的示例函数（用于自动函数调用）
     */
    public static String getCurrentWeather(String location, String unit) {
        // 这是一个示例函数，实际应用中应该调用真实的天气API
        return location + "的天气很好，温度适宜，单位：" + unit;
    }
}
