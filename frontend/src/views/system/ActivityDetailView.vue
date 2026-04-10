<template>
  <div class="system-page">
    <div class="system-page-title">
      <div class="system-page-title-text">
        <button class="back-button" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <span class="system-page-title-icon">🎉</span>
        活动详情
      </div>
      <div class="action-buttons">
        <button class="system-button system-button-primary" @click="goBack">
          返回列表
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-content">
        <p>加载中...</p>
      </div>
    </div>

    <div v-else-if="error" class="error-container">
      <el-result
        icon="error"
        title="加载失败"
        :sub-title="error"
      >
        <template #extra>
          <el-button type="primary" @click="fetchActivityDetail">重试</el-button>
          <el-button @click="goBack">返回</el-button>
        </template>
      </el-result>
    </div>

    <div v-else-if="activityDetail" class="activity-detail-container">
        <div class="detail-main-content">
          <!-- 活动基本信息 -->
          <div class="detail-card">
            <div class="detail-card-header">
              <h2>{{ activityDetail.title }}</h2>
              <div class="status-tag" :class="getStatusClass(calculatedActivityStatus)">
                {{ getStatusText(calculatedActivityStatus) }}
              </div>
            </div>
            
            <div class="detail-card-body">
              <div class="detail-row">
                <div class="detail-label">活动地点：</div>
                <div class="detail-value">{{ activityDetail.location }}</div>
              </div>
              
              <div class="detail-row">
                <div class="detail-label">开始时间：</div>
                <div class="detail-value">{{ formatDateTime(activityDetail.startTime) }}</div>
              </div>
              
              <div class="detail-row">
                <div class="detail-label">结束时间：</div>
                <div class="detail-value">{{ formatDateTime(activityDetail.endTime) }}</div>
              </div>
              
              <div class="detail-row">
                <div class="detail-label">参与人数：</div>
                <div class="detail-value">
                  {{ activityDetail.currentParticipants }} / {{ activityDetail.maxParticipants }}
                  <el-progress 
                    :percentage="getParticipantPercentage(activityDetail.currentParticipants, activityDetail.maxParticipants)"
                    :color="getProgressColor(activityDetail.currentParticipants, activityDetail.maxParticipants)"
                    :show-text="false"
                    style="width: 200px; margin-left: 10px;"
                  />
                </div>
              </div>
              
              <div class="detail-row">
                <div class="detail-label">报名状态：</div>
                <div class="detail-value">
                  <span v-if="calculatedRegistrationStatus === 0" class="registration-status closed">不可报名</span>
                  <span v-else-if="calculatedRegistrationStatus === 1" class="registration-status open">可报名</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 活动内容 -->
          <div class="detail-card">
            <div class="detail-card-header">
              <h3>活动内容</h3>
            </div>
            <div class="detail-card-body">
              <div class="activity-content">
                {{ activityDetail.content || '暂无活动内容' }}
              </div>
            </div>
          </div>



          <!-- 活动评论 -->
          <CommentList
            v-if="activityDetail"
            target-type="activity"
            :target-id="activityDetail.id"
            :admin-mode="isAdmin"
          />
        </div>

        <!-- 参与者列表 -->
        <div class="detail-sidebar">
          <div class="participants-card">
            <div class="participants-card-header">
              <h3>参与者列表</h3>
              <span class="participants-count">({{ participants.length }})</span>
            </div>
            <div class="participants-card-body">
              <div v-if="participantsLoading" class="participants-loading">
                <el-skeleton :rows="5" animated />
              </div>
              <div v-else-if="participantsError" class="participants-error">
                <el-alert type="error" :message="participantsError" show-icon />
                <el-button type="primary" size="small" @click="fetchActivityParticipants">重试</el-button>
              </div>
              <div v-else-if="participants.length === 0" class="participants-empty">
                <el-empty description="暂无参与者" />
              </div>
              <div v-else class="participants-list">
                <div v-for="participant in participants" :key="participant.id" class="participant-item">
                  <div class="participant-info">
                    <div class="participant-name">{{ participant.userName }}</div>
                    <div class="participant-time">{{ formatDateTime(participant.registrationTime) }}</div>
                  </div>
                  <div class="participant-status" :class="getStatusClass(participant.status)">
                    {{ getParticipantStatusText(participant.status) }}
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
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { activityApi } from '@/api/activity'
import { useUserStore } from '@/stores/user'
import CommentList from '@/components/common/CommentList.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 活动详情数据
const activityDetail = ref(null)
const loading = ref(true)
const error = ref('')

