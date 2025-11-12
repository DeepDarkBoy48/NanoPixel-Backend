package robin.discordbot.pojo.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class review {
    private Integer id;
    private String content;
    private Integer mediaId;
    private Integer userId;
    private LocalDateTime createTime;
}
