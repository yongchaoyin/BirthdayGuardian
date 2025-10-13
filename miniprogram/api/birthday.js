// 生日管理相关API
const request = require('../utils/request.js')

// 获取生日角色列表
const getBirthdayList = () => {
  return request.get('/api/roles')
}

// 添加生日角色
const addBirthday = (data) => {
  return request.post('/api/roles', data)
}

// 更新生日角色
const updateBirthday = (id, data) => {
  return request.put(`/api/roles/${id}`, data)
}

// 删除生日角色
const deleteBirthday = (id) => {
  return request.delete(`/api/roles/${id}`)
}

// 获取角色详情
const getBirthdayDetail = (id) => {
  return request.get(`/api/roles/${id}`)
}

module.exports = {
  getBirthdayList,
  addBirthday,
  updateBirthday,
  deleteBirthday,
  getBirthdayDetail
}
