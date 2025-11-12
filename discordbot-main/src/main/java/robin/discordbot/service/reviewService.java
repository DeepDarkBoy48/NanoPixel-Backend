package robin.discordbot.service;


import org.springframework.stereotype.Service;
import robin.discordbot.pojo.dto.reviewDto;
import robin.discordbot.pojo.vo.reviewVo;

import java.util.List;

@Service
public interface reviewService {
    void add(reviewDto reviewDto);

    List<reviewVo> getReviewListByMediaId(String mediaId);

    List<reviewVo> getReviewListByUserId();

    void delete(Integer id);
}
