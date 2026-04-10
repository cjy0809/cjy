<template>
  <div class="elderly-news">
    <!-- 页面标题 -->
    <div class="elderly-card">
      <div class="elderly-card-header">
        <h2 class="elderly-card-title">新闻资讯</h2>
      </div>
      <div class="elderly-card-body">
        <p class="page-description">了解社区最新动态和政策解读，掌握第一手资讯</p>
      </div>
    </div>
    
    <!-- 新闻列表 -->
    <div class="elderly-card">
      <div class="elderly-card-header">
        <h3 class="elderly-card-title">新闻列表</h3>
      </div>
      <div class="elderly-card-body">
        <div class="elderly-list" v-if="filteredNews.length > 0">
          <div 
            class="elderly-list-item news-item" 
            v-for="news in filteredNews" 
            :key="news.id"
            @click="viewNewsDetail(news.id)"
          >
            <div class="news-header">
              <h4 class="elderly-list-title">{{ news.title }}</h4>
              <span class="elderly-tag" :class="getNewsTypeClass(news.type)">
                {{ getNewsTypeLabel(news.type) }}
              </span>
            </div>
            
            <p class="elderly-list-desc">{{ news.content ? news.content.substring(0, 100) + '...' : '' }}</p>
            
            <div class="news-details">
              <div class="news-detail-item">
                <span class="detail-label">发布时间：</span>
                <span class="detail-value">{{ formatDateTime(news.publishTime) }}</span>
              </div>
              <div class="news-detail-item">
                <span class="detail-label">浏览次数：</span>
                <span class="detail-value">{{ news.viewCount || 0 }}次</span>
              </div>
            </div>
            
            <div class="news-actions">
              <!-- 删除了查看详情按钮，用户可以直接点击新闻项查看详情 -->
            </div>
          </div>
        </div>
        
        <div class="empty-state" v-else>
          <p>暂无新闻信息</p>
        </div>
        
        <!-- 分页器 -->
        <div v-if="total > 0" class="elderly-pagination">
          <div class="elderly-pagination-info">
            <span>共 {{ total }} 条新闻</span>
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
import { newsApi } from '@/api/news'

