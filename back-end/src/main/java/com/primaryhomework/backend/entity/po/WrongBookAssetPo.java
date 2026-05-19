package com.primaryhomework.backend.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wrong_book_asset")
public class WrongBookAssetPo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long wrongBookId;
    private String assetRole;
    private String assetType;
    private String assetUrl;
    private String assetName;
    private Integer sortNo;
    private LocalDateTime createdAt;
}
