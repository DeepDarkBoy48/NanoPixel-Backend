package robin.discordbot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import robin.discordbot.pojo.dto.PromptCategoryDto;
import robin.discordbot.pojo.entity.Result;
import robin.discordbot.pojo.vo.PromptCategoryVo;
import robin.discordbot.service.PromptCategoryService;

import java.util.List;

@RestController
@RequestMapping("/prompt-category")
public class PromptCategoryController {

    @Autowired
    private PromptCategoryService promptCategoryService;

    @PostMapping
    public Result<Void> create(@RequestBody @Validated(PromptCategoryDto.Create.class) PromptCategoryDto dto) {
        PromptCategoryVo vo = promptCategoryService.create(dto);
        return Result.success("创建分类成功");
    }

    @GetMapping()
    public Result<List<PromptCategoryVo>> list() {
        List<PromptCategoryVo> list = promptCategoryService.list();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<PromptCategoryVo> detail(@PathVariable Integer id) {
        PromptCategoryVo vo = promptCategoryService.detail(id);
        return Result.success(vo);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id,
                               @RequestBody @Validated(PromptCategoryDto.Update.class) PromptCategoryDto dto) {
        dto.setId(id);
        promptCategoryService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        promptCategoryService.delete(id);
        return Result.success("删除分类成功");
    }
}
