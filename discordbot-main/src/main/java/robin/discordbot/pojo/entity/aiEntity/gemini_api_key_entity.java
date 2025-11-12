package robin.discordbot.pojo.entity.aiEntity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class gemini_api_key_entity {
    private Integer id;
    private String apiKey;
    private Integer usageCount;
    private LocalDateTime lastUsedTime;
    private Integer isEnable;
}
