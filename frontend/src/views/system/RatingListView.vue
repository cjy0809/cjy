<template>
  <div class="system-page">
    <div class="system-page-title">
      <div class="system-page-title-text">
        <span class="system-page-title-icon">⭐</span>
        评分列表
      </div>
      <div class="action-buttons">
        <button class="system-button system-button-primary" @click="refreshData">
          刷新数据
        </button>
      </div>
    </div>

    <!-- 评分统计卡片 -->
    <div class="statistics-cards">
      <div class="stat-card">
        <div class="stat-icon">⭐</div>
        <div class="stat-content">
          <div class="stat-title">总评分数</div>
          <div class="stat-value">{{ statistics.totalRatings || 0 }}</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">🎉</div>
        <div class="stat-content">
          <div class="stat-title">活动评分</div>
          <div class="stat-value">{{ statistics.activityRatings || 0 }}</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">🔧</div>
        <div class="stat-content">
          <div class="stat-title">服务评分</div>
          <div class="stat-value">{{ statistics.serviceItemRatings || 0 }}</div>
        </div>
      </div>
    </div>

    <!-- 评分搜索 -->
    <div class="system-search-bar">
      <div class="system-search-form">
        <div class="system-search-item">
          <div class="system-form-label">评论类型</div>
          <select v-model="filter.targetType" class="system-form-input" @change="handleFilterChange">
            <option value="">全部</option>
            <option value="activity">活动评分</option>
            <option value="service_item">服务评分</option>
          </select>
        </div>
        
        <div class="system-search-item">
          <div class="system-form-label">评分等级</div>
          <select v-model="filter.rating" class="system-form-input" @change="handleFilterChange">
            <option value="">全部</option>
            <option value="5">5星</option>
            <option value="4">4星</option>
            <option value="3">3星</option>
            <option value="2">2星</option>
            <option value="1">1星</option>
          </select>
        </div>
        
        <div class="system-search-actions">
          <button class="system-button system-button-primary" @click="handleFilterChange">
            搜索
          </button>
          <button class="system-button system-button-default" @click="resetFilter">
            重置
          </button>
        </div>
      </div>
    </div>

    <!-- 评分列表 -->
    <div class="rating-table-card">
      <div class="table-header">
        <h3>评分列表</h3>
      </div>
      
      <div v-if="loading" class="loading-container">
        <p>加载中...</p>
      </div>
      
      <div v-else-if="ratings.length === 0" class="empty-container">
        <p>暂无评分数据</p>
      </div>
      
      <div v-else class="table-container">
        <table class="rating-table">
          <thead>
            <tr>
              <th>评分</th>
              <th>评论者</th>
              <th>评论类型</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="rating in ratings" :key="rating.id">
              <td class="rating-star-cell">
                <div class="rating-stars">
                  <span 
                    v-for="star in 5" 
                    :key="star" 
                    class="star"
                    :class="{ 'star-filled': star <= rating.rating }"
                  >★</span>
                </div>
              </td>
              <td>{{ rating.userName }}<span v-if="rating.userRole" class="user-role">{{ formatRole(rating.userRole) }}</span></td>
              <td>
                <span class="type-tag" :class="getTypeClass(rating.targetType)">
                  {{ getTypeLabel(rating.targetType) }}
                </span>
              </td>
              <td>
                <div class="system-table-actions">
                  <el-button size="small" type="info" @click="viewRatingDetail(rating)">查看</el-button>
                  <el-button size="small" type="danger" @click="deleteRating(rating)">删除</el-button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        
        <!-- 分页 -->
        <div class="system-pagination" :class="{ 'system-pagination-loading': loading }">
          <div class="system-pagination-info">
            <span>共 {{ pagination.total }} 条记录，第 {{ pagination.pageNum }} / {{ totalPages }} 页</span>
            <div class="system-pagination-size-selector">
              <span>每页显示</span>
              <el-select 
                v-model="pagination.pageSize" 
                size="small" 
                placeholder="请选择"
                style="width: 80px; margin: 0 8px;"
                @change="handleSizeChange"
              >
                <el-option label="10" :value="10"></el-option>
                <el-option label="20" :value="20"></el-option>
                <el-option label="50" :value="50"></el-option>
                <el-option label="100" :value="100"></el-option>
              </el-select>
              <span>条</span>
            </div>
          </div>
          <div class="system-pagination-controls">
            <button 
              class="system-pagination-btn"
              :class="{ 'system-pagination-btn-disabled': pagination.pageNum <= 1 }"
              :disabled="pagination.pageNum <= 1"
              @click="handlePageChange(1)"
              title="首页"
            >
              首页
            </button>
            <button 
              class="system-pagination-btn"
              :class="{ 'system-pagination-btn-disabled': pagination.pageNum <= 1 }"
              :disabled="pagination.pageNum <= 1"
              @click="handlePageChange(pagination.pageNum - 1)"
              title="上一页"
            >
              上一页
            </button>
            
            <template v-for="(page, index) in getPageNumbers()" :key="index">
              <span 
                v-if="page === '...'" 
                class="system-pagination-ellipsis"
              >...</span>
              <button 
                v-else
                class="system-pagination-btn"
                :class="{ 'system-pagination-btn-active': page === pagination.pageNum }"
                @click="handlePageChange(page)"
              >
                {{ page }}
              </button>
            </template>
            
            <button 
              class="system-pagination-btn"
              :class="{ 'system-pagination-btn-disabled': pagination.pageNum >= totalPages }"
              :disabled="pagination.pageNum >= totalPages"
              @click="handlePageChange(pagination.pageNum + 1)"
              title="下一页"
            >
              下一页
            </button>
            <button 
              class="system-pagination-btn"
              :class="{ 'system-pagination-btn-disabled': pagination.pageNum >= totalPages }"
              :disabled="pagination.pageNum >= totalPages"
              @click="handlePageChange(totalPages)"
              title="末页"
            >
              末页
            </button>
            
            <div class="system-pagination-jump">
              <span>跳转到</span>
              <input 
                type="number" 
                v-model="jumpPage" 
                @keyup.enter="handleJumpToPage"
                :min="1" 
                :max="totalPages"
              >
              <span>页</span>
              <button 
                class="system-pagination-btn"
                @click="handleJumpToPage"
              >
                确定
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 评分详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="评分详情"
      width="600px"
      class="rating-dialog"
    >
      <div v-if="selectedRating" class="rating-detail">
        <div class="detail-row">
          <div class="detail-label">评分：</div>
          <div class="detail-value">
            <div class="rating-stars">
              <span 
                v-for="star in 5" 
                :key="star" 
                class="star"
                :class="{ 'star-filled': star <= selectedRating.rating }"
              >★</span>
            </div>
          </div>
        </div>
        
        <div class="detail-row">
          <div class="detail-label">评论者：</div>
          <div class="detail-value">
            {{ selectedRating.userName }}
            <span v-if="selectedRating.userRole" class="user-role">{{ formatRole(selectedRating.userRole) }}</span>
          </div>
        </div>
        
        <div class="detail-row">
          <div class="detail-label">评论类型：</div>
          <div class="detail-value">
            <span class="type-tag" :class="getTypeClass(selectedRating.targetType)">
              {{ getTypeLabel(selectedRating.targetType) }}
            </span>
          </div>
        </div>
        
        <div class="detail-row">
          <div class="detail-label">目标：</div>
          <div class="detail-value">{{ selectedRating.targetTitle || '-' }}</div>
        </div>
        
        <div class="detail-row">
          <div class="detail-label">评论时间：</div>
          <div class="detail-value">{{ formatDateTime(selectedRating.createTime) }}</div>
        </div>
        
        <div class="detail-row">
          <div class="detail-label">评论内容：</div>
          <div class="detail-value comment-content-detail">
            {{ selectedRating.content }}
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { commentApi } from '@/api/comment'
import { activityApi } from '@/api/activity'
import { servicesApi } from '@/api/services'

