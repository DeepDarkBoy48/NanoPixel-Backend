package robin.discordbot.mapper;

import org.apache.ibatis.annotations.*;
import robin.discordbot.pojo.entity.SavedPrompt;
import robin.discordbot.pojo.vo.SavedPromptVo;

import java.util.List;

@Mapper
public interface SavedPromptMapper {

    @Insert("insert into savedprompt(content, categoryid, createtime, userid) " +
            "values(#{content}, #{categoryId}, #{createTime}, #{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SavedPrompt savedPrompt);

    @Update("update savedprompt set content = #{content}, categoryid = #{categoryId} " +
            "where id = #{id} and userid = #{userId}")
    int update(SavedPrompt savedPrompt);

    @Delete("delete from savedprompt where id = #{id} and userid = #{userId}")
    int deleteById(@Param("id") Integer id, @Param("userId") Integer userId);

    @Delete("delete from savedprompt where categoryid = #{categoryId} and userid = #{userId}")
    int deleteByCategory(@Param("categoryId") Integer categoryId, @Param("userId") Integer userId);

    @Select("select id, content, categoryid as categoryId, userid as userId, createtime as createTime " +
            "from savedprompt where id = #{id} and userid = #{userId}")
    SavedPrompt findById(@Param("id") Integer id, @Param("userId") Integer userId);

    @Select("select id, content, categoryid as categoryId, userid as userId, createtime as createTime " +
            "from savedprompt where id = #{id} and (userid = #{userId} or userid = 0)")
    SavedPrompt findAccessibleById(@Param("id") Integer id, @Param("userId") Integer userId);

    @Select({"<script>",
            "select sp.id, sp.content, sp.categoryid as categoryId, pc.category_name as categoryName,",
            "sp.createtime as createTime ",
            "from savedprompt sp ",
            "left join prompt_category pc on sp.categoryid = pc.id ",
            "where (sp.userid = #{userId} or sp.userid = 0) ",
            "<if test='categoryId != null'>",
            "and sp.categoryid = #{categoryId} ",
            "</if>",
            "and (pc.userid = #{userId} or pc.userid = 0) ",
            "order by case when sp.userid = 0 then 0 else 1 end, sp.createtime desc",
            "</script>"})
    List<SavedPromptVo> list(@Param("userId") Integer userId, @Param("categoryId") Integer categoryId);
}
