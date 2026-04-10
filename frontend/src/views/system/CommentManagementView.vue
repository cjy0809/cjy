<template>
  <div class="system-page">
    <div class="system-page-title">
      <div class="system-page-title-text">
        <span class="system-page-title-icon">💬</span>
        评论管理
      </div>
      <div class="action-buttons">
        <button class="system-button system-button-primary" @click="refreshData">
          刷新数据
        </button>
      </div>
    </div>

    <!-- 评论统计卡片 -->
    <div class="statistics-cards">
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <div class="stat-title">总评论数</div>
          <div class="stat-value">{{ statistics.totalComments || 0 }}</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">🎉</div>
        <div class="stat-content">
          <div class="stat-title">活动评论</div>
          <div class="stat-value">{{ statistics.activityComments || 0 }}</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">🔧</div>
        <div class="stat-content">
          <div class="stat-title">服务评论</div>
          <div class="stat-value">{{ statistics.serviceItemComments || 0 }}</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">📢</div>
        <div class="stat-content">
          <div class="stat-title">新闻评论</div>
          <div class="stat-value">{{ statistics.newsComments || 0 }}</div>
        </div>
      </div>
    </div>

    <!-- 评论搜索 -->
    <div class="system-search-bar">
      <div class="system-search-form">
        <div class="system-search-item">
          <div class="system-form-label">评论类型</div>
          <select v-model="filter.targetType" class="system-form-input" @change="handleFilterChange">
            <option value="">全部</option>
            <option value="activity">活动评论</option>
            <option value="news">新闻评论</option>
            <option value="service_item">服务评论</option>
          </select>
        </div>
        
        <div class="system-search-item">
          <div class="system-form-label">关键词</div>
          <input 
            v-model="filter.keyword" 
            type="text" 
            class="system-form-input" 
            placeholder="搜索评论内容"
            @keyup.enter="handleFilterChange"
          />
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

    <!-- 评论列表 -->
    <div class="comment-table-card">
      <div class="table-header">
        <h3>评论列表</h3>
      </div>
      
      <div v-if="loading" class="loading-container">
        <p>加载中...</p>
      </div>
      
      <div v-else-if="comments.length === 0" class="empty-container">
        <p>暂无评论数据</p>
      </div>
      
      <div v-else class="table-container">
        <table class="comment-table">
          <thead>
            <tr>
              <th>评论内容</th>
              <th>评论者</th>
              <th>评论类型</th>
              <th>评论时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="comment in comments" :key="comment.id">
              <td class="comment-content-cell">
                <div class="comment-content">{{ comment.content }}</div>
              </td>
              <td>{{ comment.userName }}<span v-if="comment.userRole" class="user-role">{{ formatRole(comment.userRole) }}</span></td>
              <td>
                <span class="type-tag" :class="getTypeClass(comment.targetType)">
                  {{ getTypeLabel(comment.targetType) }}
                </span>
              </td>
              <td>{{ formatDateTime(comment.createTime) }}</td>
              <td>
                <div class="system-table-actions">
                  <el-button size="small" type="info" @click="viewCommentDetail(comment)">查看</el-button>
                  <el-button size="small" type="danger" @click="deleteComment(comment)">删除</el-button>
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

    <!-- 评论详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="评论详情"
      width="600px"
      class="comment-dialog"
    >
      <div v-if="selectedComment" class="comment-detail">
        <div class="detail-row">
          <div class="detail-label">评论者：</div>
          <div class="detail-value">
            {{ selectedComment.userName }}
            <span v-if="selectedComment.userRole" class="user-role">{{ formatRole(selectedComment.userRole) }}</span>
          </div>
        </div>
        
        <div class="detail-row">
          <div class="detail-label">评论类型：</div>
          <div class="detail-value">
            <span class="type-tag" :class="getTypeClass(selectedComment.targetType)">
              {{ getTypeLabel(selectedComment.targetType) }}
            </span>
          </div>
        </div>
        
        <div class="detail-row">
          <div class="detail-label">目标：</div>
          <div class="detail-value">{{ selectedComment.targetTitle || selectedComment.targetId }}</div>
        </div>
        
        <div class="detail-row">
          <div class="detail-label">评论时间：</div>
          <div class="detail-value">{{ formatDateTime(selectedComment.createTime) }}</div>
        </div>
        
        <div class="detail-row">
          <div class="detail-label">评论内容：</div>
          <div class="detail-value comment-content-detail">
            {{ selectedComment.content }}
          </div>
        </div>
        
        <!-- 子评论列表 -->
        <div v-if="selectedComment.childComments && selectedComment.childComments.length > 0" class="child-comments-section">
          <div class="child-comments-title">回复列表 ({{ selectedComment.childComments.length }})</div>
          <div class="child-comments-list">
            <div 
              v-for="childComment in selectedComment.childComments"
              :key="childComment.id"
              class="child-comment-item"
            >
              <div class="child-comment-header">
                <span class="child-comment-username">
                  {{ childComment.userName }}
                  <span v-if="childComment.userRole" class="user-role">{{ formatRole(childComment.userRole) }}</span>
                </span>
                <span class="child-comment-time">{{ formatDateTime(childComment.createTime) }}</span>
              </div>
              <div class="child-comment-content">{{ childComment.content }}</div>
            </div>
          </div>
        </div>
        
        <!-- 回复表单 -->
        <div class="reply-form-section">
          <div class="reply-form-header" @click="replyFormCollapsed = !replyFormCollapsed">
            <div class="reply-form-title">回复评论</div>
            <span class="reply-form-toggle-icon" :class="{ 'collapsed': replyFormCollapsed }">
              {{ replyFormCollapsed ? '▼' : '▲' }}
            </span>
          </div>
          <div class="reply-form-content" :class="{ 'collapsed': replyFormCollapsed }">
            <textarea
              v-model="replyContent"
              class="system-textarea"
              placeholder="请输入回复内容..."
              rows="4"
              maxlength="500"
            ></textarea>
            <div class="reply-form-footer">
              <span class="char-count">{{ replyContent.length }}/500</span>
              <div class="reply-form-buttons">
                <button 
                  class="system-button system-button-primary" 
                  :disabled="!replyContent.trim() || submittingReply"
                  @click="submitReply"
                >
                  {{ submittingReply ? '提交中...' : '发表回复' }}
                </button>
              </div>
            </div>
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
import { newsApi } from '@/api/news'
import { servicesApi } from '@/api/services'

