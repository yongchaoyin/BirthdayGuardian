import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    console.log('发送请求:', config.url, config.data)
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    console.log('收到响应:', response.config.url, response.data)
    const res = response.data
    if (res.code !== 200) {
      console.error('业务错误:', res)
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    console.error('响应错误:', error)
    if (error.response) {
      console.error('错误响应:', error.response.data)
    }
    return Promise.reject(error)
  }
)

export default request
