package robin.discordbot.pojo.mq;

import lombok.Data;

@Data
public class embedPdf {
    private String url;
    private Integer fileId;
    private Integer userId;
}
