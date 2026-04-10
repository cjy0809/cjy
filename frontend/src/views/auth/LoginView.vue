<template>
  <div class="login-container">
    <!-- 跳过链接，提高可访问性 -->
    <a href="#login-form" class="skip-link">跳转到登录表单</a>
    
    <div class="login-content">
      <div class="login-left">
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
      
      <div class="login-right" id="login-form">
        <div class="login-form-container">
          <div class="login-header">
            <h2 class="login-title">欢迎回来</h2>
            <p class="login-subtitle">请登录您的账号以继续</p>
          </div>
          
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @submit.prevent="handleLogin"
          >
            <div class="form-group">
              <label class="form-label" for="phone">手机号</label>
              <el-form-item prop="phone" class="form-item">
                <el-input
                  id="phone"
                  v-model="loginForm.phone"
                  placeholder="请输入您的手机号"
                  size="large"
                  class="custom-input"
                  autocomplete="tel"
                  @focus="handleInputFocus('phone')"
                  @blur="handleInputBlur('phone')"
                >
                  <template #prefix>
                    <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M22 16.92V19.92C22.0011 20.1986 21.9441 20.4742 21.8325 20.7294C21.7209 20.9846 21.5573 21.2137 21.3521 21.4019C21.1468 21.5901 20.9046 21.7336 20.6407 21.8228C20.3769 21.912 20.0974 21.9452 19.82 21.92C16.7428 21.5857 13.787 20.5342 11.19 18.84C8.77382 17.3077 6.72534 15.2723 5.18 12.87C3.47994 10.2736 2.42837 7.31661 2.1 4.24C2.075 3.964 2.107 3.686 2.194 3.424C2.281 3.162 2.422 2.921 2.608 2.716C2.794 2.511 3.021 2.347 3.274 2.234C3.527 2.121 3.8 2.062 4.08 2.06H7.08C7.598 2.052 8.099 2.243 8.486 2.597C8.873 2.951 9.119 3.443 9.18 3.96C9.291 4.996 9.531 6.015 9.89 7C10.014 7.326 10.042 7.681 9.971 8.023C9.9 8.365 9.732 8.681 9.486 8.934L8.29 10.13C9.72534 12.2723 11.7277 14.2747 13.87 15.71L15.07 14.51C15.323 14.264 15.639 14.096 15.981 14.025C16.323 13.954 16.678 13.982 17 14.1C17.985 14.459 19.004 14.699 20.04 14.81C20.563 14.872 21.055 15.121 21.409 15.512C21.763 15.903 21.952 16.406 21.94 16.92H22Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                    </svg>
                  </template>
                </el-input>
              </el-form-item>
            </div>
            
            <div class="form-group">
              <label class="form-label" for="password">密码</label>
              <el-form-item prop="password" class="form-item">
                <el-input
                  id="password"
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入您的密码"
                  size="large"
                  show-password
                  class="custom-input"
                  autocomplete="current-password"
                  @keyup.enter="handleLogin"
                  @focus="handleInputFocus('password')"
                  @blur="handleInputBlur('password')"
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
            
            <div class="form-options">
              <el-checkbox v-model="rememberMe" class="remember-me">记住我</el-checkbox>
              <a href="#" class="forgot-password" @click.prevent="handleForgotPassword">忘记密码？</a>
            </div>
          </el-form>
          
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            <span v-if="!loading">登录</span>
            <span v-else>登录中...</span>
          </el-button>
          
          <div class="login-footer">
            <p>还没有账号？<router-link to="/register" class="register-link">立即注册</router-link></p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)
const rememberMe = ref(false)
const activeInput = ref('')

const loginForm = reactive({
  phone: '',
  password: ''
})

onMounted(() => {
  setTimeout(() => {
    document.querySelector('.login-left')?.classList.add('animate-in')
    document.querySelector('.login-right')?.classList.add('animate-in')
  }, 100)
  
  const savedPhone = localStorage.getItem('rememberedPhone')
  if (savedPhone) {
    loginForm.phone = savedPhone
    rememberMe.value = true
  }
})

const loginRules = reactive({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
})

const handleInputFocus = (field) => {
  activeInput.value = field
  // 添加焦点动画效果
  const formGroup = document.querySelector(`.form-group:has(#${field})`)
  if (formGroup) {
    formGroup.classList.add('focused')
  }
}

const handleInputBlur = (field) => {
  activeInput.value = ''
  // 移除焦点动画效果
  const formGroup = document.querySelector(`.form-group:has(#${field})`)
  if (formGroup) {
    formGroup.classList.remove('focused')
  }
}

const handleForgotPassword = () => {
  ElMessage.info('请联系管理员重置密码')
}

// 检查回车键登录
const handleKeyup = (event) => {
  if (event.key === 'Enter') {
    handleLogin()
  }
}

