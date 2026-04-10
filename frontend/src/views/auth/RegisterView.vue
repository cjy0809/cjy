<template>
  <div class="register-container">
    <div class="register-content">
      <div class="register-left">
        <div class="brand-section">
          <div class="logo-container">
            <div class="logo-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L2 7V12C2 16.5 4.23 20.68 7.62 23.15L12 21L16.38 23.15C19.77 20.68 22 16.5 22 12V7L12 2Z" fill="url(#logo-gradient)" />
                <path d="M9 12L11 14L15 10" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                <defs>
                  <linearGradient id="logo-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:#f6d365" />
                    <stop offset="100%" style="stop-color:#fda085" />
                  </linearGradient>
                </defs>
              </svg>
            </div>
            <h1 class="brand-title">社区老年活动中心</h1>
          </div>
          <p class="brand-subtitle">为老年人打造温馨、便捷的数字化生活平台</p>
          <div class="feature-list">
            <div class="feature-item">
              <div class="feature-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M9 11H15M21 11C21 16.5228 16.5228 21 11 21C5.47715 21 1 16.5228 1 11C1 5.47715 5.47715 1 11 1C16.5228 1 21 5.47715 21 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
              </div>
              <span>活动预约管理</span>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                  <path d="M9 11C11.2091 11 13 9.20914 13 7C13 4.79086 11.2091 3 9 3C6.79086 3 5 4.79086 5 7C5 9.20914 6.79086 11 9 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                  <path d="M23 21V19C22.9993 18.1137 22.7044 17.2528 22.1614 16.5523C21.6184 15.8519 20.8581 15.3516 20 15.13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                  <path d="M16 3.13C16.8604 3.35031 17.623 3.85071 18.1676 4.55232C18.7122 5.25392 19.0078 6.11683 19.0078 7.005C19.0078 7.89318 18.7122 8.75608 18.1676 9.45769C17.623 10.1593 16.8604 10.6597 16 10.88" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
              </div>
              <span>社交互动平台</span>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
              </div>
              <span>个性化服务</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="register-right">
        <div class="register-form-container">
          <div class="register-header">
            <h2 class="register-title">创建账号</h2>
            <p class="register-subtitle">加入我们，开启美好生活</p>
          </div>
          
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="register-form"
          >
            <!-- 基本注册信息 -->
            <div class="form-section">
              <h3 class="section-title">
                <svg class="section-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                  <path d="M12 11C14.2091 11 16 9.20914 16 7C16 4.79086 14.2091 3 12 3C9.79086 3 8 4.79086 8 7C8 9.20914 9.79086 11 12 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
                注册信息
              </h3>
              <p class="section-desc">请填写以下基本信息完成注册</p>
              
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">用户名</label>
                  <el-form-item prop="username" class="form-item">
                    <el-input
                      v-model="registerForm.username"
                      placeholder="请输入用户名"
                      size="large"
                      class="custom-input"
                    >
                      <template #prefix>
                        <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                          <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                          <path d="M12 11C14.2091 11 16 9.20914 16 7C16 4.79086 14.2091 3 12 3C9.79086 3 8 4.79086 8 7C8 9.20914 9.79086 11 12 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                        </svg>
                      </template>
                    </el-input>
                  </el-form-item>
                </div>
                
                <div class="form-group">
                  <label class="form-label">用户角色</label>
                  <el-form-item prop="role" class="form-item">
                    <el-select
                      v-model="registerForm.role"
                      placeholder="请选择用户角色"
                      size="large"
                      class="custom-input"
                      style="width: 100%"
                    >
                      <el-option label="老年用户" value="ELDERLY" />
                      <el-option label="工作人员" value="STAFF" />
                    </el-select>
                  </el-form-item>
                </div>
              </div>
              
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">真实姓名</label>
                  <el-form-item prop="name" class="form-item">
                    <el-input
                      v-model="registerForm.name"
                      placeholder="请输入真实姓名"
                      size="large"
                      class="custom-input"
                    >
                      <template #prefix>
                        <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                          <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                          <path d="M12 11C14.2091 11 16 9.20914 16 7C16 4.79086 14.2091 3 12 3C9.79086 3 8 4.79086 8 7C8 9.20914 9.79086 11 12 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                        </svg>
                      </template>
                    </el-input>
                  </el-form-item>
                </div>
                
                <div class="form-group">
                  <label class="form-label">手机号</label>
                  <el-form-item prop="phone" class="form-item">
                    <el-input
                      v-model="registerForm.phone"
                      placeholder="请输入手机号"
                      size="large"
                      class="custom-input"
                    >
                      <template #prefix>
                        <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                          <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                        </svg>
                      </template>
                    </el-input>
                  </el-form-item>
                </div>
              </div>
              
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">密码</label>
                  <el-form-item prop="password" class="form-item">
                    <el-input
                      v-model="registerForm.password"
                      type="password"
                      placeholder="请输入密码"
                      size="large"
                      show-password
                      class="custom-input"
                    >
                      <template #prefix>
                        <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                          <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                          <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                        </svg>
                      </template>
                    </el-input>
                  </el-form-item>
                </div>
                
                <div class="form-group">
                  <label class="form-label">确认密码</label>
                  <el-form-item prop="confirmPassword" class="form-item">
                    <el-input
                      v-model="registerForm.confirmPassword"
                      type="password"
                      placeholder="请再次输入密码"
                      size="large"
                      show-password
                      class="custom-input"
                    >
                      <template #prefix>
                        <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                          <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                          <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                        </svg>
                      </template>
                    </el-input>
                  </el-form-item>
                </div>
              </div>
            </div>
            
            <el-button
              type="primary"
              size="large"
              class="register-button"
              :loading="loading"
              @click="handleRegister"
            >
              <span v-if="!loading">注册</span>
              <span v-else>注册中...</span>
            </el-button>
          </el-form>
          
          <div class="register-footer">
            <p>已有账号？<router-link to="/login" class="login-link">立即登录</router-link></p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref()
