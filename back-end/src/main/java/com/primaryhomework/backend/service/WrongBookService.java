package com.primaryhomework.backend.service;

import com.primaryhomework.backend.entity.dto.WrongBookCreateDto;
import com.primaryhomework.backend.entity.dto.WrongBookFixDto;
import com.primaryhomework.backend.entity.dto.WrongBookPracticeSubmitDto;
import com.primaryhomework.backend.entity.dto.WrongBookQueryDto;
import com.primaryhomework.backend.entity.dto.WrongItemDto;
import com.primaryhomework.backend.entity.po.HomeworkPo;
import com.primaryhomework.backend.entity.po.HomeworkReviewPo;
import com.primaryhomework.backend.entity.po.HomeworkSubmissionPo;
import com.primaryhomework.backend.entity.po.HomeworkTaskPo;
import com.primaryhomework.backend.entity.po.UserPo;
import com.primaryhomework.backend.entity.vo.PageDTO;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookDetailVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookListVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookPracticeDetailVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookPracticeHistoryVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookPracticePlanVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookPracticeSubmitResultVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookSaveVo;
import com.primaryhomework.backend.entity.vo.teacher.ReviewWrongItemVo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface WrongBookService {

    int saveTeacherWrongItems(UserPo teacherUser,
                              HomeworkPo homework,
                              HomeworkTaskPo task,
                              HomeworkSubmissionPo submission,
                              HomeworkReviewPo review,
                              List<WrongItemDto> wrongItems);

    Map<Long, List<ReviewWrongItemVo>> loadReviewWrongItemMap(Set<Long> reviewIds);

    PageDTO<WrongBookListVo> pageStudentWrongBooks(String authorization, WrongBookQueryDto queryDto);

    WrongBookDetailVo getStudentWrongBook(String authorization, Long wrongBookId);

    WrongBookSaveVo createStudentWrongBook(String authorization, WrongBookCreateDto createDto);

    void fixStudentWrongBook(String authorization, Long wrongBookId, WrongBookFixDto fixDto);

    void markStudentWrongBookMastered(String authorization, Long wrongBookId);

    PageDTO<WrongBookListVo> pageParentWrongBooks(String authorization, Long studentId, WrongBookQueryDto queryDto);

    WrongBookPracticePlanVo generateStudentPracticePlan(String authorization, String subjectCode, Integer questionCount);

    WrongBookPracticeSubmitResultVo submitStudentPractice(String authorization, WrongBookPracticeSubmitDto submitDto);

    PageDTO<WrongBookPracticeHistoryVo> pageStudentPracticeHistory(String authorization, Integer pageNo, Integer pageSize);

    WrongBookPracticeDetailVo getStudentPractice(String authorization, Long practiceId);
}
