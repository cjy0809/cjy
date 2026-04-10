import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

/**
 * 用户管理状态管理Store
 * 管理系统用户的状态，包括用户列表、角色管理、密码重置等
 */
export const useUserManagementStore = defineStore('userManagement', () => {
  // 状态定义
  const users = ref([])           // 用户列表
  const userRoles = ref([])       // 用户角色列表
  const loading = ref(false)       // 加载状态
  const error = ref(null)         // 错误信息
  const total = ref(0)            // 总记录数
  const currentPage = ref(1)       // 当前页码
  const pageSize = ref(10)         // 每页数量

  // 计算属性
  const usersList = computed(() => users.value)
  const rolesList = computed(() => userRoles.value)
  const isLoading = computed(() => loading.value)

  /**
   * 获取用户列表
   * @param {Object} params - 查询参数
   * @returns {Promise} - 返回获取结果
   */
  const fetchUsers = async (params = {}) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await request({
        url: '/api/users',
        method: 'get',
        params: {
          page: currentPage.value,
          size: pageSize.value,
          ...params
        }
      })
      
      if (response.code === 200) {
        users.value = response.data.records || []
        total.value = response.data.total || 0
        return { success: true }
      } else {
        error.value = response.message || '获取用户列表失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('获取用户列表失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取用户详情
   * @param {Number} id - 用户ID
   * @returns {Promise} - 返回用户详情
   */
  const fetchUserDetail = async (id) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await request({
        url: `/api/users/${id}`,
        method: 'get'
      })
      
      if (response.code === 200) {
        return { success: true, data: response.data }
      } else {
        error.value = response.message || '获取用户详情失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('获取用户详情失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建用户
   * @param {Object} data - 用户数据
   * @returns {Promise} - 返回创建结果
   */
  const createUser = async (data) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await request({
        url: '/api/users',
        method: 'post',
        data
      })
      
      if (response.code === 200) {
        // 重新获取用户列表以更新数据
        await fetchUsers()
        return { success: true, message: response.message || '创建用户成功' }
      } else {
        error.value = response.message || '创建用户失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('创建用户失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新用户信息
   * @param {Number} id - 用户ID
   * @param {Object} data - 更新数据
   * @returns {Promise} - 返回更新结果
   */
  const updateUser = async (id, data) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await request({
        url: `/api/users/${id}`,
        method: 'put',
        data
      })
      
      if (response.code === 200) {
        // 重新获取用户列表以更新数据
        await fetchUsers()
        return { success: true, message: response.message || '更新用户成功' }
      } else {
        error.value = response.message || '更新用户失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('更新用户失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取用户角色列表
   * @returns {Promise} - 返回角色列表
   */
  const fetchRoles = async () => {
    loading.value = true
    error.value = null
    
    try {
      const response = await request({
        url: '/api/roles',
        method: 'get'
      })
      
      if (response.code === 200) {
        userRoles.value = response.data || []
        return { success: true }
      } else {
        error.value = response.message || '获取角色列表失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('获取角色列表失败:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 重置用户密码
   * @param {Number} id - 用户ID
   * @param {String} password - 新密码
   * @returns {Promise} - 返回重置结果
   */
  const resetPassword = async (id, password) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await request({
        url: `/api/users/${id}/reset-password`,
        method: 'post',
        data: { password }
      })
      
      if (response.code === 200) {
        return { success: true, message: response.message || '密码重置成功' }
      } else {
        error.value = response.message || '密码重置失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      error.value = '网络错误，请稍后重试'
      console.error('密码重置失败:', err)
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
    users,
    userRoles,
    loading: isLoading,
    error,
    total,
    currentPage,
    pageSize,
    
    // 计算属性
    usersList,
    rolesList,
    
    // 方法
    fetchUsers,
    fetchUserDetail,
    createUser,
    updateUser,
    fetchRoles,
    resetPassword,
    setCurrentPage,
    setPageSize
  }
})
