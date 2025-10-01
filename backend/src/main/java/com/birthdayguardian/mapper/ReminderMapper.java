package com.birthdayguardian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.birthdayguardian.entity.Reminder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提醒Mapper接口
 */
@Mapper
public interface ReminderMapper extends BaseMapper<Reminder> {
}