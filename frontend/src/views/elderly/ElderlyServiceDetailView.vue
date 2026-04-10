<template>
  <div class="elderly-service-detail">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/elderly/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/elderly/services' }">服务列表</el-breadcrumb-item>
        <el-breadcrumb-item>服务详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="service-content" v-loading="loading">
      <div class="content-header">
        <h1 class="content-title">{{ service.name }}</h1>
        <el-button type="primary" plain @click="handleBackToList" :icon="ArrowLeft">返回服务列表</el-button>
      </div>
      <el-row :gutter="24">
        <el-col :span="16">
          <div class="service-images">
            <el-carousel height="400px" indicator-position="outside">
              <el-carousel-item v-for="(image, index) in (service.imageUrls && service.imageUrls.length > 0 ? service.imageUrls : [service.imageUrl || `https://picsum.photos/seed/service${serviceId.value}/800/600.jpg`])" :key="index">
                <img :src="image" :alt="service.name" class="service-image" />
              </el-carousel-item>
            </el-carousel>
          </div>

          <div class="service-description">
            <h2>服务介绍</h2>
            <p>{{ service.description || '暂无服务介绍' }}</p>
          </div>

          <div class="service-details">
            <h2>服务详情</h2>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="服务类型">{{ service.categoryName || '未分类' }}</el-descriptions-item>
              <el-descriptions-item label="服务费用">{{ service.price ? '￥' + service.price : '免费' }}</el-descriptions-item>
              <el-descriptions-item label="服务时长">{{ service.duration ? service.duration + '分钟' : '暂无信息' }}</el-descriptions-item>
              <el-descriptions-item label="服务状态">
                <el-tag :type="getStatusType(service.status)">{{ getStatusText(service.status) }}</el-tag>
              </el-descriptions-item>
              
            </el-descriptions>
          </div>

          <!-- 用户评价模块 -->
          <div class="service-reviews">
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
              :key="`comment-list-${serviceId}`"
              :target-type="'service_item'"
              :target-id="parseInt(serviceId)"
              :can-reply="true"
              :show-rating="true"
              @comment-submitted="handleCommentSubmitted"
              @comment-deleted="handleCommentDeleted"
            />
          </div>

        </el-col>

        <el-col :span="8">
          <div class="action-card">
            <div class="service-info">
              <div class="info-item">
                <span class="label">服务时长：</span>
                <span class="value">{{ service.duration ? service.duration + '分钟' : '暂无信息' }}</span>
              </div>
              <div class="info-item">
                <span class="label">服务状态：</span>
                <span class="value">{{ getStatusText(service.status) }}</span>
              </div>
              
            </div>
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="large" 
                @click="handleBooking" 
                :disabled="!canBook"
                :loading="booking"
              >
                {{ getBookingButtonText() }}
              </el-button>
              <el-button 
                size="large" 
                @click="handleFavorite" 
                :class="isFavorited ? 'favorited-button' : 'favorite-button'"
                :icon="isFavorited ? 'StarFilled' : 'Star'"
              >
                {{ isFavorited ? '已收藏' : '收藏' }}
              </el-button>
            </div>          </div>

          <div class="related-services" v-if="relatedServices && relatedServices.length > 0">
            <h3>相关服务</h3>
            <div class="related-list">
              <div 
                v-for="item in relatedServices" 
                :key="item.id" 
                class="related-item"
                @click="goToService(item.id)"
              >
                <img :src="item.image" :alt="item.name" class="related-image" />
                <div class="related-info">
                  <h4>{{ item.name }}</h4>
                  <p class="price">{{ item.fee }}</p>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 预约确认对话框 -->
    <el-dialog
      v-model="bookingDialogVisible"
      title="预约服务"
      width="500px"
      :before-close="handleCloseBooking"
      :close-on-click-modal="false"
      :append-to-body="true"
      destroy-on-close
    >
      <el-form :model="bookingForm" :rules="bookingRules" ref="bookingFormRef" label-width="100px">
        <div class="service-summary">
          <h3>{{ service.name }}</h3>
          <div class="service-info">
            <p><i class="el-icon-price-tag"></i> 费用：{{ service.price ? '￥' + service.price : '免费' }}</p>
            <p><i class="el-icon-time"></i> 时长：{{ service.duration ? service.duration + '分钟' : '暂无信息' }}</p>
            <p><i class="el-icon-user"></i> 预约人：{{ userStore.userInfo.name || '用户' }}</p>
          </div>
        </div>
        
        <el-form-item label="预约日期" prop="reservationDate">
          <el-date-picker
            v-model="bookingForm.reservationDate"
            type="date"
            placeholder="请选择预约日期"
            :disabled-date="disabledDate"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="预约时间" prop="reservationTime">
          <div class="booking-time-container">
            <div class="time-picker-row">
              <div class="time-picker-item">
                <div class="time-label">开始时间</div>
                <el-select
                v-model="bookingForm.startTime"
                placeholder="请选择"
                class="time-select"
                popper-class="booking-time-dropdown"
                :teleported="true"
                :popper-options="{
                  placement: 'bottom-start',
                  modifiers: [
                    {
                      name: 'offset',
                      options: {
                        offset: [0, 8]
                      }
                    }
                  ]
                }"
                @change="handleStartTimeChange"
              >
                  <el-option
                    v-for="slot in startTimeSlots"
                    :key="slot.value"
                    :label="slot.label"
                    :value="slot.value"
                  />
                </el-select>
              </div>
              <div class="time-separator">至</div>
              <div class="time-picker-item">
                <div class="time-label">结束时间</div>
                <el-select
                v-model="bookingForm.endTime"
                placeholder="请选择"
                class="time-select"
                popper-class="booking-time-dropdown"
                :teleported="true"
                :popper-options="{
                  placement: 'bottom-start',
                  modifiers: [
                    {
                      name: 'offset',
                      options: {
                        offset: [0, 8]
                      }
                    }
                  ]
                }"
                :disabled="!bookingForm.startTime"
              >
                  <el-option
                    v-for="slot in endTimeSlots"
                    :key="slot.value"
                    :label="slot.label"
                    :value="slot.value"
                  />
                </el-select>
              </div>
            </div>
            <div v-if="bookingForm.startTime && bookingForm.endTime" class="time-duration-info">
              <span class="duration-label">服务时长：</span>
              <span class="duration-value">{{ service.duration || 60 }}分钟</span>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="bookingForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入特殊需求或备注信息（选填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="bookingDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBooking" :loading="submitting">
            确认预约
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
import { ArrowLeft } from '@element-plus/icons-vue'
import { servicesApi } from '@/api/services'
import { favoriteApi } from '@/api/favorite'
import { commentApi } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import CommentList from '@/components/common/CommentList.vue'

