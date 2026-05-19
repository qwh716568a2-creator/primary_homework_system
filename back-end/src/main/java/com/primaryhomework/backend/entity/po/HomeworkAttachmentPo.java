package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("homework_attachment")
public class HomeworkAttachmentPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long homeworkId;
    private String assetType;
    private String assetUrl;
    private String assetName;
    private Long assetSize;
    private Integer sortNo;
}
