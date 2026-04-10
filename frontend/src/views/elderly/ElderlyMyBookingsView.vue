<template>
  <div class="elderly-my-bookings">
    <!-- 页面标题 -->
    <div class="elderly-card">
      <div class="elderly-card-header">
        <h2 class="elderly-card-title">我的预约</h2>
      </div>
    </div>

    <!-- 选项卡 -->
    <div class="elderly-tabs">
      <div 
        class="elderly-tab" 
        :class="{ active: activeTab === 'activities' }"
        @click="activeTab = 'activities'"
      >
        <span class="tab-icon">🎉</span>
        <span>活动报名</span>
      </div>
      <div 
        class="elderly-tab" 
        :class="{ active: activeTab === 'services' }"
        @click="activeTab = 'services'"
      >
        <span class="tab-icon">🤝</span>
        <span>服务预约</span>
      </div>
      <div 
        class="elderly-tab" 
        :class="{ active: activeTab === 'venues' }"
        @click="activeTab = 'venues'"
      >
        <span class="tab-icon">🏢</span>
        <span>场地预约</span>
      </div>
    </div>

    <!-- 选项卡内容 -->
    <div class="tab-content">
      <!-- 活动预约内容 -->
      <div v-if="activeTab === 'activities'" class="tab-pane">
        <ElderlyActivityBookings />
      </div>
      
      <!-- 服务预约内容 -->
      <div v-if="activeTab === 'services'" class="tab-pane">
        <ElderlyServiceBookings />
      </div>
      
      <!-- 场地预约内容 -->
      <div v-if="activeTab === 'venues'" class="tab-pane">
        <ElderlyVenueBookings />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import ElderlyActivityBookings from './components/ElderlyActivityBookings.vue'
import ElderlyServiceBookings from './components/ElderlyServiceBookings.vue'
import ElderlyVenueBookings from './components/ElderlyVenueBookings.vue'

export default {
  name: 'ElderlyMyBookingsView',
  components: {
    ElderlyActivityBookings,
    ElderlyServiceBookings,
    ElderlyVenueBookings
  },
  setup() {
    const userStore = useUserStore()
    const activeTab = ref('activities')
    
    onMounted(() => {
      // 页面加载时可以设置默认选项卡
      // 例如根据用户最近的预约类型来设置
    })
    
    return {
      activeTab
    }
  }
}
</script>

<style scoped>
.elderly-my-bookings {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.elderly-tabs {
  display: flex;
  margin-bottom: 20px;
  background-color: #f8f9fa;
  border-radius: 12px;
  padding: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.elderly-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 18px;
  font-weight: 500;
  color: #666;
  background-color: transparent;
  border: none;
  margin: 0 4px;
  position: relative;
  overflow: hidden;
}

.elderly-tab::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.elderly-tab:hover::before {
  left: 100%;
}

.elderly-tab:hover {
  background-color: rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.elderly-tab.active {
  background-color: #409eff;
  color: white;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transform: translateY(-2px);
}

.tab-icon {
  margin-right: 8px;
  font-size: 20px;
  transition: transform 0.3s ease;
}

.elderly-tab:hover .tab-icon {
  transform: scale(1.1);
}

.elderly-tab.active .tab-icon {
  transform: scale(1.2);
}

.tab-content {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  min-height: 400px;
}

.tab-pane {
  padding: 0;
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from { 
    opacity: 0; 
    transform: translateY(20px); 
  }
  to { 
    opacity: 1; 
    transform: translateY(0); 
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .elderly-my-bookings {
    padding: 15px;
  }
  
  .elderly-tab {
    padding: 14px 16px;
    font-size: 16px;
  }
  
  .tab-icon {
    font-size: 18px;
  }
}

/* 增强的焦点样式 */
.elderly-tab:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.3);
}

.elderly-tab.active:focus {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3), 0 0 0 3px rgba(64, 158, 255, 0.3);
}
</style>