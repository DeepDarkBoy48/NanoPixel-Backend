package robin.discordbot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import robin.discordbot.pojo.dto.reviewDto;
import robin.discordbot.pojo.entity.Result;
import robin.discordbot.pojo.vo.reviewVo;
import robin.discordbot.service.reviewService;

import java.util.List;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class reviewController {

    private final reviewService reviewService;


    @GetMapping("/list/{mediaId}")
    public Result<List<reviewVo>> list(@PathVariable("mediaId") String mediaId) {
        List<reviewVo> reviewList = reviewService.getReviewListByMediaId(mediaId);
        return Result.success(reviewList);
    }

    @GetMapping( "/userlist")
    public Result<List<reviewVo>> userlist() {
        List<reviewVo> reviewList = reviewService.getReviewListByUserId();
        return Result.success(reviewList);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Integer id) {
        reviewService.delete(id);
        return Result.success("成功删除评论");
    }


    @PostMapping("/add")
    public Result add(@RequestBody reviewDto reviewDto) {
        reviewService.add(reviewDto);
        return Result.success("成功发布评论");
    }
}
