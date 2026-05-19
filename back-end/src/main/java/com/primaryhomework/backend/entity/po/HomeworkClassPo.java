package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("homework_class_rel")
public class HomeworkClassPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long homeworkId;
    private Long classId;
}
