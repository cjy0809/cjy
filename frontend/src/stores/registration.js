import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { registrationApi } from '@/api/registration'

/**
 * 活动报名状态管理Store
 * 管理活动报名的状态，包括报名列表、详情、审核等操作
 */
export const useRegistrationStore = defineStore('registration', () => {
  // 状态定义
  const registrations = ref([])           // 报名列表
  const currentRegistration = ref(null)     // 当前报名记录
  const statistics = ref(null)            // 报名统计信息
  const loading = ref(false)              // 加载状态
  const error = ref(null)                 // 错误信息
  const total = ref(0)                   // 总记录数
  const currentPage = ref(1)            // 当前页码
  const pageSize = ref(20)               // 每页数量

  // 计算属性
  const registrationsList = computed(() => registrations.value)
  const isLoading = computed(() => loading.value)

  /**
   * 获取报名列表
   * @param {Object} params - 查询参数
   * @returns {Promise} - 返回获取结果
   */
  const fetchRegistrations = async (params = {}) => {
    loading.value = true
    error.value = null
    
    try {
      console.log('发送报名列表请求，参数:', {
        page: currentPage.value,
        size: pageSize.value,
        ...params
      })
      const response = await registrationApi.getRegistrationPage({
        page: currentPage.value,
        size: pageSize.value,
        ...params
      })
      console.log('收到报名列表响应:', response)
      
      if (response.code === 200) {
        registrations.value = (response.data.records || []).sort((a, b) => b.id - a.id)
        total.value = response.data.total || 0
        console.log('报名列表数据加载成功，记录数:', registrations.value.length)
        return { success: true }
      } else {
        error.value = response.message || '获取报名列表失败'
        console.error('获取报名列表失败，错误信息:', response.message)
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('获取报名列表失败，异常详情:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取报名详情
   * @param {Number} id - 报名ID
   * @returns {Promise} - 返回报名详情
   */
  const fetchRegistrationDetail = async (id) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await registrationApi.getRegistrationDetail(id)
      
      if (response.code === 200) {
        currentRegistration.value = response.data
        return { success: true, data: response.data }
      } else {
        error.value = response.message || '获取报名详情失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('获取报名详情失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建报名记录
   * @param {Object} data - 报名数据
   * @returns {Promise} - 返回创建结果
   */
  const createRegistration = async (data) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await registrationApi.createRegistration(data)
      
      if (response.code === 200) {
        // 重新获取报名列表以更新数据
        await fetchRegistrations()
        return { success: true, message: '创建成功' }
      } else {
        error.value = response.message || '创建报名失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('创建报名失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新报名记录
   * @param {Number} id - 报名ID
   * @param {Object} data - 更新数据
   * @returns {Promise} - 返回更新结果
   */
  const updateRegistration = async (id, data) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await registrationApi.updateRegistration(id, data)
      
      if (response.code === 200) {
        // 重新获取报名列表以更新数据
        await fetchRegistrations()
        return { success: true, message: '更新成功' }
      } else {
        error.value = response.message || '更新报名失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('更新报名失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除报名记录
   * @param {Number} id - 报名ID
   * @returns {Promise} - 返回删除结果
   */
  const deleteRegistration = async (id) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await registrationApi.deleteRegistration(id)
      
      if (response.code === 200) {
        // 重新获取报名列表以更新数据
        await fetchRegistrations()
        return { success: true, message: '删除成功' }
      } else {
        error.value = response.message || '删除报名失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('删除报名失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取用户报名记录
   * @param {Number} userId - 用户ID
   * @returns {Promise} - 返回用户报名记录
   */
  const fetchUserRegistrations = async (userId) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await registrationApi.getUserRegistrations(userId)
      
      if (response.code === 200) {
        return { success: true, data: response.data }
      } else {
        error.value = response.message || '获取用户报名记录失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('获取用户报名记录失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取活动报名记录
   * @param {Number} activityId - 活动ID
   * @returns {Promise} - 返回活动报名记录
   */
  const fetchActivityRegistrations = async (activityId) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await registrationApi.getActivityRegistrations(activityId)
      
      if (response.code === 200) {
        return { success: true, data: response.data }
      } else {
        error.value = response.message || '获取活动报名记录失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('获取活动报名记录失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 审核报名
   * @param {Number} id - 报名ID
   * @param {Number} status - 审核状态
   * @param {String} remark - 审核意见
   * @returns {Promise} - 返回审核结果
   */
  const approveRegistration = async (id, status, remark) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await registrationApi.approveRegistration(id, status, remark)
      
      if (response.code === 200) {
        // 重新获取报名列表以更新数据
        await fetchRegistrations()
        return { success: true, message: '审核成功' }
      } else {
        error.value = response.message || '审核报名失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('审核报名失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 取消报名
   * @param {Number} id - 报名ID
   * @returns {Promise} - 返回取消结果
   */
  const cancelRegistration = async (id) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await registrationApi.cancelRegistration(id)
      
      if (response.code === 200) {
        // 重新获取报名列表以更新数据
        await fetchRegistrations()
        return { success: true, message: '取消报名成功' }
      } else {
        error.value = response.message || '取消报名失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('取消报名失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 批量审核报名
   * @param {Array} registrationIds - 报名ID数组
   * @param {Number} status - 审核状态
   * @param {String} remark - 审核意见
   * @returns {Promise} - 返回批量审核结果
   */
  const batchApproveRegistrations = async (registrationIds, status, remark) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await registrationApi.batchApproveRegistrations(registrationIds, status, remark)
      
      if (response.code === 200) {
        // 重新获取报名列表以更新数据
        await fetchRegistrations()
        return { success: true, message: '批量审核成功' }
      } else {
        error.value = response.message || '批量审核失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('批量审核失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取报名统计信息
   * @returns {Promise} - 返回报名统计信息
   */
  const fetchRegistrationStatistics = async () => {
    loading.value = true
    error.value = null
    
    try {
      const response = await registrationApi.getRegistrationStatistics()
      
      if (response.code === 200) {
        statistics.value = response.data
        return { success: true, data: response.data }
      } else {
        error.value = response.message || '获取报名统计信息失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('获取报名统计信息失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 设置当前页码
   * @param {Number} page - 页码
   */
  const setCurrentPage = (page) => {
    currentPage.value = page
  }

  /**
   * 设置每页数量
   * @param {Number} size - 每页数量
   */
  const setPageSize = (size) => {
    pageSize.value = size
    currentPage.value = 1 // 重置为第一页
  }

  return {
    // 状态
    registrations,
    currentRegistration,
    statistics,
    loading: isLoading,
    error,
    total,
    currentPage,
    pageSize,
    
    // 计算属性
    registrationsList,
    
    // 方法
    fetchRegistrations,
    fetchRegistrationDetail,
    createRegistration,
    updateRegistration,
    deleteRegistration,
    fetchUserRegistrations,
    fetchActivityRegistrations,
    approveRegistration,
    cancelRegistration,
    batchApproveRegistrations,
    fetchRegistrationStatistics,
    setCurrentPage,
    setPageSize
  }
})
