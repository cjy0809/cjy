<template>
  <div class="payment-container">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/elderly/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/elderly/services' }">服务列表</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: `/elderly/services/${serviceId}` }">服务详情</el-breadcrumb-item>
        <el-breadcrumb-item>支付</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="payment-content" v-loading="loading">
      <el-row :gutter="24">
        <el-col :span="16">
          <div class="payment-section">
            <h2 class="section-title">选择支付方式</h2>
            
            <div class="payment-methods">
              <div 
                v-for="method in paymentMethods" 
                :key="method.id"
                class="payment-method-card"
                :class="{ 
                  active: selectedPaymentMethod === method.id,
                  disabled: method.id === 1 && userBalance === 0
                }"
                @click="method.id === 1 && userBalance === 0 ? null : selectPaymentMethod(method.id)"
              >
                <div class="method-icon">
                  <el-icon :size="48" :color="selectedPaymentMethod === method.id ? '#67C23A' : (method.id === 1 && userBalance === 0 ? '#C0C4CC' : '#909399')">
                    <Wallet v-if="method.id === 1" />
                    <CreditCard v-else />
                  </el-icon>
                </div>
                <div class="method-info">
                  <h3>{{ method.name }}</h3>
                  <p v-if="method.id === 1">
                    <span v-if="userBalance === 0" class="insufficient-balance">余额不足，请先充值</span>
                    <span v-else>当前余额：￥{{ userBalance.toFixed(2) }} | {{ method.description }}</span>
                  </p>
                  <p v-else>{{ method.description }}</p>
                </div>
                <div class="method-check">
                  <el-icon v-if="selectedPaymentMethod === method.id" :size="24" color="#67C23A">
                    <CircleCheckFilled />
                  </el-icon>
                </div>
              </div>
            </div>

            <div class="order-info">
              <h2 class="section-title">订单信息</h2>
              <div class="info-card">
                <div class="info-row">
                  <span class="label">服务名称：</span>
                  <span class="value">{{ serviceInfo.name }}</span>
                </div>
                <div class="info-row">
                  <span class="label">预约日期：</span>
                  <span class="value">{{ bookingInfo.reservationDate }}</span>
                </div>
                <div class="info-row">
                  <span class="label">预约时间：</span>
                  <span class="value">{{ bookingInfo.reservationTime }}</span>
                </div>
                <div class="info-row">
                  <span class="label">服务时长：</span>
                  <span class="value">{{ serviceInfo.duration }}分钟</span>
                </div>
                <div class="info-row">
                  <span class="label">备注：</span>
                  <span class="value">{{ bookingInfo.remark || '无' }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-col>

        <el-col :span="8">
          <div class="payment-summary">
            <h2 class="section-title">支付详情</h2>
            <div class="summary-card">
              <div class="summary-item">
                <span class="label">服务费用</span>
                <span class="value">￥{{ serviceInfo.price }}</span>
              </div>
              <div class="summary-item">
                <span class="label">优惠金额</span>
                <span class="value discount">-￥0.00</span>
              </div>
              <div class="summary-divider"></div>
              <div class="summary-total">
                <span class="label">应付金额</span>
                <span class="value total">￥{{ serviceInfo.price }}</span>
              </div>
              
              <div class="payment-tips">
                <div class="tips-header" @click="showPaymentTips = !showPaymentTips">
                  <div class="tips-title">
                    <el-icon color="#E6A23C" :size="16">
                      <WarningFilled />
                    </el-icon>
                    <span>温馨提示</span>
                  </div>
                  <span class="tips-toggle">{{ showPaymentTips ? '收起' : '展开' }}</span>
                </div>
                <div v-show="showPaymentTips" class="tips-content">
                  <span>支付成功后，系统将自动为您完成预约</span>
                </div>
              </div>

              <div class="payment-actions">
                <el-button 
                  type="primary" 
                  size="large" 
                  class="pay-button"
                  @click="handlePayment"
                  :disabled="!selectedPaymentMethod || paying"
                  :loading="paying"
                >
                  {{ paying ? '支付中...' : '立即支付 ￥' + serviceInfo.price }}
                </el-button>

                <div class="secondary-actions">
                <el-button 
                  size="large" 
                  class="cancel-button"
                  @click="handleCancel"
                  :disabled="paying"
                >
                  取消订单
                </el-button>

                <el-button 
                  size="large" 
                  class="back-button"
                  @click="handleBackToService"
                  :disabled="paying"
                >
                  返回服务详情
                </el-button>
              </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheckFilled, CreditCard, Wallet } from '@element-plus/icons-vue'
