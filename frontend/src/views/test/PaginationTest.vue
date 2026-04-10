<template>
  <div class="pagination-test">
    <h1>分页样式测试</h1>
    <div class="system-pagination">
      <div class="pagination-info">
        共 {{ total }} 条记录，第 {{ currentPage }} / {{ totalPages }} 页
      </div>
      <div class="pagination-controls">
        <button 
          class="pagination-btn" 
          @click="prevPage"
          :disabled="currentPage === 1"
        >
          上一页
        </button>
        <button 
          v-for="pageNumber in getPageNumbers()" 
          :key="pageNumber"
          class="pagination-btn"
          :class="{ 'pagination-btn-active': pageNumber === currentPage }"
          @click="changePage(pageNumber)"
        >
          {{ pageNumber }}
        </button>
        <button 
          class="pagination-btn"
          @click="nextPage"
          :disabled="currentPage === totalPages"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// 模拟数据
const currentPage = ref(2)
const pageSize = ref(10)
const total = ref(57)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

// 页码生成函数
const getPageNumbers = () => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value
  
  if (total <= 7) {
    // 总页数小于等于7时，显示全部页码
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    // 总页数大于7时，显示当前页附近的页码
    if (current <= 4) {
      // 当前页在前4页
      for (let i = 1; i <= 5; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(total)
    } else if (current >= total - 3) {
      // 当前页在后4页
      pages.push(1)
      pages.push('...')
      for (let i = total - 4; i <= total; i++) {
        pages.push(i)
      }
    } else {
      // 当前页在中间
      pages.push(1)
      pages.push('...')
      for (let i = current - 1; i <= current + 1; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(total)
    }
  }
  
  return pages.filter(page => page !== '...') // 简化显示，只显示数字页码
}

// 翻页函数
const changePage = (page) => {
  currentPage.value = page
}

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}
</script>

<style scoped>
.pagination-test {
  padding: 20px;
  font-family: Arial, sans-serif;
}

h1 {
  color: #333;
  margin-bottom: 30px;
}

/* 分页样式 */
.system-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-radius: 8px;
  background-color: #f8f9fa;
}

.pagination-info {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.pagination-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pagination-btn {
  padding: 10px 18px;
  border: 2px solid #dcdfe6;
  background-color: #ffffff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  color: #606266;
  font-weight: 500;
  min-width: 40px;
  text-align: center;
}

.pagination-btn:hover:not(:disabled) {
  border-color: #409eff;
  color: #409eff;
  background-color: #ecf5ff;
  transform: translateY(-1px);
}

.pagination-btn:disabled {
  background-color: #f5f7fa;
  border-color: #ebeef5;
  color: #c0c4cc;
  cursor: not-allowed;
  transform: none;
}

/* 增强激活状态样式 */
.pagination-btn-active {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%) !important;
  border-color: #409eff !important;
  color: #ffffff !important;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.5);
  transform: translateY(-2px);
  position: relative;
}

.pagination-btn-active::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 6px;
  background: linear-gradient(135deg, transparent 30%, rgba(255, 255, 255, 0.2) 100%);
}

.pagination-btn-active:hover {
  background: linear-gradient(135deg, #66b1ff 0%, #91ccff 100%) !important;
  border-color: #66b1ff !important;
  color: #ffffff !important;
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.6);
  transform: translateY(-2px);
}
</style>
