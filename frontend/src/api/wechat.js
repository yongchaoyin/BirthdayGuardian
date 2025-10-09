import request from '../utils/request'

export function getWeChatOauthUrl(params) {
  return request({
    url: '/wechat/oauth-url',
    method: 'get',
    params
  })
}

export function bindWeChatAccount(data) {
  return request({
    url: '/wechat/bind',
    method: 'post',
    data
  })
}

export function getWeChatJsSdkConfig(params) {
  return request({
    url: '/wechat/js-sdk-config',
    method: 'get',
    params
  })
}
