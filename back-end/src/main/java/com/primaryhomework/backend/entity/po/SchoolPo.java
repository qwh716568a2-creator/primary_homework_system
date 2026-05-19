package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("organization_school")
public class SchoolPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String schoolName;
    private String schoolCode;
    private String status;
}
