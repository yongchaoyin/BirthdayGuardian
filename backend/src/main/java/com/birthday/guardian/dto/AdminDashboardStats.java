package com.birthday.guardian.dto;

import lombok.Data;

@Data
public class AdminDashboardStats {
    private long totalUsers;
    private long adminCount;
    private long userCount;
    private long vipCount;
    private long freeCount;

    private long todayBirthdayCount;
    private long todayEmailCount;
    private long todaySmsCount;

    private long tomorrowBirthdayCount;
    private long tomorrowEmailPlanCount;
    private long tomorrowSmsPlanCount;
}
