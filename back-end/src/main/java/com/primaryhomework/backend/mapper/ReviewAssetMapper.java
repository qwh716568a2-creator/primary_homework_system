package com.primaryhomework.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.primaryhomework.backend.entity.po.ReviewAssetPo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewAssetMapper extends BaseMapper<ReviewAssetPo> {
}
