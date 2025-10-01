import request from './request'

export const roleApi = {
  // 获取角色列表
  getRoles(userId) {
    return request.get('/role/list', { params: { userId } })
  },

  // 添加角色
  addRole(data) {
    return request.post('/role/add', data)
  },

  // 更新角色
  updateRole(data) {
    return request.put('/role/update', data)
  },

  // 删除角色
  deleteRole(id) {
    return request.delete(`/role/delete/${id}`)
  }
}