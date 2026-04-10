<template>
  <div class="elderly-layout">
    <!-- 公告区域 -->
    <div class="elderly-announcement" @mouseenter="pauseRotation" @mouseleave="resumeRotation">
      <span class="announcement-icon">📢</span>
      <transition name="announcement-text" mode="out-in">
        <span class="announcement-text" v-if="announcements.length > 0" :key="currentNewsIndex">
          {{ announcements[currentNewsIndex].content }}
        </span>
        <span class="announcement-text" v-else key="empty">
          暂无公告信息
        </span>
      </transition>
      <div class="announcement-controls" v-if="announcements.length > 1">
        <span class="control-btn" @click="prevNews">‹</span>
        <span class="control-btn" @click="nextNews">›</span>
      </div>
    </div>
    
    <!-- 顶部导航栏 -->
    <header class="elderly-header">
      <!-- 左侧：系统名称 -->
      <div class="elderly-logo">
        <span class="elderly-logo-icon">🏥</span>
        <span>社区老年活动中心</span>
      </div>
      
      <!-- 中间：菜单栏 -->
      <nav class="elderly-nav">
        <div 
          class="elderly-nav-item" 
          :class="{ active: activeNav === 'home' }"
          @click="navigateTo('/elderly/home')"
        >
          <span>首页</span>
        </div>
        <div 
          class="elderly-nav-item" 
          :class="{ active: activeNav === 'activities' }"
          @click="navigateTo('/elderly/activities')"
        >
          <span>活动</span>
        </div>
        <div 
          class="elderly-nav-item" 
          :class="{ active: activeNav === 'news' }"
          @click="navigateTo('/elderly/news')"
        >
          <span>新闻</span>
        </div>
        <div 
          class="elderly-nav-item" 
          :class="{ active: activeNav === 'services' }"
          @click="navigateTo('/elderly/services')"
        >
          <span>服务</span>
        </div>
        <div 
          class="elderly-nav-item" 
          :class="{ active: activeNav === 'venues' }"
          @click="navigateTo('/elderly/venues')"
        >
          <span>场地</span>
        </div>
        <div 
          class="elderly-nav-item" 
          :class="{ active: activeNav === 'health' }"
          @click="navigateTo('/elderly/health')"
        >
          <span>健康记录</span>
        </div>
        
        <div 
          class="elderly-nav-item" 
          :class="{ active: activeNav === 'bookings' }"
          @click="navigateTo('/elderly/bookings')"
        >
          <span>我的预约</span>
        </div>
      </nav>
      
      <!-- 右侧：用户菜单 -->
      <div class="elderly-user-info">
        <el-dropdown @command="handleCommand" trigger="click" placement="bottom-end">
          <div class="elderly-user-dropdown">
            <el-avatar :size="32" :src="getUserAvatar" class="elderly-user-avatar">{{ userInfo.name.substring(0, 1) }}</el-avatar>
            <span class="elderly-username">{{ userInfo.name || '老年用户' }}</span>
            <span class="dropdown-arrow">▼</span>
          </div>
          <template #dropdown>
            <el-dropdown-item command="profile">
              <span class="dropdown-item-icon">👤</span>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item command="recharge">
              <span class="dropdown-item-icon">💳</span>
              充值记录
            </el-dropdown-item>
            <el-dropdown-item command="favorites">
              <span class="dropdown-item-icon">⭐</span>
              我的收藏
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <span class="dropdown-item-icon">🚪</span>
              退出登录
            </el-dropdown-item>
          </template>
        </el-dropdown>
      </div>
    </header>
    
    <!-- 主要内容区域 -->
    <main class="elderly-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" v-if="Component" />
          <div v-else class="loading-container">
            <el-skeleton animated />
          </div>
        </transition>
      </router-view>
    </main>
    
    <!-- 操作指引组件 -->
    <UserGuide />
  </div>
</template>

<script>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { newsApi } from '@/api/news'
import UserGuide from '@/components/common/UserGuide.vue'

