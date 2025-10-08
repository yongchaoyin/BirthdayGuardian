package com.birthday.guardian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.birthday.guardian.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
