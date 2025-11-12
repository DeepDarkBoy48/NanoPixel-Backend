package robin.discordbot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import robin.discordbot.mapper.PromptCategoryMapper;
import robin.discordbot.mapper.SavedPromptMapper;
import robin.discordbot.pojo.dto.SavedPromptDto;
import robin.discordbot.pojo.entity.PromptCategory;
import robin.discordbot.pojo.entity.SavedPrompt;
import robin.discordbot.pojo.vo.SavedPromptVo;
import robin.discordbot.service.SavedPromptService;
import robin.discordbot.utils.ThreadLocalUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SavedPromptServiceImpl implements SavedPromptService {

    @Autowired
    private SavedPromptMapper savedPromptMapper;

    @Autowired
    private PromptCategoryMapper promptCategoryMapper;

    @Override
    public SavedPromptVo create(SavedPromptDto dto) {
        Integer userId = currentUserId();
        PromptCategory category = ensureAccessibleCategory(dto.getCategoryId());

        SavedPrompt savedPrompt = new SavedPrompt();
        savedPrompt.setContent(dto.getContent().trim());
        savedPrompt.setCategoryId(category.getId());
        savedPrompt.setUserId(userId);
        savedPrompt.setCreateTime(LocalDateTime.now());
        savedPromptMapper.insert(savedPrompt);
        return toVo(savedPrompt, category.getCategoryName());
    }

    @Override
    public List<SavedPromptVo> list(Integer categoryId) {
        Integer userId = currentUserId();
        if (categoryId != null) {
            ensureAccessibleCategory(categoryId);
        }
        return savedPromptMapper.list(userId, categoryId);
    }

    @Override
    public SavedPromptVo detail(Integer id) {
        SavedPrompt savedPrompt = ensureAccessiblePrompt(id);
        PromptCategory category = promptCategoryMapper.findAccessibleById(savedPrompt.getCategoryId(), currentUserId());
        String categoryName = category == null ? null : category.getCategoryName();
        return toVo(savedPrompt, categoryName);
    }

    @Override
    public void update(SavedPromptDto dto) {
        SavedPrompt existing = ensureOwnedPrompt(dto.getId());
        PromptCategory category = ensureAccessibleCategory(dto.getCategoryId());
        existing.setContent(dto.getContent().trim());
        existing.setCategoryId(category.getId());
        int updated = savedPromptMapper.update(existing);
        if (updated == 0) {
            throw new IllegalArgumentException("更新失败，提示不存在或无权限");
        }
    }

    @Override
    public void delete(Integer id) {
        SavedPrompt savedPrompt = ensureOwnedPrompt(id);
        int deleted = savedPromptMapper.deleteById(savedPrompt.getId(), savedPrompt.getUserId());
        if (deleted == 0) {
            throw new IllegalArgumentException("删除失败，提示不存在或无权限");
        }
    }

    private SavedPrompt ensureOwnedPrompt(Integer id) {
        Integer userId = currentUserId();
        SavedPrompt savedPrompt = savedPromptMapper.findAccessibleById(id, userId);
        if (savedPrompt == null) {
            throw new IllegalArgumentException("提示不存在或无权限");
        }
        if (savedPrompt.getUserId() == 0) {
            throw new IllegalArgumentException("官方提示不可修改或删除");
        }
        if (!savedPrompt.getUserId().equals(userId)) {
            throw new IllegalArgumentException("提示不存在或无权限");
        }
        return savedPrompt;
    }

    private SavedPrompt ensureAccessiblePrompt(Integer id) {
        SavedPrompt savedPrompt = savedPromptMapper.findAccessibleById(id, currentUserId());
        if (savedPrompt == null) {
            throw new IllegalArgumentException("提示不存在或无权限");
        }
        return savedPrompt;
    }

    private PromptCategory ensureAccessibleCategory(Integer categoryId) {
        PromptCategory category = promptCategoryMapper.findAccessibleById(categoryId, currentUserId());
        if (category == null) {
            throw new IllegalArgumentException("分类不存在或无权限");
        }
        return category;
    }

    private SavedPromptVo toVo(SavedPrompt savedPrompt, String categoryName) {
        SavedPromptVo vo = new SavedPromptVo();
        vo.setId(savedPrompt.getId());
        vo.setContent(savedPrompt.getContent());
        vo.setCategoryId(savedPrompt.getCategoryId());
        vo.setCategoryName(categoryName);
        vo.setCreateTime(savedPrompt.getCreateTime());
        return vo;
    }

    private Integer currentUserId() {
        Map<String, Object> map = ThreadLocalUtil.get();
        return (Integer) map.get("id");
    }
}
