package com.primaryhomework.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.primaryhomework.backend.entity.po.HomeworkTaskPo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HomeworkTaskMapper extends BaseMapper<HomeworkTaskPo> {
}
