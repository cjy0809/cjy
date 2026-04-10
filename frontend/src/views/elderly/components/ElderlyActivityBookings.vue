<template>
  <div class="elderly-activity-bookings">


    <!-- 预约列表 -->
    <div class="elderly-card">
      <div class="elderly-card-body">
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>加载中...</p>
        </div>
        
        <div v-else-if="bookings.length === 0" class="empty-state">
          <div class="empty-icon">📅</div>
          <h3>暂无活动预约记录</h3>
          <p>您还没有预约任何活动</p>
          <button class="elderly-button elderly-button-primary" @click="goToActivities">
            <i class="el-icon-plus"></i> 去预约活动
          </button>
        </div>
        
        <div v-else class="bookings-list">
          <div 
            v-for="booking in bookings" 
            :key="booking.id" 
            class="booking-item"
          >
            <div class="booking-header">
              <h4 class="booking-title">{{ booking.activityTitle }}</h4>
              <span :class="['status-badge', getStatusClass(booking.status)]">
                {{ getStatusText(booking.status) }}
              </span>
            </div>
            
            <div class="booking-details">
              <div class="detail-item">
                <span class="detail-label">活动时间：</span>
                <span>{{ formatDateTime(booking.activityStartTime) }} - {{ formatDateTime(booking.activityEndTime) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">活动地点：</span>
                <span>{{ booking.activityLocation || '社区活动中心' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">报名时间：</span>
                <span>{{ formatDateTime(booking.createTime) }}</span>
              </div>
              <div v-if="booking.remark" class="detail-item">
                <span class="detail-label">备注：</span>
                <span>{{ booking.remark }}</span>
              </div>
            </div>
            
            <div class="booking-actions">
              <button 
                v-if="booking.status === 1 || booking.status === 2" 
                class="elderly-button elderly-button-danger"
                @click="cancelBooking(booking.id)"
                title="取消当前活动的预约"
              >
                <i class="el-icon-close"></i> 取消预约
              </button>
              <button 
                class="elderly-button elderly-button-outline"
                @click="viewActivityDetail(booking.activityId)"
                title="查看活动的详细信息"
              >
                <i class="el-icon-view"></i> 查看活动详情
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="pagination-container">
      <button 
        class="elderly-button elderly-button-outline"
        :disabled="currentPage === 1"
        @click="currentPage--; fetchBookings()"
      >
        <i class="el-icon-arrow-left"></i> 上一页
      </button>
      <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
      <button 
        class="elderly-button elderly-button-outline"
        :disabled="currentPage === totalPages"
        @click="currentPage++; fetchBookings()"
      >
        下一页 <i class="el-icon-arrow-right"></i>
      </button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { registrationApi } from '@/api/registration'

export default {
  name: 'ElderlyActivityBookings',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    
    // 响应式数据
    const loading = ref(false)
    const bookings = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalPages = ref(0)
    
    // 计算属性
    const userId = computed(() => userStore.userInfo?.id)
    
    // 获取预约列表
    const fetchBookings = async () => {
      if (!userId.value) {
        ElMessage.error('用户信息获取失败，请重新登录')
        return
      }
      
      loading.value = true
      try {
        const response = await registrationApi.getUserRegistrations(userId.value)
        
        // 如果API返回的是数组，直接使用；如果是分页对象，提取记录
        let allBookings = []
        if (Array.isArray(response)) {
          allBookings = response
        } else if (response.data && Array.isArray(response.data)) {
          allBookings = response.data
        } else {
          allBookings = []
        }
        
        // 按照报名ID从创建时间最新的降序排列
        allBookings.sort((a, b) => b.id - a.id)
        
        // 计算分页
        totalPages.value = Math.ceil(allBookings.length / pageSize.value)
        const startIndex = (currentPage.value - 1) * pageSize.value
        const endIndex = startIndex + pageSize.value
        bookings.value = allBookings.slice(startIndex, endIndex)
        
      } catch (error) {
        console.error('获取活动预约列表失败:', error)
        ElMessage.error('获取预约列表失败，请稍后重试')
        bookings.value = []
      } finally {
        loading.value = false
      }
    }
    
    // 取消预约
    const cancelBooking = (bookingId) => {
      ElMessageBox.confirm('确定要取消这个活动预约吗？', '取消预约', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await registrationApi.cancelRegistration(bookingId)
          ElMessage.success('预约已取消')
          fetchBookings() // 重新获取列表
        } catch (error) {
          console.error('取消预约失败:', error)
          ElMessage.error('取消预约失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消操作
      })
    }
    
    // 查看活动详情
    const viewActivityDetail = (activityId) => {
      router.push(`/elderly/activities/${activityId}`)
    }
    
    // 跳转到活动页面
    const goToActivities = () => {
      router.push('/elderly/activities')
    }
    
    // 获取状态文本
    const getStatusText = (status) => {
      console.log('Activity getStatusText called with status:', status, 'type:', typeof status)
      const statusMap = {
        1: '已报名',
        2: '已通过',
        3: '已拒绝',
        4: '已取消'
      }
      const result = statusMap[status] || '未知状态'
      console.log('Activity getStatusText result:', result)
      return result
    }
    
    // 获取状态样式类
    const getStatusClass = (status) => {
      const classMap = {
        1: 'status-registered',
        2: 'status-approved',
        3: 'status-rejected',
        4: 'status-cancelled'
      }
      return classMap[status] || ''
    }
    
    // 格式化日期时间
    const formatDateTime = (dateTime) => {
      if (!dateTime) return '未设置'
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    // 生命周期
    onMounted(() => {
      fetchBookings()
    })
    
    return {
      loading,
      bookings,
      currentPage,
      totalPages,
      fetchBookings,
      cancelBooking,
      viewActivityDetail,
      goToActivities,
      getStatusText,
      getStatusClass,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.elderly-activity-bookings {
  padding: 20px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--elderly-space-xl) 0;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid var(--elderly-bg-light);
  border-top: 4px solid var(--elderly-primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: var(--elderly-space-md);
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: var(--elderly-space-xxl) var(--elderly-space-lg);
}

.empty-icon {
  font-size: var(--elderly-font-size-xxxl);
  margin-bottom: var(--elderly-space-md);
}

.empty-state h3 {
  margin: 0 0 var(--elderly-space-sm) 0;
  font-size: var(--elderly-font-size-lg);
  color: var(--elderly-text-primary);
}

.empty-state p {
  margin: 0 0 var(--elderly-space-lg) 0;
  color: var(--elderly-text-secondary);
  font-size: var(--elderly-font-size-md);
}

.bookings-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.booking-item {
  border: 1px solid var(--elderly-border-color);
  border-radius: var(--elderly-border-radius-lg);
  padding: var(--elderly-space-lg);
  background-color: var(--elderly-bg-white);
  transition: all var(--elderly-transition-base);
  position: relative;
  overflow: hidden;
  margin-bottom: var(--elderly-space-md);
}

.booking-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.05), transparent);
  transition: left 0.5s;
}

.booking-item:hover::before {
  left: 100%;
}

.booking-item:hover {
  box-shadow: var(--elderly-shadow-md);
  transform: translateY(-2px);
}

.booking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.booking-title {
  margin: 0;
  font-size: 18px;
  color: #333;
  font-weight: 600;
}

.status-badge {
  padding: var(--elderly-space-xs) var(--elderly-space-sm);
  border-radius: var(--elderly-border-radius-sm);
  font-size: var(--elderly-font-size-sm);
  font-weight: 500;
}

.status-pending {
  background-color: var(--elderly-warning-light);
  color: var(--elderly-warning-color);
}

.status-registered {
  background-color: var(--elderly-info-light);
  color: var(--elderly-info-color);
}

.status-approved {
  background-color: var(--elderly-success-light);
  color: var(--elderly-success-color);
}

.status-rejected {
  background-color: var(--elderly-error-light);
  color: var(--elderly-error-color);
}

.status-cancelled {
  background-color: var(--elderly-bg-gray);
  color: var(--elderly-text-tertiary);
}

.booking-details {
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 16px;
}

.detail-label {
  font-weight: 500;
  color: #555;
  min-width: 90px;
}

.booking-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--elderly-space-md);
  margin-top: var(--elderly-space-xl);
}

.page-info {
  font-weight: 500;
  color: var(--elderly-text-primary);
  font-size: var(--elderly-font-size-md);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .elderly-activity-bookings {
    padding: var(--elderly-space-md);
  }
  
  .booking-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--elderly-space-sm);
  }
  
  .booking-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .booking-actions button {
    width: 100%;
    margin-bottom: var(--elderly-space-sm);
  }
  
  .booking-item {
    padding: var(--elderly-space-md);
  }
}
</style>