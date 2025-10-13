// pages/index/index.js
const app = getApp()
const authApi = require('../../api/auth.js')
const birthdayApi = require('../../api/birthday.js')
const util = require('../../utils/util.js')

Page({
  data: {
    userInfo: {},
    membershipLevel: 'FREE',
    membershipText: '温馨体验',
    maxSlots: 3,
    remainingSlots: 3,
    vipExpireDate: '',
    upcomingBirthdays: []
  },

  onLoad() {
    this.checkLogin()
  },

  onShow() {
    if (app.globalData.isLogin) {
      this.loadUserInfo()
      this.loadUpcomingBirthdays()
    }
  },

  // 检查登录状态
  checkLogin() {
    if (!app.globalData.isLogin) {
      wx.redirectTo({
        url: '/pages/login/login'
      })
    } else {
      this.loadUserInfo()
      this.loadUpcomingBirthdays()
    }
  },

  // 加载用户信息
  loadUserInfo() {
    authApi.getUserInfo().then(res => {
      const maxSlots = res.membershipLevel === 'VIP' ? 20 : 3
      this.setData({
        userInfo: res,
        membershipLevel: res.membershipLevel || 'FREE',
        membershipText: util.formatMembershipLevel(res.membershipLevel || 'FREE'),
        maxSlots: maxSlots,
        vipExpireDate: res.vipExpireTime ? res.vipExpireTime.split(' ')[0] : ''
      })
      app.globalData.userInfo = res

      // 加载生日列表来计算剩余名额
      this.loadBirthdayListForSlots()
    }).catch(err => {
      console.error('加载用户信息失败:', err)
    })
  },

  // 加载生日列表计算剩余名额
  loadBirthdayListForSlots() {
    birthdayApi.getBirthdayList().then(res => {
      const currentCount = res.currentCount || 0
      const maxCount = res.maxRoleCount || 3
      this.setData({
        remainingSlots: maxCount - currentCount
      })
    }).catch(err => {
      console.error('加载生日数量失败:', err)
    })
  },

  // 加载即将到来的生日
  loadUpcomingBirthdays() {
    birthdayApi.getBirthdayList().then(res => {
      const roles = res.roles || []

      // 计算每个角色距离生日的天数
      const birthdaysWithDays = roles.map(role => {
        const daysUntil = util.getDaysUntilBirthday(role.birthDate)
        return {
          ...role,
          daysUntil,
          calendarTypeText: util.formatCalendarType(role.calendarType)
        }
      })

      // 按天数排序,取前3个
      const upcomingBirthdays = birthdaysWithDays
        .sort((a, b) => a.daysUntil - b.daysUntil)
        .slice(0, 3)

      this.setData({
        upcomingBirthdays
      })
    }).catch(err => {
      console.error('加载生日列表失败:', err)
    })
  },

  // 跳转到生日列表
  goToBirthdayList() {
    wx.switchTab({
      url: '/pages/birthdays/list'
    })
  },

  // 跳转到添加生日
  goToAddBirthday() {
    wx.navigateTo({
      url: '/pages/birthdays/edit'
    })
  },

  // 跳转到个人中心
  goToProfile() {
    wx.switchTab({
      url: '/pages/profile/profile'
    })
  }
})
