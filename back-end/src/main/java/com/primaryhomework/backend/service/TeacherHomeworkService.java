package com.primaryhomework.backend.service;

import com.primaryhomework.backend.entity.dto.teacher.ClassBindDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkPageQueryDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkRemindDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkReviewDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkRevokeDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkSaveDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkStatsQueryDto;
import com.primaryhomework.backend.entity.dto.teacher.HomeworkTaskQueryDto;
import com.primaryhomework.backend.entity.vo.PageDTO;
import com.primaryhomework.backend.entity.vo.teacher.ClassCandidateVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkDetailVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkListItemVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkOverviewStatsVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkPrintVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkSavedVo;
import com.primaryhomework.backend.entity.vo.teacher.ReviewSaveVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkTaskDetailVo;
import com.primaryhomework.backend.entity.vo.teacher.HomeworkTaskListItemVo;
import com.primaryhomework.backend.entity.vo.teacher.TeachingClassVo;

import java.util.List;

public interface TeacherHomeworkService {

    List<TeachingClassVo> listTeachingClasses(String authorization, String subjectCode);

    List<TeachingClassVo> listAvailableClasses(String authorization, String subjectCode);

    List<ClassCandidateVo> listBindingCandidates(String authorization, String keyword);

    void bindClass(String authorization, ClassBindDto bindDto);

    void unbindClass(String authorization, Long classId, String subjectCode);

    PageDTO<HomeworkListItemVo> pageHomeworks(String authorization, HomeworkPageQueryDto queryDto);

    HomeworkSavedVo createHomework(String authorization, HomeworkSaveDto saveDto);

    void updateHomework(String authorization, Long homeworkId, HomeworkSaveDto saveDto);

    void publishHomework(String authorization, Long homeworkId);

    void revokeHomework(String authorization, Long homeworkId, HomeworkRevokeDto revokeDto);

    void deleteHomework(String authorization, Long homeworkId);

    HomeworkDetailVo getHomeworkDetail(String authorization, Long homeworkId);

    HomeworkPrintVo getHomeworkPrint(String authorization, Long homeworkId);

    PageDTO<HomeworkTaskListItemVo> pageHomeworkTasks(String authorization, Long homeworkId, HomeworkTaskQueryDto queryDto);

    HomeworkTaskDetailVo getTaskDetail(String authorization, Long taskId);

    ReviewSaveVo reviewTask(String authorization, Long taskId, HomeworkReviewDto reviewDto);

    void remindHomework(String authorization, Long homeworkId, HomeworkRemindDto remindDto);

    HomeworkOverviewStatsVo getOverview(String authorization, HomeworkStatsQueryDto queryDto);
}
