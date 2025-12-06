package robin.discordbot.controller;

import cn.hutool.http.HttpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import robin.discordbot.pojo.entity.PageBean;
import robin.discordbot.pojo.entity.Result;
import robin.discordbot.pojo.entity.User;
import robin.discordbot.pojo.entity.geminiEntity.Media;
import robin.discordbot.pojo.vo.mediaVo;
import robin.discordbot.record.prompt;
import robin.discordbot.service.MediaService;
import robin.discordbot.service.CloudflareR2Service;
import robin.discordbot.service.UserService;
import robin.discordbot.utils.ThreadLocalUtil;
import robin.discordbot.utils.ai.geminiTool;
import robin.discordbot.utils.ai.nanobanana;
import robin.discordbot.utils.ai.seedream4;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {
    private final CloudflareR2Service r2Service;
    private final MediaService mediaService;
    private final UserService UserService;

    @PostMapping("/imageEdit")
    public Result<String> imageEdit(@RequestPart(required = false) MultipartFile file,
            @RequestParam(required = false) MultipartFile file2,
            @RequestParam String prompt,
            @RequestParam(required = false) String model)
            throws NoSuchMethodException {

        // 线程本地变量获取用户名
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");

        // 查看用户积分
        User user = UserService.findByUserName(username);
        if (user.getCredit() < 1) {
            return Result.error("用户积分不足");
        }

        // 设置文件夹
        String folder = "Ai.GeminiEdit";
        // 上传原始图片
        String originUrl = null;
        String originUrl2 = null;
        if (file != null) {
            originUrl = r2Service.uploadFile(file, folder);
        }
        if (file2 != null) {
            originUrl2 = r2Service.uploadFile(file2, folder);
        }
        // 设置文件名
        String fileName = prompt + ".png";
        // 设置文件类型
        String contentType = "image/png";
        // 调用nanobanana处理图片，返回byte[]
        byte[] processedImageBytes = null;
        // 调用seedream4处理图片，返回url
        String processurl = null;
        // 设置共同的url
        String url = null;

        if (model == null || model.equals("nanobanana") || model.equals("nanobananaWithChinese")) {

            // 调用nanobanana处理图片，返回byte[]
            // 使用gemini subagent 翻译 prompt
            String enhancedPrompt = "翻译以下内容为英文： 请基于提供的图片，直接生成修改后的图片，不要提供文字描述，按照以下要求进行图片编辑和生成：" + prompt;
            geminiTool.geminiToolBot geminiToolBot = geminiTool.geminiToolBot();
            String chat = geminiToolBot.chat(enhancedPrompt);
            System.out.println(chat);
            processedImageBytes = nanobanana.process(file, chat);

            if (processedImageBytes == null) {
                return Result.error("图片处理失败(nanobanana)");
            }
            url = r2Service.uploadBytes(processedImageBytes, fileName, contentType, folder);
            // 检查是否需要翻译为中文
            if (model.equals("nanobananaWithChinese")) {
                // 先使用banana再使用seedream4
                // 调用seedream4处理图片，返回url
                ArrayList<String> fistProcessedUrls = new ArrayList<>();
                fistProcessedUrls.add(url);
                String finalProcessedUrl = seedream4.process("将图片中的所有英文翻译为中文", fistProcessedUrls);
                if (finalProcessedUrl == null || finalProcessedUrl.isBlank()) {
                    return Result.error("图片处理失败(seedream4)");
                }
                byte[] bytes = HttpUtil.downloadBytes(finalProcessedUrl);
                url = r2Service.uploadBytes(bytes, fileName, contentType, folder);
            }
        } else if (model.equals("seedream4")) {
            // 调用seedream4处理图片，返回url
            ArrayList<String> originurls = new ArrayList<>();
            originurls.add(originUrl);
            url = seedream4.process(prompt, originurls);
            if (url == null || url.isBlank()) {
                return Result.error("图片处理失败(seedream4)");
            }
            byte[] bytes = HttpUtil.downloadBytes(url);
            url = r2Service.uploadBytes(bytes, fileName, contentType, folder);
        }

        // insert media table
        Media image = new Media();
        image.setUsername(username);
        image.setMediaurl(url);
        image.setPrompt(prompt);
        image.setCreatetime(LocalDateTime.now());
        image.setIspublic(0);
        image.setOriginurl(originUrl);
        image.setModel(model);
        image.setReviewcount(0);
        mediaService.save(image);

        // 消耗用户点数
        user.setCredit(user.getCredit() - 1);
        // 更新用户积分
        UserService.update(user);

        // 转换为webp格式
        String webpUrl = r2Service.handleUrl(url);
        return Result.success("图片上传成功", webpUrl);
    }

    @GetMapping("/Userlibrary")
    public Result<PageBean<mediaVo>> getLibrary(Integer pageNum, Integer pageSize) {
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        PageBean<mediaVo> mediaList = mediaService.getUserMedia(username, pageNum, pageSize);
        return Result.success(mediaList);
    }

    @GetMapping("/Alllibrary")
    public Result<PageBean<mediaVo>> getAllLibrary(Integer pageNum, Integer pageSize, String sortBy) {
        PageBean<mediaVo> mediaList = mediaService.getAllMedia(pageNum, pageSize, sortBy);
        return Result.success(mediaList);
    }

    @PutMapping("/media/public/update")
    public Result<String> setMediaPublic(@RequestParam Long mediaId, @RequestParam Boolean isPublic) {
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        boolean ok = mediaService.updateMediaPublicStatus(mediaId, isPublic, username);
        if (ok) {
            return Result.success("更新成功");
        }
        return Result.error("未找到该媒体或无权限");
    }

    @GetMapping("/media/originurl")
    public Result<String> getMediaOriginUrl(@RequestParam Long mediaId) {
        String originurl = mediaService.getOriginUrlById(mediaId);
        if (originurl == null) {
            return Result.error("未找到该媒体");
        }
        return Result.success("获取媒体成功", originurl);
    }

    @GetMapping("/media/{mediaId}")
    public Result<mediaVo> getMediaById(@PathVariable Long mediaId) {
        mediaVo mediavo = mediaService.getMediaById(mediaId);
        if (mediavo == null) {
            return Result.error("未找到该媒体");
        }
        return Result.success("获取帖子成功", mediavo);
    }

}
