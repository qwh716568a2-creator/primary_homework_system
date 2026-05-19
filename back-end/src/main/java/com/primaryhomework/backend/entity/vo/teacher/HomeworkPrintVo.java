package com.primaryhomework.backend.entity.vo.teacher;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class HomeworkPrintVo {

    private Long homeworkId;
    private String title;
    private String printTitle;
    private String templateType;
    private String schoolName;
    private String teacherName;
    private String subjectCode;
    private String subjectName;
    private String contentText;
    private LocalDateTime deadlineAt;
    private String status;
    private Boolean allowLateSubmit;
    private Boolean allowResubmit;
    private Boolean needParentConfirm;
    private List<String> submitTypes = new ArrayList<>();
    private LocalDateTime generatedAt;
    private List<ClassSummary> classList = new ArrayList<>();
    private List<ClassStudentGroup> studentGroups = new ArrayList<>();
    private List<HomeworkAssetVo> attachments = new ArrayList<>();

    @Data
    public static class ClassSummary {
        private Long classId;
        private String className;
        private Integer studentCount;
    }

    @Data
    public static class ClassStudentGroup {
        private Long classId;
        private String className;
        private List<StudentItem> students = new ArrayList<>();
    }

    @Data
    public static class StudentItem {
        private Long studentId;
        private String studentName;
        private String studentNo;
    }
}
