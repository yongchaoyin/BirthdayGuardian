// 工具函数
const formatTime = date => {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  const second = date.getSeconds()

  return `${[year, month, day].map(formatNumber).join('/')} ${[hour, minute, second].map(formatNumber).join(':')}`
}

const formatDate = date => {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()

  return `${year}-${formatNumber(month)}-${formatNumber(day)}`
}

const formatNumber = n => {
  n = n.toString()
  return n[1] ? n : `0${n}`
}

// 计算距离生日还有多少天
const getDaysUntilBirthday = (birthDate) => {
  const today = new Date()
  const birth = new Date(birthDate)

  // 设置今年的生日
  const thisYearBirthday = new Date(today.getFullYear(), birth.getMonth(), birth.getDate())

  // 如果今年的生日已过,计算明年的
  if (thisYearBirthday < today) {
    thisYearBirthday.setFullYear(today.getFullYear() + 1)
  }

  const diffTime = thisYearBirthday - today
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  return diffDays
}

// 格式化会员等级
const formatMembershipLevel = (level) => {
  const levelMap = {
    'FREE': '温馨体验',
    'VIP': 'VIP守护礼遇'
  }
  return levelMap[level] || level
}

// 格式化日历类型
const formatCalendarType = (type) => {
  return type === 1 ? '阳历' : '阴历'
}

module.exports = {
  formatTime,
  formatDate,
  formatNumber,
  getDaysUntilBirthday,
  formatMembershipLevel,
  formatCalendarType
}
