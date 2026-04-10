<template>
  <div
    :class="[
      'elderly-interactive',
      { 'elderly-interactive--pressed': isPressed },
      { 'elderly-interactive--disabled': disabled }
    ]"
    @mousedown="handleMouseDown"
    @mouseup="handleMouseUp"
    @mouseleave="handleMouseLeave"
    @touchstart="handleTouchStart"
    @touchend="handleTouchEnd"
    @touchcancel="handleTouchCancel"
    @click="handleClick"
  >
    <slot></slot>
    
    <!-- 涟漪效果 -->
    <div
      v-if="ripple"
      class="elderly-ripple-container"
    >
      <span
        v-for="(ripple, index) in ripples"
        :key="index"
        class="elderly-ripple"
        :style="ripple.style"
      ></span>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

/**
 * 交互效果组件
 * 为老年用户提供友好的交互反馈，包括涟漪效果和点击反馈
 */

const props = defineProps({
  ripple: {
    type: Boolean,
    default: true,
    description: '是否启用涟漪效果'
  },
  disabled: {
    type: Boolean,
    default: false,
    description: '是否禁用交互'
  },
  scale: {
    type: Boolean,
    default: true,
    description: '是否启用缩放效果'
  },
  highlight: {
    type: Boolean,
    default: true,
    description: '是否启用高亮效果'
  }
})

const emit = defineEmits(['click'])

const isPressed = ref(false)
const ripples = reactive([])

/**
 * 处理鼠标按下事件
 */
const handleMouseDown = () => {
  if (props.disabled) return
  isPressed.value = true
}

/**
 * 处理鼠标抬起事件
 */
const handleMouseUp = () => {
  if (props.disabled) return
  isPressed.value = false
}

/**
 * 处理鼠标离开事件
 */
const handleMouseLeave = () => {
  if (props.disabled) return
  isPressed.value = false
}

/**
 * 处理触摸开始事件
 * @param {Object} event - 触摸事件对象
 */
const handleTouchStart = (event) => {
  if (props.disabled) return
  isPressed.value = true
  
  if (props.ripple) {
    createRipple(event.touches[0])
  }
}

/**
 * 处理触摸结束事件
 */
const handleTouchEnd = () => {
  if (props.disabled) return
  isPressed.value = false
}

/**
 * 处理触摸取消事件
 */
const handleTouchCancel = () => {
  if (props.disabled) return
  isPressed.value = false
}

/**
 * 处理点击事件
 * @param {Object} event - 点击事件对象
 */
const handleClick = (event) => {
  if (props.disabled) return
  
  if (props.ripple) {
    createRipple(event)
  }
  
  emit('click', event)
}

/**
 * 创建涟漪效果
 * @param {Object} event - 事件对象
 */
const createRipple = (event) => {
  const target = event.currentTarget || event.target
  const rect = target.getBoundingClientRect()
  const size = Math.max(rect.width, rect.height)
  const x = event.clientX - rect.left - size / 2
  const y = event.clientY - rect.top - size / 2
  
  const ripple = {
    style: {
      width: `${size}px`,
      height: `${size}px`,
      left: `${x}px`,
      top: `${y}px`
    }
  }
  
  ripples.push(ripple)
  
  // 动画结束后移除涟漪
  setTimeout(() => {
    const index = ripples.indexOf(ripple)
    if (index > -1) {
      ripples.splice(index, 1)
    }
  }, 600)
}
</script>

<style scoped>
.elderly-interactive {
  position: relative;
  display: inline-block;
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.elderly-interactive--pressed {
  transform: scale(0.98);
}

.elderly-interactive--disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.elderly-ripple-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}

.elderly-ripple {
  position: absolute;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.5);
  transform: scale(0);
  animation: ripple-animation 0.6s ease-out;
  pointer-events: none;
}

@keyframes ripple-animation {
  to {
    transform: scale(4);
    opacity: 0;
  }
}

/* 悬停效果 */
.elderly-interactive:not(.elderly-interactive--disabled):hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 焦点效果 */
.elderly-interactive:focus {
  outline: 2px solid var(--elderly-primary-color);
  outline-offset: 2px;
}
</style>
