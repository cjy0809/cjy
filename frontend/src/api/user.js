import request from '@/utils/request'

/**
 * 统一处理响应格式的辅助函数
 * 确保响应数据格式一致，便于前端处理
 * @param {Object} res - 原始响应数据
 * @returns {Object} - 标准化后的响应对象，包含code、data、message字段
 */
const handleResponse = (res) => {
  // 如果后端返回的数据已经包含code和data字段，直接返回
  if (res && typeof res === 'object' && 'code' in res) {
    return res;
  }
  // 否则按照前端期望的格式包装数据
  return {
    code: 200,
    data: res || {},
    message: 'success'
  };
};

/**
 * 用户API接口集合
 * 提供用户相关的所有API调用方法，包括登录、注册、信息管理等
 */
export const userApi = {
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
    }).then(res => handleResponse(res))
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
    }).then(res => handleResponse(res))
  },

  /**
   * 获取当前登录用户信息
   * @returns {Promise} - 返回当前用户信息
   */
  getCurrentUser() {
    return request({
      url: '/api/user/info',
      method: 'get'
    }).then(res => handleResponse(res))
  },

  /**
   * 更新当前用户信息
   * @param {Object} data - 需要更新的用户信息
   * @returns {Promise} - 返回更新结果
   */
  updateUser(data) {
    return request({
      url: '/api/user/info',
      method: 'put',
      data
    }).then(res => handleResponse(res))
  },
  
  /**
   * 更新指定用户信息（管理员功能）
   * @param {Number} userId - 用户ID
   * @param {Object} data - 需要更新的用户信息
   * @returns {Promise} - 返回更新结果
   */
  updateUserById(userId, data) {
    return request({
      url: `/api/user/${userId}/info`,
      method: 'put',
      data
    }).then(res => handleResponse(res))
  },

  /**
   * 修改用户密码
   * @param {String} oldPassword - 旧密码
   * @param {String} newPassword - 新密码
   * @returns {Promise} - 返回修改结果
   */
  changePassword(oldPassword, newPassword) {
    return request({
      url: '/api/user/password',
      method: 'put',
      params: { oldPassword, newPassword }
    }).then(res => handleResponse(res))
  },

  /**
   * 分页查询用户列表（管理员功能）
   * @param {Object} params - 查询参数，包含page、size、username、name、status、role等
   * @returns {Promise} - 返回分页用户列表
   */
  getUserPage(params) {
    return request({
      url: '/api/user/page',
      method: 'get',
      params
    }).then(res => handleResponse(res))
  },

  /**
   * 更新用户状态（管理员功能）
   * @param {Number} userId - 用户ID
   * @param {Number} status - 新状态
   * @returns {Promise} - 返回更新结果
   */
  updateUserStatus(userId, status) {
    return request({
      url: `/api/user/${userId}/status`,
      method: 'put',
      params: { status }
    }).then(res => handleResponse(res))
  },

  /**
   * 删除用户（管理员功能）
   * @param {Number} userId - 用户ID
   * @returns {Promise} - 返回删除结果
   */
  deleteUser(userId) {
    return request({
      url: `/api/user/${userId}`,
      method: 'delete'
    }).then(res => handleResponse(res))
  },
  
  /**
   * 获取用户关联数据统计（管理员功能）
   * @param {Number} userId - 用户ID
   * @returns {Promise} - 返回用户关联数据的统计信息
   */
  getUserRelatedDataCount(userId) {
    return request({
      url: `/api/user/${userId}/related-data-count`,
      method: 'get'
    }).then(res => handleResponse(res))
  },

  /**
   * 根据ID获取用户信息
   * @param {Number} userId - 用户ID
   * @returns {Promise} - 返回用户信息
   */
  getUserById(userId) {
    return request({
      url: `/api/user/${userId}`,
      method: 'get'
    }).then(res => handleResponse(res))
  },

  /**
   * 上传用户头像
   * @param {File} file - 头像文件
   * @returns {Promise} - 返回头像URL
   */
  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    
    return request({
      url: '/api/user/avatar',
      method: 'post',
      data: formData,
      // 移除默认的Content-Type，让axios自动设置multipart/form-data
      headers: {
        'Content-Type': undefined
      }
    }).then(res => handleResponse(res))
  },

  /**
   * 获取所有用户（用于下拉选择等场景）
   * @returns {Promise} - 返回用户列表
   */
  getAllUsers() {
    return request({
      url: '/api/user/page',
      method: 'get',
      params: {
        page:1,
        size: 10000
      }
    }).then(res => handleResponse(res))
  },

  /**
   * 创建充值订单
   * @param {Object} data - 充值数据，包含amount、paymentMethod等
   * @returns {Promise} - 返回充值订单信息
   */
  createRecharge(data) {
    return request({
      url: '/api/recharge/create',
      method: 'post',
      data
    }).then(res => handleResponse(res))
  },

  /**
   * 根据ID获取充值记录
   * @param {Number} id - 充值记录ID
   * @returns {Promise} - 返回充值记录详情
   */
  getRechargeById(id) {
    return request({
      url: `/api/recharge/${id}`,
      method: 'get'
    }).then(res => handleResponse(res))
  }
}
