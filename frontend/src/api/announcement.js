import request from '../utils/request'

/**
 * 获取启用的公告列表（用户端）
 */
export const getActiveAnnouncements = () => {
  return request({
    url: '/announcements/active',
    method: 'get'
  })
}

/**
 * 分页获取所有公告（管理端）
 */
export const getAnnouncementList = (params) => {
  return request({
    url: '/announcements/list',
    method: 'get',
    params
  })
}

/**
 * 创建公告
 */
export const createAnnouncement = (data) => {
  return request({
    url: '/announcements',
    method: 'post',
    data
  })
}

/**
 * 更新公告
 */
export const updateAnnouncement = (id, data) => {
  return request({
    url: `/announcements/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除公告
 */
export const deleteAnnouncement = (id) => {
  return request({
    url: `/announcements/${id}`,
    method: 'delete'
  })
}

/**
 * 获取公告详情
 */
export const getAnnouncementById = (id) => {
  return request({
    url: `/announcements/${id}`,
    method: 'get'
  })
}
