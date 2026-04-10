<template>
  <div class="system-page">
    <div class="system-page-title">
      <div class="system-page-title-text">
        <span class="system-page-title-icon">🎉</span>
        社区活动
      </div>
      <button class="system-button system-button-primary" @click="openAddActivityDialog">
        新增活动
      </button>
    </div>

    <!-- 活动统计卡片 -->
    <div class="system-stats-container">
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">活动总数</div>
          <div class="system-stats-icon" style="background-color: rgba(24, 144, 255, 0.1); color: #1890ff;">
            <el-icon><List /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.totalActivities || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">进行中活动</div>
          <div class="system-stats-icon" style="background-color: rgba(82, 196, 26, 0.1); color: #52c41a;">
            <el-icon><User /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.activeActivities || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">未开始活动</div>
          <div class="system-stats-icon" style="background-color: rgba(250, 173, 20, 0.1); color: #faad14;">
            <el-icon><Calendar /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.upcomingActivities || 0 }}</div>
      </div>
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">已结束活动</div>
          <div class="system-stats-icon" style="background-color: rgba(150, 150, 150, 0.1); color: #969696;">
            <el-icon><Check /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.endedActivities || 0 }}</div>
      </div>
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">已取消活动</div>
          <div class="system-stats-icon" style="background-color: rgba(255, 77, 79, 0.1); color: #ff4d4f;">
            <el-icon><Close /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.canceledActivities || 0 }}</div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="system-search-bar">
      <div class="system-search-form">
        <div class="system-search-item">
          <div class="system-form-label">活动标题</div>
          <input class="system-form-input" type="text" v-model="searchForm.title" placeholder="请输入活动标题">
        </div>
        <div class="system-search-item">
          <div class="system-form-label">活动状态</div>
          <select class="system-form-input" v-model="searchForm.status">
            <option value="">全部状态</option>
            <option value="0">未开始</option>
            <option value="1">进行中</option>
            <option value="2">已结束</option>
            <option value="3">已取消</option>
          </select>
        </div>
        <div class="system-search-actions">
          <button class="system-button system-button-primary" @click="searchActivities">搜索</button>
          <button class="system-button system-button-default" @click="resetSearch">重置</button>
        </div>
      </div>
    </div>

    <!-- 活动列表 -->
    <div class="system-table-container">
      <table class="system-table">
        <thead>
          <tr>
            <th>活动标题</th>
            <th>开始时间</th>
            <th>结束时间</th>
            <th>活动地点</th>
            <th>参与人数</th>
            <th>活动状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="activity in activities" :key="activity.id">
            <td>{{ activity.title }}</td>
            <td>{{ formatDateTime(activity.startTime) }}</td>
            <td>{{ formatDateTime(activity.endTime) }}</td>
            <td>{{ activity.location }}</td>
            <td>
              <span :class="{ 'participants-full': activity.currentParticipants >= activity.maxParticipants }">
                {{ activity.currentParticipants }}/{{ activity.maxParticipants }}
              </span>
              <el-tag v-if="activity.currentParticipants >= activity.maxParticipants" type="danger" size="small" class="full-tag">
                已满
              </el-tag>
            </td>
            <td>
              <span v-if="activity.calculatedStatus === 0" class="status-tag status-upcoming">未开始</span>
              <span v-else-if="activity.calculatedStatus === 1" class="status-tag status-ongoing">进行中</span>
              <span v-else-if="activity.calculatedStatus === 2" class="status-tag status-finished">已结束</span>
              <span v-else-if="activity.calculatedStatus === 3" class="status-tag status-canceled">已取消</span>
            </td>
            <td>
              <div class="action-btn-group">
                <el-button size="small" type="info" @click="viewActivity(activity.id)">
                  查看
                </el-button>
                <el-button size="small" type="primary" @click="editActivity(activity.id)">
                  编辑
                </el-button>
                <el-button size="small" type="danger" @click="deleteActivity(activity.id)">
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
          <span>共 {{ filteredTotal }} 条记录，第 {{ currentPage }} / {{ totalPages }} 页</span>
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

    <!-- 新增活动对话框 - 弹窗形式 -->
    <div v-if="showAddActivityDialog" class="system-modal-overlay" @click.self="closeAddActivityDialog">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">新增活动</div>
          <button class="system-modal-close" @click="closeAddActivityDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">活动标题</div>
              <input class="system-form-input" type="text" v-model="newActivityForm.title" placeholder="请输入活动标题">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">开始时间</div>
              <el-date-picker
                v-model="newActivityForm.startTime"
                type="datetime"
                placeholder="请选择开始时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                step="30"
                style="width: 100%"
              />
            </div>
            <div class="system-form-item">
              <div class="system-form-label">结束时间</div>
              <el-date-picker
                v-model="newActivityForm.endTime"
                type="datetime"
                placeholder="请选择结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                step="30"
                style="width: 100%"
              />
            </div>
            <div class="system-form-item">
              <div class="system-form-label">活动地点</div>
              <select class="system-form-input" v-model="newActivityForm.venueId" @change="onVenueChange">
                <option value="">请选择场地</option>
                <option v-for="venue in venues" :key="venue.id" :value="venue.id">
                  {{ venue.name }} (容量: {{ venue.capacity }})
                </option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">最大参与人数</div>
              <input class="system-form-input" type="number" v-model="newActivityForm.maxParticipants" placeholder="请输入最大参与人数" readonly>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">活动内容</div>
              <textarea class="system-form-textarea" v-model="newActivityForm.content" placeholder="请输入活动内容" rows="4"></textarea>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">活动状态</div>
              <select class="system-form-input" v-model="newActivityForm.status">
                <option value="0">未开始</option>
                <option value="3">已取消</option>
              </select>
              <div class="system-form-hint">活动状态将根据开始和结束时间自动更新</div>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">报名状态</div>
              <select class="system-form-input" v-model="newActivityForm.registrationStatus">
                <option value="0">不可报名</option>
                <option value="1">可报名</option>
              </select>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <button class="btn btn-default" @click="closeAddActivityDialog">取消</button>
          <button class="btn btn-primary" @click="saveNewActivity">保存</button>
        </div>
      </div>
    </div>

    <!-- 编辑活动对话框 - 弹窗形式 -->
    <div v-if="showEditActivityDialog" class="system-modal-overlay" @click.self="closeActivityDialog">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">编辑活动</div>
          <button class="system-modal-close" @click="closeActivityDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">活动标题</div>
              <input class="system-form-input" type="text" v-model="activityForm.title" placeholder="请输入活动标题">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">开始时间</div>
              <el-date-picker
                v-model="activityForm.startTime"
                type="datetime"
                placeholder="请选择开始时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                step="30"
                style="width: 100%"
              />
            </div>
            <div class="system-form-item">
              <div class="system-form-label">结束时间</div>
              <el-date-picker
                v-model="activityForm.endTime"
                type="datetime"
                placeholder="请选择结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                step="30"
                style="width: 100%"
              />
            </div>
            <div class="system-form-item">
              <div class="system-form-label">活动地点</div>
              <input class="system-form-input" type="text" v-model="activityForm.location" placeholder="请输入活动地点">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">最大参与人数</div>
              <input class="system-form-input" type="number" v-model="activityForm.maxParticipants" placeholder="请输入最大参与人数">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">活动内容</div>
              <textarea class="system-form-textarea" v-model="activityForm.content" placeholder="请输入活动内容" rows="4"></textarea>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">活动状态</div>
              <select class="system-form-input" v-model="activityForm.status">
                <option value="0">未开始</option>
                <option value="3">已取消</option>
              </select>
              <div class="system-form-hint">活动状态将根据开始和结束时间自动更新</div>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">报名状态</div>
              <select class="system-form-input" v-model="activityForm.registrationStatus">
                <option value="0">不可报名</option>
                <option value="1">可报名</option>
              </select>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <button class="btn btn-default" @click="closeActivityDialog">取消</button>
          <button class="btn btn-primary" @click="saveActivity">保存</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { List, ArrowUp, ArrowDown, User, Check, Calendar, Close } from '@element-plus/icons-vue'
