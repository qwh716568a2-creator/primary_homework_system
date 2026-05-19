package com.primaryhomework.backend.entity.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MessageSendDto {

    @NotBlank(message = "消息类型不能为空")
    private String bizType;

    @NotBlank(message = "发送范围不能为空")
    private String scopeType;

    private Long homeworkId;

    private List<Long> classIds = new ArrayList<>();

    @NotBlank(message = "接收人类型不能为空")
    private String receiverRole;

    @NotEmpty(message = "通知渠道不能为空")
    private List<String> notifyChannels = new ArrayList<>();

    @NotBlank(message = "消息标题不能为空")
    private String notifyTitle;

    @NotBlank(message = "消息正文不能为空")
    private String notifyContent;
}
