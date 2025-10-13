// pages/birthdays/list.js
const birthdayApi = require('../../api/birthday.js')
const util = require('../../utils/util.js')

Page({
  data: {
    birthdayList: [],
    totalCount: 0,
    remainingSlots: 0,
    membershipLevel: 'FREE'
  },

  onLoad() {},

  onShow() {
    this.loadBirthdayList()
  },

  // 加载生日列表
  loadBirthdayList() {
    wx.showLoading({
      title: '加载中...'
    })

    birthdayApi.getBirthdayList().then(res => {
      wx.hideLoading()

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

      // 按天数排序
      birthdaysWithDays.sort((a, b) => a.daysUntil - b.daysUntil)

      const currentCount = res.currentCount || roles.length
      const maxCount = res.maxRoleCount || 3

      this.setData({
        birthdayList: birthdaysWithDays,
        totalCount: currentCount,
        remainingSlots: maxCount - currentCount,
        membershipLevel: res.membershipLevel || 'FREE'
      })
    }).catch(err => {
      wx.hideLoading()
      console.error('加载生日列表失败:', err)
    })
  },

  // 跳转到添加页面
  goToAdd() {
    wx.navigateTo({
      url: '/pages/birthdays/edit'
    })
  },

  // 编辑
  handleEdit(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/birthdays/edit?id=${id}`
    })
  },

  // 删除
  handleDelete(e) {
    const id = e.currentTarget.dataset.id
    const name = e.currentTarget.dataset.name

    wx.showModal({
      title: '确认删除',
      content: `确定要删除「${name}」的生日提醒吗?`,
      confirmText: '删除',
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          this.deleteBirthday(id)
        }
      }
    })
  },

  // 执行删除
  deleteBirthday(id) {
    wx.showLoading({
      title: '删除中...'
    })

    birthdayApi.deleteBirthday(id).then(() => {
      wx.hideLoading()
      wx.showToast({
        title: '删除成功',
        icon: 'success'
      })
      // 重新加载列表
      this.loadBirthdayList()
    }).catch(err => {
      wx.hideLoading()
      wx.showToast({
        title: '删除失败',
        icon: 'none'
      })
      console.error('删除失败:', err)
    })
  }
})
