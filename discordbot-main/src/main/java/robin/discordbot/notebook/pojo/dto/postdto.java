package robin.discordbot.notebook.pojo.dto;

import cn.hutool.core.date.DateTime;
import lombok.Data;

@Data
public class postdto {
    private String title;
    private String content;
    private Integer categoryId;
}
