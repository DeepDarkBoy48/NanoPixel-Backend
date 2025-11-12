package robin.discordbot.pojo.entity.aiEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class aiSearchOutputEntity {
    private String report;
    private List<String> buttonInfo;
}
