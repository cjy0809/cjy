import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * 获取token - 直接从localStorage获取，简化逻辑
 * @returns {String} - 存储的token字符串
 */
const getToken = () => {
  return localStorage.getItem('token')
}

/**
 * 创建axios实例
 * 配置基础URL、超时时间和默认请求头
 */
const request = axios.create({
  baseURL: '',  // 已在vite.config.js中配置了代理，这里使用空字符串
  timeout: 10000,    // 默认超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 请求拦截器
 * 在请求发送前自动添加token和配置超时时间
 */
request.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      // 支持Bearer和普通token两种格式
      config.headers['Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`
    }
    
    // 如果需要自定义超时时间，使用传入的timeout
    if (config.timeout) {
      config.timeout = config.timeout
    }
    
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 统一处理响应数据和错误，包括token过期处理
 */
request.interceptors.response.use(
  response => {
    // 检查响应状态码
    if (response.status === 200) {
      // 检查是否是文件上传响应
      if (response.config.url && response.config.url.includes('/avatar')) {
      }
    }
    
    // 直接返回响应数据，避免过度处理
    const res = response.data
    
    // 添加原始状态码，便于调用方判断
    if (res && typeof res === 'object') {
      res._status = response.status
      res._statusText = response.statusText
      
      // 检查业务层面的未授权错误
      if (res.code === 401) {
        // 清除token并跳转到登录页
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.replace('/login')
        
        // 抛出自定义错误，使调用方能够捕获
        const unauthorizedError = new Error(res.message || '未授权，请登录')
        unauthorizedError.code = 401
        return Promise.reject(unauthorizedError)
      }
    }
    
    return res
  },
  error => {
    // 处理响应错误
    let errorMsg = '未知错误'
    
    if (error.response) {
      // 服务器返回了错误状态码
      const status = error.response.status
      const data = error.response.data
      
      switch (status) {
        case 401:
          errorMsg = '未授权，请重新登录'
          // 清除token并跳转到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          router.replace('/login')
          break
        case 403:
          errorMsg = '权限不足，拒绝访问'
          break
        case 404:
          errorMsg = `请求的资源不存在(${error.config?.url})`
          break
        case 500:
        case 502:
        case 503:
        case 504:
          errorMsg = '服务器暂时不可用，请稍后再试'
          break
        default:
          // 尝试从响应体中获取错误信息
          if (data && typeof data === 'object') {
            errorMsg = data.message || data.error || data.msg || `请求失败(${status})`
          } else if (typeof data === 'string') {
            errorMsg = data
          } else {
            errorMsg = `请求失败(${status})`
          }
      }
    } else if (error.request) {
      // 请求已发出，但没有收到响应
      if (error.code === 'ECONNABORTED') {
        errorMsg = '请求超时，请检查网络连接'
      } else {
        errorMsg = '网络错误，请检查您的网络连接'
      }
    } else {
      // 请求配置出错
      errorMsg = error.message || '请求错误'
    }
    
    // 避免重复显示错误消息
    if (!error.config || !error.config._showError || error.config._showError !== false) {
      ElMessage.error(errorMsg)
    }
    
    return Promise.reject(error)
  }
)

export default request
