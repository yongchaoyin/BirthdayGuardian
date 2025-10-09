package com.birthday.guardian.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.birthday.guardian.common.MembershipLevel;
import com.birthday.guardian.dto.ChangePasswordRequest;
import com.birthday.guardian.dto.LoginRequest;
import com.birthday.guardian.dto.LoginResponse;
import com.birthday.guardian.dto.RegisterRequest;
import com.birthday.guardian.dto.UpdateEmailRequest;
import com.birthday.guardian.entity.User;
import com.birthday.guardian.mapper.UserMapper;
import com.birthday.guardian.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String INVITE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_CODE_LENGTH = 6;
    private static final SecureRandom INVITE_RANDOM = new SecureRandom();

    public void register(RegisterRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("邮箱已被注册");
        }

        User inviter = null;
        if (StringUtils.hasText(request.getInviteCode())) {
            String trimmed = request.getInviteCode().trim().toUpperCase();
            if (!trimmed.isEmpty()) {
                LambdaQueryWrapper<User> inviteWrapper = new LambdaQueryWrapper<>();
                inviteWrapper.eq(User::getInviteCode, trimmed);
                inviter = userMapper.selectOne(inviteWrapper);
                if (inviter == null) {
                    throw new RuntimeException("推广码无效或已停用");
                }
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(DigestUtils.md5DigestAsHex(request.getPassword().getBytes()));
        user.setRole("user");
        user.setMembershipLevel(MembershipLevel.FREE.name());
        user.setVipExpireTime(null);
        user.setInviteSuccessCount(0);
        user.setInviteCode(generateUniqueInviteCode());
        if (inviter != null) {
            user.setInvitedBy(inviter.getId());
        }

        userMapper.insert(user);

        if (inviter != null) {
            int current = inviter.getInviteSuccessCount() == null ? 0 : inviter.getInviteSuccessCount();
            inviter.setInviteSuccessCount(current + 1);
            userMapper.updateById(inviter);
        }
    }

    public LoginResponse login(LoginRequest request) {
        System.out.println("登录请求 - 账号: " + request.getAccount());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(User::getUsername, request.getAccount())
                .or()
                .eq(User::getEmail, request.getAccount()));

        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            System.out.println("登录失败 - 用户不存在: " + request.getAccount());
            throw new RuntimeException("用户不存在");
        }

        System.out.println("找到用户 - ID: " + user.getId() + ", 用户名: " + user.getUsername());

        String encryptedPassword = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
        System.out.println("密码MD5: " + encryptedPassword);
        System.out.println("数据库密码: " + user.getPassword());

        if (!user.getPassword().equals(encryptedPassword)) {
            System.out.println("登录失败 - 密码错误");
            throw new RuntimeException("密码错误");
        }

        System.out.println("密码验证成功，生成Token");

        refreshMembershipStatus(user);
        fillTransientFields(user);
        MembershipLevel level = MembershipLevel.fromCode(user.getMembershipLevel());
        int maxRoleCount = user.getMaxRoleCount() != null ? user.getMaxRoleCount() : level.getMaxRoleCount();
        int inviteBonus = user.getInviteSuccessCount() == null ? 0 : user.getInviteSuccessCount();

        LoginResponse response = new LoginResponse();
        response.setToken(jwtUtil.generateToken(user.getId()));
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setMembershipLevel(level.name());
        response.setVipExpireTime(user.getVipExpireTime());
        response.setMaxRoleCount(maxRoleCount);
        response.setVipActive(level.isVip());
        response.setInviteCode(user.getInviteCode());
        response.setInviteSuccessCount(inviteBonus);
        response.setInviteBonus(inviteBonus);

        System.out.println("登录成功 - Token: " + response.getToken());

        return response;
    }

    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            refreshMembershipStatus(user);
            fillTransientFields(user);
            user.setPassword(null);
        }
        return user;
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        refreshMembershipStatus(user);

        String encryptedOldPassword = DigestUtils.md5DigestAsHex(request.getOldPassword().getBytes());
        if (!user.getPassword().equals(encryptedOldPassword)) {
            throw new RuntimeException("旧密码错误");
        }

        String encryptedNewPassword = DigestUtils.md5DigestAsHex(request.getNewPassword().getBytes());
        user.setPassword(encryptedNewPassword);
        userMapper.updateById(user);
    }

    public void updateEmail(Long userId, UpdateEmailRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        refreshMembershipStatus(user);

        String encryptedPassword = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
        if (!user.getPassword().equals(encryptedPassword)) {
            throw new RuntimeException("密码错误");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail());
        wrapper.ne(User::getId, userId);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("邮箱已被使用");
        }

        user.setEmail(request.getEmail());
        userMapper.updateById(user);
    }

    public int getMaxRoleCount(User user) {
        if (user == null) {
            return MembershipLevel.FREE.getMaxRoleCount();
        }
        MembershipLevel level = MembershipLevel.fromCode(user.getMembershipLevel());
        int inviteBonus = user.getInviteSuccessCount() == null ? 0 : user.getInviteSuccessCount();
        return level.getMaxRoleCount() + inviteBonus;
    }

    public void updateMembership(Long userId, MembershipLevel level) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (level.isVip()) {
            user.setMembershipLevel(MembershipLevel.VIP.name());
            user.setVipExpireTime(LocalDateTime.now().plusYears(1));
        } else {
            user.setMembershipLevel(MembershipLevel.FREE.name());
            user.setVipExpireTime(null);
        }
        userMapper.updateById(user);
    }

    public void updatePhone(Long userId, String phone) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        refreshMembershipStatus(user);
        String trimmed = phone == null ? null : phone.trim();
        user.setPhone((trimmed == null || trimmed.isEmpty()) ? null : trimmed);
        userMapper.updateById(user);
    }

    public void updateUsername(Long userId, String username) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username).ne(User::getId, userId);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已被占用");
        }

        user.setUsername(username);
        userMapper.updateById(user);
    }

    private void refreshMembershipStatus(User user) {
        if (user == null) {
            return;
        }

        MembershipLevel level = MembershipLevel.fromCode(user.getMembershipLevel());
        if (level.isVip()) {
            LocalDateTime expireTime = user.getVipExpireTime();
            if (expireTime == null || expireTime.isBefore(LocalDateTime.now())) {
                user.setMembershipLevel(MembershipLevel.FREE.name());
                user.setVipExpireTime(null);
                userMapper.updateById(user);
            }
        }
    }

    private void fillTransientFields(User user) {
        if (user == null) {
            return;
        }

        boolean updated = false;

        if (user.getInviteSuccessCount() == null) {
            user.setInviteSuccessCount(0);
            updated = true;
        }

        if (!StringUtils.hasText(user.getInviteCode())) {
            user.setInviteCode(generateUniqueInviteCode());
            updated = true;
        }

        if (updated) {
            userMapper.updateById(user);
        }

        MembershipLevel level = MembershipLevel.fromCode(user.getMembershipLevel());
        int inviteBonus = user.getInviteSuccessCount();
        user.setMaxRoleCount(level.getMaxRoleCount() + inviteBonus);
        user.setVipActive(level.isVip());
        user.setInviteBonus(inviteBonus);
    }

    private String generateUniqueInviteCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String code = randomInviteCode();
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getInviteCode, code);
            if (userMapper.selectCount(wrapper) == 0) {
                return code;
            }
        }
        throw new RuntimeException("系统繁忙，请稍后再试");
    }

    private String randomInviteCode() {
        StringBuilder builder = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            int index = INVITE_RANDOM.nextInt(INVITE_ALPHABET.length());
            builder.append(INVITE_ALPHABET.charAt(index));
        }
        return builder.toString();
    }
}
