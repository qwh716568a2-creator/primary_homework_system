package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("homework")
public class HomeworkPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long schoolId;
    private Long creatorTeacherId;
    private String subjectCode;
    private String title;
    private String contentText;
    private LocalDateTime deadlineAt;
    private Boolean allowLateSubmit;
    private Boolean allowResubmit;
    private String submitTypeMask;
    private Boolean needParentConfirm;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
