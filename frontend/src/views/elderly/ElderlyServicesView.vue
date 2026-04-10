<template>
  <div class="elderly-services">
    <!-- 页面标题 -->
    <div class="elderly-card">
      <div class="elderly-card-header">
        <h2 class="elderly-card-title">社区服务</h2>
      </div>
      <div class="elderly-card-body">
        <p class="page-description">享受贴心社区服务，让生活更加便利舒适</p>
      </div>
    </div>
    

    
    <!-- 服务列表 -->
    <div class="elderly-card">
      <div class="elderly-card-header">
        <h3 class="elderly-card-title">服务列表</h3>
      </div>
      <div class="elderly-card-body">
        <div class="service-grid" v-if="filteredServices.length > 0">
          <div 
            class="service-card" 
            v-for="service in filteredServices" 
            :key="service.id"
            @click="viewServiceDetail(service.id)"
          >
            <div class="service-icon">
              {{ getServiceIcon(service) }}
            </div>
            
            <div class="service-content">
              <h4 class="service-title">{{ service.name }}</h4>
              <span class="service-category">{{ getCategoryName(service) }}</span>
              <p class="service-desc">{{ service.description }}</p>
              
              <div class="service-details">
                <div class="service-detail-item">
                  <span class="detail-label">价格：</span>
                  <span class="detail-value">¥{{ service.price }}</span>
                </div>
                <div class="service-detail-item">
                  <span class="detail-label">时长：</span>
                  <span class="detail-value">{{ service.duration }}分钟</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="empty-state" v-else>
          <p>暂无符合条件的社区服务</p>
        </div>
        
        <!-- 分页器 -->
        <div v-if="total > 0" class="elderly-pagination">
          <div class="elderly-pagination-info">
            <span>共 {{ total }} 项服务</span>
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
import { ElMessage } from 'element-plus'
import { servicesApi } from '@/api/services'

export default {
  name: 'ElderlyServicesView',
  setup() {
    const router = useRouter()
    const services = ref([])
    const serviceCategories = ref([])
    
    // 分页相关状态
    const currentPage = ref(1)
    const pageSize = ref(6) // 每页显示6个服务
    const total = ref(0)
    
    // 计算总页数
    const totalPages = computed(() => {
      return Math.ceil(filteredServicesWithoutPagination.value.length / pageSize.value)
    })
    
    // 计算可见的页码按钮
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
    
    // 计算属性：服务列表（未分页）
    const filteredServicesWithoutPagination = computed(() => {
      let result = services.value
      
      // 只显示启用中的服务（status=1 表示启用）
      result = result.filter(service => service.status === 1)
      
      return result
    })
    
    // 计算属性：筛选后的服务列表（已分页）
    const filteredServices = computed(() => {
      const filtered = filteredServicesWithoutPagination.value
      // 更新总记录数
      total.value = filtered.length
      
      // 应用分页
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      return filtered.slice(start, end)
    })
    
    // 获取服务图标
    const getServiceIcon = (service) => {
      const categoryId = service.categoryId || service.category_id
      // 确保类型一致
      const category = serviceCategories.value.find(c => String(c.id) === String(categoryId))
      const categoryName = category ? category.name : '其他'
      
      const icons = {
        '生活服务': '🛒',
        '健康服务': '🏥',
        '文化服务': '📚',
        '便民服务': '🏢',
        '志愿服务': '🤝'
      }
      return icons[categoryName] || '📋'
    }
    
    // 查看服务详情
    const viewServiceDetail = (id) => {
      router.push(`/elderly/services/${id}`)
    }
    
    // 处理页码变化
    const handlePageChange = (page) => {
      if (page === '...') return
      if (page < 1 || page > totalPages.value) return
      currentPage.value = page
    }
    

    
    // 获取服务分类名称
    const getCategoryName = (service) => {
      // 尝试使用两种可能的字段名
      const categoryId = service.categoryId || service.category_id
      console.log(`获取分类名称 - 服务ID: ${service.id}, 分类ID: ${categoryId}, 类型: ${typeof categoryId}`)
      // 确保类型一致，将所有ID转换为字符串进行比较
      const category = serviceCategories.value.find(c => String(c.id) === String(categoryId))
      console.log(`获取分类名称 - 找到分类: ${category ? category.name : '无'}`)
      return category ? category.name : '未知分类'
    }
    
    // 从数据库获取服务数据
    const fetchServices = async () => {
      try {
        const response = await servicesApi.getAllServiceItems()
        console.log('服务数据API响应:', response)
        // 检查响应格式 - 服务数据包装在data字段中
        if (response && response.data) {
          services.value = response.data
        } else {
          services.value = []
        }
        console.log('处理后服务数据:', services.value)
      } catch (error) {
        console.error('获取服务数据失败:', error)
        services.value = []
      }
    }
    
    // 从数据库获取服务分类
    const fetchServiceCategories = async () => {
      try {
        const response = await servicesApi.getServiceCategories()
        console.log('服务分类API响应:', response)
        // 检查响应格式 - 服务分类直接返回数组，不包装在data字段中
        if (Array.isArray(response)) {
          serviceCategories.value = response
        } else if (response && response.data) {
          serviceCategories.value = response.data
        } else {
          serviceCategories.value = []
        }
        console.log('处理后服务分类数据:', serviceCategories.value)
        // 输出分类ID类型
        serviceCategories.value.forEach(cat => {
          console.log(`分类ID: ${cat.id}, 类型: ${typeof cat.id}, 名称: ${cat.name}`)
        })
      } catch (error) {
        console.error('获取服务分类失败:', error)
        serviceCategories.value = []
      }
    }
    
    // 生命周期钩子
    onMounted(async () => {
      // 先获取服务分类，再获取服务列表
      await fetchServiceCategories()
      console.log('服务分类数据:', serviceCategories.value)
      await fetchServices()
      console.log('服务数据:', services.value)
      
      // 检查分类匹配情况
      services.value.forEach(service => {
        // 检查服务对象的所有属性
        console.log('服务对象:', service)
        // 尝试使用两种可能的字段名
        const categoryId = service.categoryId || service.category_id
        console.log(`服务ID: ${service.id}, 分类ID: ${categoryId}, 类型: ${typeof categoryId}`)
        // 确保类型一致
        const category = serviceCategories.value.find(c => String(c.id) === String(categoryId))
        console.log(`服务ID: ${service.id}, 分类ID: ${categoryId}, 匹配分类: ${category ? category.name : '无'}`)
      })
    })
    
    return {
      services,
      serviceCategories,
      filteredServices,
      currentPage,
      pageSize,
      total,
      totalPages,
      visiblePages,
      getCategoryName,
      getServiceIcon,
      viewServiceDetail,
      handlePageChange
    }
  }
}
</script>

