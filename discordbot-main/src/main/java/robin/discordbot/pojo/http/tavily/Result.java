package robin.discordbot.pojo.http.tavily;

import lombok.Data;

@Data
public class Result {
    private String url;
    private String title;
    private String content;
    private double score;
    // JSON中的 "raw_content" 会被映射到这个驼峰命名的字段
    private String rawContent;
    private String favicon;


    @Override
    public String toString() {
        return "Result{" +
                "url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", score=" + score +
                '}';
    }
}