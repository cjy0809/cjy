<template>
  <div class="comment-form">
    <div class="comment-form-header">
      <h4 v-if="isReply">回复评论</h4>
      <h4 v-else>发表评论</h4>
      <button v-if="isReply" class="cancel-reply-btn" @click="cancelReply">
        取消回复
      </button>
    </div>
    
    <div class="comment-form-content">
      <!-- 评分选择器 -->
      <div v-if="showRating && !isReply" class="rating-section">
        <div class="rating-label">评分：</div>
        <div class="rating-container">
          <el-rate 
            v-model="commentRating" 
            :colors="colors"
            :texts="ratingTexts"
            show-text
            :max="5"
            @change="onRatingChange"
          />
          <div class="rating-description">{{ getRatingDescription() }}</div>
        </div>
      </div>
      
      <textarea
        v-model="commentContent"
        class="comment-textarea"
        :placeholder="placeholder"
        rows="4"
        maxlength="500"
        @input="updateCharCount"
      ></textarea>
      <div class="comment-form-footer">
        <span class="char-count">{{ charCount }}/500</span>
        <button 
          class="submit-btn" 
          :disabled="!commentContent.trim() || submitting"
          @click="submitComment"
        >
          {{ submitting ? '提交中...' : '发表评论' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { commentApi } from '@/api/comment'
import { useUserStore } from '@/stores/user'

/**
 * 评论表单组件
 * 用于用户发表评论或回复评论，支持评分功能
 */

const props = defineProps({
  targetType: {
    type: String,
    required: true,
    description: '评论目标类型（如：ACTIVITY, VENUE, SERVICE等）'
  },
  targetId: {
    type: Number,
    required: true,
    description: '评论目标ID'
  },
  parentId: {
    type: Number,
    default: 0,
    description: '父评论ID，用于回复评论'
  },
  isReply: {
    type: Boolean,
    default: false,
    description: '是否为回复模式'
  },
  placeholder: {
    type: String,
    default: '请输入您的评论...',
    description: '输入框占位符文本'
  },
  showRating: {
    type: Boolean,
    default: true,
    description: '是否显示评分功能'
  }
})

const emit = defineEmits(['comment-submitted', 'reply-cancelled'])

const userStore = useUserStore()
const commentContent = ref('')
const submitting = ref(false)
const charCount = ref(0)
const commentRating = ref(0)
const hoveredRating = ref(0)
const colors = ['#F7BA2A', '#F7BA2A', '#F7BA2A', '#F7BA2A', '#F7BA2A']
const ratingTexts = ['非常差', '较差', '一般', '满意', '非常满意']

/**
 * 动态计算占位符文本
 * @returns {String} - 占位符文本
 */
const placeholder = computed(() => {
  if (props.isReply) {
    return `回复评论...`
  }
  return props.placeholder
})

/**
 * 更新字符计数
 */
const updateCharCount = () => {
  charCount.value = commentContent.value.length
}

/**
 * 评分变化处理
 * @param {Number} value - 评分值
 */
const onRatingChange = (value) => {
  commentRating.value = value
}

/**
 * 获取评分描述文本
 * @returns {String} - 评分描述
 */
const getRatingDescription = () => {
  const descriptions = ['', '非常差', '较差', '一般', '满意', '非常满意']
  return descriptions[commentRating.value] || ''
}

/**
 * 提交评论
 */
const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  // 如果启用评分且不是回复，验证是否选择了评分
  if (props.showRating && !props.isReply && commentRating.value === 0) {
    ElMessage.warning('请选择评分')
    return
  }

  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再评论')
    return
  }

  submitting.value = true
  
  try {
    const data = {
      content: commentContent.value.trim(),
      targetType: props.targetType,
      targetId: props.targetId,
      parentId: props.parentId
    }
    
    // 如果启用评分且不是回复，添加评分数据
    if (props.showRating && !props.isReply) {
      data.rating = commentRating.value
    }
    
    await commentApi.createComment(data)
    ElMessage.success('评论发表成功')
    
    // 清空表单
    commentContent.value = ''
    charCount.value = 0
    commentRating.value = 0
    
    // 通知父组件
    emit('comment-submitted')
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error(error.response?.data?.message || '发表评论失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 取消回复
 */
const cancelReply = () => {
  commentContent.value = ''
  charCount.value = 0
  // 如果不是回复，重置评分
  if (!props.isReply) {
    commentRating.value = 0
  }
  emit('reply-cancelled')
}

// 监听parentId变化，清空表单
watch(() => props.parentId, () => {
  commentContent.value = ''
  charCount.value = 0
  // 如果不是回复，重置评分
  if (!props.isReply) {
    commentRating.value = 0
  }
})
</script>

<style scoped>
.comment-form {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #eaeaea;
}

.comment-form-content {
  margin-bottom: 0;
}

.comment-form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.comment-form-header h4 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.rating-section {
  margin-bottom: 16px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #eaeaea;
}

.rating-label {
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
  display: block;
}

.rating-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rating-container :deep(.el-rate) {
  font-size: 18px;
}

.rating-container :deep(.el-rate__item) {
  margin-right: 4px;
}

.rating-description {
  font-size: 14px;
  color: #909399;
  margin-left: 8px;
  font-style: italic;
}

.cancel-reply-btn {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 14px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}

.cancel-reply-btn:hover {
  background-color: #f0f0f0;
  color: #666;
}

.comment-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.5;
  resize: vertical;
  font-family: inherit;
}

.comment-textarea:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.comment-form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.char-count {
  font-size: 12px;
  color: #999;
}

.submit-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.submit-btn:hover:not(:disabled) {
  background-color: #337ecc;
}

.submit-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

/* 老年用户模式适配 */
.elderly-mode .comment-form {
  background-color: #fff;
  border: 2px solid #e0e0e0;
}

.elderly-mode .comment-textarea {
  font-size: 16px;
  padding: 16px;
}

.elderly-mode .submit-btn {
  padding: 12px 24px;
  font-size: 16px;
}
</style>
