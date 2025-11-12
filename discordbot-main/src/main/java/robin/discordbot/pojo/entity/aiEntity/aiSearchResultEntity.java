package robin.discordbot.pojo.entity.aiEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class aiSearchResultEntity {
    private String title;
    private String url;
    private String content;
    private double score;
    private String raw_content;

    @Override
    public String toString() {
        return "Title: " + title + "\n" +
                "Url: " + url + "\n"+
                "Content: "+content;
    }
}
