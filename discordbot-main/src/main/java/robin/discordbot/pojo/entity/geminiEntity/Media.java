package robin.discordbot.pojo.entity.geminiEntity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;


@TableName("media")
@Data
public class Media {
    //数据库自增ID
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String mediaurl;
    private String prompt;
    private LocalDateTime createtime;
    private Integer ispublic;
    private String originurl;
    private String model;
    private Integer reviewcount;
}
