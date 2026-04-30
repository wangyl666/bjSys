package com.wyl.note.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.wyl.note.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload.path}")
    private String uploadPathConfig;

    @Value("${file.upload.access-url}")
    private String accessUrl;

    private String uploadPath;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp",
            "image/bmp", "image/svg+xml"
    );

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
        log.info("文件上传路径: {}", uploadPath);
    }

    @Override
    public String uploadFile(MultipartFile file, Long userId) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("不支持的文件类型，仅支持图片文件");
        }

        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("文件大小不能超过10MB");
        }

        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileExt = FileUtil.extName(file.getOriginalFilename());
            String fileName = IdUtil.simpleUUID() + "." + (fileExt.isEmpty() ? "jpg" : fileExt);
            String relativePath = userId + "/" + datePath + "/" + fileName;
            String fullPath = uploadPath + relativePath.replace("/", File.separator).replace("\\", File.separator);

            File destFile = new File(fullPath);
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }

            file.transferTo(destFile);

            log.info("文件上传成功: {}", fullPath);
            return accessUrl + relativePath;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
}
