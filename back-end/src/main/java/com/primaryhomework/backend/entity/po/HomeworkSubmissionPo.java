package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("homework_submission")
public class HomeworkSubmissionPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long homeworkId;
    private Long studentId;
    private String operatorRole;
    private Long operatorUserId;
    private String submitText;
    private LocalDateTime submittedAt;
    private Boolean isLate;
    private Integer versionNo;
    private String submitStatus;
    private LocalDateTime createdAt;
}
