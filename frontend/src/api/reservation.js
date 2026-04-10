import request from '@/utils/request'

/**
 * 预约API接口集合
 * 提供场地预约相关的所有API调用方法，包括预约创建、审核、取消等
 */
export const reservationApi = {
  /**
   * 获取预约详情
   * @param {Number} reservationId - 预约ID
   * @returns {Promise} - 返回预约详情信息
   */
  getReservationById(reservationId) {
    return request({
      url: `/api/reservation/${reservationId}`,
      method: 'get'
    })
  },

  /**
   * 分页查询预约列表
   * @param {Object} params - 查询参数，包含page、size、venueId、status等
   * @returns {Promise} - 返回分页预约列表
   */
  getReservationPage(params) {
    return request({
      url: '/api/reservation/page',
      method: 'get',
      params
    })
  },

  /**
   * 创建场地预约
   * @param {Object} data - 预约数据，包含venueId、startTime、endTime等
   * @returns {Promise} - 返回创建结果
   */
  createReservation(data) {
    return request({
      url: '/api/reservation',
      method: 'post',
      data
    })
  },

  /**
   * 更新预约信息
   * @param {Number} id - 预约ID
   * @param {Object} data - 需要更新的预约信息
   * @returns {Promise} - 返回更新结果
   */
  updateReservation(id, data) {
    return request({
      url: `/api/reservation/${id}`,
      method: 'put',
      data
    })
  },

  /**
   * 删除预约
   * @param {Number} id - 预约ID
   * @returns {Promise} - 返回删除结果
   */
  deleteReservation(id) {
    return request({
      url: `/api/reservation/${id}`,
      method: 'delete'
    })
  },

  /**
   * 审核预约（管理员功能）
   * @param {Number} id - 预约ID
   * @param {Number} status - 审核状态
   * @param {String} comment - 审核意见
   * @returns {Promise} - 返回审核结果
   */
  reviewReservation(id, status, comment) {
    return request({
      url: `/api/reservation/${id}/review`,
      method: 'put',
      params: {
        status,
        comment
      }
    })
  },
  
  /**
   * 获取日历视图预约数据
   * @param {Object} params - 查询参数，包含month、venueId、status等
   * @returns {Promise} - 返回日历视图的预约数据
   */
  getCalendarReservations(params) {
    return request({
      url: '/api/reservation/calendar',
      method: 'get',
      params
    })
  },

  /**
   * 取消预约
   * @param {Number} reservationId - 预约ID
   * @returns {Promise} - 返回取消结果
   */
  cancelReservation(reservationId) {
    return request({
      url: `/api/reservation/${reservationId}/cancel`,
      method: 'put'
    })
  },

  /**
   * 批量取消过期预约（系统功能）
   * @returns {Promise} - 返回批量取消结果
   */
  cancelExpiredReservations() {
    return request({
      url: '/api/reservation/cancel-expired',
      method: 'post'
    })
  },

  /**
   * 获取预约统计数据
   * @returns {Promise} - 返回预约统计信息
   */
  getReservationStatistics() {
    return request({
      url: '/api/reservation/statistics',
      method: 'get'
    })
  },

  /**
   * 获取场地预约人数统计
   * @param {Number} venueId - 场地ID
   * @param {Object} params - 查询参数，包含startDate、endDate等
   * @returns {Promise} - 返回场地预约统计信息
   */
  getVenueReservationStatistics(venueId, params) {
    return request({
      url: `/api/reservation/venue/${venueId}/statistics`,
      method: 'get',
      params: {
        startDate: params.startDate,
        endDate: params.endDate
      }
    })
  }
}
