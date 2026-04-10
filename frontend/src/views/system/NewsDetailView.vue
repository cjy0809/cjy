<template>
  <div class="system-page">
    <div class="system-page-title">
      <div class="system-page-title-text">
        <button class="back-button" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <span class="system-page-title-icon">📢</span>
        新闻详情
      </div>
      <div class="action-buttons">
        <button class="system-button system-button-primary" @click="goBack">
          返回列表
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="40">
        <Loading />
      </el-icon>
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error-container">
      <el-result
        icon="error"
        title="加载失败"
        :sub-title="error"
      >
        <template #extra>
          <el-button type="primary" @click="fetchNewsDetail">重试</el-button>
          <el-button @click="goBack">返回</el-button>
        </template>
      </el-result>
    </div>

    <div v-else-if="newsDetail" class="news-detail-container">
      <!-- 新闻基本信息 -->
      <div class="detail-card">
        <div class="detail-card-header">
          <h2>{{ newsDetail.title }}</h2>
          <div class="status-tag" :class="getStatusClass(newsDetail.status)">
            {{ getStatusText(newsDetail.status) }}
          </div>
        </div>
        
        <div class="detail-card-body">
          <div class="detail-row">
            <div class="detail-label">新闻类型：</div>
            <div class="detail-value">{{ getNewsTypeLabel(newsDetail.type) }}</div>
          </div>
          
          <div class="detail-row">
            <div class="detail-label">发布时间：</div>
            <div class="detail-value">{{ formatDateTime(newsDetail.publishTime) }}</div>
          </div>
          
          <div class="detail-row">
            <div class="detail-label">浏览次数：</div>
            <div class="detail-value">{{ newsDetail.viewCount }}</div>
          </div>
          
          <div class="detail-row">
            <div class="detail-label">发布者：</div>
            <div class="detail-value">{{ newsDetail.publisherName || '未知' }}</div>
          </div>
        </div>
      </div>
      
      <!-- 新闻内容 -->
      <div class="detail-card">
        <div class="detail-card-header">
          <h3>新闻内容</h3>
        </div>
        
        <div class="detail-card-body">
          <div class="news-content">
            {{ newsDetail.content }}
          </div>
        </div>
      </div>

      <!-- 新闻评论 -->
      <CommentList
        v-if="newsDetail"
        target-type="news"
        :target-id="newsDetail.id"
        :admin-mode="isAdmin"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Loading } from '@element-plus/icons-vue'
import { newsApi } from '@/api/news'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'
import CommentList from '@/components/common/CommentList.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 新闻详情数据
const newsDetail = ref(null)
const loading = ref(true)
const error = ref('')

// 判断是否为管理员
const isAdmin = computed(() => {
  return userStore.userInfo && (userStore.userInfo.role === 'admin' || userStore.userInfo.role === 'staff')
})

// 获取新闻详情
const fetchNewsDetail = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const newsId = route.params.id
    const response = await newsApi.getNewsById(newsId)
    
    // 添加调试日志，检查数据结构
    console.log('获取到的新闻详情数据:', response)
    if (response && response.publisherName) {
      console.log('发布者名称:', response.publisherName)
    } else {
      console.log('未找到发布者名称，publisherId:', response?.publisherId)
    }
    
    if (response) {
      newsDetail.value = response
      
      // 如果没有发布者名称但有发布者ID，尝试获取用户信息
      if (!response.publisherName && response.publisherId) {
        try {
          console.log('尝试获取用户信息，ID:', response.publisherId)
          const userResponse = await userApi.getUserById(response.publisherId)
          if (userResponse && userResponse.data && userResponse.data.name) {
            newsDetail.value.publisherName = userResponse.data.name
            console.log('成功获取用户名称:', userResponse.data.name)
          }
        } catch (userError) {
          console.error('获取用户信息失败:', userError)
        }
      }
      
      // 更新浏览次数
      try {
        await newsApi.updateViewCount(newsId)
        // 更新本地显示的浏览次数
        newsDetail.value.viewCount = (newsDetail.value.viewCount || 0) + 1
      } catch (viewError) {
        console.warn('更新浏览次数失败:', viewError)
        // 不显示错误消息，不影响主要功能
      }
    } else {
      error.value = '获取新闻详情失败'
    }
  } catch (err) {
    console.error('获取新闻详情失败:', err)
    error.value = '获取新闻详情失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 返回新闻列表
const goBack = () => {
  router.push('/system/news')
}

// 获取状态样式类
const getStatusClass = (status) => {
  return status === 1 ? 'status-published' : 'status-draft'
}

// 获取状态文本
const getStatusText = (status) => {
  return status === 1 ? '已发布' : '草稿'
}

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

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  
  try {
    const formattedDateTime = typeof dateTime === 'string' ? dateTime.replace('T', ' ') : dateTime;
    return new Date(formattedDateTime).toLocaleString('zh-CN')
  } catch (error) {
    console.error('日期时间格式化失败:', error)
    return dateTime
  }
}

onMounted(() => {
  fetchNewsDetail()
})
</script>

<style scoped>
/* 返回按钮样式 */
.back-button {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: var(--system-text-primary);
  margin-right: var(--system-space-sm);
  padding: 8px;
  border-radius: 50%;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-button:hover {
  background-color: var(--system-bg-light);
}

/* 详情容器样式 */
.news-detail-container {
  display: flex;
  flex-direction: column;
  gap: var(--system-space-lg);
  padding: var(--system-space-lg);
}

/* 卡片样式 */
.detail-card {
  background-color: var(--system-bg-white);
  border-radius: var(--system-border-radius-lg);
  box-shadow: var(--system-shadow-sm);
  overflow: hidden;
}

.detail-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--system-space-lg);
  border-bottom: 1px solid var(--system-border-light);
  background-color: var(--system-bg-light);
}

.detail-card-header h2 {
  margin: 0;
  font-size: var(--system-font-size-xl);
  color: var(--system-text-primary);
}

.detail-card-header h3 {
  margin: 0;
  font-size: var(--system-font-size-lg);
  color: var(--system-text-primary);
}

.detail-card-body {
  padding: var(--system-space-lg);
}

/* 详情行样式 */
.detail-row {
  display: flex;
  margin-bottom: var(--system-space-md);
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  min-width: 100px;
  font-weight: 500;
  color: var(--system-text-secondary);
}

.detail-value {
  flex: 1;
  color: var(--system-text-primary);
}

/* 状态标签样式 */
.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: var(--system-border-radius-full);
  font-size: var(--system-font-size-sm);
  font-weight: 500;
}

.status-published {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-draft {
  background-color: #fffbe6;
  color: #faad14;
  border: 1px solid #ffe58f;
}

/* 新闻内容样式 */
.news-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.8;
  color: var(--system-text-primary);
  background-color: var(--system-bg-light);
  padding: var(--system-space-lg);
  border-radius: var(--system-border-radius-md);
  border-left: 4px solid var(--system-primary-color);
  text-align: justify;
  text-indent: 2em;
}

/* 加载和错误容器样式 */
.loading-container, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--system-space-xl);
  color: var(--system-text-secondary);
}

.loading-container p {
  margin-top: var(--system-space-md);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .detail-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--system-space-sm);
  }
  
  .detail-row {
    flex-direction: column;
    gap: var(--system-space-xs);
  }
  
  .detail-label {
    min-width: auto;
  }
  
  .news-detail-container {
    padding: var(--system-space-md);
  }
}
</style>