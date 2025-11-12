package robin.discordbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * Cloudflare R2 对象存储配置类
 * Cloudflare R2 与 AWS S3 API 兼容
 */
@Configuration
public class CloudflareR2Config {

    @Value("${cloudflare.r2.access-key}")
    private String accessKey;

    @Value("${cloudflare.r2.secret-key}")
    private String secretKey;

    @Value("${cloudflare.r2.endpoint}")
    private String endpoint;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    /**
     * 配置 S3 客户端以连接到 Cloudflare R2
     * @return S3Client 实例
     */
    @Bean
    public S3Client s3Client() {
        // 创建认证凭据
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        // 构建 S3 客户端
        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(endpoint))
                .region(Region.AWS_GLOBAL) // Cloudflare R2 不依赖具体区域，但需要指定一个
                .forcePathStyle(true) // 使用路径风格的 URL
                .build();
    }

    /**
     * 获取存储桶名称
     * @return bucket name
     */
    public String getBucketName() {
        return bucketName;
    }
} 