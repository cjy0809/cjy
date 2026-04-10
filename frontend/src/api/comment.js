import request from '@/utils/request'

/**
 * 评论API接口集合
 * 提供评论和评分相关的所有API调用方法，包括评论创建、删除、查询等
 */
export const commentApi = {
  /**
   * 创建评论
   * @param {Object} data - 评论数据，包含targetType、targetId、content、rating等
   * @returns {Promise} - 返回创建结果
   */
  createComment(data) {
    return request({
      url: '/api/comment',
      method: 'post',
      data
    })
  },

  /**
   * 删除评论（用户删除自己的评论）
   * @param {Number} commentId - 评论ID
   * @returns {Promise} - 返回删除结果
   */
  deleteComment(commentId) {
    return request({
      url: `/api/comment/${commentId}`,
      method: 'delete'
    })
  },

  /**
   * 管理员删除评论
   * @param {Number} commentId - 评论ID
   * @returns {Promise} - 返回删除结果
   */
  adminDeleteComment(commentId) {
    return request({
      url: `/api/comment/admin/${commentId}`,
      method: 'delete'
    })
  },

  /**
   * 分页查询评论列表
   * @param {Object} params - 查询参数，包含page、size、targetType、targetId等
   * @returns {Promise} - 返回分页评论列表
   */
  getCommentPage(params) {
    return request({
      url: '/api/comment/page',
      method: 'get',
      params
    })
  },

  /**
   * 获取评论详情
   * @param {Number} commentId - 评论ID
   * @returns {Promise} - 返回评论详情信息
   */
  getCommentById(commentId) {
    return request({
      url: `/api/comment/${commentId}`,
      method: 'get'
    })
  },

  /**
   * 获取指定目标的评论列表
   * @param {Number} targetType - 目标类型（1-活动，2-服务，3-场地）
   * @param {Number} targetId - 目标ID
   * @param {Number} pageNum - 页码，默认为1
   * @param {Number} pageSize - 每页数量，默认为10
   * @returns {Promise} - 返回评论列表
   */
  getCommentsByTarget(targetType, targetId, pageNum = 1, pageSize = 10) {
    return request({
      url: '/api/comment/target',
      method: 'get',
      params: {
        targetType,
        targetId,
        pageNum,
        pageSize
      }
    })
  },

  /**
   * 获取评论统计信息
   * @returns {Promise} - 返回评论统计信息
   */
  getCommentStatistics() {
    return request({
      url: '/api/comment/statistics',
      method: 'get'
    })
  },

  /**
   * 分页查询评分列表
   * @param {Object} params - 查询参数，包含page、size、targetType等
   * @returns {Promise} - 返回分页评分列表
   */
  getRatingPage(params) {
    return request({
      url: '/api/comment/rating/page',
      method: 'get',
      params
    })
  },

  /**
   * 获取评分统计信息
   * @returns {Promise} - 返回评分统计信息
   */
  getRatingStatistics() {
    return request({
      url: '/api/comment/rating/statistics',
      method: 'get'
    })
  },

  /**
   * 根据目标类型获取评分统计信息
   * @param {Number} targetType - 目标类型（1-活动，2-服务，3-场地）
   * @returns {Promise} - 返回指定类型的评分统计信息
   */
  getRatingStatisticsByTargetType(targetType) {
    return request({
      url: `/api/comment/rating/statistics/${targetType}`,
      method: 'get'
    })
  }
}
