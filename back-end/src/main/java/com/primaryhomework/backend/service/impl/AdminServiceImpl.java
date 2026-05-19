package com.primaryhomework.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.primaryhomework.backend.entity.dto.admin.AdminClassQueryDto;
import com.primaryhomework.backend.entity.dto.admin.AdminUserQueryDto;
import com.primaryhomework.backend.entity.dto.admin.AdminUserSaveDto;
import com.primaryhomework.backend.entity.dto.admin.ParentRelationSaveDto;
import com.primaryhomework.backend.entity.dto.admin.TeacherRelationSaveDto;
import com.primaryhomework.backend.entity.po.HomeworkPo;
import com.primaryhomework.backend.entity.po.HomeworkTaskPo;
import com.primaryhomework.backend.entity.po.ParentPo;
import com.primaryhomework.backend.entity.po.ParentStudentPo;
import com.primaryhomework.backend.entity.po.SchoolClassPo;
import com.primaryhomework.backend.entity.po.SchoolGradePo;
import com.primaryhomework.backend.entity.po.SchoolPo;
import com.primaryhomework.backend.entity.po.StudentPo;
import com.primaryhomework.backend.entity.po.TeacherClassSubjectPo;
import com.primaryhomework.backend.entity.po.TeacherPo;
import com.primaryhomework.backend.entity.po.UserPo;
import com.primaryhomework.backend.entity.vo.PageDTO;
import com.primaryhomework.backend.entity.vo.admin.AdminClassVo;
import com.primaryhomework.backend.entity.vo.admin.AdminOverviewVo;
import com.primaryhomework.backend.entity.vo.admin.AdminSchoolVo;
import com.primaryhomework.backend.entity.vo.admin.AdminUserVo;
import com.primaryhomework.backend.entity.vo.admin.ParentRelationVo;
import com.primaryhomework.backend.entity.vo.admin.TeacherRelationVo;
import com.primaryhomework.backend.mapper.HomeworkMapper;
import com.primaryhomework.backend.mapper.HomeworkTaskMapper;
import com.primaryhomework.backend.mapper.ParentMapper;
import com.primaryhomework.backend.mapper.ParentStudentMapper;
import com.primaryhomework.backend.mapper.SchoolClassMapper;
import com.primaryhomework.backend.mapper.SchoolGradeMapper;
import com.primaryhomework.backend.mapper.SchoolMapper;
import com.primaryhomework.backend.mapper.StudentMapper;
import com.primaryhomework.backend.mapper.TeacherClassSubjectMapper;
import com.primaryhomework.backend.mapper.TeacherMapper;
import com.primaryhomework.backend.mapper.UserMapper;
import com.primaryhomework.backend.service.AdminService;
import com.primaryhomework.backend.utils.CommonException;
import com.primaryhomework.backend.utils.CurrentUserSupport;
import com.primaryhomework.backend.utils.PasswordSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final Map<String, String> SUBJECT_NAME_MAP = Map.ofEntries(
            Map.entry("chinese", "语文"),
            Map.entry("math", "数学"),
            Map.entry("english", "英语"),
            Map.entry("science", "科学"),
            Map.entry("morality", "道德与法治"),
            Map.entry("moral", "道德与法治"),
            Map.entry("art", "美术"),
            Map.entry("music", "音乐"),
            Map.entry("pe", "体育")
    );

    private static final Map<String, String> RELATION_TYPE_MAP = Map.of(
            "father", "父亲",
            "mother", "母亲",
            "guardian", "监护人",
            "grandparent", "祖辈"
    );

    private final UserMapper userMapper;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final ParentMapper parentMapper;
    private final SchoolMapper schoolMapper;
    private final SchoolGradeMapper schoolGradeMapper;
    private final SchoolClassMapper schoolClassMapper;
    private final TeacherClassSubjectMapper teacherClassSubjectMapper;
    private final ParentStudentMapper parentStudentMapper;
    private final HomeworkMapper homeworkMapper;
    private final HomeworkTaskMapper homeworkTaskMapper;

    @Override
    public AdminOverviewVo getOverview(String authorization) {
        UserPo adminUser = resolveAdminUser(authorization);

        List<HomeworkPo> homeworks = listScopedHomeworks(adminUser);
        Set<Long> homeworkIds = homeworks.stream()
                .map(HomeworkPo::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, HomeworkPo> homeworkMap = homeworks.stream()
                .collect(Collectors.toMap(HomeworkPo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        List<HomeworkTaskPo> tasks = homeworkIds.isEmpty()
                ? Collections.emptyList()
                : homeworkTaskMapper.selectList(
                new LambdaQueryWrapper<HomeworkTaskPo>()
                        .in(HomeworkTaskPo::getHomeworkId, homeworkIds)
                        .eq(HomeworkTaskPo::getIsDeleted, false)
        );

        LocalDate today = LocalDate.now();
        int publishCountToday = (int) homeworks.stream()
                .filter(item -> {
                    LocalDateTime publishTime = item.getPublishedAt() != null ? item.getPublishedAt() : item.getCreatedAt();
                    return publishTime != null && Objects.equals(publishTime.toLocalDate(), today);
                })
                .count();

        long submittedCount = tasks.stream().filter(this::hasSubmission).count();
        long overdueCount = tasks.stream()
                .filter(item -> isOverdueTask(item, homeworkMap.get(item.getHomeworkId())))
                .count();

        List<UserPo> teacherUsers = userMapper.selectList(buildScopedUserWrapper(adminUser, "teacher", "enabled", null));
        List<UserPo> studentUsers = userMapper.selectList(buildScopedUserWrapper(adminUser, "student", "enabled", null));

        AdminOverviewVo vo = new AdminOverviewVo();
        vo.setPublishCountToday(publishCountToday);
        vo.setSubmissionRate(safeRate(submittedCount, tasks.size()));
        vo.setOverdueRate(safeRate(overdueCount, tasks.size()));
        vo.setActiveTeacherCount((int) teacherUsers.stream().filter(item -> item.getLastLoginAt() != null).count());
        vo.setActiveStudentCount((int) studentUsers.stream().filter(item -> item.getLastLoginAt() != null).count());
        return vo;
    }

    @Override
    public List<AdminSchoolVo> listSchools(String authorization) {
        UserPo adminUser = resolveAdminUser(authorization);
        List<SchoolPo> schools = schoolMapper.selectList(
                new LambdaQueryWrapper<SchoolPo>().orderByAsc(SchoolPo::getId)
        ).stream().filter(item -> matchScope(adminUser, item.getId())).toList();
        if (schools.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> schoolIds = schools.stream()
                .map(SchoolPo::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, Integer> gradeCountMap = countByKey(
                schoolGradeMapper.selectList(
                        new LambdaQueryWrapper<SchoolGradePo>()
                                .in(SchoolGradePo::getSchoolId, schoolIds)
                                .eq(SchoolGradePo::getStatus, "enabled")
                ),
                SchoolGradePo::getSchoolId
        );
        Map<Long, Integer> classCountMap = countByKey(
                schoolClassMapper.selectList(
                        new LambdaQueryWrapper<SchoolClassPo>()
                                .in(SchoolClassPo::getSchoolId, schoolIds)
                                .eq(SchoolClassPo::getStatus, "enabled")
                ),
                SchoolClassPo::getSchoolId
        );
        Map<Long, Integer> teacherCountMap = countByKey(
                userMapper.selectList(
                        new LambdaQueryWrapper<UserPo>()
                                .in(UserPo::getSchoolId, schoolIds)
                                .eq(UserPo::getRoleType, "teacher")
                                .eq(UserPo::getStatus, "enabled")
                ),
                UserPo::getSchoolId
        );
        Map<Long, Integer> studentCountMap = countByKey(
                userMapper.selectList(
                        new LambdaQueryWrapper<UserPo>()
                                .in(UserPo::getSchoolId, schoolIds)
                                .eq(UserPo::getRoleType, "student")
                                .eq(UserPo::getStatus, "enabled")
                ),
                UserPo::getSchoolId
        );

        return schools.stream()
                .map(item -> {
                    AdminSchoolVo vo = new AdminSchoolVo();
                    vo.setSchoolId(item.getId());
                    vo.setSchoolName(item.getSchoolName());
                    vo.setSchoolCode(item.getSchoolCode());
                    vo.setStatus(defaultString(item.getStatus(), "enabled"));
                    vo.setGradeCount(gradeCountMap.getOrDefault(item.getId(), 0));
                    vo.setClassCount(classCountMap.getOrDefault(item.getId(), 0));
                    vo.setTeacherCount(teacherCountMap.getOrDefault(item.getId(), 0));
                    vo.setStudentCount(studentCountMap.getOrDefault(item.getId(), 0));
                    return vo;
                })
                .sorted(Comparator.comparing(item -> defaultString(item.getSchoolName(), "")))
                .toList();
    }

    @Override
    public List<AdminClassVo> listClasses(String authorization, AdminClassQueryDto queryDto) {
        UserPo adminUser = resolveAdminUser(authorization);
        AdminClassQueryDto query = queryDto == null ? new AdminClassQueryDto() : queryDto;

        LambdaQueryWrapper<SchoolClassPo> wrapper = new LambdaQueryWrapper<SchoolClassPo>()
                .orderByAsc(SchoolClassPo::getSchoolId)
                .orderByAsc(SchoolClassPo::getGradeId)
                .orderByAsc(SchoolClassPo::getClassName);

        if (query.getSchoolId() != null) {
            requireSchoolInScope(adminUser, query.getSchoolId(), false);
            wrapper.eq(SchoolClassPo::getSchoolId, query.getSchoolId());
        } else if (adminUser.getSchoolId() != null) {
            wrapper.eq(SchoolClassPo::getSchoolId, adminUser.getSchoolId());
        }
        if (query.getGradeId() != null) {
            wrapper.eq(SchoolClassPo::getGradeId, query.getGradeId());
        }

        List<SchoolClassPo> classes = schoolClassMapper.selectList(wrapper);
        if (classes.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> classIds = classes.stream().map(SchoolClassPo::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, SchoolPo> schoolMap = loadSchoolMap(classes.stream().map(SchoolClassPo::getSchoolId).collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, SchoolGradePo> gradeMap = loadGradeMap(classes.stream().map(SchoolClassPo::getGradeId).collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, UserPo> userMap = loadUserMap(classes.stream()
                .map(SchoolClassPo::getHomeroomTeacherId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, Integer> studentCountMap = countByKey(
                studentMapper.selectList(
                        new LambdaQueryWrapper<StudentPo>()
                                .in(StudentPo::getClassId, classIds)
                                .eq(StudentPo::getStatus, "enabled")
                ),
                StudentPo::getClassId
        );

        String keyword = trim(query.getKeyword());
        return classes.stream()
                .map(item -> toAdminClassVo(item, schoolMap, gradeMap, userMap, studentCountMap))
                .filter(item -> matchClassKeyword(item, keyword))
                .toList();
    }

    @Override
    public PageDTO<AdminUserVo> pageUsers(String authorization, AdminUserQueryDto queryDto) {
        UserPo adminUser = resolveAdminUser(authorization);
        AdminUserQueryDto query = queryDto == null ? new AdminUserQueryDto() : queryDto;

        String roleType = trim(query.getRoleType());
        String status = trim(query.getStatus());
        Long schoolId = query.getSchoolId();
        if (schoolId != null) {
            requireSchoolInScope(adminUser, schoolId, false);
        }

        LambdaQueryWrapper<UserPo> wrapper = buildScopedUserWrapper(adminUser, roleType, status, schoolId);
        wrapper.orderByDesc(UserPo::getId);
        List<UserPo> users = userMapper.selectList(wrapper);
        if (users.isEmpty()) {
            return emptyPage(query.getPageNo(), query.getPageSize());
        }

        Set<Long> userIds = users.stream().map(UserPo::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, TeacherPo> teacherMap = teacherMapper.selectList(
                        new LambdaQueryWrapper<TeacherPo>().in(TeacherPo::getTeacherUserId, userIds)
                ).stream()
                .collect(Collectors.toMap(TeacherPo::getTeacherUserId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, StudentPo> studentMap = studentMapper.selectList(
                        new LambdaQueryWrapper<StudentPo>().in(StudentPo::getStudentUserId, userIds)
                ).stream()
                .collect(Collectors.toMap(StudentPo::getStudentUserId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, ParentPo> parentMap = parentMapper.selectList(
                        new LambdaQueryWrapper<ParentPo>().in(ParentPo::getParentUserId, userIds)
                ).stream()
                .collect(Collectors.toMap(ParentPo::getParentUserId, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        Set<Long> schoolIds = new LinkedHashSet<>();
        Set<Long> classIds = new LinkedHashSet<>();
        Set<Long> gradeIds = new LinkedHashSet<>();
        for (UserPo user : users) {
            if (user.getSchoolId() != null) {
                schoolIds.add(user.getSchoolId());
            }
            TeacherPo teacherPo = teacherMap.get(user.getId());
            if (teacherPo != null && teacherPo.getSchoolId() != null) {
                schoolIds.add(teacherPo.getSchoolId());
            }
            StudentPo studentPo = studentMap.get(user.getId());
            if (studentPo != null) {
                if (studentPo.getSchoolId() != null) {
                    schoolIds.add(studentPo.getSchoolId());
                }
                if (studentPo.getClassId() != null) {
                    classIds.add(studentPo.getClassId());
                }
                if (studentPo.getGradeId() != null) {
                    gradeIds.add(studentPo.getGradeId());
                }
            }
            ParentPo parentPo = parentMap.get(user.getId());
            if (parentPo != null && parentPo.getSchoolId() != null) {
                schoolIds.add(parentPo.getSchoolId());
            }
        }

        Map<Long, SchoolPo> schoolMap = loadSchoolMap(schoolIds);
        Map<Long, SchoolClassPo> classMap = loadClassMap(classIds);
        for (SchoolClassPo classPo : classMap.values()) {
            if (classPo.getGradeId() != null) {
                gradeIds.add(classPo.getGradeId());
            }
        }
        Map<Long, SchoolGradePo> gradeMap = loadGradeMap(gradeIds);

        String keyword = trim(query.getKeyword()).toLowerCase(Locale.ROOT);
        List<AdminUserVo> list = users.stream()
                .map(item -> toAdminUserVo(item, teacherMap, studentMap, parentMap, schoolMap, classMap, gradeMap))
                .filter(item -> matchUserKeyword(item, keyword))
                .toList();

        PageSlice<AdminUserVo> pageSlice = slice(list, query.getPageNo(), query.getPageSize());
        return PageDTO.of(pageSlice.items(), pageSlice.total(), pageSlice.pageNo(), pageSlice.pageSize());
    }

    @Override
    @Transactional
    public void createUser(String authorization, AdminUserSaveDto saveDto) {
        UserPo adminUser = resolveAdminUser(authorization);
        if (saveDto == null) {
            throw new CommonException("请求体不能为空");
        }

        String roleType = normalizeRoleType(saveDto.getRoleType());
        String userName = requiredText(saveDto.getUserName(), "userName不能为空");
        String password = requiredText(saveDto.getPassword(), "password不能为空");
        Long schoolId = saveDto.getSchoolId();

        if (!"admin".equals(roleType) && schoolId == null) {
            throw new CommonException("非管理员账号必须选择学校");
        }
        if (schoolId != null) {
            requireSchoolInScope(adminUser, schoolId, true);
        }

        UserPo user = new UserPo();
        user.setUserName(userName);
        user.setRoleType(roleType);
        user.setSchoolId(schoolId);
        user.setStatus(normalizeStatus(saveDto.getStatus()));
        user.setPasswordHash(PasswordSupport.encode(password));

        try {
            if ("admin".equals(roleType)) {
                String loginName = requiredText(saveDto.getLoginName(), "管理员账号必须填写登录名");
                ensureAdminLoginNameUnique(loginName, null);
                user.setLoginName(loginName);
            }

            userMapper.insert(user);

            Long finalSchoolId = user.getSchoolId();
            if ("teacher".equals(roleType)) {
                finalSchoolId = saveTeacherProfile(adminUser, user, schoolId, saveDto.getProfile(), true);
            } else if ("student".equals(roleType)) {
                finalSchoolId = saveStudentProfile(adminUser, user, schoolId, saveDto.getProfile(), true);
            } else if ("parent".equals(roleType)) {
                finalSchoolId = saveParentProfile(adminUser, user, schoolId, saveDto.getProfile(), true);
            }

            if (!Objects.equals(user.getSchoolId(), finalSchoolId)) {
                user.setSchoolId(finalSchoolId);
                userMapper.updateById(user);
            }
        } catch (DuplicateKeyException e) {
            throw new CommonException("账号、手机号、学号或关系数据重复");
        }
    }

    @Override
    @Transactional
    public void updateUser(String authorization, Long userId, AdminUserSaveDto saveDto) {
        UserPo adminUser = resolveAdminUser(authorization);
        if (userId == null) {
            throw new CommonException("userId不能为空");
        }
        UserPo user = userMapper.selectById(userId);
        if (user == null) {
            throw new CommonException(40401, "账号不存在");
        }
        requireUserInScope(adminUser, user);

        if (saveDto == null) {
            throw new CommonException("请求体不能为空");
        }

        if (StringUtils.hasText(saveDto.getRoleType())) {
            String roleType = normalizeRoleType(saveDto.getRoleType());
            if (!roleType.equalsIgnoreCase(user.getRoleType())) {
                throw new CommonException("暂不支持修改账号角色");
            }
        }

        Long resolvedSchoolId = user.getSchoolId();
        if (saveDto.getSchoolId() != null) {
            requireSchoolInScope(adminUser, saveDto.getSchoolId(), true);
            resolvedSchoolId = saveDto.getSchoolId();
            user.setSchoolId(resolvedSchoolId);
        }

        if (StringUtils.hasText(saveDto.getUserName())) {
            user.setUserName(saveDto.getUserName().trim());
        }
        if (StringUtils.hasText(saveDto.getStatus())) {
            user.setStatus(normalizeStatus(saveDto.getStatus()));
        }
        if (StringUtils.hasText(saveDto.getPassword())) {
            user.setPasswordHash(PasswordSupport.encode(saveDto.getPassword().trim()));
        }

        try {
            if ("admin".equalsIgnoreCase(user.getRoleType()) && StringUtils.hasText(saveDto.getLoginName())) {
                String loginName = saveDto.getLoginName().trim();
                ensureAdminLoginNameUnique(loginName, user.getId());
                user.setLoginName(loginName);
            }

            userMapper.updateById(user);

            if ("teacher".equalsIgnoreCase(user.getRoleType())) {
                resolvedSchoolId = saveTeacherProfile(adminUser, user, resolvedSchoolId, saveDto.getProfile(), false);
            } else if ("student".equalsIgnoreCase(user.getRoleType())) {
                resolvedSchoolId = saveStudentProfile(adminUser, user, resolvedSchoolId, saveDto.getProfile(), false);
            } else if ("parent".equalsIgnoreCase(user.getRoleType())) {
                resolvedSchoolId = saveParentProfile(adminUser, user, resolvedSchoolId, saveDto.getProfile(), false);
            }

            if (!Objects.equals(user.getSchoolId(), resolvedSchoolId)) {
                user.setSchoolId(resolvedSchoolId);
                userMapper.updateById(user);
            }
        } catch (DuplicateKeyException e) {
            throw new CommonException("账号、手机号、学号或关系数据重复");
        }
    }

    @Override
    public List<TeacherRelationVo> listTeacherRelations(String authorization) {
        UserPo adminUser = resolveAdminUser(authorization);
        List<TeacherClassSubjectPo> relations = teacherClassSubjectMapper.selectList(
                new LambdaQueryWrapper<TeacherClassSubjectPo>()
                        .orderByAsc(TeacherClassSubjectPo::getClassId)
                        .orderByAsc(TeacherClassSubjectPo::getSubjectCode)
                        .orderByAsc(TeacherClassSubjectPo::getId)
        );
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, SchoolClassPo> classMap = loadClassMap(relations.stream()
                .map(TeacherClassSubjectPo::getClassId)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, UserPo> teacherUserMap = loadUserMap(relations.stream()
                .map(TeacherClassSubjectPo::getTeacherId)
                .collect(Collectors.toCollection(LinkedHashSet::new)));

        return relations.stream()
                .filter(item -> {
                    SchoolClassPo classPo = classMap.get(item.getClassId());
                    return classPo != null && matchScope(adminUser, classPo.getSchoolId());
                })
                .map(item -> {
                    SchoolClassPo classPo = classMap.get(item.getClassId());
                    UserPo teacherUser = teacherUserMap.get(item.getTeacherId());

                    TeacherRelationVo vo = new TeacherRelationVo();
                    vo.setId(item.getId());
                    vo.setTeacherId(item.getTeacherId());
                    vo.setTeacherName(teacherUser == null ? null : teacherUser.getUserName());
                    vo.setClassId(item.getClassId());
                    vo.setClassName(classPo == null ? null : classPo.getClassName());
                    vo.setSubjectCode(canonicalSubjectCode(item.getSubjectCode()));
                    vo.setSubjectName(subjectName(item.getSubjectCode()));
                    vo.setIsHeadTeacher(Boolean.TRUE.equals(item.getIsHeadTeacher()));
                    vo.setStatus(defaultString(item.getStatus(), "enabled"));
                    return vo;
                })
                .toList();
    }

    @Override
    @Transactional
    public void saveTeacherRelation(String authorization, TeacherRelationSaveDto saveDto) {
        UserPo adminUser = resolveAdminUser(authorization);
        if (saveDto == null) {
            throw new CommonException("请求体不能为空");
        }

        UserPo teacherUser = requireRoleUser(saveDto.getTeacherId(), "teacher");
        requireUserInScope(adminUser, teacherUser);
        TeacherPo teacherPo = teacherMapper.selectOne(
                new LambdaQueryWrapper<TeacherPo>()
                        .eq(TeacherPo::getTeacherUserId, teacherUser.getId())
                        .last("limit 1")
        );
        if (teacherPo == null) {
            throw new CommonException("教师资料不存在");
        }

        SchoolClassPo classPo = requireClassInScope(adminUser, saveDto.getClassId(), true);
        if (!Objects.equals(classPo.getSchoolId(), teacherPo.getSchoolId())) {
            throw new CommonException("教师和班级不属于同一所学校");
        }

        String subjectCode = normalizeSubjectCode(saveDto.getSubjectCode());
        Set<String> aliases = subjectAliases(subjectCode);

        TeacherClassSubjectPo relation = teacherClassSubjectMapper.selectOne(
                new LambdaQueryWrapper<TeacherClassSubjectPo>()
                        .eq(TeacherClassSubjectPo::getTeacherId, teacherUser.getId())
                        .eq(TeacherClassSubjectPo::getClassId, classPo.getId())
                        .in(TeacherClassSubjectPo::getSubjectCode, aliases)
                        .last("limit 1")
        );
        TeacherClassSubjectPo occupied = teacherClassSubjectMapper.selectOne(
                new LambdaQueryWrapper<TeacherClassSubjectPo>()
                        .eq(TeacherClassSubjectPo::getClassId, classPo.getId())
                        .in(TeacherClassSubjectPo::getSubjectCode, aliases)
                        .eq(TeacherClassSubjectPo::getStatus, "enabled")
                        .last("limit 1")
        );
        if (occupied != null && !Objects.equals(occupied.getTeacherId(), teacherUser.getId())) {
            throw new CommonException("该班级的该学科已绑定其他老师");
        }

        if (Boolean.TRUE.equals(saveDto.getIsHeadTeacher())
                && !Objects.equals(classPo.getHomeroomTeacherId(), teacherUser.getId())) {
            classPo.setHomeroomTeacherId(teacherUser.getId());
            schoolClassMapper.updateById(classPo);
        }

        if (relation == null) {
            relation = new TeacherClassSubjectPo();
            relation.setTeacherId(teacherUser.getId());
            relation.setClassId(classPo.getId());
            relation.setSubjectCode(subjectCode);
            relation.setStatus("enabled");
            relation.setIsHeadTeacher(Objects.equals(classPo.getHomeroomTeacherId(), teacherUser.getId()));
            teacherClassSubjectMapper.insert(relation);
        } else {
            relation.setSubjectCode(subjectCode);
            relation.setStatus("enabled");
            relation.setIsHeadTeacher(Objects.equals(classPo.getHomeroomTeacherId(), teacherUser.getId()));
            teacherClassSubjectMapper.updateById(relation);
        }

        syncHeadTeacherFlags(classPo.getId(), classPo.getHomeroomTeacherId());
    }

    @Override
    public List<ParentRelationVo> listParentRelations(String authorization) {
        UserPo adminUser = resolveAdminUser(authorization);
        List<ParentStudentPo> relations = parentStudentMapper.selectList(
                new LambdaQueryWrapper<ParentStudentPo>()
                        .orderByAsc(ParentStudentPo::getStudentId)
                        .orderByAsc(ParentStudentPo::getId)
        );
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, StudentPo> studentMap = loadStudentProfileMap(relations.stream()
                .map(ParentStudentPo::getStudentId)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, UserPo> parentUserMap = loadUserMap(relations.stream()
                .map(ParentStudentPo::getParentUserId)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, UserPo> studentUserMap = loadUserMap(studentMap.values().stream()
                .map(StudentPo::getStudentUserId)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, SchoolClassPo> classMap = loadClassMap(studentMap.values().stream()
                .map(StudentPo::getClassId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        Map<Long, ParentPo> parentMap = parentMapper.selectList(
                        new LambdaQueryWrapper<ParentPo>().in(ParentPo::getParentUserId, parentUserMap.keySet())
                ).stream()
                .collect(Collectors.toMap(ParentPo::getParentUserId, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        return relations.stream()
                .filter(item -> {
                    StudentPo studentPo = studentMap.get(item.getStudentId());
                    return studentPo != null && matchScope(adminUser, studentPo.getSchoolId());
                })
                .map(item -> {
                    StudentPo studentPo = studentMap.get(item.getStudentId());
                    UserPo parentUser = parentUserMap.get(item.getParentUserId());
                    UserPo studentUser = studentPo == null ? null : studentUserMap.get(studentPo.getStudentUserId());
                    SchoolClassPo classPo = studentPo == null ? null : classMap.get(studentPo.getClassId());
                    ParentPo parentPo = parentMap.get(item.getParentUserId());

                    ParentRelationVo vo = new ParentRelationVo();
                    vo.setId(item.getId());
                    vo.setParentUserId(item.getParentUserId());
                    vo.setParentName(parentUser == null ? null : parentUser.getUserName());
                    vo.setParentMobile(parentPo == null ? null : parentPo.getMobile());
                    vo.setStudentId(studentPo == null ? null : studentPo.getStudentUserId());
                    vo.setStudentName(studentUser == null ? null : studentUser.getUserName());
                    vo.setClassId(studentPo == null ? null : studentPo.getClassId());
                    vo.setClassName(classPo == null ? null : classPo.getClassName());
                    vo.setRelationType(defaultString(item.getRelationType(), "guardian"));
                    vo.setIsPrimary(Boolean.TRUE.equals(item.getIsPrimary()));
                    vo.setStatus(defaultString(item.getStatus(), "enabled"));
                    return vo;
                })
                .toList();
    }

    @Override
    @Transactional
    public void saveParentRelation(String authorization, ParentRelationSaveDto saveDto) {
        UserPo adminUser = resolveAdminUser(authorization);
        if (saveDto == null) {
            throw new CommonException("请求体不能为空");
        }

        UserPo parentUser = requireRoleUser(saveDto.getParentUserId(), "parent");
        requireUserInScope(adminUser, parentUser);

        StudentPo studentPo = studentMapper.selectOne(
                new LambdaQueryWrapper<StudentPo>()
                        .eq(StudentPo::getStudentUserId, saveDto.getStudentId())
                        .last("limit 1")
        );
        if (studentPo == null) {
            throw new CommonException("学生资料不存在");
        }
        if (!matchScope(adminUser, studentPo.getSchoolId())) {
            throw new CommonException(40301, "没有权限绑定该学生");
        }
        if (!Objects.equals(parentUser.getSchoolId(), studentPo.getSchoolId())) {
            throw new CommonException("家长和学生不属于同一所学校");
        }

        ParentStudentPo relation = parentStudentMapper.selectOne(
                new LambdaQueryWrapper<ParentStudentPo>()
                        .eq(ParentStudentPo::getParentUserId, parentUser.getId())
                        .eq(ParentStudentPo::getStudentId, studentPo.getId())
                        .last("limit 1")
        );

        if (Boolean.TRUE.equals(saveDto.getIsPrimary())) {
            List<ParentStudentPo> list = parentStudentMapper.selectList(
                    new LambdaQueryWrapper<ParentStudentPo>()
                            .eq(ParentStudentPo::getStudentId, studentPo.getId())
            );
            for (ParentStudentPo item : list) {
                if (Boolean.TRUE.equals(item.getIsPrimary())) {
                    item.setIsPrimary(false);
                    parentStudentMapper.updateById(item);
                }
            }
        }

        if (relation == null) {
            relation = new ParentStudentPo();
            relation.setParentUserId(parentUser.getId());
            relation.setStudentId(studentPo.getId());
            relation.setRelationType(normalizeRelationType(saveDto.getRelationType()));
            relation.setIsPrimary(Boolean.TRUE.equals(saveDto.getIsPrimary()));
            relation.setStatus("enabled");
            parentStudentMapper.insert(relation);
        } else {
            relation.setRelationType(normalizeRelationType(saveDto.getRelationType()));
            relation.setIsPrimary(Boolean.TRUE.equals(saveDto.getIsPrimary()));
            relation.setStatus("enabled");
            parentStudentMapper.updateById(relation);
        }
    }

    private UserPo resolveAdminUser(String authorization) {
        return CurrentUserSupport.requireUser(authorization, "admin", userMapper);
        /* List<UserPo> admins = userMapper.selectList(
                new LambdaQueryWrapper<UserPo>()
                        .eq(UserPo::getRoleType, "admin")
                        .orderByAsc(UserPo::getId)
        );
        for (UserPo admin : admins) {
            if (isActiveAdmin(admin)) {
                return admin;
            }
        }
        throw new CommonException(40101, "请先以管理员身份登录");
    }

        */
    }

    private boolean isActiveAdmin(UserPo user) {
        return CurrentUserSupport.isActiveUser(user, "admin");
    }

    private LambdaQueryWrapper<UserPo> buildScopedUserWrapper(UserPo adminUser, String roleType, String status, Long schoolId) {
        LambdaQueryWrapper<UserPo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(roleType)) {
            wrapper.eq(UserPo::getRoleType, roleType.trim().toLowerCase(Locale.ROOT));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(UserPo::getStatus, normalizeStatus(status));
        }

        Long scopedSchoolId = schoolId;
        if (scopedSchoolId == null && adminUser.getSchoolId() != null) {
            scopedSchoolId = adminUser.getSchoolId();
        }
        if (scopedSchoolId != null) {
            wrapper.eq(UserPo::getSchoolId, scopedSchoolId);
        }
        return wrapper;
    }

    private List<HomeworkPo> listScopedHomeworks(UserPo adminUser) {
        LambdaQueryWrapper<HomeworkPo> wrapper = new LambdaQueryWrapper<>();
        if (adminUser.getSchoolId() != null) {
            wrapper.eq(HomeworkPo::getSchoolId, adminUser.getSchoolId());
        }
        wrapper.orderByDesc(HomeworkPo::getId);
        return homeworkMapper.selectList(wrapper);
    }

    private AdminClassVo toAdminClassVo(
            SchoolClassPo classPo,
            Map<Long, SchoolPo> schoolMap,
            Map<Long, SchoolGradePo> gradeMap,
            Map<Long, UserPo> userMap,
            Map<Long, Integer> studentCountMap
    ) {
        SchoolPo schoolPo = schoolMap.get(classPo.getSchoolId());
        SchoolGradePo gradePo = gradeMap.get(classPo.getGradeId());
        UserPo homeroomTeacher = userMap.get(classPo.getHomeroomTeacherId());

        AdminClassVo vo = new AdminClassVo();
        vo.setClassId(classPo.getId());
        vo.setSchoolId(classPo.getSchoolId());
        vo.setSchoolName(schoolPo == null ? null : schoolPo.getSchoolName());
        vo.setGradeId(classPo.getGradeId());
        vo.setGradeName(gradePo == null ? null : gradePo.getGradeName());
        vo.setClassName(classPo.getClassName());
        vo.setClassCode(classPo.getClassCode());
        vo.setHomeroomTeacherId(classPo.getHomeroomTeacherId());
        vo.setHomeroomTeacherName(homeroomTeacher == null ? null : homeroomTeacher.getUserName());
        vo.setStudentCount(studentCountMap.getOrDefault(classPo.getId(), 0));
        vo.setStatus(defaultString(classPo.getStatus(), "enabled"));
        return vo;
    }

    private AdminUserVo toAdminUserVo(
            UserPo user,
            Map<Long, TeacherPo> teacherMap,
            Map<Long, StudentPo> studentMap,
            Map<Long, ParentPo> parentMap,
            Map<Long, SchoolPo> schoolMap,
            Map<Long, SchoolClassPo> classMap,
            Map<Long, SchoolGradePo> gradeMap
    ) {
        TeacherPo teacherPo = teacherMap.get(user.getId());
        StudentPo studentPo = studentMap.get(user.getId());
        ParentPo parentPo = parentMap.get(user.getId());

        Long schoolId = resolveSchoolId(user, teacherPo, studentPo, parentPo);
        SchoolClassPo classPo = studentPo == null ? null : classMap.get(studentPo.getClassId());
        Long gradeId = studentPo == null ? null : studentPo.getGradeId();
        if (gradeId == null && classPo != null) {
            gradeId = classPo.getGradeId();
        }

        AdminUserVo vo = new AdminUserVo();
        vo.setUserId(user.getId());
        vo.setUserName(user.getUserName());
        vo.setRoleType(user.getRoleType());
        vo.setSchoolId(schoolId);
        vo.setSchoolName(resolveSchoolName(schoolId, schoolMap));
        vo.setStatus(defaultString(user.getStatus(), "enabled"));
        vo.setLoginName(user.getLoginName());

        if (teacherPo != null) {
            vo.setMobile(teacherPo.getMobile());
            vo.setTeacherNo(teacherPo.getTeacherNo());
            vo.setAccount(firstText(teacherPo.getMobile(), teacherPo.getTeacherNo(), user.getLoginName()));
        } else if (studentPo != null) {
            vo.setStudentNo(studentPo.getStudentNo());
            vo.setClassId(studentPo.getClassId());
            vo.setClassName(classPo == null ? null : classPo.getClassName());
            vo.setGradeId(gradeId);
            vo.setGradeName(resolveGradeName(gradeId, gradeMap));
            vo.setAccount(firstText(studentPo.getStudentNo(), user.getLoginName()));
        } else if (parentPo != null) {
            vo.setMobile(parentPo.getMobile());
            vo.setAccount(firstText(parentPo.getMobile(), user.getLoginName()));
        } else {
            vo.setAccount(firstText(user.getLoginName()));
        }

        return vo;
    }

    private Long saveTeacherProfile(
            UserPo adminUser,
            UserPo user,
            Long schoolId,
            AdminUserSaveDto.Profile profile,
            boolean create
    ) {
        TeacherPo teacherPo = teacherMapper.selectOne(
                new LambdaQueryWrapper<TeacherPo>()
                        .eq(TeacherPo::getTeacherUserId, user.getId())
                        .last("limit 1")
        );
        if (teacherPo == null) {
            teacherPo = new TeacherPo();
            teacherPo.setTeacherUserId(user.getId());
        }

        Long finalSchoolId = schoolId != null ? schoolId : teacherPo.getSchoolId();
        if (finalSchoolId == null) {
            throw new CommonException("教师账号必须绑定学校");
        }
        requireSchoolInScope(adminUser, finalSchoolId, true);

        if (profile != null && StringUtils.hasText(profile.getMobile())) {
            String mobile = profile.getMobile().trim();
            ensureTeacherMobileUnique(mobile, teacherPo.getId());
            teacherPo.setMobile(mobile);
        }
        if (profile != null && StringUtils.hasText(profile.getTeacherNo())) {
            String teacherNo = profile.getTeacherNo().trim();
            ensureTeacherNoUnique(finalSchoolId, teacherNo, teacherPo.getId());
            teacherPo.setTeacherNo(teacherNo);
        }

        teacherPo.setSchoolId(finalSchoolId);
        if (!StringUtils.hasText(teacherPo.getMobile())) {
            throw new CommonException("教师账号必须填写手机号");
        }

        if (teacherPo.getId() == null) {
            teacherMapper.insert(teacherPo);
        } else {
            teacherMapper.updateById(teacherPo);
        }
        return finalSchoolId;
    }

    private Long saveParentProfile(
            UserPo adminUser,
            UserPo user,
            Long schoolId,
            AdminUserSaveDto.Profile profile,
            boolean create
    ) {
        ParentPo parentPo = parentMapper.selectOne(
                new LambdaQueryWrapper<ParentPo>()
                        .eq(ParentPo::getParentUserId, user.getId())
                        .last("limit 1")
        );
        if (parentPo == null) {
            parentPo = new ParentPo();
            parentPo.setParentUserId(user.getId());
        }

        Long finalSchoolId = schoolId != null ? schoolId : parentPo.getSchoolId();
        if (finalSchoolId == null) {
            throw new CommonException("家长账号必须绑定学校");
        }
        requireSchoolInScope(adminUser, finalSchoolId, true);

        if (profile != null && StringUtils.hasText(profile.getMobile())) {
            String mobile = profile.getMobile().trim();
            ensureParentMobileUnique(mobile, parentPo.getId());
            parentPo.setMobile(mobile);
        }

        parentPo.setSchoolId(finalSchoolId);
        if (!StringUtils.hasText(parentPo.getMobile())) {
            throw new CommonException("家长账号必须填写手机号");
        }

        if (parentPo.getId() == null) {
            parentMapper.insert(parentPo);
        } else {
            parentMapper.updateById(parentPo);
        }
        return finalSchoolId;
    }

    private Long saveStudentProfile(
            UserPo adminUser,
            UserPo user,
            Long schoolId,
            AdminUserSaveDto.Profile profile,
            boolean create
    ) {
        StudentPo studentPo = studentMapper.selectOne(
                new LambdaQueryWrapper<StudentPo>()
                        .eq(StudentPo::getStudentUserId, user.getId())
                        .last("limit 1")
        );
        if (studentPo == null) {
            studentPo = new StudentPo();
            studentPo.setStudentUserId(user.getId());
        }

        Long finalSchoolId = schoolId != null ? schoolId : studentPo.getSchoolId();
        Long classId = studentPo.getClassId();
        Long gradeId = studentPo.getGradeId();

        if (profile != null && profile.getClassId() != null) {
            SchoolClassPo classPo = requireClassInScope(adminUser, profile.getClassId(), true);
            if (finalSchoolId != null && !Objects.equals(finalSchoolId, classPo.getSchoolId())) {
                throw new CommonException("学生所在学校和班级不一致");
            }
            finalSchoolId = classPo.getSchoolId();
            classId = classPo.getId();
            gradeId = classPo.getGradeId();
        } else if (schoolId != null && classId != null) {
            SchoolClassPo classPo = schoolClassMapper.selectById(classId);
            if (classPo != null && !Objects.equals(classPo.getSchoolId(), schoolId)) {
                throw new CommonException("变更学校后请重新选择班级");
            }
        }

        if (profile != null && profile.getGradeId() != null && classId == null) {
            gradeId = profile.getGradeId();
        }
        if (profile != null && StringUtils.hasText(profile.getStudentNo())) {
            studentPo.setStudentNo(profile.getStudentNo().trim());
        }

        if (finalSchoolId == null) {
            throw new CommonException("学生账号必须绑定学校");
        }
        requireSchoolInScope(adminUser, finalSchoolId, true);
        if (!StringUtils.hasText(studentPo.getStudentNo())) {
            throw new CommonException("学生账号必须填写学号");
        }
        if (classId == null) {
            throw new CommonException("学生账号必须绑定班级");
        }

        ensureStudentNoUnique(finalSchoolId, studentPo.getStudentNo(), studentPo.getId());

        studentPo.setSchoolId(finalSchoolId);
        studentPo.setClassId(classId);
        studentPo.setGradeId(gradeId);
        studentPo.setStatus(defaultString(user.getStatus(), "enabled"));

        if (studentPo.getId() == null) {
            studentMapper.insert(studentPo);
        } else {
            studentMapper.updateById(studentPo);
        }
        return finalSchoolId;
    }

    private void ensureAdminLoginNameUnique(String loginName, Long excludeUserId) {
        LambdaQueryWrapper<UserPo> wrapper = new LambdaQueryWrapper<UserPo>()
                .eq(UserPo::getLoginName, loginName);
        if (excludeUserId != null) {
            wrapper.ne(UserPo::getId, excludeUserId);
        }
        if (userMapper.selectCount(wrapper) > 0) {
            throw new CommonException("管理员登录名已存在");
        }
    }

    private void ensureTeacherMobileUnique(String mobile, Long excludeTeacherId) {
        LambdaQueryWrapper<TeacherPo> wrapper = new LambdaQueryWrapper<TeacherPo>()
                .eq(TeacherPo::getMobile, mobile);
        if (excludeTeacherId != null) {
            wrapper.ne(TeacherPo::getId, excludeTeacherId);
        }
        if (teacherMapper.selectCount(wrapper) > 0) {
            throw new CommonException("教师手机号已存在");
        }
    }

    private void ensureTeacherNoUnique(Long schoolId, String teacherNo, Long excludeTeacherId) {
        if (!StringUtils.hasText(teacherNo)) {
            return;
        }
        LambdaQueryWrapper<TeacherPo> wrapper = new LambdaQueryWrapper<TeacherPo>()
                .eq(TeacherPo::getSchoolId, schoolId)
                .eq(TeacherPo::getTeacherNo, teacherNo);
        if (excludeTeacherId != null) {
            wrapper.ne(TeacherPo::getId, excludeTeacherId);
        }
        if (teacherMapper.selectCount(wrapper) > 0) {
            throw new CommonException("教师工号已存在");
        }
    }

    private void ensureParentMobileUnique(String mobile, Long excludeParentId) {
        LambdaQueryWrapper<ParentPo> wrapper = new LambdaQueryWrapper<ParentPo>()
                .eq(ParentPo::getMobile, mobile);
        if (excludeParentId != null) {
            wrapper.ne(ParentPo::getId, excludeParentId);
        }
        if (parentMapper.selectCount(wrapper) > 0) {
            throw new CommonException("家长手机号已存在");
        }
    }

    private void ensureStudentNoUnique(Long schoolId, String studentNo, Long excludeStudentId) {
        LambdaQueryWrapper<StudentPo> wrapper = new LambdaQueryWrapper<StudentPo>()
                .eq(StudentPo::getSchoolId, schoolId)
                .eq(StudentPo::getStudentNo, studentNo);
        if (excludeStudentId != null) {
            wrapper.ne(StudentPo::getId, excludeStudentId);
        }
        if (studentMapper.selectCount(wrapper) > 0) {
            throw new CommonException("学生学号已存在");
        }
    }

    private UserPo requireRoleUser(Long userId, String roleType) {
        if (userId == null) {
            throw new CommonException("用户ID不能为空");
        }
        UserPo user = userMapper.selectById(userId);
        if (user == null) {
            throw new CommonException(40401, "用户不存在");
        }
        if (!roleType.equalsIgnoreCase(user.getRoleType())) {
            throw new CommonException("用户角色不匹配");
        }
        if (!isEnabled(user.getStatus())) {
            throw new CommonException("用户已停用");
        }
        return user;
    }

    private void requireUserInScope(UserPo adminUser, UserPo user) {
        if (!matchScope(adminUser, user.getSchoolId())) {
            throw new CommonException(40301, "没有权限操作该学校的数据");
        }
    }

    private SchoolPo requireSchoolInScope(UserPo adminUser, Long schoolId, boolean mustExist) {
        if (schoolId == null) {
            if (mustExist) {
                throw new CommonException("schoolId不能为空");
            }
            return null;
        }
        if (!matchScope(adminUser, schoolId)) {
            throw new CommonException(40301, "没有权限操作该学校的数据");
        }
        SchoolPo schoolPo = schoolMapper.selectById(schoolId);
        if (schoolPo == null) {
            throw new CommonException("学校不存在");
        }
        return schoolPo;
    }

    private SchoolClassPo requireClassInScope(UserPo adminUser, Long classId, boolean requireEnabled) {
        if (classId == null) {
            throw new CommonException("classId不能为空");
        }
        SchoolClassPo classPo = schoolClassMapper.selectById(classId);
        if (classPo == null) {
            throw new CommonException("班级不存在");
        }
        if (!matchScope(adminUser, classPo.getSchoolId())) {
            throw new CommonException(40301, "没有权限操作该班级");
        }
        if (requireEnabled && !isEnabled(classPo.getStatus())) {
            throw new CommonException("班级已停用");
        }
        return classPo;
    }

    private boolean matchScope(UserPo adminUser, Long schoolId) {
        return adminUser.getSchoolId() == null || Objects.equals(adminUser.getSchoolId(), schoolId);
    }

    private Map<Long, SchoolPo> loadSchoolMap(Set<Long> schoolIds) {
        if (schoolIds == null || schoolIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return schoolMapper.selectList(
                        new LambdaQueryWrapper<SchoolPo>().in(SchoolPo::getId, schoolIds)
                ).stream()
                .collect(Collectors.toMap(SchoolPo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, SchoolGradePo> loadGradeMap(Set<Long> gradeIds) {
        if (gradeIds == null || gradeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return schoolGradeMapper.selectList(
                        new LambdaQueryWrapper<SchoolGradePo>().in(SchoolGradePo::getId, gradeIds)
                ).stream()
                .collect(Collectors.toMap(SchoolGradePo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, SchoolClassPo> loadClassMap(Set<Long> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return schoolClassMapper.selectList(
                        new LambdaQueryWrapper<SchoolClassPo>().in(SchoolClassPo::getId, classIds)
                ).stream()
                .collect(Collectors.toMap(SchoolClassPo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, UserPo> loadUserMap(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectList(
                        new LambdaQueryWrapper<UserPo>().in(UserPo::getId, userIds)
                ).stream()
                .collect(Collectors.toMap(UserPo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, StudentPo> loadStudentProfileMap(Set<Long> studentProfileIds) {
        if (studentProfileIds == null || studentProfileIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return studentMapper.selectList(
                        new LambdaQueryWrapper<StudentPo>().in(StudentPo::getId, studentProfileIds)
                ).stream()
                .collect(Collectors.toMap(StudentPo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private Long resolveSchoolId(UserPo user, TeacherPo teacherPo, StudentPo studentPo, ParentPo parentPo) {
        if (user.getSchoolId() != null) {
            return user.getSchoolId();
        }
        if (teacherPo != null) {
            return teacherPo.getSchoolId();
        }
        if (studentPo != null) {
            return studentPo.getSchoolId();
        }
        if (parentPo != null) {
            return parentPo.getSchoolId();
        }
        return null;
    }

    private boolean matchClassKeyword(AdminClassVo item, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        return contains(item.getClassName(), normalized)
                || contains(item.getSchoolName(), normalized)
                || contains(item.getGradeName(), normalized)
                || contains(item.getHomeroomTeacherName(), normalized)
                || contains(item.getClassCode(), normalized);
    }

    private boolean matchUserKeyword(AdminUserVo item, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        return contains(item.getUserName(), keyword)
                || contains(item.getAccount(), keyword)
                || contains(item.getLoginName(), keyword)
                || contains(item.getMobile(), keyword)
                || contains(item.getTeacherNo(), keyword)
                || contains(item.getStudentNo(), keyword)
                || contains(item.getClassName(), keyword)
                || contains(item.getGradeName(), keyword)
                || contains(item.getSchoolName(), keyword);
    }

    private boolean contains(String source, String keyword) {
        return StringUtils.hasText(source)
                && StringUtils.hasText(keyword)
                && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private boolean hasSubmission(HomeworkTaskPo taskPo) {
        return taskPo.getLatestSubmissionId() != null || defaultInteger(taskPo.getSubmissionCount(), 0) > 0;
    }

    private boolean isOverdueTask(HomeworkTaskPo taskPo, HomeworkPo homeworkPo) {
        if ("overdue".equalsIgnoreCase(taskPo.getTaskStatus())) {
            return true;
        }
        return homeworkPo != null
                && homeworkPo.getDeadlineAt() != null
                && LocalDateTime.now().isAfter(homeworkPo.getDeadlineAt())
                && !hasSubmission(taskPo);
    }

    private void syncHeadTeacherFlags(Long classId, Long headTeacherId) {
        if (classId == null) {
            return;
        }
        List<TeacherClassSubjectPo> relations = teacherClassSubjectMapper.selectList(
                new LambdaQueryWrapper<TeacherClassSubjectPo>()
                        .eq(TeacherClassSubjectPo::getClassId, classId)
        );
        for (TeacherClassSubjectPo relation : relations) {
            relation.setIsHeadTeacher(headTeacherId != null && Objects.equals(headTeacherId, relation.getTeacherId()));
            teacherClassSubjectMapper.updateById(relation);
        }
    }

    private String normalizeRoleType(String roleType) {
        if (!StringUtils.hasText(roleType)) {
            throw new CommonException("roleType不能为空");
        }
        String normalized = roleType.trim().toLowerCase(Locale.ROOT);
        if (!Set.of("admin", "teacher", "student", "parent").contains(normalized)) {
            throw new CommonException("roleType不正确");
        }
        return normalized;
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "enabled";
        }
        return status.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRelationType(String relationType) {
        if (!StringUtils.hasText(relationType)) {
            throw new CommonException("relationType不能为空");
        }
        return relationType.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeSubjectCode(String subjectCode) {
        if (!StringUtils.hasText(subjectCode)) {
            throw new CommonException("subjectCode不能为空");
        }
        return canonicalSubjectCode(subjectCode);
    }

    private Set<String> subjectAliases(String subjectCode) {
        String normalized = canonicalSubjectCode(subjectCode);
        if ("morality".equals(normalized)) {
            return Set.of("morality", "moral");
        }
        return Set.of(normalized);
    }

    private String canonicalSubjectCode(String subjectCode) {
        String normalized = subjectCode.trim().toLowerCase(Locale.ROOT);
        if ("moral".equals(normalized)) {
            return "morality";
        }
        return normalized;
    }

    private String subjectName(String subjectCode) {
        if (!StringUtils.hasText(subjectCode)) {
            return null;
        }
        return SUBJECT_NAME_MAP.getOrDefault(canonicalSubjectCode(subjectCode), subjectCode.trim());
    }

    private boolean isEnabled(String status) {
        return !StringUtils.hasText(status) || "enabled".equalsIgnoreCase(status);
    }

    private String defaultString(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private String resolveSchoolName(Long schoolId, Map<Long, SchoolPo> schoolMap) {
        if (schoolId == null) {
            return null;
        }
        SchoolPo schoolPo = schoolMap.get(schoolId);
        return schoolPo == null ? null : schoolPo.getSchoolName();
    }

    private String resolveGradeName(Long gradeId, Map<Long, SchoolGradePo> gradeMap) {
        if (gradeId == null) {
            return null;
        }
        SchoolGradePo gradePo = gradeMap.get(gradeId);
        return gradePo == null ? null : gradePo.getGradeName();
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private String requiredText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new CommonException(message);
        }
        return value.trim();
    }

    private int defaultInteger(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private <T, K> Map<K, Integer> countByKey(Collection<T> source, Function<T, K> keyGetter) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyMap();
        }
        return source.stream()
                .map(keyGetter)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Function.identity(),
                        item -> 1,
                        Integer::sum,
                        LinkedHashMap::new
                ));
    }

    private double safeRate(long numerator, int denominator) {
        if (denominator <= 0) {
            return 0D;
        }
        return Math.round((double) numerator * 10000D / denominator) / 10000D;
    }

    private <T> PageDTO<T> emptyPage(Integer pageNo, Integer pageSize) {
        int safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 500);
        return PageDTO.of(Collections.emptyList(), 0, safePageNo, safePageSize);
    }

    private <T> PageSlice<T> slice(List<T> source, Integer pageNo, Integer pageSize) {
        int safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 500);
        int total = source == null ? 0 : source.size();
        if (total == 0) {
            return new PageSlice<>(Collections.emptyList(), 0, safePageNo, safePageSize);
        }
        int fromIndex = Math.min((safePageNo - 1) * safePageSize, total);
        int toIndex = Math.min(fromIndex + safePageSize, total);
        return new PageSlice<>(new ArrayList<>(source.subList(fromIndex, toIndex)), total, safePageNo, safePageSize);
    }

    private record PageSlice<T>(List<T> items, long total, int pageNo, int pageSize) {
    }
}
