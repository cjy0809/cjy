<template>
  <div class="system-layout">
    <div class="system-header">
      <div class="header-left">
        <el-button 
          v-if="isMobile || sidebarCollapsed" 
          class="menu-toggle" 
          :icon="sidebarCollapsed ? Expand : Fold"
          @click="toggleSidebar"
          circle
          size="small"
        />
        <h1 class="logo">社区老年活动中心</h1>
      </div>
      <div class="header-right">
        <div class="current-time">{{ currentTime }}</div>
        <el-dropdown @command="handleUserCommand">
          <span class="user-info">
            <el-avatar :size="32" :src="getUserAvatar">{{ userInfo.name.substring(0, 1) }}</el-avatar>
            <span class="username">{{ userInfo.name }}</span>
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="system-container">
      <div 
        class="system-sidebar" 
        :class="{ 'sidebar-collapsed': sidebarCollapsed, 'mobile-open': isMobile && !sidebarCollapsed }"
        :style="{ width: isMobile ? '' : sidebarWidth }"
      >
        <el-menu
          :default-active="activeMenu"
          class="system-menu"
          @select="handleMenuSelect"
          background-color="var(--system-bg-white)"
          text-color="var(--system-text-secondary)"
          active-text-color="var(--system-primary-color)"
          :collapse="!isMobile && sidebarCollapsed"
        >
          <el-menu-item index="/system/analytics-dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据分析</span>
          </el-menu-item>

          <el-sub-menu index="users">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/system/user-management">用户列表</el-menu-item>
            <el-menu-item index="/system/recharge-management">用户充值</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="activities">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span>活动管理</span>
            </template>
            <el-menu-item index="/system/activities">活动列表</el-menu-item>
            <el-menu-item index="/system/activity-registrations">活动报名</el-menu-item>
            <el-menu-item index="/system/activity-calendar">活动日历</el-menu-item>
          </el-sub-menu>
          

          
          <el-sub-menu index="services">
            <template #title>
              <el-icon><Service /></el-icon>
              <span>社区服务</span>
            </template>
            <el-menu-item index="/system/services">服务项目</el-menu-item>
            <el-menu-item index="/system/service-reservations">服务预约</el-menu-item>
            <el-menu-item index="/system/payment-management">金额管理</el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="/system/health-records">
            <el-icon><Warning /></el-icon>
            <span>健康监测</span>
          </el-menu-item>
          
          
          <el-sub-menu index="venues">
            <template #title>
              <el-icon><Location /></el-icon>
              <span>场地管理</span>
            </template>
            <el-menu-item index="/system/venues">场地列表</el-menu-item>
            <el-menu-item index="/system/venue-reservations">场地预约</el-menu-item>
            <el-menu-item index="/system/venue-calendar">预约日历</el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="/system/news">
            <el-icon><Document /></el-icon>
            <span>新闻公告</span>
          </el-menu-item>
          
          <el-sub-menu index="comments">
            <template #title>
              <el-icon><ChatDotRound /></el-icon>
              <span>评论管理</span>
            </template>
            <el-menu-item index="/system/comment-management">评论列表</el-menu-item>
            <el-menu-item index="/system/rating-list">评分列表</el-menu-item>
          </el-sub-menu>
          
        </el-menu>
      </div>
      
      <!-- 移动端遮罩层 -->
      <div 
        v-if="isMobile && !sidebarCollapsed" 
        class="sidebar-overlay" 
        @click="toggleSidebar"
      ></div>

      <div class="system-main">
        <div class="system-breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: getBreadcrumbHome() }">{{ getBreadcrumbHomeText() }}</el-breadcrumb-item>
            <el-breadcrumb-item v-for="(item, index) in breadcrumbList" :key="index">
              {{ item }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="system-content">
          <router-view v-slot="{ Component }">
            <transition name="slide-fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ArrowDown, User, Calendar, Service, Warning, Fold, Expand, Location, Ticket, Document, VideoPlay, ChatDotRound, DataAnalysis } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

export default {
  name: 'SystemLayout',
  components: {
    ArrowDown,
    User,
    Calendar,
    Service,
    Warning,
    Fold,
    Expand,
    Location,
    Ticket,
    Document,
    VideoPlay,
    ChatDotRound,
    DataAnalysis
  },
  setup() {
    const userStore = useUserStore()
    const router = useRouter()
    const route = useRoute()
    
    // 移动端侧边栏状态
    const sidebarCollapsed = ref(false)
    const isMobile = ref(false)
    const activeMenu = ref('/system/analytics-dashboard')
    const breadcrumbList = ref([])
    const currentTime = ref('')
    let timer = null
    
    // 计算属性：用户信息
    const userInfo = computed(() => {
      return userStore.userInfo || { name: '管理员', avatar: '', role: 'ADMIN' }
    })
    
    // 计算属性：侧边栏宽度
    const sidebarWidth = computed(() => {
      return sidebarCollapsed.value ? '64px' : '240px'
    })
    
    // 检查是否为移动设备
    const checkIsMobile = () => {
      isMobile.value = window.innerWidth <= 768
      if (!isMobile.value) {
        sidebarCollapsed.value = false
      }
    }
    
    // 切换侧边栏
    const toggleSidebar = () => {
      if (isMobile.value) {
        // 移动端使用类名控制
        const sidebar = document.querySelector('.system-sidebar')
        if (sidebar) {
          sidebar.classList.toggle('mobile-open')
        }
      } else {
        // 桌面端使用宽度控制
        sidebarCollapsed.value = !sidebarCollapsed.value
      }
    }
    
    // 菜单选择处理
    const handleMenuSelect = (index) => {
      router.push(index)
      
      // 移动端点击菜单后关闭侧边栏
      if (isMobile.value) {
        const sidebar = document.querySelector('.system-sidebar')
        if (sidebar) {
          sidebar.classList.remove('mobile-open')
        }
      }
    }
    
    // 更新面包屑
    const updateBreadcrumb = () => {
      const path = route.path
      // 面包屑映射配置
      const breadcrumbMap = {
        '/system/activities': ['活动列表'],
        '/system/activity-registrations': ['活动报名'],
        '/system/activity-calendar': ['预约日历'],
        '/system/services': ['服务项目'],
        '/system/service-reservations': ['服务预约'],
        '/system/payment-management': ['金额管理'],
        '/system/user-management': ['用户列表'],
        '/system/recharge-management': ['用户充值'],
        '/system/analytics-dashboard': ['数据分析'],
        '/system/health-records': ['健康监测'],
        '/system/venues': ['场地列表'],
        '/system/venue-reservations': ['场地预约'],
        '/system/venue-calendar': ['预约日历'],
        '/system/news': ['新闻公告'],
        '/system/news/:id': ['新闻详情'],
        '/system/activities/:id': ['活动详情'],
        '/system/services/:id': ['服务详情'],
        '/system/comment-management': ['评论列表'],
        '/system/rating-list': ['评分列表'],
        '/system/profile': ['个人信息']
      }
      
      // 处理带参数的路由
      let matchedPath = path
      // 检查是否是带参数的路由
      if (path.includes('/news/') && path !== '/system/news') {
        matchedPath = '/system/news/:id'
      } else if (path.includes('/activities/') && path !== '/system/activities') {
        matchedPath = '/system/activities/:id'
      } else if (path.includes('/services/') && path !== '/system/services') {
        matchedPath = '/system/services/:id'
      }
      
      breadcrumbList.value = breadcrumbMap[matchedPath] || []
    }
    
    // 获取面包屑首页路径
    const getBreadcrumbHome = () => {
      const path = route.path
      if (path.startsWith('/system/analytics')) {
        return '/system/analytics-dashboard'
      } else if (path.startsWith('/system/user')) {
        return '/system/user-management'
      } else if (path.startsWith('/system/health')) {
        return '/system/health-records'
      } else if (path.startsWith('/system/activity')) {
        return '/system/activities'
      } else if (path.startsWith('/system/service') || path.startsWith('/system/payment')) {
        return '/system/services'
      } else if (path.startsWith('/system/venue')) {
        return '/system/venues'
      } else if (path.startsWith('/system/news')) {
        return '/system/news'
      } else if (path.startsWith('/system/comment') || path.startsWith('/system/rating')) {
        return '/system/comment-management'
      } else if (path.startsWith('/system/profile')) {
        return '/system/profile'
      } else {
        return '/system/analytics-dashboard'
      }
    }
    
    // 获取面包屑首页文本
    const getBreadcrumbHomeText = () => {
      const path = route.path
      if (path.startsWith('/system/analytics')) {
        return '数据分析'
      } else if (path.startsWith('/system/user')) {
        return '用户管理'
      } else if (path.startsWith('/system/health')) {
        return '健康监测'
      } else if (path.startsWith('/system/activity')) {
        return '活动管理'
      } else if (path.startsWith('/system/service') || path.startsWith('/system/payment')) {
        return '社区服务'
      } else if (path.startsWith('/system/venue')) {
        return '场地管理'
      } else if (path.startsWith('/system/news')) {
        return '新闻公告'
      } else if (path.startsWith('/system/comment') || path.startsWith('/system/rating')) {
        return '评论管理'
      } else if (path.startsWith('/system/profile')) {
        return '个人中心'
      } else {
        return '数据分析'
      }
    }
    
    // 更新时间
    const updateTime = () => {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const hours = String(now.getHours()).padStart(2, '0')
      const minutes = String(now.getMinutes()).padStart(2, '0')
      const seconds = String(now.getSeconds()).padStart(2, '0')
      
      currentTime.value = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    }
    
    // 获取用户头像，如果没有则使用默认头像
    const getUserAvatar = computed(() => {
      if (userInfo.value.avatar) {
        return userInfo.value.avatar
      } else {
        // 使用默认头像
        return '/uploads/avatars/default-avatar.jpg'
      }
    })
    
    // 用户命令处理
    const handleUserCommand = (command) => {
      switch (command) {
        case 'profile':
          router.push('/system/profile')
          break
        case 'password':
          ElMessage.info('修改密码功能开发中')
          break
        case 'logout':
          ElMessageBox.confirm('确定要退出登录吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(async () => {
            try {
              // 调用store中的退出登录方法
              await userStore.userLogout()
              ElMessage.success('退出登录成功')
              
              // 确保所有状态清除后再跳转
              setTimeout(() => {
                // 强制刷新页面到登录页，确保完全重置应用状态
                window.location.href = '/login'
              }, 1000)
            } catch (error) {
              ElMessage.error('退出登录失败')
            }
          }).catch(() => {
            // 取消退出
          })
          break
      }
    }
    
    // 监听路由变化
    watch(
      () => route.path,
      (newPath) => {
        activeMenu.value = newPath
        updateBreadcrumb()
      },
      { immediate: true }
    )
    
    // 生命周期钩子
    onMounted(() => {
      checkIsMobile()
      window.addEventListener('resize', checkIsMobile)
      
      // 更新时间
      updateTime()
      timer = setInterval(updateTime, 1000)
    })
    
    onBeforeUnmount(() => {
      window.removeEventListener('resize', checkIsMobile)
      // 清除定时器
      if (timer) {
        clearInterval(timer)
      }
    })
    
    return {
      userStore,
      router,
      route,
      sidebarCollapsed,
      isMobile,
      activeMenu,
      breadcrumbList,
      currentTime,
      userInfo,
      getUserAvatar,
      sidebarWidth,
      toggleSidebar,
      handleMenuSelect,
      updateBreadcrumb,
      updateTime,
      handleUserCommand,
      getBreadcrumbHome,
      getBreadcrumbHomeText
    }
  }
}
</script>

