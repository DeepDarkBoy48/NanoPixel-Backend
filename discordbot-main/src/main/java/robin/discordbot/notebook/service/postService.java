package robin.discordbot.notebook.service;

import org.springframework.stereotype.Service;
import robin.discordbot.notebook.pojo.dto.postdto;


public interface postService {
    void addNotebook(postdto postdto);
}
