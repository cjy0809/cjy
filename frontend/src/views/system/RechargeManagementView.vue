<template>
  <div class="system-page">
    <div class="system-page-title">
      <div class="system-page-title-text">
        <span class="system-page-title-icon">💳</span>
        用户充值管理
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="system-stats-container">
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">总充值金额</div>
          <div class="system-stats-icon" style="background-color: rgba(82, 196, 26, 0.1); color: var(--system-success-color);">
            <el-icon><Money /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">￥{{ formatAmount(statsData.totalAmount) }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">已支付</div>
          <div class="system-stats-icon" style="background-color: rgba(82, 196, 26, 0.1); color: var(--system-success-color);">
            <el-icon><Check /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.paidCount || 0 }}</div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="system-search-bar">
      <div class="system-search-form">
        <div class="system-search-item">
          <div class="system-form-label">用户</div>
          <input class="system-form-input" type="text" v-model="searchForm.userName" placeholder="请输入用户姓名">
        </div>
        <div class="system-search-item">
          <div class="system-form-label">充值状态</div>
          <select class="system-form-input" v-model="searchForm.status">
            <option value="">全部状态</option>
            <option value="0">待支付</option>
            <option value="1">已支付</option>
            <option value="2">已取消</option>
          </select>
        </div>
        <div class="system-search-actions">
          <el-button type="primary" @click="searchRecharges">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 充值列表 -->
    <div class="system-table-container">
      <table class="system-table">
        <thead>
          <tr>
            <th>订单号</th>
            <th>用户</th>
            <th>充值金额</th>
            <th>支付方式</th>
            <th>交易流水号</th>
            <th>充值状态</th>
            <th>充值时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="recharge in recharges" :key="recharge.id">
            <td>{{ recharge.orderNo }}</td>
            <td>{{ getUserName(recharge.userId) }}</td>
            <td>￥{{ recharge.amount }}</td>
            <td>
              <span v-if="recharge.paymentMethod === 'alipay'">支付宝</span>
              <span v-else>未知</span>
            </td>
            <td>{{ recharge.transactionId || '-' }}</td>
            <td>
              <span v-if="recharge.status === 0" class="status-tag status-unpaid">待支付</span>
              <span v-else-if="recharge.status === 1" class="status-tag status-paid">已支付</span>
              <span v-else-if="recharge.status === 2" class="status-tag status-cancelled">已取消</span>
              <span v-else-if="recharge.status === 3" class="status-tag status-failed">支付失败</span>
            </td>
            <td>{{ formatDateTime(recharge.payTime) || '-' }}</td>
            <td>
              <div class="action-btn-group">
                <el-button size="small" type="info" @click="viewRecharge(recharge)">查看</el-button>
                <el-button 
                  v-if="recharge.status === 0" 
                  size="small" 
                  type="danger" 
                  @click="cancelRecharge(recharge)">取消</el-button>
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

    <!-- 充值详情弹窗 -->
    <div v-if="dialogVisible" class="system-modal-overlay" @click.self="dialogVisible = false">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">充值详情</div>
          <button class="system-modal-close" @click="dialogVisible = false">×</button>
        </div>
        <div class="system-modal-body">
          <div class="recharge-detail-container">
            <!-- 充值信息卡片 -->
            <div class="detail-card">
              <div class="detail-card-header">
                <h3>充值信息</h3>
                <div class="status-tag" :class="getRechargeStatusClass(selectedRecharge.status)">
                  {{ getRechargeStatusText(selectedRecharge.status) }}
                </div>
              </div>
              
              <div class="detail-card-body">
                <div class="detail-row">
                  <div class="detail-label">订单号：</div>
                  <div class="detail-value">{{ selectedRecharge.orderNo }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">用户：</div>
                  <div class="detail-value">{{ getUserName(selectedRecharge.userId) }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">充值金额：</div>
                  <div class="detail-value amount">￥{{ selectedRecharge.amount }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">支付方式：</div>
                  <div class="detail-value">
                    <span v-if="selectedRecharge.paymentMethod === 'alipay'">支付宝</span>
                    <span v-else>未知</span>
                  </div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">交易流水号：</div>
                  <div class="detail-value">{{ selectedRecharge.transactionId || '-' }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">充值时间：</div>
                  <div class="detail-value">{{ formatDateTime(selectedRecharge.payTime) || '-' }}</div>
                </div>
                
              </div>
            </div>
            
            <!-- 备注信息卡片 -->
            <div v-if="selectedRecharge.remark" class="detail-card">
              <div class="detail-card-header">
                <h3>备注信息</h3>
              </div>
              <div class="detail-card-body">
                <div class="detail-row">
                  <div class="detail-label">备注：</div>
                  <div class="detail-value">{{ selectedRecharge.remark }}</div>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Money, Warning, Check } from '@element-plus/icons-vue'
import axios from 'axios'

const recharges = ref([])
const users = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const dialogVisible = ref(false)
const selectedRecharge = ref(null)
const jumpPage = ref('')

const searchForm = reactive({
  userName: '',
  status: ''
})

const statsData = reactive({
  totalAmount: 0,
  unpaidCount: 0,
  paidCount: 0
})

const totalPages = computed(() => {
  return Math.ceil(total.value / pageSize.value)
})

const fetchRecharges = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      status: searchForm.status || undefined
    }
    
    const response = await axios.get('/api/recharge/page', { params })
    if (response.data.code === 200) {
      recharges.value = response.data.data.records || []
      total.value = response.data.data.total || 0
    }
  } catch (error) {
    console.error('获取充值列表失败:', error)
    ElMessage.error('获取充值列表失败')
  } finally {
    loading.value = false
  }
}

