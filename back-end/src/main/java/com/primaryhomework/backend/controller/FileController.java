package com.primaryhomework.backend.controller;

import com.primaryhomework.backend.entity.po.UserPo;
import com.primaryhomework.backend.entity.vo.FileUploadVo;
import com.primaryhomework.backend.entity.vo.R;
import com.primaryhomework.backend.mapper.UserMapper;
import com.primaryhomework.backend.service.FileStorageService;
import com.primaryhomework.backend.utils.CommonException;
import com.primaryhomework.backend.utils.TokenSupport;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public R<FileUploadVo> upload(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bizType", required = false) String bizType
    ) throws IOException {
        requireLogin(authorization);
        if (file == null || file.isEmpty()) {
            throw new CommonException("\u8bf7\u9009\u62e9\u8981\u4e0a\u4f20\u7684\u6587\u4ef6");
        }
        return R.ok(fileStorageService.upload(file, bizType));
    }

    @GetMapping("/preview/{bizType}/{date}/{fileName}")
    public ResponseEntity<Resource> preview(
            @PathVariable String bizType,
            @PathVariable String date,
            @PathVariable String fileName
    ) throws IOException {
        return previewByRelativePath(Paths.get(
                URLDecoder.decode(bizType, StandardCharsets.UTF_8),
                URLDecoder.decode(date, StandardCharsets.UTF_8),
                URLDecoder.decode(fileName, StandardCharsets.UTF_8)
        ).toString());
    }

    @GetMapping("/preview/primary-homework/{bizType}/{date}/{fileName}")
    public ResponseEntity<Resource> previewWithDefaultPrefix(
            @PathVariable String bizType,
            @PathVariable String date,
            @PathVariable String fileName
    ) throws IOException {
        return previewByRelativePath(Paths.get(
                "primary-homework",
                URLDecoder.decode(bizType, StandardCharsets.UTF_8),
                URLDecoder.decode(date, StandardCharsets.UTF_8),
                URLDecoder.decode(fileName, StandardCharsets.UTF_8)
        ).toString());
    }

    @GetMapping("/preview/**")
    public ResponseEntity<Resource> previewAnyPath(HttpServletRequest request) throws IOException {
        String uri = request.getRequestURI();
        String marker = "/api/files/preview/";
        int index = uri.indexOf(marker);
        if (index < 0) {
            throw new CommonException(40001, "\u6587\u4ef6\u8def\u5f84\u4e0d\u5408\u6cd5");
        }
        String relativePath = uri.substring(index + marker.length());
        relativePath = URLDecoder.decode(relativePath, StandardCharsets.UTF_8);
        return previewByRelativePath(relativePath);
    }

    private ResponseEntity<Resource> previewByRelativePath(String relativePath) throws IOException {
        Path root = uploadRoot();
        Path target = root.resolve(relativePath).normalize();
        ensureInsideRoot(root, target);

        if (!Files.exists(target) || !Files.isRegularFile(target)) {
            throw new CommonException(40401, "\u6587\u4ef6\u4e0d\u5b58\u5728");
        }

        Resource resource = new UrlResource(target.toUri());
        String contentType = Files.probeContentType(target);
        MediaType mediaType = parseMediaType(contentType);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.noCache())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(target.getFileName().toString()).build().toString())
                .body(resource);
    }

    private UserPo requireLogin(String authorization) {
        TokenSupport.ParsedToken parsedToken = TokenSupport.parseAuthorization(authorization);
        if (parsedToken == null) {
            throw new CommonException(40101, "\u8bf7\u5148\u767b\u5f55\u540e\u518d\u4e0a\u4f20\u6587\u4ef6");
        }
        UserPo user = userMapper.selectById(parsedToken.userId());
        if (user == null || !StringUtils.hasText(user.getRoleType())) {
            throw new CommonException(40101, "\u5f53\u524d\u767b\u5f55\u72b6\u6001\u65e0\u6548\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55");
        }
        if (StringUtils.hasText(user.getStatus()) && !"enabled".equalsIgnoreCase(user.getStatus())) {
            throw new CommonException(40301, "\u5f53\u524d\u8d26\u53f7\u5df2\u88ab\u7981\u7528");
        }
        return user;
    }

    private Path uploadRoot() throws IOException {
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
        return root;
    }

    private void ensureInsideRoot(Path root, Path target) {
        if (!target.startsWith(root)) {
            throw new CommonException(40001, "\u6587\u4ef6\u8def\u5f84\u4e0d\u5408\u6cd5");
        }
    }

    private MediaType parseMediaType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException ignored) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
