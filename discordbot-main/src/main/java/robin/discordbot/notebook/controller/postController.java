package robin.discordbot.notebook.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import robin.discordbot.notebook.pojo.dto.postdto;
import robin.discordbot.pojo.entity.Result;
import robin.discordbot.notebook.service.postService;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class postController {

    private final postService notebookService;

    @RequestMapping("/add")
    public Result addPost(@RequestBody postdto postdto) {
        // 从请求参数中获取 notebookName
        try {
            notebookService.addNotebook(postdto);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加失败");
        }
        return Result.success("添加成功");
    }
}
