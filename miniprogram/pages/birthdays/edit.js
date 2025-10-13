// pages/birthdays/edit.js
const birthdayApi = require('../../api/birthday.js')

Page({
  data: {
    isEdit: false,
    editId: null,
    roleTypes: ['父亲', '母亲', '配偶', '子女', '朋友', '同事', '其他'],
    roleTypeIndex: 0,
    calendarTypes: ['阳历', '阴历'],
    calendarTypeIndex: 0,
    remindDaysOptions: [1, 3, 5, 7, 10, 15, 30],
    remindDaysIndex: 1, // 默认3天
    formData: {
      roleType: '父亲',
      roleName: '',
      birthDate: '',
      calendarType: 1,
      remindDays: 3,
      rolePhone: '',
      remark: ''
    }
  },

  onLoad(options) {
    if (options.id) {
      // 编辑模式
      this.setData({
        isEdit: true,
        editId: options.id
      })
      this.loadBirthdayDetail(options.id)
    }

    // 设置默认生日日期为今天
    if (!this.data.formData.birthDate) {
      const today = new Date()
      const year = today.getFullYear()
      const month = String(today.getMonth() + 1).padStart(2, '0')
      const day = String(today.getDate()).padStart(2, '0')
      this.setData({
        'formData.birthDate': `${year}-${month}-${day}`
      })
    }
  },

  // 加载生日详情
  loadBirthdayDetail(id) {
    wx.showLoading({
      title: '加载中...'
    })

    birthdayApi.getBirthdayDetail(id).then(res => {
      wx.hideLoading()

      // 查找角色类型索引
      const roleTypeIndex = this.data.roleTypes.indexOf(res.roleType)
      // 查找日历类型索引
      const calendarTypeIndex = res.calendarType === 1 ? 0 : 1
      // 查找提醒天数索引
      const remindDaysIndex = this.data.remindDaysOptions.indexOf(res.remindDays)

      this.setData({
        formData: {
          roleType: res.roleType,
          roleName: res.roleName,
          birthDate: res.birthDate,
          calendarType: res.calendarType,
          remindDays: res.remindDays,
          rolePhone: res.rolePhone || '',
          remark: res.remark || ''
        },
        roleTypeIndex: roleTypeIndex >= 0 ? roleTypeIndex : 0,
        calendarTypeIndex,
        remindDaysIndex: remindDaysIndex >= 0 ? remindDaysIndex : 1
      })
    }).catch(err => {
      wx.hideLoading()
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
      console.error('加载生日详情失败:', err)
    })
  },

  // 角色类型选择
  onRoleTypeChange(e) {
    const index = e.detail.value
    this.setData({
      roleTypeIndex: index,
      'formData.roleType': this.data.roleTypes[index]
    })
  },

  // 角色姓名输入
  onRoleNameInput(e) {
    this.setData({
      'formData.roleName': e.detail.value
    })
  },

  // 日历类型选择
  onCalendarTypeChange(e) {
    const index = e.detail.value
    this.setData({
      calendarTypeIndex: index,
      'formData.calendarType': index === 0 ? 1 : 2
    })
  },

  // 出生日期选择
  onBirthDateChange(e) {
    this.setData({
      'formData.birthDate': e.detail.value
    })
  },

  // 提前提醒天数选择
  onRemindDaysChange(e) {
    const index = e.detail.value
    this.setData({
      remindDaysIndex: index,
      'formData.remindDays': this.data.remindDaysOptions[index]
    })
  },

  // 联系电话输入
  onRolePhoneInput(e) {
    this.setData({
      'formData.rolePhone': e.detail.value
    })
  },

  // 备注输入
  onRemarkInput(e) {
    this.setData({
      'formData.remark': e.detail.value
    })
  },

  // 表单验证
  validateForm() {
    const { roleType, roleName, birthDate } = this.data.formData

    if (!roleType) {
      wx.showToast({
        title: '请选择角色类型',
        icon: 'none'
      })
      return false
    }

    if (!roleName || roleName.trim() === '') {
      wx.showToast({
        title: '请输入角色姓名',
        icon: 'none'
      })
      return false
    }

    if (!birthDate) {
      wx.showToast({
        title: '请选择出生日期',
        icon: 'none'
      })
      return false
    }

    return true
  },

  // 提交表单
  handleSubmit() {
    if (!this.validateForm()) {
      return
    }

    wx.showLoading({
      title: this.data.isEdit ? '保存中...' : '添加中...'
    })

    const apiCall = this.data.isEdit
      ? birthdayApi.updateBirthday(this.data.editId, this.data.formData)
      : birthdayApi.addBirthday(this.data.formData)

    apiCall.then(() => {
      wx.hideLoading()
      wx.showToast({
        title: this.data.isEdit ? '保存成功' : '添加成功',
        icon: 'success'
      })

      // 返回列表页
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    }).catch(err => {
      wx.hideLoading()
      wx.showToast({
        title: this.data.isEdit ? '保存失败' : '添加失败',
        icon: 'none'
      })
      console.error('提交失败:', err)
    })
  },

  // 取消
  handleCancel() {
    wx.navigateBack()
  }
})
