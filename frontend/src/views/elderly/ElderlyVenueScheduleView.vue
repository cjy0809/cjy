<template>
  <div class="elderly-venue-schedule">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/elderly/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/elderly/venues' }">场地列表</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: `/elderly/venues/${venueId}` }">场地详情</el-breadcrumb-item>
        <el-breadcrumb-item>场地日程</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="venue-schedule-content" v-loading="loading">
      <!-- 场地基本信息 -->
      <div class="venue-info-card">
        <div class="venue-header">
          <div class="venue-title-section">
            <h1 class="venue-title">{{ venue.name }}</h1>
            <div class="venue-capacity">
              <el-icon><User /></el-icon>
              <span>容纳{{ venue.capacity }}人</span>
            </div>
          </div>
          <el-button type="primary" plain @click="handleBackToDetail" :icon="ArrowLeft" size="large">
            返回场地详情
          </el-button>
        </div>
        <div class="venue-details">
          <div class="detail-item">
            <span class="label">开放时间：</span>
            <span class="value">{{ venue.openTime }}</span>
          </div>
          <div class="detail-item">
            <span class="label">场地状态：</span>
            <el-tag 
              :type="venue.status === 'AVAILABLE' ? 'success' : (venue.status === 'MAINTENANCE' ? 'warning' : 'danger')"
              size="large"
            >
              {{ venue.status === 'AVAILABLE' ? '可预约' : (venue.status === 'MAINTENANCE' ? '维护中' : '不可预约') }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 场地不可预约提示 -->
      <div v-if="venue.status === 'UNAVAILABLE'" class="venue-unavailable-alert">
        <el-icon class="alert-icon"><WarningFilled /></el-icon>
        <span class="alert-text">当前场地不可预约</span>
      </div>

      <!-- 场地维护中提示 -->
      <div v-if="venue.status === 'MAINTENANCE'" class="venue-maintenance-alert">
        <el-icon class="alert-icon"><WarningFilled /></el-icon>
        <span class="alert-text">当前场地维护中，暂时不可预约</span>
      </div>

      <!-- 日期选择器 -->
      <div class="date-selector">
        <div class="date-navigation">
          <el-button link @click="prevWeek" :disabled="isCurrentWeek">
            <el-icon><ArrowLeft /></el-icon>
            上一周
          </el-button>
          <h3 class="current-date-range">{{ currentDateRange }}</h3>
          <el-button link @click="nextWeek">
            下一周
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="date-tabs">
          <div 
            v-for="(day, index) in weekDays" 
            :key="index"
            class="date-tab"
            :class="{ active: selectedDateIndex === index, today: day.isToday }"
            @click="selectDate(index)"
          >
            <div class="day-name">{{ day.dayName }}</div>
            <div class="day-date">{{ day.date }}</div>
          </div>
        </div>
      </div>

      <!-- 时间段列表 -->
      <div class="time-slots">
        <div class="slots-header">
          <h3 class="slots-title">可选时间段</h3>
          <div class="slots-legend">
            <div class="legend-item">
              <div class="legend-color slot-available-color"></div>
              <span>可预约</span>
            </div>
            <div class="legend-item">
              <div class="legend-color slot-booked-color"></div>
              <span>已预约</span>
            </div>
            <div class="legend-item">
              <div class="legend-color slot-maintenance-color"></div>
              <span>维护中</span>
            </div>
            <div class="legend-item">
              <div class="legend-color slot-unavailable-color"></div>
              <span>不可预约</span>
            </div>
            <div class="legend-item">
              <div class="legend-color slot-full-color"></div>
              <span>已满</span>
            </div>
          </div>
        </div>
        <div class="slots-grid">
          <div 
            v-for="slot in timeSlots" 
            :key="slot.id"
            class="time-slot"
            :class="getSlotClass(slot)"
            @click="handleSlotClick(slot)"
          >
            <div class="slot-time">{{ slot.startTime }} - {{ slot.endTime }}</div>
            <div class="slot-status">{{ getSlotStatusText(slot) }}</div>
            <div class="slot-capacity" v-if="slot.status !== 'unavailable' && slot.status !== 'full' && slot.status !== 'maintenance'">
              <div class="capacity-indicator">
                <el-icon><User /></el-icon>
                <span class="capacity-text" :class="{ 'capacity-full': slot.remainingSlots === 0 }">
                  {{ slot.remainingSlots === 0 ? '已满' : `剩余${slot.remainingSlots}个名额` }}
                </span>
              </div>
            </div>
            <div class="slot-capacity" v-if="slot.status === 'full'">
              <div class="capacity-indicator">
                <el-icon><CircleClose /></el-icon>
                <span class="capacity-text capacity-full">
                  已满
                </span>
              </div>
            </div>
            <div class="slot-action" v-if="slot.status === 'available'">
              <el-icon><Plus /></el-icon>
              <span>点击预约</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 预约对话框 -->
      <el-dialog
        v-model="reservationDialogVisible"
        title="预约场地"
        width="500px"
        :before-close="handleCloseReservation"
      >
        <el-form :model="reservationForm" :rules="reservationRules" ref="reservationFormRef" label-width="100px">
          <el-form-item label="预约日期">
            <el-input :value="selectedDate" disabled />
          </el-form-item>
          <el-form-item label="预约时间">
            <el-input :value="selectedTimeSlot" disabled />
          </el-form-item>
          <el-form-item label="活动内容" prop="purpose">
            <el-input
              v-model="reservationForm.purpose"
              type="textarea"
              :rows="3"
              placeholder="请简要描述活动内容"
            />
          </el-form-item>
          <el-form-item label="参与人数" prop="participants">
            <el-input-number
              v-model="reservationForm.participants"
              :min="1"
              :max="maxParticipantsPerReservation"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="reservationDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitReservation" :loading="submitting">
              确认预约
            </el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight, User, Plus, WarningFilled, CircleClose } from '@element-plus/icons-vue'
