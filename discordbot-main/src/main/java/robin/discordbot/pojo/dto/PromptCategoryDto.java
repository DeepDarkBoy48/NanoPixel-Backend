package robin.discordbot.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Data;

@Data
public class PromptCategoryDto {
    @NotNull(groups = Update.class)
    private Integer id;

    @NotBlank(groups = {Create.class, Update.class})
    private String categoryName;

    public interface Create extends Default {
    }

    public interface Update extends Default {
    }
}
