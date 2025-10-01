package com.birthdayguardian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.birthdayguardian.entity.NoticeLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知日志Mapper接口
 */
@Mapper
public interface NoticeLogMapper extends BaseMapper<NoticeLog> {
}