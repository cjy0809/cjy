<template>
  <div class="system-page">
    <div class="system-page-title">
      <div class="system-page-title-text">
        <span class="system-page-title-icon">📊</span>
        数据分析看板
      </div>
      <div class="system-page-title-actions">
        <el-radio-group v-model="timeRange" @change="handleTimeRangeChange" style="margin-right: 12px">
          <el-radio-button label="day">日</el-radio-button>
          <el-radio-button label="week">周</el-radio-button>
          <el-radio-button label="month">月</el-radio-button>
          <el-radio-button label="year">年</el-radio-button>
        </el-radio-group>
        <el-button 
          type="primary" 
          :icon="Refresh"
          @click="handleRefresh"
        >
          刷新数据
        </el-button>
      </div>
    </div>

    <div v-if="loading" class="system-loading-overlay">
      <div class="system-loading-spinner">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>数据加载中...</span>
      </div>
    </div>

    <div v-else class="dashboard-content">
      <div class="dashboard-row">
        <div class="dashboard-card">
          <div class="card-header">
            <h3>用户增长趋势</h3>
          </div>
          <div class="card-body">
            <div ref="userTrendChart" style="width: 100%; height: 350px;"></div>
            <div v-show="!dashboardData.userTrend || dashboardData.userTrend.length === 0" class="empty-chart-overlay">暂无数据</div>
          </div>
        </div>

        <div class="dashboard-card">
          <div class="card-header">
            <h3>活动参与度</h3>
          </div>
          <div class="card-body">
            <div ref="activityChart" style="width: 100%; height: 350px;"></div>
            <div v-show="!dashboardData.activityParticipation || dashboardData.activityParticipation.length === 0" class="empty-chart-overlay">暂无数据</div>
          </div>
        </div>
      </div>

      <div class="dashboard-row">
        <div class="dashboard-card">
          <div class="card-header">
            <h3>服务预约统计</h3>
          </div>
          <div class="card-body">
            <div ref="serviceChart" style="width: 100%; height: 350px;"></div>
            <div v-show="!dashboardData.serviceBookingStats || dashboardData.serviceBookingStats.length === 0" class="empty-chart-overlay">暂无数据</div>
          </div>
        </div>

        <div class="dashboard-card">
          <div class="card-header">
            <h3>用户年龄分布</h3>
          </div>
          <div class="card-body">
            <div ref="ageDistributionChart" style="width: 100%; height: 350px;"></div>
            <div v-show="!dashboardData.ageDistribution || dashboardData.ageDistribution.length === 0" class="empty-chart-overlay">暂无数据</div>
          </div>
        </div>
      </div>

      <div class="dashboard-row">
        <div class="dashboard-card">
          <div class="card-header">
            <h3>充值金额分布</h3>
          </div>
          <div class="card-body">
            <div ref="rechargeChart" style="width: 100%; height: 350px;"></div>
            <div v-show="!dashboardData.rechargeDistribution || dashboardData.rechargeDistribution.length === 0" class="empty-chart-overlay">暂无数据</div>
          </div>
        </div>

        <div class="dashboard-card">
          <div class="card-header">
            <h3>活动类型偏好</h3>
          </div>
          <div class="card-body">
            <div ref="activityTypeChart" style="width: 100%; height: 350px;"></div>
            <div v-show="!dashboardData.activityTypePreference || dashboardData.activityTypePreference.length === 0" class="empty-chart-overlay">暂无数据</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

/**
 * 数据分析看板视图组件
 * 
 * 功能说明：
 * - 展示用户增长趋势图表
 * - 展示活动参与度统计
 * - 展示服务预约统计
 * - 展示用户年龄分布
 * - 展示充值金额分布
 * - 展示活动类型偏好
 * - 支持按日、周、月、年切换时间范围
 * - 支持手动刷新数据
 * - 使用ECharts进行数据可视化
 * 
 * @component AnalyticsDashboardView
 */

const loading = ref(false)
const timeRange = ref('month')
const dashboardData = reactive({
  userTrend: [],
  activityParticipation: [],
  serviceBookingStats: [],
  ageDistribution: [],
  rechargeDistribution: [],
  activityTypePreference: []
})

const userTrendChart = ref(null)
const activityChart = ref(null)
const serviceChart = ref(null)
const ageDistributionChart = ref(null)
const rechargeChart = ref(null)
const activityTypeChart = ref(null)

let userTrendChartInstance = null
let activityChartInstance = null
let serviceChartInstance = null
let ageDistributionChartInstance = null
let rechargeChartInstance = null
let activityTypeChartInstance = null

/**
 * 格式化日期
 * @param {Date} date - 日期对象
 * @returns {string} 格式化后的日期字符串 (YYYY-MM-DD)
 */
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 获取日期范围
 * 根据选择的时间范围（日、周、月、年）计算开始和结束日期
 * @returns {Object} 包含startDate和endDate的对象
 */
