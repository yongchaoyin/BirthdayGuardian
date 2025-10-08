package com.birthday.guardian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.birthday.guardian.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
