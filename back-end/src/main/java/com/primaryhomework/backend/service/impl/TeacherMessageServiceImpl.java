package com.primaryhomework.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.primaryhomework.backend.entity.dto.teacher.MessageQueryDto;
import com.primaryhomework.backend.entity.dto.teacher.MessageSendDto;
import com.primaryhomework.backend.entity.po.HomeworkClassPo;
import com.primaryhomework.backend.entity.po.HomeworkPo;
import com.primaryhomework.backend.entity.po.NotificationPo;
import com.primaryhomework.backend.entity.po.OperationLogPo;
import com.primaryhomework.backend.entity.po.ParentStudentPo;
import com.primaryhomework.backend.entity.po.SchoolClassPo;
import com.primaryhomework.backend.entity.po.StudentPo;
import com.primaryhomework.backend.entity.po.TeacherClassSubjectPo;
import com.primaryhomework.backend.entity.po.TeacherPo;
import com.primaryhomework.backend.entity.po.UserPo;
import com.primaryhomework.backend.entity.vo.PageDTO;
import com.primaryhomework.backend.entity.vo.teacher.MessageCreatedVo;
import com.primaryhomework.backend.entity.vo.teacher.MessageRecordVo;
import com.primaryhomework.backend.mapper.HomeworkClassMapper;
import com.primaryhomework.backend.mapper.HomeworkMapper;
import com.primaryhomework.backend.mapper.NotificationMapper;
import com.primaryhomework.backend.mapper.OperationLogMapper;
import com.primaryhomework.backend.mapper.ParentStudentMapper;
import com.primaryhomework.backend.mapper.SchoolClassMapper;
import com.primaryhomework.backend.mapper.StudentMapper;
import com.primaryhomework.backend.mapper.TeacherClassSubjectMapper;
import com.primaryhomework.backend.mapper.TeacherMapper;
import com.primaryhomework.backend.mapper.UserMapper;
import com.primaryhomework.backend.service.TeacherMessageService;
import com.primaryhomework.backend.utils.CommonException;
import com.primaryhomework.backend.utils.CurrentUserSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherMessageServiceImpl implements TeacherMessageService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserMapper userMapper;
    private final TeacherMapper teacherMapper;
    private final TeacherClassSubjectMapper teacherClassSubjectMapper;
    private final SchoolClassMapper schoolClassMapper;
    private final StudentMapper studentMapper;
    private final ParentStudentMapper parentStudentMapper;
    private final HomeworkMapper homeworkMapper;
    private final HomeworkClassMapper homeworkClassMapper;
    private final NotificationMapper notificationMapper;
    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageDTO<MessageRecordVo> pageMessages(String authorization, MessageQueryDto queryDto) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        MessageQueryDto query = queryDto == null ? new MessageQueryDto() : queryDto;

        LambdaQueryWrapper<OperationLogPo> wrapper = new LambdaQueryWrapper<OperationLogPo>()
                .eq(OperationLogPo::getOperatorUserId, teacherUser.getId())
                .eq(OperationLogPo::getOperatorRole, "teacher")
                .eq(OperationLogPo::getActionType, "teacher_message_send")
                .orderByDesc(OperationLogPo::getCreatedAt)
                .orderByDesc(OperationLogPo::getId);

        if (StringUtils.hasText(query.getBizType())) {
            wrapper.eq(OperationLogPo::getBizType, query.getBizType().trim());
        }

        List<MessageRecordVo> allRecords = operationLogMapper.selectList(wrapper).stream()
                .map(this::buildMessageRecordVo)
                .filter(item -> matchesKeyword(item, query.getKeyword()))
                .filter(item -> matchesSendStatus(item, query.getSendStatus()))
                .toList();

        PageSlice<MessageRecordVo> pageSlice = slice(allRecords, query.getPageNo(), query.getPageSize());
        return PageDTO.of(pageSlice.items(), pageSlice.total(), pageSlice.pageNo(), pageSlice.pageSize());
    }

    @Override
    @Transactional
    public MessageCreatedVo sendMessage(String authorization, MessageSendDto sendDto) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        MessageSendDto request = sendDto == null ? new MessageSendDto() : sendDto;

        String bizType = normalizeBizType(request.getBizType());
        String scopeType = normalizeScopeType(request.getScopeType());
        String receiverRole = normalizeReceiverRole(request.getReceiverRole());
        List<String> notifyChannels = normalizeChannels(request.getNotifyChannels());
        String notifyTitle = requiredText(request.getNotifyTitle(), "消息标题不能为空");
        String notifyContent = requiredText(request.getNotifyContent(), "消息正文不能为空");

        MessageTarget target = resolveTarget(teacherUser, scopeType, request.getHomeworkId(), request.getClassIds());
        List<Receiver> receivers = resolveReceivers(target.classIds(), receiverRole);
        if (receivers.isEmpty()) {
            throw new CommonException("未找到可发送的接收人");
        }

        LocalDateTime now = LocalDateTime.now();
        OperationLogPo operationLog = new OperationLogPo();
        operationLog.setOperatorUserId(teacherUser.getId());
        operationLog.setOperatorRole("teacher");
        operationLog.setBizType(bizType);
        operationLog.setBizId(target.homeworkId() == null ? 0L : target.homeworkId());
        operationLog.setActionType("teacher_message_send");
        operationLog.setRequestPayload("{}");
        operationLog.setResultCode(0);
        operationLog.setCreatedAt(now);
        operationLogMapper.insert(operationLog);

        Long messageId = operationLog.getId();
        Long notifyBizId = target.homeworkId() == null ? messageId : target.homeworkId();
        String channelText = String.join(",", notifyChannels);
        for (Receiver receiver : receivers) {
            NotificationPo notification = new NotificationPo();
            notification.setBizType(bizType);
            notification.setBizId(notifyBizId);
            notification.setReceiverUserId(receiver.userId());
            notification.setReceiverRole(receiver.role());
            notification.setNotifyChannel(channelText);
            notification.setNotifyTitle(notifyTitle);
            notification.setNotifyContent(notifyContent);
            notification.setSendStatus("success");
            notification.setSentAt(now);
            notification.setCreatedAt(now);
            notificationMapper.insert(notification);
        }

        operationLog.setBizId(notifyBizId);
        operationLog.setRequestPayload(buildPayloadJson(
                scopeType,
                notifyTitle,
                notifyContent,
                receiverRole,
                notifyChannels,
                target.classIds(),
                target.classNames(),
                target.homeworkId(),
                target.homeworkTitle(),
                receivers.size(),
                receivers.size(),
                0,
                "success",
                now
        ));
        operationLogMapper.updateById(operationLog);

        MessageCreatedVo vo = new MessageCreatedVo();
        vo.setMessageId(messageId);
        return vo;
    }

    @Override
    @Transactional
    public void deleteMessage(String authorization, Long messageId) {
        UserPo teacherUser = resolveTeacherUser(authorization);
        if (messageId == null) {
            throw new CommonException("\u6d88\u606f\u8bb0\u5f55ID\u4e0d\u80fd\u4e3a\u7a7a");
        }

        OperationLogPo record = operationLogMapper.selectById(messageId);
        if (record == null
                || !"teacher".equalsIgnoreCase(defaultString(record.getOperatorRole(), ""))
                || !"teacher_message_send".equalsIgnoreCase(defaultString(record.getActionType(), ""))) {
            throw new CommonException(40401, "\u6d88\u606f\u8bb0\u5f55\u4e0d\u5b58\u5728");
        }

        if (!Objects.equals(record.getOperatorUserId(), teacherUser.getId())) {
            throw new CommonException(40301, "\u53ea\u80fd\u5220\u9664\u81ea\u5df1\u7684\u6d88\u606f\u8bb0\u5f55");
        }

        operationLogMapper.deleteById(messageId);
    }

    private UserPo resolveTeacherUser(String authorization) {
        return CurrentUserSupport.requireUser(authorization, "teacher", userMapper);
        /* if (parsedToken != null && "teacher".equalsIgnoreCase(parsedToken.roleType())) {
            UserPo user = userMapper.selectById(parsedToken.userId());
            if (isActiveTeacher(user)) {
                return user;
            }
        }

        List<TeacherPo> teachers = teacherMapper.selectList(
                new LambdaQueryWrapper<TeacherPo>()
                        .orderByAsc(TeacherPo::getId)
                        .last("limit 1")
        );
        for (TeacherPo teacher : teachers) {
            UserPo user = userMapper.selectById(teacher.getTeacherUserId());
            if (isActiveTeacher(user)) {
                return user;
            }
        }
        throw new CommonException(40101, "请先以教师身份登录");
    }

        */
    }

    private boolean isActiveTeacher(UserPo user) {
        return CurrentUserSupport.isActiveUser(user, "teacher");
    }

    private MessageTarget resolveTarget(UserPo teacherUser, String scopeType, Long homeworkId, List<Long> requestClassIds) {
        Set<Long> teacherClassIds = teacherClassSubjectMapper.selectList(
                        new LambdaQueryWrapper<TeacherClassSubjectPo>()
                                .eq(TeacherClassSubjectPo::getTeacherId, teacherUser.getId())
                                .eq(TeacherClassSubjectPo::getStatus, "enabled")
                ).stream()
                .map(TeacherClassSubjectPo::getClassId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (teacherClassIds.isEmpty()) {
            throw new CommonException("当前教师还没有绑定可发送消息的班级");
        }

        if ("class".equals(scopeType)) {
            List<Long> classIds = uniqueIds(requestClassIds);
            if (classIds.isEmpty()) {
                throw new CommonException("请选择接收班级");
            }
            if (!teacherClassIds.containsAll(classIds)) {
                throw new CommonException("只能给当前教师已绑定的班级发送消息");
            }
            Map<Long, SchoolClassPo> classMap = loadClassMap(new LinkedHashSet<>(classIds));
            if (classMap.size() != classIds.size()) {
                throw new CommonException("存在无效班级");
            }
            return new MessageTarget(classIds, classIds.stream().map(id -> className(classMap.get(id))).toList(), null, null);
        }

        HomeworkPo homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            throw new CommonException("作业不存在");
        }
        if (!Objects.equals(homework.getCreatorTeacherId(), teacherUser.getId())) {
            throw new CommonException("只能给自己创建的作业发送消息");
        }

        Set<Long> homeworkClassIds = homeworkClassMapper.selectList(
                        new LambdaQueryWrapper<HomeworkClassPo>().eq(HomeworkClassPo::getHomeworkId, homeworkId)
                ).stream()
                .map(HomeworkClassPo::getClassId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (homeworkClassIds.isEmpty()) {
            throw new CommonException("当前作业还没有关联班级");
        }
        if (!teacherClassIds.containsAll(homeworkClassIds)) {
            throw new CommonException("当前作业包含未绑定到该教师的班级，不能发送消息");
        }

        List<Long> classIds = uniqueIds(requestClassIds);
        if (!classIds.isEmpty()) {
            if (!homeworkClassIds.containsAll(classIds)) {
                throw new CommonException("只能选择当前作业已关联的班级");
            }
        } else {
            classIds = new ArrayList<>(homeworkClassIds);
        }

        Map<Long, SchoolClassPo> classMap = loadClassMap(new LinkedHashSet<>(classIds));
        return new MessageTarget(
                classIds,
                classIds.stream().map(id -> className(classMap.get(id))).toList(),
                homeworkId,
                homework.getTitle()
        );
    }

    private List<Receiver> resolveReceivers(List<Long> classIds, String receiverRole) {
        List<StudentPo> students = studentMapper.selectList(
                new LambdaQueryWrapper<StudentPo>()
                        .in(StudentPo::getClassId, classIds)
                        .eq(StudentPo::getStatus, "enabled")
        );
        if (students.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, UserPo> studentUserMap = loadUserMap(students.stream()
                .map(StudentPo::getStudentUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)));

        LinkedHashSet<Receiver> receivers = new LinkedHashSet<>();
        if ("student".equals(receiverRole) || "both".equals(receiverRole)) {
            for (StudentPo student : students) {
                UserPo studentUser = studentUserMap.get(student.getStudentUserId());
                if (isActiveUser(studentUser, "student")) {
                    receivers.add(new Receiver(studentUser.getId(), "student"));
                }
            }
        }

        if ("parent".equals(receiverRole) || "both".equals(receiverRole)) {
            Set<Long> studentIds = students.stream()
                    .map(StudentPo::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            List<ParentStudentPo> relations = parentStudentMapper.selectList(
                    new LambdaQueryWrapper<ParentStudentPo>()
                            .in(ParentStudentPo::getStudentId, studentIds)
                            .eq(ParentStudentPo::getStatus, "enabled")
            );
            Map<Long, UserPo> parentUserMap = loadUserMap(relations.stream()
                    .map(ParentStudentPo::getParentUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
            for (ParentStudentPo relation : relations) {
                UserPo parentUser = parentUserMap.get(relation.getParentUserId());
                if (isActiveUser(parentUser, "parent")) {
                    receivers.add(new Receiver(parentUser.getId(), "parent"));
                }
            }
        }

        return new ArrayList<>(receivers);
    }

    private boolean isActiveUser(UserPo user, String roleType) {
        return user != null
                && roleType.equalsIgnoreCase(user.getRoleType())
                && (!StringUtils.hasText(user.getStatus()) || "enabled".equalsIgnoreCase(user.getStatus()));
    }

    private Map<Long, SchoolClassPo> loadClassMap(Set<Long> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return schoolClassMapper.selectList(
                        new LambdaQueryWrapper<SchoolClassPo>().in(SchoolClassPo::getId, classIds)
                ).stream()
                .collect(Collectors.toMap(SchoolClassPo::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, UserPo> loadUserMap(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectList(
                        new LambdaQueryWrapper<UserPo>().in(UserPo::getId, userIds)
                ).stream()
                .collect(Collectors.toMap(UserPo::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private MessageRecordVo buildMessageRecordVo(OperationLogPo operationLog) {
        JsonNode payload = readPayload(operationLog.getRequestPayload());

        MessageRecordVo vo = new MessageRecordVo();
        vo.setMessageId(operationLog.getId());
        vo.setBizType(defaultString(operationLog.getBizType(), readText(payload, "bizType", "custom_notice")));
        vo.setScopeType(readText(payload, "scopeType", "class"));
        vo.setNotifyTitle(readText(payload, "notifyTitle", ""));
        vo.setNotifyContent(readText(payload, "notifyContent", ""));
        vo.setReceiverRole(readText(payload, "receiverRole", "both"));
        vo.setNotifyChannels(readStringList(payload, "notifyChannels"));
        vo.setClassIds(readLongList(payload, "classIds"));
        vo.setClassNames(readStringList(payload, "classNames"));
        vo.setHomeworkId(readLong(payload, "homeworkId"));
        vo.setHomeworkTitle(readText(payload, "homeworkTitle", ""));
        vo.setReceiverCount(readInteger(payload, "receiverCount", 0));
        vo.setSuccessCount(readInteger(payload, "successCount", 0));
        vo.setFailedCount(readInteger(payload, "failedCount", 0));
        vo.setSendStatus(resolveSendStatus(operationLog, payload));
        vo.setSentAt(readText(payload, "sentAt", formatTime(operationLog.getCreatedAt())));
        vo.setCreatedAt(formatTime(operationLog.getCreatedAt()));
        return vo;
    }

    private boolean matchesKeyword(MessageRecordVo record, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String text = keyword.trim().toLowerCase(Locale.ROOT);
        if (contains(record.getNotifyTitle(), text) || contains(record.getNotifyContent(), text) || contains(record.getHomeworkTitle(), text)) {
            return true;
        }
        return defaultList(record.getClassNames()).stream().anyMatch(item -> contains(item, text));
    }

    private boolean matchesSendStatus(MessageRecordVo record, String sendStatus) {
        if (!StringUtils.hasText(sendStatus)) {
            return true;
        }
        return sendStatus.trim().equalsIgnoreCase(defaultString(record.getSendStatus(), "success"));
    }

    private boolean contains(String source, String keyword) {
        return StringUtils.hasText(source) && StringUtils.hasText(keyword)
                && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private JsonNode readPayload(String payloadText) {
        if (!StringUtils.hasText(payloadText)) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(payloadText);
        } catch (JsonProcessingException e) {
            return objectMapper.createObjectNode();
        }
    }

    private String buildPayloadJson(
            String scopeType,
            String notifyTitle,
            String notifyContent,
            String receiverRole,
            List<String> notifyChannels,
            List<Long> classIds,
            List<String> classNames,
            Long homeworkId,
            String homeworkTitle,
            int receiverCount,
            int successCount,
            int failedCount,
            String sendStatus,
            LocalDateTime sentAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("scopeType", scopeType);
        payload.put("notifyTitle", notifyTitle);
        payload.put("notifyContent", notifyContent);
        payload.put("receiverRole", receiverRole);
        payload.put("notifyChannels", notifyChannels);
        payload.put("classIds", classIds);
        payload.put("classNames", classNames);
        payload.put("homeworkId", homeworkId);
        payload.put("homeworkTitle", homeworkTitle);
        payload.put("receiverCount", receiverCount);
        payload.put("successCount", successCount);
        payload.put("failedCount", failedCount);
        payload.put("sendStatus", sendStatus);
        payload.put("sentAt", formatTime(sentAt));
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new CommonException("消息记录保存失败");
        }
    }

    private String resolveSendStatus(OperationLogPo operationLog, JsonNode payload) {
        String payloadStatus = readText(payload, "sendStatus", "");
        if (StringUtils.hasText(payloadStatus)) {
            return payloadStatus;
        }
        if (operationLog.getResultCode() == null) {
            return "pending";
        }
        return operationLog.getResultCode() == 0 ? "success" : "failed";
    }

    private String readText(JsonNode payload, String fieldName, String defaultValue) {
        JsonNode node = payload.get(fieldName);
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        String value = node.asText();
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private Long readLong(JsonNode payload, String fieldName) {
        JsonNode node = payload.get(fieldName);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.asLong();
        }
        String value = node.asText();
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer readInteger(JsonNode payload, String fieldName, int defaultValue) {
        JsonNode node = payload.get(fieldName);
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        if (node.isNumber()) {
            return node.asInt();
        }
        String value = node.asText();
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private List<String> readStringList(JsonNode payload, String fieldName) {
        JsonNode node = payload.get(fieldName);
        if (node == null || !node.isArray()) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>();
        for (JsonNode item : node) {
            String value = item == null ? "" : item.asText();
            if (StringUtils.hasText(value)) {
                list.add(value.trim());
            }
        }
        return list;
    }

    private List<Long> readLongList(JsonNode payload, String fieldName) {
        JsonNode node = payload.get(fieldName);
        if (node == null || !node.isArray()) {
            return Collections.emptyList();
        }
        List<Long> list = new ArrayList<>();
        for (JsonNode item : node) {
            if (item == null || item.isNull()) {
                continue;
            }
            if (item.isNumber()) {
                list.add(item.asLong());
                continue;
            }
            String text = item.asText();
            if (!StringUtils.hasText(text)) {
                continue;
            }
            try {
                list.add(Long.parseLong(text.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return list;
    }

    private List<Long> uniqueIds(Collection<Long> ids) {
        if (ids == null) {
            return Collections.emptyList();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private List<String> normalizeChannels(List<String> notifyChannels) {
        List<String> channels = defaultList(notifyChannels).stream()
                .filter(StringUtils::hasText)
                .map(item -> item.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
        if (channels.isEmpty()) {
            return List.of("in_app");
        }
        return channels;
    }

    private String normalizeBizType(String bizType) {
        return requiredText(bizType, "消息类型不能为空").toLowerCase(Locale.ROOT);
    }

    private String normalizeScopeType(String scopeType) {
        String value = requiredText(scopeType, "发送范围不能为空").toLowerCase(Locale.ROOT);
        if (!Set.of("class", "homework").contains(value)) {
            throw new CommonException("发送范围只支持 class 或 homework");
        }
        return value;
    }

    private String normalizeReceiverRole(String receiverRole) {
        String value = requiredText(receiverRole, "接收人类型不能为空").toLowerCase(Locale.ROOT);
        if (!Set.of("student", "parent", "both").contains(value)) {
            throw new CommonException("接收人类型只支持 student、parent、both");
        }
        return value;
    }

    private String requiredText(String text, String message) {
        if (!StringUtils.hasText(text)) {
            throw new CommonException(message);
        }
        return text.trim();
    }

    private String className(SchoolClassPo classPo) {
        if (classPo == null || !StringUtils.hasText(classPo.getClassName())) {
            return "";
        }
        return classPo.getClassName().trim();
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "" : TIME_FORMATTER.format(time);
    }

    private String defaultString(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private <T> List<T> defaultList(Collection<T> source) {
        return source == null ? Collections.emptyList() : new ArrayList<>(source);
    }

    private <T> PageSlice<T> slice(List<T> source, Integer pageNo, Integer pageSize) {
        int safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        int total = source == null ? 0 : source.size();
        if (total == 0) {
            return new PageSlice<>(Collections.emptyList(), 0, safePageNo, safePageSize);
        }
        int fromIndex = Math.min((safePageNo - 1) * safePageSize, total);
        int toIndex = Math.min(fromIndex + safePageSize, total);
        return new PageSlice<>(source.subList(fromIndex, toIndex), total, safePageNo, safePageSize);
    }

    private record Receiver(Long userId, String role) {
    }

    private record MessageTarget(List<Long> classIds, List<String> classNames, Long homeworkId, String homeworkTitle) {
    }

    private record PageSlice<T>(List<T> items, long total, int pageNo, int pageSize) {
    }
}
