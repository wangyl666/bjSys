package com.wyl.note.controller;

import com.wyl.note.common.Result;
import com.wyl.note.security.UserDetailsImpl;
import com.wyl.note.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "文件接口")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Value("${file.upload.path}")
    private String uploadPath;

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
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
