// pages/profile/profile.js
const app = getApp()
const authApi = require('../../api/auth.js')
const util = require('../../utils/util.js')

Page({
  data: {
    userInfo: {},
    membershipLevel: 'FREE',
    membershipText: '温馨体验',
    maxSlots: 3,
    remainingSlots: 0,
    vipExpireDate: ''
  },

  onLoad() {},

  onShow() {
    this.loadUserInfo()
  },

  // 加载用户信息
  loadUserInfo() {
    const birthdayApi = require('../../api/birthday.js')

    authApi.getUserInfo().then(res => {
      const maxSlots = res.membershipLevel === 'VIP' ? 20 : 3
      this.setData({
        userInfo: res,
        membershipLevel: res.membershipLevel || 'FREE',
        membershipText: util.formatMembershipLevel(res.membershipLevel || 'FREE'),
        maxSlots: maxSlots,
        vipExpireDate: res.vipExpireTime ? res.vipExpireTime.split(' ')[0] : ''
      })

      // 加载生日列表来计算剩余名额
      birthdayApi.getBirthdayList().then(listRes => {
        const currentCount = listRes.currentCount || 0
        const maxCount = listRes.maxRoleCount || maxSlots
        this.setData({
          remainingSlots: maxCount - currentCount
        })
      }).catch(err => {
        console.error('加载生日数量失败:', err)
      })
    }).catch(err => {
      console.error('加载用户信息失败:', err)
    })
  },

  // 修改用户名
  handleUpdateUsername() {
    wx.showModal({
      title: '修改用户名',
      editable: true,
      placeholderText: '请输入新用户名',
      success: (res) => {
        if (res.confirm && res.content) {
          this.updateUsername(res.content)
        }
      }
    })
  },

  // 更新用户名
  updateUsername(username) {
    wx.showLoading({
      title: '更新中...'
    })

    authApi.updateUserInfo({ username }).then(() => {
      wx.hideLoading()
      wx.showToast({
        title: '更新成功',
        icon: 'success'
      })
      this.loadUserInfo()
    }).catch(err => {
      wx.hideLoading()
      wx.showToast({
        title: '更新失败',
        icon: 'none'
      })
      console.error('更新用户名失败:', err)
    })
  },

  // 绑定手机号
  handleUpdatePhone() {
    wx.showModal({
      title: '绑定手机号',
      editable: true,
      placeholderText: '请输入手机号',
      success: (res) => {
        if (res.confirm && res.content) {
          // 验证手机号格式
          const phoneReg = /^1[3-9]\d{9}$/
          if (!phoneReg.test(res.content)) {
            wx.showToast({
              title: '手机号格式不正确',
              icon: 'none'
            })
            return
          }
          this.bindPhone(res.content)
        }
      }
    })
  },

  // 绑定手机
  bindPhone(phone) {
    wx.showLoading({
      title: '绑定中...'
    })

    authApi.bindPhone(phone).then(() => {
      wx.hideLoading()
      wx.showToast({
        title: '绑定成功',
        icon: 'success'
      })
      this.loadUserInfo()
    }).catch(err => {
      wx.hideLoading()
      wx.showToast({
        title: '绑定失败',
        icon: 'none'
      })
      console.error('绑定手机失败:', err)
    })
  },

  // 修改邮箱
  handleUpdateEmail() {
    wx.showModal({
      title: '修改邮箱',
      editable: true,
      placeholderText: '请输入新邮箱',
      success: (res) => {
        if (res.confirm && res.content) {
          // 验证邮箱格式
          const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
          if (!emailReg.test(res.content)) {
            wx.showToast({
              title: '邮箱格式不正确',
              icon: 'none'
            })
            return
          }
          this.updateEmail(res.content)
        }
      }
    })
  },

  // 更新邮箱
  updateEmail(email) {
    wx.showLoading({
      title: '更新中...'
    })

    authApi.updateUserInfo({ email }).then(() => {
      wx.hideLoading()
      wx.showToast({
        title: '更新成功',
        icon: 'success'
      })
      this.loadUserInfo()
    }).catch(err => {
      wx.hideLoading()
      wx.showToast({
        title: '更新失败',
        icon: 'none'
      })
      console.error('更新邮箱失败:', err)
    })
  },

  // 关于我们
  handleAbout() {
    wx.showModal({
      title: '关于生日守护者',
      content: '生日守护者是一款帮助您记录和管理重要人物生日的小程序。让每个重要的日子都不被遗忘,用心守护每一份情谊。',
      showCancel: false
    })
  },

  // 联系客服
  handleContact() {
    wx.showModal({
      title: '联系客服',
      content: '如需升级VIP或咨询问题,请发送邮件至: yinyc0925@outlook.com',
      showCancel: false,
      confirmText: '复制邮箱',
      success: (res) => {
        if (res.confirm) {
          wx.setClipboardData({
            data: 'yinyc0925@outlook.com',
            success: () => {
              wx.showToast({
                title: '邮箱已复制',
                icon: 'success'
              })
            }
          })
        }
      }
    })
  },

  // 退出登录
  handleLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出登录吗?',
      success: (res) => {
        if (res.confirm) {
          // 清除本地存储
          wx.removeStorageSync('token')
          app.globalData.isLogin = false
          app.globalData.userInfo = null

          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          })

          // 跳转到登录页
          setTimeout(() => {
            wx.redirectTo({
              url: '/pages/login/login'
            })
          }, 1500)
        }
      }
    })
  }
})