export default {
  name: 'ElderlyLayout',
  components: {
    UserGuide
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const userStore = useUserStore()
    const activeNav = ref('home')
    
    // 公告相关数据
    const announcements = ref([])
    const currentNewsIndex = ref(0)
    const rotationTimer = ref(null)
    const isPaused = ref(false)
    
    // 计算属性：用户信息
    const userInfo = computed(() => {
      return userStore.userInfo || { name: '老年用户', role: 'ELDERLY' }
    })
    
    // 获取用户头像，如果没有则使用默认头像
    const getUserAvatar = computed(() => {
      if (userInfo.value.avatar) {
        return userInfo.value.avatar
      } else {
        // 使用默认头像
        return '/uploads/avatars/default-avatar.jpg'
      }
    })
    
    // 导航到指定页面
    const navigateTo = (path) => {
      router.push(path)
    }
    
    // 处理下拉菜单命令
    const handleCommand = (command) => {
      if (command === 'profile') {
        navigateTo('/elderly/profile')
      } else if (command === 'recharge') {
        navigateTo('/elderly/recharge')
      } else if (command === 'favorites') {
        navigateTo('/elderly/favorites')
      } else if (command === 'logout') {
        handleLogout()
      }
    }
    
    // 处理退出登录
    const handleLogout = () => {
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await userStore.userLogout()
          ElMessage.success('退出登录成功')
          // 跳转到登录页
          router.push('/login')
        } catch (error) {
          ElMessage.error('退出登录失败')
        }
      }).catch(() => {
        // 取消退出
      })
    }
    
    // 获取公告数据
    const fetchAnnouncements = async () => {
      try {
        const response = await newsApi.getAnnouncements(5) // 获取最新5条公告
        announcements.value = response || []
        
        // 如果有公告数据，启动轮播
        if (announcements.value.length > 1) {
          startRotation()
        }
      } catch (error) {
        announcements.value = []
      }
    }
    
    // 启动轮播
    const startRotation = () => {
      if (rotationTimer.value) {
        clearInterval(rotationTimer.value)
      }
      
      rotationTimer.value = setInterval(() => {
        if (!isPaused.value) {
          nextNews()
        }
      }, 10000) // 每10秒切换一次
    }
    
    // 停止轮播
    const stopRotation = () => {
      if (rotationTimer.value) {
        clearInterval(rotationTimer.value)
        rotationTimer.value = null
      }
    }
    
    // 暂停轮播
    const pauseRotation = () => {
      isPaused.value = true
    }
    
    // 恢复轮播
    const resumeRotation = () => {
      isPaused.value = false
    }
    
    // 下一条新闻
    const nextNews = () => {
      if (announcements.value.length === 0) return
      
      currentNewsIndex.value = (currentNewsIndex.value + 1) % announcements.value.length
    }
    
    // 上一条新闻
    const prevNews = () => {
      if (announcements.value.length === 0) return
      
      currentNewsIndex.value = currentNewsIndex.value === 0 
        ? announcements.value.length - 1 
        : currentNewsIndex.value - 1
    }
    
    // 更新当前活动导航
    const updateActiveNav = () => {
      const path = route.path
      if (path === '/elderly/home' || path === '/elderly') {
        activeNav.value = 'home'
      } else if (path.startsWith('/elderly/activities')) {
        activeNav.value = 'activities'
      } else if (path.startsWith('/elderly/news')) {
        activeNav.value = 'news'
      } else if (path.startsWith('/elderly/services')) {
        activeNav.value = 'services'
      } else if (path.startsWith('/elderly/health')) {
        activeNav.value = 'health'
      } else if (path.startsWith('/elderly/venues')) {
        activeNav.value = 'venues'
      } else if (path.startsWith('/elderly/bookings')) {
        activeNav.value = 'bookings'
      } else if (path.startsWith('/elderly/favorites')) {
        activeNav.value = 'favorites'
      } else if (path.startsWith('/elderly/profile')) {
        activeNav.value = 'profile'
      } else if (path.startsWith('/elderly/recharge')) {
        activeNav.value = 'recharge'
      } else {
        activeNav.value = 'home'
      }
    }
    
    // 监听路由变化
    watch(
      () => route.path,
      () => {
        updateActiveNav()
      },
      { immediate: true }
    )
    
    // 生命周期钩子
    onMounted(() => {
      updateActiveNav()
      fetchAnnouncements() // 获取公告数据
    })
    
    // 组件卸载时清理定时器
    onUnmounted(() => {
      stopRotation()
    })
    
    return {
      activeNav,
      userInfo,
      getUserAvatar,
      navigateTo,
      handleCommand,
      handleLogout,
      // 公告相关
      announcements,
      currentNewsIndex,
      pauseRotation,
      resumeRotation,
      nextNews,
      prevNews
    }
  }
}
</script>

<style scoped>
/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>