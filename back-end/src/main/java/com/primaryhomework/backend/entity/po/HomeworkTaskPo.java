package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("student_homework_task")
public class HomeworkTaskPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long homeworkId;
    private Long studentId;
    private Long classId;
    private String taskStatus;
    private Long latestSubmissionId;
    private Integer submissionCount;
    private LocalDateTime latestSubmittedAt;
    private String latestReviewStatus;
    private LocalDateTime latestReviewedAt;
    private Boolean isLate;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