const handleLogin = async () => {
  const valid = await loginFormRef.value.validate()
  if (!valid) return
  
  loading.value = true
  try {
    const result = await userStore.userLogin(loginForm.phone, loginForm.password)
    if (result.success) {
      ElMessage.success(result.message || '登录成功')
      
      if (rememberMe.value) {
        localStorage.setItem('rememberedPhone', loginForm.phone)
      } else {
        localStorage.removeItem('rememberedPhone')
      }
      
      setTimeout(() => {
        const userRole = userStore.userInfo?.role
        console.log('登录成功，用户角色:', userRole)
        console.log('用户信息:', userStore.userInfo)
        
        if (userRole) {
          localStorage.setItem('userRole', userRole)
          console.log('用户角色已保存到localStorage:', userRole)
          
          if (userRole === 'ELDERLY') {
            console.log('跳转到老年用户首页')
            router.push('/elderly/home')
          } else if (userRole === 'ADMIN' || userRole === 'STAFF') {
            console.log('跳转到管理员/工作人员数据分析页面')
            router.push('/system/analytics-dashboard')
          } else {
            console.log('未知角色，默认跳转到数据分析页面')
            router.push('/system/analytics-dashboard')
          }
        } else {
          console.error('无法获取用户角色，使用默认跳转')
          router.push('/system/analytics-dashboard')
        }
      }, 100)
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    console.error('登录异常:', error)
    ElMessage.error('登录失败，请稍后再试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
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

.login-content {
  display: flex;
  width: 100%;
  height: 100vh;
  position: relative;
  z-index: 10;
}

.login-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  opacity: 0;
  transform: translateX(-50px);
  transition: all 0.8s ease;
}

.login-left.animate-in {
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
  transition: transform 0.3s ease;
}

.feature-item:hover {
  transform: translateX(5px);
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
  transition: all 0.3s ease;
}

.feature-item:hover .feature-icon {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.feature-icon svg {
  width: 20px;
  height: 20px;
  color: white;
}

.login-right {
  width: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  opacity: 0;
  transform: translateX(50px);
  transition: all 0.8s ease 0.2s;
}

.login-right.animate-in {
  opacity: 1;
  transform: translateX(0);
}

.login-form-container {
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-title {
  font-size: 2rem;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 10px 0;
}

.login-subtitle {
  font-size: 1.1rem;
  color: #ffffff;
  margin: 0;
}

.login-form {
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

.login-form::before {
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

.form-group {
  margin-bottom: 25px;
  transition: transform 0.3s ease;
}

.form-group.focused {
  transform: scale(1.02);
}

.form-label {
  display: block;
  font-size: 0.9rem;
  font-weight: 500;
  color: #4a5568;
  margin-bottom: 8px;
  transition: color 0.3s ease;
}

.form-group.focused .form-label {
  color: #f6d365;
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

:deep(.custom-input .el-input__wrapper:hover) {
  border-color: #cbd5e0;
}

:deep(.custom-input.is-focus .el-input__wrapper) {
  border-color: #f6d365;
  box-shadow: 0 0 0 3px rgba(246, 211, 101, 0.2);
}

:deep(.custom-input .el-input__inner) {
  font-size: 1rem;
  color: #2d3748;
  padding-left: 10px;
}

.input-icon {
  width: 20px;
  height: 20px;
  color: #a0aec0;
  transition: color 0.3s ease;
}

.form-group.focused .input-icon {
  color: #f6d365;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.remember-me {
  font-size: 0.9rem;
}

.forgot-password {
  font-size: 0.9rem;
  color: #f6d365;
  text-decoration: none;
  transition: color 0.3s ease;
}

.forgot-password:hover {
  color: #fda085;
  text-decoration: underline;
}

.login-button {
  width: 100%;
  height: 50px;
  font-size: 1.1rem;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #f6d365, #fda085);
  border: none;
  transition: all 0.3s ease;
  margin-top: 10px;
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
}

.login-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.login-button:hover::before {
  left: 100%;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(246, 211, 101, 0.3);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
  font-size: 1.2rem;
  color: #ffffff;
  margin-top: 0;
}

.register-link {
  color: #f6d365;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.register-link:hover {
  color: #fda085;
  text-decoration: underline;
}



/* 跳过链接样式 */
.skip-link {
  position: absolute;
  top: -40px;
  left: 6px;
  background: var(--primary-color);
  color: white;
  padding: 8px;
  text-decoration: none;
  z-index: var(--z-index-tooltip);
  border-radius: var(--border-radius-md);
  transition: top 0.3s ease;
}

.skip-link:focus {
  top: 6px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .login-left {
    display: none;
  }
  
  .login-right {
    width: 90%;
    max-width: 500px;
    margin: 0 auto;
  }
  
  .login-form {
    background-color: rgba(255, 255, 255, 0.95);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  }
}

@media (max-width: 768px) {
  .login-right {
    padding: 30px 20px;
  }
  
  .login-title {
    font-size: 1.8rem;
  }
  
  .brand-title {
    font-size: 1.8rem;
  }
}
</style>