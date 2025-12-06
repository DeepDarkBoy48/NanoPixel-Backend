package robin.discordbot.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubtitleVo {
    private List<SubtitleItem> subtitles;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubtitleItem {
        private String start;
        private String end;
        private String text;
    }
}
