<template>
  <div class="elderly-venues">
    <!-- 页面标题 -->
    <div class="elderly-card">
      <div class="elderly-card-header">
        <h2 class="elderly-card-title">场地预约</h2>
      </div>
      <div class="elderly-card-body">
        <p class="page-description">预约社区活动场地，组织聚会和活动</p>
      </div>
    </div>
    

    
    <!-- 场地列表 -->
    <div class="elderly-card">
      <div class="elderly-card-header">
        <h3 class="elderly-card-title">场地列表</h3>
      </div>
      <div class="elderly-card-body">
        <div class="venue-grid" v-if="!loading && filteredVenues.length > 0">
          <div 
            class="venue-card" 
            v-for="venue in paginatedVenues" 
            :key="venue.id"
            @click="viewVenueDetail(venue.id)"
          >
            
            <div class="venue-content">
              <h4 class="venue-title">{{ venue.name }}</h4>
              
              <div class="venue-description">
                {{ venue.description }}
              </div>
              
              <div class="venue-details">
                <div class="venue-detail-item">
                  <span class="detail-label">容纳人数：</span>
                  <span class="detail-value">{{ venue.capacity }}人</span>
                </div>
                <div class="venue-detail-item">
                  <span class="detail-label">开放时间：</span>
                  <span class="detail-value">{{ venue.openHours }}</span>
                </div>
                <div class="venue-detail-item">
                  <span class="detail-label">设备信息：</span>
                  <span class="detail-value">{{ venue.facilities }}</span>
                </div>
              </div>
              
              <div class="venue-status">
                <span class="status-label">状态：</span>
                <span 
                  class="status-value" 
                  :class="{
                    'status-available': venue.status === 1,
                    'status-maintenance': venue.status === 2,
                    'status-unavailable': venue.status === 0
                  }"
                >
                  {{ venue.statusText }}
                </span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="empty-state" v-else-if="!loading && filteredVenues.length === 0">
          <p>暂无符合条件的场地</p>
        </div>
        
        <div class="loading-state" v-if="loading">
          <p>正在加载数据...</p>
        </div>
        
        <!-- 分页器 -->
        <div class="pagination-container" v-if="!loading && filteredVenues.length > 0">
          <div class="pagination-info">
            共 {{ total }} 个场地 第 {{ currentPage }}/{{ totalPages }} 页
          </div>
          <div class="pagination-buttons">
            <button class="elderly-button elderly-button-default" @click="goToFirstPage" :disabled="currentPage === 1">
              首页
            </button>
            <button class="elderly-button elderly-button-default" @click="goToPrevPage" :disabled="currentPage === 1">
              上一页
            </button>
            <button 
              v-for="page in totalPages" 
              :key="page"
              class="elderly-button" 
              :class="{'elderly-button-primary': currentPage === page, 'elderly-button-default': currentPage !== page}"
              @click="goToPage(page)"
            >
              {{ page }}
            </button>
            <button class="elderly-button elderly-button-default" @click="goToNextPage" :disabled="currentPage === totalPages">
              下一页
            </button>
            <button class="elderly-button elderly-button-default" @click="goToLastPage" :disabled="currentPage === totalPages">
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
import { ElMessage } from 'element-plus'
import { venueApi } from '@/api/venue'

// 格式化日期为API需要的格式
const formatDateForAPI = (date) => {
  if (!date) {
    console.error('formatDateForAPI: 日期为空')
    return ''
  }
  
  let d
  try {
    // 确保创建有效的Date对象
    d = new Date(date)
    
    // 检查日期是否有效
    if (isNaN(d.getTime())) {
      console.error('formatDateForAPI: 无效的日期对象', date)
      return ''
    }
  } catch (error) {
    console.error('formatDateForAPI: 创建日期对象失败', error)
    return ''
  }
  
  try {
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const formattedDate = `${year}-${month}-${day}`
    
    // 验证格式化结果
    if (!/^\d{4}-\d{2}-\d{2}$/.test(formattedDate)) {
      console.error('formatDateForAPI: 格式化结果不符合预期', { input: date, output: formattedDate })
      return ''
    }
    
    console.log('格式化日期:', { input: date, output: formattedDate })
    return formattedDate
  } catch (error) {
    console.error('formatDateForAPI: 格式化日期失败', error)
    return ''
  }
}