// 格式化日期为API需要的格式
const formatDateForAPI = (date) => {
  if (!date) {
    console.error('formatDateForAPI: 日期为空')
    return ''
  }
  
  let d
  try {
    // 确保创建有效的Date对象
    d = new Date(date)
    
    // 检查日期是否有效
    if (isNaN(d.getTime())) {
      console.error('formatDateForAPI: 无效的日期对象', date)
      return ''
    }
  } catch (error) {
    console.error('formatDateForAPI: 创建日期对象失败', error)
    return ''
  }
  
  try {
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const formattedDate = `${year}-${month}-${day}`
    
    // 验证格式化结果
    if (!/^\d{4}-\d{2}-\d{2}$/.test(formattedDate)) {
      console.error('formatDateForAPI: 格式化结果不符合预期', { input: date, output: formattedDate })
      return ''
    }
    
    console.log('格式化日期:', { input: date, output: formattedDate })
    return formattedDate
  } catch (error) {
    console.error('formatDateForAPI: 格式化日期失败', error)
    return ''
  }
}

export default {
  name: 'ElderlyServiceDetailView',
  components: {
    ArrowLeft,
    CommentList
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const serviceId = ref(route.params.id)
    const userStore = useUserStore()

    const loading = ref(true)
    const service = ref({})
    const isFavorited = ref(false)
    const booking = ref(false)
    const bookingDialogVisible = ref(false)
    const submitting = ref(false)
    const bookingFormRef = ref(null)
    
    // 评价相关数据
    const reviews = ref([])
    const ratingDistribution = ref({ 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 })
    const averageRating = ref(0)
    const totalReviews = ref(0)


    // 预约表单数据
    const bookingForm = reactive({
      reservationDate: '',
      startTime: '',
      endTime: '',
      remark: ''
    })

    // 预约表单验证规则
    const bookingRules = {
      reservationDate: [
        { required: true, message: '请选择预约日期', trigger: 'change' }
      ],
      startTime: [
        { required: true, message: '请选择开始时间', trigger: 'change' }
      ],
      endTime: [
        { required: true, message: '请选择结束时间', trigger: 'change' }
      ]
    }

    // 根据服务时长生成开始时间选项（间隔为服务时长的一半）
    const startTimeSlots = computed(() => {
      const duration = service.value.duration || 60
      const interval = duration / 2
      const slots = []
      
      const morningStart = 8 * 60
      const morningEnd = 12 * 60
      const afternoonStart = 14 * 60
      const afternoonEnd = 18 * 60
      
      const now = new Date()
      const currentHour = now.getHours()
      const currentMinute = now.getMinutes()
      const currentMinutes = currentHour * 60 + currentMinute
      
      const isToday = bookingForm.reservationDate === new Date().toISOString().split('T')[0]
      
      const generateSlots = (startMinutes, endMinutes) => {
        let currentMinutes = startMinutes
        while (currentMinutes < endMinutes) {
          const hour = Math.floor(currentMinutes / 60)
          const minute = currentMinutes % 60
          const time = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
          
          if (!isToday || currentMinutes >= currentMinutes + 30) {
            slots.push({
              value: time,
              label: time
            })
          }
          
          currentMinutes += interval
        }
      }
      
      generateSlots(morningStart, morningEnd)
      generateSlots(afternoonStart, afternoonEnd)
      
      return slots
    })

    // 根据开始时间和服务时长生成结束时间选项
    const endTimeSlots = computed(() => {
      if (!bookingForm.startTime) return []
      
      const duration = service.value.duration || 60
      const slots = []
      
      const [startHour, startMinute] = bookingForm.startTime.split(':').map(Number)
      const startMinutes = startHour * 60 + startMinute
      
      const morningEnd = 12 * 60
      const afternoonEnd = 18 * 60
      
      const generateSlots = (endMinutes) => {
        const endHour = Math.floor(endMinutes / 60)
        const endMinute = endMinutes % 60
        const time = `${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}`
        slots.push({
          value: time,
          label: time
        })
      }
      
      const currentEndMinutes = startMinutes + duration
      
      if (currentEndMinutes <= morningEnd || currentEndMinutes <= afternoonEnd) {
        generateSlots(currentEndMinutes)
      }
      
      return slots
    })

    // 处理开始时间变化
    const handleStartTimeChange = () => {
      bookingForm.endTime = ''
    }

    const relatedServices = ref([
      {
        id: '2',
        name: '中医推拿',
        category: '健康服务',
        fee: '￥80/次',
        image: 'https://picsum.photos/seed/service2/200/150.jpg'
      },
      {
        id: '3',
        name: '理发服务',
        category: '生活服务',
        fee: '￥30/次',
        image: 'https://picsum.photos/seed/service3/200/150.jpg'
      }
    ])

    // 切换评价列表展开/折叠状态
    const toggleReviews = () => {
      reviewsExpanded.value = !reviewsExpanded.value
    }
    

    
    // 切换回复框显示状态
    const toggleReply = (review) => {
      // 重置所有评论的回复框状态
      serviceReviews.value.forEach(item => {
        if (item.id !== review.id) {
          item.showReplyBox = false
          item.replyContent = ''
        }
      })
      
      // 切换当前评论的回复框状态
      review.showReplyBox = !review.showReplyBox
      if (review.showReplyBox) {
        review.replyContent = ''
      }
    }
    
    // 取消回复
    const cancelReply = (review) => {
      review.showReplyBox = false
      review.replyContent = ''
    }
    
    // 提交回复
    const submitReply = async (review) => {
      if (!review.replyContent || review.replyContent.trim() === '') {
        ElMessage.warning('请输入回复内容')
        return
      }
      
      try {
        // 调用评论API提交回复
        const replyData = {
          targetType: 'service_item',
          targetId: serviceId.value,
          parentId: review.id,
          content: review.replyContent.trim(),
          replyToUserId: review.userId
        }
        
        const response = await commentApi.createComment(replyData)
        
        if (response.code === 200) {
          // 创建新回复对象
          const newReply = {
            id: response.data.id || Date.now(),
            userId: userStore.userId,
            userName: userStore.userInfo.name || '当前用户',
            userAvatar: userStore.userInfo.avatar || '',
            content: review.replyContent.trim(),
            createTime: new Date().toISOString(),
            replyToUserId: review.userId,
            replyToUserName: review.userName || '匿名用户'
          }
          
          // 添加到回复列表
          if (!review.replies) {
            review.replies = []
          }
          review.replies.push(newReply)
          
          // 更新回复数量
          review.replyCount = (review.replyCount || 0) + 1
          
          // 重置回复框
          review.showReplyBox = false
          review.replyContent = ''
          
          ElMessage.success('回复成功')
        } else {
          ElMessage.error('回复失败：' + (response.message || '未知错误'))
        }
      } catch (error) {
        console.error('提交回复失败:', error)
        ElMessage.error('回复失败，请稍后重试')
      }
    }

    // 获取服务详情
    const fetchServiceDetail = async () => {
      try {
        loading.value = true
        
        // 获取服务详情
        const serviceResponse = await servicesApi.getServiceItemDetail(serviceId.value)
        if (serviceResponse.code === 200) {
          service.value = serviceResponse.data
          
          // 获取相关服务
          fetchRelatedServices()
          
          // 获取评价数据
          fetchReviewsData()
        } else {
          ElMessage.error('获取服务详情失败: ' + (serviceResponse.message || '未知错误'))
        }
      } catch (error) {
        console.error('获取服务详情失败:', error)
        ElMessage.error('获取服务详情失败: ' + (error.message || '网络错误'))
      } finally {
        loading.value = false
      }
    }
    
    // 获取评价数据
    const fetchReviewsData = async () => {
      try {
        console.log('开始获取评价数据，serviceId:', serviceId.value);
        // 直接使用当前服务ID，确保获取的是当前服务的评论
        const targetId = parseInt(serviceId.value);
        const response = await commentApi.getCommentsByTarget('service_item', targetId, 1, 100);
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
    
    // 获取评分百分比
    const getRatingPercentage = (rating) => {
      // 计算有评分的评论总数
      const ratedReviewsCount = Object.values(ratingDistribution.value).reduce((sum, count) => sum + count, 0);
      if (ratedReviewsCount === 0) return 0;
      return Math.round((ratingDistribution.value[rating] / ratedReviewsCount) * 100);
    }
    
    // 获取评分数量
    const getRatingCount = (rating) => {
      return ratingDistribution.value[rating] || 0
    }
    

    

    
    // 获取相关服务
    const fetchRelatedServices = async () => {
      try {
        // 根据当前服务的类别获取相关服务
        if (service.value.categoryId) {
          const params = {
            pageNum: 1,
            pageSize: 10, // 增加获取数量以确保有足够的服务可选
            // 使用type参数代替categoryId，与后端API匹配
            type: service.value.categoryId,
            status: 1 // 只获取启用中的服务
          }
          
          const response = await servicesApi.getServiceItems(params)
          if (response.code === 200 && response.data) {
            // 过滤掉当前服务并按ID排序，确保数据顺序一致
            const filteredServices = response.data.records
              .filter(item => item.id !== parseInt(serviceId.value))
              .sort((a, b) => a.id - b.id) // 按ID升序排序，确保顺序一致
              
            // 选择前2个服务作为相关服务
            relatedServices.value = filteredServices.slice(0, 2)
              .map(item => ({
                id: item.id,
                name: item.name,
                category: item.categoryName || item.type || '其他服务',
                fee: item.price ? `￥${item.price}` : '免费',
                image: item.imageUrl || `https://picsum.photos/seed/service${item.id}/200/150.jpg`
              }))
          }
        }
      } catch (error) {
        console.error('获取相关服务失败:', error)
        // 如果获取失败，保留空数组
        relatedServices.value = []
      }
    }

    // 获取状态类型
    const getStatusType = (status) => {
      const statusMap = {
        0: 'info',      // 未启用
        1: 'success',   // 启用中
        2: 'danger'     // 已下架
      }
      return statusMap[status] || 'info'
    }

    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        0: '未启用',
        1: '可预约',
        2: '已下架'
      }
      return statusMap[status] || '未知'
    }

    // 是否可以预约
    const canBook = computed(() => {
      return service.value.status === 1 // 1-启用中状态可以预约
    })

    // 获取预约按钮文本
    const getBookingButtonText = () => {
      if (service.value.status === 2) return '已下架'
      if (service.value.status === 0) return '未启用'
      return '立即预约'
    }

    // 禁用过去的日期
    const disabledDate = (time) => {
      return time.getTime() < Date.now() - 8.64e7
    }

    // 处理预约按钮点击
    const handleBooking = () => {
      if (!canBook.value) return
      
      // 重置表单数据
      bookingForm.reservationDate = ''
      bookingForm.startTime = ''
      bookingForm.endTime = ''
      bookingForm.remark = ''
      
      // 显示预约对话框
      bookingDialogVisible.value = true
    }

    // 检查收藏状态
    const checkFavoriteStatus = async () => {
      try {
        const response = await favoriteApi.checkFavorite({
          targetType: 'service_item',
          targetId: serviceId.value
        })
        
        if (response && response.code === 200 && response.data) {
          isFavorited.value = response.data.isFavorited
        }
      } catch (error) {
        console.error('检查收藏状态失败:', error)
        // 检查收藏状态失败时，默认设置为未收藏
        isFavorited.value = false
      }
    }
    
    // 处理收藏
    const handleFavorite = async () => {
      try {
        // 显示加载状态
        const loading = ElMessage({
          message: isFavorited.value ? '正在取消收藏...' : '正在收藏...',
          type: 'info',
          duration: 0
        })
        
        const response = await favoriteApi.toggleFavorite({
          targetType: 'service_item',
          targetId: serviceId.value
        })
        
        // 关闭加载消息
        loading.close()
        
        if (response && response.code === 200 && response.data) {
          isFavorited.value = response.data.isFavorited
          ElMessage({
            message: response.data.message || (isFavorited.value ? '收藏成功' : '取消收藏成功'),
            type: 'success',
            duration: 2000
          })
          
          // 收藏成功后，在返回收藏列表时刷新数据
          // 通过设置一个标记，让收藏列表知道需要刷新
          sessionStorage.setItem('favoritesNeedRefresh', 'true')
        } else {
          ElMessage.error(response?.message || '操作失败，请重试')
        }
      } catch (error) {
        console.error('收藏操作失败:', error)
        ElMessage.error('操作失败，请重试')
      }
    }
    




    // 跳转到服务详情
    const goToService = async (id) => {
      try {
        // 验证服务ID是否有效
        if (!id || isNaN(id) || id <= 0) {
          ElMessage.warning('无效的服务ID')
          return
        }
        
        // 如果点击的是当前服务，不进行跳转
        if (parseInt(id) === parseInt(serviceId.value)) {
          ElMessage.info('您正在查看此服务')
          return
        }
        
        // 跳转到服务详情页
        await router.push(`/elderly/services/${id}`)
      } catch (error) {
        console.error('跳转服务详情失败:', error)
        ElMessage.error('跳转失败，请重试')
      }
    }

    // 关闭预约对话框
    const handleCloseBooking = () => {
      bookingDialogVisible.value = false
      // 重置表单
      if (bookingFormRef.value) {
        bookingFormRef.value.resetFields()
      }
    }

    // 提交预约
    const submitBooking = async () => {
      if (!bookingFormRef.value) return
      
      try {
        // 验证表单
        await bookingFormRef.value.validate()
        
        submitting.value = true

        // 准备预约数据，使用用户选择的日期和时间
        const bookingData = {
          serviceId: parseInt(serviceId.value),
          userId: userStore.userId,
          reservationDate: formatDateForAPI(bookingForm.reservationDate),
          reservationTime: `${bookingForm.startTime}-${bookingForm.endTime}`,
          remark: bookingForm.remark || '用户预约服务',
          status: 0
        }

        // 调用API提交预约
        const response = await servicesApi.bookService(bookingData)
        console.log('预约API响应:', response)
        
        // 后端返回的是Result对象，包含预约ID
        if (response && response.code === 200 && response.data) {
          ElMessage.success('预约成功！')
          
          // 关闭对话框
          bookingDialogVisible.value = false
          
          // 跳转到支付页面
          await router.push({
            path: '/elderly/payment',
            query: {
              serviceId: serviceId.value,
              bookingId: response.data,
              reservationDate: bookingData.reservationDate,
              reservationTime: bookingData.reservationTime,
              remark: bookingData.remark
            }
          })
        } else {
          ElMessage.error('预约失败，请稍后重试')
        }
      } catch (error) {
        console.error('预约失败:', error)
        console.error('错误详情:', {
          message: error.message,
          status: error.response?.status,
          data: error.response?.data
        })
        
        // 根据错误类型提供更具体的提示
        let errorMessage = '预约失败，请稍后重试'
        if (error.response) {
          if (error.response.status === 400) {
            errorMessage = '请求数据格式错误，请稍后重试'
          } else if (error.response.status === 401) {
            errorMessage = '请先登录后再进行预约'
          } else if (error.response.status === 403) {
            errorMessage = '没有权限进行此操作'
          } else if (error.response.status === 500) {
            errorMessage = '服务器内部错误，请稍后重试'
          }
        } else if (error.request) {
          errorMessage = '网络连接失败，请检查网络后重试'
        }
        
        ElMessage.error(errorMessage)
      } finally {
        submitting.value = false
      }
    }

    // 格式化日期
    const formatDate = (date) => {
      if (!date) return '-'
      const d = new Date(date)
      return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`
    }

    // 返回服务列表
    const handleBackToList = () => {
      router.push('/elderly/services')
    }
    
    // 处理评论提交事件
    const handleCommentSubmitted = () => {
      console.log('评论提交事件触发');
      fetchReviewsData();
    }
    
    // 处理评论删除事件
    const handleCommentDeleted = () => {
      console.log('评论删除事件触发');
      fetchReviewsData();
    }
    
    // 监听路由参数变化
    watch(
      () => route.params.id,
      (newId) => {
        if (newId) {
          serviceId.value = newId
          fetchServiceDetail()
        }
      },
      { immediate: false }
    )

    onMounted(() => {
      fetchServiceDetail()
      checkFavoriteStatus()
    })
    return {
      loading,
      service,
      serviceId,
      isFavorited,
      booking,
      bookingDialogVisible,
      submitting,
      bookingFormRef,
      bookingForm,
      bookingRules,
      startTimeSlots,
      endTimeSlots,
      handleStartTimeChange,
      relatedServices,
      // 评价相关
      reviews,
      ratingDistribution,
      averageRating,
      totalReviews,
      getRatingPercentage,
      getRatingCount,
      handleCommentSubmitted,
      handleCommentDeleted,
      getStatusType,
      getStatusText,
      canBook,
      getBookingButtonText,
      disabledDate,
      formatDate,
      handleBackToList,
      handleBooking,
      handleFavorite,
      goToService,
      handleCloseBooking,
      submitBooking,
      userStore,
      ArrowLeft
    }  }
}
</script>

<style scoped>
.elderly-service-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  color: #2c3e50;
  margin: 16px 0 0;
  font-weight: 600;
}

.content-title {
  font-size: 24px;
  color: #2c3e50;
  margin: 0 0 24px;
  font-weight: 600;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.content-header .content-title {
  margin-bottom: 0;
  border-bottom: none;
  padding-bottom: 0;
}

.service-content {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 24px;
}

.service-images {
  margin-bottom: 24px;
}

.service-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.service-description,
.service-details,
.service-provider {
  margin-bottom: 24px;
}

.service-description h2,
.service-details h2,
.service-provider h2 {
  font-size: 22px;
  color: #2c3e50;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}



.service-description p {
  font-size: 16px;
  line-height: 1.6;
  color: #606266;
}

.provider-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.provider-details h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #2c3e50;
}

.provider-details p {
  margin: 0 0 4px;
  color: #606266;
}

.provider-rating {
  margin-top: 8px;
}



.action-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 24px;
}

.service-info {
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  margin-bottom: 12px;
}

.label {
  color: #909399;
  width: 100px;
  flex-shrink: 0;
  white-space: nowrap;
}

.value {
  color: #606266;
  flex: 1;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-buttons .el-button {
  font-size: 16px;
  padding: 12px 0;
}

.related-services {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.related-services h3 {
  margin: 0 0 16px;
  font-size: 18px;
  color: #2c3e50;
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
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.related-item:hover {
  background-color: #f8f9fa;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.related-image {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.related-info {
  flex: 1;
}

.related-info h4 {
  margin: 0 0 4px;
  font-size: 16px;
  color: #2c3e50;
}

.related-info p {
  margin: 0 0 4px;
  font-size: 14px;
  color: #909399;
}

.price {
  color: #e6a23c;
  font-weight: 600;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 预约表单样式 - 简洁版 */
.service-summary {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.service-summary h3 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

.service-info p {
  margin: 6px 0;
  font-size: 14px;
  color: #606266;
}

.el-form-item {
  margin-bottom: 18px;
}

.el-form-item__label {
  font-size: 14px;
  color: #606266;
  font-weight: normal;
}

.el-input__inner,
.el-textarea__inner,
.el-date-editor.el-input,
.el-select .el-input__inner {
  font-size: 14px;
  border-radius: 4px;
}

.el-date-editor.el-input,
.el-select {
  width: 100%;
}

/* 对话框样式 - 简洁版 */
.el-dialog {
  border-radius: 8px;
}

.el-dialog__header {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.el-dialog__title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.el-dialog__body {
  padding: 20px;
}

.el-dialog__footer {
  padding: 12px 20px 20px;
  border-top: 1px solid #ebeef5;
}

.dialog-footer .el-button {
  font-size: 14px;
  padding: 10px 20px;
  border-radius: 4px;
}

/* 预约时间选择器样式 - 简洁版 */
.booking-time-container {
  width: 100%;
}

.time-picker-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.time-picker-item {
  flex: 1;
}

.time-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.time-select {
  width: 100%;
}

.time-select :deep(.el-input__wrapper) {
  padding: 6px 12px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.time-select :deep(.el-input__wrapper:hover) {
  border-color: #c0c4cc;
}

.time-select :deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
}

.time-separator {
  font-size: 14px;
  color: #909399;
  padding-top: 20px;
}

.time-duration-info {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
}

.duration-label {
  color: #909399;
}

.duration-value {
  color: #409eff;
  font-weight: 500;
}

/* 评价模块样式 */
.service-reviews {
  margin-top: 16px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 16px;
}

.reviews-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.reviews-header h2 {
  margin: 0 0 16px;
  font-size: 22px;
  color: #2c3e50;
}

.reviews-summary {
  display: flex;
  gap: 24px;
  align-items: center;
}

.average-rating {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 120px;
}

.rating-score {
  font-size: 36px;
  font-weight: 600;
  color: #ff9900;
  line-height: 1;
  margin-bottom: 6px;
}

.average-rating :deep(.el-rate) {
  margin: 6px 0;
}

.average-rating :deep(.el-rate__text) {
  display: none;
}

.total-reviews {
  font-size: 14px;
  color: #909399;
}

.rating-distribution {
  flex: 1;
  max-width: 400px;
}

.rating-item {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}

.star-label {
  width: 30px;
  font-size: 14px;
  color: #606266;
  text-align: right;
  margin-right: 10px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background-color: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  margin-right: 8px;
}

.progress-fill {
  height: 100%;
  background-color: #ff9900;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.star-count {
  width: 30px;
  font-size: 14px;
  color: #909399;
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .reviews-summary {
    flex-direction: column;
    gap: 20px;
    align-items: flex-start;
  }
  
  .rating-distribution {
    max-width: 100%;
  }
}

/* 老年用户模式适配 */
.elderly-mode .service-reviews {
  padding: 28px;
  border-radius: 12px;
}

.elderly-mode .reviews-header h2 {
  font-size: 24px;
}

.elderly-mode .rating-score {
  font-size: 42px;
}

.elderly-mode .star-label,
.elderly-mode .star-count {
  font-size: 16px;
}

.elderly-mode .progress-bar {
  height: 10px;
}

/* 收藏按钮样式 */
.favorite-button {
  color: #909399;
  border-color: #DCDFE6;
}

.favorite-button:hover {
  color: #F7BA2A;
  border-color: #F7BA2A;
  background-color: #FDF6EC;
}

.favorited-button {
  color: #F7BA2A;
  border-color: #F7BA2A;
  background-color: #FDF6EC;
}

.favorited-button:hover {
  color: #E6A23C;
  border-color: #E6A23C;
  background-color: #FAEBCC;
}

/* 预约时间选择器样式 */
.booking-time-container {
  width: 100%;
}

.time-picker-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.time-picker-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.time-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.time-select {
  width: 100%;
}

.time-select :deep(.el-input__wrapper) {
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s;
}

.time-select :deep(.el-input__wrapper:hover) {
  border-color: #c0c4cc;
}

.time-select :deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.time-separator {
  font-size: 16px;
  color: #909399;
  font-weight: 500;
  padding-top: 24px;
  white-space: nowrap;
}

.time-duration-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 6px;
  font-size: 14px;
}

.duration-label {
  color: #909399;
}

.duration-value {
  color: #409eff;
  font-weight: 600;
}

/* 下拉选项样式 - 简洁版 */
.booking-time-dropdown {
  max-height: 280px !important;
  min-height: auto !important;
  z-index: 9999 !important;
  position: absolute !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  background-color: #fff;
  margin-top: 4px !important;
  overflow: visible !important;
  padding: 4px 0;
}

.booking-time-dropdown :deep(.el-select-dropdown__list) {
  padding: 0;
  margin: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 0;
}

.booking-time-dropdown :deep(.el-select-dropdown__item) {
  padding: 0 16px;
  font-size: 14px;
  line-height: 36px;
  min-height: 36px;
  height: 36px !important;
  max-height: 36px !important;
  white-space: nowrap;
  overflow: hidden !important;
  text-overflow: clip;
  display: block;
  margin: 0;
  border-radius: 0;
  transition: background-color 0.2s ease;
  box-sizing: border-box;
}

.booking-time-dropdown :deep(.el-select-dropdown__item:hover) {
  background-color: #f5f7fa;
}

.booking-time-dropdown :deep(.el-select-dropdown__item.is-selected) {
  background-color: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

.booking-time-dropdown :deep(.el-select-dropdown__item.is-disabled) {
  color: #c0c4cc;
  cursor: not-allowed;
  opacity: 0.6;
}

.booking-time-dropdown :deep(.el-select-dropdown__wrap) {
  max-height: 280px;
  overflow-y: auto !important;
  overflow-x: hidden !important;
  padding: 0;
}

.booking-time-dropdown :deep(.el-scrollbar__wrap) {
  overflow-x: hidden !important;
  overflow-y: auto !important;
  max-height: 280px !important;
}

.booking-time-dropdown :deep(.el-scrollbar__view) {
  overflow: visible !important;
  padding: 0;
}

.booking-time-dropdown :deep(.el-scrollbar__bar) {
  opacity: 1;
  right: 2px;
}

.booking-time-dropdown :deep(.el-scrollbar__thumb) {
  background-color: #dcdfe6;
  border-radius: 2px;
  width: 4px;
}

.booking-time-dropdown :deep(.el-scrollbar__thumb:hover) {
  background-color: #c0c4cc;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .time-picker-row {
    flex-direction: column;
    gap: 16px;
  }
  
  .time-picker-item {
    width: 100%;
  }
  
  .time-separator {
    display: none;
  }
  
  .time-duration-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>