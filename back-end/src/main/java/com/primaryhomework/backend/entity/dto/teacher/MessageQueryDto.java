package com.primaryhomework.backend.entity.dto.teacher;

import lombok.Data;

@Data
public class MessageQueryDto {

    private String keyword;
    private String bizType;
    private String sendStatus;
    private Integer pageNo = 1;
    private Integer pageSize = 20;
}
