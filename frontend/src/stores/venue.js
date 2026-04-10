import { defineStore } from 'pinia'
import { venueApi } from '@/api/venue'

/**
 * 场地状态管理Store
 * 管理社区中心场地的状态，包括场地列表、详情查询等
 */
export const useVenueStore = defineStore('venue', {
  state: () => ({
    venues: [],           // 场地列表
    loading: false,       // 加载状态
    error: null          // 错误信息
  }),

  getters: {
    /**
     * 获取所有场地
     * @param {Object} state - store状态
     * @returns {Array} - 场地列表
     */
    allVenues: (state) => state.venues
  },

  actions: {
    /**
     * 获取场地列表
     * @returns {Promise} - 返回获取结果
     */
    async fetchVenues() {
      this.loading = true
      this.error = null
      try {
        const response = await venueApi.getVenuePage({ pageNum: 1, pageSize: 100 })
        this.venues = response.records || []
      } catch (error) {
        this.error = error.message || 'Failed to fetch venues'
        console.error('Error fetching venues:', error)
      } finally {
        this.loading = false
      }
    },

    /**
     * 根据ID获取场地详情
     * @param {Number} id - 场地ID
     * @returns {Promise} - 返回场地详情
     */
    async fetchVenueById(id) {
      this.loading = true
      this.error = null
      try {
        return await venueApi.getVenueById(id)
      } catch (error) {
        this.error = error.message || 'Failed to fetch venue'
        console.error(`Error fetching venue ${id}:`, error)
        return null
      } finally {
        this.loading = false
      }
    }
  }
})
