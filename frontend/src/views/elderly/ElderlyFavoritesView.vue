<template>
  <div class="elderly-favorites">
    <div class="elderly-card">
      <div class="elderly-card-header">
        <h2 class="elderly-card-title">我的收藏</h2>
        <div class="filter-tabs">
          <div 
            class="filter-tab" 
            :class="{ active: activeTab === 'activity' }"
            @click="changeTab('activity')"
          >
            活动
          </div>
          <div 
            class="filter-tab" 
            :class="{ active: activeTab === 'service_item' }"
            @click="changeTab('service_item')"
          >
            服务
          </div>
        </div>
      </div>
      
      <div class="elderly-card-body">
        <!-- 收藏列表 -->
        <div v-if="favorites.length > 0" class="favorites-list">
          <div 
            v-for="favorite in favorites" 
            :key="favorite.id" 
            class="favorite-item"
            @click="navigateToDetail(favorite)"
          >
            <div class="favorite-image" v-if="favorite.targetImage">
              <img :src="favorite.targetImage" :alt="favorite.targetTitle" />
            </div>
            <div class="favorite-image placeholder" v-else>
              <span class="placeholder-icon">
                {{ favorite.targetType === 'activity' ? '🎉' : '🤝' }}
              </span>
            </div>
            
            <div class="favorite-content">
              
              <h3 class="favorite-title">{{ favorite.targetTitle }}</h3>
              <p class="favorite-description">{{ favorite.targetDescription }}</p>
              <div class="favorite-time">
                收藏于：{{ formatDate(favorite.createTime) }}
              </div>
            </div>
            
            <div class="favorite-actions">
              <button 
                class="favorite-btn remove" 
                @click.stop="removeFavorite(favorite)"
                title="取消收藏"
              >
                ❤️
              </button>
            </div>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-icon">💔</div>
          <h3>暂无收藏</h3>
          <p>快去浏览活动和页面，收藏您感兴趣的内容吧！</p>
          <div class="empty-actions">
            <button class="elderly-btn primary" @click="navigateTo('/elderly/activities')">
              浏览活动
            </button>
            <button class="elderly-btn secondary" @click="navigateTo('/elderly/services')">
              浏览服务
            </button>
          </div>
        </div>
        
        <!-- 分页 -->
        <div v-if="total > pageSize" class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { favoriteApi } from '@/api/favorite'

