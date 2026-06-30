import axios from 'axios'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截：自动携带 JWT
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  return config
})

// 响应拦截：统一处理
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    // 业务错误
    alert(res.msg || '请求失败')
    return Promise.reject(res)
  },
  error => {
    if (error.response) {
      if (error.response.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        router.push('/login')
        return Promise.reject(error)
      }
      const msg = error.response.data?.msg || '网络错误'
      alert(msg)
    } else {
      alert('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default request
