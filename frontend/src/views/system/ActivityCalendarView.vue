<template>
  <div class="system-reservation-calendar">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>活动预约日历</h2>
      <el-button-group>
        <el-button @click="handleRefresh">
          <el-icon><RefreshRight /></el-icon>
          刷新数据
        </el-button>
      </el-button-group>
    </div>

    <!-- 筛选区域 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :model="filterForm" label-width="80px" class="search-form">
        <el-form-item label="活动">
          <el-select
            v-model="filterForm.activityId"
            placeholder="请选择活动"
            clearable
            style="width: 180px"
            @change="handleActivityChange"
          >
            <el-option
              v-for="activity in allActivities"
              :key="activity.id"
              :label="activity.title"
              :value="activity.id"
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
            style="width: 150px"
            @change="handleStatusChange"
          >
            <el-option label="未开始" value="0" />
            <el-option label="进行中" value="1" />
            <el-option label="已结束" value="2" />
            <el-option label="已取消" value="3" />
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
              <span class="legend-text">未开始</span>
            </div>
            <div class="legend-item">
              <span class="legend-color" style="background-color: #67C23A;"></span>
              <span class="legend-text">进行中</span>
            </div>
            <div class="legend-item">
              <span class="legend-color" style="background-color: #E6A23C;"></span>
              <span class="legend-text">已结束</span>
            </div>
            <div class="legend-item">
              <span class="legend-color" style="background-color: #909399;"></span>
              <span class="legend-text">已取消</span>
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
                :class="getStatusClass(reservation.calculatedStatus)"
                :title="getReservationTitle(reservation)"
                @click.stop="handleReservationClick(reservation)"
              >
                {{ limitTitleLength(reservation.title, 6) }}
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

    <!-- 当日活动详情 -->
    <el-card shadow="hover" class="detail-card" v-if="selectedDate">
      <template #header>
        <div class="card-header">
          <span>{{ selectedDate }} 活动详情</span>
          <el-button size="small" @click="closeDetail">
            <el-icon><Close /></el-icon>
            关闭
          </el-button>
        </div>
      </template>
      
      <div v-if="selectedDayReservations.length > 0" class="detail-content">
        <el-table :data="selectedDayReservations" style="width: 100%" border stripe>
          <el-table-column prop="title" label="活动标题" width="200" />
          <el-table-column prop="startTime" label="活动开始时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="活动结束时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="location" label="活动地点" width="200" />
          <el-table-column prop="currentParticipants" label="当前参与人数" width="120" />
          <el-table-column prop="maxParticipants" label="最大参与人数" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag
                :type="scope.row.calculatedStatus === 0 ? 'warning' : scope.row.calculatedStatus === 1 ? 'success' : scope.row.calculatedStatus === 2 ? 'info' : 'danger'"
              >
                {{ scope.row.calculatedStatus === 0 ? '未开始' : scope.row.calculatedStatus === 1 ? '进行中' : scope.row.calculatedStatus === 2 ? '已结束' : '已取消' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                @click="handleViewReservation(scope.row)"
              >
                <el-icon><View /></el-icon>
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else class="empty-detail">
        <el-empty description="当日无活动" />
      </div>
    </el-card>

    <!-- 活动详情对话框 -->
    <el-dialog
      v-model="reservationDialogVisible"
      title="活动详情"
      width="60%"
      destroy-on-close
    >
      <div v-if="currentReservation" class="reservation-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="活动标题">{{ currentReservation.title }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDateTime(currentReservation.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatDateTime(currentReservation.endTime) }}</el-descriptions-item>
          <el-descriptions-item label="活动地点">{{ currentReservation.location }}</el-descriptions-item>
          <el-descriptions-item label="当前参与人数">{{ currentReservation.currentParticipants }}</el-descriptions-item>
          <el-descriptions-item label="最大参与人数">{{ currentReservation.maxParticipants }}</el-descriptions-item>
          <el-descriptions-item label="活动状态">
            <el-tag
              :type="currentReservation.calculatedStatus === 0 ? 'warning' : currentReservation.calculatedStatus === 1 ? 'success' : currentReservation.calculatedStatus === 2 ? 'info' : 'danger'"
            >
              {{ currentReservation.calculatedStatus === 0 ? '未开始' : currentReservation.calculatedStatus === 1 ? '进行中' : currentReservation.calculatedStatus === 2 ? '已结束' : '已取消' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="活动内容" :span="2">{{ currentReservation.content }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="reservationDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { RefreshRight, Plus, ArrowLeft, ArrowRight, Close, View, Check } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { activityApi } from '@/api/activity'
import { useActivityStore } from '@/stores/activity'

const activityStore = useActivityStore()

// 状态管理
const loading = ref(false)
const allActivities = ref([]) // 存储所有活动，用于选择器选项
const activityList = ref([]) // 仅用于日历显示
const selectedDate = ref('')
const selectedDayReservations = ref([])
const reservationDialogVisible = ref(false)
const currentReservation = ref(null)

// 筛选表单
const filterForm = ref({
  activityId: '',
  month: dayjs().format('YYYY-MM'),
  status: ''
})

// 日历配置
const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const currentDate = ref(new Date())

// 计算活动状态（基于当前时间）
const calculateActivityStatus = (startTime, endTime, manualStatus) => {
  if (manualStatus === 3) return 3;
  
  const now = new Date();
  const start = new Date(startTime);
  const end = new Date(endTime);
  
  if (isNaN(start.getTime()) || isNaN(end.getTime())) {
    return manualStatus;
  }
  
  const oneWeekBeforeStart = new Date(start.getTime() - 7 * 24 * 60 * 60 * 1000);
  const oneHourBeforeEnd = new Date(end.getTime() - 1 * 60 * 60 * 1000);
  
  if (now < oneWeekBeforeStart) {
    return 0;
  } else if (now >= start && now <= end) {
    return 1;
  } else if (now > end) {
    return 2;
  } else if (now >= oneWeekBeforeStart && now < start) {
    return 0;
  } else if (now > oneHourBeforeEnd && now <= end) {
    return 1;
  } else {
    return manualStatus;
  }
}

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
    // 加载活动列表
    await loadActivityList()
  } catch (error) {
    ElMessage.error('初始化数据失败')
    console.error('初始化数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载活动列表
const loadActivityList = async () => {
  loading.value = true
  try {
    const params = {
      page: 1,
      size: 1000,
      title: '',
      status: filterForm.value.status || ''
    }
    
    const result = await activityStore.fetchActivities(params)
    
    if (result.success) {
      let activities = [...activityStore.activities]
      
      // 为每个活动计算动态状态
      activities = activities.map(activity => {
        const calculatedStatus = calculateActivityStatus(activity.startTime, activity.endTime, activity.status);
        return {
          ...activity,
          calculatedStatus
        };
      });
      
      // 将所有活动存储到allActivities，用于选择器选项
      allActivities.value = activities
      
      // 前端根据筛选条件筛选活动
      let filteredActivities = [...activities]
      
      // 根据活动ID筛选
      if (filterForm.value.activityId) {
        filteredActivities = filteredActivities.filter(activity => 
          activity.id === filterForm.value.activityId
        )
      }
      
      // 根据月份筛选
      if (filterForm.value.month) {
        const selectedMonth = filterForm.value.month
        filteredActivities = filteredActivities.filter(activity => {
          const activityMonth = dayjs(activity.startTime).format('YYYY-MM')
          return activityMonth === selectedMonth
        })
      }
      
      // 根据状态筛选
      if (filterForm.value.status) {
        filteredActivities = filteredActivities.filter(activity => 
          activity.calculatedStatus === parseInt(filterForm.value.status)
        )
      }
      
      // 将筛选后的活动存储到activityList，用于日历显示
      activityList.value = filteredActivities
    }
  } catch (error) {
    console.error('加载活动列表失败:', error)
    ElMessage.error('加载活动列表失败')
  } finally {
    loading.value = false
  }
}

// 切换到上月
const prevMonth = () => {
  currentDate.value = dayjs(currentDate.value).subtract(1, 'month').toDate()
  filterForm.value.month = dayjs(currentDate.value).format('YYYY-MM')
  loadActivityList()
}

// 切换到下月
const nextMonth = () => {
  currentDate.value = dayjs(currentDate.value).add(1, 'month').toDate()
  filterForm.value.month = dayjs(currentDate.value).format('YYYY-MM')
  loadActivityList()
}

// 判断是否为今天
const isToday = (day) => {
  return dayjs().format('YYYY-MM-DD') === dayjs(currentDate.value).date(day).format('YYYY-MM-DD')
}

// 判断某天是否有活动
const hasReservations = (day) => {
  const date = dayjs(currentDate.value).date(day).format('YYYY-MM-DD')
  return activityList.value.some(activity => {
    const activityDate = dayjs(activity.startTime).format('YYYY-MM-DD')
    return activityDate === date
  })
}

// 获取某天的活动列表
const getDayReservations = (day) => {
  const date = dayjs(currentDate.value).date(day).format('YYYY-MM-DD')
  return activityList.value.filter(activity => {
    const activityDate = dayjs(activity.startTime).format('YYYY-MM-DD')
    return activityDate === date
  })
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

// 点击活动
const handleReservationClick = (activity) => {
  currentReservation.value = activity
  reservationDialogVisible.value = true
}

// 查看活动详情
const handleViewReservation = (activity) => {
  currentReservation.value = activity
  reservationDialogVisible.value = true
}

// 获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-pending' // 未开始
    case 1: return 'status-approved' // 进行中
    case 2: return 'status-rejected' // 已结束
    case 3: return 'status-canceled' // 已取消
    default: return ''
  }
}

// 获取活动标题
const getReservationTitle = (activity) => {
  return activity.title
}

// 限制标题长度
const limitTitleLength = (title, length) => {
  if (!title) return ''
  return title.length > length ? title.substring(0, length) + '...' : title
}

// 格式化日期
const formatDate = (dateTime) => {
  return dayjs(dateTime).format('YYYY-MM-DD')
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 刷新数据
const handleRefresh = () => {
  loadActivityList()
  ElMessage.success('数据刷新成功')
}

// 筛选相关方法
const handleActivityChange = () => {
  loadActivityList()
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
  loadActivityList()
}

const handleStatusChange = () => {
  loadActivityList()
}

const handleSearch = () => {
  loadActivityList()
}

const handleReset = () => {
  // 重置当前日期为今天
  currentDate.value = new Date()
  
  // 重置筛选条件
  filterForm.value = {
    activityId: '',
    month: dayjs().format('YYYY-MM'),
    status: ''
  }
  
  // 重新加载活动列表
  loadActivityList()
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
  /* min-height: 120px; */
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