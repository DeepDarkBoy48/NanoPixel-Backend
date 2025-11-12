package robin.discordbot.pojo.http.tavily;

import lombok.Data;

import java.util.List;

@Data
public class TavilyResponseVo {
    private String query;
    private String answer;
    private List<Image> images;
    private List<Result> results;

}
