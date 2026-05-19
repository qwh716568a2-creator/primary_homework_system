package com.primaryhomework.backend.controller;

import com.primaryhomework.backend.entity.dto.teacher.MessageQueryDto;
import com.primaryhomework.backend.entity.dto.teacher.MessageSendDto;
import com.primaryhomework.backend.entity.vo.PageDTO;
import com.primaryhomework.backend.entity.vo.R;
import com.primaryhomework.backend.entity.vo.teacher.MessageCreatedVo;
import com.primaryhomework.backend.entity.vo.teacher.MessageRecordVo;
import com.primaryhomework.backend.service.TeacherMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherMessageController {

    private final TeacherMessageService teacherMessageService;

    @GetMapping("/messages")
    public R<PageDTO<MessageRecordVo>> pageMessages(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            MessageQueryDto queryDto
    ) {
        return R.ok(teacherMessageService.pageMessages(authorization, queryDto));
    }

    @PostMapping("/messages")
    public R<MessageCreatedVo> createMessage(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody MessageSendDto sendDto
    ) {
        return R.ok(teacherMessageService.sendMessage(authorization, sendDto));
    }

    @PostMapping("/messages/send")
    public R<MessageCreatedVo> sendMessage(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody MessageSendDto sendDto
    ) {
        return R.ok(teacherMessageService.sendMessage(authorization, sendDto));
    }

    @DeleteMapping("/messages/{messageId}")
    public R<Void> deleteMessage(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long messageId
    ) {
        teacherMessageService.deleteMessage(authorization, messageId);
        return R.ok();
    }
}
