import request from './request'

export const authApi = {
  // 发送验证码
  sendCode(email) {
    return request.post('/auth/sendCode', { email })
  },

  // 用户注册
  register(data) {
    return request.post('/auth/register', data)
  },

  // 用户登录
  login(data) {
    return request.post('/auth/login', data)
  },

  // 获取用户信息
  getUserProfile() {
    return request.get('/user/profile')
  },

  // 设置密码
  setPassword(data) {
    return request.post('/user/setPassword', data)
  }
}