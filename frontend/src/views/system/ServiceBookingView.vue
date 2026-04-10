<template>
  <div class="system-page">
    <div class="system-page-title">
      <div class="system-page-title-text">
        <span class="system-page-title-icon">📅</span>
        服务预约管理
      </div>
      <div class="system-page-title-actions">
        <el-button type="primary" @click="showAddBookingDialog = true">
          <span class="system-button-icon">+</span>
          新增预约
        </el-button>
      </div>
    </div>

    <!-- 预约统计卡片 -->
    <div class="system-stats-container">
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">预约总数</div>
          <div class="system-stats-icon" style="background-color: rgba(24, 144, 255, 0.1); color: var(--system-primary-color);">
            <el-icon><Calendar /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.totalCount || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">待支付预约</div>
          <div class="system-stats-icon" style="background-color: rgba(245, 108, 108, 0.1); color: var(--system-error-color);">
            <el-icon><Warning /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.unpaidCount || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">待确认预约</div>
          <div class="system-stats-icon" style="background-color: rgba(250, 173, 20, 0.1); color: var(--system-warning-color);">
            <el-icon><Clock /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.pendingCount || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">已完成预约</div>
          <div class="system-stats-icon" style="background-color: rgba(82, 196, 26, 0.1); color: var(--system-success-color);">
            <el-icon><Check /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.completedCount || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">已取消预约</div>
          <div class="system-stats-icon" style="background-color: rgba(255, 77, 79, 0.1); color: var(--system-error-color);">
            <el-icon><Close /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.cancelledCount || 0 }}</div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="system-search-bar">
      <div class="system-search-form">
        <div class="system-search-item">
          <div class="system-form-label">服务名称</div>
          <select class="system-form-input" v-model="searchForm.serviceId">
            <option value="">全部服务</option>
            <option v-for="service in serviceItems" :key="service.id" :value="service.id">
              {{ service.name }}
            </option>
          </select>
        </div>
        <div class="system-search-item">
          <div class="system-form-label">预约人</div>
          <input class="system-form-input" type="text" v-model="searchForm.userName" placeholder="请输入预约人姓名">
        </div>
        <div class="system-search-item">
          <div class="system-form-label">预约状态</div>
          <select class="system-form-input" v-model="searchForm.status">
            <option value="">全部状态</option>
            <option value="0">待支付</option>
            <option value="1">待确认</option>
            <option value="2">已确认</option>
            <option value="3">服务中</option>
            <option value="4">已完成</option>
            <option value="5">已取消</option>
          </select>
        </div>
        <div class="system-search-actions">
          <el-button type="primary" @click="searchBookings">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 预约列表 -->
    <div class="system-table-container">
      <table class="system-table">
        <thead>
          <tr>
            <!-- 删除预约编号列 -->
            <th>服务名称</th>
            <th>预约用户</th>
            <th>预约日期</th>
            <th>预约时间</th>
            <th>服务价格</th>
            <th>服务人员</th>
            <th>预约状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="booking in bookings" :key="booking.id">
            <!-- 删除预约编号列 -->
            <td>{{ getServiceName(booking.serviceId) }}</td>
            <td>{{ getUserName(booking.userId) }}</td>
            <td>{{ formatDate(booking.reservationDate) }}</td>
            <td>{{ booking.reservationTime }}</td>
            <td>￥{{ getServicePrice(booking.serviceId) }}</td>
            <td>{{ booking.servicePerson || '未分配' }}</td>
            <td>
              <span v-if="booking.status === BOOKING_STATUS.UNPAID" class="status-tag status-unpaid">待支付</span>
              <span v-else-if="booking.status === BOOKING_STATUS.PENDING" class="status-tag status-pending">待确认</span>
              <span v-else-if="booking.status === BOOKING_STATUS.CONFIRMED" class="status-tag status-confirmed">已确认</span>
              <span v-else-if="booking.status === BOOKING_STATUS.IN_SERVICE" class="status-tag status-in-service">服务中</span>
              <span v-else-if="booking.status === BOOKING_STATUS.COMPLETED" class="status-tag status-completed">已完成</span>
              <span v-else-if="booking.status === BOOKING_STATUS.CANCELLED" class="status-tag status-cancelled">已取消</span>
            </td>
            <td>
              <div class="action-btn-group">
                <!-- 查看按钮始终显示 -->
                <el-button size="small" type="info" @click="viewBooking(booking)">查看</el-button>
                
                <!-- 待支付状态下的按钮 -->
                <template v-if="booking.status === BOOKING_STATUS.UNPAID">
                  <el-button size="small" type="danger" @click="cancelBooking(booking.id)">取消</el-button>
                </template>
                
                <!-- 待确认状态下的按钮 -->
                <template v-else-if="booking.status === BOOKING_STATUS.PENDING">
                  <el-button size="small" type="primary" @click="editBooking(booking.id)">编辑</el-button>
                  <el-button size="small" type="danger" @click="cancelBooking(booking.id)">取消</el-button>
                  <el-button size="small" type="success" @click="confirmBooking(booking.id)">确认</el-button>
                </template>
                
                <!-- 已确认状态下的按钮 -->
                <template v-else-if="booking.status === BOOKING_STATUS.CONFIRMED">
                  <el-button size="small" type="primary" @click="editBooking(booking.id)">编辑</el-button>
                  <el-button size="small" type="danger" @click="cancelBooking(booking.id)">取消</el-button>
                  <el-button size="small" type="warning" @click="startService(booking.id)">开始服务</el-button>
                </template>
                
                <!-- 服务中状态下的按钮 -->
                <template v-else-if="booking.status === BOOKING_STATUS.IN_SERVICE">
                  <el-button size="small" type="success" @click="completeService(booking.id)">完成服务</el-button>
                </template>
                
                <!-- 已完成状态下只显示查看按钮 -->
                
                <!-- 已取消状态下的按钮 -->
                <template v-else-if="booking.status === BOOKING_STATUS.CANCELLED">
                  <el-button size="small" type="danger" @click="deleteBooking(booking.id)">删除</el-button>
                </template>
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

    <!-- 添加预约对话框 -->
    <div v-if="showAddBookingDialog" class="system-modal-overlay" @click.self="closeBookingDialog">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">添加预约</div>
          <button class="system-modal-close" @click="closeBookingDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">选择服务</div>
              <select class="system-form-input" v-model="bookingForm.serviceId">
                <option value="">请选择服务</option>
                <option v-for="service in serviceItems" :key="service.id" :value="service.id">
                  {{ service.name }}
                </option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">选择用户</div>
              <select class="system-form-input" v-model="bookingForm.userId">
                <option value="">请选择用户</option>
                <option v-for="user in users" :key="user.id" :value="user.id">
                  {{ user.name }}
                </option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">预约日期</div>
              <el-date-picker
                v-model="bookingForm.reservationDate"
                type="date"
                placeholder="请选择预约日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </div>
            <div class="system-form-item">
              <div class="system-form-label">预约时间</div>
              <div class="booking-time-container">
                <div class="time-picker-row">
                  <div class="time-picker-item">
                    <div class="time-label">开始时间</div>
                    <el-select
                      v-model="bookingForm.startTime"
                      placeholder="请选择"
                      class="time-select"
                      @change="handleStartTimeChange"
                    >
                      <el-option
                        v-for="slot in startTimeSlots"
                        :key="slot.value"
                        :label="slot.label"
                        :value="slot.value"
                      />
                    </el-select>
                  </div>
                  <div class="time-separator">至</div>
                  <div class="time-picker-item">
                    <div class="time-label">结束时间</div>
                    <el-select
                      v-model="bookingForm.endTime"
                      placeholder="请选择"
                      class="time-select"
                      :disabled="!bookingForm.startTime"
                    >
                      <el-option
                        v-for="slot in endTimeSlots"
                        :key="slot.value"
                        :label="slot.label"
                        :value="slot.value"
                      />
                    </el-select>
                  </div>
                </div>
                <div v-if="bookingForm.startTime && bookingForm.endTime" class="time-duration-info">
                  <span class="duration-label">服务时长：</span>
                  <span class="duration-value">{{ getServiceDuration(bookingForm.serviceId) || 60 }}分钟</span>
                </div>
              </div>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">服务人员</div>
              <input 
                class="system-form-input" 
                type="text" 
                :value="userStore.userInfo?.name || userStore.userInfo?.username || ''"
                disabled
                style="background-color: #f5f5f5; cursor: not-allowed;"
              >
            </div>
            <div class="system-form-item">
              <div class="system-form-label">备注</div>
              <textarea class="system-form-input" rows="3" v-model="bookingForm.remark" placeholder="请输入备注信息"></textarea>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <el-button @click="closeBookingDialog">取消</el-button>
          <el-button type="primary" @click="saveBooking">保存</el-button>
        </div>
      </div>
    </div>

    <!-- 编辑预约对话框 -->
    <div v-if="showEditBookingDialog" class="system-modal-overlay" @click.self="closeBookingDialog">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">编辑预约</div>
          <button class="system-modal-close" @click="closeBookingDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">选择服务</div>
              <select class="system-form-input" v-model="bookingForm.serviceId">
                <option value="">请选择服务</option>
                <option v-for="service in serviceItems" :key="service.id" :value="service.id">
                  {{ service.name }}
                </option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">选择用户</div>
              <select class="system-form-input" v-model="bookingForm.userId">
                <option value="">请选择用户</option>
                <option v-for="user in users" :key="user.id" :value="user.id">
                  {{ user.name }}
                </option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">预约日期</div>
              <el-date-picker
                v-model="bookingForm.reservationDate"
                type="date"
                placeholder="请选择预约日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </div>
            <div class="system-form-item">
              <div class="system-form-label">预约时间</div>
              <div class="booking-time-container">
                <div class="time-picker-row">
                  <div class="time-picker-item">
                    <div class="time-label">开始时间</div>
                    <el-select
                      v-model="bookingForm.startTime"
                      placeholder="请选择"
                      class="time-select"
                      @change="handleStartTimeChange"
                    >
                      <el-option
                        v-for="slot in startTimeSlots"
                        :key="slot.value"
                        :label="slot.label"
                        :value="slot.value"
                      />
                    </el-select>
                  </div>
                  <div class="time-separator">至</div>
                  <div class="time-picker-item">
                    <div class="time-label">结束时间</div>
                    <el-select
                      v-model="bookingForm.endTime"
                      placeholder="请选择"
                      class="time-select"
                      :disabled="!bookingForm.startTime"
                    >
                      <el-option
                        v-for="slot in endTimeSlots"
                        :key="slot.value"
                        :label="slot.label"
                        :value="slot.value"
                      />
                    </el-select>
                  </div>
                </div>
                <div v-if="bookingForm.startTime && bookingForm.endTime" class="time-duration-info">
                  <span class="duration-label">服务时长：</span>
                  <span class="duration-value">{{ getServiceDuration(bookingForm.serviceId) || 60 }}分钟</span>
                </div>
              </div>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">服务人员</div>
              <input 
                class="system-form-input" 
                type="text" 
                :value="userStore.userInfo?.name || userStore.userInfo?.username || ''"
                disabled
                style="background-color: #f5f5f5; cursor: not-allowed;"
              >
            </div>
            <div class="system-form-item">
              <div class="system-form-label">预约状态</div>
              <select class="system-form-input" v-model="bookingForm.status" disabled style="background-color: #f5f5f5; cursor: not-allowed;">
                <option :value="BOOKING_STATUS.UNPAID">待支付</option>
                <option :value="BOOKING_STATUS.PENDING">待确认</option>
                <option :value="BOOKING_STATUS.CONFIRMED">已确认</option>
                <option :value="BOOKING_STATUS.IN_SERVICE">服务中</option>
                <option :value="BOOKING_STATUS.COMPLETED">已完成</option>
                <option :value="BOOKING_STATUS.CANCELLED">已取消</option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">备注</div>
              <textarea class="system-form-input" rows="3" v-model="bookingForm.remark" placeholder="请输入备注信息"></textarea>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <el-button @click="closeBookingDialog">取消</el-button>
          <el-button type="primary" @click="saveBooking">保存</el-button>
        </div>
      </div>
    </div>

    <!-- 查看预约对话框 -->
    <div v-if="showViewBookingDialog" class="system-modal-overlay" @click.self="closeViewBookingDialog">
      <div class="system-modal system-modal-large">
        <div class="system-modal-header">
          <div class="system-modal-title">预约详情</div>
          <button class="system-modal-close" @click="closeViewBookingDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="booking-detail-container">
            <!-- 预约基本信息卡片 -->
            <div class="detail-card">
              <div class="detail-card-header">
                <h2>{{ getServiceName(currentBooking.serviceId) }}</h2>
                <div class="status-tag" :class="getStatusClass(currentBooking.status)">
                  {{ getStatusText(currentBooking.status) }}
                </div>
              </div>
              
              <div class="detail-card-body">
                <div class="detail-row">
                  <div class="detail-label">预约用户：</div>
                  <div class="detail-value">{{ getUserName(currentBooking.userId) }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">预约日期：</div>
                  <div class="detail-value">{{ formatDate(currentBooking.reservationDate) }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">预约时间：</div>
                  <div class="detail-value">{{ currentBooking.reservationTime }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">服务价格：</div>
                  <div class="detail-value">￥{{ getServicePrice(currentBooking.serviceId) }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">服务人员：</div>
                  <div class="detail-value">{{ currentBooking.servicePerson || '未分配' }}</div>
                </div>
                
                <div v-if="currentBooking.remark" class="detail-row">
                  <div class="detail-label">备注：</div>
                  <div class="detail-value">{{ currentBooking.remark }}</div>
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
import { ref, reactive, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Clock, Check, Close } from '@element-plus/icons-vue'
import { servicesApi } from '@/api/services'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'
import eventBus from '@/utils/eventBus'

const userStore = useUserStore()
const router = useRouter()

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

// 预约状态常量定义
const BOOKING_STATUS = {
  UNPAID: 0,     // 待支付
  PENDING: 1,    // 待确认（已支付）
  CONFIRMED: 2,  // 已确认
  IN_SERVICE: 3, // 服务中
  COMPLETED: 4,  // 已完成
  CANCELLED: 5   // 已取消
};

// 搜索表单
const searchForm = reactive({
  serviceId: '',
  userName: '',
  status: ''
})

// 服务项目和用户列表
const serviceItems = ref([])
const users = ref([])
// 添加用户映射表，提高查找效率
const userMap = ref({})

// 工作人员列表（从用户中筛选）
const staffMembers = computed(() => {
  return users.value.filter(user => 
    user.role === 'STAFF' || user.role === 'staff'
  )
})

// 预约列表
const bookings = ref([])
const total = ref(0)
const loading = ref(false)
const allBookings = ref([])

// 统计数据
const statsData = reactive({
  totalCount: 0,
  unpaidCount: 0,
  pendingCount: 0,
  completedCount: 0,
  cancelledCount: 0,
  lastMonthTotalCount: 0,
  lastMonthUnpaidCount: 0,
  lastMonthPendingCount: 0,
  lastMonthCompletedCount: 0,
  lastMonthCancelledCount: 0
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const jumpPage = ref('')

// 对话框状态
const showAddBookingDialog = ref(false)
const showEditBookingDialog = ref(false)
const showViewBookingDialog = ref(false)

// 预约表单
const bookingForm = reactive({
  id: '',
  serviceId: '',
  userId: '',
  reservationDate: '',
  startTime: '',
  endTime: '',
  servicePerson: '',
  status: BOOKING_STATUS.UNPAID,
  remark: ''
})

// 当前查看的预约数据
const currentBooking = ref({})

// 根据服务时长生成开始时间选项（间隔为服务时长的一半）
const startTimeSlots = computed(() => {
  const selectedService = serviceItems.value.find(s => s.id === bookingForm.serviceId)
  const duration = selectedService?.duration || 60
  const interval = duration / 2
  const slots = []
  
  const morningStart = 8 * 60
  const morningEnd = 12 * 60
  const afternoonStart = 14 * 60
  const afternoonEnd = 18 * 60
  
  const generateSlots = (startMinutes, endMinutes) => {
    let currentMinutes = startMinutes
    while (currentMinutes < endMinutes) {
      const hour = Math.floor(currentMinutes / 60)
      const minute = currentMinutes % 60
      const time = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
      slots.push({
        value: time,
        label: time
      })
      
      currentMinutes += interval
    }
  }
  
  generateSlots(morningStart, morningEnd)
  generateSlots(afternoonStart, afternoonEnd)
  
  return slots
})

// 根据开始时间和服务时长生成结束时间选项
const endTimeSlots = computed(() => {
  if (!bookingForm.startTime) return []
  
  const selectedService = serviceItems.value.find(s => s.id === bookingForm.serviceId)
  const duration = selectedService?.duration || 60
  const slots = []
  
  const [startHour, startMinute] = bookingForm.startTime.split(':').map(Number)
  const startMinutes = startHour * 60 + startMinute
  
  const morningEnd = 12 * 60
  const afternoonEnd = 18 * 60
  
  const generateSlots = (endMinutes) => {
    const endHour = Math.floor(endMinutes / 60)
    const endMinute = endMinutes % 60
    const time = `${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}`
    slots.push({
      value: time,
      label: time
    })
  }
  
  const currentEndMinutes = startMinutes + duration
  
  if (currentEndMinutes <= morningEnd || currentEndMinutes <= afternoonEnd) {
    generateSlots(currentEndMinutes)
  }
  
  return slots
})

// 处理开始时间变化
const handleStartTimeChange = () => {
  bookingForm.endTime = ''
}

// 获取服务时长
const getServiceDuration = (serviceId) => {
  const service = serviceItems.value.find(s => s.id === serviceId)
  return service ? service.duration : 60
}

// 获取服务项目列表
const fetchServiceItems = async () => {
  try {
    const response = await servicesApi.getServiceItems({ page: 1, size: 100 })
    if (response.data && response.data.records) {
      serviceItems.value = response.data.records.filter(item => item.status === 1) // 只显示启用的服务
    }
  } catch (error) {
    console.error('获取服务项目失败:', error)
    ElMessage.error('获取服务项目失败')
  }
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    const params = {
      page: 1,
      size: 1000
    }
    
    const response = await userApi.getUserPage(params)
    const records = response.data?.records || response.records || response.data || []
    users.value = records
    
    userMap.value = {}
    records.forEach(user => {
      if (user && user.id !== undefined) {
        const userIdStr = String(user.id)
        userMap.value[userIdStr] = user.name || '未知用户'
      }
    })
    console.log('获取到的用户列表:', users.value, '共', records.length, '条记录')
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  }
}

// 获取服务名称
const getServiceName = (serviceId) => {
  const service = serviceItems.value.find(s => s.id === serviceId)
  return service ? service.name : '未知服务'
}

// 获取服务价格
const getServicePrice = (serviceId) => {
  const service = serviceItems.value.find(s => s.id === serviceId)
  return service ? parseFloat(service.price || 0).toFixed(2) : '0.00'
}

// 获取用户名称
const getUserName = (userId) => {
  // 处理空值情况
  if (!userId && userId !== 0) {
    return '未知用户'
  }
  
  // 使用字符串转换确保类型匹配
  const userIdStr = String(userId)
  
  // 优先使用映射表查找，提高性能
  if (userMap.value[userIdStr]) {
    return userMap.value[userIdStr]
  }
  
  // 如果映射表中没有找到，尝试从用户数组中查找（作为备用）
  const user = users.value.find(u => String(u.id) === userIdStr)
  return user ? user.name : '未知用户'
}

// 重置预约表单
const resetBookingForm = () => {
  bookingForm.id = ''
  bookingForm.serviceId = ''
  bookingForm.userId = ''
  bookingForm.reservationDate = ''
  bookingForm.startTime = ''
  bookingForm.endTime = ''
  bookingForm.servicePerson = ''
  bookingForm.status = BOOKING_STATUS.UNPAID
  bookingForm.remark = ''
}

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(total.value / pageSize.value)
})

// 监听pageSize变化，重置页码并重新获取数据
watch(pageSize, () => {
  currentPage.value = 1
  fetchBookings()
})

// 更新统计数据
const updateStatistics = (bookingsData) => {
  statsData.totalCount = bookingsData.length
  statsData.unpaidCount = bookingsData.filter(item => item.status === BOOKING_STATUS.UNPAID).length
  statsData.pendingCount = bookingsData.filter(item => item.status === BOOKING_STATUS.PENDING).length
  statsData.completedCount = bookingsData.filter(item => item.status === BOOKING_STATUS.COMPLETED).length
  statsData.cancelledCount = bookingsData.filter(item => item.status === BOOKING_STATUS.CANCELLED).length
}

// 获取预约统计数据
const fetchBookingStats = async () => {
  try {
    // 获取所有预约数据进行统计（不分页）
    const params = {
      page: 1,
      size: 10000, // 获取足够多的数据用于统计
      userId: null,
      serviceId: null,
      userName: null,
      status: null
    }
    
    const response = await servicesApi.getUserServiceBookings(params)
    const allRecords = response.data?.records || response.records || response.data || [];
    
    // 使用所有数据更新统计
    updateStatistics(allRecords)
  } catch (error) {
    console.error('获取预约统计数据失败:', error)
  }
}

// 获取预约列表
const fetchBookings = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      userId: null,
      serviceId: searchForm.serviceId || null,
      userName: searchForm.userName || null,
      status: searchForm.status !== '' ? parseInt(searchForm.status) : null
    }
    
    const response = await servicesApi.getUserServiceBookings(params)
    // 使用兼容的方式处理API响应
    const records = response.data?.records || response.records || response.data || [];
    
    if (records.length > 0 || response.data?.total === 0) {
      // 直接使用后端返回的数据，不再进行本地排序
      bookings.value = records
      total.value = response.data?.total || response.total || records.length
      
      // 保存当前页数据用于其他用途
      allBookings.value = records
    }
  } catch (error) {
    console.error('获取预约列表失败:', error)
    ElMessage.error('获取预约列表失败')
  } finally {
    loading.value = false
  }
}

