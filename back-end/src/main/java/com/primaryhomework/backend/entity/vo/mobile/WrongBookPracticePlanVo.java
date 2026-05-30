package com.primaryhomework.backend.entity.vo.mobile;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WrongBookPracticePlanVo {

    private Long practiceId;
    private String practiceName;
    private Integer questionCount;
    private Integer wrongQuestionCount;
    private Integer riskyQuestionCount;
    private List<WrongBookPracticeItemVo> items = new ArrayList<>();
}
