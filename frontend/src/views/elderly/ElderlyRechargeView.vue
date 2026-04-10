<template>
  <div class="elderly-page">
    <div class="elderly-card">
      <div class="elderly-card-header">
        <h2 class="elderly-card-title">
          <span class="elderly-card-icon">💳</span>
          充值记录
        </h2>
      </div>
      <div class="elderly-card-body">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p class="loading-text">正在加载充值记录...</p>
        </div>

        <!-- 充值记录列表 -->
        <div v-else-if="recharges.length > 0" class="recharge-list">
          <div 
            v-for="recharge in recharges" 
            :key="recharge.id" 
            class="recharge-item"
          >
            <div class="recharge-header">
              <div class="recharge-info">
                <span class="recharge-amount">￥{{ formatAmount(recharge.amount) }}</span>
                <span class="recharge-date">{{ formatDateTime(recharge.createTime) }}</span>
              </div>
              <div class="recharge-status" :class="getStatusClass(recharge.status)">
                {{ getStatusText(recharge.status) }}
              </div>
            </div>
            
            <div class="recharge-details">
              <div class="detail-row">
                <span class="detail-label">订单号：</span>
                <span class="detail-value">{{ recharge.orderNo }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">支付方式：</span>
                <span class="detail-value">{{ getPaymentMethodText(recharge.paymentMethod) }}</span>
              </div>
              <div v-if="recharge.transactionId" class="detail-row">
                <span class="detail-label">交易流水号：</span>
                <span class="detail-value">{{ recharge.transactionId }}</span>
              </div>
              <div v-if="recharge.remark" class="detail-row">
                <span class="detail-label">备注：</span>
                <span class="detail-value">{{ recharge.remark }}</span>
              </div>
              <div v-if="recharge.payTime" class="detail-row">
                <span class="detail-label">支付时间：</span>
                <span class="detail-value">{{ formatDateTime(recharge.payTime) }}</span>
              </div>
            </div>

            <div v-if="recharge.status === 0" class="recharge-actions">
              <button 
                class="elderly-button elderly-button-primary" 
                @click="handlePay(recharge)"
              >
                立即支付
              </button>
              <button 
                class="elderly-button elderly-button-default" 
                @click="handleCancel(recharge)"
              >
                取消订单
              </button>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination-container" v-if="totalPages > 1">
            <button 
              class="pagination-btn"
              :disabled="currentPage <= 1"
              @click="handlePageChange(currentPage - 1)"
            >
              上一页
            </button>
            <span class="pagination-info">
              第 {{ currentPage }} / {{ totalPages }} 页
            </span>
            <button 
              class="pagination-btn"
              :disabled="currentPage >= totalPages"
              @click="handlePageChange(currentPage + 1)"
            >
              下一页
            </button>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-icon">💳</div>
          <p class="empty-text">暂无充值记录</p>
          <button class="elderly-button elderly-button-primary" @click="goToProfile">
            去充值
          </button>
        </div>
      </div>
    </div>

    <!-- 充值详情弹窗 -->
    <div v-if="dialogVisible" class="modal-overlay" @click.self="dialogVisible = false">
      <div class="modal">
        <div class="modal-header">
          <h3 class="modal-title">充值详情</h3>
          <button class="modal-close" @click="dialogVisible = false">×</button>
        </div>
        <div class="modal-body">
          <div class="detail-section">
            <h4 class="detail-section-title">订单信息</h4>
            <div class="detail-row">
              <span class="detail-label">订单号：</span>
              <span class="detail-value">{{ selectedRecharge.orderNo }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">充值金额：</span>
              <span class="detail-value amount">￥{{ formatAmount(selectedRecharge.amount) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">订单状态：</span>
              <span 
                class="detail-value status-badge" 
                :class="getStatusClass(selectedRecharge.status)"
              >
                {{ getStatusText(selectedRecharge.status) }}
              </span>
            </div>
          </div>
          
          <div class="detail-section">
            <h4 class="detail-section-title">支付信息</h4>
            <div class="detail-row">
              <span class="detail-label">支付方式：</span>
              <span class="detail-value">{{ getPaymentMethodText(selectedRecharge.paymentMethod) }}</span>
            </div>
            <div v-if="selectedRecharge.transactionId" class="detail-row">
              <span class="detail-label">交易流水号：</span>
              <span class="detail-value">{{ selectedRecharge.transactionId }}</span>
            </div>
            <div v-if="selectedRecharge.payTime" class="detail-row">
              <span class="detail-label">支付时间：</span>
              <span class="detail-value">{{ formatDateTime(selectedRecharge.payTime) }}</span>
            </div>
          </div>
          
          <div v-if="selectedRecharge.remark" class="detail-section">
            <h4 class="detail-section-title">备注信息</h4>
            <div class="detail-row">
              <span class="detail-value">{{ selectedRecharge.remark }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

export default {
  name: 'ElderlyRechargeView',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const recharges = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const dialogVisible = ref(false)
    const selectedRecharge = ref(null)

    const totalPages = ref(1)

    const fetchRecharges = async () => {
      loading.value = true
      try {
        const params = {
          page: currentPage.value,
          size: pageSize.value
        }
        
        const response = await axios.get('/api/recharge/page', { params })
        if (response.data.code === 200) {
          recharges.value = response.data.data.records || []
          total.value = response.data.data.total || 0
          totalPages.value = Math.ceil(total.value / pageSize.value)
        }
      } catch (error) {
        console.error('获取充值记录失败:', error)
        ElMessage.error('获取充值记录失败')
      } finally {
        loading.value = false
      }
    }

    const formatAmount = (amount) => {
      return parseFloat(amount || 0).toFixed(2)
    }

    const formatDateTime = (dateTime) => {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    const getStatusText = (status) => {
      const statusMap = {
        0: '待支付',
        1: '已支付',
        2: '已取消',
        3: '支付失败'
      }
      return statusMap[status] || '未知状态'
    }

    const getStatusClass = (status) => {
      const classMap = {
        0: 'status-pending',
        1: 'status-success',
        2: 'status-cancelled',
        3: 'status-failed'
      }
      return classMap[status] || ''
    }

    const getPaymentMethodText = (method) => {
      const methodMap = {
        'alipay': '支付宝'
      }
      return methodMap[method] || '未知'
    }

    const handlePay = async (recharge) => {
      try {
        await ElMessageBox.confirm(
          `即将跳转到支付宝支付页面，充值金额：￥${formatAmount(recharge.amount)}`,
          '确认支付',
          {
            confirmButtonText: '去支付',
            cancelButtonText: '取消',
            type: 'info'
          }
        )
        
        ElMessage.info('正在获取支付链接...')
        
        const response = await axios.get(`/api/recharge/pay/${recharge.id}`)
        console.log('支付接口返回数据:', response.data)
        if (response.data.code === 200) {
          const formHtml = response.data.data.formHtml
          console.log('支付表单HTML:', formHtml)
          if (formHtml) {
            const newWindow = window.open('', '_blank')
            if (newWindow) {
              newWindow.document.write(formHtml)
              newWindow.document.close()
              ElMessage.success('已跳转到支付宝支付页面')
              
              let pollCount = 0
              const maxPollCount = 60
              
              const checkPaymentStatus = setInterval(async () => {
                pollCount++
                if (pollCount > maxPollCount) {
                  clearInterval(checkPaymentStatus)
                  ElMessage.warning('支付状态检查超时，请手动刷新页面查看')
                  if (newWindow && !newWindow.closed) {
                    newWindow.close()
                  }
                  return
                }
                
                try {
                  const statusResponse = await axios.get(`/api/recharge/${recharge.id}`)
                  if (statusResponse && statusResponse.data && statusResponse.data.data && statusResponse.data.data.status === 1) {
                    clearInterval(checkPaymentStatus)
                    if (newWindow && !newWindow.closed) {
                      newWindow.close()
                    }
                    ElMessage.success('充值成功！')
                    fetchRecharges()
                  }
                } catch (error) {
                  console.error('检查充值状态失败:', error)
                }
              }, 3000)
              
              setTimeout(() => {
                clearInterval(checkPaymentStatus)
                if (newWindow && !newWindow.closed) {
                  newWindow.close()
                }
              }, 180000)
            } else {
              ElMessage.error('无法打开新窗口，请检查浏览器弹窗拦截设置')
            }
          } else {
            ElMessage.error('获取支付链接失败')
          }
        } else {
          ElMessage.error(response.data.message || '获取支付链接失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('获取支付链接失败:', error)
          ElMessage.error('获取支付链接失败')
        }
      }
    }

    const handleCancel = (recharge) => {
      ElMessageBox.confirm(
        `确定要取消订单 "${recharge.orderNo}" 吗？`,
        '取消确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          const response = await axios.put(`/api/recharge/cancel/${recharge.id}`)
          if (response.data.code === 200) {
            ElMessage.success('取消成功')
            fetchRecharges()
          }
        } catch (error) {
          console.error('取消订单失败:', error)
          ElMessage.error('取消失败')
        }
      }).catch(() => {
      })
    }

    const handlePageChange = (page) => {
      if (page < 1 || page > totalPages.value) return
      currentPage.value = page
      fetchRecharges()
    }

    const goToProfile = () => {
      router.push('/elderly/profile')
    }

    onMounted(() => {
      const route = router.currentRoute.value
      if (route.query.payment === 'success') {
        ElMessage.success('支付成功！')
        
        if (window.opener) {
          window.opener.location.reload()
          window.close()
        } else {
          router.replace({ path: '/elderly/recharge' })
        }
      }
      fetchRecharges()
    })

    return {
      loading,
      recharges,
      currentPage,
      totalPages,
      dialogVisible,
      selectedRecharge,
      formatAmount,
      formatDateTime,
      getStatusText,
      getStatusClass,
      getPaymentMethodText,
      handlePay,
      handleCancel,
      handlePageChange,
      goToProfile
    }
  }
}
</script>

<style scoped>
.elderly-page {
  padding: 20px;
}

.elderly-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.elderly-card-header {
  padding: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.elderly-card-title {
  margin: 0;
  font-size: 24px;
  color: #333;
  display: flex;
  align-items: center;
}

.elderly-card-icon {
  margin-right: 10px;
  font-size: 28px;
}

.elderly-card-body {
  padding: 20px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 16px;
  color: #666;
  font-size: 16px;
}

.recharge-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recharge-item {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
}

.recharge-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #1890ff;
}

.recharge-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.recharge-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.recharge-amount {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
}

.recharge-date {
  font-size: 14px;
  color: #999;
}

.recharge-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
}

.status-pending {
  background-color: #fff7e6;
  color: #fa8c16;
}

.status-success {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-cancelled {
  background-color: #f5f5f5;
  color: #999;
}

.status-failed {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.recharge-details {
  margin-bottom: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.detail-row {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  color: #666;
  min-width: 100px;
  flex-shrink: 0;
}

.detail-value {
  color: #333;
  flex: 1;
  word-break: break-all;
}

.detail-value.amount {
  font-size: 18px;
  font-weight: bold;
  color: #ff4d4f;
}

.detail-value.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
}

.recharge-actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.pagination-btn {
  padding: 8px 16px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.pagination-btn:hover:not(:disabled) {
  background: #40a9ff;
}

.pagination-btn:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.pagination-info {
  font-size: 14px;
  color: #666;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: #999;
  margin-bottom: 24px;
}

.modal-overlay {
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

.modal {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-title {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.modal-close:hover {
  background: #f0f0f0;
  color: #333;
}

.modal-body {
  padding: 20px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section-title {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

@media (max-width: 768px) {
  .recharge-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .recharge-status {
    align-self: flex-start;
    margin-top: 8px;
  }
  
  .recharge-actions {
    flex-direction: column;
  }
  
  .detail-row {
    flex-direction: column;
  }
  
  .detail-label {
    min-width: auto;
    margin-bottom: 4px;
  }
}
</style>
