package robin.discordbot.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import robin.discordbot.pojo.entity.Result;
import robin.discordbot.service.CloudflareR2Service;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 * 演示 Cloudflare R2 文件上传功能
 */
@RestController
public class FileUploadController {

    private final CloudflareR2Service r2Service;

    public FileUploadController(CloudflareR2Service r2Service) {
        this.r2Service = r2Service;
    }

    /**
     * 上传单个文件
     * @param file 上传的文件
     * @param folder 可选的文件夹名称
     * @return 上传结果
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {
        folder = "Robin";
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证文件
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 上传文件
            String fileUrl = r2Service.uploadFile(file, folder);
            
            result.put("fileUrl", fileUrl);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("folder", folder);
            
            return Result.success(result);
            
        } catch (Exception e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传文件
     * @param files 上传的文件列表
     * @param folder 可选的文件夹名称
     * @return 上传结果
     */
    @PostMapping("/upload-batch")
    public Result<Map<String, Object>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "folder", required = false) String folder) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证文件列表
            if (files.isEmpty()) {
                return Result.error("文件列表不能为空");
            }

            // 批量上传文件
            List<Map<String, Object>> uploadResults = files.stream()
                    .map(file -> {
                        Map<String, Object> fileResult = new HashMap<>();
                        try {
                            String fileUrl = r2Service.uploadFile(file, folder);
                            fileResult.put("success", true);
                            fileResult.put("fileName", file.getOriginalFilename());
                            fileResult.put("fileUrl", fileUrl);
                            fileResult.put("fileSize", file.getSize());
                        } catch (Exception e) {
                            fileResult.put("success", false);
                            fileResult.put("fileName", file.getOriginalFilename());
                            fileResult.put("error", e.getMessage());
                        }
                        return fileResult;
                    })
                    .toList();

            result.put("results", uploadResults);
            result.put("totalFiles", files.size());
            
            return Result.success(result);
            
        } catch (Exception e) {
            return Result.error("批量上传失败: " + e.getMessage());
        }
    }

    /**
     * 列出文件
     * @param prefix 文件前缀（文件夹）
     * @return 文件列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> listFiles(
            @RequestParam(value = "prefix", required = false) String prefix) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<S3Object> files = r2Service.listFiles(prefix);
            
            List<Map<String, Object>> fileList = files.stream()
                    .map(s3Object -> {
                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("key", s3Object.key());
                        fileInfo.put("size", s3Object.size());
                        fileInfo.put("lastModified", s3Object.lastModified());
                        fileInfo.put("eTag", s3Object.eTag());
                        return fileInfo;
                    })
                    .toList();

            result.put("files", fileList);
            result.put("count", fileList.size());
            result.put("prefix", prefix);
            
            return Result.success(result);
            
        } catch (Exception e) {
            return Result.error("获取文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     * @param fileName 文件名
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public Result<Map<String, Object>> deleteFile(
            @RequestParam("fileName") String fileName) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean deleted = r2Service.deleteFile(fileName);
            
            result.put("fileName", fileName);
            result.put("deleted", deleted);
            
            if (deleted) {
                return Result.success(result);
            } else {
                return Result.error("文件删除失败");
            }
            
        } catch (Exception e) {
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     * @param fileName 文件名
     * @return 检查结果
     */
    @GetMapping("/exists")
    public Result<Map<String, Object>> fileExists(
            @RequestParam("fileName") String fileName) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean exists = r2Service.fileExists(fileName);
            
            result.put("exists", exists);
            result.put("fileName", fileName);
            
            return Result.success(result);
            
        } catch (Exception e) {
            return Result.error("检查文件失败: " + e.getMessage());
        }
    }
}