import { activityApi } from '@/api/activity'
import { venueApi } from '@/api/venue'
import { useActivityStore } from '@/stores/activity'

const router = useRouter()
const route = useRoute()
const activityStore = useActivityStore()

// 搜索表单
const searchForm = reactive({
  title: '',
  status: ''
})

// 活动列表 - 使用 store 中的数据
const allActivities = ref([]) // 存储所有活动数据
const total = computed(() => activityStore.total)
const loading = computed(() => activityStore.loading)

// 统计数据
const statsData = reactive({
  totalActivities: 0,
  activeActivities: 0,
  upcomingActivities: 0,
  endedActivities: 0,
  canceledActivities: 0,
  lastMonthTotalActivities: 0,
  lastMonthActiveActivities: 0,
  lastMonthUpcomingActivities: 0,
  lastMonthEndedActivities: 0,
  lastMonthCanceledActivities: 0
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const jumpPage = ref('')

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
  
  // 计算活动开始时间前一周
  const oneWeekBeforeStart = new Date(start.getTime() - 7 * 24 * 60 * 60 * 1000);
  
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

// 活动列表 - 使用 store 中的数据
const activities = computed(() => {
  let filteredActivities = [...allActivities.value];
  
  // 为每个活动计算动态状态
  filteredActivities = filteredActivities.map(activity => {
    const calculatedStatus = calculateActivityStatus(activity.startTime, activity.endTime, activity.status);
    return {
      ...activity,
      calculatedStatus,
      canRegister: canRegisterActivity(activity.startTime, activity.endTime, activity.currentParticipants, activity.maxParticipants)
    };
  });
  
  // 应用搜索过滤
  if (searchForm.title.trim()) {
    filteredActivities = filteredActivities.filter(activity => 
      activity.title.toLowerCase().includes(searchForm.title.toLowerCase().trim())
    );
  }
  
  if (searchForm.status !== '') {
    filteredActivities = filteredActivities.filter(activity => 
      activity.calculatedStatus === parseInt(searchForm.status)
    );
  }
  
  // 应用分页
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredActivities.slice(start, end);
})

// 对话框状态
const showEditActivityDialog = ref(false)
const showAddActivityDialog = ref(false)

// 活动表单
const activityForm = reactive({
  id: '',
  title: '',
  startTime: '',
  endTime: '',
  location: '',
  maxParticipants: '',
  content: '',
  status: 0,
  registrationStatus: 1
})

// 新增活动表单
const newActivityForm = reactive({
  title: '',
  startTime: '',
  endTime: '',
  location: '',
  venueId: '',
  maxParticipants: '',
  content: '',
  status: 0,
  registrationStatus: 1
})

// 场地列表
const venues = ref([])

// 计算总页数
const totalPages = computed(() => {
  let filteredActivities = [...allActivities.value];
  
  // 应用搜索过滤
  if (searchForm.title.trim()) {
    filteredActivities = filteredActivities.filter(activity => 
      activity.title.toLowerCase().includes(searchForm.title.toLowerCase().trim())
    );
  }
  
  if (searchForm.status !== '') {
    filteredActivities = filteredActivities.filter(activity => 
      activity.status === parseInt(searchForm.status)
    );
  }
  
  return Math.ceil(filteredActivities.length / pageSize.value)
})

// 计算过滤后的总记录数
const filteredTotal = computed(() => {
  let filteredActivities = [...allActivities.value];
  
  // 应用搜索过滤
  if (searchForm.title.trim()) {
    filteredActivities = filteredActivities.filter(activity => 
      activity.title.toLowerCase().includes(searchForm.title.toLowerCase().trim())
    );
  }
  
  if (searchForm.status !== '') {
    filteredActivities = filteredActivities.filter(activity => 
      activity.status === parseInt(searchForm.status)
    );
  }
  
  return filteredActivities.length
})

// 获取活动统计数据
const fetchActivityStats = async () => {
  try {
    const response = await activityApi.getActivityStatistics();
    
    if (response.code === 200 && response.data) {
      statsData.totalActivities = response.data.totalActivities || 0;
      statsData.activeActivities = response.data.activeActivities || 0;
      statsData.upcomingActivities = response.data.upcomingActivities || 0;
      statsData.endedActivities = response.data.endedActivities || 0;
      statsData.canceledActivities = response.data.canceledActivities || 0;
      statsData.lastMonthTotalActivities = response.data.lastMonthTotalActivities || 0;
      statsData.lastMonthActiveActivities = response.data.lastMonthActiveActivities || 0;
      statsData.lastMonthUpcomingActivities = response.data.lastMonthUpcomingActivities || 0;
      statsData.lastMonthEndedActivities = response.data.lastMonthEndedActivities || 0;
      statsData.lastMonthCanceledActivities = response.data.lastMonthCanceledActivities || 0;
    }
  } catch (error) {
    console.error('获取活动统计数据失败:', error);
  }
};

// 组件初始化函数
const initComponent = () => {
  allActivities.value = [];
  currentPage.value = 1;
  pageSize.value = 10;
  searchForm.title = '';
  searchForm.status = '';
};

// 组件挂载时的初始化逻辑
onMounted(() => {
  initComponent();
  fetchActivities();
  fetchActivityStats();
  fetchVenues();
});

// 更新统计数据
const updateStats = () => {
  fetchActivityStats();
};

// 获取活动列表
const fetchActivities = async () => {
  try {
    // 清空现有数据，确保显示最新数据
    allActivities.value = [];
    
    // 使用 store 的方法获取活动列表
    const result = await activityStore.fetchActivities({
      page: 1,
      size: 1000,
      title: '',
      status: ''
    });
    
    if (result.success) {
      // 直接从store获取最新数据，确保数据一致性
      allActivities.value = [...activityStore.activities].sort((a, b) => a.id - b.id);
      
      // 为每个活动获取实际参与者数量
      for (const activity of allActivities.value) {
        try {
          const participantsResponse = await activityApi.getActivityParticipants(activity.id);
          if (participantsResponse.code === 200 && participantsResponse.data) {
            // 只统计状态为已通过(2)和已报名(1)的参与者数量
            const approvedParticipants = participantsResponse.data.filter(p => p.status === 2 || p.status === 1);
            activity.currentParticipants = approvedParticipants.length;
          }
        } catch (error) {
          console.error(`获取活动 ${activity.id} 的参与者失败:`, error);
        }
      }
      
      // 更新统计数据
      updateStats();
    } else {
      // 处理响应成功但数据为空的情况
      allActivities.value = [];
      console.log('未获取到活动数据');
      
      // 重置统计数据
      updateStats();
    }
  } catch (error) {
    console.error('获取活动列表失败:', error);
    ElMessage.error('获取活动列表失败');
    
    // 重置统计数据
    allActivities.value = [];
    updateStats();
  }
};

// 获取场地列表
const fetchVenues = async () => {
  try {
    const response = await venueApi.getVenuePage({
      page: 1,
      size: 1000
    });
    
    if (response.code === 200) {
      venues.value = response.data.records || [];
    } else {
      ElMessage.error(response.message || '获取场地列表失败');
    }
  } catch (error) {
    console.error('获取场地列表失败:', error);
    ElMessage.error('获取场地列表失败');
  }
};

// 搜索活动
const searchActivities = () => {
  currentPage.value = 1;
  ElMessage.success('搜索完成');
};

// 重置搜索表单
const resetSearch = () => {
  searchForm.title = '';
  searchForm.status = '';
  currentPage.value = 1;
};

// 编辑活动
const editActivity = (id) => {
  const activity = activities.value.find(a => a.id === id);
  if (activity) {
    // 格式化时间字段为 ISO 格式字符串，确保后端能正确解析为 LocalDateTime
    const formatDateTime = (dateTime) => {
      if (!dateTime) return '';
      const date = new Date(dateTime);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');
      // 使用 ISO 格式：yyyy-MM-dd'T'HH:mm:ss
      return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
    };
    
    Object.assign(activityForm, {
      ...activity,
      startTime: formatDateTime(activity.startTime),
      endTime: formatDateTime(activity.endTime)
    });
    showEditActivityDialog.value = true;
  }
};

// 查看活动详情
const viewActivity = (id) => {
  router.push(`/system/activities/${id}`);
};

// 打开新增活动对话框
const openAddActivityDialog = () => {
  showAddActivityDialog.value = true;
};

// 关闭新增活动对话框
const closeAddActivityDialog = () => {
  showAddActivityDialog.value = false;
  Object.assign(newActivityForm, {
    title: '',
    startTime: '',
    endTime: '',
    location: '',
    venueId: '',
    organizerId: '',
    maxParticipants: '',
    content: '',
    status: 0,
    registrationStatus: 1
  });
};

// 场地选择变化处理
const onVenueChange = () => {
  if (newActivityForm.venueId) {
    const selectedVenue = venues.value.find(v => v.id === newActivityForm.venueId);
    if (selectedVenue) {
      newActivityForm.maxParticipants = selectedVenue.capacity;
      newActivityForm.location = selectedVenue.name;
    }
  } else {
    newActivityForm.maxParticipants = '';
    newActivityForm.location = '';
  }
};

// 保存新活动
const saveNewActivity = async () => {
  try {
    const formatDateTimeToISO = (dateTime) => {
      if (!dateTime) return '';
      return dateTime.replace(' ', 'T');
    };

    const activityData = {
      title: newActivityForm.title,
      startTime: formatDateTimeToISO(newActivityForm.startTime),
      endTime: formatDateTimeToISO(newActivityForm.endTime),
      location: newActivityForm.location,
      venueId: newActivityForm.venueId,
      maxParticipants: Number(newActivityForm.maxParticipants),
      content: newActivityForm.content,
      status: Number(newActivityForm.status),
      registrationStatus: Number(newActivityForm.registrationStatus)
    };

    const response = await activityApi.createActivity(activityData);
    if (response.code === 200) {
      ElMessage.success('创建成功');
      closeAddActivityDialog();
      fetchActivities();
    } else {
      ElMessage.error(response.message || '创建失败');
    }
  } catch (error) {
    console.error('创建活动失败:', error);
    const errorMsg = error.response?.data?.message || error.message || '创建失败';
    ElMessage.error(errorMsg);
  }
};

// 删除活动
const deleteActivity = (id) => {
  ElMessageBox.confirm('确定要删除此活动吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await activityApi.deleteActivity(id);
      ElMessage.success('删除成功');
      fetchActivities();
    } catch (error) {
      console.error('删除活动失败:', error);
      ElMessage.error('删除失败');
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 保存活动
const saveActivity = async () => {
  try {
    const formatDateTimeToISO = (dateTime) => {
      if (!dateTime) return '';
      return dateTime.replace(' ', 'T');
    };

    const activityData = {
      title: activityForm.title,
      startTime: formatDateTimeToISO(activityForm.startTime),
      endTime: formatDateTimeToISO(activityForm.endTime),
      location: activityForm.location,
      organizerId: activityForm.organizerId,
      maxParticipants: Number(activityForm.maxParticipants),
      content: activityForm.content,
      status: Number(activityForm.status),
      registrationStatus: Number(activityForm.registrationStatus)
    };

    const response = await activityApi.updateActivity(activityForm.id, activityData);
    if (response.code === 200) {
      ElMessage.success('更新成功');
      closeActivityDialog();
      fetchActivities();
    } else {
      ElMessage.error(response.message || '更新失败');
    }
  } catch (error) {
    console.error('保存活动失败:', error);
    const errorMsg = error.response?.data?.message || error.message || '更新失败';
    ElMessage.error(errorMsg);
  }
};

// 关闭活动对话框
const closeActivityDialog = () => {
  showEditActivityDialog.value = false;
  Object.assign(activityForm, {
    title: '',
    startTime: '',
    endTime: '',
    location: '',
    maxParticipants: '',
    content: '',
    status: 0,
    registrationStatus: 1
  });
};

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '';
  
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '';
  
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}`;
};

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
  currentPage.value = page;
};

// 处理上一页
const handlePreviousPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
};

// 处理下一页
const handleNextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
};

// 分页处理
const handlePageChange = (page) => {
  if (page < 1 || page > totalPages.value || page === currentPage.value) {
    return
  }
  
  currentPage.value = page
}

// 处理页面大小变化
const handlePageSizeChange = () => {
  currentPage.value = 1 // 重置到第一页
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

// 监听pageSize变化，重置到第一页
watch(pageSize, () => {
  currentPage.value = 1;
});

// 监听路由变化，当从活动详情页面返回时，刷新活动列表
watch(() => route.path, (newPath, oldPath) => {
  if (newPath === '/system/activities') {
    fetchActivities();
    fetchActivityStats();
  }
});
</script>

<style scoped>
/* 社区活动页面特定样式 */
/* 统计卡片样式 - 使用全局样式变量确保一致性 */
.system-stats-container {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
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
  margin-bottom: var(--system-space-sm);
}

.system-stats-title {
  font-size: 12px;
  color: var(--system-text-secondary);
}

.system-stats-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--system-border-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--system-font-size-lg);
}

.system-stats-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--system-text-primary);
  margin-bottom: var(--system-space-xs);
}

.system-stats-change {
  font-size: 11px;
  color: var(--system-text-tertiary);
  display: flex;
  align-items: center;
}

.system-stats-up {
  color: var(--system-success-color);
}

.system-stats-down {
  color: var(--system-error-color);
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

.activity-detail-value {
  padding: 8px 12px;
  background-color: #f5f5f5;
  border-radius: 4px;
  min-height: 20px;
  word-break: break-word;
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

.btn-info {
  background-color: #17a2b8;
  color: white;
  border-color: #17a2b8;
}

.btn-info:hover {
  background-color: #138496;
  border-color: #138496;
}

.status-tag {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
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

/* 参与人数已满样式 */
.participants-full {
  color: #ff4d4f;
  font-weight: 500;
}

.full-tag {
  margin-left: 8px;
  vertical-align: middle;
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
</style>



