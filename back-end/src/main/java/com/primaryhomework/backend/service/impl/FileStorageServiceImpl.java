package com.primaryhomework.backend.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.primaryhomework.backend.config.OssProperties;
import com.primaryhomework.backend.entity.vo.FileUploadVo;
import com.primaryhomework.backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final DateTimeFormatter DATE_FOLDER = DateTimeFormatter.BASIC_ISO_DATE;

    private final OssProperties ossProperties;

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.public-base-url:}")
    private String publicBaseUrl;

    @Override
    public FileUploadVo upload(MultipartFile file, String bizType) throws IOException {
        String safeBizType = sanitizeBizType(bizType);
        String extension = resolveExtension(file.getOriginalFilename());
        String objectKey = buildObjectKey(safeBizType, extension);
        if (ossProperties.isConfigured()) {
            return uploadToOss(file, objectKey);
        }
        return uploadToLocal(file, objectKey);
    }

    private FileUploadVo uploadToOss(MultipartFile file, String objectKey) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        if (StringUtils.hasText(file.getContentType())) {
            metadata.setContentType(file.getContentType());
        }
        OSS ossClient = new OSSClientBuilder().build(
                normalizeEndpoint(ossProperties.getEndpoint()),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(ossProperties.getBucketName(), objectKey, inputStream, metadata);
        } finally {
            ossClient.shutdown();
        }

        FileUploadVo vo = new FileUploadVo();
        vo.setFileUrl(buildOssFileUrl(objectKey));
        vo.setFileName(StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename().trim() : objectKey.substring(objectKey.lastIndexOf('/') + 1));
        vo.setFileSize(file.getSize());
        vo.setContentType(file.getContentType());
        return vo;
    }

    private FileUploadVo uploadToLocal(MultipartFile file, String relativePath) throws IOException {
        Path root = uploadRoot();
        Path target = root.resolve(relativePath).normalize();
        ensureInsideRoot(root, target);
        Files.createDirectories(target.getParent());
        file.transferTo(target);

        FileUploadVo vo = new FileUploadVo();
        vo.setFileUrl(buildLocalFileUrl(relativePath));
        vo.setFileName(StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename().trim() : target.getFileName().toString());
        vo.setFileSize(file.getSize());
        vo.setContentType(file.getContentType());
        return vo;
    }

    private String buildObjectKey(String safeBizType, String extension) {
        String prefix = normalizePrefix(ossProperties.getPrefix());
        String date = LocalDate.now().format(DATE_FOLDER);
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        return prefix + "/" + safeBizType + "/" + date + "/" + fileName;
    }

    private String buildOssFileUrl(String objectKey) {
        String baseUrl = StringUtils.hasText(ossProperties.getPublicBaseUrl())
                ? ossProperties.getPublicBaseUrl().trim()
                : "https://" + ossProperties.getBucketName() + "." + normalizeEndpoint(ossProperties.getEndpoint());
        return baseUrl.replaceAll("/$", "") + "/" + objectKey;
    }

    private String buildLocalFileUrl(String relativePath) {
        String normalized = relativePath.replace("\\", "/");
        String previewPath = "/api/files/preview/" + normalized;
        if (!StringUtils.hasText(publicBaseUrl)) {
            return previewPath;
        }
        return publicBaseUrl.replaceAll("/$", "") + previewPath;
    }

    private Path uploadRoot() throws IOException {
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
        return root;
    }

    private void ensureInsideRoot(Path root, Path target) {
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("target path is outside upload root");
        }
    }

    private String sanitizeBizType(String bizType) {
        if (!StringUtils.hasText(bizType)) {
            return "common";
        }
        String normalized = bizType.trim().toLowerCase(Locale.ROOT).replace('\\', '-').replace('/', '-');
        normalized = normalized.replaceAll("[^a-z0-9_-]", "-");
        return StringUtils.hasText(normalized) ? normalized : "common";
    }

    private String resolveExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        String fileName = originalFilename.trim();
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        String ext = fileName.substring(index).toLowerCase(Locale.ROOT);
        return ext.length() > 16 ? "" : ext;
    }

    private String normalizeEndpoint(String endpoint) {
        if (!StringUtils.hasText(endpoint)) {
            return "";
        }
        String normalized = endpoint.trim();
        normalized = normalized.replaceFirst("^https?://", "");
        return normalized.replaceAll("/$", "");
    }

    private String normalizePrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "primary-homework";
        }
        String normalized = prefix.trim().replace('\\', '/');
        normalized = normalized.replaceAll("^/+", "");
        normalized = normalized.replaceAll("/+$", "");
        return StringUtils.hasText(normalized) ? normalized : "primary-homework";
    }
}
