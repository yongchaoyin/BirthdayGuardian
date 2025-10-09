import { getWeChatJsSdkConfig } from '../api/wechat'
import { isWeChatBrowser } from './ua'

const SDK_URL = 'https://res.wx.qq.com/open/js/jweixin-1.6.0.js'
const DEFAULT_JS_APIS = [
  'updateAppMessageShareData',
  'updateTimelineShareData',
  'onMenuShareAppMessage',
  'onMenuShareTimeline'
]

let sdkLoaderPromise = null

export const loadWeChatJssdk = () => {
  if (typeof window === 'undefined') {
    return Promise.reject(new Error('运行环境不支持 WeChat JSSDK'))
  }

  if (window.wx && typeof window.wx.config === 'function') {
    return Promise.resolve(window.wx)
  }

  if (!sdkLoaderPromise) {
    sdkLoaderPromise = new Promise((resolve, reject) => {
      const script = document.createElement('script')
      script.type = 'text/javascript'
      script.src = SDK_URL
      script.async = true
      script.onload = () => {
        if (window.wx && typeof window.wx.config === 'function') {
          resolve(window.wx)
        } else {
          reject(new Error('WeChat JSSDK 加载失败'))
        }
      }
      script.onerror = () => {
        reject(new Error('WeChat JSSDK 脚本加载失败'))
      }
      document.head.appendChild(script)
    })
  }

  return sdkLoaderPromise
}

export const initWeChatJssdk = async ({ jsApiList = DEFAULT_JS_APIS } = {}) => {
  if (!isWeChatBrowser()) {
    throw new Error('当前环境不是微信内置浏览器')
  }

  await loadWeChatJssdk()

  const currentUrl = typeof window !== 'undefined' ? window.location.href.split('#')[0] : ''
  if (!currentUrl) {
    throw new Error('无法获取当前页面 URL')
  }

  const res = await getWeChatJsSdkConfig({ url: currentUrl })
  const config = res.data || {}

  if (!config.appId) {
    throw new Error('未获取到有效的微信 JS-SDK 配置')
  }

  return new Promise((resolve, reject) => {
    window.wx.config({
      debug: false,
      appId: config.appId,
      timestamp: config.timestamp,
      nonceStr: config.nonceStr,
      signature: config.signature,
      jsApiList
    })

    window.wx.ready(() => {
      resolve(window.wx)
    })

    window.wx.error(err => {
      reject(err)
    })
  })
}

export default {
  loadWeChatJssdk,
  initWeChatJssdk
}