// 应用分页和搜索过滤
const applyPagination = () => {
  // 由于现在使用后端分页和搜索，前端不再需要本地过滤
  // 直接调用fetchBookings获取数据
  fetchBookings()
}

// 搜索预约
const searchBookings = () => {
  currentPage.value = 1
  fetchBookings()
}

// 重置搜索
const resetSearch = () => {
  searchForm.serviceId = ''
  searchForm.userName = ''
  searchForm.status = ''
  currentPage.value = 1
  fetchBookings()
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
  fetchBookings()
}

// 处理页面大小变化
const handlePageSizeChange = () => {
  currentPage.value = 1 // 重置到第一页
  fetchBookings()
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

// 保留原有的分页函数以确保兼容性
const handlePreviousPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    fetchBookings()
  }
}

const handleNextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    fetchBookings()
  }
}

const handlePageClick = (page) => {
  currentPage.value = page
  fetchBookings()
}

// 关闭预约对话框
const closeBookingDialog = () => {
  showAddBookingDialog.value = false
  showEditBookingDialog.value = false
  resetBookingForm()
}

// 查看预约详情
const viewBooking = (booking) => {
  currentBooking.value = booking
  showViewBookingDialog.value = true
}

// 关闭查看预约对话框
const closeViewBookingDialog = () => {
  showViewBookingDialog.value = false
  currentBooking.value = {}
}

