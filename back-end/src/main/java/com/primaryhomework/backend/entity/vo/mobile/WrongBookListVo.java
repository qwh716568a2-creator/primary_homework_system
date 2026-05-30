package com.primaryhomework.backend.entity.vo.mobile;

import lombok.Data;

@Data
public class WrongBookListVo {

    private Long id;
    private Long wrongBookId;
    private Long homeworkId;
    private Long taskId;
    private Long reviewId;
    private String subjectCode;
    private String subjectName;
    private String sourceType;
    private String questionNo;
    private String questionText;
    private String wrongReasonCode;
    private String wrongReasonLabel;
    private String teacherName;
    private String status;
    private String poolType;
    private Integer correctStreak;
    private Integer practiceCount;
    private String lastPracticeResult;
    private String lastPracticedAt;
    private String createdAt;
    private String lastFixedAt;
}
