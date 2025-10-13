// app.js
App({
  onLaunch() {
    // 展示本地存储能力
    const logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)

    // 检查登录态
    this.checkLoginStatus()
  },

  checkLoginStatus() {
    const token = wx.getStorageSync('token')
    if (token) {
      // 验证token是否有效
      this.verifyToken(token)
    }
  },

  verifyToken(token) {
    wx.request({
      url: `${this.globalData.apiBase}/api/user/info`,
      header: {
        'Authorization': `Bearer ${token}`
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 200) {
          this.globalData.userInfo = res.data.data
          this.globalData.isLogin = true
        } else {
          // token无效,清除本地存储
          wx.removeStorageSync('token')
          this.globalData.isLogin = false
        }
      },
      fail: () => {
        wx.removeStorageSync('token')
        this.globalData.isLogin = false
      }
    })
  },

  globalData: {
    userInfo: null,
    isLogin: false,
    apiBase: 'http://localhost:8089' // 开发环境API地址,生产环境需要修改
  }
})
