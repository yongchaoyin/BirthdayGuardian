// pages/login/login.js
const app = getApp()
const authApi = require('../../api/auth.js')

Page({
  data: {},

  onLoad() {
    // 如果已登录,跳转到首页
    if (app.globalData.isLogin) {
      wx.switchTab({
        url: '/pages/index/index'
      })
    }
  },

  // 微信登录
  handleWxLogin() {
    wx.showLoading({
      title: '登录中...'
    })

    // 调用微信登录
    wx.login({
      success: (res) => {
        if (res.code) {
          // 发送 code 到后端
          this.loginWithCode(res.code)
        } else {
          wx.hideLoading()
          wx.showToast({
            title: '登录失败,请重试',
            icon: 'none'
          })
        }
      },
      fail: (err) => {
        wx.hideLoading()
        wx.showToast({
          title: '登录失败',
          icon: 'none'
        })
        console.error('wx.login失败:', err)
      }
    })
  },

  // 使用code登录
  loginWithCode(code) {
    authApi.wxLogin(code).then(res => {
      wx.hideLoading()

      // 保存token
      wx.setStorageSync('token', res.token)

      // 更新全局状态
      app.globalData.isLogin = true
      app.globalData.userInfo = res.userInfo

      wx.showToast({
        title: '登录成功',
        icon: 'success',
        duration: 2000
      })

      // 跳转到首页
      setTimeout(() => {
        wx.switchTab({
          url: '/pages/index/index'
        })
      }, 2000)
    }).catch(err => {
      wx.hideLoading()
      wx.showToast({
        title: err.message || '登录失败',
        icon: 'none'
      })
      console.error('登录失败:', err)
    })
  }
})
