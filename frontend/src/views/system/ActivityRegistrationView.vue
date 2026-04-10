<template>
  <div class="system-page">
    <div class="system-page-title">
      <div class="system-page-title-text">
        <span class="system-page-title-icon">📝</span>
        活动报名管理
      </div>
      <div class="system-page-title-actions">
        <button class="btn btn-primary" @click="showAddRegistrationDialog = true">
          <span class="system-button-icon">+</span>
          新增报名
        </button>
      </div>
    </div>

    <!-- 报名统计卡片 -->
    <div class="system-stats-container">
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">报名总数</div>
          <div class="system-stats-icon" style="background-color: rgba(24, 144, 255, 0.1); color: #1890ff;">
            <el-icon><List /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statisticsData.totalRegistrations || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">待审核</div>
          <div class="system-stats-icon" style="background-color: rgba(250, 173, 20, 0.1); color: #faad14;">
            <el-icon><Clock /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statisticsData.pendingRegistrations || 0 }}</div>
        
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">已通过</div>
          <div class="system-stats-icon" style="background-color: rgba(82, 196, 26, 0.1); color: #52c41a;">
            <el-icon><Check /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statisticsData.approvedRegistrations || 0 }}</div>
        
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">已拒绝</div>
          <div class="system-stats-icon" style="background-color: rgba(255, 77, 79, 0.1); color: #ff4d4f;">
            <el-icon><CloseBold /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statisticsData.rejectedRegistrations || 0 }}</div>
       
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">已取消</div>
          <div class="system-stats-icon" style="background-color: rgba(150, 150, 150, 0.1); color: #969696;">
            <el-icon><Close /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statisticsData.cancelledRegistrations || 0 }}</div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="system-search-bar">
      <div class="system-search-form">
        <div class="system-search-item">
          <div class="system-form-label">活动标题</div>
          <input class="system-form-input" type="text" v-model="searchForm.activityTitle" placeholder="请输入活动标题">
        </div>
        <div class="system-search-item">
          <div class="system-form-label">用户姓名</div>
          <input class="system-form-input" type="text" v-model="searchForm.userName" placeholder="请输入用户姓名">
        </div>
        <div class="system-search-item">
          <div class="system-form-label">报名状态</div>
          <select class="system-form-input" v-model="searchForm.status">
            <option value="">全部状态</option>
            <option value="1">已报名</option>
            <option value="2">已通过</option>
            <option value="3">已拒绝</option>
            <option value="4">已取消</option>
          </select>
        </div>
        <div class="system-search-actions">
          <button class="system-button system-button-primary" @click="searchRegistrations">搜索</button>
          <button class="system-button system-button-default" @click="resetSearch">重置</button>
        </div>
      </div>
    </div>

    <!-- 批量操作区域 -->
    <div class="system-batch-actions" v-if="selectedRegistrations.length > 0">
      <div class="system-batch-actions-info">
        <span class="system-batch-actions-count">已选择 {{ selectedRegistrations.length }} 条记录</span>
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

    <!-- 报名列表 -->
    <div class="system-table-container">
      <table class="system-table">
        <thead>
          <tr>
            <th style="width: 50px; text-align: center;">
              <input type="checkbox" v-model="selectAll" @change="handleSelectAll" />
            </th>
            <th>活动标题</th>
            <th>用户姓名</th>
            <th>联系电话</th>
            <th>报名时间</th>
            <th>活动时间</th>
            <th>报名状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="registration in registrations" :key="registration.id">
            <td style="text-align: center;">
              <input 
                type="checkbox" 
                v-model="selectedRegistrations" 
                :value="registration.id"
                @change="handleSelectionChange"
              />
            </td>
            <td v-html="formatActivityTitle(registration.activityTitle)"></td>
            <td>{{ registration.userName }}</td>
            <td>{{ registration.userPhone }}</td>
            <td>{{ formatDateTime(registration.registrationTime) }}</td>
            <td>{{ formatDateTime(registration.activityStartTime) }}</td>
            <td>
              <span v-if="registration.status === 1" class="status-tag status-pending">已报名</span>
              <span v-else-if="registration.status === 2" class="status-tag status-approved">已通过</span>
              <span v-else-if="registration.status === 3" class="status-tag status-rejected">已拒绝</span>
              <span v-else-if="registration.status === 4" class="status-tag status-cancelled">已取消</span>
            </td>
            <td>
              <div class="action-btn-group">
                <el-button size="small" type="info" @click="viewRegistration(registration)">查看</el-button>
                <el-button v-if="registration.status === 1" size="small" type="success" @click="approveRegistration(registration.id)">
                  审核
                </el-button>
                <el-button v-if="registration.status === 1" size="small" type="warning" @click="cancelRegistration(registration.id)">
                  取消
                </el-button>
                <el-button size="small" type="danger" @click="deleteRegistration(registration.id)">
                  删除
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
              v-model="pageSizeLocal" 
              @change="handlePageSizeChange" 
              size="small"
              style="width: 80px; margin: 0 8px;"
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

    <!-- 批量审核对话框 -->
    <div v-if="showBatchApproveDialog" class="system-modal-overlay" @click.self="closeBatchApproveDialog">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">批量审核报名</div>
          <button class="system-modal-close" @click="closeBatchApproveDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">已选择记录</div>
              <div class="system-form-info">共 {{ selectedRegistrations.length }} 条报名记录</div>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">审核结果</div>
              <select class="system-form-input" v-model="batchApproveForm.status">
                <option value="2">通过</option>
                <option value="3">拒绝</option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">备注信息</div>
              <textarea class="system-form-textarea" v-model="batchApproveForm.remark" placeholder="请输入备注信息" rows="4"></textarea>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <button class="btn btn-default" @click="closeBatchApproveDialog">取消</button>
          <button class="btn btn-primary" @click="submitBatchApprove">确定</button>
        </div>
      </div>
    </div>

    <!-- 查看报名对话框 -->
    <!-- 新增报名对话框 -->
    <div v-if="showAddRegistrationDialog" class="system-modal-overlay" @click.self="closeAddRegistrationDialog">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">新增报名</div>
          <button class="system-modal-close" @click="closeAddRegistrationDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">活动名称</div>
              <select class="system-form-input" v-model="registrationForm.activityId" @change="handleActivityChange">
                <option value="">请选择活动</option>
                <option v-for="activity in activities" :key="activity.id" :value="activity.id">
                  {{ activity.title }} (剩余: {{ activity.maxParticipants - activity.currentParticipants }}人)
                </option>
              </select>
              <div v-if="selectedActivity" class="activity-info">
                <span class="activity-info-text">活动容量: {{ selectedActivity.maxParticipants }}人</span>
                <span class="activity-info-text">已报名: {{ selectedActivity.currentParticipants }}人</span>
                <span class="activity-info-text">剩余: {{ selectedActivity.maxParticipants - selectedActivity.currentParticipants }}人</span>
              </div>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">用户选择</div>
              <div class="user-checkbox-container">
                <div v-for="user in users" :key="user.id" class="user-checkbox-item">
                  <label class="user-checkbox-label">
                    <input 
                      type="checkbox" 
                      :value="user.id" 
                      v-model="registrationForm.userIds"
                      :disabled="!registrationForm.activityId || (registrationForm.userIds.length >= remainingParticipants && !registrationForm.userIds.includes(user.id))"
                    >
                    <span class="user-checkbox-text">{{ user.name }} ({{ user.phone }})</span>
                  </label>
                </div>
              </div>
              <div v-if="registrationForm.activityId" class="selected-users-info">
                <span>已选择 {{ registrationForm.userIds.length }} 人</span>
                <span v-if="registrationForm.userIds.length > remainingParticipants" class="warning-text">
                  (超过剩余人数 {{ remainingParticipants }} 人)
                </span>
              </div>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">备注信息</div>
              <textarea class="system-form-textarea" v-model="registrationForm.remark" placeholder="请输入备注信息" rows="4"></textarea>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <button class="btn btn-default" @click="closeAddRegistrationDialog">取消</button>
          <button class="btn btn-primary" @click="saveRegistration">保存</button>
        </div>
      </div>
    </div>

    <!-- 审核报名对话框 -->
    <div v-if="showApproveDialog" class="system-modal-overlay" @click.self="closeApproveDialog">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">审核报名</div>
          <button class="system-modal-close" @click="closeApproveDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">审核结果</div>
              <select class="system-form-input" v-model="approveForm.status">
                <option value="2">通过</option>
                <option value="3">拒绝</option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">备注信息</div>
              <textarea class="system-form-textarea" v-model="approveForm.remark" placeholder="请输入备注信息" rows="4"></textarea>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <button class="btn btn-default" @click="closeApproveDialog">取消</button>
          <button class="btn btn-primary" @click="submitApprove">确定</button>
        </div>
      </div>
    </div>

    <!-- 查看报名对话框 -->
    <div v-if="showViewRegistrationDialog" class="system-modal-overlay" @click.self="closeViewRegistrationDialog">
      <div class="system-modal system-modal-large">
        <div class="system-modal-header">
          <div class="system-modal-title">报名详情</div>
          <button class="system-modal-close" @click="closeViewRegistrationDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="registration-detail-container">
            <!-- 报名基本信息卡片 -->
            <div class="detail-card">
              <div class="detail-card-header">
                <h2>{{ currentRegistration.activityTitle }}</h2>
                <div class="status-tag" :class="getStatusClass(currentRegistration.status)">
                  {{ getStatusText(currentRegistration.status) }}
                </div>
              </div>
              
              <div class="detail-card-body">
                <div class="detail-row">
                  <div class="detail-label">用户姓名：</div>
                  <div class="detail-value">{{ currentRegistration.userName }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">联系电话：</div>
                  <div class="detail-value">{{ currentRegistration.userPhone }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">报名时间：</div>
                  <div class="detail-value">{{ formatDateTime(currentRegistration.registrationTime) }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">活动时间：</div>
                  <div class="detail-value">{{ formatDateTime(currentRegistration.activityStartTime) }}</div>
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { List, ArrowUp, ArrowDown, Check, Clock, Close, CloseBold } from '@element-plus/icons-vue'
import { useRegistrationStore } from '@/stores/registration'
import { useUserStore } from '@/stores/user'
import { activityApi } from '@/api/activity.js'
import { userApi } from '@/api/user.js'

const registrationStore = useRegistrationStore()
const userStore = useUserStore()

// 搜索表单
const searchForm = reactive({
  activityTitle: '',
  userName: '',
  status: ''
})

// 报名列表
const registrations = computed(() => registrationStore.registrations)
const total = computed(() => registrationStore.total)
const loading = computed(() => registrationStore.loading)
const currentPage = computed(() => registrationStore.currentPage)
const pageSize = computed(() => registrationStore.pageSize)

// 本地分页大小
const pageSizeLocal = ref(20)
const jumpPage = ref('')

// 统计数据
const statisticsData = reactive({
  totalRegistrations: 0,
  pendingRegistrations: 0,
  approvedRegistrations: 0,
  rejectedRegistrations: 0,
  cancelledRegistrations: 0,
  thisMonthRegistrations: 0
})

// 对话框状态
const showApproveDialog = ref(false)
const showAddRegistrationDialog = ref(false)
const showViewRegistrationDialog = ref(false)

// 批量操作相关
const selectedRegistrations = ref([])
const selectAll = ref(false)

// 批量审核表单
const batchApproveForm = reactive({
  status: '',
  remark: ''
})

const showBatchApproveDialog = ref(false)

// 批量通过
const batchApprove = () => {
  if (selectedRegistrations.value.length === 0) {
    ElMessage.warning('请先选择要审核的报名记录')
    return
  }
  batchApproveForm.status = '2'
  batchApproveForm.remark = ''
  showBatchApproveDialog.value = true
}

// 批量拒绝
const batchReject = () => {
  if (selectedRegistrations.value.length === 0) {
    ElMessage.warning('请先选择要审核的报名记录')
    return
  }
  batchApproveForm.status = '3'
  batchApproveForm.remark = ''
  showBatchApproveDialog.value = true
}

// 提交批量审核
const submitBatchApprove = async () => {
  try {
    const result = await registrationStore.batchApproveRegistrations(
      selectedRegistrations.value,
      parseInt(batchApproveForm.status),
      batchApproveForm.remark
    )
    if (result.success) {
      ElMessage.success(result.message || '批量审核成功')
      closeBatchApproveDialog()
      clearSelection()
      fetchRegistrationStatistics()
    }
  } catch (error) {
    console.error('批量审核失败:', error)
    ElMessage.error('批量审核失败')
  }
}

// 关闭批量审核对话框
const closeBatchApproveDialog = () => {
  showBatchApproveDialog.value = false
  batchApproveForm.status = ''
  batchApproveForm.remark = ''
}

// 全选/取消全选
const handleSelectAll = () => {
  if (selectAll.value) {
    selectedRegistrations.value = registrations.value.map(r => r.id)
  } else {
    selectedRegistrations.value = []
  }
}

// 处理单个选择变化
const handleSelectionChange = () => {
  selectAll.value = selectedRegistrations.value.length === registrations.value.length
}

// 清空选择
const clearSelection = () => {
  selectedRegistrations.value = []
  selectAll.value = false
}

// 当前操作的报名ID
const currentRegistrationId = ref(null)
const currentRegistration = ref({})

// 表单数据
const approveForm = reactive({
  status: '2',
  remark: ''
})

const registrationForm = reactive({
  userIds: [],
  activityId: '',
  remark: ''
})

const activities = ref([]) // 存储活动列表
const users = ref([]) // 存储用户列表

// 选中的活动
const selectedActivity = ref(null)

// 计算剩余人数
const remainingParticipants = computed(() => {
  if (!selectedActivity.value) return 0
  return selectedActivity.value.maxParticipants - selectedActivity.value.currentParticipants
})

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(total.value / pageSize.value)
})

// 获取报名统计数据
const fetchRegistrationStatistics = async () => {
  try {
    const result = await registrationStore.fetchRegistrationStatistics()
    if (result.success) {
      Object.assign(statisticsData, result.data)
    }
  } catch (error) {
    console.error('获取报名统计数据失败:', error)
  }
}

// 初始化
onMounted(() => {
  fetchRegistrations()
  fetchRegistrationStatistics()
  fetchActivities() // 获取活动列表
  fetchUsers() // 获取用户列表
  pageSizeLocal.value = pageSize.value
})

// 获取报名列表
const fetchRegistrations = async () => {
  const params = {
    activityTitle: searchForm.activityTitle,
    userName: searchForm.userName,
    status: searchForm.status ? parseInt(searchForm.status) : null
  }
  
  await registrationStore.fetchRegistrations(params)
  registrationStore.setCurrentPage(currentPage.value)
}

// 搜索报名
const searchRegistrations = () => {
  registrationStore.setCurrentPage(1)
  fetchRegistrations()
  ElMessage.success('搜索完成')
}

// 重置搜索表单
const resetSearch = () => {
  searchForm.activityTitle = ''
  searchForm.userName = ''
  searchForm.status = ''
  registrationStore.setCurrentPage(1)
  fetchRegistrations()
}

// 审核报名
const approveRegistration = (id) => {
  currentRegistrationId.value = id
  approveForm.status = '2'
  approveForm.remark = ''
  showApproveDialog.value = true
}

// 提交审核
const submitApprove = async () => {
  try {
    const result = await registrationStore.approveRegistration(
      currentRegistrationId.value,
      parseInt(approveForm.status),
      approveForm.remark
    )
    if (result.success) {
      ElMessage.success(result.message)
      closeApproveDialog()
      fetchRegistrationStatistics()
    }
  } catch (error) {
    console.error('审核报名失败:', error)
  }
}

// 取消报名
const cancelRegistration = async (id) => {
  ElMessageBox.confirm('确定要取消此报名吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const result = await registrationStore.cancelRegistration(id)
      if (result.success) {
        ElMessage.success(result.message)
        fetchRegistrationStatistics()
      }
    } catch (error) {
      console.error('取消报名失败:', error)
    }
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 获取活动列表
const fetchActivities = async () => {
  try {
    const response = await activityApi.getRegistrableActivities()
    if (response.code === 200) {
      activities.value = response.data || []
    }
  } catch (error) {
    console.error('获取活动列表失败:', error)
  }
}

// 处理活动选择变化
const handleActivityChange = () => {
  if (registrationForm.activityId) {
    selectedActivity.value = activities.value.find(a => a.id === parseInt(registrationForm.activityId))
    registrationForm.userIds = [] // 清空已选择的用户
  } else {
    selectedActivity.value = null
    registrationForm.userIds = []
  }
}

// 监听活动列表变化，更新选中的活动
watch(activities, () => {
  if (registrationForm.activityId) {
    selectedActivity.value = activities.value.find(a => a.id === parseInt(registrationForm.activityId))
  }
})

// 获取用户列表
const fetchUsers = async () => {
  try {
    const response = await userApi.getUserPage({ page: 1, size: 100, role: 'ELDERLY' })
    if (response.code === 200) {
      users.value = response.data.records || []
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 保存报名
const saveRegistration = async () => {
  if (!registrationForm.activityId) {
    ElMessage.warning('请选择活动')
    return
  }
  
  if (!registrationForm.userIds || registrationForm.userIds.length === 0) {
    ElMessage.warning('请选择至少一个用户')
    return
  }
  
  if (registrationForm.userIds.length > remainingParticipants.value) {
    ElMessage.warning(`选择人数超过剩余人数，最多只能选择 ${remainingParticipants.value} 人`)
    return
  }
  
  try {
    const activityId = parseInt(registrationForm.activityId)
    const remark = registrationForm.remark
    
    // 逐个为用户创建报名记录，以便处理重复报名等错误
    let successCount = 0
    const duplicateUserIds = []
    let otherErrorCount = 0
    const errorMessages = []
    
    for (const userId of registrationForm.userIds) {
      try {
        const data = {
          userId: parseInt(userId),
          activityId: activityId,
          remark: remark,
          registrationType: 1,
          operatorId: userStore.userId
        }
        const result = await registrationStore.createRegistration(data)
        if (result.success) {
          successCount++
        } else {
          if (result.message && result.message.includes('已报名')) {
            duplicateUserIds.push(String(userId))
          } else {
            otherErrorCount++
            errorMessages.push(result.message || '报名失败')
          }
        }
      } catch (error) {
        const errorMsg = error.response?.data?.message || error.message || '报名失败'
        if (errorMsg.includes('已报名')) {
          duplicateUserIds.push(String(userId))
        } else {
          otherErrorCount++
          errorMessages.push(errorMsg)
        }
      }
    }
    
    // 根据结果显示不同的提示信息
    if (successCount === registrationForm.userIds.length) {
      ElMessage.success(`成功为 ${successCount} 位用户报名`)
    } else if (successCount > 0) {
      let message = `成功为 ${successCount} 位用户报名`
      if (duplicateUserIds.length > 0) {
        const duplicateUserNames = users.value
          .filter(u => duplicateUserIds.includes(String(u.id)))
          .map(u => u.name)
          .join('、')
        message += `，重复报名的用户：${duplicateUserNames} 已报名`
      }
      if (otherErrorCount > 0) {
        message += `，${otherErrorCount} 位用户报名失败`
      }
      ElMessage.warning(message)
    } else {
      if (duplicateUserIds.length > 0) {
        const duplicateUserNames = users.value
          .filter(u => duplicateUserIds.includes(String(u.id)))
          .map(u => u.name)
          .join('、')
        ElMessage.warning(`重复报名的用户：${duplicateUserNames} 已报名`)
      } else {
        ElMessage.error('报名失败')
      }
    }
    
    closeAddRegistrationDialog()
    fetchRegistrationStatistics()
    fetchActivities() // 刷新活动列表以更新已报名人数和剩余人数
  } catch (error) {
    console.error('保存报名失败:', error)
    ElMessage.error('保存报名失败')
  }
}

// 关闭新增报名对话框
const closeAddRegistrationDialog = () => {
  showAddRegistrationDialog.value = false
  registrationForm.userIds = []
  registrationForm.activityId = ''
  registrationForm.remark = ''
  selectedActivity.value = null
}

// 查看报名详情
const viewRegistration = (registration) => {
  currentRegistration.value = registration
  showViewRegistrationDialog.value = true
}

// 关闭查看对话框
const closeViewRegistrationDialog = () => {
  showViewRegistrationDialog.value = false
  currentRegistration.value = {}
}

// 删除报名
const deleteRegistration = async (id) => {
  ElMessageBox.confirm('确定要删除此报名记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const result = await registrationStore.deleteRegistration(id)
      if (result.success) {
        ElMessage.success(result.message)
        fetchRegistrationStatistics()
      }
    } catch (error) {
      console.error('删除报名失败:', error)
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 关闭审核对话框
const closeApproveDialog = () => {
  showApproveDialog.value = false
  currentRegistrationId.value = null
  approveForm.status = '2'
  approveForm.remark = ''
}

// 格式化活动标题，每行最多8个字符
const formatActivityTitle = (title) => {
  if (!title) return ''
  
  const result = []
  for (let i = 0; i < title.length; i += 8) {
    result.push(title.substring(i, i + 8))
  }
  
  return result.join('<br>')
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

// 获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 1: return 'status-pending'
    case 2: return 'status-approved'
    case 3: return 'status-rejected'
    case 4: return 'status-cancelled'
    default: return ''
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 1: return '已报名'
    case 2: return '已通过'
    case 3: return '已拒绝'
    case 4: return '已取消'
    default: return '未知状态'
  }
}

// 获取分页页码
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

// 处理页码点击
const handlePageClick = (page) => {
  registrationStore.setCurrentPage(page)
  fetchRegistrations()
}

// 处理上一页
const handlePreviousPage = () => {
  if (currentPage.value > 1) {
    registrationStore.setCurrentPage(currentPage.value - 1)
    fetchRegistrations()
  }
}

// 处理下一页
const handleNextPage = () => {
  if (currentPage.value < totalPages.value) {
    registrationStore.setCurrentPage(currentPage.value + 1)
    fetchRegistrations()
  }
}

// 处理分页大小变化
const handlePageSizeChange = (newSize) => {
  registrationStore.setPageSize(newSize)
  fetchRegistrations()
}

// 分页处理
const handlePageChange = (page) => {
  if (page < 1 || page > totalPages.value || page === currentPage.value) {
    return
  }
  
  registrationStore.setCurrentPage(page)
  fetchRegistrations()
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
</script>

<style scoped>
/* 活动报名页面特定样式 */
/* 统计卡片样式 - 使用全局样式变量确保一致性 */
.system-stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--system-space-md);
  margin-bottom: var(--system-space-lg);
}

.system-stats-card {
  background-color: var(--system-bg-white);
  border-radius: var(--system-border-radius-lg);
  padding: var(--system-space-md);
  box-shadow: var(--system-shadow-sm);
  transition: all 0.3s ease;
}

.system-stats-card:hover {
  box-shadow: var(--system-shadow-md);
  transform: translateY(-2px);
}

.system-stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--system-space-md);
}

.system-stats-title {
  font-size: var(--system-font-size-sm);
  color: var(--system-text-secondary);
}

.system-stats-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.system-stats-value {
  font-size: var(--system-font-size-xl);
  font-weight: 600;
  color: var(--system-text-primary);
  margin-bottom: var(--system-space-xs);
}

.system-stats-change {
  font-size: var(--system-font-size-sm);
  color: var(--system-text-tertiary);
  display: flex;
  align-items: center;
}

.system-stats-change .el-icon {
  margin-right: 4px;
}

.system-stats-up {
  color: var(--system-color-success);
}

.system-stats-down {
  color: var(--system-color-danger);
}

/* 批量操作区域样式 */
.system-batch-actions {
  background-color: var(--system-bg-white);
  border-radius: var(--system-border-radius-lg);
  padding: var(--system-space-md);
  margin-bottom: var(--system-space-md);
  box-shadow: var(--system-shadow-sm);
  display: flex;
  justify-content: space-between;
  align-items: center;
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

/* 分页样式 */
.system-page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--system-font-size-sm);
  color: var(--system-text-secondary);
}

.system-page-size-selector .el-select {
  width: 80px;
}

/* 模态框遮罩层样式 */
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

/* 模态框样式优化 */
.system-modal {
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  animation: modalFadeIn 0.3s ease;
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
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.system-modal-title {
  font-size: 18px;
  font-weight: 600;
}

.system-modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.system-modal-body {
  padding: 16px;
}

.system-modal-footer {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.system-form-item {
  margin-bottom: 16px;
}

.system-form-label {
  font-weight: 500;
  margin-bottom: 8px;
}

.system-form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

.system-form-textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  resize: vertical;
}

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

.btn-warning {
  background-color: #faad14;
  color: white;
  border-color: #faad14;
}

.btn-warning:hover {
  background-color: #ffc53d;
  border-color: #ffc53d;
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

.btn-sm {
  padding: 2px 8px;
  font-size: 11px;
}

.status-tag {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.status-pending {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-approved {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-rejected {
  background-color: #fff2f0;
  color: #ff4d4f;
}

.status-cancelled {
  background-color: #f5f5f5;
  color: #999;
}

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
  flex-wrap: wrap;
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
  color: var(--system-text-disabled);
  font-weight: bold;
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

.system-pagination-loading {
  opacity: 0.6;
  pointer-events: none;
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

/* 活动标题多行显示样式 */
.system-table td:first-child {
  white-space: pre-wrap;
  word-wrap: break-word;
  word-break: break-all;
  line-height: 1.5;
  vertical-align: top;
}

/* 报名详情卡片样式 */
.registration-detail-container {
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
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-tag.status-approved {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-tag.status-rejected {
  background-color: #fff2f0;
  color: #ff4d4f;
}

.status-tag.status-cancelled {
  background-color: #f5f5f5;
  color: #999;
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

/* 用户多选框样式 */
.user-checkbox-container {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid var(--system-border-color);
  border-radius: var(--system-border-radius-md);
  padding: var(--system-space-sm);
  background-color: var(--system-bg-white);
}

.user-checkbox-item {
  padding: var(--system-space-sm);
  border-bottom: 1px solid var(--system-border-color-light);
  transition: background-color 0.2s;
}

.user-checkbox-item:last-child {
  border-bottom: none;
}

.user-checkbox-item:hover {
  background-color: var(--system-bg-hover);
}

.user-checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.user-checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
  margin-right: var(--system-space-sm);
  cursor: pointer;
  accent-color: var(--system-primary-color);
}

.user-checkbox-label input[type="checkbox"]:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.user-checkbox-text {
  font-size: var(--system-font-size-sm);
  color: var(--system-text-primary);
}

.user-checkbox-label input[type="checkbox"]:disabled + .user-checkbox-text {
  color: var(--system-text-disabled);
}

/* 活动信息样式 */
.activity-info {
  display: flex;
  gap: var(--system-space-md);
  margin-top: var(--system-space-sm);
  padding: var(--system-space-sm);
  background-color: var(--system-bg-light);
  border-radius: var(--system-border-radius-sm);
}

.activity-info-text {
  font-size: var(--system-font-size-sm);
  color: var(--system-text-secondary);
}

/* 已选择用户信息样式 */
.selected-users-info {
  display: flex;
  align-items: center;
  gap: var(--system-space-sm);
  margin-top: var(--system-space-sm);
  padding: var(--system-space-sm);
  background-color: var(--system-bg-light);
  border-radius: var(--system-border-radius-sm);
  font-size: var(--system-font-size-sm);
}

.warning-text {
  color: var(--system-color-warning);
  font-weight: 500;
}

</style>