// 评分统计数据
const statistics = ref({})
const loading = ref(false)
const ratings = ref([])
const dialogVisible = ref(false)
const selectedRating = ref(null)
const jumpPage = ref(1)

// 筛选条件
const filter = reactive({
  targetType: '',
  rating: ''
})

// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(pagination.total / pagination.pageSize) || 1
})

// 获取评分统计信息
const fetchStatistics = async () => {
  try {
    const response = await commentApi.getRatingStatistics()
    statistics.value = response.data || {}
  } catch (error) {
    console.error('获取评分统计失败:', error)
    ElMessage.error('获取评分统计失败')
  }
}

// 获取评分列表
const fetchRatings = async () => {
  loading.value = true
  
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      targetType: filter.targetType || undefined,
      rating: filter.rating || undefined
    }
    
    const response = await commentApi.getRatingPage(params)
    
    if (response && response.records) {
      ratings.value = response.records
      pagination.total = response.total || 0
    } else {
      ratings.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取评分列表失败:', error)
    ElMessage.error('获取评分列表失败')
  } finally {
    loading.value = false
  }
}

// 处理筛选条件变化
const handleFilterChange = () => {
  pagination.pageNum = 1
  fetchRatings()
}

// 重置筛选条件
const resetFilter = () => {
  filter.targetType = ''
  filter.rating = ''
  handleFilterChange()
}

