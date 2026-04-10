<template>
  <div class="system-page">
    <div class="system-page-title">
      <div class="system-page-title-text">
        <span class="system-page-title-icon">💰</span>
        金额管理
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="system-stats-container">
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">总金额</div>
          <div class="system-stats-icon" style="background-color: rgba(82, 196, 26, 0.1); color: var(--system-success-color);">
            <el-icon><Money /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">￥{{ formatAmount(statsData.totalAmount) }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">待支付</div>
          <div class="system-stats-icon" style="background-color: rgba(245, 108, 108, 0.1); color: var(--system-error-color);">
            <el-icon><Warning /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.unpaidCount || 0 }}</div>
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
          <div class="system-form-label">支付状态</div>
          <select class="system-form-input" v-model="searchForm.status">
            <option value="">全部状态</option>
            <option value="0">待支付</option>
            <option value="1">已支付</option>
            <option value="2">已取消</option>
            <option value="3">已退款</option>
          </select>
        </div>
        <div class="system-search-actions">
          <el-button type="primary" @click="searchPayments">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 支付列表 -->
    <div class="system-table-container">
      <table class="system-table">
        <thead>
          <tr>
            <th>用户</th>
            <th>服务名称</th>
            <th>支付方式</th>
            <th>金额</th>
            <th>交易号</th>
            <th>支付状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="payment in payments" :key="payment.id">
            <td>{{ getUserName(payment.userId) }}</td>
            <td>{{ getServiceName(payment.serviceId) }}</td>
            <td>
              <span v-if="payment.paymentMethod === '1'">余额支付</span>
              <span v-else-if="payment.paymentMethod === '2'">支付宝</span>
              <span v-else>未知</span>
            </td>
            <td>￥{{ payment.amount }}</td>
            <td>{{ payment.transactionId || '-' }}</td>
            <td>
              <span v-if="payment.status === 0" class="status-tag status-unpaid">待支付</span>
              <span v-else-if="payment.status === 1" class="status-tag status-paid">已支付</span>
              <span v-else-if="payment.status === 2" class="status-tag status-cancelled">已取消</span>
              <span v-else-if="payment.status === 3" class="status-tag status-refunded">已退款</span>
            </td>
            <td>
              <div class="action-btn-group">
                <el-button size="small" type="info" @click="viewPayment(payment)">查看</el-button>
                <el-button 
                  v-if="payment.status === 0" 
                  size="small" 
                  type="danger" 
                  @click="cancelPayment(payment)"
                >
                  取消
                </el-button>
                <el-button 
                  v-if="payment.status === 2" 
                  size="small" 
                  type="danger" 
                  @click="deletePayment(payment)"
                >
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

    <!-- 查看支付详情对话框 -->
    <div v-if="showViewPaymentDialog" class="system-modal-overlay" @click.self="closeViewPaymentDialog">
      <div class="system-modal system-modal-large">
        <div class="system-modal-header">
          <div class="system-modal-title">支付详情</div>
          <button class="system-modal-close" @click="closeViewPaymentDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="payment-detail-container">
            <!-- 支付基本信息卡片 -->
            <div class="detail-card">
              <div class="detail-card-header">
                <h2>{{ getServiceName(currentPayment.serviceId) }}</h2>
                <div class="status-tag" :class="getStatusClass(currentPayment.status)">
                  {{ getStatusText(currentPayment.status) }}
                </div>
              </div>
              
              <div class="detail-card-body">
                <div class="detail-row">
                  <div class="detail-label">用户：</div>
                  <div class="detail-value">{{ getUserName(currentPayment.userId) }}</div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">支付方式：</div>
                  <div class="detail-value">
                    <span v-if="currentPayment.paymentMethod === '1'">余额支付</span>
                    <span v-else-if="currentPayment.paymentMethod === '2'">支付宝</span>
                    <span v-else>未知</span>
                  </div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">支付金额：</div>
                  <div class="detail-value" style="color: #52c41a; font-weight: bold; font-size: 18px;">
                    ￥{{ currentPayment.amount }}
                  </div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-label">交易号：</div>
                  <div class="detail-value">{{ currentPayment.transactionId || '-' }}</div>
                </div>
                
                <div v-if="currentPayment.remark" class="detail-row">
                  <div class="detail-label">备注：</div>
                  <div class="detail-value">{{ currentPayment.remark }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Money, Warning, Check } from '@element-plus/icons-vue'
import { paymentApi } from '@/api/payment'
import { userApi } from '@/api/user'
import { servicesApi } from '@/api/services'
import eventBus from '@/utils/eventBus'

