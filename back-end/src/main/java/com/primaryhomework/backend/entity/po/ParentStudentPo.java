package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("parent_student_rel")
public class ParentStudentPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentUserId;
    private Long studentId;
    private String relationType;
    private Boolean isPrimary;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
