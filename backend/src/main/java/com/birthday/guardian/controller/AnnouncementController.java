package com.birthday.guardian.controller;

import com.birthday.guardian.common.Result;
import com.birthday.guardian.entity.Announcement;
import com.birthday.guardian.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 获取启用的公告列表（用户端）
     */
    @GetMapping("/active")
    public Result<?> getActiveAnnouncements() {
        return Result.success(announcementService.getActiveAnnouncements());
    }

    /**
     * 分页获取所有公告（管理端）
     */
    @GetMapping("/list")
    public Result<?> getAnnouncementList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(announcementService.getAnnouncementPage(pageNum, pageSize));
    }

    /**
     * 创建公告
     */
    @PostMapping
    public Result<?> createAnnouncement(@RequestBody Announcement announcement,
                                       @RequestAttribute Long userId) {
        announcement.setCreateUserId(userId);
        announcementService.createAnnouncement(announcement);
        return Result.success("公告创建成功");
    }

    /**
     * 更新公告
     */
    @PutMapping("/{id}")
    public Result<?> updateAnnouncement(@PathVariable Long id,
                                       @RequestBody Announcement announcement) {
        announcement.setId(id);
        announcementService.updateAnnouncement(announcement);
        return Result.success("公告更新成功");
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success("公告删除成功");
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public Result<?> getAnnouncementById(@PathVariable Long id) {
        return Result.success(announcementService.getAnnouncementById(id));
    }
}