import { paymentApi } from '@/api/payment'
import { servicesApi } from '@/api/services'
import { userApi } from '@/api/user'
import eventBus from '@/utils/eventBus'

export default {
  name: 'PaymentView',
  components: {
    CircleCheckFilled,
    CreditCard,
    Wallet
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    const loading = ref(false)
    const paying = ref(false)
    const selectedPaymentMethod = ref(1)
    const paymentSuccessHandled = ref(false)
    const currentPaymentStatus = ref(0)
    const showPaymentTips = ref(false)
    const userBalance = ref(0)
    
    const serviceId = ref(route.query.serviceId || '')
    const bookingId = ref(route.query.bookingId || '')
    
    const serviceInfo = reactive({
      id: '',
      name: '',
      price: 0,
      duration: 0
    })
    
    const bookingInfo = reactive({
      reservationDate: '',
      reservationTime: '',
      remark: ''
    })
    
    const paymentMethods = [
      {
        id: 1,
        name: '账户余额',
        description: '使用账户余额支付，便捷快速',
        icon: 'wallet'
      },
      {
        id: 2,
        name: '支付宝',
        description: '支付宝支付，方便快捷',
        icon: 'alipay'
      }
    ]
    
    const selectPaymentMethod = (methodId) => {
      selectedPaymentMethod.value = methodId
    }
    
    const loadUserBalance = async () => {
      try {
        const response = await userApi.getCurrentUser()
        if (response && response.code === 200 && response.data) {
          userBalance.value = response.data.balance || 0
          // 如果余额为0，自动切换到支付宝支付
          if (userBalance.value === 0) {
            selectedPaymentMethod.value = 2
          }
        }
      } catch (error) {
        console.error('加载用户余额失败:', error)
      }
    }
    
    const loadServiceInfo = async () => {
      if (!serviceId.value) {
        ElMessage.error('缺少服务信息')
        router.back()
        return
      }
      
      try {
        loading.value = true
        await loadUserBalance()
        const response = await servicesApi.getServiceItemDetail(serviceId.value)
        console.log('支付页面服务详情响应:', response)
        if (response && response.code === 200 && response.data) {
          Object.assign(serviceInfo, {
            id: response.data.id,
            name: response.data.name,
            price: response.data.price || 0,
            duration: response.data.duration || 0
          })
        } else {
          ElMessage.error('服务信息格式错误')
        }
        
        // 获取当前支付状态
        try {
          const paymentResponse = await paymentApi.getPaymentByBookingId(bookingId.value)
          if (paymentResponse && paymentResponse.code === 200 && paymentResponse.data) {
            currentPaymentStatus.value = paymentResponse.data.status
          }
        } catch (error) {
          console.error('获取支付状态失败:', error)
        }
      } catch (error) {
        console.error('加载服务信息失败:', error)
        ElMessage.error('加载服务信息失败')
      } finally {
        loading.value = false
      }
    }
    
    const handlePayment = async () => {
      if (!selectedPaymentMethod.value) {
        ElMessage.warning('请选择支付方式')
        return
      }
      
      try {
        paying.value = true
        
        if (selectedPaymentMethod.value === 1) {
          // 余额支付
          if (userBalance.value < serviceInfo.price) {
            ElMessage.error('账户余额不足，请先充值')
            return
          }
          
          try {
            const paymentData = {
              bookingId: bookingId.value,
              serviceId: serviceId.value,
              paymentMethod: selectedPaymentMethod.value,
              amount: parseFloat(serviceInfo.price)
            }
            
            const token = localStorage.getItem('token')
            const authorization = token ? `Bearer ${token}` : ''
            
            console.log('余额支付请求:', paymentData)
            
            const response = await fetch('http://localhost:8080/api/payment/balance/pay', {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
                'Authorization': authorization
              },
              body: JSON.stringify(paymentData)
            })
            
            console.log('余额支付响应状态:', response.status)
            
            if (response.ok) {
              const result = await response.json()
              console.log('余额支付响应:', result)
              
              if (result.code === 200) {
                ElMessage.success('支付成功！')
                eventBus.emit('paymentDataChanged')
                
                try {
                  await ElMessageBox.alert(
                    `支付成功！\n\n【订单信息】\n📋 服务名称：${serviceInfo.name}\n📅 预约日期：${bookingInfo.reservationDate}\n⏰ 预约时间：${bookingInfo.reservationTime}\n💰 支付金额：￥${serviceInfo.price}\n\n您的预约已确认，我们将尽快为您安排服务。`,
                    '支付成功',
                    {
                      confirmButtonText: '查看我的预约',
                      type: 'success',
                      center: true,
                      showClose: true,
                      closeOnClickModal: true,
                      closeOnPressEscape: true
                    }
                  )
                  ElMessageBox.close()
                  router.push('/elderly/bookings')
                } catch (action) {
                  ElMessageBox.close()
                  if (action === 'cancel') {
                    router.push(`/elderly/services/${serviceId.value}`)
                  }
                }
              } else {
                ElMessage.error('余额支付失败: ' + (result.message || '未知错误'))
              }
            } else {
              const errorText = await response.text()
              console.error('余额支付请求失败:', response.status, errorText)
              ElMessage.error('余额支付失败: ' + errorText)
            }
          } catch (error) {
            console.error('余额支付失败:', error)
            ElMessage.error('余额支付失败，请稍后重试')
          }
        } else {
          // 支付宝支付
          const paymentData = {
            bookingId: bookingId.value,
            serviceId: serviceId.value,
            paymentMethod: selectedPaymentMethod.value,
            amount: parseFloat(serviceInfo.price)
          }
          
          const token = localStorage.getItem('token')
          const authorization = token ? `Bearer ${token}` : ''
          
          console.log('支付宝支付请求 - Token:', token ? '存在' : '不存在')
          console.log('支付宝支付请求 - Authorization:', authorization)
          
          const response = await fetch('http://localhost:8080/api/payment/alipay/create', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': authorization
            },
            body: JSON.stringify(paymentData)
          })
          
          console.log('支付宝支付响应状态:', response.status)
          
          if (response.ok) {
            const html = await response.text()
            console.log('支付宝支付表单长度:', html.length)
            const newWindow = window.open('', '_blank')
            newWindow.document.write(html)
            newWindow.document.close()
            ElMessage.success('正在跳转到支付宝支付页面...')
            
            const checkPaymentStatus = setInterval(async () => {
              try {
                const statusResponse = await paymentApi.getPaymentByBookingId(bookingId.value)
                if (statusResponse && statusResponse.code === 200 && statusResponse.data && statusResponse.data.status === 1) {
                  clearInterval(checkPaymentStatus)
                  newWindow.close()
                  ElMessage.success('支付成功！')
                  
                  eventBus.emit('paymentDataChanged')
                  
                  try {
                    await ElMessageBox.alert(
                      `支付成功！\n\n【订单信息】\n📋 服务名称：${serviceInfo.name}\n📅 预约日期：${bookingInfo.reservationDate}\n⏰ 预约时间：${bookingInfo.reservationTime}\n💰 支付金额：￥${serviceInfo.price}\n\n您的预约已确认，我们将尽快为您安排服务。`,
                      '支付成功',
                      {
                        confirmButtonText: '查看我的预约',
                        type: 'success',
                        center: true,
                        showClose: true,
                        closeOnClickModal: true,
                        closeOnPressEscape: true
                      }
                    )
                    ElMessageBox.close()
                    router.push('/elderly/bookings')
                  } catch (action) {
                    ElMessageBox.close()
                    if (action === 'cancel') {
                      router.push(`/elderly/services/${serviceId.value}`)
                    }
                  }
                }
              } catch (error) {
                console.error('检查支付状态失败:', error)
              }
            }, 5000)
            
            let pollCount = 0
            const maxPollCount = 60 // 最多轮询60次，约5分钟
            
            const checkPaymentStatusWithCount = setInterval(async () => {
              pollCount++
              if (pollCount > maxPollCount) {
                clearInterval(checkPaymentStatusWithCount)
                ElMessage.warning('支付状态检查超时，请手动刷新页面或联系客服')
                if (newWindow && !newWindow.closed) {
                  newWindow.close()
                }
                return
              }
              
              try {
                const statusResponse = await paymentApi.getPaymentByBookingId(bookingId.value)
                if (statusResponse && statusResponse.code === 200 && statusResponse.data && statusResponse.data.status === 1) {
                  clearInterval(checkPaymentStatusWithCount)
                  newWindow.close()
                  ElMessage.success('支付成功！')
                  
                  eventBus.emit('paymentDataChanged')
                  
                  try {
                    await ElMessageBox.alert(
                      `支付成功！\n\n【订单信息】\n📋 服务名称：${serviceInfo.name}\n📅 预约日期：${bookingInfo.reservationDate}\n⏰ 预约时间：${bookingInfo.reservationTime}\n💰 支付金额：￥${serviceInfo.price}\n\n您的预约已确认，我们将尽快为您安排服务。`,
                      '支付成功',
                      {
                        confirmButtonText: '查看我的预约',
                        type: 'success',
                        center: true,
                        showClose: true,
                        closeOnClickModal: true,
                        closeOnPressEscape: true
                      }
                    )
                    ElMessageBox.close()
                    router.push('/elderly/bookings')
                  } catch (action) {
                    ElMessageBox.close()
                    if (action === 'cancel') {
                      router.push(`/elderly/services/${serviceId.value}`)
                    }
                  }
                }
              } catch (error) {
                console.error('检查支付状态失败:', error)
              }
            }, 5000)
            
            setTimeout(() => {
              clearInterval(checkPaymentStatusWithCount)
              if (newWindow && !newWindow.closed) {
                newWindow.close()
              }
            }, 300000)
          } else {
            const errorText = await response.text()
            console.error('支付宝支付请求失败:', response.status, errorText)
            try {
              const errorJson = JSON.parse(errorText)
              ElMessage.error('创建支付宝支付失败: ' + (errorJson.message || errorJson.code || '未知错误'))
            } catch (e) {
              ElMessage.error('创建支付宝支付失败: ' + errorText)
            }
          }
        }
      } catch (error) {
        console.error('支付失败:', error)
        ElMessage.error('支付失败，请稍后重试')
      } finally {
        paying.value = false
      }
    }
    
    const handleCancel = async () => {
      try {
        await ElMessageBox.confirm(
          '确定要取消此订单吗？取消后需要重新预约。',
          '取消订单',
          {
            confirmButtonText: '确定取消',
            cancelButtonText: '继续支付',
            type: 'warning'
          }
        )
        
        await paymentApi.cancelPayment(bookingId.value)
        ElMessage.info('订单已取消')
        router.back()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消订单失败:', error)
          ElMessage.error('取消订单失败')
        }
      }
    }

    const showPaymentSuccessDialog = async () => {
      try {
        await ElMessageBox.alert(
          `支付成功！\n\n【订单信息】\n📋 服务名称：${serviceInfo.name}\n📅 预约日期：${bookingInfo.reservationDate}\n⏰ 预约时间：${bookingInfo.reservationTime}\n💰 支付金额：￥${serviceInfo.price}\n\n您的预约已确认，我们将尽快为您安排服务。`,
          '支付成功',
          {
            confirmButtonText: '查看我的预约',
            type: 'success',
            center: true,
            showClose: true,
            closeOnClickModal: true,
            closeOnPressEscape: true
          }
        )
        ElMessageBox.close()
        router.push('/elderly/bookings')
      } catch (action) {
        ElMessageBox.close()
        if (action === 'cancel') {
          router.push(`/elderly/services/${serviceId.value}`)
        }
      }
    }

    const handleBackToService = () => {
      router.push(`/elderly/services/${serviceId.value}`)
    }
    
    const getStatusText = (status) => {
      const statusMap = {
        0: '待支付',
        1: '已支付',
        2: '支付失败',
        3: '已退款'
      }
      return statusMap[status] || '未知状态'
    }
    
    onMounted(async () => {
      const query = route.query
      
      if (query.serviceId) {
        serviceId.value = query.serviceId
      }
      
      if (query.bookingId) {
        bookingId.value = query.bookingId
      }
      
      if (query.reservationDate) {
        bookingInfo.reservationDate = query.reservationDate
      }
      
      if (query.reservationTime) {
        bookingInfo.reservationTime = query.reservationTime
      }
      
      if (query.remark) {
        bookingInfo.remark = query.remark
      }
      
      const tradeStatus = query.trade_status
      const outTradeNo = query.out_trade_no
      
      console.log('支付宝返回参数:', { tradeStatus, outTradeNo, query })
      
      await loadServiceInfo()
      
      if (tradeStatus && (tradeStatus === 'TRADE_SUCCESS' || tradeStatus === 'TRADE_FINISHED') && outTradeNo && !paymentSuccessHandled.value) {
        console.log('检测到支付成功，显示成功弹窗')
        paymentSuccessHandled.value = true
        showPaymentSuccessDialog()
      }
    })
    
    return {
      loading,
      paying,
      selectedPaymentMethod,
      paymentSuccessHandled,
      currentPaymentStatus,
      showPaymentTips,
      serviceId,
      bookingId,
      serviceInfo,
      bookingInfo,
      paymentMethods,
      userBalance,
      selectPaymentMethod,
      handlePayment,
      handleCancel,
      handleBackToService,
      getStatusText
    }
  }
}
</script>

