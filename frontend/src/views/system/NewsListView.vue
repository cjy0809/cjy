<template>
  <div class="system-page">
    <div class="system-page-title">
      <div class="system-page-title-text">
        <span class="system-page-title-icon">📢</span>
        新闻公告管理
      </div>
      <div class="system-page-title-actions">
        <el-button type="primary" @click="showAddNewsDialog = true">
          <span class="system-button-icon">+</span>
          发布新闻
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="system-search-bar">
      <div class="system-search-form">
        <div class="system-search-item">
          <div class="system-form-label">新闻标题</div>
          <input class="system-form-input" type="text" v-model="searchForm.title" placeholder="请输入新闻标题">
        </div>
        <div class="system-search-item">
          <div class="system-form-label">新闻类型</div>
          <select class="system-form-input" v-model="searchForm.type">
            <option value="">全部类型</option>
            <option value="0">新闻</option>
            <option value="1">公告</option>
            <option value="2">政策解读</option>
          </select>
        </div>
        <div class="system-search-item">
          <div class="system-form-label">状态</div>
          <select class="system-form-input" v-model="searchForm.status">
            <option value="">全部状态</option>
            <option value="0">草稿</option>
            <option value="1">发布</option>
          </select>
        </div>
        <div class="system-search-actions">
          <el-button type="primary" @click="searchNews">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 新闻列表 -->
    <div class="system-table-container">
      <table class="system-table">
        <thead>
          <tr>
            <th>标题</th>
            <th>类型</th>
            <th>发布时间</th>
            <th>浏览次数</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="news in newsList" :key="news.id">
            <td class="title-cell">{{ news.title }}</td>
            <td>{{ getNewsTypeLabel(news.type) }}</td>
            <td>{{ formatDateTime(news.publishTime) }}</td>
            <td>{{ news.viewCount }}</td>
            <td>
              <span v-if="news.status === 1" class="status-tag status-published">已发布</span>
              <span v-else class="status-tag status-draft">草稿</span>
            </td>
            <td>
              <div class="action-btn-group">
                <el-button size="small" type="info" @click="viewNews(news.id)">查看</el-button>
                <el-button size="small" type="primary" @click="editNews(news.id)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteNews(news.id)">删除</el-button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="system-pagination" :class="{ 'system-pagination-loading': loading }">
        <div class="system-pagination-info">
          <span>共 {{ total }} 条记录，第 {{ currentPage }} / {{ totalPages }} 页</span>
          <div class="system-pagination-size-selector">
            <span>每页显示</span>
            <el-select 
              v-model="pageSize" 
              size="small" 
              placeholder="请选择"
              style="width: 80px; margin: 0 8px;"
              @change="handlePageSizeChange"
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
            :class="{ 'system-pagination-btn-disabled': currentPage <= 1 }"
            :disabled="currentPage <= 1"
            @click="handlePageChange(1)"
            title="首页"
          >
            首页
          </button>
          <button 
            class="system-pagination-btn"
            :class="{ 'system-pagination-btn-disabled': currentPage <= 1 }"
            :disabled="currentPage <= 1"
            @click="handlePageChange(currentPage - 1)"
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
              :class="{ 'system-pagination-btn-active': page === currentPage }"
              @click="handlePageChange(page)"
            >
              {{ page }}
            </button>
          </template>
          
          <button 
            class="system-pagination-btn"
            :class="{ 'system-pagination-btn-disabled': currentPage >= totalPages }"
            :disabled="currentPage >= totalPages"
            @click="handlePageChange(currentPage + 1)"
            title="下一页"
          >
            下一页
          </button>
          <button 
            class="system-pagination-btn"
            :class="{ 'system-pagination-btn-disabled': currentPage >= totalPages }"
            :disabled="currentPage >= totalPages"
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

    <!-- 添加新闻对话框 -->
    <div v-if="showAddNewsDialog" class="system-modal-overlay" @click.self="closeNewsDialog">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">发布新闻</div>
          <button class="system-modal-close" @click="closeNewsDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">标题</div>
              <input class="system-form-input" type="text" v-model="newsForm.title" placeholder="请输入新闻标题">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">类型</div>
              <select class="system-form-input" v-model="newsForm.type">
                <option value="0">新闻</option>
                <option value="1">公告</option>
                <option value="2">政策解读</option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">内容</div>
              <textarea class="system-form-input" rows="8" v-model="newsForm.content" placeholder="请输入新闻内容"></textarea>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">发布者</div>
              <input class="system-form-input" type="text" :value="currentUserName" readonly disabled>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">状态</div>
              <select class="system-form-input" v-model="newsForm.status">
                <option value="0">草稿</option>
                <option value="1">发布</option>
              </select>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <el-button @click="closeNewsDialog">取消</el-button>
          <el-button type="primary" @click="saveNews">保存</el-button>
        </div>
      </div>
    </div>

    <!-- 编辑新闻对话框 -->
    <div v-if="showEditNewsDialog" class="system-modal-overlay" @click.self="closeNewsDialog">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">编辑新闻</div>
          <button class="system-modal-close" @click="closeNewsDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">标题</div>
              <input class="system-form-input" type="text" v-model="newsForm.title" placeholder="请输入新闻标题">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">类型</div>
              <select class="system-form-input" v-model="newsForm.type">
                <option value="0">新闻</option>
                <option value="1">公告</option>
                <option value="2">政策解读</option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">内容</div>
              <textarea class="system-form-input" rows="8" v-model="newsForm.content" placeholder="请输入新闻内容"></textarea>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">发布者</div>
              <input class="system-form-input" type="text" :value="currentUserName" readonly disabled>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">状态</div>
              <select class="system-form-input" v-model="newsForm.status">
                <option value="0">草稿</option>
                <option value="1">发布</option>
              </select>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <el-button @click="closeNewsDialog">取消</el-button>
          <el-button type="primary" @click="saveNews">保存</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { newsApi } from '@/api/news'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 获取当前用户名称
