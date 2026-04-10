import request from '@/utils/request'

/**
 * 活动报名API接口集合
 * 提供活动报名相关的API调用方法，包括报名创建、审核、取消等
 */
export const registrationApi = {
  /**
   * 创建报名记录
   * @param {Object} data - 报名数据，包含activityId、userId、contactPhone等
   * @returns {Promise} - 返回创建结果
   */
  createRegistration(data) {
    return request({
      url: '/api/registration',
      method: 'post',
      data
    })
  },

  /**
   * 更新报名记录
   * @param {Number} registrationId - 报名ID
   * @param {Object} data - 需要更新的报名信息
   * @returns {Promise} - 返回更新结果
   */
  updateRegistration(registrationId, data) {
    return request({
      url: `/api/registration/${registrationId}`,
      method: 'put',
      data
    })
  },

  /**
   * 删除报名记录
   * @param {Number} registrationId - 报名ID
   * @returns {Promise} - 返回删除结果
   */
  deleteRegistration(registrationId) {
    return request({
      url: `/api/registration/${registrationId}`,
      method: 'delete'
    })
  },

  /**
   * 获取报名详情
   * @param {Number} registrationId - 报名ID
   * @returns {Promise} - 返回报名详情信息
   */
  getRegistrationDetail(registrationId) {
    return request({
      url: `/api/registration/${registrationId}`,
      method: 'get'
    })
  },

  /**
   * 分页查询报名列表
   * @param {Object} params - 查询参数，包含page、size、activityId、status等
   * @returns {Promise} - 返回分页报名列表
   */
  getRegistrationPage(params) {
    return request({
      url: '/api/registration/page',
      method: 'get',
      params
    })
  },

  /**
   * 获取用户报名记录
   * @param {Number} userId - 用户ID
   * @returns {Promise} - 返回用户报名记录列表
   */
  getUserRegistrations(userId) {
    return request({
      url: `/api/registration/user/${userId}`,
      method: 'get'
    })
  },

  /**
   * 获取活动报名记录
   * @param {Number} activityId - 活动ID
   * @returns {Promise} - 返回活动报名记录列表
   */
  getActivityRegistrations(activityId) {
    return request({
      url: `/api/registration/activity/${activityId}`,
      method: 'get'
    })
  },

  /**
   * 审核报名（管理员功能）
   * @param {Number} registrationId - 报名ID
   * @param {Number} status - 审核状态
   * @param {String} remark - 审核意见
   * @returns {Promise} - 返回审核结果
   */
  approveRegistration(registrationId, status, remark) {
    return request({
      url: `/api/registration/${registrationId}/approve`,
      method: 'put',
      params: { status, remark }
    })
  },

  /**
   * 取消报名
   * @param {Number} registrationId - 报名ID
   * @returns {Promise} - 返回取消结果
   */
  cancelRegistration(registrationId) {
    return request({
      url: `/api/registration/${registrationId}/cancel`,
      method: 'put'
    })
  },

  /**
   * 批量审核报名（管理员功能）
   * @param {Array} registrationIds - 报名ID数组
   * @param {Number} status - 审核状态
   * @param {String} remark - 审核意见
   * @returns {Promise} - 返回批量审核结果
   */
  batchApproveRegistrations(registrationIds, status, remark) {
    return request({
      url: '/api/registration/batch-approve',
      method: 'post',
      data: {
        registrationIds,
        status,
        remark
      }
    })
  },

  /**
   * 获取报名统计信息
   * @returns {Promise} - 返回报名统计信息
   */
  getRegistrationStatistics() {
    return request({
      url: '/api/registration/statistics',
      method: 'get'
    })
  }
}