export default {
  name: 'ElderlyVenuesView',
  setup() {
    const router = useRouter()
    const venues = ref([])
    
    // 添加加载状态
    const loading = ref(false)
    
    // 计算属性：场地列表
    const filteredVenues = computed(() => {
      return venues.value
    })
    
    // 分页相关变量
    const currentPage = ref(1)
    const pageSize = ref(6)
    
    // 计算属性：总场地数
    const total = computed(() => filteredVenues.value.length)
    
    // 计算属性：总页数
    const totalPages = computed(() => Math.ceil(total.value / pageSize.value))
    
    // 计算属性：分页后的场地列表
    const paginatedVenues = computed(() => {
      const startIndex = (currentPage.value - 1) * pageSize.value
      const endIndex = startIndex + pageSize.value
      return filteredVenues.value.slice(startIndex, endIndex)
    })
    
    // 分页方法
    const goToPage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
      }
    }
    
    const goToFirstPage = () => goToPage(1)
    const goToPrevPage = () => goToPage(currentPage.value - 1)
    const goToNextPage = () => goToPage(currentPage.value + 1)
    const goToLastPage = () => goToPage(totalPages.value)
    
    // 查看场地详情
    const viewVenueDetail = (id) => {
      router.push(`/elderly/venues/${id}`)
    }
    
    // 从数据库获取场地数据
    const fetchVenues = async () => {
      loading.value = true
      try {
        // 获取所有场地数据，不分页
        const response = await venueApi.getVenuePage({ pageNum: 1, pageSize: 1000 })
        console.log('场地数据API响应:', response)
        
        if (response && response.data && response.data.records) {
          venues.value = response.data.records.map(venue => ({
            id: venue.id,
            name: venue.name,
            description: venue.description || '',
            capacity: venue.capacity,
            facilities: venue.equipment || '基础设施',
            status: venue.status,
            statusText: venue.status === 1 ? '可预约' : (venue.status === 2 ? '维护中' : '不可预约'),
            openTime: venue.openTime,
            closeTime: venue.closeTime,
            openHours: `${venue.openTime}-${venue.closeTime}`
          }))
          console.log('处理后场地数据:', venues.value)
        } else {
          venues.value = []
          console.warn('场地数据格式异常:', response)
        }
      } catch (error) {
        console.error('获取场地数据失败:', error)
        venues.value = []
        ElMessage.error('获取场地数据失败')
      } finally {
        loading.value = false
      }
    }
    
    // 生命周期钩子
    onMounted(async () => {
      await fetchVenues()
    })
    
    return {
      venues,
      filteredVenues,
      paginatedVenues, // 导出分页后的场地列表
      loading, // 添加加载状态
      viewVenueDetail,
      // 分页相关
      currentPage,
      pageSize,
      total,
      totalPages,
      goToPage,
      goToFirstPage,
      goToPrevPage,
      goToNextPage,
      goToLastPage
    }
  }
}
</script>

<style scoped>
.elderly-venues {
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

.venue-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: var(--elderly-space-lg);
}

.venue-card {
  background-color: var(--elderly-bg-white);
  border: 2px solid var(--elderly-border-light);
  border-radius: var(--elderly-border-radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--elderly-transition-base);
  display: flex;
  flex-direction: column;
}

.venue-card:hover {
  border-color: var(--elderly-primary-color);
  box-shadow: var(--elderly-shadow-md);
  transform: translateY(-4px);
}



.venue-content {
  padding: var(--elderly-space-lg);
  flex: 1;
  display: flex;
  flex-direction: column;
}

.venue-title {
  font-size: var(--elderly-font-size-lg);
  font-weight: 600;
  color: var(--elderly-text-primary);
  margin-bottom: var(--elderly-space-xs);
}

.venue-description {
  font-size: var(--elderly-font-size-md);
  color: var(--elderly-text-secondary);
  line-height: var(--elderly-line-height-md);
  margin-bottom: var(--elderly-space-md);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.venue-details {
  margin-bottom: var(--elderly-space-md);
}

.venue-detail-item {
  display: flex;
  margin-bottom: var(--elderly-space-sm);
}

.detail-label {
  font-weight: 600;
  color: var(--elderly-text-secondary);
  margin-right: var(--elderly-space-xs);
  min-width: 80px;
}

.detail-value {
  color: var(--elderly-text-primary);
  flex: 1;
}

.venue-status {
  display: flex;
  align-items: center;
  margin-bottom: var(--elderly-space-md);
}

.status-label {
  font-weight: 600;
  color: var(--elderly-text-secondary);
  margin-right: var(--elderly-space-xs);
}

.status-value {
  font-weight: 500;
}

.status-available {
  color: var(--elderly-success-color);
}

.status-maintenance {
  color: #fa8c16;
}

.status-unavailable {
  color: var(--elderly-error-color);
}

.venue-actions {
  display: flex;
  gap: var(--elderly-space-md);
  margin-top: auto;
}

.empty-state {
  text-align: center;
  padding: var(--elderly-space-xl) 0;
  color: var(--elderly-text-tertiary);
  font-size: var(--elderly-font-size-lg);
}

/* 加载状态样式 */
.loading-state {
  text-align: center;
  padding: var(--elderly-space-xl) 0;
  color: var(--elderly-text-secondary);
  font-size: var(--elderly-font-size-lg);
}

@media (max-width: 768px) {
  .venue-grid {
    grid-template-columns: 1fr;
    gap: var(--elderly-space-md);
  }
  
  .booking-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--elderly-space-sm);
  }
  
  .booking-actions {
    flex-direction: column;
  }
}

/* 分页器样式 */
.pagination-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--elderly-space-md);
  margin-top: var(--elderly-space-xl);
  padding-top: var(--elderly-space-xl);
  border-top: 2px solid var(--elderly-border-light);
}

.pagination-info {
  font-size: var(--elderly-font-size-md);
  color: var(--elderly-text-secondary);
}

.pagination-buttons {
  display: flex;
  gap: var(--elderly-space-sm);
  align-items: center;
}

.pagination-buttons .elderly-button {
  min-width: 80px;
}
</style>