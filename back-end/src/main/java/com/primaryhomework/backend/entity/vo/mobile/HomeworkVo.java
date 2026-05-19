package com.primaryhomework.backend.entity.vo.mobile;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HomeworkVo {

    private String id;
    private String taskId;
    private String title;
    private String subject;
    private String teacherName;
    private String deadline;
    private String status;
    private String summary;
    private String content;
    private Boolean allowParentAssist;
    private List<AttachmentVo> attachments = new ArrayList<>();
    private List<String> submitTypes = new ArrayList<>();
    private Boolean hasFeedback;
    private SubmissionVo latestSubmission;
    private ReviewVo review;
}
