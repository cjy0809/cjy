import request from '@/utils/request'

/**
 * 活动API接口集合
 * 提供活动相关的所有API调用方法，包括活动创建、查询、报名等
 */
export const activityApi = {
  /**
   * 创建活动
   * @param {Object} data - 活动数据，包含title、content、location、startTime等
   * @returns {Promise} - 返回创建结果
   */
  createActivity(data) {
    return request({
      url: '/api/activity',
      method: 'post',
      data
    })
  },

  /**
   * 更新活动信息
   * @param {Number} activityId - 活动ID
   * @param {Object} data - 需要更新的活动信息
   * @returns {Promise} - 返回更新结果
   */
  updateActivity(activityId, data) {
    return request({
      url: `/api/activity/${activityId}`,
      method: 'put',
      data
    })
  },

  /**
   * 删除活动
   * @param {Number} activityId - 活动ID
   * @returns {Promise} - 返回删除结果
   */
  deleteActivity(activityId) {
    return request({
      url: `/api/activity/${activityId}`,
      method: 'delete'
    })
  },

  /**
   * 获取活动详情
   * @param {Number} activityId - 活动ID
   * @returns {Promise} - 返回活动详情信息
   */
  getActivityDetail(activityId) {
    return request({
      url: `/api/activity/${activityId}`,
      method: 'get'
    })
  },

  /**
   * 分页查询活动列表
   * @param {Object} params - 查询参数，包含page、size、title、status等
   * @returns {Promise} - 返回分页活动列表
   */
  getActivityPage(params) {
    return request({
      url: '/api/activity/page',
      method: 'get',
      params
    })
  },

  /**
   * 用户报名参加活动
   * @param {Number} activityId - 活动ID
   * @returns {Promise} - 返回报名结果
   */
  registerActivity(activityId) {
    return request({
      url: `/api/activity/${activityId}/register`,
      method: 'post'
    })
  },

  /**
   * 取消活动报名
   * @param {Number} activityId - 活动ID
   * @returns {Promise} - 返回取消结果
   */
  cancelRegistration(activityId) {
    return request({
      url: `/api/activity/${activityId}/register`,
      method: 'delete'
    })
  },

  /**
   * 更新活动状态
   * @param {Number} activityId - 活动ID
   * @param {Number} status - 新状态
   * @returns {Promise} - 返回更新结果
   */
  updateActivityStatus(activityId, status) {
    return request({
      url: `/api/activity/${activityId}/status`,
      method: 'put',
      params: { status }
    })
  },
  
  /**
   * 获取活动预约日历数据
   * @param {Object} params - 查询参数，包含month、activityId等
   * @returns {Promise} - 返回日历视图的预约数据
   */
  getCalendarRegistrations(params) {
    return request({
      url: '/api/registration/calendar',
      method: 'get',
      params
    })
  },
  
  /**
   * 获取活动参与者列表
   * @param {Number} activityId - 活动ID
   * @returns {Promise} - 返回参与者列表
   */
  getActivityParticipants(activityId) {
    return request({
      url: `/api/registration/activity/${activityId}`,
      method: 'get'
    })
  },
  
  /**
   * 获取相关活动推荐
   * @param {Number} activityId - 活动ID
   * @param {Number} limit - 返回数量限制，默认为5
   * @returns {Promise} - 返回相关活动列表
   */
  getRelatedActivities(activityId, limit = 5) {
    return request({
      url: `/api/activity/${activityId}/related`,
      method: 'get',
      params: { limit }
    })
  },
  
  /**
   * 获取活动统计数据
   * @returns {Promise} - 返回活动统计信息，包括总数、参与人数等
   */
  getActivityStatistics() {
    return request({
      url: '/api/activity/statistics',
      method: 'get'
    })
  },

  /**
   * 获取可报名的活动列表
   * @returns {Promise} - 返回可报名的活动列表
   */
  getRegistrableActivities() {
    return request({
      url: '/api/activity/registrable',
      method: 'get'
    })
  }
}
