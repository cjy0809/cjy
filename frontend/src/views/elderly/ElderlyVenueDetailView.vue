<template>
  <div class="elderly-venue-detail">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/elderly/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/elderly/venues' }">场地列表</el-breadcrumb-item>
        <el-breadcrumb-item>场地详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="venue-content" v-loading="loading">
      <div class="content-header">
        <h1 class="content-title">{{ venue.name }}</h1>
        <el-button type="primary" plain @click="handleBackToList" :icon="ArrowLeft">返回场地列表</el-button>
      </div>
      <el-row :gutter="24">
        <el-col :span="16">
          <div class="venue-images">
            <el-carousel height="400px" indicator-position="outside">
              <el-carousel-item v-for="(image, index) in venue.images" :key="index">
                <img :src="image" :alt="venue.name" class="venue-image" />
              </el-carousel-item>
            </el-carousel>
          </div>

          <div class="venue-description">
            <h2>场地介绍</h2>
            <p>{{ venue.description }}</p>
          </div>

          <div class="venue-facilities">
            <h2>设施信息</h2>
            <el-row :gutter="16">
              <el-col :span="8" v-for="(facility, index) in venue.facilities" :key="index">
                <div class="facility-item">
                  <el-icon><Check /></el-icon>
                  <span>{{ facility }}</span>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-col>

        <el-col :span="8">
          <div class="venue-info-card">
            <div class="info-item">
              <h3>基本信息</h3>
              <div class="info-row">
                <span class="label">容纳人数：</span>
                <span class="value">{{ venue.capacity }}人</span>
              </div>
              <div class="info-row">
                <span class="label">开放时间：</span>
                <span class="value">{{ venue.openTime }}</span>
              </div>
              
            </div>

            <div class="info-item">
              <h3>预约信息</h3>
              <div class="info-row">
                <span class="label">预约状态：</span>
                <span class="value" :class="venue.status === 'AVAILABLE' ? 'available' : 'unavailable'">
                  {{ venue.status === 'AVAILABLE' ? '可预约' : '不可预约' }}
                </span>
              </div>
            </div>

            <div class="action-buttons">
              <el-button type="primary" size="large" @click="viewSchedule">
                查看日程
              </el-button>
            </div>
          </div>
          
          <!-- 相关场地 -->
          <div class="related-venues" v-if="relatedVenues && relatedVenues.length > 0">
            <h3>相关场地</h3>
            <div class="related-list">
              <div 
                v-for="item in relatedVenues" 
                :key="item.id" 
                class="related-item"
                @click="goToVenue(item.id)"
              >
                <img :src="item.image" :alt="item.name" class="related-image" />
                <div class="related-info">
                  <h4>{{ item.name }}</h4>
                  <p>容纳人数：{{ item.capacity }}人</p>
                  <p class="status" :class="item.status === 'AVAILABLE' ? 'available' : 'unavailable'">
                    {{ item.status === 'AVAILABLE' ? '可预约' : '不可预约' }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import { venueApi } from '@/api/venue'

export default {
  name: 'ElderlyVenueDetailView',
  components: {
    ArrowLeft,
    Check
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const venueId = ref(route.params.id)

    const loading = ref(true)
    const venue = ref({})
    const relatedVenues = ref([]) // 相关场地数据

    // 获取场地详情
    const fetchVenueDetail = async () => {
      try {
        loading.value = true
        const response = await venueApi.getVenueById(venueId.value)
        console.log('场地详情API响应:', response)
        
        if (response && response.data) {
          // 将后端数据映射到前端需要的格式
          const venueData = response.data
          
          // 将equipment字符串转换为facilities数组
          let facilitiesArray = []
          if (venueData.equipment) {
            facilitiesArray = venueData.equipment.split(/[,，、]/).map(item => item.trim())
          }
          
          // 组合开放时间
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
            address: '社区中心', // 默认值，因为后端没有此字段
            phone: '暂无', // 默认值，因为后端没有此字段
            price: 0, // 默认值，因为后端没有此字段
            status: venueData.status === 1 ? 'AVAILABLE' : 'UNAVAILABLE'
          }
          
          console.log('处理后场地数据:', venue.value)
        } else {
          ElMessage.error('获取场地详情失败：数据格式异常')
        }
      } catch (error) {
        console.error('获取场地详情失败:', error)
        ElMessage.error('获取场地详情失败，请重试')
      } finally {
        loading.value = false
      }
    }

    // 获取相关场地
    const fetchRelatedVenues = async () => {
      try {
        // 获取场地列表，设置较大的pageSize以获取足够的数据
        const response = await venueApi.getVenuePage({ 
          pageNum: 1, 
          pageSize: 20,
          status: 1 // 只获取可用场地
        })
        
        if (response && response.data && response.data.records) {
          // 过滤掉当前场地
          const filteredVenues = response.data.records.filter(v => v.id !== parseInt(venueId.value))
          
          // 随机选择3个场地
          const shuffled = [...filteredVenues].sort(() => 0.5 - Math.random())
          const selectedVenues = shuffled.slice(0, 3)
          
          // 映射数据格式
          relatedVenues.value = selectedVenues.map(v => ({
            id: v.id,
            name: v.name,
            capacity: v.capacity || 0,
            image: v.image || `https://picsum.photos/seed/venue${v.id}/200/150.jpg`,
            status: v.status === 1 ? 'AVAILABLE' : 'UNAVAILABLE'
          }))
        }
      } catch (error) {
        console.error('获取相关场地失败:', error)
        relatedVenues.value = []
      }
    }

    // 查看日程
    const viewSchedule = () => {
      // 跳转到场地日程页面
      router.push(`/elderly/venues/${venueId.value}/schedule`)
    }

    // 返回场地列表
    const handleBackToList = () => {
      router.push('/elderly/venues')
    }

    // 跳转到场地详情
    const goToVenue = async (id) => {
      try {
        // 验证场地ID是否有效
        if (!id || isNaN(id) || id <= 0) {
          ElMessage.warning('无效的场地ID')
          return
        }
        
        // 如果点击的是当前场地，不进行跳转
        if (parseInt(id) === parseInt(venueId.value)) {
          ElMessage.info('您正在查看此场地')
          return
        }
        
        // 跳转到场地详情页
        await router.push(`/elderly/venues/${id}`)
      } catch (error) {
        console.error('跳转场地详情失败:', error)
        ElMessage.error('跳转失败，请重试')
      }
    }

    // 监听路由参数变化
    watch(
      () => route.params.id,
      (newId) => {
        if (newId) {
          venueId.value = newId
          fetchVenueDetail()
          fetchRelatedVenues()
        }
      },
      { immediate: false }
    )

    onMounted(() => {
      fetchVenueDetail()
      fetchRelatedVenues()
    })

    return {
      loading,
      venue,
      relatedVenues,
      venueId,
      handleBackToList,
      viewSchedule,
      goToVenue,
      ArrowLeft,
      Check
    }
  }
}
</script>