const currentUserName = computed(() => {
  return userStore.userInfo.name || '未知用户'
})

// 获取当前用户ID
const currentUserId = computed(() => {
  return userStore.userId || 1
})

// 搜索表单
const searchForm = reactive({
  title: '',
  type: '',
  status: ''
})

// 新闻列表
const newsList = ref([])
const total = ref(0)
const loading = ref(false)
const allNews = ref([])

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const jumpPage = ref('') // 跳转页码

// 对话框状态
const showAddNewsDialog = ref(false)
const showEditNewsDialog = ref(false)

// 新闻表单
const newsForm = reactive({
  id: '',
  title: '',
  content: '',
  type: 0,
  publisherId: currentUserId.value || 1,
  status: 1
})

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(total.value / pageSize.value)
})

// 获取当前页数据
const getCurrentPageData = () => {
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  newsList.value = allNews.value.slice(startIndex, endIndex)
}

// 获取新闻列表
const fetchNews = async () => {
  try {
    loading.value = true
    // 直接获取所有数据，设置pageSize为1000
    const params = {
      pageNum: 1,
      pageSize: 1000, // 足够大的值，确保获取所有数据
      title: searchForm.title,
      type: searchForm.type ? parseInt(searchForm.type) : undefined,
      status: searchForm.status ? parseInt(searchForm.status) : undefined
    }
    
    console.log('请求参数:', params)
    const response = await newsApi.getNewsPage(params)
    console.log('响应数据完整结构:', JSON.stringify(response, null, 2))
    console.log('响应数据类型:', typeof response)
    console.log('响应数据是否包含records:', 'records' in response)
    
    // 正确处理后端返回的Page对象
    let records = [];
    if (Array.isArray(response)) {
      // 如果直接返回数组
      records = response;
      console.log('响应是数组类型，记录数:', records.length)
    } else if (response && response.records) {
      // 如果返回Page对象
      records = response.records;
      console.log('响应是Page对象，记录数:', records.length)
    } else {
      // 其他情况
      console.log('响应数据不符合预期结构')
    }
    
    console.log('最终记录数:', records.length)
    // 存储所有数据
    allNews.value = records
    total.value = records.length
    
    console.log('allNews值:', JSON.stringify(allNews.value, null, 2))
    console.log('total值:', total.value)
    
    // 获取当前页数据
    getCurrentPageData()
    console.log('当前页数据:', JSON.stringify(newsList.value, null, 2))
  } catch (error) {
    console.error('获取新闻列表失败:', error)
    console.error('错误详情:', JSON.stringify(error, null, 2))
    ElMessage.error('获取新闻列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索新闻
const searchNews = () => {
  currentPage.value = 1
  fetchNews()
}

// 重置搜索
const resetSearch = () => {
  searchForm.title = ''
  searchForm.type = ''
  searchForm.status = ''
  currentPage.value = 1
  fetchNews()
}

// 获取页码列表
const getPageNumbers = () => {
  const pages = []
  const maxPages = 7 // 增加显示的页码数量
  
  if (totalPages.value <= maxPages) {
    // 总页数少于最大显示页数，显示所有页码
    for (let i = 1; i <= totalPages.value; i++) {
      pages.push(i)
    }
  } else {
    // 总页数多于最大显示页数，需要省略号
    if (currentPage.value <= 4) {
      // 当前页在前面
      for (let i = 1; i <= 5; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(totalPages.value)
    } else if (currentPage.value >= totalPages.value - 3) {
      // 当前页在后面
      pages.push(1)
      pages.push('...')
      for (let i = totalPages.value - 4; i <= totalPages.value; i++) {
        pages.push(i)
      }
    } else {
      // 当前页在中间
      pages.push(1)
      pages.push('...')
      for (let i = currentPage.value - 1; i <= currentPage.value + 1; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(totalPages.value)
    }
  }
  
  return pages
}

// 分页处理
const handlePageChange = (page) => {
  if (page < 1 || page > totalPages.value || page === currentPage.value) {
    return
  }
  
  currentPage.value = page
  getCurrentPageData()
}

// 处理页面大小变化
const handlePageSizeChange = () => {
  currentPage.value = 1 // 重置到第一页
  getCurrentPageData()
}

// 处理跳转到指定页面
const handleJumpToPage = () => {
  const page = parseInt(jumpPage.value)
  
  if (isNaN(page) || page < 1 || page > totalPages.value) {
    ElMessage.warning(`请输入1到${totalPages.value}之间的页码`)
    return
  }
  
  handlePageChange(page)
  jumpPage.value = '' // 清空跳转页码
}

// 关闭新闻对话框
const closeNewsDialog = () => {
  showAddNewsDialog.value = false
  showEditNewsDialog.value = false
  resetNewsForm()
}

// 重置新闻表单
const resetNewsForm = () => {
  newsForm.id = ''
  newsForm.title = ''
  newsForm.content = ''
  newsForm.type = 0
  newsForm.publisherId = currentUserId.value
  newsForm.status = 1
}

// 保存新闻
const saveNews = async () => {
  if (!newsForm.title || !newsForm.content) {
    ElMessage.error('请填写完整的新闻信息')
    return
  }
  
  // 设置发布者ID为当前登录用户ID
  newsForm.publisherId = currentUserId.value
  
  try {
    loading.value = true
    let success = false
    
    if (newsForm.id) {
      // 编辑新闻
      const response = await newsApi.updateNews(newsForm.id, newsForm)
      success = response
    } else {
      // 新增新闻
      const response = await newsApi.createNews(newsForm)
      success = response
    }
    
    if (success) {
      ElMessage.success(newsForm.id ? '新闻更新成功' : '新闻发布成功')
      closeNewsDialog()
      await fetchNews()
    } else {
      ElMessage.error(newsForm.id ? '新闻更新失败' : '新闻发布失败')
    }
  } catch (error) {
    console.error('保存新闻失败:', error)
    ElMessage.error('保存新闻失败')
  } finally {
    loading.value = false
  }
}

// 查看新闻
const viewNews = (id) => {
  router.push(`/system/news/${id}`)
}

// 编辑新闻
const editNews = async (id) => {
  try {
    loading.value = true
    const news = await newsApi.getNewsById(id)
    if (news) {
      newsForm.id = news.id
      newsForm.title = news.title
      newsForm.content = news.content
      newsForm.type = news.type
      newsForm.publisherId = news.publisherId
      newsForm.status = news.status
      
      showEditNewsDialog.value = true
    } else {
      ElMessage.error('未找到新闻信息')
    }
  } catch (error) {
    console.error('获取新闻详情失败:', error)
    ElMessage.error('获取新闻详情失败')
  } finally {
    loading.value = false
  }
}

// 删除新闻
const deleteNews = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇新闻吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    const success = await newsApi.deleteNews(id)
    
    if (success) {
      ElMessage.success('新闻删除成功')
      await fetchNews()
    } else {
      ElMessage.error('新闻删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除新闻失败:', error)
      ElMessage.error('删除新闻失败')
    }
  } finally {
    loading.value = false
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  
  // 处理LocalDateTime格式，如"2025-12-30T14:30:00"
  try {
    // 将LocalDateTime格式转换为Date对象能解析的格式
    const formattedDateTime = typeof dateTime === 'string' ? dateTime.replace('T', ' ') : dateTime;
    return new Date(formattedDateTime).toLocaleString('zh-CN')
  } catch (error) {
    console.error('日期时间格式化失败:', error)
    return dateTime
  }
}

// 获取新闻类型标签
const getNewsTypeLabel = (type) => {
  // 处理类型为字符串的情况，并转换为数字
  const typeNum = typeof type === 'string' ? parseInt(type) : type;
  
  const typeMap = {
    0: '新闻',
    1: '公告',
    2: '政策解读'
  }
  return typeMap[typeNum] || `类型${typeNum}`
}

// 初始化
onMounted(async () => {
  await fetchNews()
})
</script>

<style scoped>
/* 使用与场地管理页面相同的样式 */
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

.title-cell {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.news-content {
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 300px;
  overflow-y: auto;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

/* 对话框样式 */
.system-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.system-modal {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  width: 700px;
  max-width: 90%;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  animation: modalFadeIn 0.3s ease-out;
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.system-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.system-modal-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.system-modal-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #909399;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.system-modal-close:hover {
  background-color: #f2f6fc;
  color: #606266;
}

.system-modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.system-modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 表单样式 */
.system-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.system-form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.system-form-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.system-form-input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
  transition: border-color 0.2s;
}

.system-form-input:focus {
  outline: none;
  border-color: #409eff;
}

.system-form-input:disabled {
  background-color: #f5f7fa;
  color: #c0c4cc;
  cursor: not-allowed;
}

/* 状态标签 */
.status-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

/* 分页样式 */
.system-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--system-space-md) var(--system-space-lg);
  border-top: 1px solid var(--system-border-light);
  flex-wrap: wrap;
  gap: var(--system-space-md);
}

.system-pagination-loading {
  opacity: 0.6;
  pointer-events: none;
}

.system-pagination-info {
  display: flex;
  align-items: center;
  gap: var(--system-space-md);
  font-size: var(--system-font-size-sm);
  color: var(--system-text-secondary);
}

.system-pagination-size-selector {
  display: flex;
  align-items: center;
  gap: var(--system-space-sm);
}

.system-pagination-controls {
  display: flex;
  align-items: center;
  gap: var(--system-space-xs);
}

.system-pagination-btn {
  min-width: 32px;
  height: 32px;
  padding: 0 var(--system-space-sm);
  border: 1px solid var(--system-border-color);
  background-color: var(--system-bg-white);
  color: var(--system-text-primary);
  border-radius: var(--system-border-radius-md);
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: var(--system-font-size-sm);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
}

.system-pagination-btn:hover:not(.system-pagination-btn-disabled):not(.system-pagination-btn-active) {
  border-color: var(--system-primary-color);
  color: var(--system-primary-color);
  background-color: var(--system-primary-light);
}

.system-pagination-btn-active {
  background-color: var(--system-primary-color);
  border-color: var(--system-primary-color);
  color: white;
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.3);
}

.system-pagination-btn-disabled {
  background-color: var(--system-bg-light);
  color: var(--system-text-disabled);
  border-color: var(--system-border-light);
  cursor: not-allowed;
}

.system-pagination-ellipsis {
  padding: 0 var(--system-space-xs);
  color: var(--system-text-disabled);
  font-size: var(--system-font-size-sm);
  user-select: none;
}

.system-pagination-jump {
  display: flex;
  align-items: center;
  gap: var(--system-space-xs);
  margin-left: var(--system-space-md);
  font-size: var(--system-font-size-sm);
  color: var(--system-text-secondary);
}

.system-pagination-jump input {
  width: 50px;
  height: 32px;
  padding: 0 var(--system-space-xs);
  border: 1px solid var(--system-border-color);
  border-radius: var(--system-border-radius-md);
  text-align: center;
  font-size: var(--system-font-size-sm);
}

.system-pagination-jump input:focus {
  outline: none;
  border-color: var(--system-primary-color);
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .system-pagination {
    flex-direction: column;
    align-items: stretch;
    gap: var(--system-space-sm);
  }
  
  .system-pagination-info {
    justify-content: center;
  }
  
  .system-pagination-controls {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .system-pagination-jump {
    margin-left: 0;
    margin-top: var(--system-space-sm);
    justify-content: center;
  }
}
</style>