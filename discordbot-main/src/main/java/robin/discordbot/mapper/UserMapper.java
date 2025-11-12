package robin.discordbot.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import org.apache.ibatis.annotations.Update;
import robin.discordbot.pojo.entity.User;

import java.util.List;

@Mapper
public interface UserMapper {
//    @Select("select * from users where id = #{id};")
//    User getUserById(Integer id);

    //根据用户名查询用户
    @Select("select * from user where username=#{username}")
    User findByUserName(String username);

    //添加
    @Insert("insert into user(username,password,create_time,update_time)" +
            " values(#{username},#{password},now(),now())")
    void add(String username, String password);

    @Update("update user set nickname=#{nickname},email=#{email},update_time=#{updateTime},credit = #{credit} where id=#{id}")
    void update(User user);

    @Update("update user set user_pic=#{avatarUrl},update_time=now() where id=#{id}")
    void updateAvatar(String avatarUrl,Integer id);

    @Update("update user set password=#{md5String},update_time=now() where id=#{id}")
    void updatePwd(String md5String, Integer id);

    @Select("select * from user where id=#{id}")
    User getById(int id);

    @Update("update user set credit=10")
    void resetCredit();


    List<User> getUserByIds(List<Integer> ids);
}
