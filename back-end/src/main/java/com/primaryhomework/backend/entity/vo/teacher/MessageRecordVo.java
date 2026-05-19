package com.primaryhomework.backend.entity.vo.teacher;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MessageRecordVo {

    private Long messageId;
    private String bizType;
    private String scopeType;
    private String notifyTitle;
    private String notifyContent;
    private String receiverRole;
    private List<String> notifyChannels = new ArrayList<>();
    private List<Long> classIds = new ArrayList<>();
    private List<String> classNames = new ArrayList<>();
    private Long homeworkId;
    private String homeworkTitle;
    private Integer receiverCount;
    private Integer successCount;
    private Integer failedCount;
    private String sendStatus;
    private String sentAt;
    private String createdAt;
}