<style scoped>
.system-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: var(--system-font-family);
  font-size: var(--system-font-size-md);
  line-height: var(--system-line-height-md);
  color: var(--system-text-primary);
  background-color: var(--system-bg-light);
  transition: all 0.3s ease;
}

.system-header {
  height: 64px;
  background-color: var(--system-bg-white);
  box-shadow: var(--system-shadow-md);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--system-space-lg);
  z-index: 1000;
  position: relative;
  overflow: hidden;
}

.system-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--primary-color), var(--primary-light), var(--primary-color));
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

.header-left .logo {
  margin: 0;
  font-size: var(--system-font-size-xl);
  font-weight: 600;
  color: var(--system-primary-color);
  display: flex;
  align-items: center;
  transition: transform 0.3s ease;
}

.header-left .logo:hover {
  transform: scale(1.05);
}

.header-left .logo::before {
  content: '🏥';
  margin-right: var(--system-space-sm);
  font-size: 1.5em;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: var(--system-space-sm) var(--system-space-md);
  border-radius: var(--system-border-radius-lg);
  transition: all 0.3s ease;
  position: relative;
}

.user-info:hover {
  background-color: var(--system-bg-light);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-info::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background-color: var(--primary-color);
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.user-info:hover::after {
  width: 80%;
}

.username {
  margin: 0 var(--system-space-sm);
  font-size: var(--system-font-size-sm);
  color: var(--system-text-primary);
  font-weight: 500;
}

.system-container {
  flex: 1;
  display: flex;
  position: relative;
  overflow: hidden;
}

.system-sidebar {
  background-color: var(--system-bg-white);
  box-shadow: var(--system-shadow-sm);
  transition: all 0.3s ease;
  overflow-y: auto;
  height: 100%;
  z-index: 100;
}

.system-sidebar.sidebar-collapsed {
  width: 64px;
}

.system-sidebar.mobile-open {
  transform: translateX(0);
}

.system-menu {
  border-right: none;
  height: 100%;
}

.system-menu .el-sub-menu__title:hover,
.system-menu .el-menu-item:hover {
  background-color: var(--system-bg-light);
}

.system-menu .el-menu-item.is-active {
  background-color: var(--system-primary-light);
  color: var(--system-primary-color);
}

.sidebar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 50;
}

.system-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--system-bg-light);
}

.system-breadcrumb {
  padding: var(--system-space-md) var(--system-space-lg);
  background-color: var(--system-bg-white);
  box-shadow: var(--system-shadow-sm);
}

.system-content {
  flex: 1;
  padding: var(--system-space-lg);
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .system-sidebar {
    position: absolute;
    top: 0;
    left: 0;
    height: 100%;
    transform: translateX(-100%);
    width: 240px;
    z-index: 200;
  }
  
  .system-header {
    padding: 0 var(--system-space-md);
  }
  
  .logo {
    font-size: var(--system-font-size-lg) !important;
  }
  
  .system-content {
    padding: var(--system-space-md);
  }
}

/* 过渡动画 */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(20px);
  opacity: 0;
}
</style>





