package robin.discordbot.controller;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;
import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import robin.discordbot.pojo.vo.LLMResult;
import robin.discordbot.pojo.entity.Result;
import robin.discordbot.service.ApiService;

@RestController
@RequestMapping("/api")
public class ApiController {

    /**
     * Apifox Helper Dify controller
     */
    @Resource
    private ApiService apiService;
    public static Dotenv dotenv = Dotenv.load();
    public static String getQwenToken() {
        return dotenv.get("qwen");
    }

//    @GetMapping("/aichat/{id}")
//    public String hello(@PathVariable("id") Integer id){
//        User user = apiService.getUserById(id);
//        String userInfo = user.toString();
//        return userInfo;
//    }

    @GetMapping("/translate")
    public Result<LLMResult> translate(String text){
        LLMResult translate = apiService.getTranslate(text);
        return Result.success(translate);
    }

//    @PostMapping("/setsystemprompt")
//    public Result systemprompt(String systemprompt){
//        System.out.println(systemprompt);
//        try {
//            apiService.setSystemprompt(systemprompt);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        return Result.success();
//    }


    @GetMapping("/test")
    public Result<String> test(){
        ChatModel qwenChatModel = QwenChatModel.builder()
                .apiKey(getQwenToken())
                .modelName("deepseek-r1")
                .build();
        String generate = qwenChatModel.chat("你好，你叫什么名字？");
        return Result.success(generate);
    }

    @PostMapping("/articles")
    public String article(@RequestParam String article){
        System.out.println(article);
        return "success";
    }
}
