// 封装微信请求
const app = getApp()

const request = (options) => {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${app.globalData.apiBase}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': wx.getStorageSync('token') ? `Bearer ${wx.getStorageSync('token')}` : '',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data.data)
          } else {
            wx.showToast({
              title: res.data.message || '请求失败',
              icon: 'none'
            })
            reject(res.data)
          }
        } else if (res.statusCode === 401) {
          // token失效,跳转登录
          wx.removeStorageSync('token')
          wx.redirectTo({
            url: '/pages/login/login'
          })
          reject(new Error('未授权'))
        } else {
          wx.showToast({
            title: '网络错误',
            icon: 'none'
          })
          reject(new Error('网络错误'))
        }
      },
      fail: (err) => {
        wx.showToast({
          title: '网络请求失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

module.exports = {
  get: (url, data) => request({ url, method: 'GET', data }),
  post: (url, data) => request({ url, method: 'POST', data }),
  put: (url, data) => request({ url, method: 'PUT', data }),
  delete: (url, data) => request({ url, method: 'DELETE', data })
}
