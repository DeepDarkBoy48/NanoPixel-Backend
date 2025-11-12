package robin.discordbot.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Data;

@Data
public class SavedPromptDto {
    @NotNull(groups = Update.class)
    private Integer id;

    @NotBlank(groups = {Create.class, Update.class})
    @Size(max = 1000, groups = {Create.class, Update.class})
    private String content;

    @NotNull(groups = {Create.class, Update.class})
    private Integer categoryId;

    public interface Create extends Default {
    }

    public interface Update extends Default {
    }
}
