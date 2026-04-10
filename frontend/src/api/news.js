import request from '@/utils/request'

/**
 * 新闻API接口集合
 * 提供新闻资讯相关的API调用方法，包括新闻的创建、查询、更新、删除等
 */
export const newsApi = {
  /**
   * 分页查询新闻列表
   * @param {Object} params - 查询参数，包含page、size、title、status等
   * @returns {Promise} - 返回分页新闻列表
   */
  getNewsPage(params) {
    return request({
      url: '/api/news/page',
      method: 'get',
      params
    })
  },

  /**
   * 根据ID查询新闻详情
   * @param {Number} id - 新闻ID
   * @returns {Promise} - 返回新闻详情信息
   */
  getNewsById(id) {
    return request({
      url: `/api/news/${id}`,
      method: 'get'
    }).then(res => {
      // 处理后端返回的数据结构
      if (res && res.data) {
        return res.data;
      }
      return res;
    })
  },

  /**
   * 创建新闻（管理员功能）
   * @param {Object} data - 新闻数据，包含title、content、author等
   * @returns {Promise} - 返回创建结果
   */
  createNews(data) {
    return request({
      url: '/api/news',
      method: 'post',
      data
    })
  },

  /**
   * 更新新闻（管理员功能）
   * @param {Number} id - 新闻ID
   * @param {Object} data - 需要更新的新闻信息
   * @returns {Promise} - 返回更新结果
   */
  updateNews(id, data) {
    return request({
      url: `/api/news/${id}`,
      method: 'put',
      data
    })
  },

  /**
   * 删除新闻（管理员功能）
   * @param {Number} id - 新闻ID
   * @returns {Promise} - 返回删除结果
   */
  deleteNews(id) {
    return request({
      url: `/api/news/${id}`,
      method: 'delete'
    })
  },

  /**
   * 更新新闻浏览次数
   * @param {Number} id - 新闻ID
   * @returns {Promise} - 返回更新结果
   */
  updateViewCount(id) {
    return request({
      url: `/api/news/${id}/view`,
      method: 'get'
    })
  },

  /**
   * 获取公告新闻列表（用于公告区域显示）
   * @param {Number} limit - 返回数量限制，默认为5
   * @returns {Promise} - 返回公告新闻列表
   */
  getAnnouncements(limit = 5) {
    return request({
      url: '/api/news/announcements',
      method: 'get',
      params: { limit }
    })
  }
}
