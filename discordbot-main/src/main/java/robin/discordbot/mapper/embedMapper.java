package robin.discordbot.mapper;

import org.apache.ibatis.annotations.*;
import robin.discordbot.pojo.entity.embed;

import java.util.List;

@Mapper
public interface embedMapper {

    @Insert("INSERT INTO embed (user_id, url, create_time, name, is_embed) VALUES (#{userId}, #{url}, #{createTime}, #{name}, #{isEmbed})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer insert(embed embed);

    @Select("SELECT * FROM embed WHERE user_id = #{userId}")
    List<embed> listByUserId(Integer userId);

    @Delete("DELETE FROM embed WHERE id = #{fileId} AND user_id = #{userId}")
    void deleteByFileIdAndUserId(Integer fileId, Integer userId);

    @Select("SELECT id FROM embed WHERE url = #{url}")
    Integer getIdByUrl(String url);

    @Update("UPDATE embed SET is_embed = #{isEmbed} WHERE id = #{fileId}")
    void updateIsEmbed(Integer fileId, int isEmbed);
}
