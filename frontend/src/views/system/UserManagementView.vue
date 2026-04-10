<template>
  <div class="system-page">
    <div class="system-page-title">
      <div class="system-page-title-text">
        <span class="system-page-title-icon">👥</span>
        用户管理
      </div>
      <div class="system-page-title-actions">
        <button v-if="isAdmin" class="system-button system-button-primary" @click="showAddUserDialog = true" :disabled="loading">
          <span class="system-button-icon">+</span>
          新增用户
        </button>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="system-loading-overlay">
      <div class="system-loading-spinner">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>数据加载中...</span>
      </div>
    </div>

    <!-- 用户统计卡片 -->
    <div class="system-stats-container">
      <div class="system-stats-card">
        <div class="system-stats-header">
          <h4 class="system-stats-title">用户总数</h4>
          <div class="system-stats-icon" style="background-color: rgba(24, 144, 255, 0.1); color: var(--system-primary-color);">👤</div>
        </div>
        <div class="system-stats-value">{{ totalUsers }}</div>
      </div>
      <div class="system-stats-card">
        <div class="system-stats-header">
          <h4 class="system-stats-title">活跃用户</h4>
          <div class="system-stats-icon" style="background-color: rgba(82, 196, 26, 0.1); color: var(--system-success-color);">✅</div>
        </div>
        <div class="system-stats-value">{{ activeUsers }}</div>
      </div>
      <div class="system-stats-card">
        <div class="system-stats-header">
          <h4 class="system-stats-title">管理员用户</h4>
          <div class="system-stats-icon" style="background-color: rgba(19, 194, 194, 0.1); color: var(--system-info-color);">🔧</div>
        </div>
        <div class="system-stats-value">{{ adminUsers }}</div>
      </div>
    </div>

    <!-- 角色标签页 -->
    <div class="system-role-tabs">
      <button 
        v-for="role in roleTabs" 
        :key="role.value"
        :class="['system-role-tab', { 'system-role-tab-active': currentRole === role.value }]"
        @click="switchRole(role.value)"
        :disabled="loading"
      >
        <span class="system-role-tab-icon">{{ role.icon }}</span>
        {{ role.label }}
        <span class="system-role-tab-count">{{ getRoleCount(role.value) }}</span>
      </button>
    </div>

    <!-- 搜索栏 -->
    <div class="system-search-bar">
      <div class="system-search-form">
        <div class="system-search-item">
          <div class="system-form-label">用户姓名</div>
          <input 
            type="text" 
            class="system-form-input" 
            v-model="searchForm.name"
            placeholder="请输入用户姓名"
          >
        </div>
        <div class="system-search-item">
          <div class="system-form-label">用户状态</div>
          <select class="system-form-input" v-model="searchForm.status">
            <option value="">全部</option>
            <option value="active">已激活</option>
            <option value="disabled">已禁用</option>
          </select>
        </div>
        <div class="system-search-actions">
          <button class="system-button system-button-primary" @click="searchUsers">搜索</button>
          <button class="system-button system-button-default" @click="resetSearch">重置</button>
        </div>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="system-table-container">
      <div class="system-table-header">
        <h3 class="system-table-title">用户列表</h3>
        <div class="system-table-actions">
          <button class="system-button system-button-ghost" @click="exportUsers">导出</button>
        </div>
      </div>
      <table class="system-table">
        <thead>
          <tr>
            <th>用户姓名</th>
            <!-- 只有在老年用户角色时显示紧急联系人和电话，否则显示用户账号 -->
            <template v-if="currentRole === 'elderly'">
              <th>紧急联系人</th>
              <th>紧急联系电话</th>
            </template>
            <template v-else>
              <th>用户账号</th>
            </template>
            <th>手机号</th>
            <th>用户状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.name }}</td>
            <!-- 根据当前角色动态显示不同的内容 -->
            <template v-if="currentRole === 'elderly'">
              <td>{{ user.emergencyContact || '-' }}</td>
              <td>{{ user.emergencyPhone || '-' }}</td>
            </template>
            <template v-else>
              <td>{{ user.username }}</td>
            </template>
            <td>{{ user.phone }}</td>
            <td>
              <span :class="['system-tag', `system-tag-${getUserStatusClass(user.status)}`]">
                {{ getUserStatusText(user.status) }}
              </span>
            </td>
            <td>{{ user.createTime }}</td>
            <td>
              <div class="system-table-actions">
                <el-button size="small" type="info" @click="viewUser(user)">查看</el-button>
                
                <!-- 编辑按钮：管理员可以编辑所有用户，工作人员只能编辑老年用户，但只有系统管理员才能编辑其他管理员 -->
                <el-button 
                  v-if="isAdmin || (isStaff && user.role === 'elderly')" 
                  size="small" 
                  type="primary" 
                  @click="editUser(user)"
                  :disabled="isAdmin && !isSystemAdmin && user.role === 'admin'"
                >编辑</el-button>
                
                <!-- 禁用/激活按钮：只有系统管理员才能操作其他管理员 -->
                <template v-if="isAdmin">
                  <el-button 
                    v-if="user.status === 'active'" 
                    size="small" 
                    type="warning" 
                    @click="disableUser(user)"
                    :disabled="!isSystemAdmin && user.role === 'admin'"
                  >禁用</el-button>
                  <el-button 
                    v-else-if="user.status === 'disabled'" 
                    size="small" 
                    type="success" 
                    @click="enableUser(user)"
                    :disabled="!isSystemAdmin && user.role === 'admin'"
                  >激活</el-button>
                </template>
                
                <!-- 删除按钮：只有系统管理员才能删除其他管理员 -->
                <el-button 
                  v-if="isAdmin" 
                  size="small" 
                  type="danger" 
                  @click="deleteUser(user)"
                  :disabled="!isSystemAdmin && user.role === 'admin'"
                >删除</el-button>
              </div>
            </td>
          </tr>
        </tbody>
        <tbody v-if="users.length === 0">
          <tr>
            <td :colspan="currentRole === 'elderly' ? 7 : 6" class="system-table-empty">暂无数据</td>
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
              @change="handleSizeChange"
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

    <!-- 查看用户详情对话框 -->
    <div v-if="showViewUserDialog" class="system-modal-overlay" @click.self="showViewUserDialog = false">
      <div class="system-modal system-modal-large">
        <div class="system-modal-header">
          <div class="system-modal-title">用户详情</div>
          <button class="system-modal-close" @click="showViewUserDialog = false">×</button>
        </div>
        <div class="system-modal-body">
          <div class="user-detail-container">
            <!-- 用户基本信息卡片 -->
            <div class="detail-card">
              <div class="detail-card-header">
                <h2>{{ currentUser.name }}</h2>
                <div class="status-tag" :class="getUserStatusClass(currentUser.role)">
                  {{ getUserRoleText(currentUser.role) }}
                </div>
              </div>
              
              <div class="detail-card-body">
                <div class="detail-row">
                  <div class="detail-label">用户账号：</div>
                  <div class="detail-value">{{ currentUser.username }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">手机号：</div>
                  <div class="detail-value">{{ currentUser.phone }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">性别：</div>
                  <div class="detail-value">{{ currentUser.gender === 0 ? '男' : '女' }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">用户状态：</div>
                  <div class="detail-value">
                    <span :class="['system-tag', `system-tag-${getUserStatusClass(currentUser.status)}`]">
                      {{ getUserStatusText(currentUser.status) }}
                    </span>
                  </div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">创建时间：</div>
                  <div class="detail-value">{{ currentUser.createTime }}</div>
                </div>
              </div>
            </div>

            <!-- 紧急联系人信息卡片（仅老年用户显示） -->
            <div v-if="currentUser.role === 'elderly'" class="detail-card">
              <div class="detail-card-header">
                <h3>紧急联系人信息</h3>
              </div>
              <div class="detail-card-body">
                <div class="detail-row">
                  <div class="detail-label">紧急联系人：</div>
                  <div class="detail-value">{{ currentUser.emergencyContact || '-' }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">紧急联系电话：</div>
                  <div class="detail-value">{{ currentUser.emergencyPhone || '-' }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增用户对话框 -->
    <div v-if="showAddUserDialog" class="system-modal-overlay">
      <div class="system-modal">
        <div class="system-modal-header">
          <h3 class="system-modal-title">新增用户</h3>
          <button class="system-modal-close" @click="showAddUserDialog = false">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">用户名 <span class="required">*</span></div>
              <input 
                type="text" 
                class="system-form-input" 
                v-model="userForm.username"
                placeholder="请输入用户名"
                :class="{ 'system-form-input-error': formErrors.username }"
                @input="validateUsername"
                @blur="validateUsername"
              >
              <div v-if="formErrors.username" class="system-form-error">{{ formErrors.username }}</div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">密码 <span class="required">*</span></div>
              <input 
                type="password" 
                class="system-form-input" 
                v-model="userForm.password"
                placeholder="请输入密码"
                :class="{ 'system-form-input-error': formErrors.password }"
                @input="validatePassword"
                @blur="validatePassword"
              >
              <div v-if="formErrors.password" class="system-form-error">{{ formErrors.password }}</div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">确认密码 <span class="required">*</span></div>
              <input 
                type="password" 
                class="system-form-input" 
                v-model="userForm.confirmPassword"
                placeholder="请再次输入密码"
                :class="{ 'system-form-input-error': formErrors.confirmPassword }"
                @input="validateConfirmPassword"
                @blur="validateConfirmPassword"
              >
              <div v-if="formErrors.confirmPassword" class="system-form-error">{{ formErrors.confirmPassword }}</div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">姓名 <span class="required">*</span></div>
              <input 
                type="text" 
                class="system-form-input" 
                v-model="userForm.name"
                placeholder="请输入姓名"
                :class="{ 'system-form-input-error': formErrors.name }"
                @input="validateName"
                @blur="validateName"
              >
              <div v-if="formErrors.name" class="system-form-error">{{ formErrors.name }}</div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">手机号 <span class="required">*</span></div>
              <input 
                type="text" 
                class="system-form-input" 
                v-model="userForm.phone"
                placeholder="请输入手机号"
                :class="{ 'system-form-input-error': formErrors.phone }"
                @input="validatePhone"
                @blur="validatePhone"
              >
              <div v-if="formErrors.phone" class="system-form-error">{{ formErrors.phone }}</div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">角色 <span class="required">*</span></div>
              <select 
                class="system-form-input" 
                v-model="userForm.role"
                :class="{ 'system-form-input-error': formErrors.role }"
                @change="validateRole"
                :disabled="!isAdmin || (isStaff && userForm.role !== 'elderly')"
              >
                <option value="">请选择角色</option>
                <option value="elderly">老人用户</option>
                <option value="staff">工作人员</option>
                <option v-if="isAdmin" value="admin">管理员</option>
              </select>
              <div v-if="formErrors.role" class="system-form-error">{{ formErrors.role }}</div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">性别</div>
              <select class="system-form-input" v-model="userForm.gender">
                <option value="0">男</option>
                <option value="1">女</option>
              </select>
            </div>
            
            <!-- 只有选择老人用户时才显示紧急联系人信息 -->
            <template v-if="userForm.role === 'elderly'">
              <div class="system-form-item">
                <div class="system-form-label">紧急联系人</div>
                <input 
                  type="text" 
                  class="system-form-input" 
                  v-model="userForm.emergencyContact"
                  placeholder="请输入紧急联系人姓名"
                >
              </div>
              
              <div class="system-form-item">
                <div class="system-form-label">紧急联系电话</div>
                <input 
                  type="text" 
                  class="system-form-input" 
                  v-model="userForm.emergencyPhone"
                  placeholder="请输入紧急联系电话"
                >
              </div>
            </template>
          </div>
        </div>
        <div class="system-modal-footer">
          <button class="system-button system-button-default" @click="closeAddUserDialog">取消</button>
          <button class="system-button system-button-primary" @click="saveNewUser" :disabled="submitting">
            {{ submitting ? '提交中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 编辑用户对话框 -->
    <div v-if="showEditUserDialog" class="system-modal-overlay">
      <div class="system-modal">
        <div class="system-modal-header">
          <h3 class="system-modal-title">编辑用户</h3>
          <button class="system-modal-close" @click="showEditUserDialog = false">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">用户名 <span class="required">*</span></div>
              <input 
                type="text" 
                class="system-form-input" 
                v-model="userForm.username"
                placeholder="请输入用户名"
                :class="{ 'system-form-input-error': formErrors.username }"
                @input="validateUsername"
                @blur="validateUsername"
              >
              <div v-if="formErrors.username" class="system-form-error">{{ formErrors.username }}</div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">密码</div>
              <input 
                type="password" 
                class="system-form-input" 
                v-model="userForm.password"
                placeholder="如不修改请留空"
                :class="{ 'system-form-input-error': formErrors.password }"
                @input="validatePassword"
                @blur="validatePassword"
              >
              <div v-if="formErrors.password" class="system-form-error">{{ formErrors.password }}</div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">姓名 <span class="required">*</span></div>
              <input 
                type="text" 
                class="system-form-input" 
                v-model="userForm.name"
                placeholder="请输入姓名"
                :class="{ 'system-form-input-error': formErrors.name }"
                @input="validateName"
                @blur="validateName"
              >
              <div v-if="formErrors.name" class="system-form-error">{{ formErrors.name }}</div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">手机号 <span class="required">*</span></div>
              <input 
                type="text" 
                class="system-form-input" 
                v-model="userForm.phone"
                placeholder="请输入手机号"
                :class="{ 'system-form-input-error': formErrors.phone }"
                @input="validatePhone"
                @blur="validatePhone"
              >
              <div v-if="formErrors.phone" class="system-form-error">{{ formErrors.phone }}</div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">角色 <span class="required">*</span></div>
              <select 
                class="system-form-input" 
                v-model="userForm.role"
                :class="{ 'system-form-input-error': formErrors.role }"
                @change="validateRole"
                :disabled="!isSystemAdmin && userForm.role === 'admin'"
              >
                <option value="">请选择角色</option>
                <option value="elderly">老人用户</option>
                <option value="staff">工作人员</option>
                <option v-if="isAdmin" value="admin">管理员</option>
              </select>
              <div v-if="formErrors.role" class="system-form-error">{{ formErrors.role }}</div>
              <div v-if="!isSystemAdmin && userForm.role === 'admin'" class="system-form-hint">只有系统管理员才能修改管理员角色</div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">性别</div>
              <select class="system-form-input" v-model="userForm.gender">
                <option value="0">男</option>
                <option value="1">女</option>
              </select>
            </div>
            
            <!-- 只有选择老人用户时才显示紧急联系人信息 -->
            <template v-if="userForm.role === 'elderly'">
              <div class="system-form-item">
                <div class="system-form-label">紧急联系人</div>
                <input 
                  type="text" 
                  class="system-form-input" 
                  v-model="userForm.emergencyContact"
                  placeholder="请输入紧急联系人姓名"
                >
              </div>
              
              <div class="system-form-item">
                <div class="system-form-label">紧急联系电话</div>
                <input 
                  type="text" 
                  class="system-form-input" 
                  v-model="userForm.emergencyPhone"
                  placeholder="请输入紧急联系电话"
                >
              </div>
            </template>
            
            <div class="system-form-item">
              <div class="system-form-label">状态</div>
              <select class="system-form-input" v-model="userForm.status" :disabled="!isAdmin">
                <option value="active">正常</option>
                <option value="disabled">禁用</option>
              </select>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <button class="system-button system-button-default" @click="closeUserDialog">取消</button>
          <button class="system-button system-button-primary" @click="saveUser" :disabled="submitting">
            {{ submitting ? '提交中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>


  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { userApi } from '@/api/user.js'
import { getCurrentInstance } from 'vue'
import { useUserStore } from '@/stores/user.js'

// 获取当前实例，用于消息提示
const { proxy } = getCurrentInstance()

// 获取用户store
const userStore = useUserStore()

// 获取当前用户角色
const currentUserRole = computed(() => userStore.userRole)
const isAdmin = computed(() => userStore.isAdmin)
const isSystemAdmin = computed(() => userStore.isSystemAdmin)
const isStaff = computed(() => userStore.isStaff)

// 搜索表单
const searchForm = reactive({
  name: '',
  status: ''
})

// 用户列表
const users = ref([])
const allUsers = ref([]) // 存储所有用户，用于角色分类统计
const total = ref(0)
const loading = ref(false)
const totalUsers = ref(0)
const activeUsers = ref(0)
const pendingUsers = ref(0)
const adminUsers = ref(0)

// 角色标签页数据
const roleTabs = ref([
  { value: 'admin', label: '管理员', icon: '🔧' },
  { value: 'staff', label: '工作人员', icon: '💼' },
  { value: 'elderly', label: '老人用户', icon: '👴' }
])

// 当前选中的角色
const currentRole = ref('admin') // 默认显示管理员角色

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const jumpPage = ref('')

// 对话框状态
const showAddUserDialog = ref(false)
const showEditUserDialog = ref(false)
const showViewUserDialog = ref(false)
const currentUser = ref({})

// 用户表单
const userForm = reactive({
  id: null,
  name: '',
  username: '',
  password: '',
  confirmPassword: '', // 添加确认密码字段
  role: '',
  phone: '',
  gender: '0', // 默认为男性
  status: 'active',
  emergencyContact: '',
  emergencyPhone: ''
})

// 表单验证错误
const formErrors = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  phone: '',
  role: ''
})

// 提交状态
const submitting = ref(false)

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(total.value / pageSize.value)
})

// 将数字状态码转换为字符串状态值
const convertStatusToString = (status) => {
  if (typeof status === 'string') return status // 如果已经是字符串，则直接返回
  
  const statusMap = {
    1: 'active',   // 已激活
    2: 'disabled',  // 已禁用
    
  }
  
  return statusMap[status] || 'unknown'
}

// 页面加载时获取用户列表
onMounted(() => {
  fetchUsers()
})

// 格式化日期为年月日
const formatDate = (dateString) => {
  if (!dateString) return ''
  // 移除T和时间部分，只保留年月日
  return dateString.split('T')[0]
}

// 计算统计数据
const calculateStats = (userList) => {
  // 从allUsers数组计算总数，而不是使用total.value
  totalUsers.value = allUsers.value.length
  activeUsers.value = allUsers.value.filter(user => user.status === 'active').length
  adminUsers.value = allUsers.value.filter(user => user.role?.toLowerCase() === 'admin').length
}

// 获取用户列表
const fetchUsers = async () => {
  console.log('开始获取用户列表，当前角色:', currentRole.value)
  loading.value = true
  try {
    // 使用当前选中的角色作为搜索参数
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      name: searchForm.name,
      role: currentRole.value,
      status: searchForm.status
    }
    
    // 确保role参数传递的是大写形式，与后端保持一致
    if (params.role) {
      // 根据系统设计文档，角色值应该是ELDERLY等大写形式
      if (params.role === 'elderly') params.role = 'ELDERLY'
      if (params.role === 'staff') params.role = 'STAFF'
      if (params.role === 'admin') params.role = 'ADMIN'
    }
    
    // 将状态字符串转换为数字，与后端保持一致
    if (params.status) {
      const statusMap = {
        'active': 1,    // 已激活
        'disabled': 2   // 已禁用
      }
      params.status = statusMap[params.status] || null
    }
    
    console.log('发送用户列表请求参数:', params)
    const response = await userApi.getUserPage(params)
    console.log('用户列表原始响应:', response)
    console.log('响应类型:', typeof response)
    console.log('响应code:', response?.code)
    console.log('响应data:', response?.data)
    console.log('响应data类型:', typeof response?.data)
    console.log('响应data.records:', response?.data?.records)
    
    const userList = response.data.records || []
    console.log('获取到用户数量:', userList.length)
    
    // 转换用户数据格式，适配前端展示，统一使用emergencyContact和emergencyPhone字段
    const formattedUsers = await Promise.all(userList.map(async user => {
      let emergencyContact = '';
      let emergencyPhone = '';
      
      // 如果是老年用户，直接从用户表中获取紧急联系人信息
      if (user.role === 'ELDERLY' || user.role === 'elderly') {
        emergencyContact = user.emergencyContact || user.emergency_contact || '';
        emergencyPhone = user.emergencyPhone || user.emergency_phone || '';
      } else {
        // 非老年用户也尝试从用户对象中获取紧急联系人信息（如果有）
        emergencyContact = user.emergencyContact || user.emergency_contact || '';
        emergencyPhone = user.emergencyPhone || user.emergency_phone || '';
      }
      
      return {
        id: user.id,
        name: user.name,
        username: user.username,
        role: user.role ? user.role.toLowerCase() : user.role,
        phone: user.phone || '',
        status: convertStatusToString(user.status), 
        avatar: user.avatar || '', // 添加头像字段
        // 设置紧急联系人相关字段，统一使用emergencyContact和emergencyPhone
        emergencyContact: emergencyContact,
        emergencyPhone: emergencyPhone,
        createTime: formatDate(user.createTime) // 格式化日期显示
      };
    }));
    
    // 按用户ID从小到大排序
    formattedUsers.sort((a, b) => a.id - b.id)
    
    users.value = formattedUsers
    // 当后端返回的total为0但实际有用户数据时，使用用户列表长度作为total
    total.value = response.data.total || formattedUsers.length
    
    // 始终获取完整的用户列表用于统计
    // 重置为第一页，获取完整的用户列表
    const allUsersResponse = await userApi.getUserPage({
      page: 1,
      size: 100, // 获取更多数据以确保统计准确性
      role: '',
      status: ''
    })
    
    const allUserRecords = allUsersResponse.data.records || []
    // 转换所有用户数据，统一使用emergencyContact和emergencyPhone字段
    allUsers.value = await Promise.all(allUserRecords.map(async user => {
      let emergencyContact = '';
      let emergencyPhone = '';
      
      // 如果是老年用户，直接从用户表中获取紧急联系人信息
      if (user.role === 'ELDERLY' || user.role === 'elderly') {
        emergencyContact = user.emergencyContact || user.emergency_contact || '';
        emergencyPhone = user.emergencyPhone || user.emergency_phone || '';
      } else {
        // 非老年用户也尝试从用户对象中获取紧急联系人信息（如果有）
        emergencyContact = user.emergencyContact || user.emergency_contact || '';
        emergencyPhone = user.emergencyPhone || user.emergency_phone || '';
      }
      
      return {
        id: user.id,
        name: user.name,
        username: user.username,
        role: user.role ? user.role.toLowerCase() : user.role,
        phone: user.phone || '',
        status: convertStatusToString(user.status), // 转换状态码为字符串
        avatar: user.avatar || '', // 添加头像字段
        // 设置紧急联系人相关字段，统一使用emergencyContact和emergencyPhone
        emergencyContact: emergencyContact,
        emergencyPhone: emergencyPhone,
        createTime: formatDate(user.createTime) // 格式化日期显示
      };
    }));
    
    // 按用户ID从小到大排序
    allUsers.value.sort((a, b) => a.id - b.id)
    
    // 计算统计数据
    calculateStats(formattedUsers)
  } catch (error) {
    console.error('获取用户列表失败:', error)
    proxy.$message.error('获取用户列表失败')
    users.value = []
    allUsers.value = []
    total.value = 0
    // 重置统计数据
    totalUsers.value = 0
    activeUsers.value = 0
    pendingUsers.value = 0
    adminUsers.value = 0
  } finally {
    loading.value = false
  }
}

// 切换角色标签
const switchRole = (role) => {
  currentRole.value = role
  searchForm.role = '' // 重置搜索栏的角色选择
  currentPage.value = 1 // 切换角色时重置为第一页
  fetchUsers()
}

// 获取特定角色的用户数量
const getRoleCount = (role) => {
  // 对于所有角色，从全部用户中过滤统计，不区分大小写
  return allUsers.value.filter(user => {
    const userRole = user.role || ''
    return userRole.toLowerCase() === role.toLowerCase()
  }).length
}

// 搜索用户
const searchUsers = () => {
  currentPage.value = 1
  fetchUsers()
  proxy.$message.success('搜索完成')
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.status = ''
  searchUsers()
}

// 导出用户
const exportUsers = () => {
  // 让用户选择导出格式
  ElMessageBox.confirm('请选择导出格式', '导出用户', {
    confirmButtonText: 'JSON',
    cancelButtonText: 'CSV',
    type: 'info',
    distinguishCancelAndClose: true
  }).then(() => {
    // 导出为JSON格式
    exportAsJSON()
  }).catch(action => {
    if (action === 'cancel') {
      // 导出为CSV格式
      exportAsCSV()
    } else {
      ElMessage.info('已取消导出')
    }
  })
}

// 导出为JSON格式
const exportAsJSON = () => {
  try {
    const dataStr = JSON.stringify(users.value, null, 2)
    const dataBlob = new Blob([dataStr], { type: 'application/json' })
    const url = URL.createObjectURL(dataBlob)
    const link = document.createElement('a')
    link.href = url
    link.download = `用户列表_${new Date().toISOString().slice(0, 10)}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    proxy.$message.success('JSON导出成功')
  } catch (error) {
    console.error('JSON导出失败:', error)
    proxy.$message.error('JSON导出失败')
  }
}

// 导出为CSV格式
const exportAsCSV = () => {
  try {
    // 定义CSV表头
    const headers = ['ID', '用户姓名', '用户账号', '用户角色', '手机号', '用户状态', '创建时间']
    
    // 定义CSV数据行
    const rows = users.value.map(user => [
      user.id,
      `"${user.name}"`,
      user.username,
      user.role,
      user.phone,
      user.status,
      user.createTime // 已经是格式化后的日期
    ])
    
    // 拼接CSV内容
    const csvContent = [
      headers.join(','),
      ...rows.map(row => row.join(','))
    ].join('\n')
    
    // 创建下载链接
    const dataBlob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(dataBlob)
    const link = document.createElement('a')
    link.href = url
    link.download = `用户列表_${new Date().toISOString().slice(0, 10)}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    proxy.$message.success('CSV导出成功')
  } catch (error) {
    console.error('CSV导出失败:', error)
    proxy.$message.error('CSV导出失败')
  }
}

// 查看用户详情
const viewUser = (user) => {
  console.log('查看用户详情，当前用户数据:', user)
  currentUser.value = user
  console.log('设置当前用户详情:', currentUser.value)
  showViewUserDialog.value = true
}

// 编辑用户
const editUser = (user) => {
  // 权限检查：管理员可以编辑所有用户，工作人员只能编辑老年用户
  if (!isAdmin.value && !(isStaff.value && user.role === 'elderly')) {
    ElMessage.warning('您没有权限编辑该用户')
    return
  }
  
  // 权限检查：只有系统管理员才能编辑其他管理员
  if (isAdmin.value && !isSystemAdmin.value && user.role === 'admin') {
    ElMessage.warning('只有系统管理员才能编辑其他管理员')
    return
  }
  
  console.log('编辑用户，当前用户数据:', user)
  
  Object.assign(userForm, {
    id: user.id,
    name: user.name,
    username: user.username,
    password: '', // 编辑时不预设密码
    role: user.role,
    phone: user.phone,
    status: user.status,
    // 确保紧急联系人信息正确映射，统一使用emergencyContact和emergencyPhone
    emergencyContact: user.emergencyContact || user.emergency_contact || '',
    emergencyPhone: user.emergencyPhone || user.emergency_phone || ''
  })
  
  console.log('编辑用户表单数据:', userForm)
  showEditUserDialog.value = true
}

// 禁用用户
const disableUser = async (user) => {
  // 权限检查：只有管理员可以禁用用户
  if (!isAdmin.value) {
    ElMessage.warning('您没有权限禁用用户')
    return
  }
  
  // 权限检查：只有系统管理员才能禁用其他管理员
  if (!isSystemAdmin.value && user.role === 'admin') {
    ElMessage.warning('只有系统管理员才能禁用其他管理员')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要禁用用户 "${user.name}" 吗？`,
      '禁用确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await userApi.updateUserStatus(user.id, 2) // 2表示已禁用状态
    proxy.$message.success('用户已禁用')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('禁用用户失败:', error)
      proxy.$message.error('禁用用户失败')
    }
  }
}

// 激活用户
const enableUser = async (user) => {
  // 权限检查：只有管理员可以激活用户
  if (!isAdmin.value) {
    ElMessage.warning('您没有权限激活用户')
    return
  }
  
  // 权限检查：只有系统管理员才能激活其他管理员
  if (!isSystemAdmin.value && user.role === 'admin') {
    ElMessage.warning('只有系统管理员才能激活其他管理员')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要激活用户 "${user.name}" 吗？`,
      '激活确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await userApi.updateUserStatus(user.id, 1) // 1表示已激活状态
    proxy.$message.success('用户已激活')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('激活用户失败:', error)
      proxy.$message.error('激活用户失败')
    }
  }
}

// 删除用户
const deleteUser = async (user) => {
  // 权限检查：只有管理员可以删除用户
  if (!isAdmin.value) {
    ElMessage.warning('您没有权限删除用户')
    return
  }
  
  // 权限检查：只有系统管理员才能删除其他管理员
  if (!isSystemAdmin.value && user.role === 'admin') {
    ElMessage.warning('只有系统管理员才能删除其他管理员')
    return
  }
  
  try {
    // 先获取用户关联数据统计
    const countResponse = await userApi.getUserRelatedDataCount(user.id)
    let relatedDataInfo = ''
    
    if (countResponse.code === 200 && countResponse.data) {
      const data = countResponse.data
      const dataItems = []
      
      if (data.healthRecords && data.healthRecords > 0) {
        dataItems.push(`健康记录 ${data.healthRecords} 条`)
      }
      if (data.registrations && data.registrations > 0) {
        dataItems.push(`活动报名记录 ${data.registrations} 条`)
      }
      if (data.reservations && data.reservations > 0) {
        dataItems.push(`场地预约记录 ${data.reservations} 条`)
      }
      if (data.serviceReservations && data.serviceReservations > 0) {
        dataItems.push(`服务预约记录 ${data.serviceReservations} 条`)
      }
      if (data.emergencyContacts && data.emergencyContacts > 0) {
        dataItems.push(`紧急联系人记录 ${data.emergencyContacts} 条`)
      }
      
      if (dataItems.length > 0) {
        relatedDataInfo = `\n\n该用户关联以下数据将被一并删除：\n${dataItems.join('\n')}`
      }
    }
    
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.name}" 吗？删除后该用户的所有数据将无法恢复。${relatedDataInfo}`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }
    )
    
    const response = await userApi.deleteUser(user.id)
    console.log('删除用户响应:', response)
    
    if (response.code === 200) {
      proxy.$message.success('用户及其关联数据删除成功')
      fetchUsers()
    } else {
      proxy.$message.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      proxy.$message.error('删除用户失败')
    }
  }
}

// 保存用户
const saveUser = async () => {
  // 权限检查：工作人员只能编辑老年用户，不能修改角色
  if (!isAdmin.value && userForm.role !== 'elderly') {
    ElMessage.warning('您没有权限修改该用户的角色')
    return
  }
  
  // 表单验证
  let isValid = true
  
  // 重置错误信息
  Object.keys(formErrors).forEach(key => {
    formErrors[key] = ''
  })
  
  // 验证用户名
  if (!userForm.username.trim()) {
    formErrors.username = '用户名不能为空'
    isValid = false
  }
  
  // 验证密码（编辑模式下密码是可选的）
  if (userForm.password && userForm.password.length < 6) {
    formErrors.password = '密码至少6个字符'
    isValid = false
  }
  
  // 验证姓名
  if (!userForm.name.trim()) {
    formErrors.name = '姓名不能为空'
    isValid = false
  }
  
  // 验证手机号
  if (!userForm.phone.trim()) {
    formErrors.phone = '手机号不能为空'
    isValid = false
  } else if (!/^1[3-9]\d{9}$/.test(userForm.phone)) {
    formErrors.phone = '手机号格式不正确'
    isValid = false
  }
  
  // 验证角色
  if (!userForm.role) {
    formErrors.role = '请选择角色'
    isValid = false
  }
  
  if (!isValid) {
    return
  }
  
  try {
    // 更新用户
    const updateData = {
      name: userForm.name,
      username: userForm.username,
      phone: userForm.phone,
      status: userForm.status ? (userForm.status === 'active' ? 1 : 2) : 1,
      emergencyContact: userForm.emergencyContact,
      emergencyPhone: userForm.emergencyPhone
    }
    
    // 只有管理员才能修改角色
    if (isAdmin.value && userForm.role) {
      if (userForm.role === 'elderly') updateData.role = 'ELDERLY'
      else if (userForm.role === 'staff') updateData.role = 'STAFF'
      else if (userForm.role === 'admin') updateData.role = 'ADMIN'
      else updateData.role = userForm.role.toUpperCase()
    }
    
    // 只有在用户输入了密码时才更新密码
    if (userForm.password) {
      updateData.password = userForm.password
    }
    
    console.log('准备更新的用户数据:', updateData)
    console.log('发送更新用户请求，用户ID:', userForm.id)
    const response = await userApi.updateUserById(userForm.id, updateData)
    console.log('更新用户响应:', response)
    console.log('响应类型:', typeof response)
    console.log('响应code:', response?.code)
    console.log('响应data:', response?.data)
    console.log('响应message:', response?.message)
    
    if (response && response.code === 200) {
      ElMessage.success('更新成功')
      closeUserDialog()
      console.log('准备刷新用户列表...')
      await fetchUsers()
      console.log('用户列表刷新完成')
    } else {
      console.log('更新失败，响应不符合预期')
      ElMessage.error(response?.message || '更新失败')
    }
  } catch (error) {
    console.error('保存用户失败:', error)
    console.error('错误详情:', {
      message: error.message,
      response: error.response,
      data: error.response?.data,
      status: error.response?.status,
      statusText: error.response?.statusText,
      headers: error.response?.headers,
      config: error.config
    })
    const errorMsg = error.response?.data?.message || error.message || '更新失败'
    ElMessage.error(errorMsg)
  }
}

// 关闭用户对话框
const closeUserDialog = () => {
  showAddUserDialog.value = false
  showEditUserDialog.value = false
  // 重置表单
  userForm.id = null
  userForm.name = ''
  userForm.username = ''
  userForm.password = ''
  userForm.confirmPassword = ''
  userForm.role = ''
  userForm.phone = ''
  userForm.gender = '0'
  userForm.status = 'pending' // 新用户默认为待审核状态
  userForm.emergencyContact = ''
  userForm.emergencyPhone = ''
  
  // 重置错误信息
  Object.keys(formErrors).forEach(key => {
    formErrors[key] = ''
  })
}

// 获取分页页码
const getPageNumbers = () => {
  const pages = []
  const current = currentPage.value
  const total = totalPages.value
  
  if (total <= 5) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 3) {
      pages.push(1, 2, 3, '...', total)
    } else if (current >= total - 2) {
      pages.push(1, '...', total - 2, total - 1, total)
    } else {
      pages.push(1, '...', current - 1, current, current + 1, '...', total)
    }
  }
  
  return pages
}