const loading = ref(false)

onMounted(() => {
  // 添加入场动画
  setTimeout(() => {
    document.querySelector('.register-left')?.classList.add('animate-in')
    document.querySelector('.register-right')?.classList.add('animate-in')
  }, 100)
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  name: '',
  role: 'ELDERLY'
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(value)) {
    callback(new Error('请输入有效的手机号'))
  } else {
    callback()
  }
}

const registerRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: validatePhone, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '姓名长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择用户角色', trigger: 'change' }
  ]
})

const handleRegister = async () => {
  const valid = await registerFormRef.value.validate()
  if (!valid) return
  
  loading.value = true
  try {
    // 构建符合后端UserRegisterRequest的数据结构
        const backendData = {
          username: registerForm.username,
          password: registerForm.password,
          name: registerForm.name, // 前端name字段映射到后端name字段
          phone: registerForm.phone,
          // 后端需要gender字段，根据前端表单情况设置默认值
          gender: 0,
          // 添加角色信息，直接传递角色字符串
          role: registerForm.role // 直接使用前端选择的角色字符串（ELDERLY或STAFF）
        }
    
    const result = await userStore.userRegister(backendData)
    if (result.success) {
      ElMessage.success(result.message || '注册成功')
      router.push('/login')
    } else {
      ElMessage.error(result.message || '注册失败')
    }
  } catch (error) {
    console.error('注册异常:', error)
    ElMessage.error('注册失败，请稍后再试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.register-container {
  min-height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
  background-image: url('https://images.unsplash.com/photo-1576765608535-5f04d1e3f289?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
}

.register-content {
  display: flex;
  width: 100%;
  height: 100vh;
  position: relative;
  z-index: 10;
}

.register-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  opacity: 0;
  transform: translateX(-50px);
  transition: all 0.8s ease;
}

.register-left.animate-in {
  opacity: 1;
  transform: translateX(0);
}

.brand-section {
  max-width: 500px;
  color: white;
}

.logo-container {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}

.logo-icon {
  width: 60px;
  height: 60px;
  margin-right: 15px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

.brand-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
  line-height: 1.2;
}

.brand-subtitle {
  font-size: 1.2rem;
  margin-bottom: 40px;
  opacity: 0.9;
  line-height: 1.5;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  font-size: 1.2rem;
}

.feature-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  backdrop-filter: blur(5px);
}

.feature-icon svg {
  width: 20px;
  height: 20px;
  color: white;
}

.register-right {
  width: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  opacity: 0;
  transform: translateX(50px);
  transition: all 0.8s ease 0.2s;
  overflow-y: auto;
}

.register-right.animate-in {
  opacity: 1;
  transform: translateX(0);
}

.register-form-container {
  width: 100%;
  max-width: 500px;
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
  padding-top: 10px;
}

.register-title {
  font-size: 2rem;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 10px 0;
}

.register-subtitle {
  font-size: 1.1rem;
  color: #ffffff;
  margin: 0;
}

.register-form {
  margin-bottom: 30px;
}

.form-section {
  margin-bottom: 30px;
  padding: 30px;
  background-color: rgba(248, 250, 253, 0.95);
  border-radius: 16px;
  border: 2px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(15px);
  position: relative;
  overflow: hidden;
}

.form-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #f6d365, #fda085, #f6d365);
  background-size: 200% 100%;
  animation: gradientSlide 3s linear infinite;
}

