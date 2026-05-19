package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("teacher_class_subject_rel")
public class TeacherClassPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teacherId;
    private Long classId;
    private String subjectCode;
    private Boolean isHeadTeacher;
    private String status;
}
