<template>
  <div class="elderly-activity-detail">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/elderly/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/elderly/activities' }">活动列表</el-breadcrumb-item>
        <el-breadcrumb-item>活动详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="activity-content" v-loading="loading">
      <div class="content-header">
        <h1 class="content-title">{{ activity.title }}</h1>
        <el-button type="primary" plain @click="handleBackToList" :icon="ArrowLeft">返回活动列表</el-button>
      </div>
      <el-row :gutter="24">
        <el-col :span="16">
          <div class="activity-images">
            <el-carousel height="400px" indicator-position="outside">
              <el-carousel-item v-for="(image, index) in activity.images" :key="index">
                <img :src="image" :alt="activity.title" class="activity-image" />
              </el-carousel-item>
            </el-carousel>
          </div>

          <div class="activity-description">
            <h2>活动介绍</h2>
            <p>{{ activity.description }}</p>
          </div>

          <div class="activity-details">
            <h2>活动详情</h2>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="活动时间">{{ activity.time }}</el-descriptions-item>
              <el-descriptions-item label="活动地点">{{ activity.location }}</el-descriptions-item>
              <el-descriptions-item label="活动类型">{{ activity.type }}</el-descriptions-item>
              <el-descriptions-item label="报名人数">{{ activity.enrolled }}/{{ activity.capacity }}人</el-descriptions-item>
              <el-descriptions-item label="报名状态">
                <el-tag :type="getRegistrationStatusType(calculatedRegistrationStatus)">{{ getRegistrationStatusText(calculatedRegistrationStatus) }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="activity-organizer">
            <h2>组织者信息</h2>
            <div class="organizer-info">
              <el-avatar :size="60" :src="activity.organizer?.avatar || 'https://picsum.photos/seed/default-avatar/100/100.jpg'" />
              <div class="organizer-details">
                <h3>{{ activity.organizer?.name || '组织者' }}</h3>
                <p>{{ activity.organizer?.title || '社区管理员' }}</p>
                <p>联系电话：{{ activity.organizer?.phone || '暂无' }}</p>
              </div>
            </div>
          </div>

          <!-- 用户评价模块 -->
          <div class="activity-reviews">
            <div class="reviews-header">
              <h2>用户评价</h2>
              <div class="reviews-summary">
                <div class="average-rating">
                  <span class="rating-score">{{ averageRating.toFixed(1) }}</span>
                  <el-rate 
                    v-model="averageRating" 
                    disabled 
                    text-color="#ff9900"
                    :max="5"
                  />
                  <span class="total-reviews">{{ totalReviews }}条评价</span>
                </div>
                <div class="rating-distribution">
                  <div v-for="star in 5" :key="star" class="rating-item">
                    <span class="star-label">{{ 6-star }}星</span>
                    <div class="progress-bar">
                      <div 
                        class="progress-fill" 
                        :style="{ width: getRatingPercentage(6-star) + '%' }"
                      ></div>
                    </div>
                    <span class="star-count">{{ getRatingCount(6-star) }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 评论列表组件 -->
            <CommentList
              :key="`comment-list-${activityId}`"
              :target-type="'activity'"
              :target-id="parseInt(activityId)"
              :can-reply="true"
              :show-rating="true"
              @comment-submitted="handleCommentSubmitted"
              @comment-deleted="handleCommentDeleted"
            />
          </div>
        </el-col>

        <el-col :span="8">
          <div class="action-card">
            <div class="registration-info">
              <div class="info-item">
                <span class="label">报名时间：</span>
                <span class="value">{{ activity.registrationPeriod }}</span>
              </div>

              <div class="info-item">
                <span class="label">活动时长：</span>
                <span class="value">{{ activity.duration }}</span>
              </div>
              <div class="info-item">
                <span class="label">注意事项：</span>
                <span class="value">{{ activity.notes }}</span>
              </div>
            </div>

            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="large" 
                @click="handleRegistration" 
                :disabled="!canRegister"
                :loading="registering"
              >
                {{ getRegistrationButtonText() }}
              </el-button>
              <el-button 
                size="large" 
                @click="handleFavorite" 
                :class="isFavorited ? 'favorited-button' : 'favorite-button'"
                :icon="isFavorited ? 'StarFilled' : 'Star'"
              >
                {{ isFavorited ? '已收藏' : '收藏' }}
              </el-button>

            </div>
          </div>

          <div class="related-activities">
            <h3>相关活动</h3>
            <div class="related-list">
              <div 
                v-for="item in relatedActivities" 
                :key="item.id" 
                class="related-item"
                @click="goToActivity(item.id)"
                :class="{ 'navigating': navigating }"
              >
                <img :src="item.image" :alt="item.title" class="related-image" />
                <div class="related-info">
                  <h4>{{ item.title }}</h4>
                  <p>{{ item.time }}</p>
                </div>
                <div v-if="navigating" class="loading-overlay">
                  <el-icon class="is-loading"><Loading /></el-icon>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 报名确认对话框 -->
    <el-dialog
      v-model="registrationDialogVisible"
      title="确认报名"
      width="400px"
      :before-close="handleCloseRegistration"
    >
      <div class="registration-confirmation">
        <div class="activity-summary">
          <h3>{{ activity.title }}</h3>
          <div class="activity-info">
            <p><i class="el-icon-time"></i> 时间：{{ activity.time }}</p>
            <p><i class="el-icon-location"></i> 地点：{{ activity.location }}</p>
            <p><i class="el-icon-user"></i> 报名人：{{ userStore.userInfo.name || '用户' }}</p>
          </div>
        </div>
        <div class="confirmation-message">
          <p>您确定要报名参加此活动吗？</p>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="registrationDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRegistration" :loading="submitting">
            确认报名
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Loading } from '@element-plus/icons-vue'
import { activityApi } from '@/api/activity'
import { favoriteApi } from '@/api/favorite'
import { commentApi } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import CommentList from '@/components/common/CommentList.vue'

/**
 * 老年用户活动详情视图组件
 * 
 * 功能说明：
 * - 展示活动详细信息（图片、介绍、详情、组织者信息）
 * - 支持活动报名和取消报名
 * - 支持活动收藏和取消收藏
 * - 展示用户评价和评分统计
 * - 显示相关活动推荐
 * - 自动计算活动状态和报名时间窗口
 * 
 * @component ElderlyActivityDetailView
 */
export default {
  name: 'ElderlyActivityDetailView',
  components: {
    ArrowLeft,
    Loading,
    CommentList
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const userStore = useUserStore()
    const activityId = computed(() => route.params.id)

    const loading = ref(true)
    const activity = ref({
      id: null,
      title: '',
      description: '',
      images: ['https://picsum.photos/seed/default/800/600.jpg'],
      type: '',
      location: '',
      capacity: 0,
      enrolled: 0,
      time: '',
      status: 'OPEN',
      startTime: null,
      endTime: null,
      organizer: {
        id: null,
        name: '',
        title: '',
        phone: '',
        avatar: 'https://picsum.photos/seed/default-avatar/100/100.jpg'
      },
      registrationPeriod: '',

      duration: '',
      notes: '',
      isEnrolled: false
    })
    const isFavorited = ref(false)
    const registering = ref(false)
    const registrationDialogVisible = ref(false)
    const submitting = ref(false)
    const navigating = ref(false)
    
    // 评价相关数据
    const reviews = ref([])
    const ratingDistribution = ref({ 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 })
    const averageRating = ref(0)
    const totalReviews = ref(0)

    /**
     * 数据映射函数
     * 将API返回的数据转换为前端需要的格式
     * @param {Object} apiData - API返回的原始数据
     * @returns {Object} 转换后的活动数据
     */
    const mapActivityData = (apiData) => {
      return {
        id: apiData.id || null,
        title: apiData.title || '',
        description: apiData.content || '暂无描述',
        images: apiData.coverImage ? [apiData.coverImage] : [`https://picsum.photos/seed/activity${apiData.id || 'default'}/800/600.jpg`],
        type: getActivityType(apiData.status),
        location: apiData.location || '社区中心',
        capacity: apiData.maxParticipants || 0,
        enrolled: apiData.currentParticipants || 0,
        time: formatDateTimeRange(apiData.startTime, apiData.endTime),
        status: getActivityStatus(apiData.status),
        startTime: apiData.startTime,
        endTime: apiData.endTime,
        organizer: {
          id: apiData.organizerId || null,
          name: apiData.organizerName || '活动组织者',
          title: apiData.organizerTitle || '社区管理员',
          phone: apiData.organizerPhone || '暂无',
          avatar: apiData.organizerAvatar || `https://picsum.photos/seed/avatar${apiData.organizerId || 'default'}/100/100.jpg`
        },
        registrationPeriod: getRegistrationPeriod(apiData.startTime, apiData.endTime),

        duration: getDuration(apiData.startTime, apiData.endTime),
        notes: '请准时参加，如有疑问请联系组织者',
        isEnrolled: apiData.isRegistered || false
      }
    }

    /**
     * 获取活动状态
     * @param {number} status - 活动状态码
     * @returns {string} 活动状态字符串
     */
    const getActivityStatus = (status) => {
      const statusMap = {
        0: 'NOT_STARTED', // 未开始
        1: 'IN_PROGRESS', // 进行中
        2: 'ENDED', // 已结束
        3: 'CANCELLED' // 已取消
      }
      return statusMap[status] || 'NOT_STARTED'
    }

    /**
     * 计算活动状态（基于当前时间）
     * @param {string} startTime - 活动开始时间
     * @param {string} endTime - 活动结束时间
     * @param {number} manualStatus - 手动设置的状态
     * @returns {string} 活动状态字符串
     */
    const calculateActivityStatus = (startTime, endTime, manualStatus) => {
      // 如果手动状态是已取消，保持已取消状态
      if (manualStatus === 3) return 'CANCELLED';
      
      const now = new Date();
      const start = new Date(startTime);
      const end = new Date(endTime);
      
      // 检查日期是否有效
      if (isNaN(start.getTime()) || isNaN(end.getTime())) {
        return getActivityStatus(manualStatus);
      }
      
      // 根据当前时间自动设置状态
      if (now < start) {
        // 活动未开始
        return 'NOT_STARTED'; // 未开始
      } else if (now >= start && now <= end) {
        // 活动进行中
        return 'IN_PROGRESS'; // 进行中
      } else {
        // 活动已结束
        return 'ENDED'; // 已结束
      }
    }

    /**
     * 检查是否可以报名（活动开始前一周到活动结束前一小时）
     * @param {string} startTime - 活动开始时间
     * @param {string} endTime - 活动结束时间
     * @param {number} currentParticipants - 当前参与人数
     * @param {number} maxParticipants - 最大参与人数
     * @returns {boolean} 是否可以报名
     */
    const canRegisterActivity = (startTime, endTime, currentParticipants, maxParticipants) => {
      const now = new Date();
      const start = new Date(startTime);
      const end = new Date(endTime);
      
      // 检查日期是否有效
      if (isNaN(start.getTime()) || isNaN(end.getTime())) {
        return false;
      }
      
      // 计算活动开始时间前一周
      const oneWeekBeforeStart = new Date(start.getTime() - 7 * 24 * 60 * 60 * 1000);
      
      // 计算活动结束时间前一小时
      const oneHourBeforeEnd = new Date(end.getTime() - 60 * 60 * 1000);
      
      // 检查是否在报名时间窗口内（活动开始前一周到活动结束前一小时）
      const isInRegistrationWindow = now >= oneWeekBeforeStart && now < oneHourBeforeEnd;
      
      // 检查是否已满员
      const isNotFull = currentParticipants < maxParticipants;
      
      return isInRegistrationWindow && isNotFull;
    }

    /**
     * 获取活动类型
     * @param {number} status - 活动状态
     * @returns {string} 活动类型
     */
    const getActivityType = (status) => {
      // 根据活动类型或状态返回类型
      return '文体活动' // 默认值
    }

    /**
     * 格式化日期时间范围
     * @param {string} startTime - 开始时间
     * @param {string} endTime - 结束时间
     * @returns {string} 格式化后的时间范围
     */
    const formatDateTimeRange = (startTime, endTime) => {
      if (!startTime || !endTime) return '待定'
      
      const start = new Date(startTime)
      const end = new Date(endTime)
      
      const formatDate = (date) => {
        return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
      }
      
      return `${formatDate(start)} ~ ${formatDate(end)}`
    }

    /**
     * 获取报名时间段
     * @param {string} startTime - 开始时间
     * @param {string} endTime - 结束时间
     * @returns {string} 报名时间段
     */
    const getRegistrationPeriod = (startTime, endTime) => {
      if (!startTime || !endTime) return '待定'
      
      const start = new Date(startTime)
      const end = new Date(endTime)
      
      const formatDateTime = (date) => {
        return `${date.getFullYear()}.${(date.getMonth() + 1).toString().padStart(2, '0')}.${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
      }
      
      return `${formatDateTime(start)} - ${formatDateTime(end)}`
    }

    /**
     * 获取活动时长
     * @param {string} startTime - 开始时间
     * @param {string} endTime - 结束时间
     * @returns {string} 活动时长
     */
    const getDuration = (startTime, endTime) => {
      if (!startTime || !endTime) return '待定'
      
      const start = new Date(startTime)
      const end = new Date(endTime)
      const durationMs = end - start
      
      // 计算总分钟数
      const totalMinutes = Math.floor(durationMs / (1000 * 60))
      
      // 转换为小时和分钟
      const hours = Math.floor(totalMinutes / 60)
      const minutes = totalMinutes % 60
      
      if (hours > 0 && minutes > 0) {
        return `${hours}小时${minutes}分钟`
      } else if (hours > 0) {
        return `${hours}小时`
      } else {
        return `${minutes}分钟`
      }
    }

    const relatedActivities = ref([])

    /**
     * 获取活动详情
     */
    const fetchActivityDetail = async () => {
      try {
        loading.value = true
        // 确保activityId存在
        if (!activityId.value) {
          console.warn('activityId为空，跳过获取活动详情');
          loading.value = false;
          return;
        }
        // 从API获取活动详情
        const response = await activityApi.getActivityDetail(activityId.value)
        
        // 处理API返回的数据
        if (response && response.data) {
          activity.value = mapActivityData(response.data)
          // 获取评价数据
          fetchReviewsData()
        } else {
          activity.value = {}
          ElMessage.error('获取活动详情失败')
        }
        
        loading.value = false
      } catch (error) {
        console.error('获取活动详情失败:', error)
        ElMessage.error('获取活动详情失败')
        loading.value = false
      }
    }
    
    /**
     * 获取评价数据
     */
    const fetchReviewsData = async () => {
      try {
        console.log('开始获取评价数据，activityId:', activityId.value);
        // 确保activityId存在
        if (!activityId.value) {
          console.warn('activityId为空，跳过获取评价数据');
          return;
        }
        // 直接使用当前活动ID，确保获取的是当前活动的评论
        const targetId = parseInt(activityId.value);
        const response = await commentApi.getCommentsByTarget('activity', targetId, 1, 100);
        console.log('评价API返回:', response);
        
        if (response.code === 200 && response.data && Array.isArray(response.data)) {
          reviews.value = response.data;
          console.log('获取到的评论列表:', reviews.value);
          
          // 计算评分分布
          const distribution = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
          let totalRating = 0;
          let ratingCount = 0;
          
          console.log('开始处理评论评分:');
          reviews.value.forEach((review, index) => {
            console.log(`评论${index + 1}:`, review);
            
            // 直接访问rating字段
            const rating = review.rating;
            console.log(`评论${index + 1}的rating字段值:`, rating, '类型:', typeof rating);
            
            if (rating !== undefined && rating !== null && rating !== '') {
              const ratingValue = parseInt(rating);
              console.log(`转换后评分:`, ratingValue);
              
              if (!isNaN(ratingValue) && ratingValue >= 1 && ratingValue <= 5) {
                distribution[ratingValue]++;
                totalRating += ratingValue;
                ratingCount++;
                console.log(`有效评分: ${ratingValue}, 当前分布:`, distribution);
              } else {
                console.log(`无效评分值: ${ratingValue}`);
              }
            } else {
              console.log('rating字段为空或未定义');
            }
          });
          
          console.log('最终评分分布:', distribution);
          console.log('总评分:', totalRating, '评分数量:', ratingCount);
          console.log('评论总数:', reviews.value.length);
          
          ratingDistribution.value = distribution;
          totalReviews.value = reviews.value.length;
          
          // 计算平均评分
          averageRating.value = ratingCount > 0 ? totalRating / ratingCount : 0;
          console.log('最终平均评分:', averageRating.value);
        } else {
          console.error('API返回数据格式错误:', response);
          reviews.value = [];
          ratingDistribution.value = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
          totalReviews.value = 0;
          averageRating.value = 0;
        }
      } catch (error) {
        console.error('获取评价数据失败:', error);
        reviews.value = [];
        ratingDistribution.value = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
        totalReviews.value = 0;
        averageRating.value = 0;
      }
    }
    
    /**
     * 获取评分百分比
     * @param {number} rating - 评分等级
     * @returns {number} 评分百分比
     */
    const getRatingPercentage = (rating) => {
      // 计算有评分的评论总数
      const ratedReviewsCount = Object.values(ratingDistribution.value).reduce((sum, count) => sum + count, 0);
      if (ratedReviewsCount === 0) return 0;
      return Math.round((ratingDistribution.value[rating] / ratedReviewsCount) * 100);
    }
    
    /**
     * 获取评分数量
     * @param {number} rating - 评分等级
     * @returns {number} 评分数量
     */
    const getRatingCount = (rating) => {
      return ratingDistribution.value[rating] || 0
    }
    
    /**
     * 获取相关活动
     */
    const fetchRelatedActivities = async () => {
      try {
        // 确保activityId存在
        if (!activityId.value) {
          console.warn('activityId为空，跳过获取相关活动');
          return;
        }
        const response = await activityApi.getRelatedActivities(activityId.value, 3)
        
        if (response && response.data) {
          // 将后端返回的数据转换为前端需要的格式
          relatedActivities.value = response.data.map(item => ({
            id: item.id,
            title: item.title,
            time: formatDateTimeRange(item.startTime, item.endTime),
            image: item.coverImage || `https://picsum.photos/seed/activity${item.id}/400/300.jpg`
          }))
        }
      } catch (error) {
        console.error('获取相关活动失败:', error)
        relatedActivities.value = []
      }
    }
    
    /**
     * 计算属性：计算报名状态
     * @returns {string} 报名状态
     */
    const calculatedRegistrationStatus = computed(() => {
      if (activity.value.isEnrolled) {
        return 'enrolled'
      }
      if (canRegisterActivity(activity.value.startTime, activity.value.endTime, activity.value.enrolled, activity.value.capacity)) {
        return 'available'
      }
      return 'closed'
    })
    
    /**
     * 计算属性：是否可以报名
     * @returns {boolean} 是否可以报名
     */
    const canRegister = computed(() => {
      return calculatedRegistrationStatus.value === 'available'
    })
    
    /**
     * 获取报名状态文本
     * @param {string} status - 报名状态
     * @returns {string} 状态文本
     */
    const getRegistrationStatusText = (status) => {
      switch (status) {
        case 'enrolled':
          return '已报名'
        case 'available':
          return '可报名'
        case 'closed':
          return '不可报名'
        default:
          return '未知状态'
      }
    }
    
    /**
     * 获取报名状态类型
     * @param {string} status - 报名状态
     * @returns {string} 状态类型
     */
    const getRegistrationStatusType = (status) => {
      switch (status) {
        case 'enrolled':
          return 'success'
        case 'available':
          return 'primary'
        case 'closed':
          return 'info'
        default:
          return 'info'
      }
    }
    
    /**
     * 获取报名按钮文本
     * @returns {string} 按钮文本
     */
    const getRegistrationButtonText = () => {
      if (activity.value.isEnrolled) {
        return '已报名'
      }
      if (canRegisterActivity(activity.value.startTime, activity.value.endTime, activity.value.enrolled, activity.value.capacity)) {
        return '立即报名'
      }
      return '不可报名'
    }
    
    /**
     * 处理返回活动列表
     */
    const handleBackToList = () => {
      router.push('/elderly/activities')
    }
    
    /**
     * 处理报名操作
     */
    const handleRegistration = () => {
      if (!userStore.userInfo || !userStore.userInfo.id) {
        ElMessage.warning('请先登录')
        router.push('/auth/login')
        return
      }
      
      if (activity.value.isEnrolled) {
        ElMessage.info('您已经报名了此活动')
        return
      }
      
      if (!canRegisterActivity(activity.value.startTime, activity.value.endTime, activity.value.enrolled, activity.value.capacity)) {
        ElMessage.warning('报名已截止或活动已满员')
        return
      }
      
      registrationDialogVisible.value = true
    }
    
    /**
     * 关闭报名对话框
     */
    const handleCloseRegistration = () => {
      registrationDialogVisible.value = false
    }
    
    /**
     * 提交报名
     */
    const submitRegistration = async () => {
      try {
        submitting.value = true
        
        const response = await activityApi.registerActivity(activityId.value)
        
        if (response) {
          activity.value.isEnrolled = true
          activity.value.enrolled += 1
          registrationDialogVisible.value = false
          ElMessage.success('报名成功！')
        } else {
          ElMessage.error('报名失败，请重试')
        }
      } catch (error) {
        console.error('报名失败:', error)
        ElMessage.error('报名失败，请重试')
      } finally {
        submitting.value = false
      }
    }
    
    /**
     * 处理收藏操作
     */
    const handleFavorite = async () => {
      try {
        if (isFavorited.value) {
          // 取消收藏
          const response = await favoriteApi.removeFavorite(activityId.value, 'activity')
          if (response) {
            isFavorited.value = false
            ElMessage.success('已取消收藏')
          } else {
            ElMessage.error('取消收藏失败')
          }
        } else {
          // 添加收藏
          const response = await favoriteApi.addFavorite({
            targetId: parseInt(activityId.value),
            targetType: 'activity'
          })
          if (response) {
            isFavorited.value = true
            ElMessage.success('收藏成功')
          } else {
            ElMessage.error('收藏失败')
          }
        }
      } catch (error) {
        console.error('收藏操作失败:', error)
        ElMessage.error('收藏操作失败，请重试')
      }
    }
    
    /**
     * 跳转到相关活动
     * @param {number} id - 活动ID
     */
    const goToActivity = (id) => {
      navigating.value = true
      router.push(`/elderly/activities/${id}`)
    }
    
    /**
     * 处理评论提交
     */
    const handleCommentSubmitted = () => {
      // 重新获取评价数据
      fetchReviewsData()
    }
    
    /**
     * 处理评论删除
     */
    const handleCommentDeleted = () => {
      // 重新获取评价数据
      fetchReviewsData()
    }
    
    /**
     * 监听活动ID变化
     */
    watch(activityId, (newId) => {
      if (newId) {
        fetchActivityDetail()
        fetchRelatedActivities()
      }
    }, { immediate: true })
    
    /**
     * 生命周期钩子
     * 组件挂载时获取活动详情和相关活动
     */
    onMounted(() => {
      if (activityId.value) {
        fetchActivityDetail()
        fetchRelatedActivities()
      }
    })
    
    return {
      activityId,
      loading,
      activity,
      isFavorited,
      registering,
      registrationDialogVisible,
      submitting,
      navigating,
      reviews,
      ratingDistribution,
      averageRating,
      totalReviews,
      relatedActivities,
      calculatedRegistrationStatus,
      canRegister,
      getRegistrationStatusText,
      getRegistrationStatusType,
      getRegistrationButtonText,
      handleBackToList,
      handleRegistration,
      handleCloseRegistration,
      submitRegistration,
      handleFavorite,
      goToActivity,
      getRatingPercentage,
      getRatingCount,
      handleCommentSubmitted,
      handleCommentDeleted
    }
  }
}
</script>

