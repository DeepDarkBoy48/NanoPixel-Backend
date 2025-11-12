package robin.discordbot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import robin.discordbot.mapper.PromptCategoryMapper;
import robin.discordbot.mapper.SavedPromptMapper;
import robin.discordbot.pojo.dto.PromptCategoryDto;
import robin.discordbot.pojo.entity.PromptCategory;
import robin.discordbot.pojo.vo.PromptCategoryVo;
import robin.discordbot.service.PromptCategoryService;
import robin.discordbot.utils.ThreadLocalUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PromptCategoryServiceImpl implements PromptCategoryService {

    @Autowired
    private PromptCategoryMapper promptCategoryMapper;

    @Autowired
    private SavedPromptMapper savedPromptMapper;

    @Override
    public PromptCategoryVo create(PromptCategoryDto dto) {
        PromptCategory category = new PromptCategory();
        category.setCategoryName(dto.getCategoryName().trim());
        category.setUserId(currentUserId());
        category.setCreateTime(LocalDateTime.now());
        promptCategoryMapper.insert(category);
        return toVo(category, 0L);
    }

    @Override
    public List<PromptCategoryVo> list() {
        Integer userId = currentUserId();
        return promptCategoryMapper.listAccessible(userId)
                .stream()
                .map(category -> toVo(category, promptCategoryMapper.countPromptsForUser(category.getId(), userId)))
                .collect(Collectors.toList());
    }

    @Override
    public PromptCategoryVo detail(Integer id) {
        Integer userId = currentUserId();
        PromptCategory category = ensureAccessibleCategory(id, userId);
        Long promptCount = promptCategoryMapper.countPromptsForUser(id, userId);
        return toVo(category, promptCount);
    }

    @Override
    public void update(PromptCategoryDto dto) {
        PromptCategory category = ensureOwnedCategory(dto.getId());
        category.setCategoryName(dto.getCategoryName().trim());
        int updated = promptCategoryMapper.update(category);
        if (updated == 0) {
            throw new IllegalArgumentException("更新失败，分类不存在或无权限");
        }
    }

    @Override
    public void delete(Integer id) {
        PromptCategory category = ensureOwnedCategory(id);
        savedPromptMapper.deleteByCategory(id, category.getUserId());
        int deleted = promptCategoryMapper.deleteById(id, category.getUserId());
        if (deleted == 0) {
            throw new IllegalArgumentException("删除失败，分类不存在或无权限");
        }
    }

    // 确保分类存在且属于当前用户
    private PromptCategory ensureOwnedCategory(Integer id) {
        Integer userId = currentUserId();
        PromptCategory category = ensureAccessibleCategory(id, userId);
        if (category.getUserId() == 0) {
            throw new IllegalArgumentException("官方分类不可修改或删除");
        }
        if (!category.getUserId().equals(userId)) {
            throw new IllegalArgumentException("分类不存在或无权限");
        }
        return category;
    }

    private PromptCategory ensureAccessibleCategory(Integer id) {
        return ensureAccessibleCategory(id, currentUserId());
    }

    private PromptCategory ensureAccessibleCategory(Integer id, Integer userId) {
        PromptCategory category = promptCategoryMapper.findAccessibleById(id, userId);
        if (category == null) {
            throw new IllegalArgumentException("分类不存在或无权限");
        }
        return category;
    }

    // 转换为VO，包含关联的prompt数量
    private PromptCategoryVo toVo(PromptCategory category, Long promptCount) {
        PromptCategoryVo vo = new PromptCategoryVo();
        vo.setId(category.getId());
        vo.setCategoryName(category.getCategoryName());
        vo.setCreateTime(category.getCreateTime());
        vo.setPromptCount(promptCount == null ? 0L : promptCount);
        return vo;
    }

    // 获取当前登录用户的ID
    private Integer currentUserId() {
        Map<String, Object> map = ThreadLocalUtil.get();
        return (Integer) map.get("id");
    }
}