@keyframes gradientSlide {
  0% {
    background-position: 0% 50%;
  }
  100% {
    background-position: 200% 50%;
  }
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 1.1rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 5px 0;
}

.section-icon {
  width: 20px;
  height: 20px;
  margin-right: 8px;
  color: #f6d365;
}

.section-desc {
  font-size: 0.9rem;
  color: #718096;
  margin: 0 0 25px 0;
  line-height: 1.5;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.form-row:last-child {
  margin-bottom: 5px;
}

.form-group {
  flex: 1;
}

.form-group-half {
  flex: 1;
}

.form-label {
  display: block;
  font-size: 0.95rem;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 10px;
}

.form-item {
  margin-bottom: 0;
}

.custom-input {
  width: 100%;
}

:deep(.custom-input .el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  box-shadow: none;
  padding: 14px 18px;
  transition: all 0.3s ease;
}

:deep(.custom-input:hover .el-input__wrapper) {
  border-color: #f6d365;
}

:deep(.custom-input.is-focus .el-input__wrapper) {
  border-color: #f6d365;
  box-shadow: 0 0 0 3px rgba(246, 211, 101, 0.2);
}

:deep(.custom-input .el-input__inner) {
  font-size: 1rem;
  color: #2d3748;
  padding-left: 10px;
  height: 20px;
}

:deep(.custom-input .el-input-number .el-input__wrapper) {
  width: 100%;
}

:deep(.custom-input .el-select .el-input__wrapper) {
  width: 100%;
}

.input-icon {
  width: 20px;
  height: 20px;
  color: #a0aec0;
}

.register-button {
  width: 100%;
  height: 50px;
  font-size: 1.1rem;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #f6d365, #fda085);
  border: none;
  transition: all 0.3s ease;
}

.register-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.register-button:active:not(:disabled) {
  transform: translateY(0);
}

.register-footer {
  text-align: center;
  font-size: 1.2rem;
  color: #ffffff;
}

.login-link {
  color: #f6d365;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.login-link:hover {
  color: #fda085;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .register-left {
    display: none;
  }
  
  .register-right {
    width: 90%;
    max-width: 600px;
    margin: 0 auto;
  }
  
  .form-section {
    background-color: rgba(255, 255, 255, 0.95);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  }
}

@media (max-width: 768px) {
  .register-right {
    padding: 30px 20px;
  }
  
  .register-title {
    font-size: 1.8rem;
  }
  
  .brand-title {
    font-size: 1.8rem;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  
  .form-section {
    padding: 15px;
  }
}
</style>