// 评论统计数据
const statistics = ref({})
const loading = ref(false)
const comments = ref([])
const dialogVisible = ref(false)
const selectedComment = ref(null)
const replyContent = ref('')
const submittingReply = ref(false)
const jumpPage = ref(1)
const replyFormCollapsed = ref(true)

// 筛选条件
const filter = reactive({
  targetType: '',
  keyword: ''
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

// 获取评论统计信息
const fetchStatistics = async () => {
  try {
    const response = await commentApi.getCommentStatistics()
    statistics.value = response.data || {}
  } catch (error) {
    console.error('获取评论统计失败:', error)
    ElMessage.error('获取评论统计失败')
  }
}

// 获取评论列表
const fetchComments = async () => {
  loading.value = true
  
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      targetType: filter.targetType || undefined
    }
    
    const response = await commentApi.getCommentPage(params)
    
    if (response && response.records) {
      comments.value = response.records
      pagination.total = response.total || 0
    } else {
      comments.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表失败')
  } finally {
    loading.value = false
  }
}

// 处理筛选条件变化
const handleFilterChange = () => {
  pagination.pageNum = 1
  fetchComments()
}

// 重置筛选条件
const resetFilter = () => {
  filter.targetType = ''
  filter.keyword = ''
  handleFilterChange()
}

// 刷新数据
const refreshData = () => {
  fetchStatistics()
  fetchComments()
}

// 查看评论详情
const viewCommentDetail = async (comment) => {
  try {
    const response = await commentApi.getCommentById(comment.id)
    console.log('评论详情响应:', response) // 添加调试日志
    selectedComment.value = response.data
    
    // 根据目标类型获取目标名称
    if (selectedComment.value.targetType === 'activity') {
      try {
        const activityResponse = await activityApi.getActivityDetail(selectedComment.value.targetId)
        selectedComment.value.targetTitle = activityResponse.data?.title || activityResponse.data?.name || '未知活动'
      } catch (error) {
        console.error('获取活动名称失败:', error)
        selectedComment.value.targetTitle = '未知活动'
      }
    } else if (selectedComment.value.targetType === 'news') {
      try {
        const newsResponse = await newsApi.getNewsDetail(selectedComment.value.targetId)
        selectedComment.value.targetTitle = newsResponse.data?.title || '未知新闻'
      } catch (error) {
        console.error('获取新闻名称失败:', error)
        selectedComment.value.targetTitle = '未知新闻'
      }
    } else if (selectedComment.value.targetType === 'service_item') {
      try {
        const serviceResponse = await servicesApi.getServiceItemDetail(selectedComment.value.targetId)
        selectedComment.value.targetTitle = serviceResponse.data?.name || '未知服务'
      } catch (error) {
        console.error('获取服务名称失败:', error)
        selectedComment.value.targetTitle = '未知服务'
      }
    } else {
      selectedComment.value.targetTitle = '未知目标'
    }
    
    // 保持回复表单的折叠状态
    replyFormCollapsed.value = true
    dialogVisible.value = true
  } catch (error) {
    console.error('获取评论详情失败:', error)
    console.error('错误详情:', {
      message: error.message,
      response: error.response,
      data: error.response?.data
    })
    ElMessage.error(error.response?.data?.message || error.message || '获取评论详情失败')
  }
}

// 删除评论
const deleteComment = async (comment) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await commentApi.adminDeleteComment(comment.id)
    ElMessage.success('删除成功')
    fetchComments()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 提交回复
const submitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  submittingReply.value = true
  
  try {
    console.log('提交回复数据:', {
      content: replyContent.value.trim(),
      targetType: selectedComment.value.targetType,
      targetId: selectedComment.value.targetId,
      parentId: selectedComment.value.id
    }) // 添加调试日志
    
    const data = {
      content: replyContent.value.trim(),
      targetType: selectedComment.value.targetType,
      targetId: selectedComment.value.targetId,
      parentId: selectedComment.value.id
    }
    
    await commentApi.createComment(data)
    ElMessage.success('回复发表成功')
    
    // 清空回复表单
    replyContent.value = ''
    
    // 重新获取评论详情，更新子评论列表
    const response = await commentApi.getCommentById(selectedComment.value.id)
    console.log('更新后的评论详情:', response) // 添加调试日志
    selectedComment.value = response.data
  } catch (error) {
    console.error('发表回复失败:', error)
    console.error('错误详情:', {
      message: error.message,
      response: error.response,
      data: error.response?.data
    })
    
    // 更详细的错误处理
    let errorMessage = '发表回复失败'
    if (error.response && error.response.data) {
      if (error.response.data.message) {
        errorMessage = error.response.data.message
      } else if (error.response.data.error) {
        errorMessage = error.response.data.error
      }
    } else if (error.message) {
      errorMessage = error.message
    }
    
    // 根据不同的错误类型提供更具体的提示
    if (errorMessage.includes('未登录') || errorMessage.includes('UNAUTHORIZED')) {
      errorMessage = '登录状态已过期，请重新登录'
      // 可以在这里添加跳转到登录页的逻辑
    } else if (errorMessage.includes('敏感词') || errorMessage.includes('不当信息')) {
      errorMessage = '回复内容包含不当信息，请修改后重新提交'
    } else if (errorMessage.includes('父评论不存在')) {
      errorMessage = '原评论已被删除，无法回复'
    } else if (errorMessage.includes('无效的评论目标类型')) {
      errorMessage = '评论类型错误，请刷新页面后重试'
    }
    
    ElMessage.error(errorMessage)
  } finally {
    submittingReply.value = false
  }
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchComments()
}

