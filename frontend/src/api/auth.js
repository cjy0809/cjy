import request from '@/utils/request'

/**
 * 认证API接口集合
 * 提供用户认证相关的API调用方法，包括登录、注册、登出等
 */
export const authApi = {
  /**
   * 用户登录
   * @param {Object} data - 登录数据，包含phone和password
   * @returns {Promise} - 返回登录结果，包含token和用户信息
   */
  login(data) {
    return request({
      url: '/api/user/login',
      method: 'post',
      data
    })
  },

  /**
   * 用户注册
   * @param {Object} data - 注册数据，包含用户基本信息
   * @returns {Promise} - 返回注册结果
   */
  register(data) {
    return request({
      url: '/api/user/register',
      method: 'post',
      data
    })
  },

  /**
   * 用户登出
   * @returns {Promise} - 返回登出结果
   */
  logout() {
    return request({
      url: '/api/auth/logout',
      method: 'post'
    })
  },

  /**
   * 刷新访问令牌
   * @returns {Promise} - 返回新的访问令牌
   */
  refreshToken() {
    return request({
      url: '/api/auth/refresh',
      method: 'post'
    })
  }
}
