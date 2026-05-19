package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("student_profile")
public class StudentPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentUserId;
    private Long schoolId;
    private Long gradeId;
    private Long classId;
    private String studentNo;
    private String gender;
    private String status;
}
