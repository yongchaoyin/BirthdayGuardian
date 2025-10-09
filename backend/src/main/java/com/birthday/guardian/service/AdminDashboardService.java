package com.birthday.guardian.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.birthday.guardian.common.MembershipLevel;
import com.birthday.guardian.dto.AdminDashboardStats;
import com.birthday.guardian.entity.BirthdayRole;
import com.birthday.guardian.entity.User;
import com.birthday.guardian.mapper.BirthdayRoleMapper;
import com.birthday.guardian.mapper.UserMapper;
import com.birthday.guardian.util.BirthdayDateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminDashboardService {

    @Autowired
    private BirthdayRoleMapper birthdayRoleMapper;

    @Autowired
    private NotificationLogService notificationLogService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    public AdminDashboardStats collectStats() {
        AdminDashboardStats stats = new AdminDashboardStats();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        refreshExpiredVip();

        stats.setTodayEmailCount(notificationLogService.countByDateAndChannel(today, "EMAIL"));
        stats.setTodaySmsCount(notificationLogService.countByDateAndChannel(today, "SMS"));

        populateUserCounters(stats);
        populateBirthdayCounters(stats, today, tomorrow);

        return stats;
    }

    private void populateUserCounters(AdminDashboardStats stats) {
        long totalUsers = userMapper.selectCount(null);

        LambdaQueryWrapper<User> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(User::getRole, "admin");
        long adminCount = userMapper.selectCount(adminWrapper);

        LambdaQueryWrapper<User> vipWrapper = new LambdaQueryWrapper<>();
        vipWrapper.eq(User::getMembershipLevel, MembershipLevel.VIP.name())
                  .gt(User::getVipExpireTime, LocalDateTime.now());
        long vipCount = userMapper.selectCount(vipWrapper);

        LambdaQueryWrapper<User> freeWrapper = new LambdaQueryWrapper<>();
        freeWrapper.eq(User::getMembershipLevel, MembershipLevel.FREE.name());
        long freeCount = userMapper.selectCount(freeWrapper);

        stats.setTotalUsers(totalUsers);
        stats.setAdminCount(adminCount);
        stats.setUserCount(totalUsers - adminCount);
        stats.setVipCount(vipCount);
        stats.setFreeCount(freeCount);
    }

    private void populateBirthdayCounters(AdminDashboardStats stats, LocalDate today, LocalDate tomorrow) {
        List<BirthdayRole> roles = birthdayRoleMapper.selectList(null);
        Map<Long, User> userCache = new HashMap<>();

        long todayBirthdayCount = 0;
        long tomorrowBirthdayCount = 0;
        long tomorrowEmailPlanCount = 0;
        long tomorrowSmsPlanCount = 0;

        for (BirthdayRole role : roles) {
            LocalDate upcomingBirthday = BirthdayDateUtil.resolveUpcomingBirthday(role, today);
            long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, upcomingBirthday);

            if (daysUntil == 0) {
                todayBirthdayCount++;
            }
            if (daysUntil == 1) {
                tomorrowBirthdayCount++;
            }
            if (daysUntil == role.getRemindDays() + 1) {
                tomorrowEmailPlanCount++;
            }

            if (daysUntil <= 1) {
                User user = userCache.computeIfAbsent(role.getUserId(), userService::getUserById);
                if (user != null) {
                    MembershipLevel level = MembershipLevel.fromCode(user.getMembershipLevel());
                    boolean hasRolePhone = role.getRolePhone() != null && !role.getRolePhone().trim().isEmpty();
                    if (level.isVip() && daysUntil == 1 && hasRolePhone) {
                        tomorrowSmsPlanCount++;
                    }
                }
            }
        }

        stats.setTodayBirthdayCount(todayBirthdayCount);
        stats.setTomorrowBirthdayCount(tomorrowBirthdayCount);
        stats.setTomorrowEmailPlanCount(tomorrowEmailPlanCount);
        stats.setTomorrowSmsPlanCount(tomorrowSmsPlanCount);
    }

    private void refreshExpiredVip() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getMembershipLevel, MembershipLevel.VIP.name())
               .and(w -> w.le(User::getVipExpireTime, LocalDateTime.now())
                           .or()
                           .isNull(User::getVipExpireTime));
        List<User> expired = userMapper.selectList(wrapper);
        for (User user : expired) {
            user.setMembershipLevel(MembershipLevel.FREE.name());
            user.setVipExpireTime(null);
            userMapper.updateById(user);
        }
    }
}
