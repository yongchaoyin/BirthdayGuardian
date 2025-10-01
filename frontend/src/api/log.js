import request from './request'

export const logApi = {
  // 获取通知日志列表
  getLogs(userId) {
    return request.get('/noticeLog/list', { params: { userId } })
  }
}