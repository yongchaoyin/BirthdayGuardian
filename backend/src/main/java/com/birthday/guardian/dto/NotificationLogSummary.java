package com.birthday.guardian.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class NotificationLogSummary {
    private LocalDate summaryDate;
    private String channel;
    private Long total;
}
