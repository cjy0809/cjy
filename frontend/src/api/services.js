import request from '@/utils/request'

/**
 * 服务API接口集合
 * 提供社区服务相关的所有API调用方法，包括服务项目查询、预约、评价等
 */
export const servicesApi = {
  /**
   * 获取所有服务项目（包括所有状态）
   * @returns {Promise} - 返回所有服务项目列表
   */
  getAllServiceItems() {
    return request({
      url: '/api/service-item/all',
      method: 'get'
    }).then(response => {
      console.log('获取所有服务项目API响应:', response);
      return response;
    }).catch(error => {
      console.error('获取所有服务项目API调用错误:', error);
      throw error;
    });
  },
  
  /**
   * 获取服务分类列表
   * @returns {Promise} - 返回服务分类列表
   */
  getServiceCategories() {
    console.log('开始调用获取服务分类API');
    return request({
      url: '/api/service-category',
      method: 'get'
    }).then(response => {
      console.log('获取服务分类API响应:', JSON.stringify(response));
      return response;
    }).catch(error => {
      console.error('获取服务分类API调用错误:', error);
      throw error;
    });
  },
  
  /**
   * 分页查询服务项目列表
   * @param {Object} params - 查询参数，包含page、size、categoryId、status等
   * @returns {Promise} - 返回分页服务项目列表
   */
  getServiceItems(params) {
    console.log('API调用参数:', params);
    return request({
      url: '/api/service-item/page',
      method: 'get',
      params: params
    }).then(response => {
      console.log('API原始响应:', response);
      return response;
    }).catch(error => {
      console.error('API调用错误:', error);
      throw error;
    });
  },
  
  /**
   * 获取服务项目详情
   * @param {Number} id - 服务项目ID
   * @returns {Promise} - 返回服务项目详情信息
   */
  getServiceItemDetail(id) {
    console.log('API调用 - 获取服务详情，ID:', id)
    return request({
      url: `/api/service-item/${id}`,
      method: 'get'
    }).then(response => {
      console.log('API响应 - 获取服务详情:', response)
      return response
    }).catch(error => {
      console.error('API错误 - 获取服务详情:', error)
      throw error
    })
  },
  
  /**
   * 创建服务项目（管理员功能）
   * @param {Object} data - 服务项目数据，包含name、description、price等
   * @returns {Promise} - 返回创建结果
   */
  createServiceItem(data) {
    return request({
      url: '/api/service-item',
      method: 'post',
      data: data
    })
  },
  
  /**
   * 更新服务项目（管理员功能）
   * @param {Number} id - 服务项目ID
   * @param {Object} data - 需要更新的服务项目信息
   * @returns {Promise} - 返回更新结果
   */
  updateServiceItem(id, data) {
    return request({
      url: `/api/service-item/${id}`,
      method: 'put',
      data: data
    })
  },
  
  /**
   * 删除服务项目（管理员功能）
   * @param {Number} id - 服务项目ID
   * @returns {Promise} - 返回删除结果
   */
  deleteServiceItem(id) {
    return request({
      url: `/api/service-item/${id}`,
      method: 'delete'
    })
  },
  
  /**
   * 预约服务
   * @param {Object} data - 预约数据，包含serviceId、bookingDate、timeSlot等
   * @returns {Promise} - 返回预约结果
   */
  bookService(data) {
    return request({
      url: '/api/service-reservation',
      method: 'post',
      data: data
    })
  },
  
  /**
   * 更新服务预约信息
   * @param {Number} id - 预约ID
   * @param {Object} data - 需要更新的预约信息
   * @returns {Promise} - 返回更新结果
   */
  updateServiceBooking(id, data) {
    return request({
      url: `/api/service-reservation/${id}`,
      method: 'put',
      data: data
    })
  },
  
  /**
   * 更新服务预约状态
   * @param {Number} id - 预约ID
   * @param {Number} status - 新状态（1-待确认，2-已确认，3-已完成，4-已取消）
   * @returns {Promise} - 返回更新结果
   */
  updateBookingStatus(id, status) {
    return request({
      url: `/api/service-reservation/${id}/status`,
      method: 'put',
      params: { status }
    })
  },
  
  /**
   * 取消服务预约（带退款）
   * @param {Number} id - 预约ID
   * @returns {Promise} - 返回取消结果
   */
  cancelServiceBooking(id) {
    return request({
      url: `/api/service-reservation/${id}/cancel`,
      method: 'put'
    })
  },
  
  /**
   * 删除服务预约
   * @param {Number} id - 预约ID
   * @returns {Promise} - 返回删除结果
   */
  deleteServiceBooking(id) {
    return request({
      url: `/api/service-reservation/${id}`,
      method: 'delete'
    })
  },
  
  /**
   * 获取用户服务预约列表
   * @param {Object} params - 查询参数，包含page、size、userId、serviceId、status等
   * @returns {Promise} - 返回分页预约列表
   */
  getUserServiceBookings(params) {
    // 将前端参数转换为后端需要的格式
    const backendParams = {
      pageNum: params.page || 1,
      pageSize: params.size || 10,
      userId: params.userId,
      serviceId: params.serviceId,
      status: params.status,
      userName: params.userName
    };
    return request({
      url: '/api/service-reservation/page',
      method: 'get',
      params: backendParams
    })
  },
  
  /**
   * 获取用户服务预约详情（包含服务信息）
   * @param {Object} params - 查询参数，包含userId
   * @returns {Promise} - 返回用户预约详情列表
   */
  getUserServiceBookingDetails(params) {
    // 将前端参数转换为后端需要的格式
    const backendParams = {
      userId: params.userId
    };
    return request({
      url: `/api/service-reservation/user/${params.userId}/details`,
      method: 'get'
    })
  },
  
  /**
   * 获取服务预约统计数据
   * @returns {Promise} - 返回预约统计信息，包括总数、待确认、已完成、已取消等
   */
  getServiceBookingStats() {
    return request({
      url: '/api/service-reservation/page', 
      method: 'get',
      params: {
        pageNum: 1,
        pageSize: 100 // 获取足够的数据来进行简单统计
      }
    }).then(response => {
      const data = response.data || [];
      const stats = {
        totalCount: data.length,
        pendingCount: data.filter(item => item.status === 1).length,
        completedCount: data.filter(item => item.status === 2).length,
        cancelledCount: data.filter(item => item.status === 3).length
      };
      return stats;
    }).catch(error => {
      console.warn('统计API调用失败，使用默认统计数据:', error);
      return {
        totalCount: 0,
        pendingCount: 0,
        completedCount: 0,
        cancelledCount: 0
      };
    });
  },
  
  /**
   * 提交服务评价
   * @param {Object} data - 评价数据，包含reservationId、rating、comment等
   * @returns {Promise} - 返回评价结果
   */
  submitServiceRating(data) {
    return request({
      url: '/api/service-rating',
      method: 'post',
      data: data
    })
  },
  
  /**
   * 检查预约是否已评价
   * @param {Number} reservationId - 预约ID
   * @returns {Promise} - 返回是否已评价的结果
   */
  checkReservationRated(reservationId) {
    return request({
      url: `/api/service-rating/reservation/${reservationId}/check`,
      method: 'get'
    })
  },
  
  /**
   * 取消支付
   * @param {Number} bookingId - 预约ID
   * @returns {Promise} - 返回取消支付结果
   */
  cancelPayment(bookingId) {
    return request({
      url: '/api/payment/cancel',
      method: 'post',
      data: { bookingId }
    })
  }
}
