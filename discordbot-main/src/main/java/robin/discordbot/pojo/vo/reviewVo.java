package robin.discordbot.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class reviewVo {
    private Integer id;
    private String content;
    private String userName;
    private LocalDateTime createTime;
}