export default {
  name: 'ElderlyFavoritesView',
  setup() {
    const router = useRouter()
    const favorites = ref([])
    const loading = ref(false)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const activeTab = ref('activity')
    
    // 获取收藏列表
    const fetchFavorites = async () => {
      loading.value = true
      try {
        const params = {
          page: currentPage.value,
          size: pageSize.value,
          targetType: activeTab.value
        }
        
        const response = await favoriteApi.getFavorites(params)
        if (response && response.data) {
          favorites.value = response.data.records || []
          total.value = response.data.total || 0
        }
      } catch (error) {
        console.error('获取收藏列表失败:', error)
        ElMessage.error('获取收藏列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // 切换标签
    const changeTab = (tab) => {
      activeTab.value = tab
      currentPage.value = 1
      fetchFavorites()
    }
    
    // 导航到详情页
    const navigateToDetail = (favorite) => {
      if (favorite.targetType === 'activity') {
        router.push(`/elderly/activities/${favorite.targetId}`)
      } else if (favorite.targetType === 'service_item') {
        router.push(`/elderly/services/${favorite.targetId}`)
      }
    }
    
    // 导航到指定页面
    const navigateTo = (path) => {
      router.push(path)
    }
    
    // 取消收藏
    const removeFavorite = async (favorite) => {
      try {
        await ElMessageBox.confirm(
          `确定要取消收藏"${favorite.targetTitle}"吗？`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        const params = {
          targetType: favorite.targetType,
          targetId: favorite.targetId
        }
        
        const response = await favoriteApi.removeFavorite(params)
        if (response && response.code === 200) {
          ElMessage.success('取消收藏成功')
          fetchFavorites()
        } else {
          ElMessage.error(response.message || '取消收藏失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消收藏失败:', error)
          ElMessage.error('取消收藏失败')
        }
      }
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
    }
    
    // 分页相关
    const handleSizeChange = (val) => {
      pageSize.value = val
      currentPage.value = 1
      fetchFavorites()
    }
    
    const handleCurrentChange = (val) => {
      currentPage.value = val
      fetchFavorites()
    }
    
    // 生命周期
    onMounted(() => {
      fetchFavorites()
    })
    
    // 页面激活时检查是否需要刷新数据
    onActivated(() => {
      // 检查是否需要刷新收藏列表
      if (sessionStorage.getItem('favoritesNeedRefresh') === 'true') {
        // 清除标记
        sessionStorage.removeItem('favoritesNeedRefresh')
        // 刷新数据
        fetchFavorites()
      }
    })
    
    return {
      favorites,
      loading,
      currentPage,
      pageSize,
      total,
      activeTab,
      changeTab,
      navigateToDetail,
      navigateTo,
      removeFavorite,
      formatDate,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.elderly-favorites {
  max-width: 1200px;
  margin: 0 auto;
}

.filter-tabs {
  display: flex;
  gap: var(--elderly-space-md);
}

.filter-tab {
  padding: var(--elderly-space-sm) var(--elderly-space-md);
  border-radius: var(--elderly-border-radius-md);
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.filter-tab:hover {
  background-color: var(--elderly-gray-light);
}

.filter-tab.active {
  background-color: var(--elderly-primary-color);
  color: white;
}

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: var(--elderly-space-md);
}

.favorite-item {
  display: flex;
  gap: var(--elderly-space-md);
  padding: var(--elderly-space-md);
  border-radius: var(--elderly-border-radius-md);
  background-color: var(--elderly-background-light);
  transition: all 0.3s;
  cursor: pointer;
}

.favorite-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--elderly-shadow-md);
}

.favorite-image {
  width: 120px;
  height: 120px;
  border-radius: var(--elderly-border-radius-md);
  overflow: hidden;
  flex-shrink: 0;
}

.favorite-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.favorite-image.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--elderly-gray-light);
}

.placeholder-icon {
  font-size: 3rem;
}

.favorite-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--elderly-space-xs);
}

.favorite-type {
  font-size: var(--elderly-font-size-sm);
  color: var(--elderly-primary-color);
  font-weight: 600;
}

.favorite-title {
  font-size: var(--elderly-font-size-lg);
  font-weight: 600;
  margin: 0;
  color: var(--elderly-text-primary);
}

.favorite-description {
  font-size: var(--elderly-font-size-md);
  color: var(--elderly-text-secondary);
  line-height: var(--elderly-line-height-md);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0;
}

.favorite-time {
  font-size: var(--elderly-font-size-sm);
  color: var(--elderly-text-tertiary);
  margin-top: auto;
}

.favorite-actions {
  display: flex;
  align-items: flex-start;
}

.favorite-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 1.2rem;
  transition: all 0.3s;
}

.favorite-btn.remove {
  background-color: rgba(239, 68, 68, 0.1);
}

.favorite-btn.remove:hover {
  background-color: rgba(239, 68, 68, 0.2);
  transform: scale(1.1);
}

.empty-state {
  text-align: center;
  padding: var(--elderly-space-xl) 0;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: var(--elderly-space-md);
}

.empty-state h3 {
  font-size: var(--elderly-font-size-xl);
  margin-bottom: var(--elderly-space-sm);
  color: var(--elderly-text-secondary);
}

.empty-state p {
  font-size: var(--elderly-font-size-md);
  color: var(--elderly-text-tertiary);
  margin-bottom: var(--elderly-space-lg);
}

.empty-actions {
  display: flex;
  gap: var(--elderly-space-md);
  justify-content: center;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: var(--elderly-space-xl);
}

/* 老年用户界面按钮样式 */
.elderly-btn {
  padding: var(--elderly-space-sm) var(--elderly-space-md);
  border-radius: var(--elderly-border-radius-md);
  font-size: var(--elderly-font-size-md);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--elderly-transition-base);
  border: 2px solid transparent;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
  min-width: 140px;
  box-shadow: var(--elderly-shadow-sm);
}

.elderly-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--elderly-shadow-md);
}

.elderly-btn.primary {
  background-color: var(--elderly-primary-color);
  color: var(--elderly-bg-white);
  border-color: var(--elderly-primary-color);
}

.elderly-btn.primary:hover {
  background-color: var(--elderly-primary-hover);
  border-color: var(--elderly-primary-hover);
}

.elderly-btn.secondary {
  background-color: var(--elderly-bg-white);
  color: var(--elderly-primary-color);
  border-color: var(--elderly-primary-color);
}

.elderly-btn.secondary:hover {
  background-color: var(--elderly-primary-light);
  border-color: var(--elderly-primary-hover);
}
</style>