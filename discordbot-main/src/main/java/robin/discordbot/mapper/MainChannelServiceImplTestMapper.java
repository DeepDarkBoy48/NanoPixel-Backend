package robin.discordbot.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import robin.discordbot.pojo.entity.aiEntity.gemini_api_key_entity;

@Mapper
public interface  MainChannelServiceImplTestMapper {
    @Insert("insert into gemini_api_keys(id, api_key, usage_count, last_used_time) values (#{id}, #{apiKey}, #{usageCount}, #{lastUsedTime});")
    void insertGeminiApiKey(gemini_api_key_entity apiKey);

    
    @Update("update gemini_api_keys set last_used_time = #{lastUsedTime}, usage_count = usage_count - 1 where api_key = #{apiKey}")
    void updateGeminiApiKeyUsageCount(String apiKey, LocalDateTime lastUsedTime);

    @Update("update gemini_api_keys set usage_count = 25 ")
    void refreshGeminiApiKeyUsageCount();

    @Update("update gemini_api_keys set is_enable = 0")
    void disableALLGeminiApiKey();

    @Update("update gemini_api_keys set is_enable = 1 where id = #{id}")
    void enableGeminiApiKeyById(Integer id);


    @Select("select * from gemini_api_keys where id in #{ids}")
    List<gemini_api_key_entity> getGeminiApiKeysByIds(List<Integer> ids);

    @Select("select * from gemini_api_keys where id = #{id}")
    gemini_api_key_entity getGeminiApiKeyById(Integer id);

    @Select("select * from gemini_api_keys")
    List<gemini_api_key_entity> getAllGeminiApiKeys();

    @Select(("select * from gemini_api_keys where is_enable = 1;"))
    gemini_api_key_entity getGeminiApiKeyByEnable();



}
