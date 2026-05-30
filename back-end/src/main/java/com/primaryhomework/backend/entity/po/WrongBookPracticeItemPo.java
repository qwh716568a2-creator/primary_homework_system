package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wrong_book_practice_item")
public class WrongBookPracticeItemPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long practiceId;
    private Long studentId;
    private Long wrongBookId;
    private String questionNo;
    private String subjectCode;
    private String questionText;
    private String correctAnswer;
    private String studentAnswer;
    private String itemSourceType;
    private BigDecimal itemWeight;
    private String resultStatus;
    private Integer usedDurationSeconds;
    private Integer sortNo;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