const getDateRange = () => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  let startDate, endDate

  switch (timeRange.value) {
    case 'day':
      startDate = today
      endDate = today
      break
    case 'week':
      const dayOfWeek = today.getDay()
      const diff = today.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1)
      startDate = new Date(today.setDate(diff))
      endDate = new Date()
      break
    case 'month':
      startDate = new Date(today.getFullYear(), today.getMonth(), 1)
      endDate = new Date()
      break
    case 'year':
      startDate = new Date(today.getFullYear(), 0, 1)
      endDate = new Date()
      break
    default:
      startDate = new Date(today.getFullYear(), today.getMonth(), 1)
      endDate = new Date()
  }

  return {
    startDate: formatDate(startDate),
    endDate: formatDate(endDate)
  }
}

/**
 * 处理时间范围变化
 */
const handleTimeRangeChange = () => {
  fetchDashboardData()
}

/**
 * 处理刷新数据
 */
const handleRefresh = () => {
  fetchDashboardData()
}

/**
 * 获取看板数据
 * 从后端API获取统计数据并更新图表
 */
const fetchDashboardData = async () => {
  loading.value = true
  try {
    const dateRangeParams = getDateRange()
    const params = {
      startDate: dateRangeParams.startDate,
      endDate: dateRangeParams.endDate
    }

    const response = await request.get('/api/analytics/dashboard', { params })
    
    if (response.code === 200) {
      const data = response.data

      dashboardData.userTrend = data.userTrend || []
      dashboardData.activityParticipation = data.activityParticipation || []
      dashboardData.serviceBookingStats = data.serviceBookingStats || []
      dashboardData.ageDistribution = data.ageDistribution || []
      dashboardData.rechargeDistribution = data.rechargeDistribution || []
      dashboardData.activityTypePreference = data.activityTypePreference || []

      loading.value = false
      await nextTick()
      await nextTick()
      await nextTick()
      initCharts()
    }
  } catch (error) {
    console.error('获取看板数据失败:', error)
    ElMessage.error('获取看板数据失败')
    loading.value = false
  }
}

/**
 * 初始化所有图表
 * 根据数据是否存在来决定是否初始化对应的图表
 */
const initCharts = () => {
  
  if (dashboardData.userTrend && dashboardData.userTrend.length > 0) {
    initUserTrendChart()
  }
  
  if (dashboardData.activityParticipation && dashboardData.activityParticipation.length > 0) {
    initActivityChart()
  }
  
  if (dashboardData.serviceBookingStats && dashboardData.serviceBookingStats.length > 0) {
    initServiceChart()
  }
  
  if (dashboardData.ageDistribution && dashboardData.ageDistribution.length > 0) {
    initAgeDistributionChart()
  }
  
  if (dashboardData.rechargeDistribution && dashboardData.rechargeDistribution.length > 0) {
    initRechargeChart()
  }
  
  if (dashboardData.activityTypePreference && dashboardData.activityTypePreference.length > 0) {
    initActivityTypeChart()
  }
  
}

/**
 * 初始化用户增长趋势图表
 * 使用折线图展示新增用户和活跃用户的趋势
 */
const initUserTrendChart = () => {
  if (!userTrendChart.value) {
    setTimeout(() => {
      if (userTrendChart.value) {
        initUserTrendChart()
      }
    }, 100)
    return
  }

  if (userTrendChartInstance) {
    userTrendChartInstance.dispose()
  }

  userTrendChartInstance = echarts.init(userTrendChart.value)

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['新增用户', '活跃用户']
    },
    xAxis: {
      type: 'category',
      data: dashboardData.userTrend.map(item => item.date)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        data: dashboardData.userTrend.map(item => item.newUsers),
        smooth: true,
        itemStyle: {
          color: '#1890ff'
        }
      },
      {
        name: '活跃用户',
        type: 'line',
        data: dashboardData.userTrend.map(item => item.activeUsers),
        smooth: true,
        itemStyle: {
          color: '#52c41a'
        }
      }
    ]
  }

  userTrendChartInstance.setOption(option)
}

/**
 * 初始化活动参与度图表
 * 使用柱状图展示报名人数和参与人数
 */
const initActivityChart = () => {
  if (!activityChart.value) return

  if (activityChartInstance) {
    activityChartInstance.dispose()
  }

  activityChartInstance = echarts.init(activityChart.value)

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['报名人数', '参与人数']
    },
    xAxis: {
      type: 'category',
      data: dashboardData.activityParticipation.map(item => item.date)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '报名人数',
        type: 'bar',
        data: dashboardData.activityParticipation.map(item => item.registrations),
        itemStyle: {
          color: '#1890ff'
        }
      },
      {
        name: '参与人数',
        type: 'bar',
        data: dashboardData.activityParticipation.map(item => item.participants),
        itemStyle: {
          color: '#52c41a'
        }
      }
    ]
  }

  activityChartInstance.setOption(option)
}

