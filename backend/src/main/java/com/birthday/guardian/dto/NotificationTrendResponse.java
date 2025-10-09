package com.birthday.guardian.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotificationTrendResponse {
    private List<NotificationTrendPoint> points;
}
