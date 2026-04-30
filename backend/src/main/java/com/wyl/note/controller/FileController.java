package com.wyl.note.controller;

import com.wyl.note.common.Result;
import com.wyl.note.security.UserDetailsImpl;
import com.wyl.note.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "文件接口")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Value("${file.upload.path}")
    private String uploadPathConfig;

    private String uploadPath;

    @PostConstruct
    public void init() {
        if (uploadPathConfig.startsWith("./") || uploadPathConfig.startsWith(".\\")) {
            String userDir = System.getProperty("user.dir");
            uploadPath = Paths.get(userDir, uploadPathConfig.replaceFirst("\\./", "").replaceFirst("\\.\\\\", "")).toString() + File.separator;
        } else if (!new File(uploadPathConfig).isAbsolute()) {
            String userDir = System.getProperty("user.dir");
            uploadPath = Paths.get(userDir, uploadPathConfig).toString() + File.separator;
        } else {
            uploadPath = uploadPathConfig.endsWith(File.separator) ? uploadPathConfig : uploadPathConfig + File.separator;
        }
        log.info("文件访问路径: {}", uploadPath);
    }

    @Operation(summary = "上传图片")
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file,
                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String url = fileService.uploadFile(file, userDetails.getId());
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        result.put("name", file.getOriginalFilename());
        return Result.success(result);
    }

    @Operation(summary = "访问图片")
    @GetMapping("/{userId}/{date}/{fileName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable Long userId,
                                              @PathVariable String date,
                                              @PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadPath)
                    .resolve(userId.toString())
                    .resolve(date.replace("-", "/"))
                    .resolve(fileName)
                    .normalize();
            log.info("尝试访问图片: {}", filePath);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = "image/jpeg";
                if (fileName.endsWith(".png")) {
                    contentType = "image/png";
                } else if (fileName.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (fileName.endsWith(".webp")) {
                    contentType = "image/webp";
                } else if (fileName.endsWith(".svg")) {
                    contentType = "image/svg+xml";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000")
                        .body(resource);
            } else {
                log.warn("图片不存在或不可读: {}", filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            log.error("访问图片出错", e);
            return ResponseEntity.notFound().build();
        }
    }
}
