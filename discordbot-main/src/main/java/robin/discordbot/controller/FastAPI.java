package robin.discordbot.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import robin.discordbot.pojo.entity.Result;
import robin.discordbot.pojo.vo.SubtitleVo;

@RestController
@RequestMapping("/FastApi")
public class FastAPI {
    @GetMapping("/getSubTitle")
    public Result<SubtitleVo> getSubTitle(String youtubeUrl) {
        try {
            HttpResponse response = HttpRequest.get("http://47.79.43.73:8001/fastapi/getsubtitles")
                    .form("youtubeUrl", youtubeUrl)
                    .execute();
            if (response.isOk()) {
                String jsonBody = response.body();
                SubtitleVo subtitleVo = JSONUtil.toBean(jsonBody, SubtitleVo.class);
                return Result.success(subtitleVo);
            }
        } catch (Exception e) {

            e.printStackTrace();

        }
        return Result.error("操作失败");
    }
}