// 刷新数据
const refreshData = () => {
  fetchStatistics()
  fetchRatings()
}

// 查看评分详情
const viewRatingDetail = async (rating) => {
  try {
    selectedRating.value = { ...rating }
    
    console.log('查看评分详情:', rating)
    
    // 根据目标类型获取目标标题/名称
    if (rating.targetType === 'activity') {
      console.log('获取活动详情, ID:', rating.targetId)
      const response = await activityApi.getActivityDetail(rating.targetId)
      console.log('活动详情完整响应:', response)
      const activityData = response.data || response
      console.log('活动数据:', activityData)
      selectedRating.value.targetTitle = activityData.title || activityData.name || '未知活动'
    } else if (rating.targetType === 'service_item') {
      console.log('获取服务详情, ID:', rating.targetId)
      const response = await servicesApi.getServiceItemDetail(rating.targetId)
      console.log('服务详情完整响应:', response)
      const serviceData = response.data || response
      console.log('服务数据:', serviceData)
      selectedRating.value.targetTitle = serviceData.name || '未知服务'
    } else {
      selectedRating.value.targetTitle = '未知目标'
    }
    
    console.log('设置后的目标标题:', selectedRating.value.targetTitle)
    
    dialogVisible.value = true
  } catch (error) {
    console.error('获取评分详情失败:', error)
    ElMessage.error('获取评分详情失败')
  }
}

// 删除评分
const deleteRating = async (rating) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评分吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await commentApi.adminDeleteComment(rating.id)
    ElMessage.success('删除成功')
    fetchRatings()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评分失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchRatings()
}

// 处理页码变化
const handlePageChange = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    pagination.pageNum = page
    jumpPage.value = page
    fetchRatings()
  }
}

// 处理跳转到指定页
const handleJumpToPage = () => {
  let page = parseInt(jumpPage.value)
  if (isNaN(page) || page < 1) {
    page = 1
  } else if (page > totalPages.value) {
    page = totalPages.value
  }
  jumpPage.value = page
  handlePageChange(page)
}

// 生成页码列表
const getPageNumbers = () => {
  const pages = []
  const current = pagination.pageNum
  const total = totalPages.value
  
  if (total <= 5) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 3) {
      pages.push(1, 2, 3, '...', total)
    } else if (current >= total - 2) {
      pages.push(1, '...', total - 2, total - 1, total)
    } else {
      pages.push(1, '...', current - 1, current, current + 1, '...', total)
    }
  }
  
  return pages
}

// 获取类型标签
const getTypeLabel = (type) => {
  const typeMap = {
    'activity': '活动评分',
    'service_item': '服务评分'
  }
  return typeMap[type] || '未知类型'
}

// 获取类型样式类
const getTypeClass = (type) => {
  const classMap = {
    'activity': 'type-activity',
    'service_item': 'type-service'
  }
  return classMap[type] || ''
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
  
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const formatRole = (role) => {
  const roleMap = {
    'ADMIN': '管理员',
    'admin': '管理员',
    'STAFF': '工作人员',
    'staff': '工作人员',
    'ELDERLY': '用户',
    'elderly': '用户',
  }
  return roleMap[role] || role
}

// 组件挂载时获取数据
onMounted(() => {
  fetchStatistics()
  fetchRatings()
})
</script>

<style scoped>
.statistics-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--system-space-lg);
  margin-bottom: var(--system-space-lg);
}