<style scoped>
.payment-container {
  padding: 20px;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.payment-content {
  max-width: 1400px;
  margin: 0 auto;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
}

.payment-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 32px;
}

.payment-method-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border: 2px solid #DCDFE6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.payment-method-card:hover {
  border-color: #409EFF;
  background-color: #F5F7FA;
}

.payment-method-card.active {
  border-color: #67C23A;
  background-color: #F0F9FF;
}

.payment-method-card.disabled {
  border-color: #C0C4CC;
  background-color: #F5F7FA;
  cursor: not-allowed;
  opacity: 0.6;
}

.payment-method-card.disabled:hover {
  border-color: #C0C4CC;
  background-color: #F5F7FA;
}

.insufficient-balance {
  color: #F56C6C;
  font-weight: 600;
}

.method-icon {
  width: 64px;
  height: 64px;
  margin-right: 20px;
}

.method-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.method-info {
  flex: 1;
}

.method-info h3 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px 0;
}

.method-info p {
  font-size: 0.9rem;
  color: #909399;
  margin: 0;
}

.method-check {
  margin-left: 20px;
}

.order-info {
  margin-top: 32px;
}

.info-card {
  background: #F5F7FA;
  border-radius: 8px;
  padding: 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #E4E7ED;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  font-weight: 500;
  color: #606266;
}

