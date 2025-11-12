package robin.discordbot.pojo.http.tavily;

import lombok.Data;

@Data
public class Image {
    private String url;
    private String title;
    private String description;


    @Override
    public String toString() {
        return "Image{" +
                "url='" + url + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}