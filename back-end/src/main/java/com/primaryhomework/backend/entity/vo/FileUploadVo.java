package com.primaryhomework.backend.entity.vo;

import lombok.Data;

@Data
public class FileUploadVo {

    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String contentType;
}
