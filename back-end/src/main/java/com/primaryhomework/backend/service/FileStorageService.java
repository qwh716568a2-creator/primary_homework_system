package com.primaryhomework.backend.service;

import com.primaryhomework.backend.entity.vo.FileUploadVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {

    FileUploadVo upload(MultipartFile file, String bizType) throws IOException;
}
