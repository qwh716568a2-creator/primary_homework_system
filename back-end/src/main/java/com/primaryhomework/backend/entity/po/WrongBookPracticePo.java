package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wrong_book_practice")
public class WrongBookPracticePo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private String practiceName;
    private String practiceType;
    private Integer questionCount;
    private Integer wrongQuestionCount;
    private Integer riskyQuestionCount;
    private Integer submittedCount;
    private Integer correctCount;
    private Integer wrongCount;
    private BigDecimal accuracyRate;
    private String status;
    private LocalDateTime generatedAt;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
