<template>
  <div class="elderly-profile">
    <!-- 页面标题 -->
    <div class="elderly-card">
      <div class="elderly-card-header">
        <h2 class="elderly-card-title">个人中心</h2>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-container">
      <div class="loading-spinner"></div>
      <p class="loading-text">正在加载个人信息...</p>
    </div>

    <!-- 个人信息卡片 -->
    <div v-else class="elderly-card">
      <div class="elderly-card-header">
        <h3 class="elderly-card-title">个人信息</h3>
        <button 
          class="elderly-button elderly-button-primary" 
          @click="toggleEditMode"
          v-if="!isEditing"
        >
          编辑信息
        </button>
      </div>
      <div class="elderly-card-body">
        <!-- 查看模式 -->
        <div v-if="!isEditing" class="profile-view">
          <div class="profile-avatar">
            <img :src="avatarUrl" alt="用户头像" class="avatar-img" />
          </div>
          <div class="profile-info">
            <div class="info-row">
              <span class="info-label">姓名：</span>
              <span class="info-value">{{ userInfo.name || '未填写' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">性别：</span>
              <span class="info-value">{{ formatGender(userInfo.gender) }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">年龄：</span>
              <span class="info-value">{{ userInfo.age || '未填写' }}岁</span>
            </div>
            <div class="info-row">
              <span class="info-label">电话：</span>
              <span class="info-value">{{ userInfo.phone || '未填写' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">地址：</span>
              <span class="info-value">{{ userInfo.address || '未填写' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">紧急联系人：</span>
              <span class="info-value">{{ userInfo.emergencyContact || '未填写' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">紧急联系电话：</span>
              <span class="info-value">{{ userInfo.emergencyPhone || '未填写' }}</span>
            </div>
            <div class="info-row balance-row">
              <div class="balance-info">
                <span class="info-label">账户余额：</span>
                <span class="info-value balance-value">￥{{ formatBalance(userInfo.balance) }}</span>
              </div>
              <div class="balance-actions">
                <button class="elderly-button elderly-button-primary recharge-btn" @click="showRechargeDialog">
                  充值
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 编辑模式 -->
        <div v-else class="profile-edit">
          <div class="profile-avatar">
            <img :src="avatarUrl" alt="用户头像" class="avatar-img" />
            <div class="avatar-upload">
              <button class="elderly-button elderly-button-default" @click="triggerFileUpload">
                更换头像
              </button>
              <input 
                type="file" 
                ref="fileInput" 
                accept="image/*" 
                @change="handleAvatarChange" 
                style="display: none"
              />
            </div>
          </div>
          <div class="profile-form">
            <div class="elderly-form-group">
              <label class="elderly-form-label">姓名</label>
              <input 
                v-model="editForm.name" 
                type="text" 
                class="elderly-form-input" 
                placeholder="请输入您的姓名"
              />
            </div>
            <div class="elderly-form-group">
              <label class="elderly-form-label">性别</label>
              <select v-model="editForm.gender" class="elderly-form-input">
                <option value="">请选择性别</option>
                <option value="0">男</option>
                <option value="1">女</option>
              </select>
            </div>
            <div class="elderly-form-group">
              <label class="elderly-form-label">年龄</label>
              <input 
                v-model.number="editForm.age" 
                type="number" 
                class="elderly-form-input" 
                placeholder="请输入您的年龄"
                min="50"
                max="120"
              />
            </div>
            <div class="elderly-form-group">
              <label class="elderly-form-label">电话</label>
              <input 
                v-model="editForm.phone" 
                type="tel" 
                class="elderly-form-input" 
                placeholder="请输入您的电话号码"
                pattern="^1[3-9]\d{9}$"
              />
            </div>
            <div class="elderly-form-group">
              <label class="elderly-form-label">地址</label>
              <input 
                v-model="editForm.address" 
                type="text" 
                class="elderly-form-input" 
                placeholder="请输入您的家庭地址"
              />
            </div>
            <div class="elderly-form-group">
              <label class="elderly-form-label">紧急联系人</label>
              <input 
                v-model="editForm.emergencyContact" 
                type="text" 
                class="elderly-form-input" 
                placeholder="请输入紧急联系人姓名"
              />
            </div>
            <div class="elderly-form-group">
              <label class="elderly-form-label">紧急联系电话</label>
              <input 
                v-model="editForm.emergencyPhone" 
                type="tel" 
                class="elderly-form-input" 
                placeholder="请输入紧急联系人电话"
                pattern="^1[3-9]\d{9}$"
              />
            </div>
            <div class="elderly-form-footer">
              <button class="elderly-button elderly-button-default" @click="cancelEdit">
                取消
              </button>
              <button 
                class="elderly-button elderly-button-primary" 
                @click="saveProfile"
                :disabled="isSaving"
              >
                {{ isSaving ? '保存中...' : '保存' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 密码修改卡片 -->
    <div class="elderly-card">
      <div class="elderly-card-header">
        <h3 class="elderly-card-title">密码管理</h3>
        <button 
          class="elderly-button elderly-button-primary" 
          @click="togglePasswordMode"
          v-if="!isEditingPassword"
        >
          修改密码
        </button>
      </div>
      <div class="elderly-card-body">
        <!-- 密码修改表单 -->
        <div v-if="isEditingPassword" class="password-edit">
          <div class="elderly-form-group">
            <label class="elderly-form-label">当前密码</label>
            <input 
              v-model="passwordForm.oldPassword" 
              type="password" 
              class="elderly-form-input" 
              placeholder="请输入当前密码"
            />
          </div>
          <div class="elderly-form-group">
            <label class="elderly-form-label">新密码</label>
            <input 
              v-model="passwordForm.newPassword" 
              type="password" 
              class="elderly-form-input" 
              placeholder="请输入新密码"
            />
            <div class="password-strength" v-if="passwordForm.newPassword">
              <div class="strength-segments">
                <div 
                  v-for="(segment, index) in 4" 
                  :key="index" 
                  class="strength-segment"
                  :class="{ 'active': index < passwordStrengthLevel }"
                  :style="{ backgroundColor: index < passwordStrengthLevel ? passwordStrengthColor : '' }"
                ></div>
              </div>
              <span class="strength-text" :style="{ color: passwordStrengthColor }">{{ passwordStrengthText }}</span>
            </div>
          </div>
          <div class="elderly-form-group">
            <label class="elderly-form-label">确认新密码</label>
            <input 
              v-model="passwordForm.confirmPassword" 
              type="password" 
              class="elderly-form-input" 
              placeholder="请再次输入新密码"
            />
          </div>
          <div class="elderly-form-footer">
            <button class="elderly-button elderly-button-default" @click="cancelPasswordEdit">
              取消
            </button>
            <button 
              class="elderly-button elderly-button-primary" 
              @click="changePassword"
              :disabled="isChangingPassword"
            >
              {{ isChangingPassword ? '修改中...' : '修改密码' }}
            </button>
          </div>
        </div>
        <div v-else class="password-info">
            <p>为了您的账户安全，建议定期更换密码</p>
        </div>
      </div>
    </div>

    <!-- 充值弹窗 -->
    <div v-if="showRechargeModal" class="modal-overlay" @click="closeRechargeDialog">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">账户充值</h3>
          <button class="modal-close" @click="closeRechargeDialog">×</button>
        </div>
        <div class="modal-body">
          <div class="elderly-form-group">
            <label class="elderly-form-label">充值金额</label>
            <input 
              v-model="rechargeForm.amount" 
              type="number" 
              class="elderly-form-input" 
              placeholder="请输入充值金额"
              min="1"
              step="0.01"
            />
          </div>
          <div class="elderly-form-group">
            <label class="elderly-form-label">支付方式</label>
            <div class="payment-method">
              <div class="payment-option selected">
                <img src="/src/assets/1.webp" alt="支付宝" class="payment-icon" />
                <span>支付宝</span>
                <div class="check-icon">✓</div>
              </div>
            </div>
          </div>
          <div class="recharge-tips">
            <div class="tips-header" @click="showRechargeTips = !showRechargeTips">
              <span class="tips-title">💡 温馨提示</span>
              <span class="tips-toggle">{{ showRechargeTips ? '收起' : '展开' }}</span>
            </div>
            <div v-show="showRechargeTips" class="tips-content">
              <p>• 单次充值金额最低为 1 元</p>
              <p>• 充值成功后余额将立即到账</p>
              <p>• 如有疑问请联系客服</p>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="elderly-button elderly-button-default" @click="closeRechargeDialog">
            取消
          </button>
          <button 
            class="elderly-button elderly-button-primary" 
            @click="submitRecharge"
            :disabled="isSubmittingRecharge"
          >
            {{ isSubmittingRecharge ? '处理中...' : '立即充值' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'

export default {
  name: 'ElderlyProfileView',
  setup() {
    const route = useRoute()
    const userStore = useUserStore()
    const isEditing = ref(false)
    const isEditingPassword = ref(false)
    const isLoading = ref(false)
    const isSaving = ref(false)
    const isChangingPassword = ref(false)
    const fileInput = ref(null)
    
    // 用户信息
    const userInfo = ref({
      id: '',
      username: '',
      name: '',
      phone: '',
      age: '',
      address: '',
      avatar: '',
      gender: '',
      emergencyContact: '',
      emergencyPhone: ''
    })
    
    // 编辑表单
    const editForm = ref({
      name: '',
      phone: '',
      age: '',
      address: '',
      avatar: '',
      gender: '',
      emergencyContact: '',
      emergencyPhone: ''
    })
    
    // 密码表单
    const passwordForm = ref({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    // 计算密码强度
    const passwordStrength = computed(() => {
      const password = passwordForm.value.newPassword
      if (!password) return 0
      
      let strength = 0
      // 基础长度分数 (最高40分)
      if (password.length >= 6) strength += 10
      if (password.length >= 8) strength += 10
      if (password.length >= 10) strength += 10
      if (password.length >= 12) strength += 10
      
      // 复杂度分数 (最高60分)
      if (/[a-z]/.test(password)) strength += 10
      if (/[A-Z]/.test(password)) strength += 10
      if (/[0-9]/.test(password)) strength += 10
      if (/[^a-zA-Z0-9]/.test(password)) strength += 15
      if (/[a-z]/.test(password) && /[A-Z]/.test(password) && /[0-9]/.test(password)) strength += 15
      
      return Math.min(strength, 100)
    })
    
    // 处理头像URL，确保能够正确显示
    const avatarUrl = computed(() => {
      const avatar = userInfo.value.avatar
      if (!avatar) return '/src/assets/default-avatar.svg'
      
      if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
        if (avatar.includes('localhost:8080')) {
          return avatar.replace('http://localhost:8080', '')
        }
        return avatar
      }
      
      return avatar.startsWith('/') ? avatar : `/${avatar}`
    })
    
    const passwordStrengthLevel = computed(() => {
      const strength = passwordStrength.value
      if (strength <= 0) return 0
      if (strength <= 25) return 1
      if (strength <= 50) return 2
      if (strength <= 75) return 3
      return 4
    })

    const passwordStrengthColor = computed(() => {
      const level = passwordStrengthLevel.value
      if (level <= 1) return '#F56C6C'
      if (level <= 2) return '#E6A23C'
      if (level <= 3) return '#F7BA2A'
      return '#67C23A'
    })
    
    const passwordStrengthText = computed(() => {
      const level = passwordStrengthLevel.value
      if (level <= 0) return ''
      if (level <= 1) return '弱'
      if (level <= 2) return '中等'
      if (level <= 3) return '强'
      return '非常强'
    })
    
    // 格式化性别显示
const formatGender = (gender) => {
  if (gender === 1 || gender === '1') return '男'
  if (gender === 0 || gender === '0') return '女'
  return '未填写'
}
    
    // 切换编辑模式
    const toggleEditMode = () => {
      isEditing.value = true
      // 复制用户信息到编辑表单
      editForm.value = { ...userInfo.value }
    }
    
    // 取消编辑
    const cancelEdit = () => {
      isEditing.value = false
      // 重置表单
      editForm.value = { ...userInfo.value }
    }
    
    // 保存个人信息
    const saveProfile = async () => {
      try {
        // 表单验证
        if (!editForm.value.name) {
          ElMessage.warning('请输入姓名')
          return
        }
        
        if (editForm.value.phone && !/^1[3-9]\d{9}$/.test(editForm.value.phone)) {
          ElMessage.warning('请输入正确的手机号码')
          return
        }
        
        if (editForm.value.emergencyPhone && !/^1[3-9]\d{9}$/.test(editForm.value.emergencyPhone)) {
          ElMessage.warning('请输入正确的紧急联系人手机号码')
          return
        }
        
        if (editForm.value.age && (editForm.value.age < 50 || editForm.value.age > 120)) {
          ElMessage.warning('年龄应在50-120岁之间')
          return
        }
        
        // 设置保存状态
        isSaving.value = true
        
        // 提交更新
        await userApi.updateUser(editForm.value)
        
        // 更新本地用户信息
        userInfo.value = { ...editForm.value }
        userStore.setUserInfo(userInfo.value)
        
        isEditing.value = false
        ElMessage.success('个人信息更新成功')
      } catch (error) {
        console.error('更新个人信息失败:', error)
        ElMessage.error('更新个人信息失败，请稍后重试')
      } finally {
        isSaving.value = false
      }
    }
    
    // 触发文件选择
    const triggerFileUpload = () => {
      fileInput.value.click()
    }
    
    // 处理头像变更
    const handleAvatarChange = async (event) => {
      const file = event.target.files[0]
      if (!file) return
      
      let loadingInstance = null
      
      try {
        // 检查文件类型
        if (!file.type.startsWith('image/')) {
          ElMessage.warning('请选择图片文件')
          return
        }
        
        // 检查文件大小 (限制为5MB)
        if (file.size > 5 * 1024 * 1024) {
          ElMessage.warning('图片大小不能超过5MB')
          return
        }
        
        // 显示加载状态
        loadingInstance = ElLoading.service({
          lock: true,
          text: '正在上传头像...',
          background: 'rgba(0, 0, 0, 0.7)'
        })
        
        // 上传头像
        const response = await userApi.uploadAvatar(file)
        
        if (response.code === 200 && response.data) {
          // 更新编辑表单和用户信息中的头像
          editForm.value.avatar = response.data
          userInfo.value.avatar = response.data
          
          // 同步到store
          userStore.setUserInfo(userInfo.value)
          
          ElMessage.success('头像上传成功')
        } else {
          // 显示更详细的错误信息
          const errorMessage = response.message || '头像上传失败'
          console.error('头像上传失败，服务器响应:', response)
          ElMessage.error(errorMessage)
        }
      } catch (error) {
        console.error('上传头像失败:', error)
        
        // 显示更详细的错误信息
        let errorMessage = '上传头像失败，请稍后重试'
        
        if (error.response) {
          // 服务器返回了错误响应
          if (error.response.data && error.response.data.message) {
            errorMessage = error.response.data.message
          } else if (error.response.status) {
            errorMessage = `服务器错误 (${error.response.status})`
          }
        } else if (error.message) {
          // 网络错误或其他错误
          errorMessage = error.message
        }
        
        ElMessage.error(errorMessage)
      } finally {
        // 确保关闭加载状态
        if (loadingInstance) {
          loadingInstance.close()
        }
      }
    }
    
    // 切换密码编辑模式
    const togglePasswordMode = () => {
      isEditingPassword.value = true
      // 重置表单
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    }
    
    // 取消密码编辑
    const cancelPasswordEdit = () => {
      isEditingPassword.value = false
      // 重置表单
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    }
    
    // 修改密码
    const changePassword = async () => {
      try {
        // 表单验证
        if (!passwordForm.value.oldPassword) {
          ElMessage.warning('请输入当前密码')
          return
        }
        
        if (!passwordForm.value.newPassword) {
          ElMessage.warning('请输入新密码')
          return
        }
        
        if (passwordForm.value.newPassword.length < 6) {
          ElMessage.warning('新密码长度不能少于6位')
          return
        }
        
        if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
          ElMessage.warning('两次输入的新密码不一致')
          return
        }
        
        await ElMessageBox.confirm('确定要修改密码吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        isChangingPassword.value = true
        
        await userApi.changePassword(passwordForm.value.oldPassword, passwordForm.value.newPassword)
        
        isEditingPassword.value = false
        ElMessage.success('密码修改成功')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('修改密码失败:', error)
          ElMessage.error('修改密码失败，请检查当前密码是否正确')
        }
      } finally {
        isChangingPassword.value = false
      }
    }
    
    // 获取用户信息
    const fetchUserInfo = async () => {
      try {
        isLoading.value = true
        const response = await userApi.getCurrentUser()
        if (response.code === 200 && response.data) {
          userInfo.value = response.data
          // 使用userStore的setUserInfo方法同步信息
          userStore.setUserInfo(response.data)
          console.log('获取到的完整用户信息:', userInfo.value)
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        ElMessage.error('获取用户信息失败')
      } finally {
        isLoading.value = false
      }
    }
    
    // 充值相关
    const showRechargeModal = ref(false)
    const isSubmittingRecharge = ref(false)
    const rechargeForm = ref({
      amount: ''
    })
    const showRechargeTips = ref(false)
    
    const showRechargeDialog = () => {
      showRechargeModal.value = true
      rechargeForm.value.amount = ''
      showRechargeTips.value = false
    }
    
    const closeRechargeDialog = () => {
      showRechargeModal.value = false
      rechargeForm.value.amount = ''
    }
    
    const formatBalance = (balance) => {
      if (!balance) return '0.00'
      return parseFloat(balance).toFixed(2)
    }
    
    const submitRecharge = async () => {
      const amount = parseFloat(rechargeForm.value.amount)
      
      if (!rechargeForm.value.amount || isNaN(amount)) {
        ElMessage.warning('请输入有效的充值金额')
        return
      }
      
      if (amount <= 0) {
        ElMessage.warning('充值金额必须大于0元')
        return
      }
      
      if (amount < 1) {
        ElMessage.warning('充值金额不能小于1元')
        return
      }
      
      if (amount > 10000) {
        ElMessage.warning('单次充值金额不能超过10000元')
        return
      }
      
      isSubmittingRecharge.value = true
      
      try {
        const response = await userApi.createRecharge({
          amount: parseFloat(rechargeForm.value.amount),
          paymentMethod: 'alipay',
          remark: '用户自主充值'
        })
        
        if (response.code === 200) {
          const rechargeData = response.data.recharge
          const payUrl = response.data.payUrl
          
          ElMessage.success('充值订单创建成功，正在跳转到支付页面...')
          
          setTimeout(() => {
            closeRechargeDialog()
            
            const newWindow = window.open('', '_blank')
            newWindow.document.write(payUrl)
            newWindow.document.close()
            
            ElMessage.info('请在支付宝沙盒环境中完成支付，支付完成后请手动关闭支付窗口')
            
            const checkPaymentStatus = setInterval(async () => {
              try {
                const statusResponse = await userApi.getRechargeById(rechargeData.id)
                if (statusResponse && statusResponse.code === 200 && statusResponse.data && statusResponse.data.status === 1) {
                  clearInterval(checkPaymentStatus)
                  if (newWindow && !newWindow.closed) {
                    newWindow.close()
                  }
                  ElMessage.success('充值成功！')
                  await fetchUserInfo()
                }
              } catch (error) {
                console.error('检查充值状态失败:', error)
              }
            }, 3000)
            
            let pollCount = 0
            const maxPollCount = 60
            
            const checkPaymentStatusWithCount = setInterval(async () => {
              pollCount++
              if (pollCount > maxPollCount) {
                clearInterval(checkPaymentStatusWithCount)
                ElMessage.warning('支付状态检查超时，请手动刷新页面查看余额')
                if (newWindow && !newWindow.closed) {
                  newWindow.close()
                }
                return
              }
              
              try {
                const statusResponse = await userApi.getRechargeById(rechargeData.id)
                if (statusResponse && statusResponse.code === 200 && statusResponse.data && statusResponse.data.status === 1) {
                  clearInterval(checkPaymentStatusWithCount)
                  if (newWindow && !newWindow.closed) {
                    newWindow.close()
                  }
                  ElMessage.success('充值成功！')
                  await fetchUserInfo()
                }
              } catch (error) {
                console.error('检查充值状态失败:', error)
              }
            }, 3000)
            
            setTimeout(() => {
              clearInterval(checkPaymentStatusWithCount)
              if (newWindow && !newWindow.closed) {
                newWindow.close()
              }
            }, 180000)
          }, 1000)
        }
      } catch (error) {
        console.error('充值失败:', error)
        ElMessage.error('充值失败，请稍后重试')
      } finally {
        isSubmittingRecharge.value = false
      }
    }

    onMounted(async () => {
      await fetchUserInfo()
      editForm.value = { ...userInfo.value }
      
      const query = route.query
      const tradeStatus = query.trade_status
      const outTradeNo = query.out_trade_no
      
      if (tradeStatus && (tradeStatus === 'TRADE_SUCCESS' || tradeStatus === 'TRADE_FINISHED')) {
        ElMessage.success('充值成功！')
        await fetchUserInfo()
        
        try {
          await ElMessageBox.alert(
            `充值成功！\n\n【充值信息】\n💰 充值金额：￥${query.total_amount || '未知'}\n📋 订单号：${outTradeNo || '未知'}\n\n余额已更新到您的账户。`,
            '充值成功',
            {
              confirmButtonText: '确定',
              type: 'success',
              center: true,
              showClose: true,
              closeOnClickModal: true,
              closeOnPressEscape: true
            }
          )
        } catch (action) {
          ElMessageBox.close()
        }
      }
    })
    
    return {
      userInfo,
      editForm,
      passwordForm,
      isEditing,
      isEditingPassword,
      isLoading,
      isSaving,
      isChangingPassword,
      fileInput,
      passwordStrength,
      passwordStrengthLevel,
      passwordStrengthColor,
      passwordStrengthText,
      avatarUrl,
      formatGender,
      toggleEditMode,
      cancelEdit,
      saveProfile,
      triggerFileUpload,
      handleAvatarChange,
      togglePasswordMode,
      cancelPasswordEdit,
      changePassword,
      showRechargeModal,
      isSubmittingRecharge,
      rechargeForm,
      showRechargeTips,
      showRechargeDialog,
      closeRechargeDialog,
      submitRecharge,
      formatBalance
    }
  }
}
</script>

<style scoped>
.elderly-profile {
  max-width: 800px;
  margin: 0 auto;
}

/* 加载状态样式 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--elderly-space-xxl) 0;
  color: var(--elderly-text-secondary);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--elderly-border-light);
  border-top: 4px solid var(--elderly-primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: var(--elderly-space-md);
}

.loading-text {
  font-size: var(--elderly-font-size-md);
  color: var(--elderly-text-secondary);
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.profile-view {
  display: flex;
  flex-direction: column;
  gap: var(--elderly-space-lg);
}

.profile-edit {
  display: flex;
  flex-direction: column;
  gap: var(--elderly-space-lg);
}

.profile-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--elderly-space-md);
}

.avatar-img {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--elderly-primary-color);
}

.avatar-upload {
  display: flex;
  gap: var(--elderly-space-sm);
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: var(--elderly-space-md);
}

.info-row {
  display: flex;
  align-items: center;
  padding: var(--elderly-space-sm) 0;
  border-bottom: 1px solid var(--elderly-border-light);
}

.info-label {
  font-weight: 600;
  color: var(--elderly-text-secondary);
  min-width: 120px;
}

.info-value {
  color: var(--elderly-text-primary);
  font-size: var(--elderly-font-size-md);
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: var(--elderly-space-lg);
}

.password-edit {
  display: flex;
  flex-direction: column;
  gap: var(--elderly-space-lg);
}

.password-info {
  padding: var(--elderly-space-md);
  background-color: var(--elderly-info-light);
  border-radius: var(--elderly-border-radius-md);
  color: var(--elderly-info-color);
  font-size: var(--elderly-font-size-md);
}

.password-strength {
  margin-top: var(--elderly-space-sm);
  display: flex;
  align-items: center;
  gap: 10px;
}

.strength-segments {
  display: flex;
  gap: 4px;
  align-items: center;
  min-height: 6px;
}

.strength-segment {
  width: 40px;
  height: 6px;
  background-color: var(--elderly-border-light);
  border-radius: 3px;
  border: 1px solid var(--elderly-border-color);
  transition: all var(--elderly-transition-base);
}

.strength-text {
  font-size: var(--elderly-font-size-sm);
  color: var(--elderly-text-secondary);
  white-space: nowrap;
  min-width: 48px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-view {
    gap: var(--elderly-space-md);
  }
  
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--elderly-space-xs);
  }
  
  .info-label {
    min-width: auto;
  }
  
  .avatar-img {
    width: 100px;
    height: 100px;
  }
}

/* 充值弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.modal-title {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.modal-close {
  background: none;
  border: none;
  font-size: 32px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.modal-close:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 20px;
  border-top: 1px solid #e4e7ed;
}

.balance-row {
  align-items: center;
  gap: 12px;
  justify-content: space-between;
}

.balance-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.balance-value {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}

.balance-actions {
  display: flex;
  align-items: center;
}

.recharge-btn {
  padding: 8px 20px;
  font-size: 16px;
}

.payment-method {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.payment-option:hover {
  border-color: #409eff;
}

.payment-option.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.payment-icon {
  width: 40px;
  height: 40px;
  object-fit: contain;
}

.payment-option span {
  flex: 1;
  font-size: 16px;
  color: #333;
}

.check-icon {
  width: 24px;
  height: 24px;
  background: #409eff;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
}

.recharge-tips {
  background: #f4f4f5;
  padding: 16px;
  border-radius: 8px;
  margin-top: 16px;
}

.tips-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  user-select: none;
  padding: 4px 0;
}

.tips-title {
  font-weight: bold;
  color: #333;
  font-size: 14px;
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
}

.recharge-tips p {
  margin: 4px 0;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.recharge-tips p:first-child {
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}
</style>