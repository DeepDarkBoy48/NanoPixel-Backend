package robin.discordbot.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import robin.discordbot.mapper.MainChannelServiceImplTestMapper;
import robin.discordbot.pojo.entity.aiEntity.gemini_api_key_entity;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiFactory {

    @Autowired
    public void setMainChannelServiceImplTestMapper(MainChannelServiceImplTestMapper mainChannelServiceImplTestMapper) {
        GeminiFactory.mainChannelServiceImplTestMapper = mainChannelServiceImplTestMapper;
    }

    private static MainChannelServiceImplTestMapper mainChannelServiceImplTestMapper;

    // 添加成员变量用于存储和轮换 API 密钥
    private static List<String> geminiApiKeys;
    private static int currentKeyIndex = 0;
    private static int keyUsageCount = 0;
    private static final int MAX_KEY_USAGE = 1; // 每个 key 使用的最大次数

    // 使用实例初始化块加载 API 密钥
    @PostConstruct
    public void initializer() {
        geminiApiKeys = new ArrayList<>();
        List<gemini_api_key_entity> geminiApiKeysEntity = mainChannelServiceImplTestMapper.getAllGeminiApiKeys();
        currentKeyIndex = mainChannelServiceImplTestMapper.getGeminiApiKeyByEnable().getId() - 1;
        for (gemini_api_key_entity geminiApiKeyEntity : geminiApiKeysEntity) {
            geminiApiKeys.add(geminiApiKeyEntity.getApiKey());
        }
    }

    public static List<gemini_api_key_entity> updateGeminiApiKey() {
        List<gemini_api_key_entity> allGeminiApiKeys = mainChannelServiceImplTestMapper.getAllGeminiApiKeys();
        currentKeyIndex = mainChannelServiceImplTestMapper.getGeminiApiKeyByEnable().getId() - 1;
        for (gemini_api_key_entity geminiApiKeyEntity : allGeminiApiKeys) {
            geminiApiKeys.add(geminiApiKeyEntity.getApiKey());
        }
        return allGeminiApiKeys;
    }

    // 修改 getGeminiToken 方法以实现轮换逻辑
    public static synchronized String getGeminiToken() { // 添加 synchronized 保证线程安全
        if (geminiApiKeys == null || geminiApiKeys.isEmpty()) {
            // 处理未加载密钥的情况
            System.err.println("错误：Gemini API 密钥列表为空或未初始化。");
            // 根据需要返回 null、抛出异常或返回默认/占位符密钥
            // return "DEFAULT_OR_PLACEHOLDER_KEY";
            throw new IllegalStateException("Gemini API 密钥未加载。");
        }

        // 获取当前 key
        String currentKey = geminiApiKeys.get(currentKeyIndex);
        keyUsageCount++; // 增加使用计数
        System.out.println("使用 API Key 索引: " + currentKeyIndex + ", 使用次数: " + keyUsageCount + "/" + MAX_KEY_USAGE); // 调试日志

        // 检查是否需要切换到下一个 key
        if (keyUsageCount >= MAX_KEY_USAGE) {
            currentKeyIndex = (currentKeyIndex + 1) % geminiApiKeys.size(); // 循环切换索引
            keyUsageCount = 0; // 重置新 key 的使用计数
            mainChannelServiceImplTestMapper.disableALLGeminiApiKey(); // 更新数据库中当前使用的 key

            mainChannelServiceImplTestMapper.enableGeminiApiKeyById(currentKeyIndex + 1); // 启用下一个 key

            System.out.println("切换到下一个 API Key 索引: " + currentKeyIndex); // 调试日志
        }

        return currentKey; // 返回当前使用的 key
        // 你好awdawdsd
    }
}
