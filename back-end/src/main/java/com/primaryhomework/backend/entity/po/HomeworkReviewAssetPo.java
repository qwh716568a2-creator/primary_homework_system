package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("homework_review_asset")
public class HomeworkReviewAssetPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reviewId;
    private String assetType;
    private String assetUrl;
    private Integer sortNo;
    private LocalDateTime createdAt;
}
