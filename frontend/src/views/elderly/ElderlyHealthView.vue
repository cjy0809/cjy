<template>
  <div class="elderly-health">
    <!-- 页面标题 -->
    <div class="elderly-card">
      <div class="elderly-card-header">
        <h2 class="elderly-card-title">健康记录</h2>
      </div>
      <div class="elderly-card-body">
        <p class="page-description">查看和管理您的健康数据，关注身体状况变化</p>
      </div>
    </div>
    
    <!-- 健康记录表格 -->
    <div class="elderly-card">
      <div class="elderly-card-header">
        <h3 class="elderly-card-title">健康记录</h3>
      </div>
      <div class="elderly-card-body">
        <div class="health-records-table-container" v-if="!loading && filteredRecords.length > 0">
          <div class="health-records-grid">
            <div class="health-records-column">
              <div class="health-records-column-header">
                <div class="column-header-title">记录日期</div>
                <div class="column-header-title">操作</div>
              </div>
              <div class="health-records-column-body">
                <div v-for="(record, index) in leftColumnRecords" :key="record.id" class="record-row">
                  <div class="record-date">{{ formatDateTime(record.recordTime) }}</div>
                  <div class="record-action">
                    <button class="view-detail-btn" @click="viewDetail(record)">
                      查看详情
                    </button>
                  </div>
                </div>
              </div>
            </div>
            <div class="health-records-column">
              <div class="health-records-column-header">
                <div class="column-header-title">记录日期</div>
                <div class="column-header-title">操作</div>
              </div>
              <div class="health-records-column-body">
                <div v-for="(record, index) in rightColumnRecords" :key="record.id" class="record-row">
                  <div class="record-date">{{ formatDateTime(record.recordTime) }}</div>
                  <div class="record-action">
                    <button class="view-detail-btn" @click="viewDetail(record)">
                      查看详情
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 分页器 -->
        <div v-if="totalPages > 1" class="pagination-container">
          <div class="pagination-info">
            <span>共 {{ healthRecords.length }} 条记录</span>
            <span>第 {{ currentPage }} / {{ totalPages }} 页</span>
          </div>
          
          <div class="pagination-controls">
            <button 
              class="pagination-btn" 
              @click="goToFirstPage"
              :disabled="currentPage === 1"
              title="首页"
            >
              首页
            </button>
            
            <button 
              class="pagination-btn" 
              @click="goToPrevPage"
              :disabled="currentPage === 1"
              title="上一页"
            >
              上一页
            </button>
            
            <!-- 页码按钮 -->
            <div class="pagination-pages">
              <button 
                v-for="page in visiblePages" 
                :key="page"
                class="pagination-btn pagination-number"
                :class="{ active: page === currentPage }"
                @click="handlePageChange(page)"
              >
                {{ page }}
              </button>
            </div>
            
            <button 
              class="pagination-btn" 
              @click="goToNextPage"
              :disabled="currentPage === totalPages"
              title="下一页"
            >
              下一页
            </button>
            
            <button 
              class="pagination-btn" 
              @click="goToLastPage"
              :disabled="currentPage === totalPages"
              title="末页"
            >
              末页
            </button>
          </div>
        </div>
      </div>
          
      <div class="loading-container" v-if="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
          
      <div class="empty-state" v-if="!loading && filteredRecords.length === 0">
        <p>暂无健康记录</p>
        <p class="empty-hint">系统尚未记录您的健康数据，请联系社区工作人员添加健康记录</p>
      </div>
    </div>
    
    <!-- 健康详情弹窗 -->
    <div v-if="showDetailDialog" class="health-report-modal-overlay" @click.self="closeDetailDialog">
      <div class="health-report-modal">
        <div class="health-report-header">
          <button class="health-report-close" @click="closeDetailDialog">×</button>
          <h2 class="health-report-title">健康检查报告</h2>
          <div class="health-report-meta">
            <span>检查日期：{{ formatDateTime(currentRecord.recordTime) }}</span>
          </div>
        </div>
        
        <div class="health-report-body">
          <!-- 基础项目 -->
          <div class="health-report-section" v-if="hasBasicItems">
            <h3 class="health-report-section-title">基础项目</h3>
            <table class="health-report-table">
              <tbody>
                <tr v-if="currentRecord.bloodPressureHigh || currentRecord.bloodPressureLow">
                  <td class="health-report-label">血压（收缩压/舒张压 mmHg）</td>
                  <td class="health-report-value">{{ currentRecord.bloodPressureHigh }}/{{ currentRecord.bloodPressureLow }}</td>
                  <td class="health-report-status" :class="getBloodPressureStatusClass(currentRecord.bloodPressureHigh, currentRecord.bloodPressureLow)">
                    {{ getBloodPressureStatus(currentRecord.bloodPressureHigh, currentRecord.bloodPressureLow) }}
                  </td>
                </tr>
                <tr v-if="currentRecord.heartRate">
                  <td class="health-report-label">心率（次/分钟）</td>
                  <td class="health-report-value">{{ currentRecord.heartRate }}</td>
                  <td class="health-report-status" :class="getHeartRateStatusClass(currentRecord.heartRate)">
                    {{ getHeartRateStatus(currentRecord.heartRate) }}
                  </td>
                </tr>
                <tr v-if="currentRecord.bloodSugar">
                  <td class="health-report-label">血糖（mmol/L）</td>
                  <td class="health-report-value">{{ currentRecord.bloodSugar }}</td>
                  <td class="health-report-status" :class="getBloodSugarStatusClass(currentRecord.bloodSugar, currentRecord.bloodSugarType)">
                    {{ getBloodSugarStatus(currentRecord.bloodSugar, currentRecord.bloodSugarType) }}
                  </td>
                </tr>
                <tr v-if="currentRecord.temperature">
                  <td class="health-report-label">体温（°C）</td>
                  <td class="health-report-value">{{ currentRecord.temperature }}</td>
                  <td class="health-report-status" :class="getTemperatureStatusClass(currentRecord.temperature, currentRecord.temperatureType)">
                    {{ getTemperatureStatus(currentRecord.temperature, currentRecord.temperatureType) }}
                  </td>
                </tr>
                <tr v-if="currentRecord.weight && currentRecord.height">
                  <td class="health-report-label">身高/体重（cm/kg）</td>
                  <td class="health-report-value">{{ currentRecord.height }}/{{ currentRecord.weight }}</td>
                  <td class="health-report-status" :class="getBMIStatusClass(currentRecord.weight, currentRecord.height)">
                    BMI: {{ calculateBMI(currentRecord.weight, currentRecord.height) }} - {{ getBMIStatus(currentRecord.weight, currentRecord.height) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          
          <!-- 血常规检查 -->
          <div class="health-report-section" v-if="hasBloodTestItems">
            <h3 class="health-report-section-title">血常规检查</h3>
            <table class="health-report-table">
              <tbody>
                <tr v-if="currentRecord.whiteBloodCell">
                  <td class="health-report-label">白细胞计数 (×10^9/L)</td>
                  <td class="health-report-value">{{ currentRecord.whiteBloodCell }}</td>
                  <td class="health-report-status" :class="getWhiteBloodCellStatusClass(currentRecord.whiteBloodCell)">
                    {{ getWhiteBloodCellStatus(currentRecord.whiteBloodCell) }}
                  </td>
                </tr>
                <tr v-if="currentRecord.hemoglobin">
                  <td class="health-report-label">血红蛋白 (g/L)</td>
                  <td class="health-report-value">{{ currentRecord.hemoglobin }}</td>
                  <td class="health-report-status" :class="getHemoglobinStatusClass(currentRecord.hemoglobin)">
                    {{ getHemoglobinStatus(currentRecord.hemoglobin) }}
                  </td>
                </tr>
                <tr v-if="currentRecord.hematocrit">
                  <td class="health-report-label">红细胞压积 (%)</td>
                  <td class="health-report-value">{{ currentRecord.hematocrit }}</td>
                  <td class="health-report-status" :class="getHematocritStatusClass(currentRecord.hematocrit)">
                    {{ getHematocritStatus(currentRecord.hematocrit) }}
                  </td>
                </tr>
                <tr v-if="currentRecord.plateletCount">
                  <td class="health-report-label">血小板计数 (×10^9/L)</td>
                  <td class="health-report-value">{{ currentRecord.plateletCount }}</td>
                  <td class="health-report-status" :class="getPlateletCountStatusClass(currentRecord.plateletCount)">
                    {{ getPlateletCountStatus(currentRecord.plateletCount) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          
          <!-- 肝功能检查 -->
          <div class="health-report-section" v-if="hasLiverFunctionItems">
            <h3 class="health-report-section-title">肝功能检查</h3>
            <table class="health-report-table">
              <tbody>
                <tr v-if="currentRecord.alt">
                  <td class="health-report-label">谷丙转氨酶 (U/L)</td>
                  <td class="health-report-value">{{ currentRecord.alt }}</td>
                  <td class="health-report-status" :class="getAltStatusClass(currentRecord.alt)">
                    {{ getAltStatus(currentRecord.alt) }}
                  </td>
                </tr>
                <tr v-if="currentRecord.ast">
                  <td class="health-report-label">谷草转氨酶 (U/L)</td>
                  <td class="health-report-value">{{ currentRecord.ast }}</td>
                  <td class="health-report-status" :class="getAstStatusClass(currentRecord.ast)">
                    {{ getAstStatus(currentRecord.ast) }}
                  </td>
                </tr>
                <tr v-if="currentRecord.ggt">
                  <td class="health-report-label">γ-谷氨酰转移酶 (U/L)</td>
                  <td class="health-report-value">{{ currentRecord.ggt }}</td>
                  <td class="health-report-status" :class="getGgtStatusClass(currentRecord.ggt)">
                    {{ getGgtStatus(currentRecord.ggt) }}
                  </td>
                </tr>
                <tr v-if="currentRecord.alp">
                  <td class="health-report-label">碱性磷酸酶 (U/L)</td>
                  <td class="health-report-value">{{ currentRecord.alp }}</td>
                  <td class="health-report-status" :class="getAlpStatusClass(currentRecord.alp)">
                    {{ getAlpStatus(currentRecord.alp) }}
                  </td>
                </tr>
                <tr v-if="currentRecord.albumin">
                  <td class="health-report-label">白蛋白 (g/L)</td>
                  <td class="health-report-value">{{ currentRecord.albumin }}</td>
                  <td class="health-report-status" :class="getAlbuminStatusClass(currentRecord.albumin)">
                    {{ getAlbuminStatus(currentRecord.albumin) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          
          <!-- 肾功能与尿酸 -->
          <div class="health-report-section" v-if="hasKidneyFunctionItems">
            <h3 class="health-report-section-title">肾功能与尿酸</h3>
            <table class="health-report-table">
              <tbody>
                <tr v-if="currentRecord.creatinine">
                  <td class="health-report-label">肌酐 (μmol/L)</td>
                  <td class="health-report-value">{{ currentRecord.creatinine }}</td>
                  <td class="health-report-status" :class="getCreatinineStatusClass(currentRecord.creatinine)">
                    {{ getCreatinineStatus(currentRecord.creatinine) }}
                  </td>
                </tr>
                <tr v-if="currentRecord.bloodUreaNitrogen">
                  <td class="health-report-label">尿素氮 (mmol/L)</td>
                  <td class="health-report-value">{{ currentRecord.bloodUreaNitrogen }}</td>
                  <td class="health-report-status" :class="getBloodUreaNitrogenStatusClass(currentRecord.bloodUreaNitrogen)">
                    {{ getBloodUreaNitrogenStatus(currentRecord.bloodUreaNitrogen) }}
                  </td>
                </tr>
                <tr v-if="currentRecord.egfr">
                  <td class="health-report-label">eGFR (mL/min/1.73m²)</td>
                  <td class="health-report-value">{{ currentRecord.egfr }}</td>
                  <td class="health-report-status" :class="getEgfrStatusClass(currentRecord.egfr)">
                    {{ getEgfrStatus(currentRecord.egfr) }}
                  </td>
                </tr>
                <tr v-if="currentRecord.uricAcid">
                  <td class="health-report-label">尿酸 (μmol/L)</td>
                  <td class="health-report-value">{{ currentRecord.uricAcid }}</td>
                  <td class="health-report-status" :class="getUricAcidStatusClass(currentRecord.uricAcid)">
                    {{ getUricAcidStatus(currentRecord.uricAcid) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          
          <!-- 血脂四项 -->
          <div class="health-report-section" v-if="hasLipidProfileItems">
            <h3 class="health-report-section-title">血脂四项</h3>
            <table class="health-report-table">
              <tbody>
                <tr v-if="currentRecord.totalCholesterol">
                  <td class="health-report-label">总胆固醇 (mmol/L)</td>
                  <td class="health-report-value">{{ currentRecord.totalCholesterol }}</td>
                  <td class="health-report-status" :class="getTotalCholesterolStatusClass(currentRecord.totalCholesterol)">
                    {{ getTotalCholesterolStatus(currentRecord.totalCholesterol) }}
                  </td>
                </tr>
                <tr v-if="currentRecord.triglycerides">
                  <td class="health-report-label">甘油三酯 (mmol/L)</td>
                  <td class="health-report-value">{{ currentRecord.triglycerides }}</td>
                  <td class="health-report-status" :class="getTriglyceridesStatusClass(currentRecord.triglycerides)">
                    {{ getTriglyceridesStatus(currentRecord.triglycerides) }}
                  </td>
                </tr>
                <tr v-if="currentRecord.ldlCholesterol">
                  <td class="health-report-label">低密度脂蛋白胆固醇 (mmol/L)</td>
                  <td class="health-report-value">{{ currentRecord.ldlCholesterol }}</td>
                  <td class="health-report-status" :class="getLdlCholesterolStatusClass(currentRecord.ldlCholesterol)">
                    {{ getLdlCholesterolStatus(currentRecord.ldlCholesterol) }}
                  </td>
                </tr>
                <tr v-if="currentRecord.hdlCholesterol">
                  <td class="health-report-label">高密度脂蛋白胆固醇 (mmol/L)</td>
                  <td class="health-report-value">{{ currentRecord.hdlCholesterol }}</td>
                  <td class="health-report-status" :class="getHdlCholesterolStatusClass(currentRecord.hdlCholesterol)">
                    {{ getHdlCholesterolStatus(currentRecord.hdlCholesterol) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          
          <!-- 备注 -->
          <div class="health-report-section" v-if="currentRecord.remark">
            <h3 class="health-report-section-title">备注</h3>
            <div class="health-report-remark">{{ currentRecord.remark }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { healthApi } from '@/api/health'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

export default {
  name: 'ElderlyHealthView',
  setup() {
    const userStore = useUserStore()
    const healthRecords = ref([])
    const loading = ref(false)
    const currentUserId = ref(null)
    const currentPage = ref(1)
    const pageSize = ref(12)
    const showDetailDialog = ref(false)
    const currentRecord = ref({})
    
    // 计算总页数
    const totalPages = computed(() => {
      return Math.ceil(healthRecords.value.length / pageSize.value)
    })
    
    // 计算可见的页码按钮
    const visiblePages = computed(() => {
      const pages = []
      const total = totalPages.value
      const current = currentPage.value
      
      if (total <= 7) {
        for (let i = 1; i <= total; i++) {
          pages.push(i)
        }
      } else {
        pages.push(1)
        
        if (current > 4) {
          pages.push('...')
        }
        
        const start = Math.max(2, current - 2)
        const end = Math.min(total - 1, current + 2)
        
        for (let i = start; i <= end; i++) {
          pages.push(i)
        }
        
        if (current < total - 3) {
          pages.push('...')
        }
        
        pages.push(total)
      }
      
      return pages
    })
    
    // 计算属性：筛选后的健康记录
    const filteredRecords = computed(() => {
      let result = healthRecords.value
      
      console.log('原始健康记录数量:', healthRecords.value.length)
      
      result.sort((a, b) => new Date(b.recordTime) - new Date(a.recordTime))
      
      const startIndex = (currentPage.value - 1) * pageSize.value
      const endIndex = startIndex + pageSize.value
      result = result.slice(startIndex, endIndex)
      
      console.log('最终筛选后健康记录数量:', result.length)
      return result
    })
    
    // 左列记录
    const leftColumnRecords = computed(() => {
      const mid = Math.ceil(filteredRecords.value.length / 2)
      return filteredRecords.value.slice(0, mid)
    })
    
    // 右列记录
    const rightColumnRecords = computed(() => {
      const mid = Math.ceil(filteredRecords.value.length / 2)
      return filteredRecords.value.slice(mid)
    })
    
    // 判断是否有基础项目
    const hasBasicItems = computed(() => {
      return currentRecord.value.bloodPressureHigh || currentRecord.value.bloodPressureLow || 
             currentRecord.value.heartRate || currentRecord.value.bloodSugar || 
             currentRecord.value.temperature || (currentRecord.value.weight && currentRecord.value.height)
    })
    
    // 判断是否有血常规项目
    const hasBloodTestItems = computed(() => {
      return currentRecord.value.whiteBloodCell || currentRecord.value.hemoglobin ||
             currentRecord.value.hematocrit || currentRecord.value.plateletCount
    })
    
    // 判断是否有肝功能项目
    const hasLiverFunctionItems = computed(() => {
      return currentRecord.value.alt || currentRecord.value.ast || 
             currentRecord.value.ggt || currentRecord.value.alp || currentRecord.value.albumin
    })
    
    // 判断是否有肾功能项目
    const hasKidneyFunctionItems = computed(() => {
      return currentRecord.value.creatinine || currentRecord.value.bloodUreaNitrogen ||
             currentRecord.value.egfr || currentRecord.value.uricAcid
    })
    
    // 判断是否有血脂项目
    const hasLipidProfileItems = computed(() => {
      return currentRecord.value.totalCholesterol || currentRecord.value.triglycerides ||
             currentRecord.value.ldlCholesterol || currentRecord.value.hdlCholesterol
    })
    
    // 格式化日期时间
    const formatDateTime = (date) => {
      if (!date) return '-'
      const d = new Date(date)
      const year = d.getFullYear()
      const month = (d.getMonth() + 1).toString().padStart(2, '0')
      const day = d.getDate().toString().padStart(2, '0')
      const hours = d.getHours().toString().padStart(2, '0')
      const minutes = d.getMinutes().toString().padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    }
    
    // 格式化日期
    const formatDate = (date) => {
      if (!date) return '-'
      const d = new Date(date)
      return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`
    }
    
    // 格式化血压
    const formatBloodPressure = (record) => {
      if (record.bloodPressureHigh && record.bloodPressureLow) {
        return `${record.bloodPressureHigh}/${record.bloodPressureLow}mmHg`
      }
      return '-'
    }
    
    // 计算BMI
    const calculateBMI = (weight, height) => {
      if (!weight || !height) return '-'
      const heightInMeters = height / 100
      const bmi = weight / (heightInMeters * heightInMeters)
      return bmi.toFixed(1)
    }
    
    // 血压状态判断
    const getBloodPressureStatus = (high, low) => {
      if (!high || !low) return '-'
      const highVal = parseInt(high)
      const lowVal = parseInt(low)
      
      if (highVal >= 160 || lowVal >= 100) {
        return '高血压2级'
      } else if (highVal >= 140 || lowVal >= 90) {
        return '高血压1级'
      } else if (highVal < 90 || lowVal < 60) {
        return '低血压'
      } else if (highVal >= 130 || lowVal >= 85) {
        return '正常高值'
      } else {
        return '理想状态'
      }
    }
    
    const getBloodPressureStatusClass = (high, low) => {
      const status = getBloodPressureStatus(high, low)
      if (status === '理想状态') return 'status-normal'
      if (status === '正常高值') return 'status-warning'
      return 'status-abnormal'
    }
    
    // 心率状态判断
    const getHeartRateStatus = (rate) => {
      if (!rate) return '-'
      const rateVal = parseInt(rate)
      if (rateVal < 60 || rateVal > 100) {
        return '异常'
      }
      return '正常'
    }
    
    const getHeartRateStatusClass = (rate) => {
      const status = getHeartRateStatus(rate)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 血糖状态判断
    const getBloodSugarStatus = (value, type) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (type === 'fasting') {
        if (val < 3.9 || val > 6.1) return '异常'
      } else if (type === 'postprandial') {
        if (val < 4.4 || val > 7.8) return '异常'
      } else {
        if (val < 3.9 || val > 6.1) return '异常'
      }
      return '正常'
    }
    
    const getBloodSugarStatusClass = (value, type) => {
      const status = getBloodSugarStatus(value, type)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 体温状态判断
    const getTemperatureStatus = (value, type) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (type === 'oral') {
        if (val < 36.3 || val > 37.2) return '异常'
      } else if (type === 'axillary') {
        if (val < 36.0 || val > 37.0) return '异常'
      } else if (type === 'rectal') {
        if (val < 36.6 || val > 37.8) return '异常'
      } else if (type === 'ear') {
        if (val < 35.7 || val > 37.7) return '异常'
      } else if (type === 'forehead') {
        if (val < 35.6 || val > 37.5) return '异常'
      } else {
        if (val < 36.0 || val > 37.0) return '异常'
      }
      return '正常'
    }
    
    const getTemperatureStatusClass = (value, type) => {
      const status = getTemperatureStatus(value, type)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // BMI状态判断
    const getBMIStatus = (weight, height) => {
      if (!weight || !height) return '-'
      const bmi = calculateBMI(weight, height)
      if (bmi === '-') return '-'
      const bmiVal = parseFloat(bmi)
      if (isNaN(bmiVal)) return '-'
      if (bmiVal < 18.5 || bmiVal > 24) {
        return '异常'
      }
      return '正常'
    }
    
    const getBMIStatusClass = (weight, height) => {
      const status = getBMIStatus(weight, height)
      if (status === '-') return ''
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 白细胞计数状态判断
    const getWhiteBloodCellStatus = (value) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (val < 3.5 || val > 9.5) return '异常'
      return '正常'
    }
    
    const getWhiteBloodCellStatusClass = (value) => {
      const status = getWhiteBloodCellStatus(value)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 血红蛋白状态判断
    const getHemoglobinStatus = (value) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (val < 110 || val > 160) return '异常'
      return '正常'
    }
    
    const getHemoglobinStatusClass = (value) => {
      const status = getHemoglobinStatus(value)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 红细胞压积状态判断
    const getHematocritStatus = (value) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (val < 35 || val > 50) return '异常'
      return '正常'
    }
    
    const getHematocritStatusClass = (value) => {
      const status = getHematocritStatus(value)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 血小板计数状态判断
    const getPlateletCountStatus = (value) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (val < 125 || val > 350) return '异常'
      return '正常'
    }
    
    const getPlateletCountStatusClass = (value) => {
      const status = getPlateletCountStatus(value)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 谷丙转氨酶状态判断
    const getAltStatus = (value) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (val < 7 || val > 40) return '异常'
      return '正常'
    }
    
    const getAltStatusClass = (value) => {
      const status = getAltStatus(value)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 谷草转氨酶状态判断
    const getAstStatus = (value) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (val < 13 || val > 35) return '异常'
      return '正常'
    }
    
    const getAstStatusClass = (value) => {
      const status = getAstStatus(value)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // γ-谷氨酰转移酶状态判断
    const getGgtStatus = (value) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (val < 10 || val > 60) return '异常'
      return '正常'
    }
    
    const getGgtStatusClass = (value) => {
      const status = getGgtStatus(value)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 碱性磷酸酶状态判断
    const getAlpStatus = (value) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (val < 45 || val > 125) return '异常'
      return '正常'
    }
    
    const getAlpStatusClass = (value) => {
      const status = getAlpStatus(value)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 白蛋白状态判断
    const getAlbuminStatus = (value) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (val < 40 || val > 55) return '异常'
      return '正常'
    }
    
    const getAlbuminStatusClass = (value) => {
      const status = getAlbuminStatus(value)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 肌酐状态判断
    const getCreatinineStatus = (value) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (val < 45 || val > 104) return '异常'
      return '正常'
    }
    
    const getCreatinineStatusClass = (value) => {
      const status = getCreatinineStatus(value)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 尿素氮状态判断
    const getBloodUreaNitrogenStatus = (value) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (val < 3.2 || val > 7.1) return '异常'
      return '正常'
    }
    
    const getBloodUreaNitrogenStatusClass = (value) => {
      const status = getBloodUreaNitrogenStatus(value)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // eGFR状态判断
    const getEgfrStatus = (value) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (val < 90) return '异常'
      return '正常'
    }
    
    const getEgfrStatusClass = (value) => {
      const status = getEgfrStatus(value)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 尿酸状态判断
    const getUricAcidStatus = (value) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (val < 155 || val > 428) return '异常'
      return '正常'
    }
    
    const getUricAcidStatusClass = (value) => {
      const status = getUricAcidStatus(value)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 总胆固醇状态判断
    const getTotalCholesterolStatus = (value) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (val >= 5.2) return '异常'
      return '正常'
    }
    
    const getTotalCholesterolStatusClass = (value) => {
      const status = getTotalCholesterolStatus(value)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 甘油三酯状态判断
    const getTriglyceridesStatus = (value) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (val >= 1.7) return '异常'
      return '正常'
    }
    
    const getTriglyceridesStatusClass = (value) => {
      const status = getTriglyceridesStatus(value)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 低密度脂蛋白胆固醇状态判断
    const getLdlCholesterolStatus = (value) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (val >= 3.4) return '异常'
      return '正常'
    }
    
    const getLdlCholesterolStatusClass = (value) => {
      const status = getLdlCholesterolStatus(value)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 高密度脂蛋白胆固醇状态判断
    const getHdlCholesterolStatus = (value) => {
      if (!value) return '-'
      const val = parseFloat(value)
      if (val < 1.0) return '异常'
      return '正常'
    }
    
    const getHdlCholesterolStatusClass = (value) => {
      const status = getHdlCholesterolStatus(value)
      return status === '正常' ? 'status-normal' : 'status-abnormal'
    }
    
    // 查看详情
    const viewDetail = (record) => {
      currentRecord.value = record
      showDetailDialog.value = true
    }
    
    // 关闭详情弹窗
    const closeDetailDialog = () => {
      showDetailDialog.value = false
      currentRecord.value = {}
    }
    
    // 获取当前用户ID
    const getCurrentUserId = () => {
      try {
        const userInfo = userStore.userInfo
        if (userInfo && userInfo.id) {
          console.log('从用户存储获取用户ID:', userInfo.id)
          return userInfo.id
        }
        
        const storeUserId = userStore.userId
        if (storeUserId && storeUserId !== 1) {
          console.log('从userStore.userId获取用户ID:', storeUserId)
          return storeUserId
        }
        
        const token = localStorage.getItem('token')
        if (token) {
          try {
            const payload = JSON.parse(atob(token.split('.')[1]))
            const userId = payload.userId || payload.sub
            if (userId) {
              console.log('从token获取用户ID:', userId)
              return userId
            }
          } catch (e) {
            console.error('解析token失败:', e)
          }
        }
        
        console.log('使用默认用户ID: 1')
        return 1
      } catch (e) {
        console.error('获取用户ID失败:', e)
        return 1
      }
    }
    
    // 获取健康记录数据
    const fetchHealthRecords = async () => {
      try {
        loading.value = true
        
        const userId = getCurrentUserId()
        console.log('准备获取健康记录，用户ID:', userId)
        
        currentUserId.value = userId
        
        const response = await healthApi.getUserHealthRecords(userId, {})
        
        console.log('健康记录API响应:', response)
        
        if (response.code === 200) {
          const records = Array.isArray(response.data) ? response.data : []
          healthRecords.value = records
          console.log('健康记录数据:', records)
          
          if (records.length === 0) {
            ElMessage.info('暂无健康记录')
          }
        } else {
          console.error('获取健康记录失败，响应码:', response.code, '消息:', response.message)
          ElMessage.error('获取健康记录失败: ' + (response.message || '未知错误'))
          healthRecords.value = []
        }
      } catch (error) {
        console.error('获取健康记录失败:', error)
        ElMessage.error('获取健康记录失败: ' + (error.message || '未知错误'))
        healthRecords.value = []
      } finally {
        loading.value = false
      }
    }
    
    // 分页相关函数
    const handlePageChange = (page) => {
      if (page < 1 || page > totalPages.value) return
      currentPage.value = page
    }
    
    const goToFirstPage = () => {
      currentPage.value = 1
    }
    
    const goToLastPage = () => {
      currentPage.value = totalPages.value
    }
    
    const goToPrevPage = () => {
      if (currentPage.value > 1) {
        currentPage.value--
      }
    }
    
    const goToNextPage = () => {
      if (currentPage.value < totalPages.value) {
        currentPage.value++
      }
    }
    
    // 生命周期钩子
    onMounted(() => {
      fetchHealthRecords()
    })
    
    return {
      healthRecords,
      filteredRecords,
      leftColumnRecords,
      rightColumnRecords,
      currentPage,
      pageSize,
      totalPages,
      visiblePages,
      loading,
      currentUserId,
      showDetailDialog,
      currentRecord,
      hasBasicItems,
      hasBloodTestItems,
      hasLiverFunctionItems,
      hasKidneyFunctionItems,
      hasLipidProfileItems,
      formatDateTime,
      formatDate,
      formatBloodPressure,
      calculateBMI,
      getBloodPressureStatus,
      getBloodPressureStatusClass,
      getHeartRateStatus,
      getHeartRateStatusClass,
      getBloodSugarStatus,
      getBloodSugarStatusClass,
      getTemperatureStatus,
      getTemperatureStatusClass,
      getBMIStatus,
      getBMIStatusClass,
      getWhiteBloodCellStatus,
      getWhiteBloodCellStatusClass,
      getHemoglobinStatus,
      getHemoglobinStatusClass,
      getHematocritStatus,
      getHematocritStatusClass,
      getPlateletCountStatus,
      getPlateletCountStatusClass,
      getAltStatus,
      getAltStatusClass,
      getAstStatus,
      getAstStatusClass,
      getGgtStatus,
      getGgtStatusClass,
      getAlpStatus,
      getAlpStatusClass,
      getAlbuminStatus,
      getAlbuminStatusClass,
      getCreatinineStatus,
      getCreatinineStatusClass,
      getBloodUreaNitrogenStatus,
      getBloodUreaNitrogenStatusClass,
      getEgfrStatus,
      getEgfrStatusClass,
      getUricAcidStatus,
      getUricAcidStatusClass,
      getTotalCholesterolStatus,
      getTotalCholesterolStatusClass,
      getTriglyceridesStatus,
      getTriglyceridesStatusClass,
      getLdlCholesterolStatus,
      getLdlCholesterolStatusClass,
      getHdlCholesterolStatus,
      getHdlCholesterolStatusClass,
      viewDetail,
      closeDetailDialog,
      handlePageChange,
      goToFirstPage,
      goToLastPage,
      goToPrevPage,
      goToNextPage
    }
  }
}
</script>

<style scoped>
.elderly-health {
  max-width: 1200px;
  margin: 0 auto;
}

.page-description {
  font-size: var(--elderly-font-size-lg);
  color: var(--elderly-text-secondary);
  line-height: var(--elderly-line-height-lg);
}

/* 健康记录两列布局样式 */
.health-records-table-container {
  overflow-x: auto;
}

.health-records-grid {
  display: flex;
  gap: var(--elderly-space-lg);
}

.health-records-column {
  flex: 1;
  min-width: 0;
}

.health-records-column-header {
  display: flex;
  background-color: var(--elderly-primary-color);
  color: white;
  border-radius: 4px 4px 0 0;
  overflow: hidden;
}

.column-header-title {
  flex: 1;
  padding: 12px 16px;
  font-weight: 600;
  font-size: 14px;
  text-align: left;
  border-right: 1px solid rgba(255, 255, 255, 0.3);
}

.column-header-title:last-child {
  border-right: none;
  text-align: center;
  flex: 0 0 120px;
}

.health-records-column-body {
  background-color: white;
  border: 1px solid var(--elderly-border-light);
  border-top: none;
  border-radius: 0 0 4px 4px;
}

.record-row {
  display: flex;
  border-bottom: 1px solid var(--elderly-border-light);
  transition: background-color 0.2s;
}

.record-row:last-child {
  border-bottom: none;
}

.record-row:hover {
  background-color: var(--elderly-bg-light);
}

.record-date {
  flex: 1;
  padding: 12px 16px;
  color: var(--elderly-text-primary);
  font-weight: 500;
  font-size: 14px;
  border-right: 1px solid var(--elderly-border-light);
}

.record-action {
  flex: 0 0 120px;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.view-detail-btn {
  padding: 6px 16px;
  background-color: var(--elderly-primary-color);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.2s;
}

.view-detail-btn:hover {
  background-color: #1a73e8;
  color: white;
}

/* 健康报告弹窗样式 */
.health-report-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.health-report-modal {
  background-color: white;
  border-radius: 8px;
  width: 800px;
  max-width: 95%;
  max-height: 90vh;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
}

.health-report-header {
  background: #ffffff;
  color: #333;
  padding: 24px 32px;
  border-radius: 8px 8px 0 0;
  border-bottom: 2px solid var(--elderly-primary-color);
  position: relative;
  flex-shrink: 0;
}

.health-report-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: var(--elderly-primary-color);
}

.health-report-meta {
  font-size: 14px;
  color: #666;
  text-align: center;
  padding: 8px 0;
}

.health-report-close {
  position: absolute;
  top: 16px;
  right: 16px;
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #999;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.health-report-close:hover {
  background-color: #f5f5f5;
  color: #333;
}

.health-report-body {
  padding: 24px 32px;
  overflow-y: auto;
  flex: 1;
}

.health-report-section {
  margin-bottom: 24px;
}

.health-report-section:last-child {
  margin-bottom: 0;
}

.health-report-section-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--elderly-primary-color);
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e0e0e0;
}

.health-report-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 16px;
}

.health-report-table td {
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  font-size: 14px;
}

.health-report-label {
  font-weight: 600;
  color: #333;
  min-width: 200px;
}

.health-report-value {
  color: #333;
  font-weight: 500;
}

.health-report-status {
  text-align: center;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 4px;
  min-width: 60px;
}

.health-report-status.status-normal {
  background-color: #f6ffed;
  color: #52c41a;
}

.health-report-status.status-warning {
  background-color: #fffbe6;
  color: #faad14;
}

.health-report-status.status-abnormal {
  background-color: #fff2f0;
  color: #ff4d4f;
}

.health-report-remark {
  background-color: #f9f9f9;
  padding: 12px 16px;
  border-radius: 4px;
  color: #333;
  line-height: 1.6;
}

.empty-state {
  text-align: center;
  padding: var(--elderly-space-xl) 0;
  color: var(--elderly-text-tertiary);
  font-size: var(--elderly-font-size-lg);
}

.empty-hint {
  font-size: var(--elderly-font-size-md);
  color: var(--elderly-text-secondary);
  margin-top: var(--elderly-space-md);
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--elderly-space-xl) 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: var(--elderly-primary-color);
  animation: spin 1s ease-in-out infinite;
  margin-bottom: var(--elderly-space-md);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 分页器样式 */
.pagination-container {
  display: flex;
  flex-direction: column;
  gap: var(--elderly-space-md);
  align-items: center;
  margin-top: var(--elderly-space-xl);
  padding: var(--elderly-space-lg) 0;
  border-top: 1px solid var(--elderly-border-light);
}

.pagination-info {
  display: flex;
  justify-content: center;
  gap: var(--elderly-space-lg);
  color: var(--elderly-text-secondary);
  font-size: var(--elderly-font-size-md);
  margin-bottom: var(--elderly-space-md);
}

.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--elderly-space-sm);
  flex-wrap: wrap;
}

.pagination-pages {
  display: flex;
  gap: var(--elderly-space-xs);
  align-items: center;
}

.pagination-btn {
  padding: var(--elderly-space-sm) var(--elderly-space-md);
  border: 1px solid var(--elderly-border-color);
  background-color: var(--elderly-bg-white);
  color: var(--elderly-text-primary);
  border-radius: var(--elderly-border-radius-md);
  cursor: pointer;
  transition: all var(--elderly-transition-base);
  font-size: var(--elderly-font-size-md);
  font-weight: 500;
  min-width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.pagination-btn:hover:not(:disabled) {
  background-color: var(--elderly-primary-color);
  color: white;
  border-color: var(--elderly-primary-color);
}

.pagination-btn:disabled {
  background-color: var(--elderly-bg-light);
  color: var(--elderly-text-disabled);
  border-color: var(--elderly-border-light);
  cursor: not-allowed;
}

.pagination-btn.active {
  background-color: var(--elderly-primary-color);
  color: white;
  border-color: var(--elderly-primary-color);
  font-weight: 600;
}

.pagination-number {
  min-width: 40px;
}

@media (max-width: 768px) {
  .health-records-grid {
    flex-direction: column;
    gap: var(--elderly-space-md);
  }
  
  .column-header-title {
    padding: 10px 12px;
    font-size: 13px;
  }
  
  .record-date {
    padding: 10px 12px;
    font-size: 13px;
  }
  
  .record-action {
    flex: 0 0 100px;
  }
  
  .view-detail-btn {
    padding: 5px 12px;
    font-size: 13px;
  }
  
  .health-report-modal {
    width: 95%;
    max-height: 80vh;
  }
  
  .health-report-header {
    padding: 16px 20px;
  }
  
  .health-report-body {
    padding: 16px 20px;
  }
  
  .pagination-container {
    padding: var(--elderly-space-md) 0;
  }
  
  .pagination-controls {
    gap: var(--elderly-space-sm);
  }
  
  .pagination-btn {
    min-width: 36px;
    height: 36px;
    font-size: var(--elderly-font-size-sm);
  }
}
</style>