// 处理页码变化
const handlePageChange = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchUsers()
}

// 处理每页显示数量变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchUsers()
}

// 处理跳转到指定页
const handleJumpToPage = () => {
  const page = parseInt(jumpPage.value)
  if (isNaN(page) || page < 1 || page > totalPages.value) {
    ElMessage.warning('请输入有效的页码')
    return
  }
  currentPage.value = page
  jumpPage.value = ''
  fetchUsers()
}

// 获取用户角色文本
const getUserRoleText = (role) => {
  if (!role) return ''
  if (role.toLowerCase() === 'admin') return '管理员'
  if (role.toLowerCase() === 'staff') return '工作人员'
  if (role.toLowerCase() === 'elderly') return '老人用户'
  return role
}

// 获取用户状态文本
const getUserStatusText = (status) => {
  const statusMap = {
    active: '已激活',
    disabled: '已禁用'
  }
  return statusMap[status] || status
}

// 获取用户状态样式类
const getUserStatusClass = (status) => {
  const classMap = {
    active: 'success',
    disabled: 'error'
  }
  return classMap[status] || 'default'
}

// 表单验证
const validateForm = () => {
  // 重置错误信息
  Object.keys(formErrors).forEach(key => {
    formErrors[key] = ''
  })
  
  let isValid = true
  
  // 验证用户名
  if (!userForm.username.trim()) {
    formErrors.username = '用户名不能为空'
    isValid = false
  }
  
  // 验证密码
  if (!userForm.password) {
    formErrors.password = '密码不能为空'
    isValid = false
  } else if (userForm.password.length < 6) {
    formErrors.password = '密码至少6个字符'
    isValid = false
  }
  
  // 验证确认密码
  if (!userForm.confirmPassword) {
    formErrors.confirmPassword = '请确认密码'
    isValid = false
  } else if (userForm.password !== userForm.confirmPassword) {
    formErrors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }
  
  // 验证姓名
  if (!userForm.name.trim()) {
    formErrors.name = '姓名不能为空'
    isValid = false
  }
  
  // 验证手机号
  if (!userForm.phone.trim()) {
    formErrors.phone = '手机号不能为空'
    isValid = false
  } else if (!/^1[3-9]\d{9}$/.test(userForm.phone)) {
    formErrors.phone = '手机号格式不正确'
    isValid = false
  }
  
  // 验证角色
  if (!userForm.role) {
    formErrors.role = '请选择角色'
    isValid = false
  }
  
  return isValid
}