// 保存预约
const saveBooking = async () => {
  if (!bookingForm.serviceId || !bookingForm.userId || !bookingForm.reservationDate || !bookingForm.startTime || !bookingForm.endTime) {
    ElMessage.error('请填写完整的预约信息')
    return
  }
  
  try {
    loading.value = true
    
    // 设置服务人员为当前登录用户
    const currentUser = userStore.userInfo
    bookingForm.servicePerson = currentUser?.name || currentUser?.username || ''
    
    // 将开始和结束时间组合为预约时间
    bookingForm.reservationTime = `${bookingForm.startTime}-${bookingForm.endTime}`
    
    let success = false
    
    if (bookingForm.id) {
      // 编辑预约
      const response = await servicesApi.updateServiceBooking(bookingForm.id, bookingForm)
      success = response
    } else {
      // 新增预约
      const response = await servicesApi.bookService(bookingForm)
      success = response
    }
    
    if (success) {
      ElMessage.success(bookingForm.id ? '预约更新成功' : '预约创建成功')
      closeBookingDialog()
      await fetchBookings()
      await fetchBookingStats()
      eventBus.emit('bookingDataChanged')
    } else {
      ElMessage.error(bookingForm.id ? '预约更新失败' : '预约创建失败')
    }
  } catch (error) {
    console.error('保存预约失败:', error)
    ElMessage.error('保存预约失败')
  } finally {
    loading.value = false
  }
}

