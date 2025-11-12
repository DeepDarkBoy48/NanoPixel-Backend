package robin.discordbot.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import robin.discordbot.mapper.MediaMapper;
import robin.discordbot.pojo.entity.PageBean;
import robin.discordbot.pojo.entity.geminiEntity.Media;
import robin.discordbot.pojo.vo.mediaVo;
import robin.discordbot.service.CloudflareR2Service;
import robin.discordbot.service.MediaService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaMapper mediaMapper;
    private final CloudflareR2Service r2Service;


    @Override
    public String save(Media media) {
        // 保存图片到数据库
        try {
            mediaMapper.insertMedia(media);
            return "插入媒体成功";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "插入媒体失败";
    }

    @Override
    public PageBean<mediaVo> getUserMedia(String username, Integer pageNum, Integer pageSize) {
        PageBean<mediaVo> mediaVoPageBean = new PageBean<>();
        PageHelper.startPage(pageNum, pageSize);
        // 分页查询
        List<Media> mediaList = mediaMapper.selectMediaByUsername(username);
        for (Media m : mediaList) {
            m.setMediaurl(r2Service.handleUrl(m.getMediaurl()));
            m.setOriginurl(r2Service.handleUrl(m.getOriginurl()));
        }
        // 分页
        Page<Media> p = (Page<Media>) mediaList;
        // 转换为VO
        List<mediaVo> mediaVosList = getMediaVos(p.getResult());
        mediaVoPageBean.setTotal(p.getTotal());
        mediaVoPageBean.setItems(mediaVosList);
        return mediaVoPageBean;
    }

    @Override
    public PageBean<mediaVo> getAllMedia(Integer pageNum, Integer pageSize, String sortBy) {
        PageBean<mediaVo> mediaVoPageBean = new PageBean<>();
        PageHelper.startPage(pageNum,pageSize);
        // 分页查询
        List<Media> mediaList = mediaMapper.selectAllMedia(sortBy);
        for (Media m : mediaList) {
            m.setMediaurl(r2Service.handleUrl(m.getMediaurl()));
            m.setOriginurl(r2Service.handleUrl(m.getOriginurl()));
        }
        // 分页
        Page<Media> p = (Page<Media>) mediaList;
        // 转换为VO
        List<mediaVo> mediaVosList = getMediaVos(p.getResult());
        mediaVoPageBean.setTotal(p.getTotal());
        mediaVoPageBean.setItems(mediaVosList);
        return mediaVoPageBean;
    }

    @Override
    public boolean updateMediaPublicStatus(Long id, boolean isPublic, String username) {
        int flag = isPublic ? 1 : 0;
        int updated = mediaMapper.updateIsPublicByIdAndUsername(id, username, flag);
        return updated > 0;
    }

    @Override
    public String getOriginUrlById(Long mediaId) {
        String originurl = mediaMapper.selectOriginUrlById(mediaId);
        if (originurl != null) {
            originurl = r2Service.handleUrl(originurl);
        }
        return originurl;
    }

    @Override
    public mediaVo getMediaById(Long mediaId) {
        Media media = mediaMapper.selectSingelMediaById(mediaId);
        //转换webp格式
        media.setMediaurl(r2Service.handleUrl(media.getMediaurl()));
        media.setOriginurl(r2Service.handleUrl(media.getOriginurl()));
        mediaVo mediaVo = new mediaVo();
        BeanUtil.copyProperties(media,mediaVo);
        return mediaVo;
    }

    @Override
    public void updateCommitId1() {
        mediaMapper.updateCommitId1();
    }

    @NotNull
    private List<mediaVo> getMediaVos(List<Media> mediaList) {
        List<mediaVo> mediaVos = new ArrayList<>(mediaList.size());
        for (Media media : mediaList) {
            mediaVo mediaVo = new mediaVo();
            mediaVo.setId(media.getId());
            mediaVo.setMediaurl(media.getMediaurl());
            mediaVo.setPrompt(media.getPrompt());
            mediaVo.setCreatetime(media.getCreatetime());
            mediaVo.setUsername(media.getUsername());
            mediaVo.setIspublic(media.getIspublic());
            mediaVo.setReviewcount(media.getReviewcount());
            mediaVo.setModel(media.getModel());
            mediaVos.add(mediaVo);
        }
        return mediaVos;
    }
}