// 实时验证用户名
const validateUsername = () => {
  if (!userForm.username.trim()) {
    formErrors.username = '用户名不能为空'
  } else {
    formErrors.username = ''
  }
}

// 实时验证密码
const validatePassword = () => {
  if (userForm.password && userForm.password.length < 6) {
    formErrors.password = '密码至少6个字符'
  } else {
    formErrors.password = ''
  }
}

// 实时验证确认密码
const validateConfirmPassword = () => {
  if (!userForm.confirmPassword) {
    formErrors.confirmPassword = '请确认密码'
  } else if (userForm.password !== userForm.confirmPassword) {
    formErrors.confirmPassword = '两次输入的密码不一致'
  } else {
    formErrors.confirmPassword = ''
  }
}

// 实时验证姓名
const validateName = () => {
  if (!userForm.name.trim()) {
    formErrors.name = '姓名不能为空'
  } else {
    formErrors.name = ''
  }
}

// 实时验证手机号
const validatePhone = () => {
  if (!userForm.phone.trim()) {
    formErrors.phone = '手机号不能为空'
  } else if (!/^1[3-9]\d{9}$/.test(userForm.phone)) {
    formErrors.phone = '手机号格式不正确'
  } else {
    formErrors.phone = ''
  }
}

// 实时验证角色
const validateRole = () => {
  if (!userForm.role) {
    formErrors.role = '请选择角色'
  } else {
    formErrors.role = ''
  }
}

