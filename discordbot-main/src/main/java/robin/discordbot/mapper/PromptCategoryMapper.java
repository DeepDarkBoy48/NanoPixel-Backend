package robin.discordbot.mapper;

import org.apache.ibatis.annotations.*;
import robin.discordbot.pojo.entity.PromptCategory;

import java.util.List;

@Mapper
public interface PromptCategoryMapper {

    @Insert("insert into prompt_category(category_name, userid, create_time) " +
            "values(#{categoryName}, #{userId}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PromptCategory promptCategory);

    @Update("update prompt_category set category_name = #{categoryName} " +
            "where id = #{id} and userid = #{userId}")
    int update(PromptCategory promptCategory);

    @Delete("delete from prompt_category where id = #{id} and userid = #{userId}")
    int deleteById(@Param("id") Integer id, @Param("userId") Integer userId);

    @Select("select id, category_name as categoryName, userid as userId, create_time as createTime " +
            "from prompt_category where id = #{id} and userid = #{userId}")
    PromptCategory findById(@Param("id") Integer id, @Param("userId") Integer userId);

    @Select("select id, category_name as categoryName, userid as userId, create_time as createTime " +
            "from prompt_category where id = #{id} and (userid = #{userId} or userid = 0)")
    PromptCategory findAccessibleById(@Param("id") Integer id, @Param("userId") Integer userId);

    @Select("select id, category_name as categoryName, userid as userId, create_time as createTime " +
            "from prompt_category where userid = #{userId} order by create_time desc")
    List<PromptCategory> listByUser(@Param("userId") Integer userId);

    @Select("select id, category_name as categoryName, userid as userId, create_time as createTime " +
            "from prompt_category where userid in (0, #{userId}) " +
            "order by case when userid = 0 then 0 else 1 end, create_time desc")
    List<PromptCategory> listAccessible(@Param("userId") Integer userId);

    @Select("select count(1) from savedprompt where categoryid = #{categoryId} and (userid = 0 or userid = #{userId})")
    Long countPromptsForUser(@Param("categoryId") Integer categoryId, @Param("userId") Integer userId);
}