// 编辑预约
const editBooking = async (id) => {
  try {
    loading.value = true
    // 从本地数据中查找
    const booking = allBookings.value.find(item => item.id === id)
    if (booking) {
      // 创建副本避免直接修改原数据
      bookingForm.id = booking.id
      bookingForm.serviceId = booking.serviceId
      bookingForm.userId = booking.userId
      bookingForm.reservationDate = booking.reservationDate
      
      // 解析预约时间（格式为"开始时间-结束时间"）
      if (booking.reservationTime && booking.reservationTime.includes('-')) {
        const [start, end] = booking.reservationTime.split('-')
        bookingForm.startTime = start
        bookingForm.endTime = end
      } else {
        bookingForm.startTime = ''
        bookingForm.endTime = ''
      }
      
      // 设置服务人员为当前登录用户
      const currentUser = userStore.userInfo
      bookingForm.servicePerson = currentUser?.name || currentUser?.username || ''
      bookingForm.status = booking.status
      bookingForm.remark = booking.remark
      
      showEditBookingDialog.value = true
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

// 取消预约
const cancelBooking = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    
    const success = await servicesApi.cancelServiceBooking(id)
    
    if (success) {
      ElMessage.success('预约取消成功')
      await fetchBookings()
      await fetchBookingStats()
      eventBus.emit('bookingDataChanged')
    } else {
      ElMessage.error('预约取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预约失败:', error)
      ElMessage.error('取消预约失败')
    }
  } finally {
    loading.value = false
  }
}

// 删除预约（仅已取消状态）
const deleteBooking = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个预约吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error',
      confirmButtonClass: 'el-button--danger'
    })
    
    loading.value = true
    const success = await servicesApi.deleteServiceBooking(id)
    
    if (success) {
      ElMessage.success('预约删除成功')
      await fetchBookings()
      await fetchBookingStats()
      eventBus.emit('bookingDataChanged')
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

// 确认预约（待确认 -> 已确认）
const confirmBooking = async (id) => {
  try {
    await ElMessageBox.confirm('确定要确认这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    loading.value = true
    const success = await servicesApi.updateBookingStatus(id, BOOKING_STATUS.CONFIRMED)
    
    if (success) {
      ElMessage.success('预约确认成功')
      await fetchBookings()
      await fetchBookingStats()
      eventBus.emit('bookingDataChanged')
    } else {
      ElMessage.error('预约确认失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认预约失败:', error)
      ElMessage.error('确认预约失败')
    }
  } finally {
    loading.value = false
  }
}

// 开始服务（已确认 -> 服务中）
const startService = async (id) => {
  try {
    await ElMessageBox.confirm('确定要开始这个服务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    loading.value = true
    const success = await servicesApi.updateBookingStatus(id, BOOKING_STATUS.IN_SERVICE)
    
    if (success) {
      ElMessage.success('服务开始成功')
      await fetchBookings()
      await fetchBookingStats()
      eventBus.emit('bookingDataChanged')
    } else {
      ElMessage.error('服务开始失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('开始服务失败:', error)
      ElMessage.error('开始服务失败')
    }
  } finally {
    loading.value = false
  }
}

// 完成服务（服务中 -> 已完成）
const completeService = async (id) => {
  try {
    await ElMessageBox.confirm('确定要完成这个服务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    loading.value = true
    const success = await servicesApi.updateBookingStatus(id, BOOKING_STATUS.COMPLETED)
    
    if (success) {
      ElMessage.success('服务完成成功')
      await fetchBookings()
      await fetchBookingStats()
      eventBus.emit('bookingDataChanged')
    } else {
      ElMessage.error('服务完成失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成服务失败:', error)
      ElMessage.error('完成服务失败')
    }
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case BOOKING_STATUS.UNPAID: return 'status-unpaid'
    case BOOKING_STATUS.PENDING: return 'status-pending'
    case BOOKING_STATUS.CONFIRMED: return 'status-confirmed'
    case BOOKING_STATUS.IN_SERVICE: return 'status-in-service'
    case BOOKING_STATUS.COMPLETED: return 'status-completed'
    case BOOKING_STATUS.CANCELLED: return 'status-cancelled'
    default: return ''
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case BOOKING_STATUS.UNPAID: return '待支付'
    case BOOKING_STATUS.PENDING: return '待确认'
    case BOOKING_STATUS.CONFIRMED: return '已确认'
    case BOOKING_STATUS.IN_SERVICE: return '服务中'
    case BOOKING_STATUS.COMPLETED: return '已完成'
    case BOOKING_STATUS.CANCELLED: return '已取消'
    default: return '未知状态'
  }
}

const handlePaymentDataChanged = async () => {
  console.log('收到支付数据变更事件，刷新预约数据')
  await fetchBookings()
  await fetchBookingStats()
}

// 初始化
onMounted(async () => {
  await fetchServiceItems()
  await fetchUsers()
  await fetchBookings()
  await fetchBookingStats()
  
  eventBus.on('paymentDataChanged', handlePaymentDataChanged)
})

onUnmounted(() => {
  eventBus.off('paymentDataChanged', handlePaymentDataChanged)
})
</script>

<style scoped>
/* 使用与服务管理页面相同的样式 */
.status-unpaid {
  background-color: #fff1f0;
  color: #f5222d;
  border: 1px solid #ffa39e;
}

.status-pending {
  background-color: #f0f9ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.status-confirmed {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-in-service {
  background-color: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

.status-completed {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-cancelled {
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

/* 按钮样式 - 与活动页面保持一致 */
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

.btn-info {
  background-color: #17a2b8;
  color: white;
  border-color: #17a2b8;
}

.btn-info:hover {
  background-color: #138496;
  border-color: #138496;
}

.btn-info-custom {
  background-color: #17a2b8 !important;
  border-color: #17a2b8 !important;
  color: white !important;
}

.btn-info-custom:hover {
  background-color: #138496 !important;
  border-color: #138496 !important;
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

.system-pagination-info {
  color: var(--system-text-secondary);
  font-size: var(--system-font-size-sm);
  display: flex;
  align-items: center;
  gap: var(--system-space-md);
}

.system-pagination-size-selector {
  display: flex;
  align-items: center;
  gap: var(--system-space-xs);
  font-size: var(--system-font-size-sm);
}

.system-pagination-size-selector select {
  padding: var(--system-space-xs) var(--system-space-sm);
  border: 1px solid var(--system-border-color);
  border-radius: var(--system-border-radius-md);
  font-size: var(--system-font-size-sm);
  min-width: 80px;
}

.system-pagination-controls {
  display: flex;
  align-items: center;
  gap: var(--system-space-xs);
}

.system-pagination-jump {
  display: flex;
  align-items: center;
  gap: var(--system-space-xs);
  margin-left: var(--system-space-md);
  font-size: var(--system-font-size-sm);
}

.system-pagination-jump input {
  width: 50px;
  padding: var(--system-space-xs);
  border: 1px solid var(--system-border-color);
  border-radius: var(--system-border-radius-md);
  text-align: center;
  font-size: var(--system-font-size-sm);
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
  padding: 0 var(--system-space-sm);
  color: var(--system-text-tertiary);
  font-size: var(--system-font-size-sm);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
}

/* 分页器加载状态 */
.system-pagination-loading {
  position: relative;
  pointer-events: none;
}

.system-pagination-loading::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 16px;
  height: 16px;
  margin: -8px 0 0 -8px;
  border: 2px solid var(--system-border-color);
  border-top-color: var(--system-primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
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
    flex-wrap: wrap;
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
  
  .system-pagination-btn {
    min-width: 36px;
    height: 36px;
    font-size: var(--system-font-size-md);
  }
}

/* 预约时间选择器样式 */
.booking-time-container {
  width: 100%;
}

.time-picker-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.time-picker-item {
  flex: 1;
}

.time-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.time-select {
  width: 100%;
}

.time-select :deep(.el-input__wrapper) {
  padding: 6px 12px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.time-select :deep(.el-input__wrapper:hover) {
  border-color: #c0c4cc;
}

.time-select :deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
}

.time-separator {
  font-size: 14px;
  color: #909399;
  padding-top: 20px;
}

.time-duration-info {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
}

.duration-label {
  color: #909399;
}

.duration-value {
  color: #409eff;
  font-weight: 500;
}

/* Element Plus 日期选择器样式 */
.system-form-item :deep(.el-date-editor) {
  width: 100%;
}

.system-form-item :deep(.el-input__wrapper) {
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.system-form-item :deep(.el-input__wrapper:hover) {
  border-color: #c0c4cc;
}

.system-form-item :deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
}

/* 服务预约详情卡片样式 */
.booking-detail-container {
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

.status-tag.status-unpaid {
  background-color: #fff1f0;
  color: #f5222d;
}

.status-tag.status-pending {
  background-color: #f0f9ff;
  color: #1890ff;
}

.status-tag.status-confirmed {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-tag.status-in-service {
  background-color: #fff7e6;
  color: #fa8c16;
}

.status-tag.status-completed {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-tag.status-cancelled {
  background-color: #fff2f0;
  color: #ff4d4f;
}

.system-modal-large {
  max-width: 800px;
  width: 90%;
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