package com.primaryhomework.backend.controller;

import com.primaryhomework.backend.entity.dto.admin.AdminClassQueryDto;
import com.primaryhomework.backend.entity.dto.admin.AdminUserQueryDto;
import com.primaryhomework.backend.entity.dto.admin.AdminUserSaveDto;
import com.primaryhomework.backend.entity.dto.admin.ParentRelationSaveDto;
import com.primaryhomework.backend.entity.dto.admin.TeacherRelationSaveDto;
import com.primaryhomework.backend.entity.vo.PageDTO;
import com.primaryhomework.backend.entity.vo.R;
import com.primaryhomework.backend.entity.vo.admin.AdminClassVo;
import com.primaryhomework.backend.entity.vo.admin.AdminOverviewVo;
import com.primaryhomework.backend.entity.vo.admin.AdminSchoolVo;
import com.primaryhomework.backend.entity.vo.admin.AdminUserVo;
import com.primaryhomework.backend.entity.vo.admin.ParentRelationVo;
import com.primaryhomework.backend.entity.vo.admin.TeacherRelationVo;
import com.primaryhomework.backend.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard/overview")
    public R<AdminOverviewVo> getOverview(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return R.ok(adminService.getOverview(authorization));
    }

    @GetMapping("/schools")
    public R<List<AdminSchoolVo>> listSchools(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return R.ok(adminService.listSchools(authorization));
    }

    @GetMapping("/classes")
    public R<List<AdminClassVo>> listClasses(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            AdminClassQueryDto queryDto
    ) {
        return R.ok(adminService.listClasses(authorization, queryDto));
    }

    @GetMapping("/users")
    public R<PageDTO<AdminUserVo>> pageUsers(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            AdminUserQueryDto queryDto
    ) {
        return R.ok(adminService.pageUsers(authorization, queryDto));
    }

    @PostMapping("/users")
    public R<Void> createUser(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AdminUserSaveDto saveDto
    ) {
        adminService.createUser(authorization, saveDto);
        return R.ok();
    }

    @PutMapping("/users/{userId}")
    public R<Void> updateUser(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long userId,
            @RequestBody AdminUserSaveDto saveDto
    ) {
        adminService.updateUser(authorization, userId, saveDto);
        return R.ok();
    }

    @GetMapping("/teacher-class-subject-rels")
    public R<List<TeacherRelationVo>> listTeacherRelations(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return R.ok(adminService.listTeacherRelations(authorization));
    }

    @PostMapping("/teacher-class-subject-rels")
    public R<Void> saveTeacherRelation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody TeacherRelationSaveDto saveDto
    ) {
        adminService.saveTeacherRelation(authorization, saveDto);
        return R.ok();
    }

    @GetMapping("/parent-student-rels")
    public R<List<ParentRelationVo>> listParentRelations(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return R.ok(adminService.listParentRelations(authorization));
    }

    @PostMapping("/parent-student-rels")
    public R<Void> saveParentRelation(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ParentRelationSaveDto saveDto
    ) {
        adminService.saveParentRelation(authorization, saveDto);
        return R.ok();
    }
}
