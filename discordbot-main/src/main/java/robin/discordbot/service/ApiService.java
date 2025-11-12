package robin.discordbot.service;

import robin.discordbot.pojo.vo.LLMResult;

public interface ApiService {
//    User getUserById(Integer id);

    LLMResult getTranslate(String text);

    void setSystemprompt(String systemprompt);
}
