package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("organization_grade")
public class SchoolGradePo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long schoolId;
    private String gradeName;
    private String schoolYear;
    private String status;
}
