package robin.discordbot.notebook.pojo.entity;

import cn.hutool.core.date.DateTime;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class post {
    private Long id;
    private String title;
    private String content;
    private Integer categoryId;
    private DateTime createTime;
}
