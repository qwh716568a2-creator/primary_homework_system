package com.primaryhomework.backend.entity.vo.teacher;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReviewWrongItemVo {

    private String questionNo;
    private String questionText;
    private String studentAnswer;
    private String correctAnswer;
    private String analysisText;
    private String wrongReasonCode;
    private List<HomeworkAssetVo> assets = new ArrayList<>();
}
