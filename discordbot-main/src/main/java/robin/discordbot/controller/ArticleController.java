package robin.discordbot.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import robin.discordbot.pojo.entity.Article;
import robin.discordbot.pojo.entity.Result;
import robin.discordbot.service.ArticleService;
import robin.discordbot.pojo.entity.PageBean;


@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @PostMapping
    public Result add(@RequestBody @Validated Article article) {
        articleService.add(article);
        return Result.success();
    }

   @GetMapping
   public Result<PageBean<Article>> list(
           Integer pageNum,
           Integer pageSize,
           @RequestParam(required = false) Integer categoryId,
           @RequestParam(required = false) String state
   ) {
      PageBean<Article> pb =  articleService.list(pageNum,pageSize,categoryId,state);
      return Result.success(pb);
   }

   @PutMapping
    public Result update(@RequestBody Article article) {
        articleService.update(article);
        return Result.success();
    }

   @DeleteMapping
    public Result delete(@RequestParam Long id) {
        articleService.delete(id);
        return Result.success();
   }
}
