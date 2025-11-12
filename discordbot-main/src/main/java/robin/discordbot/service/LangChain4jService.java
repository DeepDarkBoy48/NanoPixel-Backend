package robin.discordbot.service;

import robin.discordbot.pojo.entity.aiEntity.AiMessageFormat;
import robin.discordbot.pojo.entity.aiEntity.aiSearchFinalEntity;

public interface LangChain4jService {


//    String deepdarkaiText(String id, AiMessageFormat aiMessageFormat);
//
//    String deepdarkaiImage(String id,AiMessageFormat aiMessageFormat);
//
//    String deepDarkTreadAiChat(String id,AiMessageFormat aiMessageFormat);
//
//    String embediframe(String id,AiMessageFormat aiMessageFormat);
//
    aiSearchFinalEntity aisearch(String id, AiMessageFormat aiMessageFormat);

    String grok(String message);



    aiSearchFinalEntity aisearchNSFW(String id, AiMessageFormat aiMessageFormat);


}
