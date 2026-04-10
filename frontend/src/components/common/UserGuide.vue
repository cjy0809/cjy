<template>
  <div class="user-guide" v-if="showGuide">
    <div class="guide-button" @click="toggleGuide">
      <span class="guide-icon">📖</span>
      <span class="guide-text">操作指引</span>
    </div>

    <transition name="guide-panel">
      <div class="guide-panel" v-show="isGuideOpen">
        <div class="guide-header">
          <h3 class="guide-title">{{ currentGuide.title }}</h3>
          <button class="guide-close" @click="toggleGuide" title="关闭指引">×</button>
        </div>

        <div class="guide-content">
          <div class="guide-tips" v-if="currentGuide.tips">
            <span class="tips-icon">💡</span>
            <span class="tips-text">{{ currentGuide.tips }}</span>
          </div>

          <div class="guide-steps">
            <div 
              class="guide-step" 
              v-for="(step, index) in currentGuide.steps" 
              :key="index"
            >
              <span class="step-number">{{ index + 1 }}</span>
              <span class="step-icon">{{ step.icon }}</span>
              <span class="step-text">{{ step.text }}</span>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useGuideStore } from '@/stores/guide'
import { getGuideByPath } from '@/data/guideData'

/**
 * 用户指引组件
 * 为老年用户提供操作指引，帮助用户更好地使用应用功能
 */

export default {
  name: 'UserGuide',
  setup() {
    const route = useRoute()
    const guideStore = useGuideStore()

    /**
     * 当前页面的指引信息
     * @returns {Object} - 指引信息对象
     */
    const currentGuide = computed(() => {
      const path = route.path
      return getGuideByPath(path) || {
        title: '操作指引',
        steps: [],
        tips: '暂无该页面的操作指引'
      }
    })

    /**
     * 是否显示指引按钮
     * @returns {Boolean} - 是否显示
     */
    const showGuide = computed(() => {
      if (!currentGuide.value || !currentGuide.value.steps || currentGuide.value.steps.length === 0) {
        return false
      }
      return true
    })

    /**
     * 指引面板的显示状态
     */
    const isGuideOpen = computed({
      get() {
        return guideStore.isGuideVisible
      },
      set(value) {
        guideStore.isGuideVisible = value
      }
    })

    /**
     * 切换指引面板的显示状态
     */
    const toggleGuide = () => {
      guideStore.toggleGuide()
    }

    // 监听路由变化，关闭指引面板
    watch(() => route.path, () => {
      isGuideOpen.value = false
    })

    return {
      showGuide,
      isGuideOpen,
      currentGuide,
      toggleGuide
    }
  }
}
</script>

<style scoped>
.user-guide {
  position: fixed;
  bottom: 80px;
  right: 20px;
  z-index: 1000;
}

.guide-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 80px;
  background-color: #4a90e2;
  color: white;
  border-radius: 8px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(74, 144, 226, 0.3);
  transition: all 0.3s ease;
  user-select: none;
}

.guide-button:hover {
  background-color: #357abd;
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.4);
}

.guide-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.guide-text {
  font-size: 12px;
  font-weight: 500;
}

.guide-panel {
  position: fixed;
  bottom: 140px;
  right: 20px;
  width: 400px;
  max-width: 90vw;
  max-height: 70vh;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  z-index: 999;
}

.guide-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background-color: #4a90e2;
  color: white;
  border-radius: 12px 12px 0 0;
}

.guide-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
}

.guide-close {
  background: none;
  border: none;
  color: white;
  font-size: 28px;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.guide-close:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.guide-content {
  padding: 20px;
  max-height: calc(70vh - 80px);
  overflow-y: auto;
}

.guide-tips {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background-color: #f0f9ff;
  border-left: 4px solid #667eea;
  border-radius: 8px;
  margin-bottom: 20px;
}

.tips-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.tips-text {
  font-size: 15px;
  color: #333;
  line-height: 1.6;
}

.guide-steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.guide-step {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.guide-step:hover {
  background-color: #e9ecef;
  transform: translateX(-4px);
}

.step-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background-color: #4a90e2;
  color: white;
  border-radius: 50%;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}

.step-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.step-text {
  font-size: 15px;
  color: #333;
  line-height: 1.5;
  flex: 1;
}

.guide-panel-enter-active,
.guide-panel-leave-active {
  transition: all 0.3s ease;
}

.guide-panel-enter-from,
.guide-panel-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}

.guide-panel-enter-to,
.guide-panel-leave-from {
  opacity: 1;
  transform: translateY(0) scale(1);
}

@media (max-width: 768px) {
  .user-guide {
    bottom: 60px;
    right: 10px;
  }

  .guide-button {
    width: 45px;
    height: 45px;
  }

  .guide-icon {
    font-size: 22px;
  }

  .guide-panel {
    width: 350px;
    max-width: 95vw;
    bottom: 120px;
    right: 10px;
  }

  .guide-header {
    padding: 16px;
  }

  .guide-title {
    font-size: 16px;
  }

  .guide-content {
    padding: 16px;
    max-height: calc(70vh - 70px);
  }

  .guide-step {
    padding: 10px;
  }

  .step-number {
    width: 24px;
    height: 24px;
    font-size: 12px;
  }

  .step-icon {
    font-size: 18px;
  }

  .step-text {
    font-size: 14px;
  }

  .tips-text {
    font-size: 14px;
  }
}
</style>
