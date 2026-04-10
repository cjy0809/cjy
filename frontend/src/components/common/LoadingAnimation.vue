<template>
  <div class="elderly-loading-container">
    <div class="elderly-loading" :class="sizeClass">
      <!-- 默认加载动画 -->
      <div v-if="type === 'default'" class="elderly-loading__default">
        <div class="elderly-loading__dot" v-for="i in 3" :key="i" :style="{ animationDelay: `${i * 0.2}s` }"></div>
      </div>
      
      <!-- 圆形加载动画 -->
      <div v-else-if="type === 'circle'" class="elderly-loading__circle">
        <div class="elderly-loading__circle-inner"></div>
      </div>
      
      <!-- 波浪加载动画 -->
      <div v-else-if="type === 'wave'" class="elderly-loading__wave">
        <div class="elderly-loading__wave-bar" v-for="i in 5" :key="i" :style="{ animationDelay: `${i * 0.1}s` }"></div>
      </div>
      
      <!-- 脉冲加载动画 -->
      <div v-else-if="type === 'pulse'" class="elderly-loading__pulse">
        <div class="elderly-loading__pulse-dot"></div>
      </div>
      
      <!-- 骨架屏加载动画 -->
      <div v-else-if="type === 'skeleton'" class="elderly-loading__skeleton">
        <div class="elderly-loading__skeleton-line" v-for="i in lines" :key="i" :style="{ width: i === lines ? '60%' : '100%' }"></div>
      </div>
    </div>
    
    <!-- 加载文本 -->
    <div v-if="text" class="elderly-loading__text">{{ text }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

/**
 * 加载动画组件
 * 为老年用户提供多种友好的加载动画效果
 */

const props = defineProps({
  type: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'circle', 'wave', 'pulse', 'skeleton'].includes(value),
    description: '加载动画类型'
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value),
    description: '加载动画尺寸'
  },
  text: {
    type: String,
    default: '',
    description: '加载提示文本'
  },
  lines: {
    type: Number,
    default: 3,
    description: '骨架屏行数（仅skeleton类型有效）'
  }
})

/**
 * 计算尺寸类名
 * @returns {String} - 尺寸类名
 */
const sizeClass = computed(() => `elderly-loading--${props.size}`)
</script>

<style scoped>
.elderly-loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--elderly-space-md);
}

.elderly-loading {
  display: flex;
  align-items: center;
  justify-content: center;
}

.elderly-loading--small {
  width: 24px;
  height: 24px;
}

.elderly-loading--medium {
  width: 40px;
  height: 40px;
}

.elderly-loading--large {
  width: 60px;
  height: 60px;
}

.elderly-loading__text {
  margin-top: var(--elderly-space-sm);
  font-size: var(--elderly-font-size-sm);
  color: var(--elderly-text-secondary);
}

/* 默认加载动画 */
.elderly-loading__default {
  display: flex;
  gap: 6px;
}

.elderly-loading__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: var(--elderly-primary-color);
  animation: dot-bounce 1.4s infinite ease-in-out both;
}

@keyframes dot-bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* 圆形加载动画 */
.elderly-loading__circle {
  position: relative;
  width: 100%;
  height: 100%;
}

.elderly-loading__circle-inner {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: 3px solid var(--elderly-border-color);
  border-top: 3px solid var(--elderly-primary-color);
  border-radius: 50%;
  animation: circle-spin 1s linear infinite;
}

@keyframes circle-spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 波浪加载动画 */
.elderly-loading__wave {
  display: flex;
  gap: 4px;
  align-items: flex-end;
}

.elderly-loading__wave-bar {
  width: 4px;
  background-color: var(--elderly-primary-color);
  border-radius: 2px;
  animation: wave-stretch 1.2s infinite ease-in-out;
}

.elderly-loading--small .elderly-loading__wave-bar {
  height: 16px;
}

.elderly-loading--medium .elderly-loading__wave-bar {
  height: 24px;
}

.elderly-loading--large .elderly-loading__wave-bar {
  height: 32px;
}

@keyframes wave-stretch {
  0%, 40%, 100% {
    transform: scaleY(0.4);
  }
  20% {
    transform: scaleY(1);
  }
}

/* 脉冲加载动画 */
.elderly-loading__pulse {
  position: relative;
  width: 100%;
  height: 100%;
}

.elderly-loading__pulse-dot {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 50%;
  height: 50%;
  background-color: var(--elderly-primary-color);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: pulse-pulse 1.5s infinite ease-in-out;
}

@keyframes pulse-pulse {
  0% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(1.5);
    opacity: 0;
  }
}

/* 骨架屏加载动画 */
.elderly-loading__skeleton {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.elderly-loading__skeleton-line {
  height: 12px;
  background-color: var(--elderly-background-secondary);
  border-radius: 6px;
  position: relative;
  overflow: hidden;
}

.elderly-loading__skeleton-line::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.4), 
    transparent
  );
  animation: skeleton-shimmer 1.5s infinite;
}

@keyframes skeleton-shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}
</style>