export default {
  name: 'PaymentManagementView',
  components: {
    Document,
    Money,
    Warning,
    Check
  },
  setup() {
    const loading = ref(false)
    const payments = ref([])
    const users = ref([])
    const services = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const jumpPage = ref('')

    const showViewPaymentDialog = ref(false)
    const currentPayment = ref({})

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

    const formatAmount = (amount) => {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2)
    }

    const formatDateTime = (dateTime) => {
      if (!dateTime) return '-'
      return new Date(dateTime).toLocaleString('zh-CN')
    }

    const getUserName = (userId) => {
      const user = users.value.find(u => u.id === userId)
      return user ? user.name : '未知用户'
    }

    const getServiceName = (serviceId) => {
      const service = services.value.find(s => s.id === serviceId)
      return service ? service.name : '未知服务'
    }

    const getStatusClass = (status) => {
      switch (status) {
        case 0: return 'status-unpaid'
        case 1: return 'status-paid'
        case 2: return 'status-cancelled'
        case 3: return 'status-refunded'
        default: return ''
      }
    }

    const getStatusText = (status) => {
      switch (status) {
        case 0: return '待支付'
        case 1: return '已支付'
        case 2: return '已取消'
        case 3: return '已退款'
        default: return '未知状态'
      }
    }

    const fetchUsers = async () => {
      try {
        const response = await userApi.getAllUsers()
        const records = response.data?.records || response.records || response.data || []
        users.value = records
      } catch (error) {
        console.error('获取用户列表失败:', error)
      }
    }

    const fetchServices = async () => {
      try {
        const response = await servicesApi.getAllServiceItems()
        services.value = response.data || []
        console.log('服务列表加载成功:', services.value.length, '条')
      } catch (error) {
        console.error('获取服务列表失败:', error)
      }
    }

    const fetchPayments = async () => {
      try {
        loading.value = true
        const params = {
          pageNum: currentPage.value,
          pageSize: pageSize.value,
          userId: null,
          serviceId: null,
          status: searchForm.status !== '' ? parseInt(searchForm.status) : null
        }
        
        const response = await paymentApi.getPaymentHistory(params)
        
        const records = response.data?.records || []
        console.log('支付列表数据:', records)
        console.log('支付列表状态详情:', records.map(r => ({ id: r.id, status: r.status, amount: r.amount })))
        payments.value = records
        total.value = response.data?.total || 0
      } catch (error) {
        console.error('获取支付列表失败:', error)
        ElMessage.error('获取支付列表失败')
      } finally {
        loading.value = false
      }
    }

    const fetchPaymentStats = async () => {
      try {
        const params = {
          pageNum: 1,
          pageSize: 10000,
          userId: null,
          serviceId: null,
          status: null
        }
        
        const response = await paymentApi.getPaymentHistory(params)
        
        const allRecords = response.data?.records || []
        
        statsData.totalAmount = allRecords.filter(item => item.status === 1).reduce((sum, item) => sum + (parseFloat(item.amount) || 0), 0)
        statsData.unpaidCount = allRecords.filter(item => item.status === 0).length
        statsData.paidCount = allRecords.filter(item => item.status === 1).length
      } catch (error) {
        console.error('获取支付统计数据失败:', error)
      }
    }

    const searchPayments = () => {
      currentPage.value = 1
      fetchPayments()
    }

    const resetSearch = () => {
      searchForm.userName = ''
      searchForm.status = ''
      currentPage.value = 1
      fetchPayments()
    }

    const viewPayment = (payment) => {
      currentPayment.value = payment
      showViewPaymentDialog.value = true
    }

    const cancelPayment = async (payment) => {
      try {
        await ElMessageBox.confirm('确定要取消这个支付订单吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        loading.value = true
        const cancelResult = await request({
          url: '/api/payment/cancel',
          method: 'post',
          data: { bookingId: payment.bookingId }
        })
        
        if (cancelResult) {
          ElMessage.success('支付订单取消成功')
          await fetchPayments()
          await fetchPaymentStats()
          eventBus.emit('paymentDataChanged')
        } else {
          ElMessage.error('支付订单取消失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消支付订单失败:', error)
          ElMessage.error('取消支付订单失败')
        }
      } finally {
        loading.value = false
      }
    }

    const deletePayment = async (payment) => {
      try {
        await ElMessageBox.confirm('确定要删除这个支付记录吗？删除后无法恢复！', '提示', {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'error',
          confirmButtonClass: 'el-button--danger'
        })
        
        loading.value = true
        const deleteResult = await request({
          url: `/api/payment/${payment.id}`,
          method: 'delete'
        })
        
        if (deleteResult) {
          ElMessage.success('支付记录删除成功')
          await fetchPayments()
          await fetchPaymentStats()
          eventBus.emit('paymentDataChanged')
        } else {
          ElMessage.error('支付记录删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除支付记录失败:', error)
          ElMessage.error('删除支付记录失败')
        }
      } finally {
        loading.value = false
      }
    }

    const closeViewPaymentDialog = () => {
      showViewPaymentDialog.value = false
      currentPayment.value = {}
    }

    const handlePageChange = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
        fetchPayments()
      }
    }

    const handlePageSizeChange = () => {
      currentPage.value = 1
      fetchPayments()
    }

    const handleJumpToPage = () => {
      const page = parseInt(jumpPage.value)
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
        fetchPayments()
        jumpPage.value = ''
      } else {
        ElMessage.warning('请输入有效的页码')
      }
    }

    const getPageNumbers = () => {
      const pages = []
      const maxVisible = 5
      const total = totalPages.value
      
      if (total <= maxVisible) {
        for (let i = 1; i <= total; i++) {
          pages.push(i)
        }
      } else {
        if (currentPage.value <= 3) {
          for (let i = 1; i <= 4; i++) {
            pages.push(i)
          }
          pages.push('...')
          pages.push(total)
        } else if (currentPage.value >= total - 2) {
          pages.push(1)
          pages.push('...')
          for (let i = total - 3; i <= total; i++) {
            pages.push(i)
          }
        } else {
          pages.push(1)
          pages.push('...')
          for (let i = currentPage.value - 1; i <= currentPage.value + 1; i++) {
            pages.push(i)
          }
          pages.push('...')
          pages.push(total)
        }
      }
      
      return pages
    }

    watch(pageSize, () => {
      currentPage.value = 1
      fetchPayments()
    })

    const handleBookingDataChanged = async () => {
      console.log('收到预约数据变更事件，刷新支付数据')
      await fetchPayments()
      await fetchPaymentStats()
    }

    const handlePaymentDataChanged = async () => {
      console.log('收到支付数据变更事件，刷新支付数据')
      await fetchPayments()
      await fetchPaymentStats()
    }

    onMounted(async () => {
      await fetchUsers()
      await fetchServices()
      await fetchPayments()
      await fetchPaymentStats()
      
      eventBus.on('bookingDataChanged', handleBookingDataChanged)
      eventBus.on('paymentDataChanged', handlePaymentDataChanged)
    })

    onUnmounted(() => {
      eventBus.off('bookingDataChanged', handleBookingDataChanged)
      eventBus.off('paymentDataChanged', handlePaymentDataChanged)
    })

    return {
      loading,
      payments,
      users,
      services,
      total,
      currentPage,
      pageSize,
      jumpPage,
      showViewPaymentDialog,
      currentPayment,
      searchForm,
      statsData,
      totalPages,
      formatAmount,
      formatDateTime,
      getUserName,
      getServiceName,
      getStatusClass,
      getStatusText,
      searchPayments,
      resetSearch,
      viewPayment,
      cancelPayment,
      deletePayment,
      closeViewPaymentDialog,
      handlePageChange,
      handlePageSizeChange,
      handleJumpToPage,
      getPageNumbers
    }
  }
}
</script>

