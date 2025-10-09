package com.birthday.guardian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.birthday.guardian.dto.NotificationLogSummary;
import com.birthday.guardian.entity.NotificationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NotificationLogMapper extends BaseMapper<NotificationLog> {
    @Select("SELECT DATE(send_time) AS summary_date, channel, COUNT(*) AS total " +
            "FROM notification_log " +
            "WHERE send_time >= #{start} AND send_time < #{end} " +
            "GROUP BY DATE(send_time), channel " +
            "ORDER BY summary_date")
    List<NotificationLogSummary> selectSummary(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);
}
