package robin.discordbot.service;

import org.springframework.web.multipart.MultipartFile;
import robin.discordbot.pojo.vo.LLMResult;

public interface ShortCutService {

    LLMResult Unhinged(String text);

    void setSystemMessage(String text);

    String audioAnalyzer(MultipartFile file, String prompt);
}
