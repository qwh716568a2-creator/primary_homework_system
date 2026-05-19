package com.primaryhomework.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.primaryhomework.backend.entity.po.UserPo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserPo> {
}