<style scoped>
.elderly-services {
  max-width: 1200px;
  margin: 0 auto;
}

.page-description {
  font-size: var(--elderly-font-size-lg);
  color: var(--elderly-text-secondary);
  line-height: var(--elderly-line-height-lg);
}



.service-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: var(--elderly-space-lg);
}

.service-card {
  background-color: var(--elderly-bg-white);
  border: 2px solid var(--elderly-border-light);
  border-radius: var(--elderly-border-radius-lg);
  padding: var(--elderly-space-lg);
  cursor: pointer;
  transition: all var(--elderly-transition-base);
  display: flex;
  flex-direction: column;
}

.service-card:hover {
  border-color: var(--elderly-primary-color);
  box-shadow: var(--elderly-shadow-md);
  transform: translateY(-4px);
}

.service-icon {
  font-size: var(--elderly-font-size-xxxl);
  text-align: center;
  margin-bottom: var(--elderly-space-md);
}

.service-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.service-title {
  font-size: var(--elderly-font-size-lg);
  font-weight: 600;
  color: var(--elderly-text-primary);
  margin-bottom: var(--elderly-space-xs);
  text-align: center;
}

.service-category {
  align-self: center;
  background-color: var(--elderly-primary-light);
  color: var(--elderly-primary-color);
  padding: var(--elderly-space-xs) var(--elderly-space-sm);
  border-radius: var(--elderly-border-radius-sm);
  font-size: var(--elderly-font-size-sm);
  font-weight: 500;
  margin-bottom: var(--elderly-space-md);
}

.service-desc {
  font-size: var(--elderly-font-size-md);
  color: var(--elderly-text-secondary);
  line-height: var(--elderly-line-height-lg);
  margin-bottom: var(--elderly-space-md);
  flex: 1;
}

.service-details {
  margin-bottom: var(--elderly-space-md);
}

.service-detail-item {
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

.service-actions {
  display: flex;
  justify-content: center;
}

.empty-state {
  text-align: center;
  padding: var(--elderly-space-xl) 0;
  color: var(--elderly-text-tertiary);
  font-size: var(--elderly-font-size-lg);
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

@media (max-width: 768px) {
  .service-grid {
    grid-template-columns: 1fr;
    gap: var(--elderly-space-md);
  }
  
  .service-card {
    padding: var(--elderly-space-md);
  }
}
</style>