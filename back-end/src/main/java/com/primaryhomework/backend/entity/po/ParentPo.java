package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("parent_profile")
public class ParentPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentUserId;
    private Long schoolId;
    private String mobile;
    private String gender;
}
