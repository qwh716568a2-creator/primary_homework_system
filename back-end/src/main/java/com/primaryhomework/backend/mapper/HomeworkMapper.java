package com.primaryhomework.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.primaryhomework.backend.entity.po.HomeworkPo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HomeworkMapper extends BaseMapper<HomeworkPo> {
}
