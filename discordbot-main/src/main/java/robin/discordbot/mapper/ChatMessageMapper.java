package robin.discordbot.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import robin.discordbot.pojo.entity.ChatMessage;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    @Insert("INSERT INTO chat_messages (sender_id, sender_nickname, message_content, room_id, created_at) " +
            "VALUES (#{senderId}, #{senderNickname}, #{messageContent}, #{roomId}, NOW())")
    void insert(ChatMessage message);

    @Select("SELECT * FROM chat_messages WHERE room_id = #{roomId} ORDER BY created_at DESC LIMIT #{limit}")
    List<ChatMessage> findRecentMessages(String roomId, int limit);
}