export default {
  name: 'ElderlyNewsView',
  setup() {
    const router = useRouter()
    const news = ref([])
    
    // 分页相关状态
    const currentPage = ref(1)
    const pageSize = ref(6) // 每页显示6条新闻
    const total = ref(0)
    
    // 计算总页数
    const totalPages = computed(() => {
      return Math.ceil(filteredNewsWithoutPagination.value.length / pageSize.value)
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
    
    // 处理页码变化
    const handlePageChange = (page) => {
      if (page === '...') return
      if (page < 1 || page > totalPages.value) return
      currentPage.value = page
    }
    
    // 计算属性：新闻列表（未分页）
    const filteredNewsWithoutPagination = computed(() => {
      return news.value
    })
    
    // 计算属性：新闻列表（已分页）
    const filteredNews = computed(() => {
      const filtered = filteredNewsWithoutPagination.value
      // 更新总记录数
      total.value = filtered.length
      
      // 应用分页
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      return filtered.slice(start, end)
    })
    
    // 获取新闻类型标签
    const getNewsTypeLabel = (type) => {
      const typeNum = typeof type === 'string' ? parseInt(type) : type
      
      const typeMap = {
        0: '新闻',
        1: '公告',
        2: '政策解读'
      }
      return typeMap[typeNum] || `类型${typeNum}`
    }
    
    // 获取新闻类型样式类
    const getNewsTypeClass = (type) => {
      const typeNum = typeof type === 'string' ? parseInt(type) : type
      
      switch (typeNum) {
        case 0:
          return 'elderly-tag-primary'
        case 1:
          return 'elderly-tag-warning'
        case 2:
          return 'elderly-tag-success'
        default:
          return 'elderly-tag-primary'
      }
    }
    
    // 查看新闻详情
    const viewNewsDetail = (id) => {
      router.push(`/elderly/news/${id}`)
    }
    
    // 格式化日期时间
    const formatDateTime = (dateTime) => {
      if (!dateTime) return ''
      
      try {
        const formattedDateTime = typeof dateTime === 'string' ? dateTime.replace('T', ' ') : dateTime;
        return new Date(formattedDateTime).toLocaleDateString('zh-CN')
      } catch (error) {
        console.error('日期时间格式化失败:', error)
        return dateTime
      }
    }
    
    // 从数据库获取新闻数据
    const fetchNews = async () => {
      try {
        console.log('开始获取新闻数据...')
        
        // 分别获取新闻和政策解读类型的数据，只获取已发布的新闻（status=1）
        const [newsResponse, policyResponse] = await Promise.all([
          newsApi.getNewsPage({
            pageNum: 1,
            pageSize: 1000, // 获取足够多的数据
            type: 0, // 获取新闻类型的数据
            status: 1 // 只获取已发布的新闻
          }),
          newsApi.getNewsPage({
            pageNum: 1,
            pageSize: 1000, // 获取足够多的数据
            type: 2, // 获取政策解读类型的数据
            status: 1 // 只获取已发布的新闻
          })
        ])
        
        console.log('新闻API响应:', newsResponse)
        console.log('政策解读API响应:', policyResponse)
        
        // 合并两次获取的数据
        let allNews = []
        
        // 处理新闻数据 - 后端直接返回Page对象，不是包装在data中
        if (newsResponse && newsResponse.records) {
          console.log('新闻记录数据:', newsResponse.records)
          allNews = allNews.concat(newsResponse.records)
        } else {
          console.log('新闻响应结构不正确:', newsResponse)
        }
        
        // 处理政策解读数据
        if (policyResponse && policyResponse.records) {
          console.log('政策解读记录数据:', policyResponse.records)
          allNews = allNews.concat(policyResponse.records)
        } else {
          console.log('政策解读响应结构不正确:', policyResponse)
        }
        
        // 按发布时间排序（最新的在前）
        allNews.sort((a, b) => {
          const timeA = new Date(a.publishTime)
          const timeB = new Date(b.publishTime)
          return timeB - timeA
        })
        
        news.value = allNews
        console.log('合并后的新闻数据:', news.value)
        
        if (news.value.length === 0) {
          ElMessage.info('暂无新闻数据')
        }
      } catch (error) {
        console.error('获取新闻数据失败:', error)
        news.value = []
        ElMessage.error('获取新闻数据失败')
      }
    }
    
    // 生命周期钩子
    onMounted(() => {
      console.log('组件挂载完成，开始获取新闻数据')
      // 立即调用获取数据函数
      fetchNews()
    })
    
    return {
      news,
      filteredNews,
      currentPage,
      pageSize,
      total,
      totalPages,
      visiblePages,
      getNewsTypeLabel,
      getNewsTypeClass,
      viewNewsDetail,
      formatDateTime,
      handlePageChange
    }
  }
}
</script>

<style scoped>
.elderly-news {
  max-width: 1200px;
  margin: 0 auto;
}

.page-description {
  font-size: var(--elderly-font-size-lg);
  color: var(--elderly-text-secondary);
  line-height: var(--elderly-line-height-lg);
}

.news-item {
  cursor: pointer;
  transition: all var(--elderly-transition-base);
}

.news-item:hover {
  background-color: var(--elderly-bg-light);
  transform: translateY(-2px);
}

.news-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--elderly-space-sm);
}

.news-details {
  margin: var(--elderly-space-md) 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--elderly-space-sm);
}

.news-detail-item {
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

.news-actions {
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
  .news-details {
    grid-template-columns: 1fr;
  }
  
  .news-actions {
    flex-direction: column;
  }
  
  .news-actions .elderly-button {
    width: 100%;
  }
}
</style>