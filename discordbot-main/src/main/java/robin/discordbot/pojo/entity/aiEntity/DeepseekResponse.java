package robin.discordbot.pojo.entity.aiEntity;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class DeepseekResponse {
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    private String system_fingerprint;

    @Data
    public class Choice {
        private Integer index;
        private Message message;
        private Object logprobs;
        private String finish_reason;
    }

    @Data
    public class Message {
        private String role;
        private String content;
    }

    @Data
    public class Usage {
        private Integer prompt_tokens;
        private Integer completion_tokens;
        private Integer total_tokens;
        private Map<String, Integer> prompt_tokens_details;
        private Integer prompt_cache_hit_tokens;
        private Integer prompt_cache_miss_tokens;
    }
}