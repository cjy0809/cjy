<template>
  <div class="elderly-activities">
    <!-- 页面标题 -->
    <div class="elderly-card">
      <div class="elderly-card-header">
        <h2 class="elderly-card-title">社区活动</h2>
      </div>
      <div class="elderly-card-body">
        <p class="page-description">参加社区活动，丰富老年生活，结交新朋友</p>
      </div>
    </div>
    

    
    <!-- 活动列表 -->
    <div class="elderly-card">
      <div class="elderly-card-header">
        <h3 class="elderly-card-title">活动列表</h3>

      </div>
      <div class="elderly-card-body">
        <div class="elderly-list" v-if="filteredActivities.length > 0">
          <div 
            class="elderly-list-item activity-item" 
            v-for="activity in filteredActivities" 
            :key="activity.id"
            @click="viewActivityDetail(activity.id)"
          >
            <div class="activity-header">
              <h4 class="elderly-list-title">{{ activity.title }}</h4>
              <span class="elderly-tag" :class="getStatusTagClass(activity.calculatedStatus)">
                {{ getStatusText(activity.calculatedStatus) }}
              </span>
            </div>
            
            <p class="elderly-list-desc">{{ activity.content ? activity.content.substring(0, 50) + '...' : '' }}</p>
            
            <div class="activity-details">
              <div class="activity-detail-item">
                <span class="detail-label">时间：</span>
                <span class="detail-value">{{ formatDateTime(activity.startTime) }}</span>
              </div>
              <div class="activity-detail-item">
                <span class="detail-label">地点：</span>
                <span class="detail-value">{{ activity.location }}</span>
              </div>
              <div class="activity-detail-item">
                <span class="detail-label">人数：</span>
                <span class="detail-value">{{ activity.currentParticipants }}/{{ activity.maxParticipants }}人</span>
              </div>
            </div>
            
            <div class="activity-actions">
              <button 
                class="elderly-button elderly-button-primary" 
                v-if="activity.canRegister && !activity.isEnrolled"
                @click.stop="enrollActivity(activity.id)"
              >
                报名参加
              </button>
              <button 
                class="elderly-button elderly-button-default" 
                v-if="activity.canRegister && activity.isEnrolled"
                @click.stop="cancelEnrollment(activity.id)"
              >
                取消报名
              </button>
              <button 
                class="elderly-button elderly-button-success" 
                v-if="activity.calculatedStatus === 1 && activity.isEnrolled"
                @click.stop="viewActivityDetail(activity.id)"
              >
                查看详情
              </button>
            </div>
          </div>
        </div>
        
        <div class="empty-state" v-else>
          <p>暂无符合条件的活动</p>
        </div>
        
        <!-- 分页器 -->
        <div v-if="total > 0" class="elderly-pagination">
          <div class="elderly-pagination-info">
            <span>共 {{ total }} 个活动</span>
            <span>第 {{ currentPage }} / {{ totalPages }} 页</span>
          </div>
          <div class="elderly-pagination-controls">
            <button 
              class="elderly-button elderly-button-default" 
              @click="handlePageChange(1)"
              :disabled="currentPage === 1"
            >
              首页
            </button>
            <button 
              class="elderly-button elderly-button-default" 
              @click="handlePageChange(currentPage - 1)"
              :disabled="currentPage === 1"
            >
              上一页
            </button>
            
            <!-- 页码按钮 -->
            <div class="elderly-pagination-pages">
              <button 
                v-for="page in visiblePages" 
                :key="page"
                class="elderly-pagination-btn"
                :class="{ active: page === currentPage }"
                @click="handlePageChange(page)"
              >
                {{ page }}
              </button>
            </div>
            
            <button 
              class="elderly-button elderly-button-default" 
              @click="handlePageChange(currentPage + 1)"
              :disabled="currentPage === totalPages"
            >
              下一页
            </button>
            <button 
              class="elderly-button elderly-button-default" 
              @click="handlePageChange(totalPages)"
              :disabled="currentPage === totalPages"
            >
              末页
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { activityApi } from '@/api/activity'

/**
 * 老年用户活动列表视图组件
 * 
 * 功能说明：
 * - 展示社区活动列表
 * - 支持活动报名和取消报名
 * - 自动计算活动状态（未开始、进行中、已结束、已取消）
 * - 支持分页浏览活动
 * - 提供活动详情查看入口
 * 
 * @component ElderlyActivitiesView
 */
