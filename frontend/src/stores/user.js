import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserInfo, logout, refreshToken } from '../utils/auth'

/**
 * 角色映射函数
 * 将后端返回的数字角色转换为前端使用的字符串角色
 * @param {String|Number} rawRole - 原始角色值
 * @returns {String} - 映射后的角色字符串
 */
const mapRole = (rawRole) => {
  switch (rawRole) {
    case "1":
    case "STAFF":
      return "STAFF"
    case "2":
    case "ELDERLY":
      return "ELDERLY"
    case "0":
    case "ADMIN":
      return "ADMIN"
    default:
      return rawRole
  }
}

/**
 * 用户状态管理Store
 * 管理用户的登录状态、用户信息、角色权限等
 */
export const useUserStore = defineStore('user', () => {
  // 状态定义
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  
  // 从用户信息中获取用户ID
  const userId = computed(() => userInfo.value?.id || userInfo.value?.userId || 1)

  // 判断用户是否已登录
  const isLoggedIn = computed(() => !!token.value)
  
  // 获取用户角色（统一为字符串格式）
  const userRole = computed(() => {
    const rawRole = userInfo.value?.role || ''
    return mapRole(rawRole)
  })
  
  // 检查用户是否为管理员
  const isAdmin = computed(() => userRole.value === 'ADMIN')
  
  // 检查用户是否为系统管理员（手机号为13800138000）
  const isSystemAdmin = computed(() => {
    return isAdmin.value && userInfo.value?.phone === '13800138000'
  })
  
  // 检查用户是否为工作人员
  const isStaff = computed(() => userRole.value === 'STAFF')
  
  // 检查用户是否为老年人
  const isElderly = computed(() => userRole.value === 'ELDERLY')

  /**
   * 处理用户信息，统一角色格式
   * @param {Object} info - 原始用户信息
   * @returns {Object} - 处理后的用户信息
   */
  const processUserInfo = (info) => {
    if (info) {
      return {
        ...info,
        role: mapRole(info.role) // 确保角色为字符串格式
      }
    }
    return info
  }

  /**
   * 用户登录
   * @param {String} phone - 手机号
   * @param {String} password - 密码
   * @returns {Promise} - 返回登录结果
   */
  const userLogin = async (phone, password) => {
    try {
      console.log('开始登录请求:', { phone })
      const response = await login(phone, password)
      console.log('登录响应数据:', response)
      
      if (response && response.code === 200 && response.data) {
        const tokenData = response.data.token
        if (tokenData) {
          token.value = tokenData
          localStorage.setItem('token', token.value)
          
          const processedUserInfo = {
            id: response.data.userId,
            userId: response.data.userId,
            username: response.data.username,
            name: response.data.name,
            role: mapRole(response.data.role),
            avatar: response.data.avatar || '' // 添加 avatar 字段
          }
          userInfo.value = processedUserInfo
          localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
          console.log('用户信息已保存:', userInfo.value)
          
          // 登录成功后获取完整用户信息（包含头像）
          console.log('登录成功，开始获取完整用户信息')
          await fetchUserInfo()
          console.log('获取完整用户信息完成')
          
          return { success: true, message: response.message || '登录成功' }
        } else {
          console.error('登录响应中没有token:', response)
          return { success: false, message: '登录响应异常，缺少token' }
        }
      } else {
        return { success: false, message: response?.message || '登录失败' }
      }
    } catch (error) {
      console.error('登录请求异常:', error)
      let errorMsg = '登录失败，请稍后再试'
      if (error.response) {
        errorMsg = error.response.data?.message || `登录失败(${error.response.status})`
        console.log('服务器返回错误:', error.response.status, error.response.data)
      } else if (error.request) {
        errorMsg = '服务器无响应，请检查网络连接'
        console.log('网络请求错误:', error.request)
      }
      return { success: false, message: errorMsg }
    }
  }

  /**
   * 用户注册
   * @param {Object} data - 注册数据
   * @returns {Promise} - 返回注册结果
   */
  const userRegister = async (data) => {
    try {
      const response = await register(data)
      if (response.code === 200) {
        return { success: true, message: response.message || '注册成功，请等待审核' }
      } else {
        return { success: false, message: response.message || '注册失败' }
      }
    } catch (error) {
      console.error('注册失败', error)
      return { success: false, message: error.message || '注册失败，请稍后再试' }
    }
  }

  /**
   * 设置用户信息
   * @param {Object} info - 用户信息
   */
  const setUserInfo = (info) => {
    const processedInfo = processUserInfo(info)
    userInfo.value = processedInfo
    localStorage.setItem('userInfo', JSON.stringify(processedInfo))
  }

  /**
   * 获取用户信息
   * @returns {Promise} - 返回用户信息
   */
  const fetchUserInfo = async () => {
    try {
      const response = await getUserInfo()
      if (response.code === 200) {
        const processedInfo = processUserInfo(response.data)
        userInfo.value = processedInfo
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        return { success: true, data: processedInfo }
      } else {
        return { success: false, message: response.message }
      }
    } catch (error) {
      console.error('获取用户信息失败', error)
      return { success: false, message: '获取用户信息失败' }
    }
  }

  /**
   * 退出登录
   * @returns {Promise} - 返回登出结果
   */
  const userLogout = async () => {
    try {
      await logout()
    } catch (error) {
      console.error('退出登录失败', error)
    } finally {
      // 无论后端是否成功，都清除本地数据
      token.value = ''
      userInfo.value = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('userRole') // 清除用户角色信息
    }
  }

  /**
   * 刷新token
   * @returns {Promise} - 返回刷新结果
   */
  const userRefreshToken = async () => {
    try {
      const response = await refreshToken()
      if (response.code === 200) {
        token.value = response.data.token
        localStorage.setItem('token', token.value)
        return { success: true }
      } else {
        return { success: false, message: response.message }
      }
    } catch (error) {
      console.error('刷新token失败', error)
      return { success: false, message: '刷新token失败' }
    }
  }

  return {
    // 状态
    token,
    userInfo,
    userId,
    isLoggedIn,
    userRole,
    isAdmin,
    isSystemAdmin,
    isStaff,
    isElderly,
    
    // 方法
    userLogin,
    userRegister,
    fetchUserInfo,
    setUserInfo,
    userLogout,
    logout: userLogout, // 添加logout别名，兼容组件调用
    userRefreshToken
  }
})
