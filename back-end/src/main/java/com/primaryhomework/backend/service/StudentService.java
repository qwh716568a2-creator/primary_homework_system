package com.primaryhomework.backend.service;

import com.primaryhomework.backend.entity.dto.WrongBookCreateDto;
import com.primaryhomework.backend.entity.dto.WrongBookFixDto;
import com.primaryhomework.backend.entity.dto.WrongBookQueryDto;
import com.primaryhomework.backend.entity.dto.mobile.SubmitDto;
import com.primaryhomework.backend.entity.vo.PageDTO;
import com.primaryhomework.backend.entity.vo.mobile.HomeworkVo;
import com.primaryhomework.backend.entity.vo.mobile.MessageVo;
import com.primaryhomework.backend.entity.vo.mobile.ReviewVo;
import com.primaryhomework.backend.entity.vo.mobile.SubmissionVo;
import com.primaryhomework.backend.entity.vo.mobile.SubjectOptionVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookDetailVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookListVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookSaveVo;

import java.util.List;

public interface StudentService {

    List<HomeworkVo> listHomeworks(String authorization, String tab);

    HomeworkVo getHomework(String authorization, Long homeworkId);

    void submitHomework(String authorization, Long homeworkId, SubmitDto submitDto);

    List<SubmissionVo> listSubmissions(String authorization, Long homeworkId);

    List<ReviewVo> listReviews(String authorization, Long homeworkId);

    PageDTO<MessageVo> listNotifications(String authorization, String readStatus, Integer pageNo, Integer pageSize);

    MessageVo getNotification(String authorization, Long notificationId);

    void markNotificationRead(String authorization, Long notificationId);

    List<SubjectOptionVo> listWrongBookSubjects(String authorization);

    PageDTO<WrongBookListVo> listWrongBooks(String authorization, WrongBookQueryDto queryDto);

    WrongBookDetailVo getWrongBook(String authorization, Long wrongBookId);

    WrongBookSaveVo createWrongBook(String authorization, WrongBookCreateDto createDto);

    void fixWrongBook(String authorization, Long wrongBookId, WrongBookFixDto fixDto);

    void markWrongBookMastered(String authorization, Long wrongBookId);
}
