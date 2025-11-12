package robin.discordbot.controller;
import cn.hutool.core.date.DateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import robin.discordbot.pojo.entity.Result;
import robin.discordbot.pojo.entity.User;
import robin.discordbot.pojo.entity.embed;
import robin.discordbot.pojo.mq.embedPdf;
import robin.discordbot.service.CloudflareR2Service;
import robin.discordbot.service.EmbedService;
import robin.discordbot.service.UserService;
import robin.discordbot.utils.ThreadLocalUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/embed")
@RequiredArgsConstructor
public class EmbedController {

    private final EmbedService embedService;
    private final CloudflareR2Service r2Service;
    private final UserService userService;
    private final RabbitTemplate rabbitTemplate;

    @PostMapping("/upload")
    public Result uploadPdf(MultipartFile file) {
        // 线程本地变量获取用户名
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User user = userService.findByUserName(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        Integer userId = user.getId();


        String url = r2Service.uploadFile(file, "robin.pdf");
        String fileName = file.getOriginalFilename();
        embed embed = new embed();
        embed.setUserId(userId);
        embed.setUrl(url);
        embed.setName(fileName);
        embed.setCreateTime(LocalDateTime.now());
        embed.setIsEmbed(0);
        // 插入数据库
        embedService.insert(embed);
        Integer id = embedService.getIdByUrl(url);
        //异步嵌入pdf
        try{
            embedPdf embedPdf = new embedPdf();
            embedPdf.setUrl(url);
            embedPdf.setFileId(id);
            embedPdf.setUserId(userId);
            rabbitTemplate.convertAndSend("embed.direct","embed.pdf",embedPdf);
        }catch (Exception e){
            return Result.error("文件上传失败");
        }
        return Result.success("文件上传成功");
    }


    @PostMapping("/list")
    public Result<List<embed>> listPdf() {
        // 线程本地变量获取用户名
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User user = userService.findByUserName(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        Integer userId = user.getId();
        List<embed> embedList = embedService.listPdf(userId);
        return Result.success("查询成功", embedList);

    }


    @DeleteMapping("/delete")
    public Result deletePdf(Integer fileid) {
        // 线程本地变量获取用户名
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User user = userService.findByUserName(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        Integer userId = user.getId();
        embedService.deletePdf(fileid, userId);
        return Result.success("文件删除成功");
    }



}