<style scoped>
/* 使用系统页面通用样式 */
/* .system-page {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
} */

.system-page-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.system-page-title-text {
  display: flex;
  align-items: center;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.system-page-title-icon {
  margin-right: 10px;
  font-size: 28px;
}

.system-stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.system-stats-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
}

.system-stats-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
}

.system-search-bar {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.system-search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: flex-end;
}

.system-search-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.system-form-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.system-form-input {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  color: #333;
  transition: all 0.3s;
}

.system-form-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.system-search-actions {
  display: flex;
  gap: 10px;
}

.system-table-container {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.system-table {
  width: 100%;
  border-collapse: collapse;
}

.system-table thead {
  background-color: #fafafa;
}

.system-table th {
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #e8e8e8;
}

.system-table td {
  padding: 12px;
  border-bottom: 1px solid #e8e8e8;
  color: #666;
}

.system-table tbody tr:hover {
  background-color: #f5f5f5;
}

.action-btn-group {
  display: flex;
  gap: 8px;
}

.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-unpaid {
  background-color: #fff1f0;
  color: #f5222d;
  border: 1px solid #ffa39e;
}

.status-paid {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-cancelled {
  background-color: #f5f5f5;
  color: #8c8c8c;
  border: 1px solid #d9d9d9;
}

.status-refunded {
  background-color: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

.system-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e8e8e8;
}

.system-pagination-loading {
  opacity: 0.5;
  pointer-events: none;
}

.system-pagination-info {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 14px;
  color: #666;
}

.system-pagination-controls {
  display: flex;
  align-items: center;
  gap: 5px;
}

.system-pagination-btn {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  background-color: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.system-pagination-btn:hover:not(.system-pagination-btn-disabled) {
  border-color: #1890ff;
  color: #1890ff;
}

.system-pagination-btn-active {
  background-color: #1890ff;
  color: #fff;
  border-color: #1890ff;
}

.system-pagination-btn-disabled {
  background-color: #f5f5f5;
  color: #d9d9d9;
  cursor: not-allowed;
}

.system-pagination-ellipsis {
  padding: 6px 12px;
  color: #999;
}

.system-pagination-jump {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-left: 15px;
}

.system-pagination-jump input {
  width: 50px;
  padding: 6px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  text-align: center;
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
  z-index: 1000;
}

.system-modal {
  background-color: #fff;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.system-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e8e8e8;
}

.system-modal-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.system-modal-close {
  background: none;
  border: none;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.system-modal-close:hover {
  color: #333;
}

.system-modal-body {
  padding: 20px;
}

.system-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px;
  border-top: 1px solid #e8e8e8;
}

.system-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.system-form-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.value-container {
  padding: 8px 12px;
  background-color: #f5f5f5;
  border-radius: 4px;
  color: #333;
  font-size: 14px;
}

/* 支付详情卡片样式 */
.payment-detail-container {
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

.status-tag.status-paid {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-tag.status-cancelled {
  background-color: #f5f5f5;
  color: #8c8c8c;
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