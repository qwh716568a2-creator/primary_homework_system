package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("homework_review")
public class HomeworkReviewPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long homeworkId;
    private Long studentId;
    private Long submissionId;
    private Long reviewerTeacherId;
    private String reviewStatus;
    private BigDecimal score;
    private String scoreLevel;
    private String commentText;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
