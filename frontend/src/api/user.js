import request from '../utils/request'

export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export function changePassword(data) {
  return request({
    url: '/user/change-password',
    method: 'post',
    data
  })
}

export function updateEmail(data) {
  return request({
    url: '/user/update-email',
    method: 'post',
    data
  })
}

export function updatePhone(data) {
  return request({
    url: '/user/update-phone',
    method: 'post',
    data
  })
}

export function updateUsername(data) {
  return request({
    url: '/user/update-username',
    method: 'post',
    data
  })
}
