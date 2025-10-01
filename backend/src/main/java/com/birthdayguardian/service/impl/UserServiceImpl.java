package com.birthdayguardian.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.birthdayguardian.entity.User;
import com.birthdayguardian.mapper.UserMapper;
import com.birthdayguardian.service.EmailService;
import com.birthdayguardian.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;

    private static final String VERIFICATION_CODE_PREFIX = "verification_code:";
    private static final int CODE_EXPIRE_MINUTES = 5;

    @Override
    public User findByEmail(String email) {
        return this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .eq(User::getStatus, 1));
    }

    @Override
    public void sendVerificationCode(String email) {
        // 生成6位数字验证码
        String code = RandomUtil.randomNumbers(6);
        
        // 存储到Redis，5分钟过期
        String key = VERIFICATION_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 发送邮件
        emailService.sendVerificationCode(email, code);
    }

    @Override
    public User register(String email, String code, String nickname) {
        // 验证验证码
        if (!verifyCode(email, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        
        // 检查邮箱是否已注册
        User existUser = findByEmail(email);
        if (existUser != null) {
            throw new RuntimeException("邮箱已注册");
        }
        
        // 创建用户
        User user = new User();
        user.setEmail(email);
        user.setNickname(nickname);
        user.setStatus(1);
        
        this.save(user);
        
        // 删除验证码
        deleteVerificationCode(email);
        
        return user;
    }

    @Override
    public User loginByCode(String email, String code) {
        // 验证验证码
        if (!verifyCode(email, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        
        User user = findByEmail(email);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 删除验证码
        deleteVerificationCode(email);
        
        return user;
    }

    @Override
    public User loginByPassword(String email, String password) {
        User user = findByEmail(email);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (StrUtil.isBlank(user.getPassword())) {
            throw new RuntimeException("用户未设置密码，请使用验证码登录");
        }
        
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        return user;
    }

    @Override
    public void setPassword(Long userId, String password) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 加密密码
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        user.setPassword(hashedPassword);
        
        this.updateById(user);
    }

    /**
     * 验证验证码
     */
    private boolean verifyCode(String email, String code) {
        String key = VERIFICATION_CODE_PREFIX + email;
        String storedCode = (String) redisTemplate.opsForValue().get(key);
        return StrUtil.isNotBlank(storedCode) && storedCode.equals(code);
    }

    /**
     * 删除验证码
     */
    private void deleteVerificationCode(String email) {
        String key = VERIFICATION_CODE_PREFIX + email;
        redisTemplate.delete(key);
    }
}