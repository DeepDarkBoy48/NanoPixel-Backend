package robin.discordbot.pojo.entity.discordEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class buttonInfoEntity {
    private List<String> buttonContent;
}
