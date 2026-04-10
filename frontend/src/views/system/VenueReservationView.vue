<template>
  <div class="system-page">
    <!-- 页面标题 -->
    <div class="system-page-title">
      <div class="system-page-title-text">
        <span class="system-page-title-icon">🏟️</span>
        场地预约管理
      </div>
      <div class="system-page-title-actions">
        <button class="system-button system-button-primary" @click="showAddReservationDialog = true" :disabled="loading">
          <span class="system-button-icon">+</span>
          新增预约
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="system-stats-container">
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">总预约数</div>
          <div class="system-stats-icon" style="background-color: rgba(24, 144, 255, 0.1); color: var(--system-primary-color);">
            <el-icon><Calendar /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.totalCount || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">待审核</div>
          <div class="system-stats-icon" style="background-color: rgba(230, 162, 60, 0.1); color: var(--system-warning-color);">
            <el-icon><Warning /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.pendingCount || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">已通过</div>
          <div class="system-stats-icon" style="background-color: rgba(82, 196, 26, 0.1); color: var(--system-success-color);">
            <el-icon><Check /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.approvedCount || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">已拒绝</div>
          <div class="system-stats-icon" style="background-color: rgba(255, 77, 79, 0.1); color: var(--system-error-color);">
            <el-icon><Close /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.rejectedCount || 0 }}</div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="system-search-bar">
      <div class="system-search-form">
        <div class="system-search-item">
          <div class="system-form-label">场地名称</div>
          <input 
            type="text" 
            class="system-form-input" 
            v-model="searchForm.venueName"
            placeholder="请输入场地名称"
          >
        </div>
        <div class="system-search-item">
          <div class="system-form-label">预约人</div>
          <input 
            type="text" 
            class="system-form-input" 
            v-model="searchForm.userName"
            placeholder="请输入预约人姓名"
          >
        </div>
        <div class="system-search-item">
          <div class="system-form-label">预约状态</div>
          <select class="system-form-input" v-model="searchForm.status">
            <option value="">全部状态</option>
            <option value="0">待审核</option>
            <option value="1">已通过</option>
            <option value="2">已拒绝</option>
          </select>
        </div>
        <div class="system-search-actions">
          <el-button type="primary" @click="searchReservations">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 批量操作区域 -->
    <div class="system-batch-actions" v-if="selectedReservations.length > 0">
      <div class="system-batch-actions-info">
        <span class="system-batch-actions-count">已选择 {{ selectedReservations.length }} 条记录</span>
      </div>
      <div class="system-batch-actions-buttons">
        <button class="system-button system-button-success" @click="batchApprove">
          <span class="system-button-icon">✓</span>
          批量通过
        </button>
        <button class="system-button system-button-warning" @click="batchReject">
          <span class="system-button-icon">✕</span>
          批量拒绝
        </button>
        <button class="system-button system-button-default" @click="clearSelection">
          <span class="system-button-icon">✕</span>
          清空选择
        </button>
      </div>
    </div>

    <!-- 预约列表 -->
    <div class="system-table-container">
      <table class="system-table">
        <thead>
          <tr>
            <th style="width: 50px; text-align: center;">
              <input type="checkbox" v-model="selectAll" @change="handleSelectAll" />
            </th>
            <th>场地名称</th>
            <th>预约人</th>
            <th>预约日期</th>
            <th>开始时间</th>
            <th>结束时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="reservation in reservations" :key="reservation.id">
            <td style="text-align: center;">
              <input 
                type="checkbox" 
                v-model="selectedReservations" 
                :value="reservation.id"
                @change="handleSelectionChange"
              />
            </td>
            <td>{{ reservation.venueName }}</td>
            <td>{{ reservation.userName }}</td>
            <td>{{ formatDate(reservation.reservationDate) }}</td>
            <td>{{ formatTime(reservation.startTime) }}</td>
            <td>{{ formatTime(reservation.endTime) }}</td>
            <td>
              <span v-if="reservation.status === 0" class="status-tag status-pending">待审核</span>
              <span v-else-if="reservation.status === 1" class="status-tag status-approved">已通过</span>
              <span v-else class="status-tag status-rejected">已拒绝</span>
            </td>
            <td>
              <div class="action-btn-group">
                <el-button size="small" type="info" @click="viewReservation(reservation.id)">查看</el-button>
                <el-button size="small" type="danger" @click="deleteReservation(reservation.id)">删除</el-button>
                <el-button 
                  v-if="reservation.status === 0" 
                  size="small"
                  type="success"
                  @click="approveReservation(reservation.id)"
                >
                  通过
                </el-button>
                <el-button 
                  v-if="reservation.status === 0" 
                  size="small"
                  type="warning"
                  @click="rejectReservation(reservation.id)"
                >
                  拒绝
                </el-button>
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

    <!-- 查看预约对话框 -->
    <div v-if="showViewVenueDialog" class="system-modal-overlay" @click.self="closeViewVenueDialog">
      <div class="system-modal system-modal-large">
        <div class="system-modal-header">
          <div class="system-modal-title">预约详情</div>
          <button class="system-modal-close" @click="closeViewVenueDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="venue-detail-container">
            <!-- 预约基本信息卡片 -->
            <div class="detail-card">
              <div class="detail-card-header">
                <h2>{{ currentVenue.venueName }}</h2>
                <div class="status-tag" :class="getStatusClass(currentVenue.status)">
                  {{ getStatusText(currentVenue.status) }}
                </div>
              </div>
              
              <div class="detail-card-body">
                <div class="detail-row">
                  <div class="detail-label">预约人：</div>
                  <div class="detail-value">{{ currentVenue.userName }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">预约日期：</div>
                  <div class="detail-value">{{ formatDate(currentVenue.reservationDate) }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">开始时间：</div>
                  <div class="detail-value">{{ currentVenue.startTime }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">结束时间：</div>
                  <div class="detail-value">{{ currentVenue.endTime }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">预约用途：</div>
                  <div class="detail-value">{{ currentVenue.purpose }}</div>
                </div>
              </div>
            </div>

            <!-- 审核信息卡片（仅非待审核状态显示） -->
            <div v-if="currentVenue.status !== 0" class="detail-card">
              <div class="detail-card-header">
                <h3>审核信息</h3>
              </div>
              <div class="detail-card-body">
                <div class="detail-row">
                  <div class="detail-label">审核人：</div>
                  <div class="detail-value">{{ currentVenue.reviewerName || '未知' }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">审核时间：</div>
                  <div class="detail-value">{{ formatDateTime(currentVenue.reviewTime) }}</div>
                </div>
                
                <div v-if="currentVenue.reviewComment" class="detail-row">
                  <div class="detail-label">审核意见：</div>
                  <div class="detail-value">{{ currentVenue.reviewComment }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 拒绝预约对话框 -->
    <div v-if="rejectDialogVisible" class="system-modal-overlay" @click.self="cancelRejectReservation">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">拒绝预约</div>
          <button class="system-modal-close" @click="cancelRejectReservation">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">拒绝原因</div>
              <textarea 
                class="system-form-input" 
                v-model="rejectComment" 
                placeholder="请输入拒绝原因" 
                rows="4"
                maxlength="200"
              ></textarea>
              <div class="system-form-hint">
                已输入 {{ rejectComment.length }}/200 字符
              </div>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <el-button @click="cancelRejectReservation">取消</el-button>
          <el-button 
            type="danger" 
            @click="confirmRejectReservation"
            :loading="reviewLoading"
          >
            确认拒绝
          </el-button>
        </div>
      </div>
    </div>

    <!-- 新增预约对话框 -->
    <div v-if="showAddReservationDialog" class="system-modal-overlay" @click.self="closeAddReservationDialog">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">新增预约</div>
          <button class="system-modal-close" @click="closeAddReservationDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">场地名称 <span class="required">*</span></div>
              <select 
                class="system-form-input" 
                v-model="reservationForm.venueId"
                :class="{ 'system-form-input-error': formErrors.venueId }"
                @change="handleVenueChange"
              >
                <option value="">请选择场地</option>
                <option v-for="venue in venueList" :key="venue.id" :value="venue.id">
                  {{ venue.name }} ({{ venue.openTime || '--' }}-{{ venue.closeTime || '--' }})
                </option>
              </select>
              <div v-if="formErrors.venueId" class="system-form-error">{{ formErrors.venueId }}</div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">预约人 <span class="required">*</span></div>
              <select 
                class="system-form-input" 
                v-model="reservationForm.userId"
                :class="{ 'system-form-input-error': formErrors.userId }"
                @change="validateUserId"
              >
                <option value="">请选择预约人</option>
                <option v-for="user in userList" :key="user.id" :value="user.id">
                  {{ user.name }}
                </option>
              </select>
              <div v-if="formErrors.userId" class="system-form-error">{{ formErrors.userId }}</div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">预约日期 <span class="required">*</span></div>
              <el-date-picker
                v-model="reservationForm.reservationDate"
                type="date"
                placeholder="选择预约日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                :disabled-date="disabledDate"
                :class="{ 'system-form-input-error': formErrors.reservationDate }"
                @change="validateReservationDate"
                class="venue-date-picker"
              />
              <div v-if="formErrors.reservationDate" class="system-form-error">{{ formErrors.reservationDate }}</div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">开始时间 <span class="required">*</span></div>
              <select 
                class="system-form-input time-select" 
                v-model="reservationForm.startTime"
                :class="{ 'system-form-input-error': formErrors.startTime }"
                @change="validateStartTime"
              >
                <option value="">请选择开始时间</option>
                <option v-for="time in availableStartTimeSlots" :key="time.value" :value="time.value">
                  {{ time.label }}
                </option>
              </select>
              <div v-if="formErrors.startTime" class="system-form-error">{{ formErrors.startTime }}</div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">结束时间 <span class="required">*</span></div>
              <select 
                class="system-form-input time-select" 
                v-model="reservationForm.endTime"
                :class="{ 'system-form-input-error': formErrors.endTime }"
                @change="validateEndTime"
              >
                <option value="">请选择结束时间</option>
                <option v-for="time in availableEndTimeSlots" :key="time.value" :value="time.value">
                  {{ time.label }}
                </option>
              </select>
              <div v-if="formErrors.endTime" class="system-form-error">{{ formErrors.endTime }}</div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">预约用途</div>
              <textarea 
                class="system-form-input" 
                v-model="reservationForm.purpose" 
                placeholder="请输入预约用途" 
                rows="3"
                maxlength="200"
              ></textarea>
              <div class="system-form-hint">
                已输入 {{ reservationForm.purpose.length }}/200 字符
              </div>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <el-button @click="closeAddReservationDialog">取消</el-button>
          <el-button 
            type="primary" 
            @click="saveNewReservation"
            :loading="submitting"
          >
            {{ submitting ? '提交中...' : '保存' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 批量审核对话框 -->
    <div v-if="showBatchApproveDialog" class="system-modal-overlay" @click.self="closeBatchApproveDialog">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">批量审核预约</div>
          <button class="system-modal-close" @click="closeBatchApproveDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form-group">
            <div class="system-form-label">审核操作</div>
            <select class="system-form-input" v-model="batchApproveForm.status">
              <option value="1">通过</option>
              <option value="2">拒绝</option>
            </select>
          </div>
          <div class="system-form-group">
            <div class="system-form-label">备注信息</div>
            <textarea class="system-form-textarea" v-model="batchApproveForm.remark" placeholder="请输入备注信息" rows="4"></textarea>
          </div>
          <div class="system-form-group">
            <div class="system-form-label">已选择预约</div>
            <div class="selected-reservations-list">
              <div v-for="id in selectedReservations" :key="id" class="selected-reservation-item">
                {{ getReservationInfo(id) }}
              </div>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <el-button @click="closeBatchApproveDialog">取消</el-button>
          <el-button 
            type="primary" 
            @click="submitBatchApprove"
            :loading="reviewLoading"
          >
            {{ reviewLoading ? '审核中...' : '确定' }}
          </el-button>
        </div>
      </div>
    </div>


  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Check, Close, Warning, Search } from '@element-plus/icons-vue'
import { reservationApi } from '@/api/reservation'
import { venueApi } from '@/api/venue'
import { userApi } from '@/api/user'

// 格式化日期为API需要的格式
const formatDateForAPI = (date) => {
  if (!date) {
    console.error('formatDateForAPI: 日期为空')
    return ''
  }
  
  let d
  try {
    // 确保创建有效的Date对象
    d = new Date(date)
    
    // 检查日期是否有效
    if (isNaN(d.getTime())) {
      console.error('formatDateForAPI: 无效的日期对象', date)
      return ''
    }
  } catch (error) {
    console.error('formatDateForAPI: 创建日期对象失败', error)
    return ''
  }
  
  try {
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const formattedDate = `${year}-${month}-${day}`
    
    // 验证格式化结果
    if (!/^\d{4}-\d{2}-\d{2}$/.test(formattedDate)) {
      console.error('formatDateForAPI: 格式化结果不符合预期', { input: date, output: formattedDate })
      return ''
    }
    
    console.log('格式化日期:', { input: date, output: formattedDate })
    return formattedDate
  } catch (error) {
    console.error('formatDateForAPI: 格式化日期失败', error)
    return ''
  }
}

// 搜索表单
const searchForm = reactive({
  venueName: '',
  userName: '',
  status: ''
})

// 预约列表
const reservations = ref([])
const total = ref(0)
const loading = ref(false)

// 场地列表和用户列表
const venueList = ref([])
const userList = ref([])

// 统计数据
const statsData = reactive({
  totalCount: 0,
  pendingCount: 0,
  approvedCount: 0,
  rejectedCount: 0
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const jumpPage = ref('') // 跳转页码

// 对话框状态
const showViewVenueDialog = ref(false)
const rejectDialogVisible = ref(false)
const showAddReservationDialog = ref(false)
const reviewLoading = ref(false)
const submitting = ref(false)
const currentVenue = ref({})
const currentRejectId = ref(null)
const rejectComment = ref('')

// 批量操作相关
const selectedReservations = ref([])
const selectAll = ref(false)
const showBatchApproveDialog = ref(false)
const batchApproveForm = reactive({
  status: '',
  remark: ''
})

// 预约表单
const reservationForm = reactive({
  venueId: '',
  userId: '',
  reservationDate: '',
  startTime: '',
  endTime: '',
  purpose: ''
})

// 表单错误
const formErrors = reactive({
  venueId: '',
  userId: '',
  reservationDate: '',
  startTime: '',
  endTime: ''
})

// 获取当前选中的场地
const selectedVenue = computed(() => {
  if (!reservationForm.venueId) return null
  return venueList.value.find(v => v.id === parseInt(reservationForm.venueId))
})

// 生成开始时间的可用时间段（30分钟间隔）
const availableStartTimeSlots = computed(() => {
  const venue = selectedVenue.value
  if (!venue || !venue.openTime || !venue.closeTime) {
    // 如果没有选中场地或场地没有营业时间，返回默认时间段（08:00-22:00）
    return generateTimeSlots('08:00', '22:00', true)
  }
  
  return generateTimeSlots(venue.openTime, venue.closeTime, true)
})

// 生成结束时间的可用时间段（30分钟间隔）
const availableEndTimeSlots = computed(() => {
  const venue = selectedVenue.value
  if (!venue || !venue.openTime || !venue.closeTime) {
    // 如果没有选中场地或场地没有营业时间，返回默认时间段（08:00-22:00）
    return generateTimeSlots('08:00', '22:00', false)
  }
  
  return generateTimeSlots(venue.openTime, venue.closeTime, false)
})

// 生成时间段函数
const generateTimeSlots = (startTime, endTime, isStartTime) => {
  const slots = []
  const [startHour, startMinute] = startTime.split(':').map(Number)
  const [endHour, endMinute] = endTime.split(':').map(Number)
  
  let currentMinutes = startHour * 60 + startMinute
  const endMinutes = endHour * 60 + endMinute
  
  while (currentMinutes <= endMinutes) {
    const hour = Math.floor(currentMinutes / 60)
    const minute = currentMinutes % 60
    const timeValue = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
    const timeLabel = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
    
    // 开始时间不包含结束时间，结束时间不包含开始时间
    if (!(isStartTime && currentMinutes >= endMinutes) && !(!isStartTime && currentMinutes <= startHour * 60 + startMinute)) {
      slots.push({
        value: timeValue,
        label: timeLabel
      })
    }
    
    currentMinutes += 30 // 30分钟间隔
  }
  
  return slots
}

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(total.value / pageSize.value)
})

// 更新统计数据
const fetchReservations = async () => {
  try {
    loading.value = true
    // 使用当前页码和页面大小，让后端处理分页和搜索
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      venueName: (searchForm.venueName || '').trim(),
      userName: (searchForm.userName || '').trim(),
      status: searchForm.status
    }
    
    console.log('发送搜索请求，参数:', params)
    const response = await reservationApi.getReservationPage(params)
    console.log('接收到响应:', response)
    
    // 处理分页响应
    if (response && response.data && response.data.records) {
      // 处理嵌套的data结构
      reservations.value = response.data.records.sort((a, b) => b.id - a.id)
      total.value = response.data.total || 0
      
      // 更新统计数据
      fetchStatistics()
    } else if (response && response.records) {
      // 直接使用后端返回的分页数据
      reservations.value = response.records.sort((a, b) => b.id - a.id)
      total.value = response.total || 0
      
      // 更新统计数据
      fetchStatistics()
    } else if (response && response.data && response.data.data && response.data.data.records) {
      // 处理更深层的嵌套结构
      reservations.value = response.data.data.records.sort((a, b) => b.id - a.id)
      total.value = response.data.data.total || 0
      
      // 更新统计数据
      fetchStatistics()
    } else {
      console.error('响应格式不正确:', response)
      ElMessage.error('获取预约列表失败')
    }
  } catch (error) {
    console.error('获取预约列表失败:', error)
    ElMessage.error('获取预约列表失败')
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStatistics = () => {
  statsData.totalCount = reservations.value.length
  statsData.pendingCount = reservations.value.filter(item => item.status === 0).length
  statsData.approvedCount = reservations.value.filter(item => item.status === 1).length
  statsData.rejectedCount = reservations.value.filter(item => item.status === 2).length
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    // 使用分页接口获取所有预约记录进行统计
    const response = await reservationApi.getReservationPage({
      pageNum: 1,
      pageSize: 1000 // 设置足够大的页面大小以获取所有记录
    })
    
    let allReservations = []
    
    // 处理不同的响应格式
    if (response && response.data && response.data.records) {
      allReservations = response.data.records
    } else if (response && response.records) {
      allReservations = response.records
    } else if (response && response.data && response.data.data && response.data.data.records) {
      allReservations = response.data.data.records
    }
    
    // 从所有记录中计算统计数据
    statsData.totalCount = allReservations.length
    statsData.pendingCount = allReservations.filter(item => item.status === 0).length
    statsData.approvedCount = allReservations.filter(item => item.status === 1).length
    statsData.rejectedCount = allReservations.filter(item => item.status === 2).length
    
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 如果API调用失败，回退到使用当前页数据
    updateStatistics()
  }
}

// 搜索预约
const searchReservations = () => {
  currentPage.value = 1
  fetchReservations()
}

// 重置搜索
const resetSearch = () => {
  searchForm.venueName = ''
  searchForm.userName = ''
  searchForm.status = ''
  currentPage.value = 1
  fetchReservations()
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
  // 重新从后端获取数据
  fetchReservations()
}

// 处理页面大小变化
const handlePageSizeChange = () => {
  currentPage.value = 1 // 重置到第一页
  // 重新从后端获取数据
  fetchReservations()
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

// 关闭查看场地对话框
const closeViewVenueDialog = () => {
  showViewVenueDialog.value = false
  currentVenue.value = {}
}

// 获取场地列表
const fetchVenueList = async () => {
  try {
    const response = await venueApi.getVenuePage({ pageNum: 1, pageSize: 1000 })
    venueList.value = response.records || response.data?.records || []
  } catch (error) {
    console.error('获取场地列表失败:', error)
    ElMessage.error('获取场地列表失败')
  }
}

// 获取用户列表
const fetchUserList = async () => {
  try {
    const response = await userApi.getUserPage({ pageNum: 1, pageSize: 1000 })
    userList.value = response.records || response.data?.records || []
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  }
}

// 查看预约
const viewReservation = async (id) => {
  try {
    loading.value = true
    const response = await reservationApi.getReservationById(id)
    const venue = response.data
    if (venue) {
      currentVenue.value = venue
      showViewVenueDialog.value = true
    } else {
      ElMessage.error('未找到预约信息')
    }
  } catch (error) {
    console.error('获取预约详情失败:', error)
    ElMessage.error('获取预约详情失败')
  } finally {
    loading.value = false
  }
}



// 删除预约
const deleteReservation = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    const success = await reservationApi.deleteReservation(id)
    
    if (success) {
      ElMessage.success('预约删除成功')
      // 刷新预约列表和统计数据
      await Promise.all([
        fetchReservations(),
        fetchStatistics()
      ])
    } else {
      ElMessage.error('预约删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除预约失败:', error)
      ElMessage.error('删除预约失败')
    }
  } finally {
    loading.value = false
  }
}

// 批准预约
const approveReservation = async (id) => {
  try {
    await ElMessageBox.confirm('确定要通过这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    reviewLoading.value = true
    const success = await reservationApi.reviewReservation(id, 1, '审核通过')
    
    if (success) {
      ElMessage.success('预约已通过')
      // 刷新预约列表和统计数据
      await Promise.all([
        fetchReservations(),
        fetchStatistics()
      ])
    } else {
      ElMessage.error('操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
      ElMessage.error('审核失败')
    }
  } finally {
    reviewLoading.value = false
  }
}

// 拒绝预约
const rejectReservation = (id) => {
  currentRejectId.value = id
  rejectComment.value = ''
  rejectDialogVisible.value = true
}

// 确认拒绝预约
const confirmRejectReservation = async () => {
  if (!rejectComment.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  try {
    reviewLoading.value = true
    const success = await reservationApi.reviewReservation(currentRejectId.value, 2, rejectComment.value)
    
    if (success) {
      ElMessage.success('预约已拒绝')
      rejectDialogVisible.value = false
      rejectComment.value = ''
      // 刷新预约列表和统计数据
      await Promise.all([
        fetchReservations(),
        fetchStatistics()
      ])
    } else {
      ElMessage.error('操作失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  } finally {
    reviewLoading.value = false
  }
}

// 取消拒绝预约
const cancelRejectReservation = () => {
  rejectDialogVisible.value = false
  rejectComment.value = ''
  currentRejectId.value = null
}

// 表单验证函数
const validateVenueId = () => {
  if (!reservationForm.venueId) {
    formErrors.venueId = '请选择场地'
    return false
  }
  formErrors.venueId = ''
  return true
}

const validateUserId = () => {
  if (!reservationForm.userId) {
    formErrors.userId = '请选择预约人'
    return false
  }
  formErrors.userId = ''
  return true
}

// 禁用今天之前的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 86400000
}

const validateReservationDate = () => {
  if (!reservationForm.reservationDate) {
    formErrors.reservationDate = '请选择预约日期'
    return false
  }
  
  // 检查日期是否是今天或之后
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const selectedDate = new Date(reservationForm.reservationDate)
  
  if (selectedDate < today) {
    formErrors.reservationDate = '预约日期不能早于今天'
    return false
  }
  
  formErrors.reservationDate = ''
  return true
}

const validateStartTime = () => {
  if (!reservationForm.startTime) {
    formErrors.startTime = '请选择开始时间'
    return false
  }
  formErrors.startTime = ''
  return true
}

const validateEndTime = () => {
  if (!reservationForm.endTime) {
    formErrors.endTime = '请选择结束时间'
    return false
  }
  
  // 检查结束时间是否晚于开始时间
  if (reservationForm.startTime && reservationForm.endTime) {
    const start = new Date(`2000-01-01T${reservationForm.startTime}`)
    const end = new Date(`2000-01-01T${reservationForm.endTime}`)
    
    if (end <= start) {
      formErrors.endTime = '结束时间必须晚于开始时间'
      return false
    }
  }
  
  formErrors.endTime = ''
  return true
}

// 验证整个表单
const validateForm = () => {
  const isVenueIdValid = validateVenueId()
  const isUserIdValid = validateUserId()
  const isReservationDateValid = validateReservationDate()
  const isStartTimeValid = validateStartTime()
  const isEndTimeValid = validateEndTime()
  
  return isVenueIdValid && isUserIdValid && isReservationDateValid && isStartTimeValid && isEndTimeValid
}

// 保存新预约
const saveNewReservation = async () => {
  // 表单验证
  if (!validateForm()) {
    return
  }
  
  submitting.value = true
  
  try {
    // 准备创建数据
    const createData = {
      venueId: reservationForm.venueId,
      userId: reservationForm.userId,
      reservationDate: formatDateForAPI(reservationForm.reservationDate),
      startTime: reservationForm.startTime,
      endTime: reservationForm.endTime,
      purpose: reservationForm.purpose || ''
    }
    
    const response = await reservationApi.createReservation(createData)
    
    if (response.code === 200 || response.success) {
      ElMessage.success('预约创建成功')
      closeAddReservationDialog()
      
      // 刷新预约列表和统计数据
      await Promise.all([
        fetchReservations(),
        fetchStatistics()
      ])
    } else {
      ElMessage.error(response.message || '创建失败')
    }
  } catch (error) {
    console.error('创建预约失败:', error)
    ElMessage.error('创建预约失败')
  } finally {
    submitting.value = false
  }
}

// 处理场地选择变化
const handleVenueChange = () => {
  validateVenueId()
  // 清空已选择的时间，因为不同场地的营业时间可能不同
  reservationForm.startTime = ''
  reservationForm.endTime = ''
}

// 关闭新增预约对话框
const closeAddReservationDialog = () => {
  showAddReservationDialog.value = false
  // 重置表单
  reservationForm.venueId = ''
  reservationForm.userId = ''
  reservationForm.reservationDate = ''
  reservationForm.startTime = ''
  reservationForm.endTime = ''
  reservationForm.purpose = ''
  
  // 重置错误信息
  Object.keys(formErrors).forEach(key => {
    formErrors[key] = ''
  })
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  // 如果是ISO格式的日期字符串（如2023-12-30），直接返回格式化为YYYY-MM-DD
  if (typeof date === 'string') {
    // 移除可能的时间部分
    const datePart = date.split('T')[0]
    return datePart
  }
  // 如果是Date对象
  if (date instanceof Date) {
    return date.toLocaleDateString('zh-CN')
  }
  // 其他情况
  return String(date)
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-pending'
    case 1: return 'status-approved'
    case 2: return 'status-rejected'
    default: return ''
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    default: return '未知状态'
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  // 如果是字符串格式的时间，直接返回
  if (typeof time === 'string') {
    return time
  }
  // 如果是Date对象，格式化为时间字符串
  if (time instanceof Date) {
    return time.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  // 如果是LocalTime类型的对象（从后端返回的JSON），直接返回字符串表示
  return String(time)
}

// 批量操作相关函数
const handleSelectAll = () => {
  if (selectAll.value) {
    selectedReservations.value = reservations.value.map(r => r.id)
  } else {
    selectedReservations.value = []
  }
}

const handleSelectionChange = () => {
  selectAll.value = selectedReservations.value.length === reservations.value.length && reservations.value.length > 0
}

const clearSelection = () => {
  selectedReservations.value = []
  selectAll.value = false
}

const batchApprove = () => {
  if (selectedReservations.value.length === 0) {
    ElMessage.warning('请先选择要审核的预约记录')
    return
  }
  batchApproveForm.status = '1'
  batchApproveForm.remark = ''
  showBatchApproveDialog.value = true
}

const batchReject = () => {
  if (selectedReservations.value.length === 0) {
    ElMessage.warning('请先选择要审核的预约记录')
    return
  }
  batchApproveForm.status = '2'
  batchApproveForm.remark = ''
  showBatchApproveDialog.value = true
}

const submitBatchApprove = async () => {
  try {
    reviewLoading.value = true
    for (const id of selectedReservations.value) {
      if (batchApproveForm.status === '1') {
        await venueApi.approveReservation(id)
      } else {
        await venueApi.rejectReservation(id, batchApproveForm.remark)
      }
    }
    ElMessage.success('批量审核成功')
    closeBatchApproveDialog()
    clearSelection()
    fetchReservations()
    fetchStatistics()
  } catch (error) {
    console.error('批量审核失败:', error)
    ElMessage.error('批量审核失败')
  } finally {
    reviewLoading.value = false
  }
}

const closeBatchApproveDialog = () => {
  showBatchApproveDialog.value = false
  batchApproveForm.status = ''
  batchApproveForm.remark = ''
}

const getReservationInfo = (id) => {
  const reservation = reservations.value.find(r => r.id === id)
  if (!reservation) return ''
  return `${reservation.venueName} - ${reservation.userName} (${reservation.reservationDate})`
}

// 初始化
onMounted(async () => {
  await Promise.all([
    fetchReservations(),
    fetchVenueList(),
    fetchUserList(),
    fetchStatistics()
  ])
})
</script>

<style scoped>
/* 使用与服务预约页面相同的样式 */
.status-available {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-unavailable {
  background-color: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffa39e;
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

.system-form-label .required {
  color: #f56c6c;
  margin-left: 2px;
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



/* 日期选择器样式 */
.venue-date-picker {
  width: 100% !important;
}

.venue-date-picker .el-input__wrapper {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  border-radius: 4px;
}

.venue-date-picker .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.venue-date-picker .el-input__wrapper.is-focus {
  box-shadow: 0 0 0 1px #409eff inset;
}

/* 时间选择器样式 */
.time-select {
  padding-right: 40px;
  cursor: pointer;
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23606266' d='M6 8L1 3h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: calc(100% - 32px) center;
  background-size: 12px 12px;
}

.time-select:focus {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23409eff' d='M6 8L1 3h10z'/%3E%3C/svg%3E");
}

.system-form-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  color: #303133;
  transition: all 0.3s ease;
  resize: vertical;
  min-height: 120px;
  line-height: 1.6;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.system-form-textarea:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.system-form-textarea::placeholder {
  color: #a8abb2;
  font-style: italic;
}

.system-form-textarea:hover {
  border-color: #c0c4cc;
}

.system-form-input-error {
  border-color: #f56c6c;
}

.system-form-error {
  font-size: 12px;
  color: #f56c6c;
  margin-top: 4px;
}

.system-form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  text-align: right;
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

.system-form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  text-align: right;
}

/* 状态标签 */
.status-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-pending {
  background-color: #fefce8;
  color: #e6a23c;
  border: 1px solid #fcd34d;
}

.status-approved {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-rejected {
  background-color: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffa39e;
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

/* 页面标题区域样式 */
.system-page-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.system-page-title-text {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.system-page-title-icon {
  font-size: 24px;
}

.system-page-title-actions {
  display: flex;
  gap: 12px;
}

.system-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  gap: 6px;
}

.system-button-primary {
  background-color: #409eff;
  color: white;
}

.system-button-primary:hover {
  background-color: #66b1ff;
}

.system-button-primary:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.system-button-icon {
  font-size: 16px;
  font-weight: bold;
}

/* 操作按钮组样式 */
.action-btn-group {
  display: flex;
  gap: 8px;
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

/* 场地预约详情卡片样式 */
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

.status-tag.status-pending {
  background-color: #fefce8;
  color: #e6a23c;
}

.status-tag.status-approved {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-tag.status-rejected {
  background-color: #fff2f0;
  color: #ff4d4f;
}

.system-modal-large {
  max-width: 800px;
  width: 90%;
}

/* 批量操作区域样式 */
.system-batch-actions {
  background-color: var(--system-bg-white);
  border: 1px solid var(--system-border-light);
  border-radius: var(--system-border-radius);
  padding: var(--system-space-md) var(--system-space-lg);
  margin-bottom: var(--system-space-md);
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--system-shadow-sm);
}

.system-batch-actions-info {
  display: flex;
  align-items: center;
}

.system-batch-actions-count {
  font-size: var(--system-font-size-base);
  color: var(--system-text-primary);
  font-weight: 500;
}

.system-batch-actions-buttons {
  display: flex;
  gap: var(--system-space-sm);
}

.selected-reservations-list {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid var(--system-border-light);
  border-radius: var(--system-border-radius);
  padding: var(--system-space-sm);
  background-color: var(--system-bg-gray);
}

.selected-reservation-item {
  padding: var(--system-space-xs) var(--system-space-sm);
  border-bottom: 1px solid var(--system-border-light);
  font-size: var(--system-font-size-sm);
  color: var(--system-text-primary);
}

.selected-reservation-item:last-child {
  border-bottom: none;
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
}
</style>