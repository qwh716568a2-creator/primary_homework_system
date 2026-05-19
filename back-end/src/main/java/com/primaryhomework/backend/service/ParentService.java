package com.primaryhomework.backend.service;

import com.primaryhomework.backend.entity.dto.WrongBookQueryDto;
import com.primaryhomework.backend.entity.dto.mobile.SubmitDto;
import com.primaryhomework.backend.entity.vo.PageDTO;
import com.primaryhomework.backend.entity.vo.mobile.ChildVo;
import com.primaryhomework.backend.entity.vo.mobile.HomeworkVo;
import com.primaryhomework.backend.entity.vo.mobile.MessageVo;
import com.primaryhomework.backend.entity.vo.mobile.WrongBookListVo;

import java.util.List;

public interface ParentService {

    List<ChildVo> listStudents(String authorization);

    List<HomeworkVo> listHomeworks(String authorization, Long studentId, String tab);

    HomeworkVo getHomework(String authorization, Long studentId, Long homeworkId);

    void assistSubmit(String authorization, Long homeworkId, SubmitDto submitDto);

    PageDTO<MessageVo> listNotifications(String authorization, String readStatus, Integer pageNo, Integer pageSize);

    MessageVo getNotification(String authorization, Long notificationId);

    void markNotificationRead(String authorization, Long notificationId);

    PageDTO<WrongBookListVo> listWrongBooks(String authorization, Long studentId, WrongBookQueryDto queryDto);
}
