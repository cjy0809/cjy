import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useGuideStore = defineStore('guide', () => {
  const savedState = localStorage.getItem('guideVisible')
  const isGuideVisible = ref(savedState === 'true')

  watch(isGuideVisible, (newValue) => {
    localStorage.setItem('guideVisible', String(newValue))
  })

  const toggleGuide = () => {
    isGuideVisible.value = !isGuideVisible.value
  }

  return {
    isGuideVisible,
    toggleGuide
  }
})
