package com.birthday.guardian.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.birthday.guardian.entity.UserWechat;
import com.birthday.guardian.mapper.UserWechatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserWechatService {

    @Autowired
    private UserWechatMapper userWechatMapper;

    public UserWechat getByUserId(Long userId) {
        LambdaQueryWrapper<UserWechat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWechat::getUserId, userId);
        return userWechatMapper.selectOne(wrapper);
    }

    public UserWechat getByOpenId(String openid) {
        LambdaQueryWrapper<UserWechat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWechat::getOpenid, openid);
        return userWechatMapper.selectOne(wrapper);
    }

    public void bindWechat(UserWechat wechat) {
        UserWechat existing = getByUserId(wechat.getUserId());
        if (existing != null) {
            wechat.setId(existing.getId());
            userWechatMapper.updateById(wechat);
        } else {
            userWechatMapper.insert(wechat);
        }
    }
}
