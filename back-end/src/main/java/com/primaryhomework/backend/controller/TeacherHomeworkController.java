package com.primaryhomework.backend.controller;

import com.primaryhomework.backend.entity.dto.teacher.ClassBindDto;
import com.primaryhomework.backend.entity.dto.teacher.ClassCandidateQueryDto;
import com.primaryhomework.backend.entity.dto.teacher.ClassQueryDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkPageQueryDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkRemindDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkReviewDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkRevokeDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkSaveDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkStatsQueryDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkTaskQueryDto;
import com.primaryhomework.backend.entity.vo.PageDTO;
import com.primaryhomework.backend.entity.vo.R;
import com.primaryhomework.backend.entity.vo.teacher.ClassCandidateVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkDetailVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkListItemVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkOverviewStatsVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkPrintVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkSavedVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkTaskDetailVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkTaskListItemVo;
import com.primaryhomework.backend.entity.vo.teacher.ReviewSaveVo;
import com.primaryhomework.backend.entity.vo.teacher.TeachingClassVo;
import com.primaryhomework.backend.service.TeacherHomeworkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherHomeworkController {

    private final TeacherHomeworkService teacherHomeworkService;

    @GetMapping("/classes")
    public R<List<TeachingClassVo>> listClasses(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            ClassQueryDto queryDto
    ) {
        return R.ok(teacherHomeworkService.listTeachingClasses(authorization, queryDto.getSubjectCode()));
    }

    @GetMapping("/classes/available")
    public R<List<TeachingClassVo>> listAvailableClasses(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            ClassQueryDto queryDto
    ) {
        return R.ok(teacherHomeworkService.listAvailableClasses(authorization, queryDto.getSubjectCode()));
    }

    @GetMapping("/class-bindings/candidates")
    public R<List<ClassCandidateVo>> listBindingCandidates(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            ClassCandidateQueryDto queryDto
    ) {
        return R.ok(teacherHomeworkService.listBindingCandidates(authorization, queryDto.getKeyword()));
    }

    @PostMapping("/classes/bind")
    public R<Void> bindClass(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ClassBindDto bindDto
    ) {
        teacherHomeworkService.bindClass(authorization, bindDto);
        return R.ok();
    }

    @PostMapping("/class-bindings")
    public R<Void> createClassBinding(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ClassBindDto bindDto
    ) {
        teacherHomeworkService.bindClass(authorization, bindDto);
        return R.ok();
    }

    @DeleteMapping("/classes/bind")
    public R<Void> unbindClass(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Long classId,
            @RequestParam String subjectCode
    ) {
        teacherHomeworkService.unbindClass(authorization, classId, subjectCode);
        return R.ok();
    }

    @DeleteMapping("/class-bindings")
    public R<Void> deleteClassBinding(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Long classId,
            @RequestParam String subjectCode
    ) {
        teacherHomeworkService.unbindClass(authorization, classId, subjectCode);
        return R.ok();
    }

    @GetMapping("/homeworks")
    public R<PageDTO<HomeworkListItemVo>> pageHomeworks(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HomeworkPageQueryDto queryDto
    ) {
        return R.ok(teacherHomeworkService.pageHomeworks(authorization, queryDto));
    }

    @PostMapping("/homeworks")
    public R<HomeworkSavedVo> createHomework(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody HomeworkSaveDto saveDto
    ) {
        return R.ok(teacherHomeworkService.createHomework(authorization, saveDto));
    }

    @PutMapping("/homeworks/{homeworkId}")
    public R<Void> updateHomework(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long homeworkId,
            @Valid @RequestBody HomeworkSaveDto saveDto
    ) {
        teacherHomeworkService.updateHomework(authorization, homeworkId, saveDto);
        return R.ok();
    }

    @PostMapping("/homeworks/{homeworkId}/publish")
    public R<Void> publishHomework(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long homeworkId
    ) {
        teacherHomeworkService.publishHomework(authorization, homeworkId);
        return R.ok();
    }

    @PostMapping("/homeworks/{homeworkId}/revoke")
    public R<Void> revokeHomework(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long homeworkId,
            @RequestBody(required = false) HomeworkRevokeDto revokeDto
    ) {
        teacherHomeworkService.revokeHomework(authorization, homeworkId, revokeDto == null ? new HomeworkRevokeDto() : revokeDto);
        return R.ok();
    }

    @DeleteMapping("/homeworks/{homeworkId}")
    public R<Void> deleteHomework(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long homeworkId
    ) {
        teacherHomeworkService.deleteHomework(authorization, homeworkId);
        return R.ok();
    }

    @GetMapping("/homeworks/{homeworkId}")
    public R<HomeworkDetailVo> getHomeworkDetail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long homeworkId
    ) {
        return R.ok(teacherHomeworkService.getHomeworkDetail(authorization, homeworkId));
    }

    @GetMapping("/homeworks/{homeworkId}/print")
    public R<HomeworkPrintVo> getHomeworkPrint(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long homeworkId
    ) {
        return R.ok(teacherHomeworkService.getHomeworkPrint(authorization, homeworkId));
    }

    @GetMapping("/homeworks/{homeworkId}/tasks")
    public R<PageDTO<HomeworkTaskListItemVo>> pageHomeworkTasks(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long homeworkId,
            HomeworkTaskQueryDto queryDto
    ) {
        return R.ok(teacherHomeworkService.pageHomeworkTasks(authorization, homeworkId, queryDto));
    }

    @GetMapping("/tasks/{taskId}")
    public R<HomeworkTaskDetailVo> getTaskDetail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long taskId
    ) {
        return R.ok(teacherHomeworkService.getTaskDetail(authorization, taskId));
    }

    @PostMapping("/tasks/{taskId}/reviews")
    public R<ReviewSaveVo> reviewTask(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long taskId,
            @Valid @RequestBody HomeworkReviewDto reviewDto
    ) {
        return R.ok(teacherHomeworkService.reviewTask(authorization, taskId, reviewDto));
    }

    @PostMapping("/homeworks/{homeworkId}/remind")
    public R<Void> remindHomework(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long homeworkId,
            @RequestBody(required = false) HomeworkRemindDto remindDto
    ) {
        teacherHomeworkService.remindHomework(authorization, homeworkId, remindDto == null ? new HomeworkRemindDto() : remindDto);
        return R.ok();
    }

    @GetMapping("/stats/homework-overview")
    public R<HomeworkOverviewStatsVo> getOverview(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HomeworkStatsQueryDto queryDto
    ) {
        return R.ok(teacherHomeworkService.getOverview(authorization, queryDto));
    }
}
