import request from '@/utils/request'

/**
 * 支付API接口集合
 * 提供支付相关的API调用方法，包括支付创建、状态查询、历史记录等
 */
export const paymentApi = {
  /**
   * 创建支付订单
   * @param {Object} data - 支付数据，包含bookingId、amount、paymentMethod等
   * @returns {Promise} - 返回支付订单信息
   */
  createPayment(data) {
    return request({
      url: '/api/payment/create',
      method: 'post',
      data: data
    })
  },
  
  /**
   * 获取支付状态
   * @param {Number} paymentId - 支付ID
   * @returns {Promise} - 返回支付状态信息
   */
  getPaymentStatus(paymentId) {
    return request({
      url: `/api/payment/${paymentId}/status`,
      method: 'get'
    })
  },
  
  /**
   * 根据预约ID获取支付信息
   * @param {Number} bookingId - 预约ID
   * @returns {Promise} - 返回支付信息
   */
  getPaymentByBookingId(bookingId) {
    return request({
      url: `/api/payment/booking/${bookingId}`,
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
  },
  
  /**
   * 获取支付历史记录
   * @param {Object} params - 查询参数，包含page、size、userId等
   * @returns {Promise} - 返回分页支付历史记录
   */
  getPaymentHistory(params) {
    return request({
      url: '/api/payment/page',
      method: 'get',
      params: params
    })
  }
}
