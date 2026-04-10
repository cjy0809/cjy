<template>
  <div class="system-profile">
    <!-- 页面标题 -->
    <el-card class="page-header-card">
      <div class="page-header">
        <h2>个人中心</h2>
        <p>管理您的个人信息和账户设置</p>
      </div>
    </el-card>

    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 个人信息卡片 -->
    <el-card v-else class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button 
            v-if="!isEditing" 
            type="primary" 
            @click="toggleEditMode"
          >
            <Edit style="width: 1em; height: 1em; margin-right: 5px;" />
            编辑信息
          </el-button>
        </div>
      </template>

      <!-- 查看模式 -->
      <div v-if="!isEditing" class="profile-view">
        <div class="profile-avatar">
          <el-avatar :size="120" :src="avatarUrl">
            <el-icon :size="60"><User /></el-icon>
          </el-avatar>
        </div>
        <div class="profile-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户名">{{ userInfo.username || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ userInfo.name || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ formatGender(userInfo.gender) }}</el-descriptions-item>
            <el-descriptions-item label="年龄">{{ userInfo.age || '未填写' }}岁</el-descriptions-item>
            <el-descriptions-item label="电话">{{ userInfo.phone || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="角色">{{ formatRole(userInfo.role) }}</el-descriptions-item>
            <el-descriptions-item label="地址" :span="2">{{ userInfo.address || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="紧急联系人">{{ userInfo.emergencyContact || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="紧急联系电话">{{ userInfo.emergencyPhone || '未填写' }}</el-descriptions-item>

          </el-descriptions>
        </div>
      </div>

      <!-- 编辑模式 -->
      <div v-else class="profile-edit">
        <el-form :model="editForm" :rules="formRules" ref="profileFormRef" label-width="100px">
          <div class="avatar-section">
            <el-avatar :size="120" :src="avatarUrl">
              <el-icon :size="60"><User /></el-icon>
            </el-avatar>
            <div class="avatar-upload">
              <el-upload
                class="avatar-uploader"
                :action="uploadAction"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                :on-error="handleAvatarError"
              >
                <el-button size="small" type="primary">更换头像</el-button>
              </el-upload>
            </div>
          </div>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="editForm.username" placeholder="请输入用户名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="姓名" prop="name">
                <el-input v-model="editForm.name" placeholder="请输入姓名" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="性别" prop="gender">
                <el-select v-model="editForm.gender" placeholder="请选择性别" style="width: 100%">
                  <el-option label="男" :value="1" />
                  <el-option label="女" :value="0" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="年龄" prop="age">
                <el-input-number 
                  v-model="editForm.age" 
                  :min="18" 
                  :max="100" 
                  placeholder="请输入年龄"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="电话" prop="phone">
                <el-input v-model="editForm.phone" placeholder="请输入电话号码" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="地址" prop="address">
                <el-input v-model="editForm.address" placeholder="请输入地址" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="紧急联系人" prop="emergencyContact">
                <el-input v-model="editForm.emergencyContact" placeholder="请输入紧急联系人姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="紧急联系电话" prop="emergencyPhone">
                <el-input v-model="editForm.emergencyPhone" placeholder="请输入紧急联系人电话" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item>
            <el-button @click="cancelEdit">取消</el-button>
            <el-button type="primary" @click="saveProfile" :loading="isSaving">
              {{ isSaving ? '保存中...' : '保存' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 密码修改卡片 -->
    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <span>密码管理</span>
          <el-button 
            v-if="!isEditingPassword" 
            type="primary" 
            @click="togglePasswordMode"
          >
            <Key style="width: 1em; height: 1em; margin-right: 5px;" />
            修改密码
          </el-button>
        </div>
      </template>

      <!-- 密码修改表单 -->
      <div v-if="isEditingPassword" class="password-edit">
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
          <el-form-item label="当前密码" prop="oldPassword">
            <el-input 
              v-model="passwordForm.oldPassword" 
              type="password" 
              placeholder="请输入当前密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input 
              v-model="passwordForm.newPassword" 
              type="password" 
              placeholder="请输入新密码"
              show-password
              :class="{'password-weak': passwordStrengthLevel === 1, 'password-medium': passwordStrengthLevel === 2, 'password-strong': passwordStrengthLevel === 3, 'password-very-strong': passwordStrengthLevel === 4}"
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
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input 
              v-model="passwordForm.confirmPassword" 
              type="password" 
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button @click="cancelPasswordEdit">取消</el-button>
            <el-button type="primary" @click="changePassword" :loading="isChangingPassword">
              {{ isChangingPassword ? '修改中...' : '修改密码' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      <div v-else class="password-info">
        <el-alert
          title="密码安全提示"
          type="info"
          description="为了您的账户安全，建议定期更换密码，并使用包含大小写字母、数字和特殊字符的复杂密码。"
          :closable="false"
          show-icon
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, User, Key, Close, Warning, CircleCheck } from '@element-plus/icons-vue'

export default {
  name: 'SystemProfileView',
  components: {
    Edit,
    User,
    Key,
    Close,
    Warning,
    CircleCheck
  },
  setup() {
    const userStore = useUserStore()
    const isEditing = ref(false)
    const isEditingPassword = ref(false)
    const isLoading = ref(false)
    const isSaving = ref(false)
    const isChangingPassword = ref(false)
    const profileFormRef = ref(null)
    const passwordFormRef = ref(null)
    
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
      emergencyPhone: '',
      role: ''
    })
    
    // 编辑表单
    const editForm = ref({
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
    
    // 密码表单
    const passwordForm = ref({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    // 表单验证规则
    const formRules = {
      name: [
        { required: true, message: '请输入姓名', trigger: 'blur' }
      ],
      phone: [
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
      ],
      emergencyPhone: [
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的紧急联系人手机号码', trigger: 'blur' }
      ]
    }
    
    // 密码表单验证规则
    const passwordRules = {
      oldPassword: [
        { required: true, message: '请输入当前密码', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '新密码长度不能少于6位', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== passwordForm.value.newPassword) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }
    
    // 处理头像URL，确保能够正确显示
    const avatarUrl = computed(() => {
      const avatar = userInfo.value.avatar
      if (!avatar) return '/uploads/avatars/default-avatar.jpg'
      
      // 如果是完整URL，直接返回
      if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
        // 将完整的后端URL转换为可通过代理访问的URL
        if (avatar.includes('localhost:8080')) {
          return avatar.replace('http://localhost:8080', '')
        }
        return avatar
      }
      
      // 如果是相对路径，确保以/开头
      return avatar.startsWith('/') ? avatar : `/${avatar}`
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
    
    // 计算密码强度级别（1-4）
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
      if (level <= 1) return '#F56C6C'  // 红色 - 弱
      if (level <= 2) return '#E6A23C'  // 橙色 - 中等
      if (level <= 3) return '#F7BA2A'  // 黄色 - 强
      return '#67C23A'  // 绿色 - 非常强
    })
    
    const passwordStrengthText = computed(() => {
      const level = passwordStrengthLevel.value
      if (level <= 0) return ''
      if (level <= 1) return '弱'
      if (level <= 2) return '中等'
      if (level <= 3) return '强'
      return '非常强'
    })
    
    const passwordStrengthIcon = computed(() => {
      const level = passwordStrengthLevel.value
      if (level <= 1) return 'Close'
      if (level <= 2) return 'Warning'
      return 'CircleCheck'
    })
    
    // 上传相关配置
    const uploadAction = computed(() => {
      return '/api/user/avatar'
    })
    
    const uploadHeaders = computed(() => {
      return {
        'Authorization': `Bearer ${userStore.token}`
      }
    })
    
    // 格式化性别显示
    const formatGender = (gender) => {
      if (gender === 1 || gender === '1') return '男'
      if (gender === 0 || gender === '0') return '女'
      return '未填写'
    }
    
    // 格式化角色显示
    const formatRole = (role) => {
      if (role === 'ADMIN') return '管理员'
      if (role === 'STAFF') return '工作人员'
      if (role === 'ELDERLY') return '老年用户'
      return '未填写'
    }
    
    // 格式化日期显示
    const formatDate = (dateString) => {
      if (!dateString) return '未填写'
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
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
        await profileFormRef.value.validate()
        
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
        if (error !== false) { // 不是表单验证错误
          console.error('更新个人信息失败:', error)
          ElMessage.error('更新个人信息失败，请稍后重试')
        }
      } finally {
        isSaving.value = false
      }
    }
    
    // 头像上传成功回调
    const handleAvatarSuccess = (response) => {
      if (response.code === 200 && response.data) {
        // 更新编辑表单和用户信息中的头像
        editForm.value.avatar = response.data
        userInfo.value.avatar = response.data
        
        // 同步到store
        userStore.setUserInfo(userInfo.value)
        
        ElMessage.success('头像上传成功')
      } else {
        ElMessage.error(response.message || '头像上传失败')
      }
    }
    
    // 头像上传前验证
    const beforeAvatarUpload = (file) => {
      // 检查文件类型
      if (!file.type.startsWith('image/')) {
        ElMessage.warning('请选择图片文件')
        return false
      }
      
      // 检查文件大小 (限制为5MB)
      if (file.size > 5 * 1024 * 1024) {
        ElMessage.warning('图片大小不能超过5MB')
        return false
      }
      
      return true
    }
    
    // 头像上传错误处理
    const handleAvatarError = () => {
      ElMessage.error('头像上传失败，请稍后重试')
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
        await passwordFormRef.value.validate()
        
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
        if (error !== 'cancel' && error !== false) {
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
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        ElMessage.error('获取用户信息失败')
      } finally {
        isLoading.value = false
      }
    }
    
    // 生命周期钩子
    onMounted(async () => {
      // 总是从API获取最新的完整用户信息
      await fetchUserInfo()
      // 初始化编辑表单
      editForm.value = { ...userInfo.value }
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
      profileFormRef,
      passwordFormRef,
      formRules,
      passwordRules,
      uploadAction,
      uploadHeaders,
      passwordStrength,
      passwordStrengthLevel,
      passwordStrengthColor,
      passwordStrengthText,
      passwordStrengthIcon,
      avatarUrl,
      formatGender,
      formatRole,
      formatDate,
      toggleEditMode,
      cancelEdit,
      saveProfile,
      handleAvatarSuccess,
      beforeAvatarUpload,
      handleAvatarError,
      togglePasswordMode,
      cancelPasswordEdit,
      changePassword
    }
  }
}
</script>

<style scoped>
.system-profile {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header-card {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: var(--system-text-primary);
}

.page-header p {
  margin: 0;
  color: var(--system-text-secondary);
  font-size: 14px;
}

.profile-card, .password-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-edit {
  padding: 20px 0;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
  gap: 15px;
}

.avatar-upload {
  margin-top: 10px;
}

.password-edit {
  padding: 20px 0;
}

.password-info {
  padding: 10px 0;
}

.password-strength {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
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
  background-color: #e4e7ed;
  border-radius: 3px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
}

.strength-text {
  font-size: 12px;
  white-space: nowrap;
  min-width: 48px;
}

/* 密码输入框边框颜色变化 */
.password-weak .el-input__wrapper {
  box-shadow: 0 0 0 1px #F56C6C inset;
}

.password-medium .el-input__wrapper {
  box-shadow: 0 0 0 1px #E6A23C inset;
}

.password-strong .el-input__wrapper {
  box-shadow: 0 0 0 1px #F7BA2A inset;
}

.password-very-strong .el-input__wrapper {
  box-shadow: 0 0 0 1px #67C23A inset;
}

.loading-container {
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .system-profile {
    padding: 10px;
  }
  
  .profile-view {
    gap: 15px;
  }
  
  .avatar-section {
    margin-bottom: 20px;
  }
}
</style>