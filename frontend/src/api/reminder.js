import request from './request'

export const reminderApi = {
  // 获取提醒列表
  getReminders(userId) {
    return request.get('/reminder/list', { params: { userId } })
  },

  // 添加提醒
  addReminder(data) {
    return request.post('/reminder/add', data)
  },

  // 删除提醒
  deleteReminder(id) {
    return request.delete(`/reminder/delete/${id}`)
  }
}