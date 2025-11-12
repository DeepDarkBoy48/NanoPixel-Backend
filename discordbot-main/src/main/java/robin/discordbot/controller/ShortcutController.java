package robin.discordbot.controller;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import robin.discordbot.pojo.dto.shortcutDto;
import robin.discordbot.pojo.vo.LLMResult;
import robin.discordbot.pojo.entity.Result;
import robin.discordbot.service.ShortCutService;

@RestController
@RequestMapping("/shortcut")
public class ShortcutController {
    /**
     * Apifox Helper Dify controller
     */
    @Resource
    private ShortCutService shortCutService;
    public static Dotenv dotenv = Dotenv.load();


    @PostMapping("/Unhinged")
    public Result Unhinged(@RequestBody shortcutDto shortcutDto) {
        String text = shortcutDto.getText();
        LLMResult unhinged = shortCutService.Unhinged(text);
        System.out.println(unhinged.toString());
        return Result.success(unhinged);
    }


    @PostMapping("/SetSystemMessage")
    public Result SetSystemMessage(@RequestBody shortcutDto shortcutDto) {
        String text = shortcutDto.getText();
        shortCutService.setSystemMessage(text);
        return Result.success();
    }

    @PostMapping("/AnalyzeAudio")
    public Result AnalyzeAudio(MultipartFile file,String prompt) {
        if( prompt == null  || prompt.isEmpty()){
            prompt = "请详细解析录音内容，并给出一个完美的报告，不需要md格式";
        }
        String result = shortCutService.audioAnalyzer(file, prompt);
        return Result.success(result);
    }
}
