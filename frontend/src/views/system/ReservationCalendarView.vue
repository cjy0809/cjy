<template>
  <div class="system-reservation-calendar">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>预约日历</h2>
      <el-button-group>
        <el-button @click="handleRefresh">
          <el-icon><RefreshRight /></el-icon>
          刷新数据
        </el-button>
      </el-button-group>
    </div>

    <!-- 筛选区域 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :model="filterForm" label-width="60px" class="search-form">
        <el-form-item label="场地">
          <el-select
            v-model="filterForm.venueId"
            placeholder="请选择场地"
            clearable
            style="width: 180px"
            @change="handleVenueChange"
          >
            <el-option
              v-for="venue in venueList"
              :key="venue.id"
              :label="venue.name"
              :value="venue.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="月份">
          <el-date-picker
            v-model="filterForm.month"
            type="month"
            placeholder="选择月份"
            format="YYYY年MM月"
            value-format="YYYY-MM"
            style="width: 180px"
            @change="handleMonthChange"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select
            v-model="filterForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 180px"
            @change="handleStatusChange"
          >
            <el-option label="待审核" value="0" />
            <el-option label="已通过" value="1" />
            <el-option label="已拒绝" value="2" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日历区域 -->
    <el-card shadow="hover" class="calendar-card">
      <div class="calendar-container">
        <!-- 日历头部 -->
        <div class="calendar-header">
          <div class="calendar-nav">
            <el-button type="text" @click="prevMonth">
              <el-icon><ArrowLeft /></el-icon>
              上月
            </el-button>
            <h3 class="current-month">{{ currentYearMonth }}</h3>
            <el-button type="text" @click="nextMonth">
              下月
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div class="calendar-legend">
            <div class="legend-item">
              <span class="legend-color" style="background-color: #F56C6C;"></span>
              <span class="legend-text">待审核</span>
            </div>
            <div class="legend-item">
              <span class="legend-color" style="background-color: #67C23A;"></span>
              <span class="legend-text">已通过</span>
            </div>
            <div class="legend-item">
              <span class="legend-color" style="background-color: #E6A23C;"></span>
              <span class="legend-text">已拒绝</span>
            </div>
          </div>
        </div>

        <!-- 日历主体 -->
        <div class="calendar-body">
          <!-- 星期标题 -->
          <div class="calendar-weekdays">
            <div class="weekday" v-for="day in weekdays" :key="day">{{ day }}</div>
          </div>

          <!-- 日历网格 -->
          <div class="calendar-grid">
            <!-- 上月日期占位 -->
            <div
              v-for="i in prevMonthDays"
              :key="`prev-${i}`"
              class="calendar-day prev-month"
            ></div>

            <!-- 当月日期 -->
            <div
              v-for="day in currentMonthDays"
              :key="`current-${day}`"
              class="calendar-day current-month"
              :class="{ 'today': isToday(day), 'has-reservations': hasReservations(day) }"
              @click="handleDayClick(day)"
            >
              <div class="day-number">{{ day }}</div>
              <div class="day-reservations">
                <div
                  v-for="reservation in getDayReservations(day)"
                  :key="reservation.id"
                  class="reservation-item"
                  :class="getStatusClass(reservation.status)"
                  :title="getReservationTitle(reservation)"
                  @click.stop="handleReservationClick(reservation)"
                >
                  {{ formatTime(reservation.startTime) }}-{{ formatTime(reservation.endTime) }}
                </div>
              </div>
            </div>

            <!-- 下月日期占位 -->
            <div
              v-for="i in nextMonthDays"
              :key="`next-${i}`"
              class="calendar-day next-month"
            ></div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 当日预约详情 -->
    <el-card shadow="hover" class="detail-card" v-if="selectedDate">
      <template #header>
        <div class="card-header">
          <span>{{ selectedDate }} 预约详情</span>
          <el-button size="small" @click="closeDetail">
            <el-icon><Close /></el-icon>
            关闭
          </el-button>
        </div>
      </template>
      
      <div v-if="selectedDayReservations.length > 0" class="detail-content">
        <el-table :data="selectedDayReservations" style="width: 100%" border stripe>
          <el-table-column prop="venueName" label="场地" width="150" />
          <el-table-column prop="userName" label="预约人" width="120" />
          <el-table-column label="预约时间" width="180">
            <template #default="scope">
              {{ formatTime(scope.row.startTime) }}-{{ formatTime(scope.row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="purpose" label="预约用途" min-width="200" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag
                :type="scope.row.status === 0 ? 'danger' : scope.row.status === 1 ? 'success' : 'warning'"
              >
                {{ scope.row.status === 0 ? '待审核' : scope.row.status === 1 ? '已通过' : '已拒绝' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                @click="handleViewReservation(scope.row)"
              >
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button
                v-if="scope.row.status === 0"
                type="success"
                size="small"
                @click="handleApproveReservation(scope.row)"
                :loading="reviewLoading"
              >
                <el-icon><Check /></el-icon>
                通过
              </el-button>
              <el-button
                v-if="scope.row.status === 0"
                type="danger"
                size="small"
                @click="handleRejectReservation(scope.row)"
                :loading="reviewLoading"
              >
                <el-icon><Close /></el-icon>
                拒绝
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else class="empty-detail">
        <el-empty description="当日无预约" />
      </div>
    </el-card>

    <!-- 预约详情对话框 -->
    <el-dialog
      v-model="reservationDialogVisible"
      title="预约详情"
      width="60%"
      destroy-on-close
    >
      <div v-if="currentReservation" class="reservation-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="预约编号">{{ currentReservation.id }}</el-descriptions-item>
          <el-descriptions-item label="预约日期">{{ currentReservation.reservationDate }}</el-descriptions-item>
          <el-descriptions-item label="场地">{{ currentReservation.venueName }}</el-descriptions-item>
          <el-descriptions-item label="预约人">{{ currentReservation.userName }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatTime(currentReservation.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatTime(currentReservation.endTime) }}</el-descriptions-item>
          <el-descriptions-item label="预约用途" :span="2">{{ currentReservation.purpose }}</el-descriptions-item>
          <el-descriptions-item label="预约状态">
            <el-tag
              :type="currentReservation.status === 0 ? 'danger' : currentReservation.status === 1 ? 'success' : 'warning'"
            >
              {{ currentReservation.status === 0 ? '待审核' : currentReservation.status === 1 ? '已通过' : '已拒绝' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentReservation.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="审核人" v-if="currentReservation.reviewerId">{{ currentReservation.reviewerName }}</el-descriptions-item>
          <el-descriptions-item label="审核时间" v-if="currentReservation.reviewTime">{{ formatDateTime(currentReservation.reviewTime) }}</el-descriptions-item>
          <el-descriptions-item label="审核意见" :span="2" v-if="currentReservation.reviewComment">{{ currentReservation.reviewComment }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="reservationDialogVisible = false">关闭</el-button>
          <el-button
            v-if="currentReservation && currentReservation.status === 0"
            type="success"
            @click="handleApproveReservation(currentReservation)"
            :loading="reviewLoading"
          >
            <el-icon><Check /></el-icon>
            审核通过
          </el-button>
          <el-button
            v-if="currentReservation && currentReservation.status === 0"
            type="danger"
            @click="handleRejectReservation(currentReservation)"
            :loading="reviewLoading"
          >
            <el-icon><Close /></el-icon>
            拒绝
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 拒绝意见对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝预约"
      width="40%"
      destroy-on-close
    >
      <div class="reject-dialog-content">
        <p>请输入拒绝原因：</p>
        <el-input
          v-model="rejectComment"
          type="textarea"
          placeholder="请输入拒绝原因"
          rows="4"
          resize="vertical"
          maxlength="200"
          show-word-limit
        ></el-input>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelRejectReservation">取消</el-button>
          <el-button
            type="danger"
            @click="confirmRejectReservation"
            :loading="reviewLoading"
          >
            确认拒绝
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { RefreshRight, ArrowLeft, ArrowRight, Close, View, Check } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { venueApi } from '@/api/venue'
import { reservationApi } from '@/api/reservation'

// 状态管理
const loading = ref(false)
const reviewLoading = ref(false)
const venueList = ref([])
const reservationList = ref([])
const selectedDate = ref('')
const selectedDayReservations = ref([])
const reservationDialogVisible = ref(false)
const currentReservation = ref(null)
const rejectDialogVisible = ref(false)
const rejectComment = ref('')

// 筛选表单
const filterForm = ref({
  venueId: '',
  month: dayjs().format('YYYY-MM'),
  status: ''
})

// 日历配置
const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const currentDate = ref(new Date())

// 计算属性：当前年月
const currentYearMonth = computed(() => {
  return dayjs(currentDate.value).format('YYYY年MM月')
})

// 计算属性：当前月天数
const currentMonthDays = computed(() => {
  const date = dayjs(currentDate.value)
  return date.isValid() ? date.daysInMonth() : 0
})

// 计算属性：当前月第一天是星期几
const firstDayOfMonth = computed(() => {
  const date = dayjs(currentDate.value)
  return date.isValid() ? date.startOf('month').day() : 0
})

// 计算属性：上月天数占位
const prevMonthDays = computed(() => {
  // 确保返回的是有效的数字，防止 v-for 指令错误
  const days = firstDayOfMonth.value
  return isNaN(days) ? 0 : Math.max(0, days)
})

// 计算属性：下月天数占位
const nextMonthDays = computed(() => {
  // 确保返回的是有效的数字，防止 v-for 指令错误
  const currentDays = currentMonthDays.value
  const prevDays = prevMonthDays.value
  if (isNaN(currentDays) || isNaN(prevDays)) {
    return 0
  }
  
  const totalDays = currentDays + prevDays
  const remainder = totalDays % 7
  return remainder === 0 ? 0 : 7 - remainder
})

// 初始化数据
const initData = async () => {
  loading.value = true
  try {
    // 加载场地列表
    const venueResponse = await venueApi.getVenuePage({ pageNum: 1, pageSize: 100 })
    venueList.value = venueResponse.records || venueResponse.data?.records || []
    
    // 加载预约数据
    await loadReservations()
  } catch (error) {
    ElMessage.error('初始化数据失败')
    console.error('初始化数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载预约数据
const loadReservations = async () => {
  loading.value = true
  try {
    // 构建参数对象，确保 month 参数有效
    const params = {}
    
    // 检查月份是否有效，如果无效则使用当前日期对应的月份
    let month = filterForm.value.month
    if (!month) {
      // 如果月份为空，使用当前日期对应的月份
      month = dayjs().format('YYYY-MM')
      filterForm.value.month = month
    } else {
      // 如果月份有效，使用指定的月份
      const date = dayjs(month)
      if (!date.isValid()) {
        // 如果月份无效，使用当前日期对应的月份
        month = dayjs().format('YYYY-MM')
        filterForm.value.month = month
      }
    }
    
    // 添加有效的 month 参数
    params.month = month
    
    // 统一条件判断逻辑，只有当值存在且不是空字符串时，才添加到参数中
    if (filterForm.value.venueId !== '' && filterForm.value.venueId !== undefined) {
      params.venueId = Number(filterForm.value.venueId)
    }
    
    // 统一条件判断逻辑，只有当值存在且不是空字符串时，才添加到参数中
    if (filterForm.value.status !== '' && filterForm.value.status !== undefined) {
      params.status = Number(filterForm.value.status)
    }
    
    const response = await reservationApi.getCalendarReservations(params)
    reservationList.value = response.data || response || []
  } catch (error) {
    ElMessage.error('加载预约数据失败')
    console.error('加载预约数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 切换到上月
const prevMonth = () => {
  currentDate.value = dayjs(currentDate.value).subtract(1, 'month').toDate()
  filterForm.value.month = dayjs(currentDate.value).format('YYYY-MM')
  loadReservations()
}

// 切换到下月
const nextMonth = () => {
  currentDate.value = dayjs(currentDate.value).add(1, 'month').toDate()
  filterForm.value.month = dayjs(currentDate.value).format('YYYY-MM')
  loadReservations()
}

// 判断是否为今天
const isToday = (day) => {
  return dayjs().format('YYYY-MM-DD') === dayjs(currentDate.value).date(day).format('YYYY-MM-DD')
}

// 判断某天是否有预约
const hasReservations = (day) => {
  const date = dayjs(currentDate.value).date(day).format('YYYY-MM-DD')
  return reservationList.value.some(reservation => reservation.reservationDate === date)
}

// 获取某天的预约列表
const getDayReservations = (day) => {
  const date = dayjs(currentDate.value).date(day).format('YYYY-MM-DD')
  return reservationList.value.filter(reservation => reservation.reservationDate === date)
}

// 点击日期
const handleDayClick = (day) => {
  selectedDate.value = dayjs(currentDate.value).date(day).format('YYYY-MM-DD')
  selectedDayReservations.value = getDayReservations(day)
}

// 关闭详情
const closeDetail = () => {
  selectedDate.value = ''
  selectedDayReservations.value = []
}

// 点击预约
const handleReservationClick = (reservation) => {
  currentReservation.value = reservation
  reservationDialogVisible.value = true
}

// 查看预约详情
const handleViewReservation = (reservation) => {
  currentReservation.value = reservation
  reservationDialogVisible.value = true
}

// 审核通过预约
const handleApproveReservation = async (reservation) => {
  reviewLoading.value = true
  try {
    await reservationApi.reviewReservation(reservation.id, 1, '')
    ElMessage.success('预约审核通过')
    loadReservations()
    reservationDialogVisible.value = false
  } catch (error) {
    ElMessage.error('审核失败：' + (error.message || '操作异常'))
    console.error('审核通过失败:', error)
  } finally {
    reviewLoading.value = false
  }
}

// 拒绝预约
const handleRejectReservation = (reservation) => {
  currentReservation.value = reservation
  rejectComment.value = ''
  rejectDialogVisible.value = true
}

// 确认拒绝预约
const confirmRejectReservation = async () => {
  if (!rejectComment.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  reviewLoading.value = true
  try {
    await reservationApi.reviewReservation(currentReservation.value.id, 2, rejectComment.value)
    ElMessage.success('预约已拒绝')
    loadReservations()
    rejectDialogVisible.value = false
    reservationDialogVisible.value = false
  } catch (error) {
    ElMessage.error('拒绝失败：' + (error.message || '操作异常'))
    console.error('拒绝预约失败:', error)
  } finally {
    reviewLoading.value = false
  }
}

// 取消拒绝操作
const cancelRejectReservation = () => {
  rejectDialogVisible.value = false
  rejectComment.value = ''
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

// 获取预约标题
const getReservationTitle = (reservation) => {
  return `${reservation.userName} - ${reservation.purpose}`
}

// 格式化时间
const formatTime = (time) => {
  return time.substring(0, 5)
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 刷新数据
const handleRefresh = () => {
  loadReservations()
  ElMessage.success('数据刷新成功')
}


// 筛选相关方法
const handleVenueChange = () => {
  loadReservations()
}

const handleMonthChange = () => {
  // 检查月份是否有效，如果无效则使用当前日期
  if (!filterForm.value.month) {
    // 如果月份为空，使用当前日期
    currentDate.value = new Date()
    filterForm.value.month = dayjs().format('YYYY-MM')
  } else {
    // 如果月份有效，使用指定的月份
    const date = dayjs(filterForm.value.month)
    if (date.isValid()) {
      currentDate.value = date.toDate()
    } else {
      // 如果月份无效，使用当前日期
      currentDate.value = new Date()
      filterForm.value.month = dayjs().format('YYYY-MM')
    }
  }
  
  // 重新加载预约数据
  loadReservations()
}

const handleStatusChange = () => {
  loadReservations()
}

const handleSearch = () => {
  loadReservations()
}

const handleReset = () => {
  // 重置当前日期为今天
  currentDate.value = new Date()
  
  // 重置筛选条件，与初始状态保持一致
  filterForm.value = {
    venueId: '',
    month: dayjs().format('YYYY-MM'),
    status: ''
  }
  
  // 重新加载预约数据
  loadReservations()
}

// 初始加载
onMounted(() => {
  initData()
})
</script>

<style scoped>
.system-reservation-calendar {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.filter-card {
  margin-bottom: 20px;
}

/* 搜索表单样式优化 */
.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  padding: 10px 0;
}

.search-form .el-form-item {
  margin-bottom: 0;
  margin-right: 0;
}

/* 按钮样式优化 */
.search-form .el-button {
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.search-form .el-button--primary {
  background-color: #409EFF;
  border-color: #409EFF;
}

.search-form .el-button--primary:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.search-form .el-button--default {
  border-color: #dcdfe6;
}

.search-form .el-button--default:hover {
  border-color: #c6e2ff;
  color: #409EFF;
  background-color: #ecf5ff;
}

.calendar-card {
  margin-bottom: 20px;
}

.calendar-container {
  width: 100%;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.calendar-nav {
  display: flex;
  align-items: center;
  gap: 20px;
}

.current-month {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.calendar-legend {
  display: flex;
  gap: 20px;
  align-items: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.calendar-body {
  width: 100%;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background-color: #e0e0e0;
  margin-bottom: 1px;
}

.weekday {
  padding: 15px;
  background-color: #f5f7fa;
  text-align: center;
  font-weight: 600;
  color: #606266;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background-color: #e0e0e0;
}

.calendar-day {
  background-color: #fff;
  min-height: 120px;
  padding: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.calendar-day:hover {
  background-color: #f5f7fa;
}

.calendar-day.prev-month,
.calendar-day.next-month {
  background-color: #fafafa;
  color: #c0c4cc;
  cursor: default;
}

.calendar-day.current-month.has-reservations {
  border: 1px solid #409EFF;
}

.calendar-day.today {
  background-color: #ecf5ff;
  border: 1px solid #409EFF;
}

.day-number {
  font-weight: 600;
  margin-bottom: 5px;
  text-align: right;
}

.day-reservations {
  display: flex;
  flex-direction: column;
  gap: 3px;
  max-height: 100px;
  overflow-y: auto;
}

.reservation-item {
  padding: 3px 6px;
  border-radius: 3px;
  font-size: 12px;
  color: #fff;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: all 0.3s ease;
}

.reservation-item:hover {
  opacity: 0.8;
  transform: scale(1.05);
}

.status-pending {
  background-color: #F56C6C;
}

.status-approved {
  background-color: #67C23A;
}

.status-rejected {
  background-color: #E6A23C;
}

.status-canceled {
  background-color: #909399;
}

.detail-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-content {
  padding: 20px 0;
}

.empty-detail {
  padding: 40px 0;
  text-align: center;
}

.reservation-detail {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}

.reject-dialog-content {
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;
  margin: 10px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.reject-dialog-content p {
  margin-bottom: 15px;
  font-weight: 600;
  color: #303133;
  font-size: 15px;
  display: flex;
  align-items: center;
}

.reject-dialog-content p::before {
  content: "⚠️";
  margin-right: 8px;
  font-size: 18px;
}

:deep(.el-input__inner) {
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
  
  font-size: 14px;
  line-height: 1.5;
}

:deep(.el-input__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

:deep(.el-input__inner:hover) {
  border-color: #c6e2ff;
}

:deep(.el-textarea__inner) {
  resize: vertical;
  min-height: 120px;
  padding: 12px;
  font-family: inherit;
}

:deep(.el-input__count) {
  color: #909399;
  font-size: 12px;
  text-align: right;
  margin-top: 8px;
  padding-right: 4px;
}

:deep(.el-input__count.is-warning) {
  color: #e6a23c;
}

:deep(.el-input__count.is-error) {
  color: #f56c6c;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 20px 20px 15px;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #ebeef5;
  padding: 15px 20px 20px;
}

:deep(.el-button) {
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover),
:deep(.el-button--danger:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

:deep(.el-button--danger) {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

:deep(.el-button--danger:hover) {
  background-color: #f78989;
  border-color: #f78989;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}
</style>