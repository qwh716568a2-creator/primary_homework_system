package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("organization_class")
public class SchoolClassPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long schoolId;
    private Long gradeId;
    private String className;
    private String classCode;
    private Long homeroomTeacherId;
    private String status;
}
