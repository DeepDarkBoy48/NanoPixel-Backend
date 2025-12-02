package robin.discordbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import robin.discordbot.config.CloudflareR2Config;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Cloudflare R2 对象存储服务类
 * 提供文件上传、下载、删除等功能
 */
@Service
public class CloudflareR2Service {

    private static final Logger log = LoggerFactory.getLogger(CloudflareR2Service.class);

    private final S3Client s3Client;
    private final CloudflareR2Config r2Config;

    public CloudflareR2Service(S3Client s3Client, CloudflareR2Config r2Config) {
        this.s3Client = s3Client;
        this.r2Config = r2Config;
    }

    /**
     * 上传文件到 Cloudflare R2
     * @param file 要上传的文件
     * @param folder 存储文件夹（可选）
     * @return 文件的公开访问 URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 生成唯一的文件名
            String fileName = generateFileName(file.getOriginalFilename(), folder);
            
            // 构建上传请求
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            // 上传文件
            PutObjectResponse response = s3Client.putObject(putObjectRequest, 
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("文件上传成功: {}, ETag: {}", fileName, response.eTag());
            
            // 返回文件访问 URL（需要配置 Custom Domain）
            return buildFileUrl(fileName);
            
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 上传字节数组到 Cloudflare R2
     * @param data 字节数据
     * @param fileName 文件名
     * @param contentType 内容类型
     * @param folder 存储文件夹（可选）
     * @return 文件的公开访问 URL
     */
    public String uploadBytes(byte[] data, String fileName, String contentType, String folder) {
        try {
            // 生成唯一的文件名
            String uniqueFileName = generateFileName(fileName, folder);
            
            // 构建上传请求
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(uniqueFileName)
                    .contentType(contentType)
                    .build();

            // 上传文件
            PutObjectResponse response = s3Client.putObject(putObjectRequest, 
                    RequestBody.fromBytes(data));

            log.info("字节数组上传成功: {}, ETag: {}", uniqueFileName, response.eTag());
            
            return buildFileUrl(uniqueFileName);
            
        } catch (Exception e) {
            log.error("字节数组上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("字节数组上传失败", e);
        }
    }

    /**
     * 删除文件
     * @param fileName 文件名（包含路径）
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileName) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("文件删除成功: {}", fileName);
            return true;
            
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量删除文件
     * @param fileNames 文件名列表
     * @return 删除结果
     */
    public DeleteObjectsResponse deleteFiles(List<String> fileNames) {
        try {
            // 构建删除对象列表
            List<ObjectIdentifier> objectsToDelete = fileNames.stream()
                    .map(fileName -> ObjectIdentifier.builder().key(fileName).build())
                    .toList();

            Delete delete = Delete.builder().objects(objectsToDelete).build();

            DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .delete(delete)
                    .build();

            DeleteObjectsResponse response = s3Client.deleteObjects(deleteObjectsRequest);
            log.info("批量删除文件成功: {} 个文件", response.deleted().size());
            
            return response;
            
        } catch (Exception e) {
            log.error("批量删除文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量删除文件失败", e);
        }
    }

    /**
     * 列出存储桶中的所有文件
     * @param prefix 文件前缀（用于筛选特定文件夹）
     * @return 文件列表
     */
    public List<S3Object> listFiles(String prefix) {
        try {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(r2Config.getBucketName());
            
            if (prefix != null && !prefix.isEmpty()) {
                requestBuilder.prefix(prefix);
            }

            ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());
            return response.contents();
            
        } catch (Exception e) {
            log.error("列出文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("列出文件失败", e);
        }
    }

    /**
     * 检查文件是否存在
     * @param fileName 文件名
     * @return 是否存在
     */
    public boolean fileExists(String fileName) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(fileName)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
            
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("检查文件存在性失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 生成带时间戳的唯一文件名
     * @param originalFileName 原始文件名
     * @param folder 文件夹
     * @return 唯一文件名
     */
    private String generateFileName(String originalFileName, String folder) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFileName);
        
        StringBuilder fileName = new StringBuilder();
        if (folder != null && !folder.isEmpty()) {
            fileName.append(folder);
            if (!folder.endsWith("/")) {
                fileName.append("/");
            }
        }
        
        fileName.append(timestamp)
                .append("-")
                .append(uuid);
        
        if (extension != null && !extension.isEmpty()) {
            fileName.append(".").append(extension);
        }
        
        return fileName.toString();
    }

    /**
     * 获取文件扩展名
     * @param fileName 文件名
     * @return 扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 构建文件访问 URL
     * 注意：需要在 Cloudflare R2 中配置 Custom Domain
     * @param fileName 文件名
     * @return 文件访问 URL
     */
    private String buildFileUrl(String fileName) {
        // 使用存储桶的直接访问 URL
        // 格式: https://<bucket-name>.<account-id>.r2.cloudflarestorage.com/<file-name>
        // 或者如果配置了自定义域名，可以使用自定义域名
        System.out.println(fileName);
        return "https://img.nanopixel.uk/" + fileName;
    }

    public String handleUrl(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        return "https://img.nanopixel.uk/cdn-cgi/image/format=webp,quality=85/" + url;
    }

}

