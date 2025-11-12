package robin.discordbot.service.impl;

import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import robin.discordbot.mapper.embedMapper;
import robin.discordbot.pojo.entity.embed;
import robin.discordbot.pojo.mq.embedPdf;
import robin.discordbot.service.EmbedService;

import java.time.Duration;
import java.util.List;

import static dev.langchain4j.store.embedding.chroma.ChromaApiVersion.V2;
import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Service
@RequiredArgsConstructor
public class EmbedServiceImpl implements EmbedService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final embedMapper embedMapper;
    private final Dotenv dotenv = Dotenv.load();



    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "embed.queue", durable = "true"),
            exchange = @Exchange(name = "embed.direct"),
            key = "embed.pdf"
    ))
    public void ingestPdf(embedPdf embedPdf) {
        String url = embedPdf.getUrl();
        Integer fileId = embedPdf.getFileId();
        Integer userId = embedPdf.getUserId();

        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                .apiVersion(V2)
                .baseUrl(dotenv.get("chromaBaseUrl"))
                .collectionName("xuzi")
                .logRequests(true)
                .logResponses(true)
                .timeout(Duration.ofMinutes(1))
                .build();
        Document document = UrlDocumentLoader.load(url, new ApachePdfBoxDocumentParser());

        document.metadata()
                .put("fileId", fileId.toString())
                .put("userId", userId.toString());

        DocumentSplitter ds = DocumentSplitters.recursive(100,20);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .documentSplitter(ds)
                .build();
        ingestor.ingest(document);
        // 更新数据库
        embedMapper.updateIsEmbed(fileId, 1);
        System.out.println("向量化成功");
    }

    @Override
    public Integer insert(embed embed) {
        // 插入数据库
        Integer id = embedMapper.insert(embed);
        return id;
    }

    @Override
    public List<embed> listPdf(Integer userId) {
        List<embed> embeds = embedMapper.listByUserId(userId);
        return embeds;
    }

    @Override
    @Transactional
    public void deletePdf(Integer fileId, Integer userId) {
        try {
            // 删除数据库
            embedMapper.deleteByFileIdAndUserId(fileId, userId);
            // 删除向量数据库
            Filter filter = metadataKey("fileId").isEqualTo(fileId.toString()).and(metadataKey("userId").isEqualTo(userId.toString()));
            embeddingStore.removeAll(filter);
        } catch (Exception e) {
            throw new RuntimeException("删除文件失败", e);
        }
    }

    @Override
    public Integer getIdByUrl(String url) {
        Integer id = embedMapper.getIdByUrl(url);
        return id;
    }
}
