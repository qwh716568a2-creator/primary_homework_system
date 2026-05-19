package com.primaryhomework.backend.entity.vo.teacher;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClassCandidateVo {

    private Long classId;
    private String className;
    private Long gradeId;
    private String gradeName;
    private Long schoolId;
    private String schoolName;
    private Integer studentCount;
    private String status;
    private List<ClassSubjectVo> subjectBindings = new ArrayList<>();
}
