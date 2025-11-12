package robin.discordbot.service;

import robin.discordbot.pojo.dto.SavedPromptDto;
import robin.discordbot.pojo.vo.SavedPromptVo;

import java.util.List;

public interface SavedPromptService {
    SavedPromptVo create(SavedPromptDto dto);

    List<SavedPromptVo> list(Integer categoryId);

    SavedPromptVo detail(Integer id);

    void update(SavedPromptDto dto);

    void delete(Integer id);
}
