package robin.discordbot.service.impl;

import cn.hutool.core.bean.BeanUtil;
import robin.discordbot.mapper.MediaMapper;
import robin.discordbot.pojo.dto.reviewDto;
import robin.discordbot.pojo.entity.User;
import robin.discordbot.pojo.entity.review;
import robin.discordbot.pojo.vo.reviewVo;
import robin.discordbot.service.UserService;
import robin.discordbot.service.reviewService;

import robin.discordbot.mapper.reviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import robin.discordbot.utils.ThreadLocalUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class reviewServiceImpl implements reviewService {
    private final UserService userService;
    private final reviewMapper reviewMapper;
    private final MediaMapper mediaMapper;
    @Override
    public void add(reviewDto reviewDto) {
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        review review = new review();
        BeanUtil.copyProperties(reviewDto, review);
        User user = userService.findByUserName(username);
        review.setUserId(user.getId());
        review.setCreateTime(LocalDateTime.now());
        reviewMapper.add(review);
        mediaMapper.updateReviewCount(reviewDto.getMediaId(), 1);
    }

    @Override
    public List<reviewVo> getReviewListByMediaId(String mediaId) {
        List<review> reviewList = reviewMapper.getReviewListByMediaId(mediaId);
        if (reviewList.isEmpty()){
            return new ArrayList<>();
        }
        List<Integer> ids = reviewList.stream().map(review::getUserId).collect(Collectors.toList());
        List<User> userList = userService.getUserByIds(ids);
        Map<Integer, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, u -> u));
        ArrayList<reviewVo> reviewVos = new ArrayList<>();
        for (review review : reviewList) {
            reviewVo reviewVo = new reviewVo();
            BeanUtil.copyProperties(review, reviewVo);
            Integer userId = review.getUserId();
            String username = userMap.get(userId).getUsername();
            reviewVo.setUserName(username);
            reviewVos.add(reviewVo);
        }

        return reviewVos;
    }

    @Override
    public List<reviewVo> getReviewListByUserId() {
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User user = userService.findByUserName(username);
        List<review> reviewList = reviewMapper.getReviewListByUserId(user.getId());
        return reviewList.stream().map(review -> {
            reviewVo reviewVo = new reviewVo();
            BeanUtil.copyProperties(review, reviewVo);
            return reviewVo;
        }).collect(Collectors.toList());
    }

    @Override
    public void delete(Integer id) {
        review review = reviewMapper.getReviewById(id);
        Integer mediaid = review.getMediaId();
        mediaMapper.updateReviewCount(mediaid, -1);
        reviewMapper.delete(id);
    }
}
