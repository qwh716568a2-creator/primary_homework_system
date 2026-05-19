package com.primaryhomework.backend.service;

import com.primaryhomework.backend.entity.dto.teacher.MessageQueryDto;
import com.primaryhomework.backend.entity.dto.teacher.MessageSendDto;
import com.primaryhomework.backend.entity.vo.PageDTO;
import com.primaryhomework.backend.entity.vo.teacher.MessageCreatedVo;
import com.primaryhomework.backend.entity.vo.teacher.MessageRecordVo;

public interface TeacherMessageService {

    PageDTO<MessageRecordVo> pageMessages(String authorization, MessageQueryDto queryDto);

    MessageCreatedVo sendMessage(String authorization, MessageSendDto sendDto);

    void deleteMessage(String authorization, Long messageId);
}