const fetchUsers = async () => {
  try {
    const response = await axios.get('/api/user/page', {
      params: { page: 1, size: 1000 }
    })
    if (response.data.code === 200) {
      users.value = response.data.data.records || []
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

const fetchStats = async () => {
  try {
    const response = await axios.get('/api/recharge/page', {
      params: { page: 1, size: 1000 }
    })
    if (response.data.code === 200) {
      const allRecharges = response.data.data.records || []
      statsData.totalAmount = allRecharges
        .filter(r => r.status === 1)
        .reduce((sum, r) => sum + parseFloat(r.amount), 0)
      statsData.unpaidCount = allRecharges.filter(r => r.status === 0).length
      statsData.paidCount = allRecharges.filter(r => r.status === 1).length
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const searchRecharges = () => {
  currentPage.value = 1
  fetchRecharges()
}

const resetSearch = () => {
  searchForm.userName = ''
  searchForm.status = ''
  currentPage.value = 1
  fetchRecharges()
}

const getUserName = (userId) => {
  const user = users.value.find(u => u.id === userId)
  return user ? user.name : '未知用户'
}

const viewRecharge = (recharge) => {
  selectedRecharge.value = recharge
  dialogVisible.value = true
}

const cancelRecharge = async (recharge) => {
  try {
    await ElMessageBox.confirm(`确定要取消订单 "${recharge.orderNo}" 吗？`, '取消确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await axios.put(`/api/recharge/cancel/${recharge.id}`)
    if (response.data.code === 200) {
      ElMessage.success('取消成功')
      fetchRecharges()
      fetchStats()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消充值失败:', error)
      ElMessage.error('取消失败')
    }
  }
}

const handlePageChange = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchRecharges()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchRecharges()
}

const handleJumpToPage = () => {
  const page = parseInt(jumpPage.value)
  if (isNaN(page) || page < 1 || page > totalPages.value) {
    ElMessage.warning('请输入有效的页码')
    return
  }
  currentPage.value = page
  jumpPage.value = ''
  fetchRecharges()
}

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

const formatAmount = (amount) => {
  return parseFloat(amount || 0).toFixed(2)
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN')
}

const getRechargeStatusClass = (status) => {
  const statusMap = {
    0: 'status-unpaid',
    1: 'status-paid',
    2: 'status-cancelled',
    3: 'status-failed'
  }
  return statusMap[status] || ''
}

const getRechargeStatusText = (status) => {
  const statusMap = {
    0: '待支付',
    1: '已支付',
    2: '已取消',
    3: '支付失败'
  }
  return statusMap[status] || '未知状态'
}

onMounted(() => {
  fetchUsers()
  fetchRecharges()
  fetchStats()
})
</script>

<style scoped>
.system-page {
  padding: 20px;
}

.system-page-title {
  margin-bottom: 20px;
}

.system-page-title-text {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  display: flex;
  align-items: center;
}

.system-page-title-icon {
  margin-right: 10px;
}

.system-stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.system-stats-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.system-stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.system-stats-title {
  font-size: 14px;
  color: #666;
}

.system-stats-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.system-stats-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.system-search-bar {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.system-search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: flex-end;
}

.system-search-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.system-form-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.system-form-input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  min-width: 200px;
}

.system-search-actions {
  display: flex;
  gap: 10px;
}

.system-table-container {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.system-table {
  width: 100%;
  border-collapse: collapse;
}

.system-table th {
  background: #f5f7fa;
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #e4e7ed;
}

.system-table td {
  padding: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.action-btn-group {
  display: flex;
  gap: 8px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

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
  z-index: 2000;
}

.recharge-detail-container {
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

.detail-value.amount {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.status-unpaid {
  background-color: #fef0f0;
  color: #f56c6c;
}

.status-tag.status-paid {
  background-color: #f0f9ff;
  color: #67c23a;
}

.status-tag.status-cancelled {
  background-color: #f4f4f5;
  color: #909399;
}

.status-tag.status-failed {
  background-color: #fef0f0;
  color: #f56c6c;
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
