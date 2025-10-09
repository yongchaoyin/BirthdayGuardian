const getUserAgent = () => (typeof navigator !== 'undefined' ? navigator.userAgent : '')

export const isWeChatBrowser = () => /MicroMessenger/i.test(getUserAgent())

export const isMobileDevice = () => /MicroMessenger|Mobi|Android|iPhone|iPad|iPod|Windows Phone/i.test(getUserAgent())

export default {
  isMobileDevice,
  isWeChatBrowser
}