.info-row .value {
  color: #303133;
}

.payment-summary {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 20px;
}

.summary-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-item .label {
  font-size: 1rem;
  color: #606266;
}

.summary-item .value {
  font-size: 1.1rem;
  font-weight: 600;
  color: #303133;
}

.summary-item .value.discount {
  color: #67C23A;
}

.summary-divider {
  height: 1px;
  background: #E4E7ED;
  margin: 8px 0;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
}

.summary-total .label {
  font-size: 1.1rem;
  font-weight: 600;
  color: #303133;
}

.summary-total .value.total {
  font-size: 1.5rem;
  font-weight: 700;
  color: #F56C6C;
}

.payment-tips {
  background: #FEF0F0;
  border-radius: 6px;
  padding: 12px;
  color: #E6A23C;
  font-size: 0.9rem;
}

.tips-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.tips-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tips-toggle {
  color: #409eff;
  font-size: 12px;
  cursor: pointer;
}

.tips-toggle:hover {
  color: #66b1ff;
}

.tips-content {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.pay-button {
  width: 100%;
  height: 56px;
  font-size: 1.2rem;
  font-weight: 700;
  margin-top: 20px;
  border-radius: 8px;
  letter-spacing: 1px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.pay-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.pay-button:active:not(:disabled) {
  transform: translateY(0);
}

.secondary-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.cancel-button,
.back-button {
  flex: 1;
  height: 44px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.cancel-button:hover:not(:disabled),
.back-button:hover:not(:disabled) {
  transform: translateY(-1px);
}

.cancel-button:active:not(:disabled),
.back-button:active:not(:disabled) {
  transform: translateY(0);
}

.payment-status-info {
  background: #F8F9FA;
  border: 1px solid #E4E7ED;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.status-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.status-item:last-child {
  margin-bottom: 0;
}

.status-label {
  font-size: 0.9rem;
  color: #606266;
  min-width: 100px;
}

.status-value {
  font-size: 0.9rem;
  font-weight: 600;
  color: #303133;
}

@media (max-width: 768px) {
  .payment-content {
    padding: 0;
  }
  
  .payment-section,
  .payment-summary {
    border-radius: 0;
    box-shadow: none;
  }
  
  .payment-summary {
    position: static;
    margin-top: 20px;
  }
}
</style>
