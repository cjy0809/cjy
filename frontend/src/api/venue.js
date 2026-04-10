import request from '@/utils/request'

/**
 * 场地API接口集合
 * 提供场地相关的所有API调用方法，包括场地查询、预约等
 */
export const venueApi = {
  /**
   * 分页查询场地列表
   * @param {Object} params - 查询参数，包含pageNum、pageSize、name、status等
   * @returns {Promise} - 返回分页场地列表
   */
  getVenuePage(params) {
    return request({
      url: '/api/venue/page',
      method: 'get',
      params: {
        pageNum: params.pageNum ||1,
        pageSize: params.pageSize || 10,
        name: params.name,
        status: params.status
      }
    })
  },

  /**
   * 根据ID查询场地详情
   * @param {Number} id - 场地ID
   * @returns {Promise} - 返回场地详情信息
   */
  getVenueById(id) {
    return request({
      url: `/api/venue/${id}`,
      method: 'get'
    })
  },

  /**
   * 创建场地
   * @param {Object} data - 场地数据，包含name、description、capacity等
   * @returns {Promise} - 返回创建结果
   */
  createVenue(data) {
    return request({
      url: '/api/venue',
      method: 'post',
      data
    })
  },

  /**
   * 更新场地信息
   * @param {Number} id - 场地ID
   * @param {Object} data - 需要更新的场地信息
   * @returns {Promise} - 返回更新结果
   */
  updateVenue(id, data) {
    return request({
      url: `/api/venue/${id}`,
      method: 'put',
      data
    })
  },

  /**
   * 批量更新场地状态
   * @param {Object} params - 批量更新参数，包含ids和status
   * @returns {Promise} - 返回更新结果
   */
  batchUpdateVenueStatus(params) {
    return request({
      url: '/api/venue/batch-status',
      method: 'put',
      data: params
    })
  },

  /**
   * 获取场地预约日程
   * @param {Number} venueId - 场地ID
   * @param {Object} params - 查询参数，包含month、status等
   * @returns {Promise} - 返回场地预约日程数据
   */
  getVenueSchedule(venueId, params) {
    return request({
      url: `/api/reservation/calendar`,
      method: 'get',
      params: {
        month: params.month,
        venueId: venueId,
        status: params.status
      }
    })
  },

}