// 参与者列表数据
const participants = ref([])
const participantsLoading = ref(false)
const participantsError = ref('')

// 判断是否为管理员
const isAdmin = computed(() => {
  return userStore.userInfo && (userStore.userInfo.role === 'admin' || userStore.userInfo.role === 'staff')
})

// 获取活动参与者列表
const fetchActivityParticipants = async () => {
  participantsLoading.value = true
  participantsError.value = ''
  
  try {
    const activityId = route.params.id
    const response = await activityApi.getActivityParticipants(activityId)
    
    if (response.code === 200 && response.data) {
      // 只保留状态为已通过(2)和已报名(1)的参与者
      participants.value = response.data.filter(p => p.status === 2 || p.status === 1)
      
      // 更新活动详情中的参与人数
      if (activityDetail.value) {
        activityDetail.value.currentParticipants = participants.value.length
      }
    } else {
      participantsError.value = response.message || '获取参与者列表失败'
    }
  } catch (err) {
    console.error('获取参与者列表失败:', err)
    participantsError.value = '获取参与者列表失败，请稍后重试'
  } finally {
    participantsLoading.value = false
  }
}

// 计算活动状态（基于当前时间）
const calculateActivityStatus = (startTime, endTime, manualStatus) => {
  // 如果手动状态是已取消，保持已取消状态
  if (manualStatus === 3) return 3;
  
  const now = new Date();
  const start = new Date(startTime);
  const end = new Date(endTime);
  
  // 检查日期是否有效
  if (isNaN(start.getTime()) || isNaN(end.getTime())) {
    return manualStatus;
  }
  
  // 根据当前时间自动设置状态
  if (now < start) {
    // 活动未开始
    return 0; // 未开始
  } else if (now >= start && now <= end) {
    // 活动进行中
    return 1; // 进行中
  } else {
    // 活动已结束
    return 2; // 已结束
  }
}

// 计算属性：活动状态（基于当前时间）
const calculatedActivityStatus = computed(() => {
  if (!activityDetail.value) return 0;
  return calculateActivityStatus(
    activityDetail.value.startTime,
    activityDetail.value.endTime,
    activityDetail.value.status
  );
});

// 检查是否可以报名（活动开始前一周到活动结束前一小时）
const canRegisterActivity = (startTime, endTime, currentParticipants, maxParticipants) => {
  const now = new Date();
  const start = new Date(startTime);
  const end = new Date(endTime);
  
  // 检查日期是否有效
  if (isNaN(start.getTime()) || isNaN(end.getTime())) {
    return false;
  }
  
  // 计算活动开始时间前一周
  const oneWeekBeforeStart = new Date(start.getTime() - 7 * 24 * 60 * 60 * 1000);
  
  // 计算活动结束时间前一小时
  const oneHourBeforeEnd = new Date(end.getTime() - 60 * 60 * 1000);
  
  // 检查是否在报名时间窗口内（活动开始前一周到活动结束前一小时）
  const isInRegistrationWindow = now >= oneWeekBeforeStart && now < oneHourBeforeEnd;
  
  // 检查是否已满员
  const isNotFull = currentParticipants < maxParticipants;
  
  return isInRegistrationWindow && isNotFull;
}

// 计算属性：报名状态（基于当前时间和报名时间逻辑）
const calculatedRegistrationStatus = computed(() => {
  if (!activityDetail.value) return 0;
  return canRegisterActivity(
    activityDetail.value.startTime,
    activityDetail.value.endTime,
    activityDetail.value.currentParticipants,
    activityDetail.value.maxParticipants
  ) ? 1 : 0;
});

