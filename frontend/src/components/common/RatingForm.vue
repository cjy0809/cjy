<template>
  <div class="rating-form">
    <el-form
      ref="ratingFormRef"
      :model="ratingForm"
      :rules="rules"
      label-position="top"
      :class="{ 'elderly-mode': isElderlyMode }"
    >
      <el-form-item :label="serviceTitle" prop="rating">
        <div class="rating-container">
          <el-rate
            v-model="ratingForm.rating"
            :colors="colors"
            :texts="ratingTexts"
            show-text
            :size="isElderlyMode ? 'large' : 'default'"
          />
        </div>
       
      </el-form-item>
      
      <el-form-item label="评价内容" prop="content">
        <el-input
          v-model="ratingForm.content"
          type="textarea"
          :rows="isElderlyMode ? 6 : 4"
          placeholder="请分享您对此次服务的体验..."
          maxlength="500"
          show-word-limit
          :class="{ 'elderly-input': isElderlyMode }"
        />
      </el-form-item>
      
      <div class="form-actions">
        <el-button
          type="primary"
          :loading="submitting"
          :size="isElderlyMode ? 'large' : 'default'"
          @click="submitRating"
        >
          {{ submitting ? '提交中...' : '提交评价' }}
        </el-button>
        <el-button
          :size="isElderlyMode ? 'large' : 'default'"
          @click="cancelRating"
        >
          取消
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { ratingsApi } from '@/api/ratings'
import { useUserStore } from '@/stores/user'

/**
 * 评分表单组件
 * 用于用户对服务进行评分和评价
 */

const userStore = useUserStore()

const props = defineProps({
  serviceId: {
    type: Number,
    required: true,
    description: '服务ID'
  },
  reservationId: {
    type: Number,
    required: true,
    description: '预约ID'
  },
  serviceTitle: {
    type: String,
    default: '服务评分',
    description: '服务标题'
  },
  isElderlyMode: {
    type: Boolean,
    default: false,
    description: '是否为老年用户模式'
  }
})

const emit = defineEmits(['submit-success', 'cancel'])

const ratingFormRef = ref(null)
const submitting = ref(false)

const ratingForm = reactive({
  rating: 0,
  content: ''
})

const colors = ['#F7BA2A', '#F7BA2A', '#F7BA2A']

const ratingTexts = ['非常差', '较差', '一般', '满意', '非常满意']

const rules = {
  rating: [
    { required: true, message: '请选择评分', trigger: 'change' },
    { type: 'number', min: 1, max: 5, message: '评分必须在1-5星之间', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'change' },
    { min: 1, max: 500, message: '评价内容长度在1-500个字符之间', trigger: 'blur' }
  ]
}

/**
 * 提交评分
 */
const submitRating = async () => {
  if (!ratingFormRef.value) return
  
  try {
    // 确保表单数据是最新的
    console.log('提交前的表单数据:', {
      rating: ratingForm.rating,
      content: ratingForm.content,
      serviceId: props.serviceId,
      reservationId: props.reservationId,
      userId: userStore.userInfo?.id || 1
    })
    
    await ratingFormRef.value.validate()
    submitting.value = true
    
    const ratingData = {
      serviceId: props.serviceId,
      reservationId: props.reservationId,
      rating: ratingForm.rating,
      content: ratingForm.content,
      userId: userStore.userInfo?.id || 1 // 从用户状态中获取，如果没有则使用默认值
    }
    
    // 使用新的API同时提交评分和评论
    await ratingsApi.submitRatingWithComment(ratingData)
    ElMessage.success('评价提交成功！')
    emit('submit-success', ratingData)
    
    // 重置表单
    ratingForm.rating = 0
    ratingForm.content = ''
  } catch (error) {
    console.error('提交评价失败:', error)
    ElMessage.error(error.response?.data?.message || '提交评价失败，请重试')
  } finally {
    submitting.value = false
  }
}

/**
 * 取消评分
 */
const cancelRating = () => {
  emit('cancel')
}
</script>

<style scoped>
.rating-form {
  padding: 20px;
}

.elderly-mode {
  font-size: 18px;
}

.rating-container {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 20px 0;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px dashed #e0e0e0;
}

.elderly-input :deep(.el-textarea__inner) {
  font-size: 18px;
  padding: 12px;
  line-height: 1.6;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 30px;
}

.elderly-mode .form-actions {
  gap: 20px;
}

.elderly-mode .el-button {
  padding: 12px 24px;
  font-size: 18px;
}

:deep(.el-rate__text) {
  font-size: 14px;
  margin-left: 8px;
  font-weight: 500;
}

.elderly-mode :deep(.el-rate__text) {
  font-size: 16px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
  font-size: 16px;
}

.elderly-mode :deep(.el-form-item__label) {
  font-size: 18px;
  font-weight: 600;
}

:deep(.el-rate) {
  height: auto;
}

:deep(.el-rate__icon) {
  font-size: 24px;
  margin-right: 6px;
}

.elderly-mode :deep(.el-rate__icon) {
  font-size: 32px;
  margin-right: 8px;
}

:deep(.el-textarea__inner) {
  resize: none;
  transition: border-color 0.3s;
}

:deep(.el-textarea__inner):focus {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

.elderly-mode :deep(.el-form-item) {
  margin-bottom: 30px;
}

/* 添加评分等级颜色提示 */
.rating-hint {
  display: flex;
  justify-content: space-between;
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}

.elderly-mode .rating-hint {
  font-size: 14px;
}

/* 添加表单提交按钮动画效果 */
.form-actions .el-button {
  transition: all 0.3s ease;
}

.form-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.form-actions .el-button:active {
  transform: translateY(0);
}
</style>
