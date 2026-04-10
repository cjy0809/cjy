import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { activityApi } from '@/api/activity'

/**
 * 活动状态管理Store
 * 管理社区中心活动的状态，包括活动列表、详情、创建、更新等操作
 */
export const useActivityStore = defineStore('activity', () => {
  // 状态定义
  const activities = ref([])
  const currentActivity = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(20)

  // 计算属性
  const activitiesList = computed(() => activities.value)
  const isLoading = computed(() => loading.value)

  /**
   * 获取活动列表
   * @param {Object} params - 查询参数
   * @returns {Promise} - 返回获取结果
   */
  const fetchActivities = async (params = {}) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await activityApi.getActivityPage({
        page: currentPage.value,
        size: pageSize.value,
        ...params
      })
      
      if (response.code === 200) {
        activities.value = response.data.records || []
        total.value = response.data.total || 0
        return { success: true }
      } else {
        error.value = response.message || '获取活动列表失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('获取活动列表失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取活动详情
   * @param {Number} id - 活动ID
   * @returns {Promise} - 返回活动详情
   */
  const fetchActivityDetail = async (id) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await activityApi.getActivityDetail(id)
      
      if (response.code === 200) {
        currentActivity.value = response.data
        return { success: true, data: response.data }
      } else {
        error.value = response.message || '获取活动详情失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('获取活动详情失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建活动
   * @param {Object} data - 活动数据
   * @returns {Promise} - 返回创建结果
   */
  const createActivity = async (data) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await activityApi.createActivity(data)
      
      if (response.code === 200) {
        // 重新获取活动列表以更新数据
        await fetchActivities()
        return { success: true, message: '创建成功' }
      } else {
        error.value = response.message || '创建活动失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('创建活动失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新活动
   * @param {Number} id - 活动ID
   * @param {Object} data - 更新数据
   * @returns {Promise} - 返回更新结果
   */
  const updateActivity = async (id, data) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await activityApi.updateActivity(id, data)
      
      if (response.code === 200) {
        // 重新获取活动列表以更新数据
        await fetchActivities()
        return { success: true, message: '更新成功' }
      } else {
        error.value = response.message || '更新活动失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('更新活动失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 报名活动
   * @param {Number} id - 活动ID
   * @returns {Promise} - 返回报名结果
   */
  const registerActivity = async (id) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await activityApi.registerActivity(id)
      
      if (response.code === 200) {
        // 重新获取活动详情以更新报名状态
        await fetchActivityDetail(id)
        // 重新获取活动列表以更新参与人数
        await fetchActivities()
        return { success: true, message: response.message || '报名成功' }
      } else {
        error.value = response.message || '报名活动失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('报名活动失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 取消报名
   * @param {Number} id - 活动ID
   * @returns {Promise} - 返回取消结果
   */
  const cancelRegistration = async (id) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await activityApi.cancelRegistration(id)
      
      if (response.code === 200) {
        // 重新获取活动详情以更新报名状态
        await fetchActivityDetail(id)
        // 重新获取活动列表以更新参与人数
        await fetchActivities()
        return { success: true, message: response.message || '取消报名成功' }
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
   * 删除活动
   * @param {Number} id - 活动ID
   * @returns {Promise} - 返回删除结果
   */
  const deleteActivity = async (id) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await activityApi.deleteActivity(id)
      
      if (response.code === 200) {
        // 重新获取活动列表以更新数据
        await fetchActivities()
        return { success: true, message: '删除成功' }
      } else {
        error.value = response.message || '删除活动失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('删除活动失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新活动状态
   * @param {Number} id - 活动ID
   * @param {Number} status - 新状态
   * @returns {Promise} - 返回更新结果
   */
  const updateActivityStatus = async (id, status) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await activityApi.updateActivityStatus(id, status)
      
      if (response.code === 200) {
        // 重新获取活动列表以更新数据
        await fetchActivities()
        return { success: true, message: '状态更新成功' }
      } else {
        error.value = response.message || '更新活动状态失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('更新活动状态失败:', err)
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
    activities,
    currentActivity,
    loading: isLoading,
    error,
    total,
    currentPage,
    pageSize,
    
    // 计算属性
    activitiesList,
    
    // 方法
    fetchActivities,
    fetchActivityDetail,
    createActivity,
    updateActivity,
    deleteActivity,
    updateActivityStatus,
    registerActivity,
    cancelRegistration,
    setCurrentPage,
    setPageSize
  }
})
