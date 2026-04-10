<template>
  <div class="system-page">
    <div class="system-page-title">
      <div class="system-page-title-text">
        <span class="system-page-title-icon">🏟️</span>
        场地列表管理
      </div>
      <div class="system-page-title-actions">
        <el-button type="primary" @click="showAddVenueDialog = true">
          <span class="system-button-icon">+</span>
          新增场地
        </el-button>
      </div>
    </div>

    <!-- 场地统计卡片 -->
    <div class="system-stats-container">
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">场地总数</div>
          <div class="system-stats-icon" style="background-color: rgba(24, 144, 255, 0.1); color: var(--system-primary-color);">
            <el-icon><Calendar /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.totalCount || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">可用场地</div>
          <div class="system-stats-icon" style="background-color: rgba(82, 196, 26, 0.1); color: var(--system-success-color);">
            <el-icon><Check /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.availableCount || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">维护中场地</div>
          <div class="system-stats-icon" style="background-color: rgba(250, 140, 22, 0.1); color: #fa8c16;">
            <el-icon><Warning /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.maintenanceCount || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">不可用场地</div>
          <div class="system-stats-icon" style="background-color: rgba(255, 77, 79, 0.1); color: var(--system-error-color);">
            <el-icon><Close /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.unavailableCount || 0 }}</div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="system-search-bar">
      <!-- 基础搜索条件 -->
      <div class="system-search-basic">
        <div class="system-search-item">
          <div class="system-form-label">场地名称</div>
          <input class="system-form-input" type="text" v-model="searchForm.name" placeholder="请输入场地名称">
        </div>
        <div class="system-search-item">
          <div class="system-form-label">场地状态</div>
          <select class="system-form-input" v-model="searchForm.status">
            <option value="">全部状态</option>
            <option value="1">可用</option>
            <option value="2">维护中</option>
            <option value="0">不可用</option>
          </select>
        </div>
        <div class="system-search-actions">
          <el-button type="primary" @click="searchVenues">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>
      
      <!-- 批量操作 -->
      <div class="system-search-batch">
        <div class="batch-operations">
          <div class="batch-header">
            <span class="batch-title">批量操作</span>
            <el-button type="warning" @click="batchUpdateStatus" :disabled="!canBatchUpdate">批量更新</el-button>
          </div>
          <div class="batch-content">
            <div class="batch-section maintenance-section">
              <div class="system-form-label">维护日期</div>
              <div class="maintenance-controls">
                <el-date-picker
                  v-model="batchForm.maintenanceDate"
                  type="date"
                  placeholder="选择维护日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  :clearable="true"
                  class="batch-date-picker"
                />
                <div class="time-range">
                  <el-time-select
                    v-model="batchForm.startTime"
                    placeholder="请选择开始时间"
                    :clearable="true"
                    class="batch-time-select"
                    start="00:00"
                    end="23:30"
                    step="00:30"
                  />
                  <span class="time-separator">至</span>
                  <el-time-select
                    v-model="batchForm.endTime"
                    placeholder="请选择结束时间"
                    :clearable="true"
                    class="batch-time-select"
                    start="00:00"
                    end="23:30"
                    step="00:30"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 场地列表 -->
    <div class="system-table-container">
      <table class="system-table">
        <thead>
          <tr>
            <th>
              <input type="checkbox" @change="toggleSelectAll" :checked="isAllSelected">
            </th>
            <th>场地名称</th>
            <th>容量</th>
            <th>设备信息</th>
            <th>开放时间</th>
            <th>关闭时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="venue in venues" :key="venue.id">
            <td>
              <input type="checkbox" :value="venue.id" v-model="selectedVenues" @change="updateSelectAll">
            </td>
            <td>{{ venue.name }}</td>
            <td>{{ venue.capacity }}人</td>
            <td>{{ venue.equipment }}</td>
            <td>{{ venue.openTime }}</td>
            <td>{{ venue.closeTime }}</td>
            <td>
              <div class="venue-status-cell">
                <div v-if="venue.status === 1" class="status-tag status-available">可用</div>
                <div v-else-if="venue.status === 2" class="status-maintenance-container">
                  <span class="status-tag status-maintenance">维护中</span>
                  <div v-if="venue.maintenanceDate || venue.maintenanceStartTime || venue.maintenanceEndTime" class="maintenance-info">
                    <span class="maintenance-icon">⚠️</span>
                    <span class="maintenance-text">
                      {{ formatDate(venue.maintenanceDate) }} {{ venue.maintenanceStartTime }}-{{ venue.maintenanceEndTime }}
                    </span>
                  </div>
                </div>
                <div v-else class="status-tag status-unavailable">不可用</div>
              </div>
            </td>
            <td>
              <div class="action-btn-group">
                <el-button size="small" type="info" @click="viewVenue(venue.id)">查看</el-button>
                <el-button size="small" type="primary" @click="editVenue(venue.id)">编辑</el-button>
                <el-button v-if="venue.status === 1" size="small" type="warning" @click="disableVenue(venue.id)">禁用</el-button>
                <el-button v-else-if="venue.status === 2" size="small" type="warning" @click="disableVenue(venue.id)">禁用</el-button>
                <el-button v-else size="small" type="success" @click="enableVenue(venue.id)">启用</el-button>
                <el-button size="small" type="danger" @click="deleteVenue(venue.id)">删除</el-button>
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

    <!-- 添加场地对话框 -->
    <div v-if="showAddVenueDialog" class="system-modal-overlay" @click.self="closeVenueDialog">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">添加场地</div>
          <button class="system-modal-close" @click="closeVenueDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">场地名称</div>
              <input class="system-form-input" type="text" v-model="venueForm.name" placeholder="请输入场地名称">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">场地描述</div>
              <textarea class="system-form-input" rows="3" v-model="venueForm.description" placeholder="请输入场地描述"></textarea>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">容量</div>
              <input class="system-form-input" type="number" v-model="venueForm.capacity" placeholder="请输入场地容量">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">设备信息</div>
              <input class="system-form-input" type="text" v-model="venueForm.equipment" placeholder="请输入设备信息">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">开放时间</div>
              <select class="system-form-input" v-model="venueForm.openTime">
                <option value="">请选择开放时间</option>
                <option v-for="time in timeOptions" :key="time" :value="time">{{ time }}</option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">关闭时间</div>
              <select class="system-form-input" v-model="venueForm.closeTime">
                <option value="">请选择关闭时间</option>
                <option v-for="time in timeOptions" :key="time" :value="time">{{ time }}</option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">状态</div>
              <select class="system-form-input" v-model="venueForm.status">
                <option value="1">可用</option>
                <option value="2">维护中</option>
                <option value="0">不可用</option>
              </select>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <el-button @click="closeVenueDialog">取消</el-button>
          <el-button type="primary" @click="saveVenue">保存</el-button>
        </div>
      </div>
    </div>

    <!-- 编辑场地对话框 -->
    <div v-if="showEditVenueDialog" class="system-modal-overlay" @click.self="closeVenueDialog">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">编辑场地</div>
          <button class="system-modal-close" @click="closeVenueDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">场地名称</div>
              <input class="system-form-input" type="text" v-model="venueForm.name" placeholder="请输入场地名称">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">场地描述</div>
              <textarea class="system-form-input" rows="3" v-model="venueForm.description" placeholder="请输入场地描述"></textarea>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">容量</div>
              <input class="system-form-input" type="number" v-model="venueForm.capacity" placeholder="请输入场地容量">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">设备信息</div>
              <input class="system-form-input" type="text" v-model="venueForm.equipment" placeholder="请输入设备信息">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">开放时间</div>
              <select class="system-form-input" v-model="venueForm.openTime">
                <option value="">请选择开放时间</option>
                <option v-for="time in timeOptions" :key="time" :value="time">{{ time }}</option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">关闭时间</div>
              <select class="system-form-input" v-model="venueForm.closeTime">
                <option value="">请选择关闭时间</option>
                <option v-for="time in timeOptions" :key="time" :value="time">{{ time }}</option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">状态</div>
              <select class="system-form-input" v-model="venueForm.status">
                <option value="1">可用</option>
                <option value="2">维护中</option>
                <option value="0">不可用</option>
              </select>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <el-button @click="closeVenueDialog">取消</el-button>
          <el-button type="primary" @click="saveVenue">保存</el-button>
        </div>
      </div>
    </div>

    <!-- 查看场地对话框 -->
    <div v-if="showViewVenueDialog" class="system-modal-overlay" @click.self="closeViewVenueDialog">
      <div class="system-modal system-modal-large">
        <div class="system-modal-header">
          <div class="system-modal-title">查看场地</div>
          <button class="system-modal-close" @click="closeViewVenueDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="venue-detail-container">
            <!-- 场地基本信息卡片 -->
            <div class="detail-card">
              <div class="detail-card-header">
                <h2>{{ currentVenue.name }}</h2>
                <div class="status-tag" :class="getStatusClass(currentVenue.status)">
                  {{ getStatusText(currentVenue.status) }}
                </div>
              </div>
              
              <div class="detail-card-body">
                <div class="detail-row">
                  <div class="detail-label">场地描述：</div>
                  <div class="detail-value">{{ currentVenue.description }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">容量：</div>
                  <div class="detail-value">{{ currentVenue.capacity }}人</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">设备信息：</div>
                  <div class="detail-value">{{ currentVenue.equipment }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">开放时间：</div>
                  <div class="detail-value">{{ currentVenue.openTime }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">关闭时间：</div>
                  <div class="detail-value">{{ currentVenue.closeTime }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">维护日期：</div>
                  <div class="detail-value">{{ formatDate(currentVenue.maintenanceDate) || '未设置' }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">维护开始时间：</div>
                  <div class="detail-value">{{ currentVenue.maintenanceStartTime || '未设置' }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">维护结束时间：</div>
                  <div class="detail-value">{{ currentVenue.maintenanceEndTime || '未设置' }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Check, Close, Warning } from '@element-plus/icons-vue'
import { venueApi } from '@/api/venue'

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 格式化日期
const formatDate = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 搜索表单
const searchForm = reactive({
  name: '',
  status: ''
})

// 批量操作表单
const batchForm = reactive({
  maintenanceDate: '',
  startTime: '',
  endTime: '',
  status: ''
})

// 场地列表
const venues = ref([])
const total = ref(0)
const loading = ref(false)
const allVenues = ref([])
const selectedVenues = ref([])

// 定时刷新间隔
let refreshInterval = null

// 统计数据
const statsData = reactive({
  totalCount: 0,
  availableCount: 0,
  maintenanceCount: 0,
  unavailableCount: 0
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const jumpPage = ref('') // 跳转页码

// 对话框状态
const showAddVenueDialog = ref(false)
const showEditVenueDialog = ref(false)
const showViewVenueDialog = ref(false)
const currentVenue = ref({})

// 场地表单
const venueForm = reactive({
  id: '',
  name: '',
  description: '',
  capacity: '',
  equipment: '',
  openTime: '',
  closeTime: '',
  status: 1
})

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(total.value / pageSize.value)
})

// 计算是否可以批量更新
const canBatchUpdate = computed(() => {
  return batchForm.maintenanceDate && batchForm.startTime && batchForm.endTime
})

// 生成30分钟间隔的时间选项
const timeOptions = computed(() => {
  const options = []
  for (let hour = 0; hour < 24; hour++) {
    for (let minute = 0; minute < 60; minute += 30) {
      const timeStr = `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
      options.push(timeStr)
    }
  }
  return options
})

// 计算是否全选
const isAllSelected = computed(() => {
  return venues.value.length > 0 && selectedVenues.value.length === venues.value.length
})

// 更新统计数据
const updateStatistics = () => {
  statsData.totalCount = allVenues.value.length
  statsData.availableCount = allVenues.value.filter(item => item.status === 1).length
  statsData.maintenanceCount = allVenues.value.filter(item => item.status === 2).length
  statsData.unavailableCount = allVenues.value.filter(item => item.status === 0).length
}

// 获取当前页数据
const getCurrentPageData = () => {
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  venues.value = allVenues.value.slice(startIndex, endIndex)
}

// 获取场地列表
const fetchVenues = async () => {
  try {
    loading.value = true
    // 获取所有数据，设置一个足够大的pageSize
    const params = {
      pageNum: 1,
      pageSize: 1000, // 假设最大数据量不超过1000
      name: searchForm.name,
      status: searchForm.status
    }
    
    console.log('获取场地列表参数:', params)
    
    const response = await venueApi.getVenuePage(params)
    
    console.log('获取场地列表响应:', response)
    
    const records = response.records || response.data?.records || response.data || [];
    
    console.log('处理后的场地数据:', records)
    
    // 存储所有数据
    allVenues.value = records
    total.value = records.length
    
    // 获取当前页数据
    getCurrentPageData()
    
    // 更新统计数据（基于所有数据）
    updateStatistics()
  } catch (error) {
    console.error('获取场地列表失败:', error)
    ElMessage.error('获取场地列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索场地
const searchVenues = () => {
  currentPage.value = 1
  fetchVenues()
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.status = ''
  batchForm.maintenanceDate = ''
  batchForm.startTime = ''
  batchForm.endTime = ''
  batchForm.status = ''
  selectedVenues.value = []
  currentPage.value = 1
  fetchVenues()
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

// 关闭场地对话框
const closeVenueDialog = () => {
  showAddVenueDialog.value = false
  showEditVenueDialog.value = false
  resetVenueForm()
}

// 关闭查看场地对话框
const closeViewVenueDialog = () => {
  showViewVenueDialog.value = false
  currentVenue.value = {}
}

// 获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 1: return 'status-available'
    case 2: return 'status-maintenance'
    case 0: return 'status-unavailable'
    default: return ''
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 1: return '可用'
    case 2: return '维护中'
    case 0: return '不可用'
    default: return '未知'
  }
}

// 重置场地表单
const resetVenueForm = () => {
  venueForm.id = ''
  venueForm.name = ''
  venueForm.description = ''
  venueForm.capacity = ''
  venueForm.equipment = ''
  venueForm.openTime = ''
  venueForm.closeTime = ''
  venueForm.status = 1
}

// 保存场地
const saveVenue = async () => {
  if (!venueForm.name || !venueForm.capacity || !venueForm.openTime || !venueForm.closeTime) {
    ElMessage.error('请填写完整的场地信息')
    return
  }
  
  try {
    loading.value = true
    let success = false
    
    if (venueForm.id) {
      // 编辑场地
      const response = await venueApi.updateVenue(venueForm.id, venueForm)
      success = response.data || response
    } else {
      // 新增场地
      const response = await venueApi.createVenue(venueForm)
      success = response.data || response
    }
    
    if (success) {
      ElMessage.success(venueForm.id ? '场地更新成功' : '场地创建成功')
      closeVenueDialog()
      await fetchVenues()
    } else {
      ElMessage.error(venueForm.id ? '场地更新失败' : '场地创建失败')
    }
  } catch (error) {
    console.error('保存场地失败:', error)
    ElMessage.error('保存场地失败')
  } finally {
    loading.value = false
  }
}

// 查看场地
const viewVenue = async (id) => {
  try {
    loading.value = true
    const response = await venueApi.getVenueById(id)
    const venue = response.data || response
    if (venue) {
      currentVenue.value = venue
      showViewVenueDialog.value = true
    } else {
      ElMessage.error('未找到场地信息')
    }
  } catch (error) {
    console.error('获取场地详情失败:', error)
    ElMessage.error('获取场地详情失败')
  } finally {
    loading.value = false
  }
}

// 编辑场地
const editVenue = async (id) => {
  try {
    loading.value = true
    const response = await venueApi.getVenueById(id)
    const venue = response.data || response
    if (venue) {
      venueForm.id = venue.id
      venueForm.name = venue.name
      venueForm.description = venue.description
      venueForm.capacity = venue.capacity
      venueForm.equipment = venue.equipment
      venueForm.openTime = venue.openTime
      venueForm.closeTime = venue.closeTime
      venueForm.status = venue.status
      
      showEditVenueDialog.value = true
    } else {
      ElMessage.error('未找到场地信息')
    }
  } catch (error) {
    console.error('获取场地详情失败:', error)
    ElMessage.error('获取场地详情失败')
  } finally {
    loading.value = false
  }
}

// 删除场地
const deleteVenue = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个场地吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    const response = await venueApi.deleteVenue(id)
    const success = response.data || response
    
    if (success) {
      ElMessage.success('场地删除成功')
      await fetchVenues()
    } else {
      ElMessage.error('场地删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除场地失败:', error)
      ElMessage.error('删除场地失败')
    }
  } finally {
    loading.value = false
  }
}

// 启用场地
const enableVenue = async (id) => {
  try {
    loading.value = true
    const response = await venueApi.updateVenue(id, { status: 1 })
    const success = response.data || response
    
    if (success) {
      ElMessage.success('场地启用成功')
      await fetchVenues()
    } else {
      ElMessage.error('场地启用失败')
    }
  } catch (error) {
    console.error('启用场地失败:', error)
    ElMessage.error('场地启用失败')
  } finally {
    loading.value = false
  }
}

// 禁用场地
const disableVenue = async (id) => {
  try {
    loading.value = true
    const response = await venueApi.updateVenue(id, { status: 0 })
    const success = response.data || response
    
    if (success) {
      ElMessage.success('场地禁用成功')
      await fetchVenues()
    } else {
      ElMessage.error('场地禁用失败')
    }
  } catch (error) {
    console.error('禁用场地失败:', error)
    ElMessage.error('场地禁用失败')
  } finally {
    loading.value = false
  }
}

// 批量更新场地状态
const batchUpdateStatus = async () => {
  if (!canBatchUpdate.value) {
    ElMessage.warning('请同时填写维护日期和时间段')
    return
  }
  
  try {
    loading.value = true
    
    const params = {
      venueIds: selectedVenues.value,
      maintenanceDate: batchForm.maintenanceDate,
      startTime: batchForm.startTime,
      endTime: batchForm.endTime,
      status: 2
    }
    
    console.log('批量更新参数:', params)
    
    const response = await venueApi.batchUpdateVenueStatus(params)
    
    console.log('批量更新响应:', response)
    
    if (response.code === 200) {
      ElMessage.success('批量更新成功')
      selectedVenues.value = []
      batchForm.maintenanceDate = ''
      batchForm.startTime = ''
      batchForm.endTime = ''
      searchForm.status = ''
      console.log('开始重新加载数据')
      await fetchVenues()
      console.log('数据加载完成，新场地数量:', venues.value.length)
      console.log('数据加载完成，场地状态:', venues.value.map(v => ({id: v.id, status: v.status})))
    } else {
      const errorMsg = response.message || '批量更新失败'
      ElMessage.error(errorMsg)
    }
  } catch (error) {
    console.error('批量更新失败:', error)
    ElMessage.error('批量更新失败')
  } finally {
    loading.value = false
  }
}

// 全选/取消全选
const toggleSelectAll = (checked) => {
  if (checked) {
    // 全选
    selectedVenues.value = venues.value.map(venue => venue.id)
  } else {
    // 取消全选
    selectedVenues.value = []
  }
}

// 更新全选状态
const updateSelectAll = () => {
  // 当选择发生变化时，更新全选状态
}

// 初始化
onMounted(async () => {
  await fetchVenues()
  
  refreshInterval = setInterval(async () => {
    await fetchVenues()
  }, 60000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})

// 监听选择变化，更新全选状态
watch(selectedVenues, () => {
  updateSelectAll()
})
</script>

<style scoped>
/* 使用与服务预约页面相同的样式 */
.status-available {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-maintenance {
  background-color: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

.status-unavailable {
  background-color: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffa39e;
}

/* 场地详情卡片样式 */
.venue-detail-container {
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
  font-size: 18px;
  font-weight: 600;
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

.venue-status-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}

.status-maintenance-container {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}

.maintenance-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #fa8c16;
  background-color: #fff7e6;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid #ffd591;
  max-width: 200px;
}

.maintenance-icon {
  font-size: 12px;
  flex-shrink: 0;
}

.maintenance-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.description-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 批量操作样式 */
.batch-operations {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #eaeaea;
  margin-top: 16px;
}

.batch-date-section,
.batch-time-section,
.batch-status-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-range input {
  flex: 1;
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
  width: 500px;
  max-width: 90%;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  animation: modalFadeIn 0.3s ease-out;
}

.system-modal-large {
  width: 600px;
  max-width: 95%;
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

/* 搜索栏样式 */
.system-search-bar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

/* 基础搜索条件 */
.system-search-basic {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #eaeaea;
}

.system-search-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.system-search-item .system-form-label {
  min-width: 80px;
  font-weight: 500;
  color: #606266;
}

.system-search-item .system-form-input {
  min-width: 200px;
}

.system-search-actions {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

/* 批量操作 */
.system-search-batch {
  width: 100%;
}

.batch-operations {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #eaeaea;
}

.batch-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #eaeaea;
}

.batch-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.batch-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.batch-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.batch-section .system-form-label {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
}

.batch-section .system-form-input {
  width: 100%;
}

.batch-section .time-range {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.batch-section .time-range input {
  flex: 1;
}

/* 维护日期和时间控件水平布局 */
.maintenance-section {
  grid-column: 1 / -1;
}

.maintenance-controls {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.maintenance-controls .batch-date-picker {
  width: 180px;
}

.maintenance-controls .time-range {
  flex: 1;
  min-width: 300px;
}

.maintenance-controls .batch-time-select {
  width: 120px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .system-search-basic {
    flex-direction: column;
    align-items: stretch;
  }
  
  .system-search-actions {
    margin-left: 0;
    justify-content: flex-end;
  }
  
  .batch-content {
    grid-template-columns: 1fr;
  }
}

/* 状态标签 */
.status-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
}

/* 表格样式 */
.system-table {
  width: 100%;
  border-collapse: collapse;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  overflow: hidden;
}

.system-table th,
.system-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.system-table th {
  background-color: #f8f9fa;
  font-weight: 500;
  color: #303133;
  text-align: left;
  white-space: nowrap;
}

.system-table th:nth-child(7),
.system-table td:nth-child(7) {
  min-width: 80px;
  text-align: center;
}

/* 容量列样式 */
.system-table th:nth-child(3),
.system-table td:nth-child(3) {
  white-space: nowrap;
}

/* 时间列样式 */
.system-table th:nth-child(5),
.system-table td:nth-child(5),
.system-table th:nth-child(6),
.system-table td:nth-child(6) {
  min-width: 100px;
  text-align: center;
  white-space: nowrap;
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

/* 操作按钮组样式 */
.action-btn-group {
  display: flex;
  gap: 8px;
}

/* 表单值容器样式 */
.value-container {
  background-color: #f5f7fa;
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  color: #303133;
  font-weight: 500;
}

.btn {
  padding: 4px 12px;
  border: 1px solid transparent;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.btn-primary {
  background-color: #1890ff;
  color: white;
  border-color: #1890ff;
}

.btn-primary:hover {
  background-color: #40a9ff;
  border-color: #40a9ff;
}

.btn-default {
  background-color: #fff;
  color: #333;
  border-color: #d9d9d9;
}

.btn-default:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.btn-danger {
  background-color: #ff4d4f;
  color: white;
  border-color: #ff4d4f;
}

.btn-danger:hover {
  background-color: #ff7875;
  border-color: #ff7875;
}

.btn-info {
  background-color: #17a2b8;
  color: white;
  border-color: #17a2b8;
}

.btn-info:hover {
  background-color: #138496;
  border-color: #138496;
}

.btn-success {
  background-color: #52c41a;
  color: white;
  border-color: #52c41a;
}

.btn-success:hover {
  background-color: #73d13d;
  border-color: #73d13d;
}

.btn-warning {
  background-color: #faad14;
  color: white;
  border-color: #faad14;
}

.btn-warning:hover {
  background-color: #ffc53d;
  border-color: #ffc53d;
}
</style>