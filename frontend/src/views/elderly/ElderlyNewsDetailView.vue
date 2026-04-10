<template>
  <div class="elderly-news-detail">
    <div class="page-header">
      <div class="header-content">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/elderly/home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/elderly/news' }">新闻列表</el-breadcrumb-item>
          <el-breadcrumb-item>新闻详情</el-breadcrumb-item>
        </el-breadcrumb>
        <el-button type="primary" plain @click="handleBackToList" :icon="ArrowLeft">返回新闻列表</el-button>
      </div>
    </div>

    <div class="news-content" v-loading="loading">
      <div v-if="newsDetail" class="content-header">
        <h1 class="content-title">{{ newsDetail.title }}</h1>
      </div>
      
      <div v-if="newsDetail" class="content-meta">
        <div class="meta-item">
          <span class="meta-label">类型：</span>
          <span class="meta-value" :class="getNewsTypeClass(newsDetail.type)">
            {{ getNewsTypeLabel(newsDetail.type) }}
          </span>
        </div>
        <div class="meta-item">
          <span class="meta-label">发布时间：</span>
          <span class="meta-value">{{ formatDateTime(newsDetail.publishTime) }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">浏览次数：</span>
          <span class="meta-value">{{ newsDetail.viewCount || 0 }}次</span>
        </div>
      </div>
      
      <div v-if="newsDetail" class="content-body">
        <div class="news-content" v-html="formatNewsContent(newsDetail.content)"></div>
      </div>
      
      <!-- 新闻评论 -->
      <CommentList
        v-if="newsDetail"
        target-type="news"
        :target-id="newsDetail.id"
        :admin-mode="false"
        :show-rating="false"
      />
    </div>
    
    <!-- 空状态 -->
    <div v-if="!loading && !newsDetail" class="empty-state">
      <el-result
        icon="error"
        title="新闻不存在"
        sub-title="抱歉，您访问的新闻不存在或已被删除"
      >
        <template #extra>
          <el-button type="primary" @click="handleBackToList">返回新闻列表</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { newsApi } from '@/api/news'
import CommentList from '@/components/common/CommentList.vue'

export default {
  name: 'ElderlyNewsDetailView',
  components: {
    CommentList
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const newsDetail = ref(null)
    const loading = ref(true)
    
    // 获取新闻详情
    const fetchNewsDetail = async () => {
      loading.value = true
      try {
        const newsId = route.params.id
        const response = await newsApi.getNewsById(newsId)
        
        if (response) {
          newsDetail.value = response
          
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
          ElMessage.error('获取新闻详情失败')
        }
      } catch (error) {
        console.error('获取新闻详情失败:', error)
        ElMessage.error('获取新闻详情失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
    
    // 返回新闻列表
    const handleBackToList = () => {
      router.push('/elderly/news')
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
    
    // 获取新闻类型样式类
    const getNewsTypeClass = (type) => {
      const typeNum = typeof type === 'string' ? parseInt(type) : type
      
      switch (typeNum) {
        case 0:
          return 'news-type-primary'
        case 1:
          return 'news-type-warning'
        case 2:
          return 'news-type-success'
        default:
          return 'news-type-primary'
      }
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
    
    // 格式化新闻内容，每段开头保留两个字的空间
    const formatNewsContent = (content) => {
      if (!content) return ''
      
      // 将内容按换行符分割成段落
      const paragraphs = content.split('\n')
      
      // 为每个段落添加两个字的空间（使用全角空格）
      const formattedParagraphs = paragraphs.map(para => {
        const trimmedPara = para.trim()
        if (trimmedPara) {
          return '<p style="text-indent: 2em; margin-bottom: 1em;">' + trimmedPara + '</p>'
        }
        return ''
      })
      
      return formattedParagraphs.join('')
    }
    
    // 生命周期钩子
    onMounted(() => {
      fetchNewsDetail()
    })
    
    // 组件卸载时清理数据
    onUnmounted(() => {
      newsDetail.value = null
    })
    
    return {
      newsDetail,
      loading,
      ArrowLeft,
      handleBackToList,
      getNewsTypeLabel,
      getNewsTypeClass,
      formatDateTime,
      formatNewsContent
    }
  }
}
</script>

<style scoped>
.elderly-news-detail {
  max-width: 1000px;
  margin: 0 auto;
  padding: var(--elderly-space-lg);
}

.page-header {
  margin-bottom: var(--elderly-space-lg);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--elderly-space-lg);
  padding-bottom: var(--elderly-space-md);
  border-bottom: 1px solid var(--elderly-border-light);
}

.content-title {
  font-size: var(--elderly-font-size-xxl);
  font-weight: 600;
  color: var(--elderly-text-primary);
  margin: 0;
}

.content-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--elderly-space-lg);
  margin-bottom: var(--elderly-space-lg);
  padding: var(--elderly-space-md);
  background-color: var(--elderly-bg-light);
  border-radius: var(--elderly-border-radius-md);
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-label {
  font-weight: 600;
  color: var(--elderly-text-secondary);
  margin-right: var(--elderly-space-sm);
}

.meta-value {
  color: var(--elderly-text-primary);
  font-weight: 500;
}

.news-type-primary {
  background-color: var(--elderly-primary-light);
  color: var(--elderly-primary-color);
  padding: var(--elderly-space-xs) var(--elderly-space-sm);
  border-radius: var(--elderly-border-radius-sm);
  font-size: var(--elderly-font-size-sm);
}

.news-type-warning {
  background-color: var(--elderly-warning-light);
  color: var(--elderly-warning-color);
  padding: var(--elderly-space-xs) var(--elderly-space-sm);
  border-radius: var(--elderly-border-radius-sm);
  font-size: var(--elderly-font-size-sm);
}

.news-type-success {
  background-color: var(--elderly-success-light);
  color: var(--elderly-success-color);
  padding: var(--elderly-space-xs) var(--elderly-space-sm);
  border-radius: var(--elderly-border-radius-sm);
  font-size: var(--elderly-font-size-sm);
}

.content-body {
  margin-bottom: var(--elderly-space-xl);
}

.news-content {
  background-color: var(--elderly-bg-white);
  padding: var(--elderly-space-xl);
  border-radius: var(--elderly-border-radius-lg);
  border: 1px solid var(--elderly-border-light);
  line-height: var(--elderly-line-height-lg);
  font-size: var(--elderly-font-size-md);
  color: var(--elderly-text-primary);
  word-break: break-word;
  min-height: 300px;
}

.news-content p {
  margin: 0;
  text-align: justify;
}

.empty-state {
  padding: var(--elderly-space-xxl) 0;
}

@media (max-width: 768px) {
  .elderly-news-detail {
    padding: var(--elderly-space-md);
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--elderly-space-md);
  }
  
  .content-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--elderly-space-md);
  }
  
  .content-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--elderly-space-md);
  }
  
  .content-title {
    font-size: var(--elderly-font-size-xl);
  }
  
  .news-content {
    padding: var(--elderly-space-lg);
    min-height: 200px;
  }
}
</style>