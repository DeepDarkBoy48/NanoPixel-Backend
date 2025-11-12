package robin.discordbot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import robin.discordbot.pojo.dto.SavedPromptDto;
import robin.discordbot.pojo.entity.Result;
import robin.discordbot.pojo.vo.SavedPromptVo;
import robin.discordbot.service.SavedPromptService;

import java.util.List;

@RestController
@RequestMapping("/saved-prompt")
public class SavedPromptController {

    @Autowired
    private SavedPromptService savedPromptService;

    @PostMapping
    public Result<Void> create(@RequestBody @Validated(SavedPromptDto.Create.class) SavedPromptDto dto) {
        SavedPromptVo vo = savedPromptService.create(dto);
        return Result.success("创建prompt成功");
    }

    @GetMapping
    public Result<List<SavedPromptVo>> list(@RequestParam(value = "categoryId", required = false) Integer categoryId) {
        List<SavedPromptVo> list = savedPromptService.list(categoryId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<SavedPromptVo> detail(@PathVariable Integer id) {
        SavedPromptVo vo = savedPromptService.detail(id);
        return Result.success(vo);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id,
                               @RequestBody @Validated(SavedPromptDto.Update.class) SavedPromptDto dto) {
        dto.setId(id);
        savedPromptService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        savedPromptService.delete(id);
        return Result.success("删除prompt成功");
    }
}