// 关闭新增用户对话框
const closeAddUserDialog = () => {
  showAddUserDialog.value = false
  // 重置表单
  userForm.name = ''
  userForm.username = ''
  userForm.password = ''
  userForm.confirmPassword = ''
  userForm.role = ''
  userForm.phone = ''
  userForm.gender = '0'
  userForm.status = 'active'
  userForm.emergencyContact = ''
  userForm.emergencyPhone = ''
  
  // 重置错误信息
  Object.keys(formErrors).forEach(key => {
    formErrors[key] = ''
  })
}

// 保存新用户
const saveNewUser = async () => {
  // 权限检查：只有管理员可以创建用户
  if (!isAdmin.value) {
    ElMessage.warning('您没有权限创建用户')
    return
  }
  
  // 表单验证
  if (!validateForm()) {
    return
  }
  
  submitting.value = true
  
  try {
    // 准备创建数据，只包含后端UserRegisterRequest类中定义的字段
    const createData = {
      name: userForm.name,
      username: userForm.username,
      password: userForm.password,
      phone: userForm.phone,
      gender: parseInt(userForm.gender),
      role: userForm.role ? (userForm.role === 'elderly' ? 'ELDERLY' : userForm.role === 'staff' ? 'STAFF' : userForm.role === 'admin' ? 'ADMIN' : userForm.role.toUpperCase()) : 'ELDERLY'
    }
    
    // 如果是老人用户且有紧急联系人信息，则添加到请求数据中
    if (userForm.role === 'elderly' && userForm.emergencyContact && userForm.emergencyPhone) {
      createData.emergencyContact = userForm.emergencyContact
      createData.emergencyPhone = userForm.emergencyPhone
    }
    
    console.log('发送用户注册请求:', createData)
    const response = await userApi.register(createData)
    console.log('用户注册响应:', response)
    
    if (response.code === 200) {
      ElMessage.success('用户创建成功')
      
      // 先保存当前创建的用户角色，用于刷新列表
      const createdUserRole = userForm.role
      
      // 关闭对话框
      closeAddUserDialog()
      
      // 刷新用户列表
      currentPage.value = 1
      currentRole.value = createdUserRole
      setTimeout(() => { 
        fetchUsers() 
      }, 500)
    } else {
      ElMessage.error(response.message || '创建失败')
    }
  } catch (error) {
    console.error('创建用户失败:', error)
    console.error('错误详情:', {
      message: error.message,
      response: error.response,
      data: error.response?.data,
      status: error.response?.status,
      statusText: error.response?.statusText,
      headers: error.response?.headers,
      config: error.config
    })
    
    // 处理特定错误
    if (error.response?.status === 400) {
      const errorMessage = error.response?.data?.message || '请求参数错误'
      if (errorMessage.includes('用户名')) {
        formErrors.username = errorMessage
      } else if (errorMessage.includes('手机号')) {
        formErrors.phone = errorMessage
      } else if (errorMessage.includes('密码')) {
        formErrors.password = errorMessage
      } else {
        ElMessage.error(errorMessage)
      }
    } else if (error.response?.status === 409) {
      ElMessage.error('用户名或手机号已存在')
    } else if (error.response?.status === 500) {
      ElMessage.error('服务器内部错误，请稍后重试')
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '创建失败')
    }
  } finally {
    submitting.value = false
  }
}