import { venueApi } from '@/api/venue'
import { reservationApi } from '@/api/reservation'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

export default {
  name: 'ElderlyVenueScheduleView',
  components: {
    ArrowLeft,
    ArrowRight,
    User,
    Plus,
    WarningFilled,
    CircleClose
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const venueId = ref(route.params.id)
    const userStore = useUserStore()
    
    // 状态管理
    const loading = ref(true)
    const venue = ref({})
    const reservationDialogVisible = ref(false)
    const submitting = ref(false)
    const reservationFormRef = ref(null)
    const timeSlots = ref([])
    const weekDays = ref([])
    const selectedDateIndex = ref(0)
    const currentWeekStart = ref(dayjs().startOf('week'))
    const selectedSlot = ref(null)
    
    // 预约表单
    const reservationForm = reactive({
      purpose: '',
      participants: 1
    })
    
    // 表单验证规则
    const reservationRules = {
      purpose: [{ required: true, message: '请输入活动内容', trigger: 'blur' }],
      participants: [{ required: true, message: '请输入参与人数', trigger: 'blur' }]
    }
    
    // 计算属性
    const selectedDate = computed(() => {
      if (weekDays.value.length > 0 && selectedDateIndex.value >= 0) {
        return weekDays.value[selectedDateIndex.value].fullDate
      }
      return ''
    })
    
    const selectedTimeSlot = computed(() => {
      return selectedSlot.value ? `${selectedSlot.value.startTime} - ${selectedSlot.value.endTime}` : ''
    })

    const maxParticipantsPerReservation = computed(() => {
      if (!venue.value.capacity) {
        return 1
      }
      
      const capacityPerSlot = Math.max(1, Math.floor(venue.value.capacity / 10))
      return capacityPerSlot
    })

    const currentDateRange = computed(() => {
      const start = currentWeekStart.value.format('MM月DD日')
      const end = currentWeekStart.value.add(6, 'day').format('MM月DD日')
      return `${start} - ${end}`
    })
    
    const isCurrentWeek = computed(() => {
      return currentWeekStart.value.isSame(dayjs().startOf('week'), 'day')
    })
    
    // 获取场地详情
    const fetchVenueDetail = async () => {
      try {
        const response = await venueApi.getVenueById(venueId.value)
        if (response && response.data) {
          const venueData = response.data
          console.log('场地详情数据:', venueData)
          console.log('维护日期:', venueData.maintenanceDate)
          console.log('维护开始时间:', venueData.maintenanceStartTime)
          console.log('维护结束时间:', venueData.maintenanceEndTime)
          
          let facilitiesArray = []
          if (venueData.equipment) {
            facilitiesArray = venueData.equipment.split(/[,，、]/).map(item => item.trim())
          }
          const openHours = `${venueData.openTime || '08:00'}-${venueData.closeTime || '22:00'}`
          
          venue.value = {
            id: venueData.id,
            name: venueData.name,
            description: venueData.description || '暂无描述',
            capacity: venueData.capacity || 0,
            images: [
              venueData.image || `https://picsum.photos/seed/venue${venueData.id}/800/600.jpg`
            ],
            facilities: facilitiesArray.length > 0 ? facilitiesArray : ['基础设施'],
            openTime: openHours,
            address: '社区中心',
            phone: '暂无',
            price: 0,
            status: venueData.status === 1 ? 'AVAILABLE' : (venueData.status === 2 ? 'MAINTENANCE' : 'UNAVAILABLE'),
            maintenanceDate: venueData.maintenanceDate || null,
            maintenanceStartTime: venueData.maintenanceStartTime || null,
            maintenanceEndTime: venueData.maintenanceEndTime || null
          }
          
          console.log('场地状态:', venue.value.status)
          console.log('场地维护信息:', {
            maintenanceDate: venue.value.maintenanceDate,
            maintenanceStartTime: venue.value.maintenanceStartTime,
            maintenanceEndTime: venue.value.maintenanceEndTime
          })
        }
      } catch (error) {
        console.error('获取场地详情失败:', error)
        ElMessage.error('获取场地详情失败，请重试')
      }
    }
    
    // 生成周日期数据
    const generateWeekDays = () => {
      const days = []
      const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const today = dayjs().format('YYYY-MM-DD')
      
      for (let i = 0; i < 7; i++) {
        const date = currentWeekStart.value.add(i, 'day')
        days.push({
          dayName: dayNames[date.day()],
          date: date.format('MM/DD'),
          fullDate: date.format('YYYY-MM-DD'),
          isToday: date.format('YYYY-MM-DD') === today
        })
      }
      
      weekDays.value = days
    }
    
    // 生成时间段数据
    const generateTimeSlots = () => {
      const slots = []
      const openTime = venue.value.openTime ? venue.value.openTime.split('-')[0] : '08:00'
      const closeTime = venue.value.openTime ? venue.value.openTime.split('-')[1] : '22:00'
      
      // 计算每个时间段的最大预约人数（场地容量的十分之一，至少为1）
      const maxParticipants = Math.max(1, Math.floor((venue.value.capacity || 0) / 5))
      
      // 生成从开放时间到关闭时间的时间段，每30分钟一个时间段
      const startHour = parseInt(openTime.split(':')[0])
      const startMinute = parseInt(openTime.split(':')[1])
      const endHour = parseInt(closeTime.split(':')[0])
      const endMinute = parseInt(closeTime.split(':')[1])
      
      let currentHour = startHour
      let currentMinute = startMinute
      let slotId = 1
      
      while (currentHour < endHour || (currentHour === endHour && currentMinute < endMinute)) {
        const startTime = `${currentHour.toString().padStart(2, '0')}:${currentMinute.toString().padStart(2, '0')}`
        
        // 计算30分钟后的时间
        let nextHour = currentHour
        let nextMinute = currentMinute + 30
        if (nextMinute >= 60) {
          nextHour += 1
          nextMinute -= 60
        }
        
        // 确保不超过关闭时间
        if (nextHour > endHour || (nextHour === endHour && nextMinute > endMinute)) {
          break
        }
        
        const endTime = `${nextHour.toString().padStart(2, '0')}:${nextMinute.toString().padStart(2, '0')}`
        
        // 根据场地状态设置时间段状态
        let slotStatus = 'available'
        if (venue.value.status === 'UNAVAILABLE') {
          slotStatus = 'unavailable'
        } else if (venue.value.status === 'MAINTENANCE') {
          slotStatus = 'maintenance'
        } else {
          // 检查该时间段是否在维护时间段内
          if (venue.value.maintenanceDate && venue.value.maintenanceStartTime && venue.value.maintenanceEndTime) {
            const maintenanceDate = dayjs(venue.value.maintenanceDate).format('YYYY-MM-DD')
            const currentDate = dayjs(selectedDate.value).format('YYYY-MM-DD')
            
            console.log('检查维护状态:', {
              maintenanceDate,
              currentDate,
              maintenanceStart: venue.value.maintenanceStartTime,
              maintenanceEnd: venue.value.maintenanceEndTime,
              slotStartTime: startTime,
              slotEndTime: endTime
            })
            
            // 检查是否是维护日期
            if (maintenanceDate === currentDate) {
              // 检查维护时间段是否已经过去
              const maintenanceEndDateTime = dayjs(`${maintenanceDate} ${venue.value.maintenanceEndTime}`)
              const currentDateTime = dayjs()
              
              // 如果维护结束时间已经早于当前时间，则跳过维护状态检查
              // 这样维护时间段过去后，会按照正常的"时间段已过"逻辑处理
              if (maintenanceEndDateTime.isBefore(currentDateTime)) {
                console.log('维护时间段已过去，跳过维护状态检查')
              } else {
                // 检查时间段是否在维护时间段内
                const maintenanceStart = venue.value.maintenanceStartTime
                const maintenanceEnd = venue.value.maintenanceEndTime
                
                // 比较时间，如果时间段在维护时间段内，则设置为维护状态
                if (startTime >= maintenanceStart && endTime <= maintenanceEnd) {
                  console.log('时间段在维护时间段内，设置为维护状态')
                  slotStatus = 'maintenance'
                } else if (startTime < maintenanceEnd && endTime > maintenanceStart) {
                  // 时间段与维护时间段有重叠，也设置为维护状态
                  console.log('时间段与维护时间段有重叠，设置为维护状态')
                  slotStatus = 'maintenance'
                }
              }
            }
          }
        }
        
        slots.push({
          id: slotId++,
          date: selectedDate.value,
          startTime,
          endTime,
          status: slotStatus,
          userName: '',
          purpose: '',
          maxParticipants, // 每个时间段的最大预约人数
          currentParticipants: 0, // 当前已预约人数
          remainingSlots: maxParticipants, // 剩余可预约数量
          fullDate: selectedDate.value // 添加完整日期信息
        })
        
        currentHour = nextHour
        currentMinute = nextMinute
      }
      
      timeSlots.value = slots
    }
    
    // 获取场地预约数据
    const fetchVenueSchedule = async () => {
      if (!selectedDate.value) return
      
      try {
        loading.value = true
        const params = {
          month: dayjs(selectedDate.value).format('YYYY-MM'),
          venueId: venueId.value
        }
        
        const response = await venueApi.getVenueSchedule(venueId.value, params)
        const reservations = response.data || []
        
        // 更新时间段状态
        timeSlots.value.forEach(slot => {
          // 如果场地不可预约，所有时间段都不可预约
          if (venue.value.status === 'UNAVAILABLE') {
            slot.status = 'unavailable'
            slot.userName = ''
            slot.purpose = ''
            slot.currentParticipants = 0
            slot.remainingSlots = slot.maxParticipants
            return
          }
          
          // 如果场地维护中，所有时间段都显示为维护中
          if (venue.value.status === 'MAINTENANCE') {
            slot.status = 'maintenance'
            slot.userName = ''
            slot.purpose = ''
            slot.currentParticipants = 0
            slot.remainingSlots = 0
            return
          }
          
          // 检查该时间段是否在维护时间段内
          if (venue.value.maintenanceDate && venue.value.maintenanceStartTime && venue.value.maintenanceEndTime) {
            const maintenanceDate = dayjs(venue.value.maintenanceDate).format('YYYY-MM-DD')
            const currentDate = dayjs(selectedDate.value).format('YYYY-MM-DD')
            
            // 检查是否是维护日期
            if (maintenanceDate === currentDate) {
              // 检查维护时间段是否已经过去
              const maintenanceEndDateTime = dayjs(`${maintenanceDate} ${venue.value.maintenanceEndTime}`)
              const currentDateTime = dayjs()
              
              // 如果维护结束时间已经早于当前时间，则跳过维护状态检查
              // 这样维护时间段过去后，会按照正常的"时间段已过"逻辑处理
              if (maintenanceEndDateTime.isBefore(currentDateTime)) {
                console.log('维护时间段已过去，跳过维护状态检查')
              } else {
                // 检查时间段是否在维护时间段内
                const maintenanceStart = venue.value.maintenanceStartTime
                const maintenanceEnd = venue.value.maintenanceEndTime
                
                // 比较时间，如果时间段在维护时间段内，则设置为维护状态
                if (slot.startTime >= maintenanceStart && slot.endTime <= maintenanceEnd) {
                  slot.status = 'maintenance'
                  slot.userName = ''
                  slot.purpose = ''
                  slot.currentParticipants = 0
                  slot.remainingSlots = 0
                  return
                } else if (slot.startTime < maintenanceEnd && slot.endTime > maintenanceStart) {
                  // 时间段与维护时间段有重叠，也设置为维护状态
                  slot.status = 'maintenance'
                  slot.userName = ''
                  slot.purpose = ''
                  slot.currentParticipants = 0
                  slot.remainingSlots = 0
                  return
                }
              }
            }
          }
          
          // 统计该时间段的已预约人数
          const slotReservations = reservations.filter(r => 
            r.reservationDate === selectedDate.value && 
            r.startTime.startsWith(slot.startTime) && 
            r.endTime.startsWith(slot.endTime) &&
            (r.status === 1 || r.status === 0) // 已通过或待审核的预约
          )
          
          // 检查当前用户是否已经在该时间段预约过
          const userHasReservation = slotReservations.some(r => r.userId === userStore.userId)
          
          // 检查时间段是否已经过去（包括日期和时间）
          const slotDateTime = dayjs(`${slot.fullDate} ${slot.startTime}`)
          const currentDateTime = dayjs()
          const isSlotPast = slotDateTime.isBefore(currentDateTime)
          
          // 计算当前已预约数量（每个预约只占1个名额，不管参与人数多少）
          slot.currentParticipants = slotReservations.length
          
          // 计算剩余可预约数量
          slot.remainingSlots = Math.max(0, slot.maxParticipants - slot.currentParticipants)
          
          // 如果场地不可预约或时间段已过，设置为不可预约
          if (isSlotPast) {
            slot.status = 'unavailable'
            slot.remainingSlots = 0
          } else if (userHasReservation) {
            // 当前用户已在该时间段预约过
            slot.status = 'booked'
          } else if (slot.remainingSlots <= 0) {
            slot.status = 'full'
          } else {
            // 只要有剩余名额，就显示为可预约状态
            slot.status = 'available'
          }
          
          // 设置预约信息（用于显示，如果有预约）
          if (slotReservations.length > 0) {
            const latestReservation = slotReservations[slotReservations.length - 1]
            slot.userName = latestReservation.userName
            slot.purpose = latestReservation.purpose
          } else {
            slot.userName = ''
            slot.purpose = ''
          }
        })
      } catch (error) {
        console.error('获取场地日程失败:', error)
        ElMessage.error('获取场地日程失败，请重试')
      } finally {
        loading.value = false
      }
    }
    
    // 选择日期
    const selectDate = (index) => {
      selectedDateIndex.value = index
      generateTimeSlots()
      fetchVenueSchedule()
    }
    
    // 上一周
    const prevWeek = () => {
      currentWeekStart.value = currentWeekStart.value.subtract(1, 'week')
      generateWeekDays()
      selectedDateIndex.value = 0
      generateTimeSlots()
      fetchVenueSchedule()
    }
    
    // 下一周
    const nextWeek = () => {
      currentWeekStart.value = currentWeekStart.value.add(1, 'week')
      generateWeekDays()
      selectedDateIndex.value = 0
      generateTimeSlots()
      fetchVenueSchedule()
    }
    
    // 返回场地详情
    const handleBackToDetail = () => {
      router.push(`/elderly/venues/${venueId.value}`)
    }
    
    // 处理时间段点击
    const handleSlotClick = (slot) => {
      if (venue.value.status === 'UNAVAILABLE') {
        ElMessage.warning('当前场地不可预约')
        return
      }
      
      if (venue.value.status === 'MAINTENANCE') {
        ElMessage.warning('当前场地维护中，暂时不可预约')
        return
      }
      
      // 如果时间段已过，显示提示信息
      const slotDateTime = dayjs(`${slot.fullDate} ${slot.startTime}`)
      const currentDateTime = dayjs()
      if (slot.status === 'unavailable' && slotDateTime.isBefore(currentDateTime)) {
        ElMessage.warning('该时间段已过，不可预约')
        return
      }
      
      // 如果时间段不可用，显示提示信息
      if (slot.status !== 'available') {
        if (slot.status === 'full') {
          ElMessage.warning('该时间段预约已满')
        } else if (slot.status === 'booked') {
          ElMessage.warning('您已预约该时间段，不可重复预约')
        } else if (slot.status === 'maintenance') {
          ElMessage.warning('当前场地维护中，暂时不可预约')
        } else {
          ElMessage.warning('该时间段不可预约')
        }
        return
      }
      
      selectedSlot.value = slot
      reservationForm.purpose = ''
      reservationForm.participants = 1
      reservationDialogVisible.value = true
    }
    
    // 获取时间段样式类
    const getSlotClass = (slot) => {
      return {
        'slot-available': slot.status === 'available',
        'slot-booked': slot.status === 'booked',
        'slot-maintenance': slot.status === 'maintenance',
        'slot-unavailable': slot.status === 'unavailable',
        'slot-full': slot.status === 'full'
      }
    }
    
    // 获取时间段状态文本
    const getSlotStatusText = (slot) => {
      switch (slot.status) {
        case 'available':
          return '可预约'
        case 'booked':
          return '已预约'
        case 'full':
          return '已满'
        case 'maintenance':
          return '维护中'
        case 'unavailable':
          return '不可预约'
        default:
          return '未知状态'
      }
    }
    
    // 关闭预约对话框
    const handleCloseReservation = () => {
      reservationDialogVisible.value = false
      if (reservationFormRef.value) {
        reservationFormRef.value.resetFields()
      }
      selectedSlot.value = null
    }
    
    // 提交预约
    const submitReservation = async () => {
      if (!reservationFormRef.value) return
      
      try {
        await reservationFormRef.value.validate()
        submitting.value = true
        
        const requestData = {
          userId: userStore.userId,
          venueId: parseInt(venueId.value),
          reservationDate: selectedDate.value,
          startTime: selectedSlot.value.startTime + ':00',
          endTime: selectedSlot.value.endTime + ':00',
          purpose: reservationForm.purpose,
          participants: reservationForm.participants
        }
        
        await reservationApi.createReservation(requestData)
        ElMessage.success('预约提交成功，请等待确认')
        reservationDialogVisible.value = false
        if (reservationFormRef.value) {
          reservationFormRef.value.resetFields()
        }
        selectedSlot.value = null
        
        // 刷新日程数据
        fetchVenueSchedule()
      } catch (error) {
        console.error('预约失败:', error)
        ElMessage.error('预约失败，请重试')
      } finally {
        submitting.value = false
      }
    }
    
    // 初始化
    const init = async () => {
      loading.value = true
      try {
        await fetchVenueDetail()
        generateWeekDays()
        generateTimeSlots()
        await fetchVenueSchedule()
      } catch (error) {
        console.error('初始化失败:', error)
      } finally {
        loading.value = false
      }
    }
    
    // 监听场地状态变化
    watch(
      () => venue.value.status,
      (newStatus, oldStatus) => {
        // 只有当状态发生变化时才更新
        if (newStatus !== oldStatus) {
          console.log('场地状态变化:', { from: oldStatus, to: newStatus })
          
          // 重新生成时间段，应用新的状态
          generateTimeSlots()
          
          // 如果已有预约数据，也需要更新时间段状态
          if (timeSlots.value.length > 0) {
            fetchVenueSchedule()
          }
        }
      },
      { immediate: false }
    )
    
    onMounted(() => {
      init()
    })
    
    return {
      loading,
      venue,
      venueId,
      reservationDialogVisible,
      submitting,
      reservationFormRef,
      reservationForm,
      reservationRules,
      timeSlots,
      weekDays,
      selectedDateIndex,
      currentDateRange,
      isCurrentWeek,
      selectedDate,
      selectedTimeSlot,
      maxParticipantsPerReservation,
      handleBackToDetail,
      prevWeek,
      nextWeek,
      selectDate,
      handleSlotClick,
      getSlotClass,
      getSlotStatusText,
      handleCloseReservation,
      submitReservation,
      ArrowLeft,
      ArrowRight,
      User,
      Plus,
      WarningFilled,
      CircleClose
    }
  }
}
</script>

