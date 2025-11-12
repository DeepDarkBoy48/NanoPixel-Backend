package robin.discordbot.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class mediaVo {
    private Long id;
    private String mediaurl;
    private String prompt;
    private LocalDateTime createtime;
    private String username;
    private Integer ispublic;
    private String originurl;
    private String model;
    private Integer reviewcount;
}
