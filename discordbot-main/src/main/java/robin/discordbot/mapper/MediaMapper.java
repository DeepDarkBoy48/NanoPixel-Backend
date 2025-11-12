package robin.discordbot.mapper;


import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import robin.discordbot.pojo.entity.geminiEntity.Media;

import java.util.List;

@Mapper
public interface MediaMapper {
    @Insert("INSERT INTO media (username, mediaurl, createtime, prompt,ispublic,originurl,model,review_count) VALUES (#{username}, #{mediaurl}, #{createtime}, #{prompt},#{ispublic},#{originurl},#{model},#{reviewcount})")
    void insertMedia(Media media);

    @Select("SELECT * FROM media WHERE username = #{username} order by createtime desc")
    List<Media> selectMediaByUsername(String username);

    @Select("SELECT * FROM media where ispublic = 1 order by ${sortBy} desc")
    List<Media> selectAllMedia(@Param("sortBy") String sortBy);

    @Update("UPDATE media SET ispublic = #{isPublic} WHERE id = #{id} AND username = #{username}")
    int updateIsPublicByIdAndUsername(@Param("id") Long id,
                                      @Param("username") String username,
                                      @Param("isPublic") int isPublic);

    @Select("SELECT originurl FROM media WHERE id = #{mediaId}")
    String selectOriginUrlById(Long mediaId);

    @Select("SELECT * FROM media WHERE id = #{mediaId}")
    Media selectSingelMediaById(Long mediaId);

    @Update("update media set review_count=review_count+#{i} where id=#{mediaid}")
    void updateReviewCount(Integer mediaid, int i);

    @Update("update media set review_count = review_count + 1 where id = 1")
    void updateCommitId1();
}
