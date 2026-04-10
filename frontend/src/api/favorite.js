import request from '@/utils/request'

/**
 * 收藏API接口集合
 * 提供用户收藏功能相关的API调用方法，包括收藏添加、删除、查询等
 */
export const favoriteApi = {
  /**
   * 获取用户收藏列表
   * @param {Object} params - 查询参数，包含userId、targetType等
   * @returns {Promise} - 返回用户收藏列表
   */
  getFavorites(params) {
    return request({
      url: '/api/favorites',
      method: 'get',
      params
    })
  },
  
  /**
   * 添加收藏
   * @param {Object} data - 收藏数据，包含userId、targetType、targetId等
   * @returns {Promise} - 返回添加结果
   */
  addFavorite(data) {
    return request({
      url: '/api/favorites',
      method: 'post',
      data
    })
  },
  
  /**
   * 取消收藏
   * @param {Object} params - 查询参数，包含userId、targetType、targetId等
   * @returns {Promise} - 返回取消结果
   */
  removeFavorite(params) {
    return request({
      url: '/api/favorites',
      method: 'delete',
      params
    })
  },
  
  /**
   * 检查是否已收藏
   * @param {Object} params - 查询参数，包含userId、targetType、targetId等
   * @returns {Promise} - 返回是否已收藏的结果
   */
  checkFavorite(params) {
    return request({
      url: '/api/favorites/check',
      method: 'get',
      params
    })
  },
  
  /**
   * 切换收藏状态（如果已收藏则取消，未收藏则添加）
   * @param {Object} data - 收藏数据，包含userId、targetType、targetId等
   * @returns {Promise} - 返回切换后的收藏状态
   */
  toggleFavorite(data) {
    return request({
      url: '/api/favorites/toggle',
      method: 'post',
      data
    })
  }
}
