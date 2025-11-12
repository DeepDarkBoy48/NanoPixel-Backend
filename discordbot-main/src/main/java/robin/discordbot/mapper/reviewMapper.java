package robin.discordbot.mapper;

import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import robin.discordbot.pojo.dto.reviewDto;
import robin.discordbot.pojo.entity.review;

import java.util.List;

@Mapper
public interface reviewMapper {
    @Insert("insert into review (media_id, user_id, content,create_time) values(#{mediaId}, #{userId}, #{content}, #{createTime})")
    void add(review review);

    @Select("select * from review where media_id=#{mediaId} order by create_time desc")
    List<review> getReviewListByMediaId(String mediaId);

    @Select("select * from review where user_id=#{id} order by create_time desc")
    List<review> getReviewListByUserId(@NotNull Integer userId);

    @Delete("delete from review where id=#{id}")
    void delete(Integer id);

    @Select("select * from review where id=#{id}")
    review getReviewById(Integer id);
}
