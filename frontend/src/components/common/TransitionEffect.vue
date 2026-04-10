<template>
  <transition
    :name="transitionName"
    @before-enter="beforeEnter"
    @enter="enter"
    @after-enter="afterEnter"
    @before-leave="beforeLeave"
    @leave="leave"
    @after-leave="afterLeave"
  >
    <slot></slot>
  </transition>
</template>

<script setup>
import { ref, computed } from 'vue'

/**
 * 过渡效果组件
 * 为老年用户提供友好的过渡动画效果
 */

const props = defineProps({
  type: {
    type: String,
    default: 'fade',
    validator: (value) => ['fade', 'slide-up', 'slide-down', 'slide-left', 'slide-right', 'zoom', 'flip'].includes(value),
    description: '过渡动画类型'
  },
  duration: {
    type: [Number, Object],
    default: 300,
    description: '动画持续时间（毫秒）'
  },
  easing: {
    type: String,
    default: 'ease',
    description: '动画缓动函数'
  }
})

/**
 * 计算过渡名称
 * @returns {String} - 过渡名称
 */
const transitionName = computed(() => `elderly-${props.type}`)

/**
 * 计算动画持续时间
 * @returns {Object} - 进入和离开的持续时间
 */
const duration = computed(() => {
  if (typeof props.duration === 'number') {
    return {
      enter: props.duration,
      leave: props.duration
    }
  }
  return {
    enter: props.duration.enter || 300,
    leave: props.duration.leave || 300
  }
})

/**
 * 进入前钩子
 * @param {HTMLElement} el - 元素节点
 */
const beforeEnter = (el) => {
  el.style.transitionDuration = `${duration.value.enter}ms`
  el.style.transitionTimingFunction = props.easing
}

/**
 * 进入钩子
 * @param {HTMLElement} el - 元素节点
 */
const enter = (el) => {
  // 动画进入时的处理
}

/**
 * 进入后钩子
 * @param {HTMLElement} el - 元素节点
 */
const afterEnter = (el) => {
  // 动画进入完成后的处理
}

/**
 * 离开前钩子
 * @param {HTMLElement} el - 元素节点
 */
const beforeLeave = (el) => {
  el.style.transitionDuration = `${duration.value.leave}ms`
  el.style.transitionTimingFunction = props.easing
}

/**
 * 离开钩子
 * @param {HTMLElement} el - 元素节点
 */
const leave = (el) => {
  // 动画离开时的处理
}

/**
 * 离开后钩子
 * @param {HTMLElement} el - 元素节点
 */
const afterLeave = (el) => {
  // 动画离开完成后的处理
}
</script>

<style scoped>
/* 淡入淡出动画 */
.elderly-fade-enter-active,
.elderly-fade-leave-active {
  transition: opacity var(--elderly-transition-duration) var(--elderly-transition-timing);
}

.elderly-fade-enter-from,
.elderly-fade-leave-to {
  opacity: 0;
}

/* 上滑动画 */
.elderly-slide-up-enter-active,
.elderly-slide-up-leave-active {
  transition: all var(--elderly-transition-duration) var(--elderly-transition-timing);
}

.elderly-slide-up-enter-from {
  opacity: 0;
  transform: translateY(30px);
}

.elderly-slide-up-leave-to {
  opacity: 0;
  transform: translateY(-30px);
}

/* 下滑动画 */
.elderly-slide-down-enter-active,
.elderly-slide-down-leave-active {
  transition: all var(--elderly-transition-duration) var(--elderly-transition-timing);
}

.elderly-slide-down-enter-from {
  opacity: 0;
  transform: translateY(-30px);
}

.elderly-slide-down-leave-to {
  opacity: 0;
  transform: translateY(30px);
}

/* 左滑动画 */
.elderly-slide-left-enter-active,
.elderly-slide-left-leave-active {
  transition: all var(--elderly-transition-duration) var(--elderly-transition-timing);
}

.elderly-slide-left-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.elderly-slide-left-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* 右滑动画 */
.elderly-slide-right-enter-active,
.elderly-slide-right-leave-active {
  transition: all var(--elderly-transition-duration) var(--elderly-transition-timing);
}

.elderly-slide-right-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.elderly-slide-right-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

/* 缩放动画 */
.elderly-zoom-enter-active,
.elderly-zoom-leave-active {
  transition: all var(--elderly-transition-duration) var(--elderly-transition-timing);
}

.elderly-zoom-enter-from,
.elderly-zoom-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

/* 翻转动画 */
.elderly-flip-enter-active,
.elderly-flip-leave-active {
  transition: all var(--elderly-transition-duration) var(--elderly-transition-timing);
  transform-style: preserve-3d;
}

.elderly-flip-enter-from {
  opacity: 0;
  transform: rotateY(-90deg);
}

.elderly-flip-leave-to {
  opacity: 0;
  transform: rotateY(90deg);
}
</style>