<style scoped>
.elderly-venue-detail {
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

.venue-content {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 24px;
}

.venue-images {
  margin-bottom: 24px;
}

.venue-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.venue-description,
.venue-facilities {
  margin-bottom: 24px;
}

.venue-description h2,
.venue-facilities h2 {
  font-size: 22px;
  color: #2c3e50;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.venue-description p {
  font-size: 16px;
  line-height: 1.6;
  color: #606266;
}

.facility-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding: 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.facility-item .el-icon {
  color: #67c23a;
  margin-right: 8px;
  font-size: 18px;
}

.venue-info-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.info-item {
  margin-bottom: 24px;
}

.info-item h3 {
  font-size: 18px;
  color: #2c3e50;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.info-row {
  display: flex;
  margin-bottom: 12px;
}

.label {
  color: #909399;
  width: 100px;
  flex-shrink: 0;
}

.value {
  color: #606266;
  flex: 1;
}

.price {
  color: #e6a23c;
  font-weight: 600;
  font-size: 18px;
}

.available {
  color: #67c23a;
  font-weight: 600;
}

.unavailable {
  color: #f56c6c;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.action-buttons .el-button {
  flex: 1;
  font-size: 16px;
  padding: 12px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.related-venues {
  margin-top: 24px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.related-venues h3 {
  font-size: 20px;
  color: #2c3e50;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.related-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.related-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.related-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
  transform: translateY(-2px);
}

.related-image {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 12px;
}

.related-info {
  flex: 1;
}

.related-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #2c3e50;
}

.related-info p {
  margin: 4px 0;
  font-size: 14px;
  color: #606266;
}

.related-info .status.available {
  color: #67c23a;
  font-weight: 600;
}

.related-info .status.unavailable {
  color: #f56c6c;
  font-weight: 600;
}
</style>