<style scoped>
.elderly-venue-schedule {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.venue-schedule-content {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  padding: 24px;
}

.venue-info-card {
  margin-bottom: 24px;
  padding: 24px;
  background-color: #f8f9fa;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.venue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.venue-title-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.venue-title {
  font-size: 28px;
  color: #2c3e50;
  margin: 0;
  font-weight: 600;
}

.venue-capacity {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  color: #606266;
}

.venue-details {
  display: flex;
  flex-wrap: wrap;
  gap: 32px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label {
  color: #909399;
  font-weight: 500;
  font-size: 16px;
}

.value {
  color: #606266;
  font-weight: 500;
  font-size: 16px;
}

.available {
  color: #67c23a;
  font-weight: 600;
}

.unavailable {
  color: #f56c6c;
  font-weight: 600;
}


.venue-unavailable-alert {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  margin-bottom: 24px;
  background-color: #fef0f0;
  border: 2px solid #f56c6c;
  border-radius: 12px;
  color: #f56c6c;
  font-size: 18px;
  font-weight: 600;
  box-shadow: 0 2px 12px rgba(245, 108, 108, 0.1);
}

.venue-maintenance-alert {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  margin-bottom: 24px;
  background-color: #fdf6ec;
  border: 2px solid #e6a23c;
  border-radius: 12px;
  color: #e6a23c;
  font-size: 18px;
  font-weight: 600;
  box-shadow: 0 2px 12px rgba(230, 162, 60, 0.1);
}

.alert-icon {
  font-size: 24px;
}

.alert-text {
  font-size: 18px;
  font-weight: 600;
}

.date-selector {
  margin-bottom: 32px;
  background-color: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.date-navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.current-date-range {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.date-tabs {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 4px;
}

.date-tab {
  min-width: 100px;
  padding: 16px;
  text-align: center;
  border-radius: 12px;
  background-color: #f5f7fa;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 3px solid transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.date-tab:hover {
  background-color: #ecf5ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.date-tab.active {
  background-color: #409eff;
  color: white;
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.date-tab.today {
  border-color: #e6a23c;
}

.day-name {
  font-size: 16px;
  margin-bottom: 6px;
  font-weight: 600;
}

.day-date {
  font-size: 18px;
  font-weight: 700;
}

.time-slots {
  margin-bottom: 24px;
  background-color: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.slots-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.slots-title {
  font-size: 20px;
  color: #2c3e50;
  margin: 0;
  font-weight: 600;
}

.slots-legend {
  display: flex;
  gap: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.slot-available-color {
  background-color: #409eff;
}

.slot-booked-color {
  background-color: #f56c6c;
}

.slot-unavailable-color {
  background-color: #c0c4cc;
}

.slot-full-color {
  background-color: #f56c6c;
}

.slot-maintenance-color {
  background-color: #e6a23c;
}

.slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.time-slot {
  padding: 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 3px solid transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.time-slot:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.slot-time {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.slot-status {
  font-size: 16px;
  font-weight: 500;
}

.slot-info {
  font-size: 14px;
  color: #909399;
}

.slot-capacity {
  margin-top: 8px;
  padding: 8px;
  background-color: rgba(64, 158, 255, 0.1);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.capacity-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
}

.capacity-text {
  font-size: 14px;
  font-weight: 500;
  color: #409eff;
}

.capacity-full {
  color: #f56c6c;
  font-weight: 600;
}

.slot-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 8px;
  padding: 8px;
  background-color: rgba(64, 158, 255, 0.1);
  border-radius: 8px;
  color: #409eff;
  font-weight: 500;
  font-size: 14px;
}

.slot-available {
  background-color: #f0f9ff;
  border-color: #409eff;
}

.slot-available:hover {
  background-color: #ecf5ff;
}

.slot-booked {
  background-color: #fef0f0;
  border-color: #f56c6c;
  color: #f56c6c;
  cursor: not-allowed;
}

.slot-unavailable {
  background-color: #f5f7fa;
  border-color: #c0c4cc;
  color: #909399;
  cursor: not-allowed;
}

.slot-full {
  background-color: #fef0f0;
  border-color: #f56c6c;
  color: #f56c6c;
  cursor: not-allowed;
}

.slot-maintenance {
  background-color: #fdf6ec;
  border-color: #e6a23c;
  color: #e6a23c;
  cursor: not-allowed;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .elderly-venue-schedule {
    padding: 15px;
  }
  
  .venue-schedule-content {
    padding: 16px;
  }
  
  .venue-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .venue-title {
    font-size: 24px;
  }
  
  .venue-details {
    flex-direction: column;
    gap: 16px;
  }
  
  .date-navigation {
    flex-direction: column;
    gap: 16px;
  }
  
  .date-tabs {
    justify-content: space-between;
  }
  
  .date-tab {
    min-width: 70px;
    padding: 12px;
  }
  
  .day-name {
    font-size: 14px;
  }
  
  .day-date {
    font-size: 16px;
  }
  
  .slots-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .slots-legend {
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .slots-grid {
    grid-template-columns: 1fr;
  }
  
  .time-slot {
    padding: 16px;
  }
  
  .slot-time {
    font-size: 16px;
  }
  
  .slot-status {
    font-size: 14px;
  }
  
  .venue-unavailable-alert {
    flex-direction: column;
    gap: 12px;
    padding: 12px;
  }
  
  .venue-maintenance-alert {
    flex-direction: column;
    gap: 12px;
    padding: 12px;
  }
  
  .alert-icon {
    font-size: 20px;
  }
  
  .alert-text {
    font-size: 16px;
  }
  
  .slot-capacity {
    margin-top: 6px;
    padding: 6px;
  }
  
  .capacity-indicator {
    gap: 4px;
  }
  
  .capacity-text {
    font-size: 12px;
  }
}
</style>