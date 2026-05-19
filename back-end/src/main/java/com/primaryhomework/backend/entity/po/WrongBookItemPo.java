package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wrong_book_item")
public class WrongBookItemPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long schoolId;
    private Long studentId;
    private Long homeworkId;
    private Long taskId;
    private Long submissionId;
    private Long reviewId;
    private String subjectCode;
    private String sourceType;
    private String questionNo;
    private String questionText;
    private String studentAnswer;
    private String correctAnswer;
    private String analysisText;
    private String wrongReasonCode;
    private String status;
    private Long addedByUserId;
    private String addedByRole;
    private BigDecimal recognizedConfidence;
    private String lastFixedText;
    private LocalDateTime lastFixedAt;
    private Integer fixCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
