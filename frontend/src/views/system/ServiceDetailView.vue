<template>
  <div class="system-page">
    <div class="system-page-title">
      <div class="system-page-title-text">
        <button class="back-button" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <span class="system-page-title-icon">🛠️</span>
        服务详情
      </div>
      <div class="action-buttons">
        <button class="system-button system-button-primary" @click="goBack">
          返回列表
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading-container" v-loading="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error-container">
      <el-result
        icon="error"
        title="加载失败"
        :sub-title="error"
      >
        <template #extra>
          <el-button type="primary" @click="fetchServiceDetail">重试</el-button>
          <el-button @click="goBack">返回</el-button>
        </template>
      </el-result>
    </div>

    <div v-else-if="serviceDetail" class="service-detail-container">
        <div class="detail-main-content">
          <!-- 服务基本信息 -->
          <div class="detail-card">
            <div class="detail-card-header">
              <h2>{{ serviceDetail.name }}</h2>
              <div class="status-tag" :class="getStatusClass(serviceDetail.status)">
                {{ getStatusText(serviceDetail.status) }}
              </div>
            </div>
            
            <div class="detail-card-body">
              <div class="detail-row">
                <div class="detail-label">服务类别：</div>
                <div class="detail-value">{{ getCategoryName(serviceDetail.categoryId) }}</div>
              </div>
              
              <div class="detail-row">
                <div class="detail-label">服务价格：</div>
                <div class="detail-value">¥{{ serviceDetail.price }}</div>
              </div>
              
              <div class="detail-row">
                <div class="detail-label">服务时长：</div>
                <div class="detail-value">{{ serviceDetail.duration || '-' }} 分钟</div>
              </div>
            </div>
          </div>

          <!-- 服务描述 -->
          <div class="detail-card">
            <div class="detail-card-header">
              <h3>服务描述</h3>
            </div>
            <div class="detail-card-body">
              <div class="service-content">
                {{ serviceDetail.description || '暂无服务描述' }}
              </div>
            </div>
          </div>

          <!-- 服务评论 -->
          <CommentList
            v-if="serviceDetail"
            target-type="service_item"
            :target-id="serviceDetail.id"
            :admin-mode="isAdmin"
          />
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { servicesApi } from '@/api/services'
import { useUserStore } from '@/stores/user'
import CommentList from '@/components/common/CommentList.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 服务详情数据
const serviceDetail = ref(null)
const serviceCategories = ref([])
const loading = ref(true)
const error = ref('')

// 判断是否为管理员
const isAdmin = computed(() => {
  return userStore.userInfo && (userStore.userInfo.role === 'admin' || userStore.userInfo.role === 'staff')
})

// 获取服务详情
const fetchServiceDetail = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const serviceId = route.params.id
    console.log('开始获取服务详情，ID:', serviceId)
    
    // 添加参数验证
    if (!serviceId || serviceId === 'undefined' || serviceId === 'null') {
      error.value = '服务ID无效，请重新选择服务'
      console.error('服务ID无效:', serviceId)
      return
    }
    
    const response = await servicesApi.getServiceItemDetail(serviceId)
    console.log('服务详情API响应:', response)
    
    if (response.code === 200 && response.data) {
      serviceDetail.value = response.data
      console.log('服务详情数据加载成功:', serviceDetail.value)
    } else {
      error.value = response.message || '获取服务详情失败'
      console.error('服务详情API返回错误:', response)
    }
  } catch (err) {
    console.error('获取服务详情失败:', err)
    // 添加更详细的错误信息
    let errorMessage = '获取服务详情失败，请稍后重试'
    
    if (err.response) {
      // 服务器返回了错误状态码
      console.error('错误响应状态:', err.response.status)
      console.error('错误响应数据:', err.response.data)
      
      if (err.response.status === 500) {
        errorMessage = '服务器内部错误，请联系管理员'
      } else if (err.response.status === 404) {
        errorMessage = '服务不存在或已被删除'
      } else if (err.response.data && err.response.data.message) {
        errorMessage = err.response.data.message
      }
    } else if (err.request) {
      // 请求已发出但没有收到响应
      errorMessage = '网络连接失败，请检查网络后重试'
      console.error('网络请求失败:', err.request)
    } else {
      // 请求配置出错
      errorMessage = '请求配置错误: ' + err.message
      console.error('请求配置错误:', err.message)
    }
    
    error.value = errorMessage
  } finally {
    loading.value = false
  }
}

