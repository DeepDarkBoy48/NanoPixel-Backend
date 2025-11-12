package robin.discordbot.service;

import robin.discordbot.pojo.dto.PromptCategoryDto;
import robin.discordbot.pojo.vo.PromptCategoryVo;

import java.util.List;

public interface PromptCategoryService {
    PromptCategoryVo create(PromptCategoryDto dto);

    List<PromptCategoryVo> list();

    PromptCategoryVo detail(Integer id);

    void update(PromptCategoryDto dto);

    void delete(Integer id);
}
