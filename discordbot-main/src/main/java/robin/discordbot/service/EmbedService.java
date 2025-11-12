package robin.discordbot.service;

import robin.discordbot.pojo.entity.embed;
import robin.discordbot.pojo.mq.embedPdf;

import java.util.List;

public interface EmbedService {
    void ingestPdf(embedPdf embedPdf);

    Integer insert(embed embed);

    List<embed> listPdf(Integer userId);

    void deletePdf(Integer fileId, Integer userId);

    Integer getIdByUrl(String url);
}