// 获取服务分类列表
const fetchServiceCategories = async () => {
  try {
    const response = await servicesApi.getServiceCategories()
    if (Array.isArray(response)) {
      serviceCategories.value = response
    } else if (response && response.data) {
      if (Array.isArray(response.data)) {
        serviceCategories.value = response.data
      }
    }
  } catch (error) {
    console.error('获取服务分类失败:', error)
  }
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  if (!categoryId) return '未分类'
  
  const category = serviceCategories.value.find(c => 
    String(c.id) === String(categoryId)
  )
  
  return category ? category.name : '未知分类'
}

// 返回上一页
const goBack = () => {
  router.push('/system/services')
}

// 获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-inactive'
    case 1: return 'status-active'
    case 2: return 'status-disabled'
    default: return ''
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '未启用'
    case 1: return '启用中'
    case 2: return '已下架'
    default: return '未知状态'
  }
}

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  if (isNaN(date.getTime())) return ''
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 组件挂载时获取服务详情
onMounted(async () => {
  await fetchServiceCategories()
  fetchServiceDetail()
})
</script>

<style scoped>
.back-button {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  margin-right: 10px;
  color: var(--system-text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.back-button:hover {
  background-color: var(--system-bg-light);
}

.loading-container, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
}

.service-detail-container {
  display: flex;
  flex-direction: column;
  gap: var(--system-space-lg);
}

.detail-main-content {
  flex: 3;
  display: flex;
  flex-direction: column;
  gap: var(--system-space-lg);
}

.detail-card {
  background-color: var(--system-bg-white);
  border-radius: var(--system-border-radius-lg);
  box-shadow: var(--system-shadow-sm);
  overflow: hidden;
  transition: all 0.3s ease;
}

.detail-card:hover {
  box-shadow: var(--system-shadow-md);
}

.detail-card-header {
  padding: var(--system-space-lg);
  border-bottom: 1px solid var(--system-border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.detail-row {
  display: flex;
  margin-bottom: var(--system-space-md);
  align-items: center;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  width: 120px;
  font-weight: 500;
  color: var(--system-text-secondary);
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: var(--system-text-primary);
  display: flex;
  align-items: center;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}

.status-active {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-inactive {
  background-color: #f5f7fa;
  color: #909399;
}

.status-disabled {
  background-color: #fff2f0;
  color: #ff4d4f;
}

.service-content {
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  min-height: 100px;
  padding: var(--system-space-md);
  background-color: var(--system-bg-light);
  border-radius: var(--system-border-radius-md);
}

.action-buttons {
  display: flex;
  gap: var(--system-space-md);
  flex-wrap: wrap;
}

.system-button {
  padding: var(--system-space-sm) var(--system-space-lg);
  border: none;
  border-radius: var(--system-border-radius-md);
  cursor: pointer;
  font-size: var(--system-font-size-sm);
  font-weight: 500;
  transition: all 0.3s ease;
}

.system-button-primary {
  background-color: var(--system-primary-color);
  color: white;
}

.system-button-primary:hover {
  background-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);
  transform: translateY(-1px);
}

.system-button-success {
  background-color: var(--system-success-color);
  color: white;
}

.system-button-success:hover {
  background-color: #52c41a;
}

.system-button-warning {
  background-color: var(--system-warning-color);
  color: white;
}

.system-button-warning:hover {
  background-color: #faad14;
}

.system-button-danger {
  background-color: var(--system-error-color);
  color: white;
}

.system-button-danger:hover {
  background-color: #ff4d4f;
}

.system-button-info {
  background-color: #17a2b8;
  color: white;
}

.system-button-info:hover {
  background-color: #138496;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .detail-row {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .detail-label {
    width: 100%;
    margin-bottom: var(--system-space-xs);
  }
  
  .detail-value {
    width: 100%;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .system-button {
    width: 100%;
  }
}
</style>