// 获取活动详情
const fetchActivityDetail = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const activityId = route.params.id
    const response = await activityApi.getActivityDetail(activityId)
    
    if (response.code === 200 && response.data) {
      activityDetail.value = response.data
      // 获取参与者列表
      await fetchActivityParticipants()
    } else {
      error.value = response.message || '获取活动详情失败'
    }
  } catch (err) {
    console.error('获取活动详情失败:', err)
    error.value = '获取活动详情失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.push('/system/activities')
}

// 获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-upcoming'
    case 1: return 'status-ongoing'
    case 2: return 'status-finished'
    case 3: return 'status-canceled'
    default: return ''
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '未开始'
    case 1: return '进行中'
    case 2: return '已结束'
    case 3: return '已取消'
    default: return '未知状态'
  }
}

// 获取参与者状态文本
const getParticipantStatusText = (status) => {
  switch (status) {
    case 1: return '已报名'
    case 2: return '已通过'
    case 3: return '已拒绝'
    case 4: return '已取消'
    default: return '未知状态'
  }
}

// 获取参与人数百分比
const getParticipantPercentage = (current, max) => {
  if (!max || max === 0) return 0
  return Math.round((current / max) * 100)
}

// 获取进度条颜色
const getProgressColor = (current, max) => {
  const percentage = getParticipantPercentage(current, max)
  if (percentage < 50) return '#67c23a'
  if (percentage < 80) return '#e6a23c'
  return '#f56c6c'
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

// 组件挂载时获取活动详情
onMounted(() => {
  fetchActivityDetail()
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

.activity-detail-container {
  display: flex;
  gap: var(--system-space-lg);
}

.detail-main-content {
  flex: 3;
  display: flex;
  flex-direction: column;
  gap: var(--system-space-lg);
}

.detail-sidebar {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--system-space-lg);
}

.participants-card {
  background-color: var(--system-bg-white);
  border-radius: var(--system-border-radius-lg);
  box-shadow: var(--system-shadow-sm);
  overflow: hidden;
  transition: all 0.3s ease;
  height: fit-content;
}

.participants-card:hover {
  box-shadow: var(--system-shadow-md);
}

.participants-card-header {
  padding: var(--system-space-lg);
  border-bottom: 1px solid var(--system-border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.participants-card-header h3 {
  margin: 0;
  font-size: var(--system-font-size-lg);
  color: var(--system-text-primary);
}

.participants-count {
  font-size: var(--system-font-size-sm);
  color: var(--system-text-secondary);
  font-weight: 400;
}

.participants-card-body {
  padding: var(--system-space-lg);
}

.participants-loading, .participants-error, .participants-empty {
  margin-bottom: var(--system-space-md);
}

.participants-error {
  display: flex;
  flex-direction: column;
  gap: var(--system-space-sm);
}

.participants-list {
  display: flex;
  flex-direction: column;
  gap: var(--system-space-md);
  max-height: 500px;
  overflow-y: auto;
}

.participant-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--system-space-md);
  background-color: var(--system-bg-light);
  border-radius: var(--system-border-radius-md);
  transition: all 0.3s ease;
}

.participant-item:hover {
  background-color: var(--system-bg-lighter);
  box-shadow: var(--system-shadow-sm);
}

.participant-info {
  flex: 1;
}

.participant-name {
  font-weight: 500;
  color: var(--system-text-primary);
  margin-bottom: var(--system-space-xs);
}

.participant-time {
  font-size: var(--system-font-size-xs);
  color: var(--system-text-secondary);
}

.participant-status {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .activity-detail-container {
    flex-direction: column;
  }
  
  .detail-sidebar {
    order: -1;
  }
  
  .participants-list {
    max-height: 300px;
  }
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

.status-upcoming {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-ongoing {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-finished {
  background-color: #f5f5f5;
  color: #999;
}

.status-canceled {
  background-color: #fff2f0;
  color: #ff4d4f;
}

.registration-status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.registration-status.open {
  background-color: #f6ffed;
  color: #52c41a;
}

.registration-status.closed {
  background-color: #fff2f0;
  color: #ff4d4f;
}

.activity-content {
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