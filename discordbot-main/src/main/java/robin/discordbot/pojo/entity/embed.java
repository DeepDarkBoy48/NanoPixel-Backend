package robin.discordbot.pojo.entity;

import cn.hutool.core.date.DateTime;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class embed {
    private Integer id;
    private Integer userId;
    private String url;
    private LocalDateTime createTime;
    private String name;
    private Integer isEmbed;
}
