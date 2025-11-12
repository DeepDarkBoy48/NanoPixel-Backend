package robin.discordbot.service;

import robin.discordbot.pojo.entity.PageBean;
import robin.discordbot.pojo.entity.geminiEntity.Media;
import robin.discordbot.pojo.vo.mediaVo;

import java.util.List;

public interface MediaService {
    String save(Media image);

    PageBean<mediaVo> getUserMedia(String username, Integer pageNum, Integer pageSize);

    PageBean<mediaVo> getAllMedia(Integer pageNum, Integer pageSize, String sortBy);

    boolean updateMediaPublicStatus(Long id, boolean isPublic, String username);

    String getOriginUrlById(Long mediaId);

    mediaVo getMediaById(Long mediaId);

    void updateCommitId1();
}