export default {
  name: 'ElderlyActivitiesView',
  setup() {
    const router = useRouter()
    const activities = ref([])
    
    // 分页相关状态
    const currentPage = ref(1)
    const pageSize = ref(6) // 每页显示6个活动，符合用户要求
    const total = ref(0)
    
    /**
     * 计算总页数
     * @returns {number} 总页数
     */
    const totalPages = computed(() => {
      return Math.ceil(filteredActivitiesWithoutPagination.value.length / pageSize.value)
    })
    
    /**
     * 计算可见的页码按钮
     * 根据总页数和当前页码，智能显示页码按钮
     * @returns {Array} 页码数组，包含数字和省略号
     */
    const visiblePages = computed(() => {
      const pages = []
      const total = totalPages.value
      const current = currentPage.value
      
      // 总是显示首页和末页
      if (total <= 7) {
        // 如果总页数小于等于7，显示所有页码
        for (let i = 1; i <= total; i++) {
          pages.push(i)
        }
      } else {
        // 显示当前页附近的页码
        pages.push(1)
        
        if (current > 4) {
          pages.push('...')
        }
        
        const start = Math.max(2, current - 2)
        const end = Math.min(total - 1, current + 2)
        
        for (let i = start; i <= end; i++) {
          pages.push(i)
        }
        
        if (current < total - 3) {
          pages.push('...')
        }
        
        pages.push(total)
      }
      
      return pages
    })
    
    /**
     * 处理页码变化
     * @param {number|string} page - 目标页码
     */
    const handlePageChange = (page) => {
      if (page === '...') return
      if (page < 1 || page > totalPages.value) return
      currentPage.value = page
    }
    
    /**
     * 格式化日期时间
     * @param {string} dateString - 日期时间字符串
     * @returns {string} 格式化后的日期时间字符串
     */
    const formatDateTime = (dateString) => {
      if (!dateString) return '待定'
      
      const date = new Date(dateString)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    }

    /**
     * 计算活动状态（基于当前时间）
     * @param {string} startTime - 活动开始时间
     * @param {string} endTime - 活动结束时间
     * @param {number} manualStatus - 手动设置的状态
     * @returns {number} 活动状态：0-未开始，1-进行中，2-已结束，3-已取消
     */
    const calculateActivityStatus = (startTime, endTime, manualStatus) => {
      // 如果手动状态是已取消，保持已取消状态
      if (manualStatus === 3) return 3;
      
      const now = new Date();
      const start = new Date(startTime);
      const end = new Date(endTime);
      
      // 检查日期是否有效
      if (isNaN(start.getTime()) || isNaN(end.getTime())) {
        return manualStatus;
      }
      
      // 根据当前时间自动设置状态
      if (now < start) {
        // 活动未开始
        return 0; // 未开始
      } else if (now >= start && now <= end) {
        // 活动进行中
        return 1; // 进行中
      } else {
        // 活动已结束
        return 2; // 已结束
      }
    }

    /**
     * 检查是否可以报名（活动开始前一周到活动结束前一小时）
     * @param {string} startTime - 活动开始时间
     * @param {string} endTime - 活动结束时间
     * @param {number} currentParticipants - 当前参与人数
     * @param {number} maxParticipants - 最大参与人数
     * @returns {boolean} 是否可以报名
     */
    const canRegisterActivity = (startTime, endTime, currentParticipants, maxParticipants) => {
      const now = new Date();
      const start = new Date(startTime);
      const end = new Date(endTime);
      
      // 检查日期是否有效
      if (isNaN(start.getTime()) || isNaN(end.getTime())) {
        return false;
      }
      
      // 计算活动开始时间前一周
      const oneWeekBeforeStart = new Date(start.getTime() - 7 * 24 * 60 * 60 * 1000);
      
      // 计算活动结束时间前一小时
      const oneHourBeforeEnd = new Date(end.getTime() - 60 * 60 * 1000);
      
      // 检查是否在报名时间窗口内（活动开始前一周到活动结束前一小时）
      const isInRegistrationWindow = now >= oneWeekBeforeStart && now < oneHourBeforeEnd;
      
      // 检查是否已满员
      const isNotFull = currentParticipants < maxParticipants;
      
      return isInRegistrationWindow && isNotFull;
    }

    /**
     * 计算属性：活动列表（未分页）
     * 为每个活动计算动态状态和是否可报名
     * 只显示未开始和进行中的活动
     * @returns {Array} 活动列表
     */
    const filteredActivitiesWithoutPagination = computed(() => {
      // 为每个活动计算动态状态
      const activitiesWithStatus = activities.value.map(activity => {
        const calculatedStatus = calculateActivityStatus(activity.startTime, activity.endTime, activity.status);
        return {
          ...activity,
          calculatedStatus,
          canRegister: canRegisterActivity(activity.startTime, activity.endTime, activity.enrolled, activity.maxParticipants)
        };
      });
      
      // 过滤：只显示未开始(0)和进行中(1)的活动
      const filtered = activitiesWithStatus.filter(activity => {
        return activity.calculatedStatus === 0 || activity.calculatedStatus === 1;
      });
      
      // 排序：可报名的活动排在前面
      return filtered.sort((a, b) => {
        // 可报名的活动排在前面
        if (a.canRegister && !b.canRegister) return -1;
        if (!a.canRegister && b.canRegister) return 1;
        
        // 如果都可报名或都不可报名，则按开始时间排序
        return new Date(a.startTime) - new Date(b.startTime);
      });
    })
    
    /**
     * 计算属性：筛选后的活动列表（已分页）
     * @returns {Array} 分页后的活动列表
     */
    const filteredActivities = computed(() => {
      const filtered = filteredActivitiesWithoutPagination.value
      // 更新总记录数
      total.value = filtered.length
      
      // 应用分页
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      return filtered.slice(start, end)
    })
    
    /**
     * 获取状态文本
     * @param {number} status - 活动状态
     * @returns {string} 状态文本
     */
    const getStatusText = (status) => {
      switch (status) {
        case 0:
          return '未开始'
        case 1:
          return '进行中'
        case 2:
          return '已结束'
        case 3:
          return '已取消'
        default:
          return '未知状态'
      }
    }
    
    /**
     * 获取状态标签样式类
     * @param {number} status - 活动状态
     * @returns {string} 样式类名
     */
    const getStatusTagClass = (status) => {
      switch (status) {
        case 0:
          return 'elderly-tag-primary'
        case 1:
          return 'elderly-tag-success'
        case 2:
          return 'elderly-tag-error'
        case 3:
          return 'elderly-tag-warning'
        default:
          return 'elderly-tag-primary'
      }
    }
    
    /**
     * 查看活动详情
     * @param {number} id - 活动ID
     */
    const viewActivityDetail = (id) => {
      router.push(`/elderly/activities/${id}`)
    }
    
    /**
     * 报名活动
     * @param {number} id - 活动ID
     */
    const enrollActivity = async (id) => {
      try {
        const response = await activityApi.registerActivity(id)
        
        if (response) {
          // 更新本地活动状态
          const activity = activities.value.find(a => a.id === id)
          if (activity) {
            activity.isEnrolled = true
            activity.enrolled += 1
          }
          
          ElMessage.success('报名成功！')
        } else {
          ElMessage.error('报名失败')
        }
      } catch (error) {
        console.error('报名失败:', error)
        ElMessage.error('报名失败，请重试')
      }
    }
    
    /**
     * 取消报名
     * @param {number} id - 活动ID
     */
    const cancelEnrollment = async (id) => {
      try {
        const response = await activityApi.cancelRegistration(id)
        
        if (response) {
          // 更新本地活动状态
          const activity = activities.value.find(a => a.id === id)
          if (activity) {
            activity.isEnrolled = false
            activity.enrolled -= 1
          }
          
          ElMessage.success('已取消报名')
        } else {
          ElMessage.error('取消报名失败')
        }
      } catch (error) {
        console.error('取消报名失败:', error)
        ElMessage.error('取消报名失败，请重试')
      }
    }
    

    
    /**
     * 从数据库获取活动数据
     */
    const fetchActivities = async () => {
      try {
        // 简化版API调用，直接获取数据
        const response = await activityApi.getActivityPage({
          pageNum: 1,
          pageSize: 20
        })
        
        console.log('完整API响应:', response)
        
        if (response && response.data && response.data.records) {
          console.log('活动记录数据:', response.data.records)
          activities.value = response.data.records
        } else {
          console.log('响应结构不正确:', response)
          activities.value = []
        }
      } catch (error) {
        console.error('获取活动数据失败:', error)
        activities.value = []
      }
    }
    
    /**
     * 生命周期钩子
     * 组件挂载时获取活动数据
     */
    onMounted(() => {
      console.log('组件挂载完成，开始获取活动数据')
      // 立即调用获取数据函数
      fetchActivities()
    })
    

    return {
      activities,
      filteredActivities,
      currentPage,
      pageSize,
      total,
      totalPages,
      visiblePages,
      getStatusTagClass,
      getStatusText,
      viewActivityDetail,
      enrollActivity,
      cancelEnrollment,
      calculateActivityStatus,
      canRegisterActivity,
      handlePageChange,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.elderly-activities {
  max-width: 1200px;
  margin: 0 auto;
}

.page-description {
  font-size: var(--elderly-font-size-lg);
  color: var(--elderly-text-secondary);
  line-height: var(--elderly-line-height-lg);
}



.list-info {
  font-size: var(--elderly-font-size-md);
  color: var(--elderly-text-secondary);
}

.activity-item {
  cursor: pointer;
  transition: all var(--elderly-transition-base);
}

.activity-item:hover {
  background-color: var(--elderly-bg-light);
  transform: translateY(-2px);
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--elderly-space-sm);
}

.activity-details {
  margin: var(--elderly-space-md) 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--elderly-space-sm);
}

.activity-detail-item {
  display: flex;
  align-items: center;
}

.detail-label {
  font-weight: 600;
  color: var(--elderly-text-secondary);
  margin-right: var(--elderly-space-xs);
}

.detail-value {
  color: var(--elderly-text-primary);
}

.activity-actions {
  margin-top: var(--elderly-space-md);
  display: flex;
  gap: var(--elderly-space-md);
}

.empty-state {
  text-align: center;
  padding: var(--elderly-space-xl) 0;
  color: var(--elderly-text-tertiary);
  font-size: var(--elderly-font-size-lg);
}

@media (max-width: 768px) {
  .activity-details {
    grid-template-columns: 1fr;
  }
  
  .activity-actions {
    flex-direction: column;
  }
  
  .activity-actions .elderly-button {
    width: 100%;
  }
}

/* 分页器样式 */
.elderly-pagination {
  margin-top: var(--elderly-space-lg);
  padding-top: var(--elderly-space-md);
  border-top: 1px solid var(--elderly-border-light);
  display: flex;
  flex-direction: column;
  gap: var(--elderly-space-md);
  align-items: center;
}

.elderly-pagination-info {
  color: var(--elderly-text-secondary);
  font-size: var(--elderly-font-size-md);
  display: flex;
  gap: var(--elderly-space-lg);
  align-items: center;
}

.elderly-pagination-controls {
  display: flex;
  gap: var(--elderly-space-md);
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
}

.elderly-pagination-pages {
  display: flex;
  gap: var(--elderly-space-xs);
  align-items: center;
}

.elderly-pagination-btn {
  min-width: 40px;
  height: 40px;
  padding: 0 var(--elderly-space-md);
  border: 1px solid var(--elderly-border-color);
  background-color: var(--elderly-bg-white);
  color: var(--elderly-text-primary);
  border-radius: var(--elderly-border-radius-md);
  cursor: pointer;
  transition: all var(--elderly-transition-base);
  font-size: var(--elderly-font-size-md);
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.elderly-pagination-btn:hover:not(.active):not(:disabled) {
  background-color: var(--elderly-bg-light);
  border-color: var(--elderly-primary-color);
  color: var(--elderly-primary-color);
}

.elderly-pagination-btn.active {
  background-color: var(--elderly-primary-color);
  border-color: var(--elderly-primary-color);
  color: white;
}

.elderly-pagination-btn:disabled {
  background-color: var(--elderly-bg-light);
  color: var(--elderly-text-disabled);
  border-color: var(--elderly-border-light);
  cursor: not-allowed;
}

/* 响应式分页器 */
@media (max-width: 768px) {
  .elderly-pagination {
    gap: var(--elderly-space-sm);
  }
  
  .elderly-pagination-info {
    flex-direction: column;
    gap: var(--elderly-space-xs);
    font-size: var(--elderly-font-size-sm);
  }
  
  .elderly-pagination-controls {
    gap: var(--elderly-space-sm);
  }
  
  .elderly-pagination-btn {
    min-width: 36px;
    height: 36px;
    padding: 0 var(--elderly-space-sm);
    font-size: var(--elderly-font-size-sm);
  }
}
</style>
