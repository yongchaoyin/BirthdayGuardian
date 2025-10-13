// 认证相关API
const request = require('../utils/request.js')

// 微信登录
const wxLogin = (code) => {
  return request.post('/api/auth/wx-miniprogram-login', { code })
}

// 绑定手机号
const bindPhone = (phone) => {
  return request.post('/api/user/update-phone', { phone })
}

// 获取用户信息
const getUserInfo = () => {
  return request.get('/api/user/info')
}

// 更新用户信息
const updateUserInfo = (data) => {
  return request.post('/api/user/update', data)
}

module.exports = {
  wxLogin,
  bindPhone,
  getUserInfo,
  updateUserInfo
}
