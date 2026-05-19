package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("homework_submission_asset")
public class SubmissionAssetPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long submissionId;
    private String assetType;
    private String assetUrl;
    private String assetName;
    private Long assetSize;
    private Integer sortNo;
}