// 处理页码变化
const handlePageChange = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    pagination.pageNum = page
    jumpPage.value = page
    fetchComments()
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
    'activity': '活动评论',
    'news': '新闻评论',
    'service_item': '服务评论'
  }
  return typeMap[type] || '未知类型'
}

// 获取类型样式类
const getTypeClass = (type) => {
  const classMap = {
    'activity': 'type-activity',
    'news': 'type-news',
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
  fetchComments()
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


.comment-table-card {
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

.comment-table {
  width: 100%;
  border-collapse: collapse;
}

.comment-table th, .comment-table td {
  padding: var(--system-space-md);
  text-align: left;
  border-bottom: 1px solid var(--system-border-light);
}

.comment-table th {
  background-color: var(--system-bg-light);
  font-weight: 500;
  color: var(--system-text-secondary);
  font-size: var(--system-font-size-sm);
}

.comment-table td {
  font-size: var(--system-font-size-sm);
  color: var(--system-text-primary);
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

.type-service {
  background-color: #f6ffed;
  color: #52c41a;
}

.type-news {
  background-color: #fff2e8;
  color: #fa8c16;
}

.system-table-actions {
  display: flex;
  gap: var(--system-space-xs);
  flex-wrap: wrap;
}

.system-table-actions .el-button {
  margin: 0;
}

.comment-dialog .comment-detail {
  padding: var(--system-space-md) 0;
}

.detail-row {
  display: flex;
  margin-bottom: var(--system-space-md);
}

.detail-label {
  width: 100px;
  font-weight: 500;
  color: var(--system-text-secondary);
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: var(--system-text-primary);
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

.child-comments-section {
  margin-top: var(--system-space-lg);
  padding-top: var(--system-space-lg);
  border-top: 1px solid var(--system-border-light);
}

.child-comments-title {
  font-size: var(--system-font-size-md);
  font-weight: 500;
  color: var(--system-text-primary);
  margin-bottom: var(--system-space-md);
}

.child-comments-list {
  max-height: 200px;
  overflow-y: auto;
  padding: var(--system-space-md);
  background-color: var(--system-bg-light);
  border-radius: var(--system-border-radius-md);
}

.child-comment-item {
  margin-bottom: var(--system-space-md);
  padding-bottom: var(--system-space-md);
  border-bottom: 1px solid var(--system-border-light);
}

.child-comment-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.child-comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--system-space-sm);
}

.child-comment-username {
  font-size: var(--system-font-size-sm);
  font-weight: 500;
  color: var(--system-text-primary);
}

.child-comment-time {
  font-size: var(--system-font-size-xs);
  color: var(--system-text-secondary);
}

.child-comment-content {
  font-size: var(--system-font-size-sm);
  line-height: 1.6;
  color: var(--system-text-primary);
}

.reply-form-section {
  margin-top: var(--system-space-lg);
  padding-top: var(--system-space-lg);
  border-top: 1px solid var(--system-border-light);
}

.reply-form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  padding: var(--system-space-sm) 0;
  transition: all 0.3s ease;
  user-select: none;
}

.reply-form-header:hover {
  background-color: var(--system-bg-light);
  border-radius: var(--system-border-radius-md);
  padding-left: var(--system-space-sm);
  padding-right: var(--system-space-sm);
}

.reply-form-title {
  font-size: var(--system-font-size-md);
  font-weight: 500;
  color: var(--system-text-primary);
}

.reply-form-toggle-icon {
  font-size: var(--system-font-size-sm);
  color: var(--system-text-secondary);
  transition: transform 0.3s ease;
  display: inline-block;
}

.reply-form-toggle-icon.collapsed {
  transform: rotate(180deg);
}

.reply-form-content {
  overflow: hidden;
  transition: max-height 0.3s ease, opacity 0.3s ease;
  max-height: 500px;
  opacity: 1;
}

.reply-form-content.collapsed {
  max-height: 0;
  opacity: 0;
  padding-top: 0;
  margin-top: 0;
}

.system-textarea {
  width: 100%;
  padding: var(--system-space-md);
  border: 1px solid var(--system-border);
  border-radius: var(--system-border-radius-md);
  font-size: var(--system-font-size-sm);
  line-height: 1.6;
  resize: vertical;
  font-family: inherit;
}

.system-textarea:focus {
  outline: none;
  border-color: var(--system-primary-color);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.reply-form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--system-space-md);
}

.char-count {
  font-size: var(--system-font-size-xs);
  color: var(--system-text-secondary);
}

.reply-form-buttons {
  display: flex;
  gap: var(--system-space-sm);
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

.system-button-secondary {
  background-color: var(--system-bg-light);
  color: var(--system-text-secondary);
  border: 1px solid var(--system-border);
}

.system-button-secondary:hover {
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