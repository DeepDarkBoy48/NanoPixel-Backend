package robin.discordbot.pojo.entity.aiEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class aiSearchEntity {
    private String query;
    private List<String> images;
    private List<aiSearchResultEntity> results;
    private String responseTime;
}
