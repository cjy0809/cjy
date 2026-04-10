<template>
  <div class="elderly-venue-bookings">
    
    <!-- 预约列表 -->
    <div class="elderly-card">
      <div class="elderly-card-body">
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>加载中...</p>
        </div>
        
        <div v-else-if="bookings.length === 0" class="empty-state">
          <div class="empty-icon">🏢</div>
          <h3>暂无场地预约记录</h3>
          <p>您还没有预约任何场地</p>
          <button class="elderly-button elderly-button-primary" @click="goToVenues">
            <i class="el-icon-plus"></i> 去预约场地
          </button>
        </div>
        
        <div v-else class="bookings-list">
          <div 
            v-for="booking in bookings" 
            :key="booking.id" 
            class="booking-item"
          >
            <div class="booking-header">
              <h4 class="booking-title">{{ booking.venueName || '社区场地' }}</h4>
              <span :class="['status-badge', getStatusClass(booking.status)]">
                {{ getStatusText(booking.status) }}
              </span>
            </div>
            
            <div class="booking-details">
              <div class="detail-item">
                <span class="detail-label">预约日期：</span>
                <span>{{ booking.reservationDate }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">预约时间：</span>
                <span>{{ booking.startTime }} - {{ booking.endTime }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">参与人数：</span>
                <span>{{ booking.participants || '1' }}人</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">预约用途：</span>
                <span>{{ booking.purpose || '未设置' }}</span>
              </div>
              <div v-if="booking.remark" class="detail-item">
                <span class="detail-label">备注：</span>
                <span>{{ booking.remark }}</span>
              </div>
            </div>
            
            <div class="booking-actions">
              <button 
                v-if="booking.status === 0 || booking.status === 1" 
                class="elderly-button elderly-button-danger"
                @click="cancelBooking(booking.id)"
                title="取消当前场地的预约"
              >
                <i class="el-icon-close"></i> 取消预约
              </button>
              <button 
                class="elderly-button elderly-button-outline"
                @click="viewVenueDetail(booking.venueId)"
                title="查看场地的详细信息"
              >
                <i class="el-icon-view"></i> 查看场地详情
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
import { reservationApi } from '@/api/reservation'

export default {
  name: 'ElderlyVenueBookings',
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
        const params = {
          userId: userId.value,
          page: currentPage.value,
          size: pageSize.value
        }
        
        // 使用预约API获取场地预约列表
        const response = await reservationApi.getReservationPage(params)
        
        // 处理API响应
        if (response && response.code === 200) {
          let allBookings = response.data?.records || []
          
          // 按照预约ID从创建时间最新的降序排列
          allBookings.sort((a, b) => b.id - a.id)
          
          bookings.value = allBookings
          totalPages.value = Math.ceil(response.data.total / pageSize.value)
        } else {
          bookings.value = []
          totalPages.value = 0
          ElMessage.error(response?.message || '获取预约列表失败')
        }
        
      } catch (error) {
        console.error('获取场地预约列表失败:', error)
        ElMessage.error('获取预约列表失败，请稍后重试')
        bookings.value = []
      } finally {
        loading.value = false
      }
    }
    
    // 取消预约
    const cancelBooking = (bookingId) => {
      ElMessageBox.confirm('确定要取消这个场地预约吗？', '取消预约', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await reservationApi.deleteReservation(bookingId)
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
    
    // 查看场地详情
    const viewVenueDetail = (venueId) => {
      router.push(`/elderly/venues/${venueId}`)
    }
    
    // 跳转到场地页面
    const goToVenues = () => {
      router.push('/elderly/venues')
    }
    
    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        0: '待审核',
        1: '已通过',
        2: '已拒绝',
        3: '已取消'
      }
      return statusMap[status] || '未知状态'
    }
    
    // 获取状态样式类
    const getStatusClass = (status) => {
      const classMap = {
        0: 'status-pending',
        1: 'status-approved',
        2: 'status-rejected',
        3: 'status-cancelled'
      }
      return classMap[status] || ''
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
      viewVenueDetail,
      goToVenues,
      getStatusText,
      getStatusClass
    }
  }
}
</script>

<style scoped>
.elderly-venue-bookings {
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
  .elderly-venue-bookings {
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