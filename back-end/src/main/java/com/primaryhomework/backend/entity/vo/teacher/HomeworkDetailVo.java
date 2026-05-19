package com.primaryhomework.backend.entity.vo.teacher;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class HomeworkDetailVo {

    private BaseInfo baseInfo;
    private List<ClassSummary> classList = new ArrayList<>();
    private List<HomeworkAssetVo> attachments = new ArrayList<>();

    @Data
    public static class BaseInfo {
        private Long homeworkId;
        private String title;
        private String subjectCode;
        private String contentText;
        private LocalDateTime deadlineAt;
        private String status;
        private Boolean allowLateSubmit;
        private Boolean allowResubmit;
        private Boolean needParentConfirm;
        private List<String> submitTypes = new ArrayList<>();
    }

    @Data
    public static class ClassSummary {
        private Long classId;
        private String className;
        private Integer studentCount;
        private Integer submittedCount;
        private Integer completedCount;
        private Integer revisionRequiredCount;
        private Integer overdueCount;
    }
}
