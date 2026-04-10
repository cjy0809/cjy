<template>
  <div class="comment-list">
    <div class="comment-list-header">
      <h3>评论 ({{ totalComments }})</h3>
    </div>
    
    <!-- 评论表单 -->
    <CommentForm
      :target-type="targetType"
      :target-id="targetId"
      :show-rating="showRating"
      @comment-submitted="handleCommentSubmitted"
    />
    
    <!-- 评论加载状态 -->
    <div v-if="loading" class="comment-loading">
      <p>加载评论中...</p>
    </div>
    
    <!-- 评论列表为空 -->
    <div v-else-if="comments.length === 0" class="empty-comments">
      <p>暂无评论，快来发表第一条评论吧！</p>
    </div>
    
    <!-- 评论列表 -->
    <div v-else class="comments-container">
      <CommentItem
        v-for="comment in comments"
        :key="comment.id"
        :comment="comment"
        :can-reply="canReply"
        :can-delete="canDeleteComment(comment)"
        :child-comments="comment.childComments"
        @comment-deleted="handleCommentDeleted"
        @reply-submitted="handleCommentSubmitted"
      />
      
      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <button 
          class="load-more-btn"
          :disabled="loadingMore"
          @click="loadMoreComments"
        >
          {{ loadingMore ? '加载中...' : '加载更多评论' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { commentApi } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import CommentForm from './CommentForm.vue'
import CommentItem from './CommentItem.vue'

/**
 * 评论列表组件
 * 显示评论列表，支持分页加载、发表评论、回复和删除功能
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
  canReply: {
    type: Boolean,
    default: true,
    description: '是否允许回复'
  },
  adminMode: {
    type: Boolean,
    default: false,
    description: '是否为管理员模式'
  },
  showRating: {
    type: Boolean,
    default: true,
    description: '是否显示评分功能'
  }
})

const emit = defineEmits(['comment-submitted', 'comment-deleted'])

const userStore = useUserStore()
const comments = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const hasMore = ref(false)

/**
 * 计算评论总数（包括子评论）
 * @returns {Number} - 评论总数
 */
const totalComments = computed(() => {
  return comments.value.reduce((total, comment) => {
    return total + 1 + (comment.childComments ? comment.childComments.length : 0)
  }, 0)
})

/**
 * 判断是否可以删除评论
 * @param {Object} comment - 评论对象
 * @returns {Boolean} - 是否可以删除
 */
const canDeleteComment = (comment) => {
  if (props.adminMode) {
    return true
  }
  
  // 只有评论作者可以删除自己的评论
  return userStore.isLoggedIn && userStore.userInfo.id === comment.userId
}

/**
 * 获取评论列表
 * @param {Boolean} reset - 是否重置列表
 */
const fetchComments = async (reset = false) => {
  if (reset) {
    currentPage.value = 1
    comments.value = []
  }
  
  loading.value = reset ? true : loadingMore.value
  
  try {
    const response = await commentApi.getCommentsByTarget(
      props.targetType, 
      props.targetId, 
      currentPage.value, 
      pageSize.value
    )
    
    if (reset) {
      comments.value = response.data || []
    } else {
      // 追加新评论到现有列表
      comments.value = [...comments.value, ...(response.data || [])]
    }
    
    // 判断是否还有更多数据
    hasMore.value = (response.data || []).length >= pageSize.value
    
    // 只有在非重置模式下（加载更多）才递增页码
    if (!reset) {
      currentPage.value++
    }
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表失败')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

/**
 * 加载更多评论
 */
const loadMoreComments = () => {
  if (loadingMore.value || !hasMore.value) return
  fetchComments(false)
}

/**
 * 处理评论提交成功
 */
const handleCommentSubmitted = () => {
  // 刷新评论列表
  fetchComments(true)
  // 向父组件发送事件
  emit('comment-submitted')
}

/**
 * 处理评论删除
 */
const handleCommentDeleted = () => {
  // 刷新评论列表
  fetchComments(true)
  // 向父组件发送事件
  emit('comment-deleted')
}

// 组件挂载时获取评论列表
onMounted(() => {
  fetchComments(true)
})
</script>

<style scoped>
.comment-list {
  margin-top: 16px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.comment-list-header {
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-list-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.comment-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #999;
}

.empty-comments {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

.comments-container {
  margin-top: 12px;
}

.load-more {
  text-align: center;
  margin-top: 20px;
}

.load-more-btn {
  background: none;
  border: 1px solid #ddd;
  color: #666;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.load-more-btn:hover:not(:disabled) {
  border-color: #409eff;
  color: #409eff;
}

.load-more-btn:disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

/* 老年用户模式适配 */
.elderly-mode .comment-list {
  padding: 24px;
  border-radius: 12px;
}

.elderly-mode .comment-list-header h3 {
  font-size: 20px;
}

.elderly-mode .load-more-btn {
  padding: 12px 24px;
  font-size: 16px;
  border-radius: 6px;
}
</style>
