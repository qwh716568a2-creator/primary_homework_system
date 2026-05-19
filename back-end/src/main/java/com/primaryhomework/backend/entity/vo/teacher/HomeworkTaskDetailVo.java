package com.primaryhomework.backend.entity.vo.teacher;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class HomeworkTaskDetailVo {

    private TaskInfo taskInfo;
    private List<Submission> submissions = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();

    @Data
    public static class TaskInfo {
        private Long taskId;
        private Long studentId;
        private String studentName;
        private String taskStatus;
        private String reviewStatus;
    }

    @Data
    public static class Submission {
        private Long submissionId;
        private Integer versionNo;
        private String operatorRole;
        private LocalDateTime submittedAt;
        private String submitText;
        private List<HomeworkAssetVo> assets = new ArrayList<>();
    }

    @Data
    public static class Review {
        private Long reviewId;
        private String reviewStatus;
        private BigDecimal score;
        private String scoreLevel;
        private String commentText;
        private LocalDateTime reviewedAt;
        private List<HomeworkAssetVo> reviewAssets = new ArrayList<>();
        private List<ReviewWrongItemVo> wrongItems = new ArrayList<>();
    }
}
