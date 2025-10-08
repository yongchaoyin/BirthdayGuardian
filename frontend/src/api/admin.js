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
 * 删除用户
 */
export const deleteUser = (id) => {
  return request({
    url: `/admin/users/${id}`,
    method: 'delete'
  })
}
