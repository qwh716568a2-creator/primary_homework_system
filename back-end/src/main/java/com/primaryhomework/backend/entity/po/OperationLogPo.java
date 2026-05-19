package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLogPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long operatorUserId;
    private String operatorRole;
    private String bizType;
    private Long bizId;
    private String actionType;
    private String requestPayload;
    private Integer resultCode;
    private LocalDateTime createdAt;
}
