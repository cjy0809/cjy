import { authApi } from '@/api/auth'
import { userApi } from '@/api/user'

/**
 * 用户登录
 * @param {String} phone - 手机号
 * @param {String} password - 密码
 * @returns {Promise} - 返回登录结果，包含token和用户信息
 */
export const login = (phone, password) => {
  console.log('使用真实API登录:', { phone, password })
  return authApi.login({ phone, password })
}

/**
 * 用户注册
 * @param {Object} data - 注册数据，包含用户基本信息
 * @returns {Promise} - 返回注册结果
 */
export const register = (data) => {
  console.log('使用真实API注册:', data)
  return authApi.register(data)
}

/**
 * 获取当前用户信息
 * @returns {Promise} - 返回当前用户信息
 */
export const getUserInfo = () => {
  console.log('使用真实API获取用户信息')
  return userApi.getCurrentUser()
}

/**
 * 用户登出
 * @returns {Promise} - 返回登出结果
 */
export const logout = () => {
  console.log('使用真实API退出登录')
  return authApi.logout()
}

/**
 * 刷新访问令牌
 * @returns {Promise} - 返回新的访问令牌
 */
export const refreshToken = () => {
  console.log('使用真实API刷新token')
  return authApi.refreshToken()
}
