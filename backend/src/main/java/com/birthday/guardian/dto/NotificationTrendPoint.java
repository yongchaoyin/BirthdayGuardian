package com.birthday.guardian.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class NotificationTrendPoint {
    private LocalDate date;
    private Long emailCount;
    private Long smsCount;
    private Long wechatCount;
}