.stat-card {
  background-color: var(--system-bg-white);
  border-radius: var(--system-border-radius-lg);
  box-shadow: var(--system-shadow-sm);
  padding: var(--system-space-lg);
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.stat-card:hover {
  box-shadow: var(--system-shadow-md);
  transform: translateY(-2px);
}

.stat-icon {
  font-size: 32px;
  margin-right: var(--system-space-md);
}

.stat-content {
  flex: 1;
}

.stat-title {
  font-size: var(--system-font-size-sm);
  color: var(--system-text-secondary);
  margin-bottom: var(--system-space-xs);
}

.stat-value {
  font-size: var(--system-font-size-xl);
  font-weight: 600;
  color: var(--system-text-primary);
}

.rating-table-card {
  background-color: var(--system-bg-white);
  border-radius: var(--system-border-radius-lg);
  box-shadow: var(--system-shadow-sm);
  overflow: hidden;
}

.table-header {
  padding: var(--system-space-lg);
  border-bottom: 1px solid var(--system-border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-header h3 {
  margin: 0;
  font-size: var(--system-font-size-lg);
  color: var(--system-text-primary);
}

.loading-container, .empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--system-space-xl);
  color: var(--system-text-secondary);
}

.table-container {
  overflow-x: auto;
}

.rating-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.rating-table th, .rating-table td {
  padding: var(--system-space-md);
  text-align: center;
  border-bottom: 1px solid var(--system-border-light);
  width: 25%;
}

.rating-table th {
  background-color: var(--system-bg-light);
  font-weight: 500;
  color: var(--system-text-secondary);
  font-size: var(--system-font-size-sm);
}

.rating-table td {
  font-size: var(--system-font-size-sm);
  color: var(--system-text-primary);
}

.rating-star-cell {
  width: 25%;
}

.rating-stars {
  display: flex;
  align-items: center;
  justify-content: center;
}

.star {
  color: #ddd;
  font-size: 18px;
  margin-right: 2px;
}

.star-filled {
  color: #f5a623;
}

.comment-content-cell {
  max-width: 200px;
}

.comment-content {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.type-tag {
  padding: 2px 8px;
  border-radius: var(--system-border-radius-full);
  font-size: var(--system-font-size-xs);
  font-weight: 500;
}

.user-role {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  border-radius: 4px;
  background-color: #f0f9ff;
  color: #1890ff;
  margin-left: 6px;
  font-weight: normal;
}

.type-activity {
  background-color: #e6f7ff;
  color: #1890ff;
}

.type-service {
  background-color: #f6ffed;
  color: #52c41a;
}

.system-table-actions {
  display: flex;
  gap: var(--system-space-xs);
  flex-wrap: wrap;
}

.system-table-actions .el-button {
  margin: 0;
}

.rating-dialog .rating-detail {
  padding: var(--system-space-md) 0;
}

.detail-row {
  display: flex;
  margin-bottom: var(--system-space-md);
  align-items: center;
}

.detail-label {
  min-width: 80px;
  font-weight: 500;
  color: var(--system-text-secondary);
  flex-shrink: 0;
  text-align: right;
  padding-right: 8px;
}

.detail-value {
  flex: 1;
  color: var(--system-text-primary);
}

/* 针对评分星星的特殊样式 */
.detail-row:first-child .detail-value {
  flex: none;
  display: flex;
  align-items: center;
}

.comment-content-detail {
  max-height: 200px;
  overflow-y: auto;
  line-height: 1.6;
  word-break: break-word;
  padding: var(--system-space-sm);
  background-color: var(--system-bg-light);
  border-radius: var(--system-border-radius-md);
  border-left: 4px solid var(--system-primary-color);
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
}

.system-button-default {
  background-color: var(--system-bg-light);
  color: var(--system-text-secondary);
  border: 1px solid var(--system-border);
}

.system-button-default:hover {
  background-color: var(--system-bg-hover);
  border-color: var(--system-border-dark);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .statistics-cards {
    grid-template-columns: 1fr;
  }
  
  .table-container {
    overflow-x: scroll;
  }
}
</style>