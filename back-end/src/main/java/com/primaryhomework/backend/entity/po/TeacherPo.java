package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("teacher_profile")
public class TeacherPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teacherUserId;
    private Long schoolId;
    private String teacherNo;
    private String mobile;
    private String gender;
}
