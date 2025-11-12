package robin.discordbot.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import robin.discordbot.pojo.entity.aiEntity.aiPrompt;

// 这个mapper是用来操作ai_prompts表的
@Mapper
public interface AiMapper {
    @Select("select * from ai_prompts where is_enable = 1 and category = #{category};")
    aiPrompt getAiByCategory(String category);

    @Select("select * from ai_prompts where category = #{category};")
    List<aiPrompt> getALLAgentByCategory(String category);


    @Delete("delete from ai_prompts where id = #{id};")
    void deleteAiById(Integer id);

    void updateAI(@Param("aiPrompt") aiPrompt aiPrompt, Integer id);

    @Update("UPDATE ai_prompts SET is_enable = 0 where category = #{category};")
    void unenableALL(String category);

    @Insert("insert into ai_prompts(prompt, creator, creation_time,category,model_name,is_enable) value (#{prompt}, #{creator}, #{creationTime},#{category},#{modelName},#{isEnable});")
    void addAi(aiPrompt aiPrompt);

    // --- NEW METHODS for executing arbitrary SQL ---

    /**
     * Executes a given SELECT SQL statement.
     * Warning: Directly executing SQL strings can be vulnerable to SQL injection. Use with caution.
     *
     * @param sql The SELECT statement to execute.
     * @return A list of maps, where each map represents a row with column names as keys.
     */
    List<Map<String, Object>> executeSelect(@Param("sql") String sql);

    /**
     * Executes a given INSERT, UPDATE, or DELETE SQL statement.
     * Warning: Directly executing SQL strings can be vulnerable to SQL injection. Use with caution.
     *
     * @param sql The INSERT, UPDATE, or DELETE statement to execute.
     * @return The number of rows affected by the statement.
     */
    int executeUpdateOrInsertOrDelete(@Param("sql") String sql);

}
