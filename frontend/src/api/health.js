import request from '@/utils/request'

/**
 * 健康记录API接口集合
 * 提供健康档案相关的API调用方法，包括健康记录的创建、查询、更新、删除等
 */
export const healthApi = {
  /**
   * 获取健康记录列表
   * @param {Object} params - 查询参数，包含page、size、userId等
   * @returns {Promise} - 返回分页健康记录列表
   */
  getHealthRecords(params) {
    return request({
      url: '/api/health-records',
      method: 'get',
      params: params
    })
  },

  /**
   * 获取健康记录详情
   * @param {Number} id - 健康记录ID
   * @returns {Promise} - 返回健康记录详情信息
   */
  getHealthRecordDetail(id) {
    return request({
      url: `/api/health-records/${id}`,
      method: 'get'
    })
  },

  /**
   * 创建健康记录
   * @param {Object} data - 健康记录数据，包含userId、checkDate、checkItems等
   * @returns {Promise} - 返回创建结果
   */
  createHealthRecord(data) {
    return request({
      url: '/api/health-records',
      method: 'post',
      data: data
    })
  },

  /**
   * 更新健康记录
   * @param {Number} id - 健康记录ID
   * @param {Object} data - 需要更新的健康记录信息
   * @returns {Promise} - 返回更新结果
   */
  updateHealthRecord(id, data) {
    return request({
      url: `/api/health-records/${id}`,
      method: 'put',
      data: data
    })
  },

  /**
   * 删除健康记录
   * @param {Number} id - 健康记录ID
   * @returns {Promise} - 返回删除结果
   */
  deleteHealthRecord(id) {
    return request({
      url: `/api/health-records/${id}`,
      method: 'delete'
    })
  },

  /**
   * 获取指定用户的健康记录
   * @param {Number} userId - 用户ID
   * @param {Object} params - 查询参数，包含page、size等
   * @returns {Promise} - 返回用户的健康记录列表
   */
  getUserHealthRecords(userId, params) {
    return request({
      url: `/api/health-records/user/${userId}`,
      method: 'get',
      params: params
    })
  }
}