<style scoped>
.elderly-activity-detail {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.activity-content {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.content-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.activity-images {
  margin-bottom: 24px;
  border-radius: 8px;
  overflow: hidden;
}

.activity-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.activity-description {
  margin-bottom: 24px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.activity-description h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #333;
}

.activity-description p {
  font-size: 16px;
  line-height: 1.8;
  color: #666;
  margin: 0;
}

.activity-details {
  margin-bottom: 24px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.activity-details h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: #333;
}

.activity-organizer {
  margin-bottom: 24px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.activity-organizer h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: #333;
}

.organizer-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.organizer-details h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #333;
}

.organizer-details p {
  font-size: 14px;
  color: #666;
  margin: 4px 0;
}

.activity-reviews {
  margin-bottom: 24px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.reviews-header {
  margin-bottom: 20px;
}

.reviews-header h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: #333;
}

.reviews-summary {
  display: flex;
  gap: 32px;
  padding: 16px;
  background: white;
  border-radius: 8px;
}

.average-rating {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.rating-score {
  font-size: 36px;
  font-weight: 600;
  color: #ff9900;
}

.total-reviews {
  font-size: 14px;
  color: #666;
}

.rating-distribution {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rating-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.star-label {
  width: 40px;
  font-size: 14px;
  color: #666;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #e8e8e8;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #ff9900;
  transition: width 0.3s ease;
}

.star-count {
  width: 40px;
  font-size: 14px;
  color: #666;
  text-align: right;
}

.action-card {
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
}

.registration-info {
  margin-bottom: 20px;
}

.info-item {
  margin-bottom: 12px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .label {
  display: block;
  font-weight: 600;
  color: #666;
  margin-bottom: 4px;
}

.info-item .value {
  color: #333;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.favorited-button {
  background-color: #ff9900;
  border-color: #ff9900;
  color: white;
}

.favorited-button:hover {
  background-color: #e68a00;
  border-color: #e68a00;
}

.favorite-button {
  background-color: white;
  border-color: #d9d9d9;
  color: #666;
}

.favorite-button:hover {
  border-color: #ff9900;
  color: #ff9900;
}

.related-activities {
  padding: 20px;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
}

.related-activities h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: #333;
}

.related-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.related-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.related-item:hover {
  background: #f0f0f0;
  transform: translateX(4px);
}

.related-item.navigating {
  opacity: 0.6;
  pointer-events: none;
}

.related-image {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.related-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.related-info h4 {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #333;
}

.related-info p {
  font-size: 12px;
  color: #666;
  margin: 0;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
}

.registration-confirmation {
  padding: 16px 0;
}

.activity-summary {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e8e8e8;
}

.activity-summary h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #333;
}

.activity-info p {
  font-size: 14px;
  color: #666;
  margin: 8px 0;
}

.confirmation-message {
  text-align: center;
  padding: 16px;
  background: #fffbe6;
  border: 1px solid #ffe58f;
  border-radius: 4px;
}

.confirmation-message p {
  font-size: 16px;
  color: #faad14;
  margin: 0;
}

@media (max-width: 768px) {
  .elderly-activity-detail {
    padding: 12px;
  }
  
  .activity-content {
    padding: 16px;
  }
  
  .content-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .content-title {
    font-size: 24px;
  }
  
  .activity-images {
    margin-bottom: 16px;
  }
  
  .activity-description,
  .activity-details,
  .activity-organizer,
  .activity-reviews {
    padding: 16px;
    margin-bottom: 16px;
  }
  
  .organizer-info {
    flex-direction: column;
    text-align: center;
  }
  
  .reviews-summary {
    flex-direction: column;
    gap: 16px;
  }
  
  .action-card {
    padding: 16px;
  }
  
  .action-buttons {
    flex-direction: row;
  }
  
  .action-buttons .el-button {
    flex: 1;
  }
  
  .related-activities {
    padding: 16px;
  }
}
</style>
