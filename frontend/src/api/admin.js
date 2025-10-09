import request from '../utils/request'

/**
 * 获取所有用户列表
 */
export const getUserList = (params) => {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

/**
 * 获取用户统计信息
 */
export const getUserStats = () => {
  return request({
    url: '/admin/stats',
    method: 'get'
  })
}

/**
 * 更新用户角色
 */
export const updateUserRole = (id, role) => {
  return request({
    url: `/admin/users/${id}/role`,
    method: 'put',
    data: { role }
  })
}

/**
 * 更新用户会员等级
 */
export const updateUserMembership = (id, level) => {
  return request({
    url: `/admin/users/${id}/membership`,
    method: 'put',
    data: { level }
  })
}

/**
 * 删除用户
 */
export const deleteUser = (id) => {
  return request({
    url: `/admin/users/${id}`,
    method: 'delete'
  })
}

export const broadcastEmail = (data) => {
  return request({
    url: '/admin/notifications/email',
    method: 'post',
    data
  })
}

export const broadcastSms = (data) => {
  return request({
    url: '/admin/notifications/sms',
    method: 'post',
    data
  })
}

export const getNotificationStats = (params) => {
  return request({
    url: '/admin/notifications/stats',
    method: 'get',
    params
  })
}
