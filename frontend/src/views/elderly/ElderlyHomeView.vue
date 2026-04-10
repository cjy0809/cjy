<template>
  <div class="elderly-home">
    <!-- 欢迎区域 -->
    <div class="elderly-card">
      <div class="elderly-card-header">
        <h2 class="elderly-card-title">欢迎回来，{{ userInfo.name || '老年朋友' }}！</h2>
      </div>
      <div class="elderly-card-body">
        <p class="welcome-message">今天是{{ currentDate }}，祝您身体健康，生活愉快！</p>
      </div>
    </div>
    
    <!-- 功能快捷入口 -->
    <div class="elderly-feature-grid">
      <div class="elderly-feature-card" @click="navigateTo('/elderly/activities')">
        <div class="elderly-feature-icon">🎉</div>
        <h3 class="elderly-feature-title">活动中心</h3>
        <p class="elderly-feature-desc">查看和报名社区活动，与朋友一起参与</p>
      </div>
      
      <div class="elderly-feature-card" @click="navigateTo('/elderly/services')">
        <div class="elderly-feature-icon">🤝</div>
        <h3 class="elderly-feature-title">社区服务</h3>
        <p class="elderly-feature-desc">预约社区服务，享受贴心关怀</p>
      </div>
      
      <div class="elderly-feature-card" @click="navigateTo('/elderly/health')">
        <div class="elderly-feature-icon">❤️</div>
        <h3 class="elderly-feature-title">健康记录</h3>
        <p class="elderly-feature-desc">查看健康数据，关注身体状况</p>
      </div>
      
      <div class="elderly-feature-card" @click="navigateTo('/elderly/venues')">
        <div class="elderly-feature-icon">🏛️</div>
        <h3 class="elderly-feature-title">场地预约</h3>
        <p class="elderly-feature-desc">预约活动场地，组织聚会活动</p>
      </div>
      
      <div class="elderly-feature-card" @click="navigateTo('/elderly/news')">
        <div class="elderly-feature-icon">📰</div>
        <h3 class="elderly-feature-title">新闻资讯</h3>
        <p class="elderly-feature-desc">了解社区最新动态和政策解读</p>
      </div>
    </div>
  
    

    
    <!-- 健康小贴士 -->
    <div class="elderly-card">
      <div class="elderly-card-header">
        <h2 class="elderly-card-title">健康小贴士</h2>
      </div>
      <div class="elderly-card-body">
        <div class="health-tip">
          <div class="tip-header" @click="showHealthTip = !showHealthTip">
            <h4 class="tip-title">{{ healthTip.title }}</h4>
            <span class="tip-toggle">{{ showHealthTip ? '收起' : '展开' }}</span>
          </div>
          <div v-show="showHealthTip" class="tip-content">
            <p>{{ healthTip.content }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

export default {
  name: 'ElderlyHomeView',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const currentDate = ref('')
    const showHealthTip = ref(false)
    const healthTip = ref({
      title: '适量运动，保持健康',
      content: '每天坚持30分钟以上的适度运动，如散步、太极拳等，有助于增强体质，提高免疫力。运动时要注意循序渐进，避免过度劳累。'
    })
    
    // 计算属性：用户信息
    const userInfo = computed(() => {
      return userStore.userInfo || { name: '老年朋友', role: 'ELDERLY' }
    })
    
    // 导航到指定页面
    const navigateTo = (path) => {
      router.push(path)
    }
    
    // 格式化当前日期
    const formatDate = () => {
      const now = new Date()
      const year = now.getFullYear()
      const month = now.getMonth() + 1
      const day = now.getDate()
      const weekDay = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][now.getDay()]
      currentDate.value = `${year}年${month}月${day}日 ${weekDay}`
    }
    
    // 随机获取健康小贴士
    const getRandomHealthTip = () => {
      const tips = [
        {
          title: '适量运动，保持健康',
          content: '每天坚持30分钟以上的适度运动，如散步、太极拳等，有助于增强体质，提高免疫力。运动时要注意循序渐进，避免过度劳累。'
        },
        {
          title: '合理饮食，均衡营养',
          content: '老年人应多吃蔬菜水果，适量摄入蛋白质，减少油腻和高盐食物。饮食要规律，不暴饮暴食，保持健康体重。'
        },
        {
          title: '规律作息，充足睡眠',
          content: '保持规律的作息时间，每天保证7-8小时的充足睡眠。午休时间不宜过长，避免影响夜间睡眠质量。'
        },
        {
          title: '心理健康，积极乐观',
          content: '保持积极乐观的心态，多与家人朋友交流。可以参加社区活动，培养兴趣爱好，丰富精神生活。'
        }
      ]
      
      const randomIndex = Math.floor(Math.random() * tips.length)
      healthTip.value = tips[randomIndex]
      showHealthTip.value = false
    }
    
    // 生命周期钩子
    onMounted(() => {
      formatDate()
      getRandomHealthTip()
    })
    
    return {
      userInfo,
      currentDate,
      healthTip,
      showHealthTip,
      navigateTo
    }
  }
}
</script>

<style scoped>
.elderly-home {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-message {
  font-size: var(--elderly-font-size-lg);
  color: var(--elderly-text-secondary);
  line-height: var(--elderly-line-height-lg);
}

.health-tip {
  background-color: var(--elderly-info-light);
  border-left: 4px solid var(--elderly-info-color);
  padding: var(--elderly-space-md);
  border-radius: var(--elderly-border-radius-md);
}

.tip-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.tip-title {
  font-size: var(--elderly-font-size-lg);
  font-weight: 600;
  color: var(--elderly-info-color);
  margin: 0;
}

.tip-toggle {
  color: var(--elderly-primary-color);
  font-size: 12px;
  cursor: pointer;
}

.tip-toggle:hover {
  color: var(--elderly-primary-dark);
}

.tip-content {
  margin-top: 12px;
  line-height: var(--elderly-line-height-lg);
  color: var(--elderly-text-secondary);
}

.empty-state {
  text-align: center;
  padding: var(--elderly-space-xl) 0;
  color: var(--elderly-text-tertiary);
  font-size: var(--elderly-font-size-lg);
}
</style>