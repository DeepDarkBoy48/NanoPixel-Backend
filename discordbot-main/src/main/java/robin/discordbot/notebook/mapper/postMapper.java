package robin.discordbot.notebook.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import robin.discordbot.notebook.pojo.entity.post;
@Mapper
public interface postMapper {

    @Insert("insert into post(title, content, category_id, create_time) values(#{title}, #{content}, #{categoryId}, #{createTime})")
    void insert(post post);
}
