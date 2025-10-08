package com.birthday.guardian.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.birthday.guardian.entity.Announcement;
import com.birthday.guardian.mapper.AnnouncementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    /**
     * 获取启用的公告列表（用户端）
     */
    public List<Announcement> getActiveAnnouncements() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getCreateTime);
        return announcementMapper.selectList(wrapper);
    }

    /**
     * 分页获取所有公告（管理端）
     */
    public Page<Announcement> getAnnouncementPage(int pageNum, int pageSize) {
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Announcement::getCreateTime);
        return announcementMapper.selectPage(page, wrapper);
    }

    /**
     * 创建公告
     */
    public void createAnnouncement(Announcement announcement) {
        announcementMapper.insert(announcement);
    }

    /**
     * 更新公告
     */
    public void updateAnnouncement(Announcement announcement) {
        announcementMapper.updateById(announcement);
    }

    /**
     * 删除公告
     */
    public void deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
    }

    /**
     * 根据ID获取公告
     */
    public Announcement getAnnouncementById(Long id) {
        return announcementMapper.selectById(id);
    }
}
