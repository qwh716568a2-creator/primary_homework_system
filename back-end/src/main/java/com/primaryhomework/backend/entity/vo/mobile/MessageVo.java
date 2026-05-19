package com.primaryhomework.backend.entity.vo.mobile;

import lombok.Data;

@Data
public class MessageVo {

    private String id;
    private String title;
    private String content;
    private String time;
    private String kind;
    private Boolean unread;
    private String childName;
}