</script>

<style scoped>
/* 用户管理页面特定样式 */
.system-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.system-table-actions {
  display: flex;
  gap: var(--system-space-xs);
}

/* 加载状态样式 */
.system-loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(2px);
}

.system-loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--system-space-lg);
  background-color: var(--system-bg-white);
  border-radius: var(--system-border-radius-lg);
  box-shadow: var(--system-shadow-lg);
}

.system-loading-spinner .el-icon.is-loading {
  font-size: var(--system-font-size-2xl);
  color: var(--system-primary-color);
  margin-bottom: var(--system-space-sm);
  animation: rotate 2s linear infinite;
}

.system-loading-spinner span {
  font-size: var(--system-font-size-base);
  color: var(--system-text-secondary);
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 角色标签页样式 */
.system-role-tabs {
  display: flex;
  gap: var(--system-space-sm);
  margin-bottom: var(--system-space-lg);
  background-color: var(--system-bg-light);
  padding: var(--system-space-xs);
  border-radius: var(--system-border-radius-md);
  overflow-x: auto;
}

.system-role-tab {
  display: flex;
  align-items: center;
  gap: var(--system-space-xs);
  padding: var(--system-space-sm) var(--system-space-lg);
  background-color: var(--system-bg-white);
  border: 1px solid var(--system-border-color);
  border-radius: var(--system-border-radius-md);
  cursor: pointer;
  font-size: var(--system-font-size-sm);
  font-weight: 500;
  color: var(--system-text-secondary);
  transition: all 0.3s ease;
  white-space: nowrap;
  min-height: 40px;
}

.system-role-tab:hover {
  background-color: var(--system-bg-hover);
  border-color: var(--system-primary-color);
  color: var(--system-primary-color);
  transform: translateY(-1px);
  box-shadow: var(--system-shadow-sm);
}

.system-role-tab-active {
  background-color: var(--system-primary-color) !important;
  border-color: var(--system-primary-color) !important;
  color: var(--system-text-white) !important;
  box-shadow: var(--system-shadow-md);
}

.system-role-tab:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.system-role-tab:disabled:hover {
  background-color: var(--system-bg-white);
  border-color: var(--system-border-color);
  color: var(--system-text-secondary);
  box-shadow: none;
}

.system-role-tab-icon {
  font-size: var(--system-font-size-base);
}

.system-role-tab-count {
  background-color: rgba(255, 255, 255, 0.2);
  color: var(--system-text-white);
  font-size: var(--system-font-size-xs);
  padding: 2px 8px;
  border-radius: 12px;
  min-width: 20px;
  text-align: center;
  font-weight: 600;
}

.system-role-tab:not(.system-role-tab-active) .system-role-tab-count {
  background-color: var(--system-bg-light);
  color: var(--system-text-secondary);
}

.system-role-tab:hover:not(.system-role-tab-active) .system-role-tab-count {
  background-color: var(--system-primary-color);
  color: var(--system-text-white);
}

/* 按钮样式定义 */
.system-button {
  padding: var(--system-space-sm) var(--system-space-md);
  border: none;
  border-radius: var(--system-border-radius-md);
  font-size: var(--system-font-size-md);
  cursor: pointer;
  transition: all 0.3s ease;
}

.system-button-warning {
  background-color: var(--system-warning-color);
  color: #ffffff;
}

.system-button-warning:hover {
  background-color: #FFC53D;
}

.system-button-success {
  background-color: var(--system-success-color);
  color: #ffffff;
}

.system-button-success:hover {
  background-color: #73D13D;
}

/* 确保其他按钮样式也一致 */
.system-button-primary {
  background-color: var(--system-primary-color);
  color: #ffffff;
}

.system-button-primary:hover {
  background-color: #40A9FF;
}

.system-button-default {
  background-color: var(--system-bg-white);
  border: 1px solid var(--system-border-color);
  color: var(--system-text-primary);
}

.system-button-default:hover {
  border-color: var(--system-primary-color);
  color: var(--system-primary-color);
  background-color: var(--system-primary-light);
}

.system-button-ghost {
  background-color: transparent;
  border: 1px solid var(--system-border-color);
  color: var(--system-text-primary);
}

.system-button-ghost:hover {
  border-color: var(--system-primary-color);
  color: var(--system-primary-color);
  background-color: var(--system-primary-light);
}

.system-button-danger {
  background-color: var(--system-error-color);
  color: #ffffff;
}

.system-button-danger:hover {
  background-color: #FF4D4F;
}

.system-button-icon {
  margin-right: 4px;
}

/* 用户头像样式 */
.system-user-avatar {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
}

.system-avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.system-avatar-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background-color: #e6f7ff;
  color: #1890ff;
  font-weight: bold;
  font-size: 14px;
}

/* 空数据提示样式 */
.system-table-empty {
  text-align: center;
  color: #999;
  padding: 40px 0;
}

/* 用户详情卡片样式 */
.user-detail-container {
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

.status-tag.admin {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-tag.staff {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-tag.elderly {
  background-color: #fff7e6;
  color: #faad14;
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

/* 表单错误提示样式 */
.system-form-error {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}

.system-form-input-error {
  border-color: #ff4d4f !important;
}

.required {
  color: #ff4d4f;
}
</style>