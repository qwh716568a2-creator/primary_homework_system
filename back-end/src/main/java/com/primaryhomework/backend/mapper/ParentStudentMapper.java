package com.primaryhomework.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.primaryhomework.backend.entity.po.ParentStudentPo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ParentStudentMapper extends BaseMapper<ParentStudentPo> {
}
