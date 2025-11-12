package robin.discordbot.service;


import robin.discordbot.pojo.entity.Article;
import robin.discordbot.pojo.entity.PageBean;

public interface ArticleService {
    //新增文章
    void add(Article article);

    //条件分页列表查询
    PageBean<Article> list(Integer pageNum, Integer pageSize, Integer categoryId, String state);

    void update(Article article);

    void delete(Long id);
}
