package robin.discordbot.tutorial.gemini;

import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * 简单的控制台示例：
 * - 使用 Scanner 在控制台选择要执行的方法
 * - 包含对 gemini-2.5-flash-image-preview 的调用示例
 * <p>
 * 依赖：
 * - com.google.genai:google-genai:1.15.0
 * - io.github.cdimascio:dotenv-java (可选，用于从 .env 读取密钥)
 * <p>
 * API Key 读取顺序：
 * - 环境变量：GEMINI_API_KEY 或 GOOGLE_API_KEY
 * - .env 文件：GEMINI_API_KEY 或 GOOGLE_API_KEY
 */
public class GeminiImagePreviewConsole {

    private static final String MODEL_TEXT = "gemini-2.5-flash";
    private static final String MODEL_IMAGE_PREVIEW = "gemini-2.5-flash-image-preview";


    public static void main(String[] args) {
        String apiKey = "your_api_key_here";
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("未找到 API Key。请设置环境变量 GEMINI_API_KEY 或 GOOGLE_API_KEY，或在 .env 中配置。");
            return;
        }

        Client client = Client.builder().apiKey(apiKey).build();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println();
                System.out.println("=== Gemini 控制台演示 ===");
                System.out.println("1) 基础文本生成 (" + MODEL_TEXT + ")");
                System.out.println("2) 图像预览描述 (" + MODEL_IMAGE_PREVIEW + ")");
                System.out.println("3) 流式文本生成 (" + MODEL_TEXT + ")");
                System.out.println("0) 退出");
                System.out.print("选择功能: ");

                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1":
                        basicTextGeneration(client, scanner);
                        break;
                    case "2":
                        imagePreviewDescribe(client, scanner);
                        break;
                    case "3":
                        streamTextGeneration(client, scanner);
                        break;
                    case "0": {
                        System.out.println("已退出。");
                        return;
                    }
                    default:
                        System.out.println("无效选择，请重试。");
                }
            }
        }
    }

    // 选项1：基础文本生成
    private static void basicTextGeneration(Client client, Scanner scanner) {
        System.out.print("请输入你的问题: ");
        String prompt = scanner.nextLine().trim();

        try {
            GenerateContentResponse response = client.models.generateContent(MODEL_TEXT, prompt, null);
            System.out.println("回答: " + response.text());
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
    }

    // 选项2：图像预览（使用 gemini-2.5-flash-image-preview 模型）
    // 提示用户输入图片 URL，将图片与提示词一并发送
    private static void imagePreviewDescribe(Client client, Scanner scanner) {
        client = Client.builder().vertexAI(true).build();

        Image image = Image.fromFile("path/to/your/image");

        // Edit image with a mask.
        EditImageConfig config =
                EditImageConfig.builder()
                        .editMode(EditMode.Known.EDIT_MODE_INPAINT_INSERTION)
                        .numberOfImages(1)
                        .outputMimeType("image/jpeg")
                        .build();

        ArrayList<ReferenceImage> referenceImages = new ArrayList<>();
        RawReferenceImage rawReferenceImage =
                RawReferenceImage.builder().referenceImage(image).referenceId(1).build();
        referenceImages.add(rawReferenceImage);

        MaskReferenceImage maskReferenceImage =
                MaskReferenceImage.builder()
                        .referenceId(2)
                        .config(
                                MaskReferenceConfig.builder()
                                        .maskMode(MaskReferenceMode.Known.MASK_MODE_BACKGROUND)
                                        .maskDilation(0.0f))
                        .build();
        referenceImages.add(maskReferenceImage);

        EditImageResponse response =
                client.models.editImage(
                        "imagen-3.0-capability-001", "Sunlight and clear sky", referenceImages, config);

        response.generatedImages().ifPresent(
                images -> {
                    Image editedImage = images.get(0).image().orElse(null);
                    // Do something with the edited image.
                }
        );

    }

    // 选项3：流式文本生成
    private static void streamTextGeneration(Client client, Scanner scanner) {
        System.out.print("请输入要流式生成的主题: ");
        String prompt = scanner.nextLine().trim();
        try (ResponseStream<GenerateContentResponse> stream =
                     client.models.generateContentStream(MODEL_TEXT, prompt, null)) {
            System.out.print("流式响应: ");
            for (GenerateContentResponse r : stream) {
                System.out.print(r.text());
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
    }
}

