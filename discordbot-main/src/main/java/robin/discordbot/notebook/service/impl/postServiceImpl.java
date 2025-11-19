package robin.discordbot.notebook.service.impl;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import robin.discordbot.notebook.pojo.dto.postdto;
import robin.discordbot.notebook.pojo.entity.post;
import robin.discordbot.notebook.service.postService;

import robin.discordbot.notebook.mapper.postMapper;

@Service
@RequiredArgsConstructor
public class postServiceImpl implements postService {

    private final postMapper postMapper;

    @Override
    public void addNotebook(postdto postdto) {
        post post = new post();
        BeanUtil.copyProperties(postdto, post);
        postMapper.insert(post);
    }
}