/**
 * 初始化服务预约统计图表
 * 使用柱状图展示预约数、完成数和取消数
 */
const initServiceChart = () => {
  if (!serviceChart.value) return

  if (serviceChartInstance) {
    serviceChartInstance.dispose()
  }

  serviceChartInstance = echarts.init(serviceChart.value)

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['预约数', '完成数', '取消数']
    },
    xAxis: {
      type: 'category',
      data: dashboardData.serviceBookingStats.map(item => item.date)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '预约数',
        type: 'bar',
        data: dashboardData.serviceBookingStats.map(item => item.bookings),
        itemStyle: {
          color: '#1890ff'
        }
      },
      {
        name: '完成数',
        type: 'bar',
        data: dashboardData.serviceBookingStats.map(item => item.completed),
        itemStyle: {
          color: '#52c41a'
        }
      },
      {
        name: '取消数',
        type: 'bar',
        data: dashboardData.serviceBookingStats.map(item => item.cancelled),
        itemStyle: {
          color: '#ff4d4f'
        }
      }
    ]
  }

  serviceChartInstance.setOption(option)
}

/**
 * 初始化用户年龄分布图表
 * 使用饼图展示用户年龄分布
 */
const initAgeDistributionChart = () => {
  if (!ageDistributionChart.value) return

  if (ageDistributionChartInstance) {
    ageDistributionChartInstance.dispose()
  }

  ageDistributionChartInstance = echarts.init(ageDistributionChart.value)

  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '年龄分布',
        type: 'pie',
        radius: '50%',
        data: dashboardData.ageDistribution.map(item => ({
          value: item.count,
          name: item.ageRange
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }

  ageDistributionChartInstance.setOption(option)
}

/**
 * 初始化充值金额分布图表
 * 使用柱状图展示不同充值金额段的次数
 */
const initRechargeChart = () => {
  if (!rechargeChart.value) return

  if (rechargeChartInstance) {
    rechargeChartInstance.dispose()
  }

  rechargeChartInstance = echarts.init(rechargeChart.value)

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: dashboardData.rechargeDistribution.map(item => item.amountRange)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '充值次数',
        type: 'bar',
        data: dashboardData.rechargeDistribution.map(item => item.count),
        itemStyle: {
          color: '#1890ff'
        }
      }
    ]
  }

  rechargeChartInstance.setOption(option)
}

/**
 * 初始化活动类型偏好图表
 * 使用柱状图展示不同活动类型的报名数和参与数
 */
const initActivityTypeChart = () => {
  if (!activityTypeChart.value) return

  if (activityTypeChartInstance) {
    activityTypeChartInstance.dispose()
  }

  activityTypeChartInstance = echarts.init(activityTypeChart.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['报名数', '参与数']
    },
    xAxis: {
      type: 'category',
      data: dashboardData.activityTypePreference.map(item => item.type),
      axisLabel: {
        interval: 0,
        rotate: 45
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '报名数',
        type: 'bar',
        data: dashboardData.activityTypePreference.map(item => item.registrations),
        itemStyle: {
          color: '#1890ff'
        }
      },
      {
        name: '参与数',
        type: 'bar',
        data: dashboardData.activityTypePreference.map(item => item.participants),
        itemStyle: {
          color: '#52c41a'
        }
      }
    ]
  }

  activityTypeChartInstance.setOption(option)
}

/**
 * 处理窗口大小变化
 * 调整所有图表的大小以适应新的窗口尺寸
 */
const handleResize = () => {
  userTrendChartInstance?.resize()
  activityChartInstance?.resize()
  serviceChartInstance?.resize()
  ageDistributionChartInstance?.resize()
  rechargeChartInstance?.resize()
  activityTypeChartInstance?.resize()
}

/**
 * 组件挂载时获取数据并添加窗口大小监听
 */
onMounted(() => {
  fetchDashboardData()
  window.addEventListener('resize', handleResize)
})

/**
 * 组件卸载前清理资源
 * 移除事件监听并销毁所有图表实例
 */
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  userTrendChartInstance?.dispose()
  activityChartInstance?.dispose()
  serviceChartInstance?.dispose()
  ageDistributionChartInstance?.dispose()
  rechargeChartInstance?.dispose()
  activityTypeChartInstance?.dispose()
})
</script>

<style scoped>
.dashboard-content {
  padding: 20px;
}

.dashboard-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.dashboard-card {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.dashboard-card.full-width {
  flex: 2;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.card-body {
  padding: 20px;
  position: relative;
}

.empty-chart {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 350px;
  color: #999;
  font-size: 14px;
}

.empty-chart-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  color: #999;
  font-size: 14px;
  z-index: 10;
}

.system-loading-overlay {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.system-loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #999;
}

.system-loading-spinner .el-icon {
  font-size: 32px;
  color: #1890ff;
}
</style>
