<template>
  <div class="comment-item" :class="{ 'child-comment': isChild }">
    <div class="comment-avatar">
      <img 
        :src="comment.userAvatar || defaultAvatar" 
        :alt="comment.userName"
        class="avatar-img"
      />
    </div>
    
    <div class="comment-content">
      <div class="comment-header">
        <span class="comment-username">
          {{ comment.userName }}
          <span v-if="comment.userRole" class="user-role">{{ formatRole(comment.userRole) }}</span>
        </span>
        <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
      </div>
      
      <div class="comment-text">{{ comment.content }}</div>
      
      <div class="comment-actions">
        <button 
          v-if="!isChild && canReply"
          class="action-btn reply-btn"
          @click="handleReply"
        >
          回复
        </button>
        
        <button 
          v-if="canDelete"
          class="action-btn delete-btn"
          @click="handleDelete"
        >
          删除
        </button>
      </div>
      
      <!-- 回复表单 -->
      <div v-if="showReplyForm" class="reply-form-container">
        <CommentForm
          :target-type="comment.targetType"
          :target-id="comment.targetId"
          :parent-id="comment.id"
          :is-reply="true"
          @comment-submitted="handleReplySubmitted"
          @reply-cancelled="handleReplyCancelled"
        />
      </div>
      
      <!-- 子评论列表 -->
      <div v-if="childComments && childComments.length > 0" class="child-comments">
        <CommentItem
          v-for="childComment in childComments"
          :key="childComment.id"
          :comment="childComment"
          :is-child="true"
          :can-reply="canReply"
          :can-delete="canDelete"
          @comment-deleted="$emit('comment-deleted')"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { commentApi } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import CommentForm from './CommentForm.vue'

/**
 * 评论项组件
 * 显示单条评论，支持回复和删除功能，可递归显示子评论
 */

const props = defineProps({
  comment: {
    type: Object,
    required: true,
    description: '评论数据对象'
  },
  isChild: {
    type: Boolean,
    default: false,
    description: '是否为子评论'
  },
  canReply: {
    type: Boolean,
    default: true,
    description: '是否允许回复'
  },
  canDelete: {
    type: Boolean,
    default: false,
    description: '是否允许删除'
  },
  childComments: {
    type: Array,
    default: () => [],
    description: '子评论列表'
  }
})

const emit = defineEmits(['comment-deleted', 'reply-submitted'])

const userStore = useUserStore()
const showReplyForm = ref(false)

/**
 * 默认头像URL
 * @returns {String} - 默认头像地址
 */
const defaultAvatar = computed(() => {
  return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
})

/**
 * 格式化时间显示
 * @param {String} timeString - 时间字符串
 * @returns {String} - 格式化后的时间文本
 */
const formatTime = (timeString) => {
  if (!timeString) return ''
  
  const date = new Date(timeString)
  const now = new Date()
  const diff = now - date
  
  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }
  
  // 小于1小时
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }
  
  // 小于1天
  if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  }
  
  // 小于7天
  if (diff < 604800000) {
    return `${Math.floor(diff / 86400000)}天前`
  }
  
  // 超过7天显示具体日期
  return date.toLocaleDateString()
}

/**
 * 格式化用户角色
 * @param {String} role - 角色代码
 * @returns {String} - 角色名称
 */
const formatRole = (role) => {
  const roleMap = {
    'ADMIN': '管理员',
    'admin': '管理员',
    'STAFF': '工作人员',
    'staff': '工作人员',
    'ELDERLY': '用户',
    'elderly': '用户',
    'VOLUNTEER': '志愿者',
    'volunteer': '志愿者',
    'DOCTOR': '医生',
    'doctor': '医生',
    'NURSE': '护士',
    'nurse': '护士',
    'SOCIAL_WORKER': '社工',
    'social_worker': '社工'
  }
  return roleMap[role] || role
}

/**
 * 处理回复操作
 */
const handleReply = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再回复')
    return
  }
  
  showReplyForm.value = true
}

/**
 * 处理回复提交成功
 */
const handleReplySubmitted = () => {
  showReplyForm.value = false
  ElMessage.success('回复成功')
  emit('reply-submitted') // 触发父组件刷新评论列表
}

/**
 * 处理取消回复
 */
const handleReplyCancelled = () => {
  showReplyForm.value = false
}

/**
 * 处理删除评论
 */
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await commentApi.deleteComment(props.comment.id)
    ElMessage.success('删除成功')
    emit('comment-deleted')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}
</script>

<style scoped>
.comment-item {
  display: flex;
  margin-bottom: 16px;
}

.child-comment {
  margin-left: 48px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.comment-avatar {
  flex-shrink: 0;
  margin-right: 12px;
}

.avatar-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.comment-username {
  font-weight: 500;
  color: #333;
  margin-right: 8px;
}

.user-role {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  border-radius: 4px;
  background-color: #f0f9ff;
  color: #1890ff;
  margin-left: 6px;
  font-weight: normal;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  line-height: 1.6;
  color: #555;
  margin-bottom: 8px;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}

.action-btn:hover {
  background-color: #f0f0f0;
  color: #666;
}

.reply-btn:hover {
  color: #409eff;
}

.delete-btn:hover {
  color: #f56c6c;
}

.reply-form-container {
  margin-top: 12px;
}

.child-comments {
  margin-top: 12px;
}

/* 老年用户模式适配 */
.elderly-mode .comment-item {
  margin-bottom: 20px;
}

.elderly-mode .avatar-img {
  width: 48px;
  height: 48px;
}

.elderly-mode .comment-username {
  font-size: 16px;
}

.elderly-mode .comment-text {
  font-size: 16px;
  line-height: 1.8;
}

.elderly-mode .action-btn {
  font-size: 14px;
  padding: 6px 12px;
}
</style>
