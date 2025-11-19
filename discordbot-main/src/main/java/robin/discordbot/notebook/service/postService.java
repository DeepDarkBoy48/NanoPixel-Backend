package robin.discordbot.notebook.service;

import org.springframework.stereotype.Service;
import robin.discordbot.notebook.pojo.dto.postdto;
import robin.discordbot.pojo.entity.Result;

@Service
public interface notebookService {
    void addNotebook(postdto postdto);
}
