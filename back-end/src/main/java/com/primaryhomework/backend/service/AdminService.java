package com.primaryhomework.backend.service;

import com.primaryhomework.backend.entity.dto.admin.AdminClassQueryDto;
import com.primaryhomework.backend.entity.dto.admin.AdminUserQueryDto;
import com.primaryhomework.backend.entity.dto.admin.AdminUserSaveDto;
import com.primaryhomework.backend.entity.dto.admin.ParentRelationSaveDto;
import com.primaryhomework.backend.entity.dto.admin.TeacherRelationSaveDto;
import com.primaryhomework.backend.entity.vo.PageDTO;
import com.primaryhomework.backend.entity.vo.admin.AdminClassVo;
import com.primaryhomework.backend.entity.vo.admin.AdminOverviewVo;
import com.primaryhomework.backend.entity.vo.admin.AdminSchoolVo;
import com.primaryhomework.backend.entity.vo.admin.AdminUserVo;
import com.primaryhomework.backend.entity.vo.admin.ParentRelationVo;
import com.primaryhomework.backend.entity.vo.admin.TeacherRelationVo;

import java.util.List;

public interface AdminService {

    AdminOverviewVo getOverview(String authorization);

    List<AdminSchoolVo> listSchools(String authorization);

    List<AdminClassVo> listClasses(String authorization, AdminClassQueryDto queryDto);

    PageDTO<AdminUserVo> pageUsers(String authorization, AdminUserQueryDto queryDto);

    void createUser(String authorization, AdminUserSaveDto saveDto);

    void updateUser(String authorization, Long userId, AdminUserSaveDto saveDto);

    List<TeacherRelationVo> listTeacherRelations(String authorization);

    void saveTeacherRelation(String authorization, TeacherRelationSaveDto saveDto);

    List<ParentRelationVo> listParentRelations(String authorization);

    void saveParentRelation(String authorization, ParentRelationSaveDto saveDto);
}
