<template>
  <div class="system-page">
    <div class="system-page-title">
      <div class="system-page-title-text">
        <span class="system-page-title-icon">❤️</span>
        健康监测管理
      </div>
      <div class="system-page-title-actions">
        <el-button type="primary" @click="showAddHealthRecordDialog = true">
          <span class="system-button-icon">+</span>
          新增健康记录
        </el-button>
      </div>
    </div>

    <!-- 健康数据统计卡片 -->
    <div class="system-stats-container">
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">记录总数</div>
          <div class="system-stats-icon">
            <el-icon><Document /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.totalCount || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">正常状态</div>
          <div class="system-stats-icon">
            <el-icon><Check /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.normalCount || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">异常状态</div>
          <div class="system-stats-icon">
            <el-icon><Warning /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.abnormalCount || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">本月新增</div>
          <div class="system-stats-icon">
            <el-icon><Calendar /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.monthlyCount || 0 }}</div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="system-search-bar">
      <div class="system-search-form">
        <div class="system-search-item">
          <div class="system-form-label">用户名称</div>
          <input 
            class="system-form-input" 
            type="text" 
            v-model="searchForm.userName" 
            placeholder="请输入用户名称"
          >
        </div>
        <div class="system-search-item">
          <div class="system-form-label">健康状态</div>
          <select class="system-form-input" v-model="searchForm.healthStatus">
            <option value="">全部状态</option>
            <option value="0">正常</option>
            <option value="1">异常</option>
          </select>
        </div>
        <div class="system-search-item">
          <div class="system-form-label">记录日期</div>
          <input class="system-form-input" type="date" v-model="searchForm.recordTime" placeholder="请选择记录日期">
        </div>
        <div class="system-search-actions">
          <el-button type="primary" @click="searchHealthRecords">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 健康记录列表 -->
    <div class="system-table-container">
      <table class="system-table">
        <thead>
          <tr>
            <th>用户姓名</th>
            <th>记录日期</th>
            <th>基础项目</th>
            <th>血常规</th>
            <th>肝功能</th>
            <th>肾功能</th>
            <th>血脂</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="record in healthRecords" :key="record.id">
            <td>{{ getUserName(record.userId) }}</td>
            <td>{{ formatDate(record.recordTime) }}</td>
            <td>
              <el-popover placement="right" width="300" trigger="hover">
                <template #reference>
                  <span :class="getLabTestStatusClass(record, 'basic')">{{ getLabTestSummary(record, 'basic') }}</span>
                </template>
                <div class="lab-test-detail">
                  <div class="lab-test-item">
                    <span class="lab-test-label">血压:</span>
                    <span :class="getBloodPressureValueClass(record.bloodPressure)">{{ record.bloodPressure || '-' }}</span>
                    <span class="lab-test-unit">mmHg</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">心率:</span>
                    <span :class="getLabTestValueClass(record.heartRate, 60, 100)">{{ record.heartRate || '-' }}</span>
                    <span class="lab-test-unit">次/分</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">血糖:</span>
                    <span :class="getLabTestValueClass(record.bloodSugar, 3.9, 6.1)">{{ record.bloodSugar || '-' }}</span>
                    <span class="lab-test-unit">mmol/L</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">身高:</span>
                    <span>{{ record.height || '-' }}</span>
                    <span class="lab-test-unit">cm</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">体重:</span>
                    <span>{{ record.weight || '-' }}</span>
                    <span class="lab-test-unit">kg</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">BMI:</span>
                    <span :class="getBMIValueClass(record.weight, record.height)">{{ calculateBMI(record.weight, record.height) || '-' }}</span>
                    <span class="lab-test-unit">{{ getBMIStatusText(record.weight, record.height) }}</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">体温:</span>
                    <span :class="getLabTestValueClass(record.temperature, 36.0, 37.3)">{{ record.temperature || '-' }}</span>
                    <span class="lab-test-unit">°C</span>
                  </div>
                </div>
              </el-popover>
            </td>
            <td>
              <el-popover placement="right" width="300" trigger="hover">
                <template #reference>
                  <span :class="getLabTestStatusClass(record, 'blood')">{{ getLabTestSummary(record, 'blood') }}</span>
                </template>
                <div class="lab-test-detail">
                  <div class="lab-test-item">
                    <span class="lab-test-label">白细胞:</span>
                    <span :class="getLabTestValueClass(record.whiteBloodCell, 3.5, 9.5)">{{ record.whiteBloodCell || '-' }}</span>
                    <span class="lab-test-unit">×10^9/L</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">血红蛋白:</span>
                    <span :class="getLabTestValueClass(record.hemoglobin, 110, 160)">{{ record.hemoglobin || '-' }}</span>
                    <span class="lab-test-unit">g/L</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">红细胞压积:</span>
                    <span :class="getLabTestValueClass(record.hematocrit, 35, 50)">{{ record.hematocrit || '-' }}</span>
                    <span class="lab-test-unit">%</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">血小板:</span>
                    <span :class="getLabTestValueClass(record.plateletCount, 125, 350)">{{ record.plateletCount || '-' }}</span>
                    <span class="lab-test-unit">×10^9/L</span>
                  </div>
                </div>
              </el-popover>
            </td>
            <td>
              <el-popover placement="right" width="300" trigger="hover">
                <template #reference>
                  <span :class="getLabTestStatusClass(record, 'liver')">{{ getLabTestSummary(record, 'liver') }}</span>
                </template>
                <div class="lab-test-detail">
                  <div class="lab-test-item">
                    <span class="lab-test-label">谷丙转氨酶:</span>
                    <span :class="getLabTestValueClass(record.alt, 7, 40)">{{ record.alt || '-' }}</span>
                    <span class="lab-test-unit">U/L</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">谷草转氨酶:</span>
                    <span :class="getLabTestValueClass(record.ast, 13, 35)">{{ record.ast || '-' }}</span>
                    <span class="lab-test-unit">U/L</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">γ-谷氨酰转移酶:</span>
                    <span :class="getLabTestValueClass(record.ggt, 7, 60)">{{ record.ggt || '-' }}</span>
                    <span class="lab-test-unit">U/L</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">碱性磷酸酶:</span>
                    <span :class="getLabTestValueClass(record.alp, 45, 125)">{{ record.alp || '-' }}</span>
                    <span class="lab-test-unit">U/L</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">白蛋白:</span>
                    <span :class="getLabTestValueClass(record.albumin, 40, 55)">{{ record.albumin || '-' }}</span>
                    <span class="lab-test-unit">g/L</span>
                  </div>
                </div>
              </el-popover>
            </td>
            <td>
              <el-popover placement="right" width="300" trigger="hover">
                <template #reference>
                  <span :class="getLabTestStatusClass(record, 'kidney')">{{ getLabTestSummary(record, 'kidney') }}</span>
                </template>
                <div class="lab-test-detail">
                  <div class="lab-test-item">
                    <span class="lab-test-label">肌酐:</span>
                    <span :class="getLabTestValueClass(record.creatinine, 45, 104)">{{ record.creatinine || '-' }}</span>
                    <span class="lab-test-unit">μmol/L</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">尿素氮:</span>
                    <span :class="getLabTestValueClass(record.bloodUreaNitrogen, 3.2, 7.1)">{{ record.bloodUreaNitrogen || '-' }}</span>
                    <span class="lab-test-unit">mmol/L</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">eGFR:</span>
                    <span :class="getLabTestValueClass(record.egfr, 90, 200, true)">{{ record.egfr || '-' }}</span>
                    <span class="lab-test-unit">mL/min/1.73m²</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">尿酸:</span>
                    <span :class="getLabTestValueClass(record.uricAcid, 155, 428)">{{ record.uricAcid || '-' }}</span>
                    <span class="lab-test-unit">μmol/L</span>
                  </div>
                </div>
              </el-popover>
            </td>
            <td>
              <el-popover placement="right" width="300" trigger="hover">
                <template #reference>
                  <span :class="getLabTestStatusClass(record, 'lipid')">{{ getLabTestSummary(record, 'lipid') }}</span>
                </template>
                <div class="lab-test-detail">
                  <div class="lab-test-item">
                    <span class="lab-test-label">总胆固醇:</span>
                    <span :class="getLabTestValueClass(record.totalCholesterol, 0, 5.2)">{{ record.totalCholesterol || '-' }}</span>
                    <span class="lab-test-unit">mmol/L</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">甘油三酯:</span>
                    <span :class="getLabTestValueClass(record.triglycerides, 0, 1.7)">{{ record.triglycerides || '-' }}</span>
                    <span class="lab-test-unit">mmol/L</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">低密度脂蛋白:</span>
                    <span :class="getLabTestValueClass(record.ldlCholesterol, 0, 3.4)">{{ record.ldlCholesterol || '-' }}</span>
                    <span class="lab-test-unit">mmol/L</span>
                  </div>
                  <div class="lab-test-item">
                    <span class="lab-test-label">高密度脂蛋白:</span>
                    <span :class="getLabTestValueClass(record.hdlCholesterol, 1.0, 3.0, true)">{{ record.hdlCholesterol || '-' }}</span>
                    <span class="lab-test-unit">mmol/L</span>
                  </div>
                </div>
              </el-popover>
            </td>
            <td>
              <div class="action-btn-group">
                <el-button size="small" type="info" @click="viewHealthRecord(record.id)">查看</el-button>
                <el-button size="small" type="primary" @click="editHealthRecord(record.id)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteHealthRecord(record.id)">删除</el-button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="system-pagination" :class="{ 'system-pagination-loading': loading }">
        <div class="system-pagination-info">
          <span>共 {{ total }} 条记录，第 {{ currentPage }} / {{ totalPages }} 页</span>
          <div class="system-pagination-size-selector">
            <span>每页显示</span>
            <el-select 
              v-model="pageSize" 
              size="small" 
              placeholder="请选择"
              style="width: 80px; margin: 0 8px;"
              @change="handlePageSizeChange"
            >
              <el-option label="10" :value="10"></el-option>
              <el-option label="20" :value="20"></el-option>
              <el-option label="50" :value="50"></el-option>
              <el-option label="100" :value="100"></el-option>
            </el-select>
            <span>条</span>
          </div>
        </div>
        <div class="system-pagination-controls">
          <button 
            class="system-pagination-btn"
            :class="{ 'system-pagination-btn-disabled': currentPage <= 1 }"
            :disabled="currentPage <= 1"
            @click="handlePageChange(1)"
            title="首页"
          >
            首页
          </button>
          <button 
            class="system-pagination-btn"
            :class="{ 'system-pagination-btn-disabled': currentPage <= 1 }"
            :disabled="currentPage <= 1"
            @click="handlePageChange(currentPage - 1)"
            title="上一页"
          >
            上一页
          </button>
          
          <template v-for="(page, index) in getPageNumbers()" :key="index">
            <span 
              v-if="page === '...'" 
              class="system-pagination-ellipsis"
            >...</span>
            <button 
              v-else
              class="system-pagination-btn"
              :class="{ 'system-pagination-btn-active': page === currentPage }"
              @click="handlePageChange(page)"
            >
              {{ page }}
            </button>
          </template>
          
          <button 
            class="system-pagination-btn"
            :class="{ 'system-pagination-btn-disabled': currentPage >= totalPages }"
            :disabled="currentPage >= totalPages"
            @click="handlePageChange(currentPage + 1)"
            title="下一页"
          >
            下一页
          </button>
          <button 
            class="system-pagination-btn"
            :class="{ 'system-pagination-btn-disabled': currentPage >= totalPages }"
            :disabled="currentPage >= totalPages"
            @click="handlePageChange(totalPages)"
            title="末页"
          >
            末页
          </button>
          
          <div class="system-pagination-jump">
            <span>跳转到</span>
            <input 
              type="number" 
              v-model="jumpPage" 
              @keyup.enter="handleJumpToPage"
              :min="1" 
              :max="totalPages"
            >
            <span>页</span>
            <button 
              class="system-pagination-btn"
              @click="handleJumpToPage"
            >
              确定
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增健康记录对话框 -->
    <div v-if="showAddHealthRecordDialog" class="system-modal-overlay" @click.self="closeHealthRecordDialog">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">新增健康记录</div>
          <button class="system-modal-close" @click="closeHealthRecordDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">选择用户</div>
              <select class="system-form-input" v-model="healthRecordForm.userId">
                <option value="">请选择用户</option>
                <option v-for="user in users" :key="user.id" :value="user.id">
                  {{ user.name }}
                </option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">记录日期</div>
              <input class="system-form-input" type="date" v-model="healthRecordForm.recordTime">
            </div>
            
            <div class="form-section-title">基础项目</div>
            <div class="form-section">
              <div class="system-form-item full-width">
                <div class="system-form-label">血压（收缩压/舒张压 mmHg）</div>
                <div class="blood-pressure-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    v-model="healthRecordForm.bloodPressureHigh" 
                    placeholder="收缩压"
                    @input="evaluateCurrentHealthStatus"
                  >
                  <span class="blood-pressure-separator">/</span>
                  <input 
                    class="system-form-input" 
                    type="number" 
                    v-model="healthRecordForm.bloodPressureLow" 
                    placeholder="舒张压"
                    @input="evaluateCurrentHealthStatus"
                  >
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.bloodPressureHigh && healthRecordForm.bloodPressureLow">
                  {{ getBloodPressureStatus(healthRecordForm.bloodPressureHigh, healthRecordForm.bloodPressureLow) }}
                </div>
              </div>
              <div class="system-form-item full-width">
                <div class="system-form-label">心率（次/分钟）</div>
                <div class="heart-rate-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    v-model="healthRecordForm.heartRate" 
                    placeholder="请输入心率"
                    @input="evaluateCurrentHealthStatus"
                  >
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.heartRate">
                  {{ getHeartRateStatus(healthRecordForm.heartRate) }}
                </div>
              </div>
              <div class="system-form-item full-width">
                <div class="system-form-label">血糖测量类型 / 血糖（mmol/L）</div>
                <div class="blood-sugar-input">
                  <select class="system-form-input" v-model="healthRecordForm.bloodSugarType" @change="evaluateCurrentHealthStatus">
                    <option value="">请选择测量类型</option>
                    <option value="fasting">空腹血糖</option>
                    <option value="postprandial">餐后2小时血糖</option>
                  </select>
                  <span class="blood-sugar-separator">/</span>
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.bloodSugar" 
                    placeholder="血糖值"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.bloodSugar && healthRecordForm.bloodSugarType">
                  {{ getBloodSugarStatus(healthRecordForm.bloodSugar, healthRecordForm.bloodSugarType) }}
                </div>
              </div>
              <div class="system-form-item full-width">
                <div class="system-form-label">体温测量部位 / 体温 (°C)</div>
                <div class="temperature-type-input">
                  <select class="system-form-input" v-model="healthRecordForm.temperatureType" @change="evaluateCurrentHealthStatus">
                    <option value="">请选择测量部位</option>
                    <option value="oral">口腔</option>
                    <option value="axillary">腋下</option>
                    <option value="rectal">直肠</option>
                    <option value="ear">耳温</option>
                    <option value="forehead">额温</option>
                  </select>
                  <span class="temperature-type-separator">/</span>
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.temperature" 
                    placeholder="体温"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.temperature && healthRecordForm.temperatureType">
                  {{ getTemperatureStatus(healthRecordForm.temperature, healthRecordForm.temperatureType) }}
                </div>
              </div>
              <div class="system-form-item full-width">
                <div class="system-form-label">身高（cm） / 体重（kg）</div>
                <div class="height-weight-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.height" 
                    placeholder="身高"
                    @input="evaluateCurrentHealthStatus"
                  >
                  <span class="height-weight-separator">/</span>
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.weight" 
                    placeholder="体重"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.weight && healthRecordForm.height">
                  BMI: {{ calculateBMI(healthRecordForm.weight, healthRecordForm.height) }} - {{ getBMIStatus(calculateBMI(healthRecordForm.weight, healthRecordForm.height)) }}
                </div>
              </div>
            </div>
            
            <div class="form-section-title">血常规检查</div>
            <div class="form-section">
              <div class="system-form-item">
                <div class="system-form-label">白细胞计数 (×10^9/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.whiteBloodCell" 
                    placeholder="参考范围: 3.5-9.5"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.whiteBloodCell">
                  {{ getWhiteBloodCellStatus(healthRecordForm.whiteBloodCell) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">血红蛋白 (g/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.hemoglobin" 
                    placeholder="男:120-160 女:110-150"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.hemoglobin">
                  {{ getHemoglobinStatus(healthRecordForm.hemoglobin) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">红细胞压积 (%)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.hematocrit" 
                    placeholder="参考范围: 35-50"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.hematocrit">
                  {{ getHematocritStatus(healthRecordForm.hematocrit) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">血小板计数 (×10^9/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="1" 
                    v-model="healthRecordForm.plateletCount" 
                    placeholder="参考范围: 125-350"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.plateletCount">
                  {{ getPlateletCountStatus(healthRecordForm.plateletCount) }}
                </div>
              </div>
            </div>

            <div class="form-section-title">肝功能检查</div>
            <div class="form-section">
              <div class="system-form-item">
                <div class="system-form-label">谷丙转氨酶 (U/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.alt" 
                    placeholder="参考范围: 7-40"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.alt">
                  {{ getAltStatus(healthRecordForm.alt) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">谷草转氨酶 (U/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.ast" 
                    placeholder="参考范围: 13-35"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.ast">
                  {{ getAstStatus(healthRecordForm.ast) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">γ-谷氨酰转移酶 (U/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.ggt" 
                    placeholder="男:10-60 女:7-45"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.ggt">
                  {{ getGgtStatus(healthRecordForm.ggt) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">碱性磷酸酶 (U/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.alp" 
                    placeholder="参考范围: 45-125"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.alp">
                  {{ getAlpStatus(healthRecordForm.alp) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">白蛋白 (g/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.albumin" 
                    placeholder="参考范围: 40-55"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.albumin">
                  {{ getAlbuminStatus(healthRecordForm.albumin) }}
                </div>
              </div>
            </div>

            <div class="form-section-title">肾功能与尿酸</div>
            <div class="form-section">
              <div class="system-form-item">
                <div class="system-form-label">肌酐 (μmol/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.creatinine" 
                    placeholder="男:59-104 女:45-84"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.creatinine">
                  {{ getCreatinineStatus(healthRecordForm.creatinine) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">尿素氮 (mmol/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.bloodUreaNitrogen" 
                    placeholder="参考范围: 3.2-7.1"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.bloodUreaNitrogen">
                  {{ getBloodUreaNitrogenStatus(healthRecordForm.bloodUreaNitrogen) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">eGFR (mL/min/1.73m²)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.egfr" 
                    placeholder="参考范围: >90"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.egfr">
                  {{ getEgfrStatus(healthRecordForm.egfr) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">尿酸 (μmol/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.uricAcid" 
                    placeholder="男:208-428 女:155-357"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.uricAcid">
                  {{ getUricAcidStatus(healthRecordForm.uricAcid) }}
                </div>
              </div>
            </div>

            <div class="form-section-title">血脂四项</div>
            <div class="form-section">
              <div class="system-form-item">
                <div class="system-form-label">总胆固醇 (mmol/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.totalCholesterol" 
                    placeholder="参考范围: <5.2"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.totalCholesterol">
                  {{ getTotalCholesterolStatus(healthRecordForm.totalCholesterol) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">甘油三酯 (mmol/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.triglycerides" 
                    placeholder="参考范围: <1.7"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.triglycerides">
                  {{ getTriglyceridesStatus(healthRecordForm.triglycerides) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">低密度脂蛋白胆固醇 (mmol/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.ldlCholesterol" 
                    placeholder="参考范围: <3.4"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.ldlCholesterol">
                  {{ getLdlCholesterolStatus(healthRecordForm.ldlCholesterol) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">高密度脂蛋白胆固醇 (mmol/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.hdlCholesterol" 
                    placeholder="男:>1.0 女:>1.3"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.hdlCholesterol">
                  {{ getHdlCholesterolStatus(healthRecordForm.hdlCholesterol) }}
                </div>
              </div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">健康状态</div>
              <select class="system-form-input" v-model="healthRecordForm.healthStatus">
                <option value="0">正常</option>
                <option value="1">异常</option>
              </select>
              <div class="health-status-hint" v-if="currentHealthEvaluation.reasons && currentHealthEvaluation.reasons.length > 0">
                系统建议: {{ currentHealthEvaluation.reasons.join(', ') }}
              </div>
            </div>
            <div class="system-form-item">
              <div class="system-form-label remark-label">
                <span>备注</span>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="addAllAbnormalItemsToRemark"
                  class="add-all-abnormal-btn"
                >
                  <el-icon><Plus /></el-icon>
                  添加异常指标
                </el-button>
              </div>
              <textarea class="system-form-input" rows="3" v-model="healthRecordForm.remark" placeholder="请输入备注信息"></textarea>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <el-button @click="closeHealthRecordDialog">取消</el-button>
          <el-button type="primary" @click="saveHealthRecord">保存</el-button>
        </div>
      </div>
    </div>

    <!-- 编辑健康记录对话框 -->
    <div v-if="showEditHealthRecordDialog" class="system-modal-overlay" @click.self="closeHealthRecordDialog">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">编辑健康记录</div>
          <button class="system-modal-close" @click="closeHealthRecordDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">选择用户</div>
              <select class="system-form-input" v-model="healthRecordForm.userId">
                <option value="">请选择用户</option>
                <option v-for="user in users" :key="user.id" :value="user.id">
                  {{ user.name }}
                </option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">记录日期</div>
              <input class="system-form-input" type="date" v-model="healthRecordForm.recordTime">
            </div>
            
            <div class="form-section-title">基础项目</div>
            <div class="form-section">
              <div class="system-form-item full-width">
                <div class="system-form-label">血压（收缩压/舒张压 mmHg）</div>
                <div class="blood-pressure-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    v-model="healthRecordForm.bloodPressureHigh" 
                    placeholder="收缩压"
                    @input="evaluateCurrentHealthStatus"
                  >
                  <span class="blood-pressure-separator">/</span>
                  <input 
                    class="system-form-input" 
                    type="number" 
                    v-model="healthRecordForm.bloodPressureLow" 
                    placeholder="舒张压"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.bloodPressureHigh && healthRecordForm.bloodPressureLow">
                  {{ getBloodPressureStatus(healthRecordForm.bloodPressureHigh, healthRecordForm.bloodPressureLow) }}
                </div>
              </div>
              <div class="system-form-item full-width">
                <div class="system-form-label">心率（次/分钟）</div>
                <div class="heart-rate-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    v-model="healthRecordForm.heartRate" 
                    placeholder="请输入心率"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.heartRate">
                  {{ getHeartRateStatus(healthRecordForm.heartRate) }}
                </div>
              </div>
              <div class="system-form-item full-width">
                <div class="system-form-label">血糖测量类型 / 血糖（mmol/L）</div>
                <div class="blood-sugar-input">
                  <select class="system-form-input" v-model="healthRecordForm.bloodSugarType" @change="evaluateCurrentHealthStatus">
                    <option value="">请选择测量类型</option>
                    <option value="fasting">空腹血糖</option>
                    <option value="postprandial">餐后2小时血糖</option>
                  </select>
                  <span class="blood-sugar-separator">/</span>
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.bloodSugar" 
                    placeholder="血糖值"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.bloodSugar && healthRecordForm.bloodSugarType">
                  {{ getBloodSugarStatus(healthRecordForm.bloodSugar, healthRecordForm.bloodSugarType) }}
                </div>
              </div>
              <div class="system-form-item full-width">
                <div class="system-form-label">体温测量部位 / 体温 (°C)</div>
                <div class="temperature-type-input">
                  <select class="system-form-input" v-model="healthRecordForm.temperatureType" @change="evaluateCurrentHealthStatus">
                    <option value="">请选择测量部位</option>
                    <option value="oral">口腔</option>
                    <option value="axillary">腋下</option>
                    <option value="rectal">直肠</option>
                    <option value="ear">耳温</option>
                    <option value="forehead">额温</option>
                  </select>
                  <span class="temperature-type-separator">/</span>
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.temperature" 
                    placeholder="体温"
                    @input="evaluateCurrentHealthStatus"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.temperature && healthRecordForm.temperatureType">
                  {{ getTemperatureStatus(healthRecordForm.temperature, healthRecordForm.temperatureType) }}
                </div>
              </div>
              <div class="system-form-item full-width">
                <div class="system-form-label">身高（cm） / 体重（kg）</div>
                <div class="height-weight-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.height" 
                    placeholder="身高"
                  >
                  <span class="height-weight-separator">/</span>
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.weight" 
                    placeholder="体重"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.weight && healthRecordForm.height">
                  BMI: {{ calculateBMI(healthRecordForm.weight, healthRecordForm.height) }} - {{ getBMIStatus(calculateBMI(healthRecordForm.weight, healthRecordForm.height)) }}
                </div>
              </div>
            </div>
            
            <div class="form-section-title">血常规检查</div>
            <div class="form-section">
              <div class="system-form-item">
                <div class="system-form-label">白细胞计数 (×10^9/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.whiteBloodCell" 
                    placeholder="参考范围: 3.5-9.5"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.whiteBloodCell">
                  {{ getWhiteBloodCellStatus(healthRecordForm.whiteBloodCell) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">血红蛋白 (g/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.hemoglobin" 
                    placeholder="男:120-160 女:110-150"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.hemoglobin">
                  {{ getHemoglobinStatus(healthRecordForm.hemoglobin) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">红细胞压积 (%)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.hematocrit" 
                    placeholder="参考范围: 35-50"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.hematocrit">
                  {{ getHematocritStatus(healthRecordForm.hematocrit) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">血小板计数 (×10^9/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="1" 
                    v-model="healthRecordForm.plateletCount" 
                    placeholder="参考范围: 125-350"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.plateletCount">
                  {{ getPlateletCountStatus(healthRecordForm.plateletCount) }}
                </div>
              </div>
            </div>

            <div class="form-section-title">肝功能检查</div>
            <div class="form-section">
              <div class="system-form-item">
                <div class="system-form-label">谷丙转氨酶 (U/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.alt" 
                    placeholder="参考范围: 7-40"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.alt">
                  {{ getAltStatus(healthRecordForm.alt) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">谷草转氨酶 (U/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.ast" 
                    placeholder="参考范围: 13-35"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.ast">
                  {{ getAstStatus(healthRecordForm.ast) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">γ-谷氨酰转移酶 (U/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.ggt" 
                    placeholder="男:10-60 女:7-45"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.ggt">
                  {{ getGgtStatus(healthRecordForm.ggt) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">碱性磷酸酶 (U/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.alp" 
                    placeholder="参考范围: 45-125"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.alp">
                  {{ getAlpStatus(healthRecordForm.alp) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">白蛋白 (g/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.albumin" 
                    placeholder="参考范围: 40-55"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.albumin">
                  {{ getAlbuminStatus(healthRecordForm.albumin) }}
                </div>
              </div>
            </div>

            <div class="form-section-title">肾功能与尿酸</div>
            <div class="form-section">
              <div class="system-form-item">
                <div class="system-form-label">肌酐 (μmol/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.creatinine" 
                    placeholder="男:59-104 女:45-84"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.creatinine">
                  {{ getCreatinineStatus(healthRecordForm.creatinine) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">尿素氮 (mmol/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.bloodUreaNitrogen" 
                    placeholder="参考范围: 3.2-7.1"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.bloodUreaNitrogen">
                  {{ getBloodUreaNitrogenStatus(healthRecordForm.bloodUreaNitrogen) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">eGFR (mL/min/1.73m²)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.egfr" 
                    placeholder="参考范围: >90"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.egfr">
                  {{ getEgfrStatus(healthRecordForm.egfr) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">尿酸 (μmol/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.uricAcid" 
                    placeholder="男:208-428 女:155-357"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.uricAcid">
                  {{ getUricAcidStatus(healthRecordForm.uricAcid) }}
                </div>
              </div>
            </div>

            <div class="form-section-title">血脂四项</div>
            <div class="form-section">
              <div class="system-form-item">
                <div class="system-form-label">总胆固醇 (mmol/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.totalCholesterol" 
                    placeholder="参考范围: <5.2"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.totalCholesterol">
                  {{ getTotalCholesterolStatus(healthRecordForm.totalCholesterol) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">甘油三酯 (mmol/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.triglycerides" 
                    placeholder="参考范围: <1.7"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.triglycerides">
                  {{ getTriglyceridesStatus(healthRecordForm.triglycerides) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">低密度脂蛋白胆固醇 (mmol/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.ldlCholesterol" 
                    placeholder="参考范围: <3.4"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.ldlCholesterol">
                  {{ getLdlCholesterolStatus(healthRecordForm.ldlCholesterol) }}
                </div>
              </div>
              <div class="system-form-item">
                <div class="system-form-label">高密度脂蛋白胆固醇 (mmol/L)</div>
                <div class="test-item-input">
                  <input 
                    class="system-form-input" 
                    type="number" 
                    step="0.1" 
                    v-model="healthRecordForm.hdlCholesterol" 
                    placeholder="男:>1.0 女:>1.3"
                  >
                  
                </div>
                <div class="health-status-hint" v-if="healthRecordForm.hdlCholesterol">
                  {{ getHdlCholesterolStatus(healthRecordForm.hdlCholesterol) }}
                </div>
              </div>
            </div>
            
            <div class="system-form-item">
              <div class="system-form-label">健康状态</div>
              <select class="system-form-input" v-model="healthRecordForm.healthStatus">
                <option value="0">正常</option>
                <option value="1">异常</option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label remark-label">
                <span>备注</span>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="addAllAbnormalItemsToRemark"
                  class="add-all-abnormal-btn"
                >
                  <el-icon><Plus /></el-icon>
                  添加异常指标
                </el-button>
              </div>
              <textarea class="system-form-input" rows="3" v-model="healthRecordForm.remark" placeholder="请输入备注信息"></textarea>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <el-button @click="closeHealthRecordDialog">取消</el-button>
          <el-button type="primary" @click="saveHealthRecord">保存</el-button>
        </div>
      </div>
    </div>

    <!-- 查看健康记录对话框 -->
    <div v-if="showViewHealthRecordDialog" class="system-modal-overlay" @click.self="closeViewHealthRecordDialog">
      <div class="system-modal system-modal-large health-report-modal">
        <div class="health-report-header">
          <div class="health-report-title">
            <h1>健康体检报告</h1>
            <div class="close-button" @click="closeViewHealthRecordDialog">×</div>
          </div>
          <div class="health-report-info">
            <div class="info-item">
              <span class="info-label">姓名：</span>
              <span class="info-value">{{ getUserName(currentHealthRecord.userId) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">体检日期：</span>
              <span class="info-value">{{ formatDate(currentHealthRecord.recordTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">健康状态：</span>
              <span class="info-value status-tag" :class="getStatusClass(currentHealthRecord.healthStatus)">
                {{ getHealthStatusText(currentHealthRecord.healthStatus) }}
              </span>
            </div>
          </div>
        </div>
        
        <div class="health-report-body">
          <!-- 基础项目表格 -->
          <div class="report-section">
            <div class="report-section-title">基础项目</div>
            <table class="report-table">
              <thead>
                <tr>
                  <th class="col-item">检查项目</th>
                  <th class="col-result">检测结果</th>
                  <th class="col-unit">单位</th>
                  <th class="col-reference">参考范围</th>
                  <th class="col-status">状态</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td class="col-item">血压</td>
                  <td class="col-result" :class="getBloodPressureValueClass(currentHealthRecord.bloodPressure)">{{ currentHealthRecord.bloodPressure || '-' }}</td>
                  <td class="col-unit">mmHg</td>
                  <td class="col-reference">90-140 / 60-90</td>
                  <td class="col-status" :class="getBloodPressureValueClass(currentHealthRecord.bloodPressure)">
                    {{ getBloodPressureStatusText(currentHealthRecord.bloodPressure) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">心率</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.heartRate, 60, 100)">{{ currentHealthRecord.heartRate || '-' }}</td>
                  <td class="col-unit">次/分</td>
                  <td class="col-reference">60-100</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.heartRate, 60, 100)">
                    {{ getHeartRateStatusText(currentHealthRecord.heartRate) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">血糖</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.bloodSugar, 3.9, 6.1)">{{ currentHealthRecord.bloodSugar || '-' }}</td>
                  <td class="col-unit">mmol/L</td>
                  <td class="col-reference">3.9-6.1</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.bloodSugar, 3.9, 6.1)">
                    {{ getBloodSugarStatusText(currentHealthRecord.bloodSugar) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">体温</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.temperature, 36.0, 37.3)">{{ currentHealthRecord.temperature || '-' }}</td>
                  <td class="col-unit">°C</td>
                  <td class="col-reference">36.0-37.3</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.temperature, 36.0, 37.3)">
                    {{ getTemperatureStatusText(currentHealthRecord.temperature) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">BMI</td>
                  <td class="col-result" :class="getBMIValueClass(currentHealthRecord.weight, currentHealthRecord.height)">{{ calculateBMI(currentHealthRecord.weight, currentHealthRecord.height) || '-' }}</td>
                  <td class="col-unit">-</td>
                  <td class="col-reference">18.5-23.9</td>
                  <td class="col-status" :class="getBMIValueClass(currentHealthRecord.weight, currentHealthRecord.height)">
                    {{ getBMIStatusText(currentHealthRecord.weight, currentHealthRecord.height) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">身高</td>
                  <td class="col-result">{{ currentHealthRecord.height || '-' }}</td>
                  <td class="col-unit">cm</td>
                  <td class="col-reference">-</td>
                  <td class="col-status">-</td>
                </tr>
                <tr>
                  <td class="col-item">体重</td>
                  <td class="col-result">{{ currentHealthRecord.weight || '-' }}</td>
                  <td class="col-unit">kg</td>
                  <td class="col-reference">-</td>
                  <td class="col-status">-</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 血常规检查表格 -->
          <div v-if="hasBloodTestData()" class="report-section">
            <div class="report-section-title">血常规检查</div>
            <table class="report-table">
              <thead>
                <tr>
                  <th class="col-item">检查项目</th>
                  <th class="col-result">检测结果</th>
                  <th class="col-unit">单位</th>
                  <th class="col-reference">参考范围</th>
                  <th class="col-status">状态</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td class="col-item">白细胞计数</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.whiteBloodCell, 3.5, 9.5)">{{ currentHealthRecord.whiteBloodCell || '-' }}</td>
                  <td class="col-unit">×10^9/L</td>
                  <td class="col-reference">3.5-9.5</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.whiteBloodCell, 3.5, 9.5)">
                    {{ getWhiteBloodCellStatusText(currentHealthRecord.whiteBloodCell) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">血红蛋白</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.hemoglobin, 110, 160)">{{ currentHealthRecord.hemoglobin || '-' }}</td>
                  <td class="col-unit">g/L</td>
                  <td class="col-reference">男:120-160 女:110-150</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.hemoglobin, 110, 160)">
                    {{ getHemoglobinStatusText(currentHealthRecord.hemoglobin) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">红细胞压积</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.hematocrit, 35, 50)">{{ currentHealthRecord.hematocrit || '-' }}</td>
                  <td class="col-unit">%</td>
                  <td class="col-reference">35-50</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.hematocrit, 35, 50)">
                    {{ getHematocritStatusText(currentHealthRecord.hematocrit) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">血小板计数</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.plateletCount, 125, 350)">{{ currentHealthRecord.plateletCount || '-' }}</td>
                  <td class="col-unit">×10^9/L</td>
                  <td class="col-reference">125-350</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.plateletCount, 125, 350)">
                    {{ getPlateletCountStatusText(currentHealthRecord.plateletCount) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 肝功能检查表格 -->
          <div v-if="hasLiverTestData()" class="report-section">
            <div class="report-section-title">肝功能检查</div>
            <table class="report-table">
              <thead>
                <tr>
                  <th class="col-item">检查项目</th>
                  <th class="col-result">检测结果</th>
                  <th class="col-unit">单位</th>
                  <th class="col-reference">参考范围</th>
                  <th class="col-status">状态</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td class="col-item">谷丙转氨酶</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.alt, 7, 40)">{{ currentHealthRecord.alt || '-' }}</td>
                  <td class="col-unit">U/L</td>
                  <td class="col-reference">7-40</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.alt, 7, 40)">
                    {{ getAltStatusText(currentHealthRecord.alt) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">谷草转氨酶</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.ast, 13, 35)">{{ currentHealthRecord.ast || '-' }}</td>
                  <td class="col-unit">U/L</td>
                  <td class="col-reference">13-35</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.ast, 13, 35)">
                    {{ getAstStatusText(currentHealthRecord.ast) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">γ-谷氨酰转移酶</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.ggt, 7, 60)">{{ currentHealthRecord.ggt || '-' }}</td>
                  <td class="col-unit">U/L</td>
                  <td class="col-reference">男:10-60 女:7-45</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.ggt, 7, 60)">
                    {{ getGgtStatusText(currentHealthRecord.ggt) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">碱性磷酸酶</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.alp, 45, 125)">{{ currentHealthRecord.alp || '-' }}</td>
                  <td class="col-unit">U/L</td>
                  <td class="col-reference">45-125</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.alp, 45, 125)">
                    {{ getAlpStatusText(currentHealthRecord.alp) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">白蛋白</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.albumin, 40, 55)">{{ currentHealthRecord.albumin || '-' }}</td>
                  <td class="col-unit">g/L</td>
                  <td class="col-reference">40-55</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.albumin, 40, 55)">
                    {{ getAlbuminStatusText(currentHealthRecord.albumin) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 肾功能与尿酸表格 -->
          <div v-if="hasKidneyTestData()" class="report-section">
            <div class="report-section-title">肾功能与尿酸</div>
            <table class="report-table">
              <thead>
                <tr>
                  <th class="col-item">检查项目</th>
                  <th class="col-result">检测结果</th>
                  <th class="col-unit">单位</th>
                  <th class="col-reference">参考范围</th>
                  <th class="col-status">状态</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td class="col-item">肌酐</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.creatinine, 45, 104)">{{ currentHealthRecord.creatinine || '-' }}</td>
                  <td class="col-unit">μmol/L</td>
                  <td class="col-reference">男:59-104 女:45-84</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.creatinine, 45, 104)">
                    {{ getCreatinineStatusText(currentHealthRecord.creatinine) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">尿素氮</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.bloodUreaNitrogen, 3.2, 7.1)">{{ currentHealthRecord.bloodUreaNitrogen || '-' }}</td>
                  <td class="col-unit">mmol/L</td>
                  <td class="col-reference">3.2-7.1</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.bloodUreaNitrogen, 3.2, 7.1)">
                    {{ getBloodUreaNitrogenStatusText(currentHealthRecord.bloodUreaNitrogen) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">eGFR</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.egfr, 90, 200, true)">{{ currentHealthRecord.egfr || '-' }}</td>
                  <td class="col-unit">mL/min/1.73m²</td>
                  <td class="col-reference">>90</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.egfr, 90, 200, true)">
                    {{ getEgfrStatusText(currentHealthRecord.egfr) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">尿酸</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.uricAcid, 155, 428)">{{ currentHealthRecord.uricAcid || '-' }}</td>
                  <td class="col-unit">μmol/L</td>
                  <td class="col-reference">男:208-428 女:155-357</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.uricAcid, 155, 428)">
                    {{ getUricAcidStatusText(currentHealthRecord.uricAcid) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 血脂四项表格 -->
          <div v-if="hasLipidTestData()" class="report-section">
            <div class="report-section-title">血脂四项</div>
            <table class="report-table">
              <thead>
                <tr>
                  <th class="col-item">检查项目</th>
                  <th class="col-result">检测结果</th>
                  <th class="col-unit">单位</th>
                  <th class="col-reference">参考范围</th>
                  <th class="col-status">状态</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td class="col-item">总胆固醇</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.totalCholesterol, 0, 5.2)">{{ currentHealthRecord.totalCholesterol || '-' }}</td>
                  <td class="col-unit">mmol/L</td>
                  <td class="col-reference">&lt;5.2</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.totalCholesterol, 0, 5.2)">
                    {{ getTotalCholesterolStatusText(currentHealthRecord.totalCholesterol) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">甘油三酯</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.triglycerides, 0, 1.7)">{{ currentHealthRecord.triglycerides || '-' }}</td>
                  <td class="col-unit">mmol/L</td>
                  <td class="col-reference">&lt;1.7</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.triglycerides, 0, 1.7)">
                    {{ getTriglyceridesStatusText(currentHealthRecord.triglycerides) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">低密度脂蛋白</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.ldlCholesterol, 0, 3.4)">{{ currentHealthRecord.ldlCholesterol || '-' }}</td>
                  <td class="col-unit">mmol/L</td>
                  <td class="col-reference">&lt;3.4</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.ldlCholesterol, 0, 3.4)">
                    {{ getLdlCholesterolStatusText(currentHealthRecord.ldlCholesterol) }}
                  </td>
                </tr>
                <tr>
                  <td class="col-item">高密度脂蛋白</td>
                  <td class="col-result" :class="getLabTestValueClass(currentHealthRecord.hdlCholesterol, 1.0, 3.0, true)">{{ currentHealthRecord.hdlCholesterol || '-' }}</td>
                  <td class="col-unit">mmol/L</td>
                  <td class="col-reference">男:&gt;1.0 女:&gt;1.3</td>
                  <td class="col-status" :class="getLabTestValueClass(currentHealthRecord.hdlCholesterol, 1.0, 3.0, true)">
                    {{ getHdlCholesterolStatusText(currentHealthRecord.hdlCholesterol) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 备注信息 -->
          <div v-if="currentHealthRecord.remark" class="report-section">
            <div class="report-section-title">备注信息</div>
            <div class="report-remark">{{ currentHealthRecord.remark }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Check, Warning, Calendar, ArrowUp, ArrowDown, Plus } from '@element-plus/icons-vue'
import { healthApi } from '@/api/health'
import { userApi } from '@/api/user'

// 搜索表单
const searchForm = reactive({
  userName: '',
  healthStatus: '',
  recordTime: ''
})

// 用户列表
const users = ref([])
// 添加用户映射表，提高查找效率
const userMap = ref({})

// 健康记录列表
const healthRecords = ref([])
const total = ref(0)
const loading = ref(false)
const allHealthRecords = ref([])

// 统计数据
const statsData = reactive({
  totalCount: 0,
  normalCount: 0,
  abnormalCount: 0,
  monthlyCount: 0,
  lastMonthTotalCount: 0,
  lastMonthNormalCount: 0,
  lastMonthAbnormalCount: 0,
  lastMonthMonthlyCount: 0
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const jumpPage = ref('')

// 对话框状态
const showAddHealthRecordDialog = ref(false)
const showEditHealthRecordDialog = ref(false)
const showViewHealthRecordDialog = ref(false)
const currentHealthRecord = ref({})

// 当前健康评估结果
const currentHealthEvaluation = reactive({
  status: 0,
  reasons: []
})

// 健康记录表单
const healthRecordForm = reactive({
  id: '',
  userId: '',
  recordTime: '',
  bloodPressureHigh: '',
  bloodPressureLow: '',
  bloodSugarType: '',
  temperatureType: '',
  heartRate: '',
  bloodSugar: '',
  temperature: '',
  weight: '',
  height: '',
  healthStatus: 0,
  remark: '',
  whiteBloodCell: '',
  hemoglobin: '',
  hematocrit: '',
  plateletCount: '',
  alt: '',
  ast: '',
  ggt: '',
  alp: '',
  totalProtein: '',
  albumin: '',
  creatinine: '',
  bloodUreaNitrogen: '',
  egfr: '',
  uricAcid: '',
  totalCholesterol: '',
  triglycerides: '',
  ldlCholesterol: '',
  hdlCholesterol: ''
})

// 获取用户列表
const fetchUsers = async () => {
  try {
    const params = {
      page: 1,
      size: 1000
    }
    
    const response = await userApi.getUserPage(params);
    const records = response.data?.records || response.records || response.data || [];
    
    users.value = records;
    
    // 构建用户ID到用户名的映射表
    userMap.value = {};
    records.forEach(user => {
      if (user && user.id !== undefined) {
        const userIdStr = String(user.id);
        userMap.value[userIdStr] = user.name || '未知用户';
      }
    });
    
  } catch (error) {
    console.error('获取用户列表失败:', error);
    ElMessage.error('获取用户列表失败');
  }
}

// 获取用户名称
const getUserName = (userId) => {
  if (!userId && userId !== 0) {
    return '未知用户'
  }
  
  const userIdStr = String(userId)
  
  if (userMap.value[userIdStr]) {
    return userMap.value[userIdStr]
  }
  
  const user = users.value.find(u => String(u.id) === userIdStr)
  return user ? user.name : '未知用户'
}

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(total.value / pageSize.value)
})

// 获取健康记录列表
const fetchHealthRecords = async () => {
  try {
    loading.value = true
    const params = {
      page: 1,
      size: 1000
    }
    
    const response = await healthApi.getHealthRecords(params)
    console.log('健康记录响应:', response)
    
    // 兼容不同的响应格式
    const records = response.data?.records || response.records || response.data || [];
    const totalCount = response.data?.total || response.total || records.length;
    
    console.log('健康记录数据:', records, '总数:', totalCount)
    
    // 按id升序排序，确保按数据库顺序展示
    allHealthRecords.value = records.sort((a, b) => {
      const idA = parseInt(a.id);
      const idB = parseInt(b.id);
      return idA - idB;
    });
    
    // 应用分页和搜索过滤
    applyPagination()
    total.value = totalCount
    
    // 更新统计数据
    updateStatsData()
  } catch (error) {
    console.error('获取健康记录列表失败:', error)
    ElMessage.error('获取健康记录列表失败')
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStatsData = () => {
  const hasData = (value) => value !== null && value !== undefined && value !== ''
  const isAbnormal = (value, min, max, reverse = false) => {
    if (value === null || value === undefined || value === '') return false
    const val = parseFloat(value)
    if (reverse) {
      return val < min
    }
    return val < min || val > max
  }
  
  const isBloodPressureAbnormal = (bloodPressure) => {
    if (!bloodPressure) return false
    const parts = bloodPressure.split('/')
    if (parts.length !== 2) return false
    const systolic = parseFloat(parts[0])
    const diastolic = parseFloat(parts[1])
    if (isNaN(systolic) || isNaN(diastolic)) return false
    return systolic < 90 || systolic > 140 || diastolic < 60 || diastolic > 90
  }
  
  const isBMIAbnormal = (weight, height) => {
    if (!weight || !height) return false
    const weightKg = parseFloat(weight)
    const heightM = parseFloat(height) / 100
    if (isNaN(weightKg) || isNaN(heightM) || heightM === 0) return false
    const bmi = weightKg / (heightM * heightM)
    return bmi < 18.5 || bmi > 24
  }
  
  const isRecordAbnormal = (record) => {
    const isBasicAbnormal = isAbnormal(record.heartRate, 60, 100) ||
                            isAbnormal(record.bloodSugar, 3.9, 6.1) ||
                            isAbnormal(record.temperature, 36.0, 37.3) ||
                            isBloodPressureAbnormal(record.bloodPressure) ||
                            isBMIAbnormal(record.weight, record.height)
    const isBloodAbnormal = isAbnormal(record.whiteBloodCell, 3.5, 9.5) ||
                            isAbnormal(record.hemoglobin, 110, 160) ||
                            isAbnormal(record.hematocrit, 35, 50) ||
                            isAbnormal(record.plateletCount, 125, 350)
    const isLiverAbnormal = isAbnormal(record.alt, 7, 40) ||
                            isAbnormal(record.ast, 13, 35) ||
                            isAbnormal(record.ggt, 7, 60) ||
                            isAbnormal(record.alp, 45, 125) ||
                            isAbnormal(record.albumin, 40, 55)
    const isKidneyAbnormal = isAbnormal(record.creatinine, 45, 104) ||
                             isAbnormal(record.bloodUreaNitrogen, 3.2, 7.1) ||
                             isAbnormal(record.egfr, 90, 200, true) ||
                             isAbnormal(record.uricAcid, 155, 428)
    const isLipidAbnormal = isAbnormal(record.totalCholesterol, 0, 5.2) ||
                            isAbnormal(record.triglycerides, 0, 1.7) ||
                            isAbnormal(record.ldlCholesterol, 0, 3.4) ||
                            isAbnormal(record.hdlCholesterol, 1.0, 3.0, true)
    return isBasicAbnormal || isBloodAbnormal || isLiverAbnormal || isKidneyAbnormal || isLipidAbnormal
  }
  
  statsData.totalCount = allHealthRecords.value.length
  statsData.normalCount = allHealthRecords.value.filter(record => !isRecordAbnormal(record)).length
  statsData.abnormalCount = allHealthRecords.value.filter(record => isRecordAbnormal(record)).length
  
  const currentMonth = new Date().getMonth() + 1
  const currentYear = new Date().getFullYear()
  statsData.monthlyCount = allHealthRecords.value.filter(record => {
    const recordDate = new Date(record.recordTime)
    return recordDate.getMonth() + 1 === currentMonth && recordDate.getFullYear() === currentYear
  }).length
  
  statsData.lastMonthTotalCount = Math.floor(statsData.totalCount * 0.9)
  statsData.lastMonthNormalCount = Math.floor(statsData.normalCount * 0.9)
  statsData.lastMonthAbnormalCount = Math.floor(statsData.abnormalCount * 0.9)
  statsData.lastMonthMonthlyCount = Math.floor(statsData.monthlyCount * 0.9)
}

// 应用分页和搜索过滤
const applyPagination = () => {
  let filteredRecords = [...allHealthRecords.value]
  
  // 应用搜索过滤
  if (searchForm.userName) {
    // 先根据用户名过滤出用户ID，再过滤健康记录
    const matchedUserIds = users.value
      .filter(user => user.name.includes(searchForm.userName))
      .map(user => user.id)
    filteredRecords = filteredRecords.filter(record => matchedUserIds.includes(record.userId))
  }
  
  if (searchForm.healthStatus !== '') {
    filteredRecords = filteredRecords.filter(record => 
      record.healthStatus === parseInt(searchForm.healthStatus)
    )
  }
  
  if (searchForm.recordTime) {
    filteredRecords = filteredRecords.filter(record => 
      record.recordTime === searchForm.recordTime
    )
  }
  
  // 更新总数
  total.value = filteredRecords.length
  
  // 应用分页
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  healthRecords.value = filteredRecords.slice(start, end)
}

// 搜索健康记录
const searchHealthRecords = () => {
  currentPage.value = 1
  applyPagination()
}

// 重置搜索
const resetSearch = () => {
  searchForm.userName = ''
  searchForm.healthStatus = ''
  searchForm.recordTime = ''
  currentPage.value = 1
  applyPagination()
}

// 获取页码列表
const getPageNumbers = () => {
  const pages = []
  const maxPages = 7 // 增加显示的页码数量
  
  if (totalPages.value <= maxPages) {
    // 总页数少于最大显示页数，显示所有页码
    for (let i = 1; i <= totalPages.value; i++) {
      pages.push(i)
    }
  } else {
    // 总页数多于最大显示页数，需要省略号
    if (currentPage.value <= 4) {
      // 当前页在前面
      for (let i = 1; i <= 5; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(totalPages.value)
    } else if (currentPage.value >= totalPages.value - 3) {
      // 当前页在后面
      pages.push(1)
      pages.push('...')
      for (let i = totalPages.value - 4; i <= totalPages.value; i++) {
        pages.push(i)
      }
    } else {
      // 当前页在中间
      pages.push(1)
      pages.push('...')
      for (let i = currentPage.value - 1; i <= currentPage.value + 1; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(totalPages.value)
    }
  }
  
  return pages
}

// 分页处理
const handlePageChange = (page) => {
  if (page < 1 || page > totalPages.value || page === currentPage.value) {
    return
  }
  
  currentPage.value = page
  applyPagination()
}

// 处理页面大小变化
const handlePageSizeChange = () => {
  currentPage.value = 1 // 重置到第一页
  applyPagination()
}

// 处理跳转到指定页面
const handleJumpToPage = () => {
  const page = parseInt(jumpPage.value)
  
  if (isNaN(page) || page < 1 || page > totalPages.value) {
    ElMessage.warning(`请输入1到${totalPages.value}之间的页码`)
    return
  }
  
  handlePageChange(page)
  jumpPage.value = '' // 清空跳转页码
}

// 保留原有的分页函数以确保兼容性
const handlePreviousPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    applyPagination()
  }
}

const handleNextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    applyPagination()
  }
}

const handlePageClick = (page) => {
  currentPage.value = page
  applyPagination()
}

// 监听pageSize变化，重置currentPage并重新应用分页
watch(pageSize, () => {
  currentPage.value = 1
  applyPagination()
})

// 关闭健康记录对话框
const closeHealthRecordDialog = () => {
  showAddHealthRecordDialog.value = false
  showEditHealthRecordDialog.value = false
  resetHealthRecordForm()
}

// 重置健康记录表单
const resetHealthRecordForm = () => {
  healthRecordForm.id = ''
  healthRecordForm.userId = ''
  healthRecordForm.recordTime = ''
  healthRecordForm.bloodPressureHigh = ''
  healthRecordForm.bloodPressureLow = ''
  healthRecordForm.bloodSugarType = ''
  healthRecordForm.temperatureType = ''
  healthRecordForm.heartRate = ''
  healthRecordForm.bloodSugar = ''
  healthRecordForm.temperature = ''
  healthRecordForm.weight = ''
  healthRecordForm.height = ''
  healthRecordForm.healthStatus = 0
  healthRecordForm.remark = ''
  healthRecordForm.whiteBloodCell = ''
  healthRecordForm.hemoglobin = ''
  healthRecordForm.hematocrit = ''
  healthRecordForm.plateletCount = ''
  healthRecordForm.alt = ''
  healthRecordForm.ast = ''
  healthRecordForm.ggt = ''
  healthRecordForm.alp = ''
  healthRecordForm.totalProtein = ''
  healthRecordForm.albumin = ''
  healthRecordForm.creatinine = ''
  healthRecordForm.bloodUreaNitrogen = ''
  healthRecordForm.egfr = ''
  healthRecordForm.uricAcid = ''
  healthRecordForm.totalCholesterol = ''
  healthRecordForm.triglycerides = ''
  healthRecordForm.ldlCholesterol = ''
  healthRecordForm.hdlCholesterol = ''
  
  // 重置健康评估结果
  currentHealthEvaluation.status = 0
  currentHealthEvaluation.reasons = []
}

// 关闭查看健康记录对话框
const closeViewHealthRecordDialog = () => {
  showViewHealthRecordDialog.value = false
  currentHealthRecord.value = {}
}

// 保存健康记录
const saveHealthRecord = async () => {
  if (!healthRecordForm.userId || !healthRecordForm.recordTime) {
    ElMessage.error('请填写完整的健康记录信息')
    return
  }
  
  try {
    loading.value = true
    
    // 将收缩压和舒张压合并为血压字段
    const recordData = {
      ...healthRecordForm,
      bloodPressure: `${healthRecordForm.bloodPressureHigh}/${healthRecordForm.bloodPressureLow}`
    }
    
    if (healthRecordForm.id) {
      // 编辑健康记录
      const response = await healthApi.updateHealthRecord(healthRecordForm.id, recordData)
      
      if (response) {
        ElMessage.success('健康记录更新成功')
        closeHealthRecordDialog()
        await fetchHealthRecords()
      } else {
        ElMessage.error('健康记录更新失败')
      }
    } else {
      // 新增健康记录
      const response = await healthApi.createHealthRecord(recordData)
      
      if (response) {
        ElMessage.success('健康记录创建成功')
        closeHealthRecordDialog()
        await fetchHealthRecords()
      } else {
        ElMessage.error('健康记录创建失败')
      }
    }
  } catch (error) {
    console.error('保存健康记录失败:', error)
    ElMessage.error('保存健康记录失败')
  } finally {
    loading.value = false
  }
}

// 查看健康记录
const viewHealthRecord = async (id) => {
  try {
    loading.value = true
    // 从本地数据中查找
    const record = allHealthRecords.value.find(item => item.id === id)
    if (record) {
      currentHealthRecord.value = { ...record } // 创建副本避免直接修改原数据
      showViewHealthRecordDialog.value = true
    } else {
      ElMessage.error('未找到健康记录信息')
    }
  } catch (error) {
    console.error('获取健康记录详情失败:', error)
    ElMessage.error('获取健康记录详情失败')
  } finally {
    loading.value = false
  }
}

// 编辑健康记录
const editHealthRecord = async (id) => {
  try {
    loading.value = true
    // 从本地数据中查找
    const record = allHealthRecords.value.find(item => item.id === id)
    if (record) {
      // 创建副本避免直接修改原数据
      healthRecordForm.id = record.id
      healthRecordForm.userId = record.userId
      healthRecordForm.recordTime = record.recordTime
      
      // 将血压字符串拆分为收缩压和舒张压
      if (record.bloodPressure) {
        const [high, low] = record.bloodPressure.split('/')
        healthRecordForm.bloodPressureHigh = high
        healthRecordForm.bloodPressureLow = low
      } else {
        healthRecordForm.bloodPressureHigh = ''
        healthRecordForm.bloodPressureLow = ''
      }
      
      healthRecordForm.heartRate = record.heartRate
      healthRecordForm.bloodSugar = record.bloodSugar
      healthRecordForm.temperature = record.temperature
      healthRecordForm.weight = record.weight
      healthRecordForm.height = record.height
      healthRecordForm.healthStatus = record.healthStatus
      healthRecordForm.remark = record.remark
      healthRecordForm.whiteBloodCell = record.whiteBloodCell
      healthRecordForm.hemoglobin = record.hemoglobin
      healthRecordForm.hematocrit = record.hematocrit
      healthRecordForm.plateletCount = record.plateletCount
      healthRecordForm.alt = record.alt
      healthRecordForm.ast = record.ast
      healthRecordForm.ggt = record.ggt
      healthRecordForm.alp = record.alp
      healthRecordForm.totalProtein = record.totalProtein
      healthRecordForm.albumin = record.albumin
      healthRecordForm.creatinine = record.creatinine
      healthRecordForm.bloodUreaNitrogen = record.bloodUreaNitrogen
      healthRecordForm.egfr = record.egfr
      healthRecordForm.uricAcid = record.uricAcid
      healthRecordForm.totalCholesterol = record.totalCholesterol
      healthRecordForm.triglycerides = record.triglycerides
      healthRecordForm.ldlCholesterol = record.ldlCholesterol
      healthRecordForm.hdlCholesterol = record.hdlCholesterol
      
      showEditHealthRecordDialog.value = true
    } else {
      ElMessage.error('未找到健康记录信息')
    }
  } catch (error) {
    console.error('获取健康记录详情失败:', error)
    ElMessage.error('获取健康记录详情失败')
  } finally {
    loading.value = false
  }
}

// 删除健康记录
const deleteHealthRecord = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条健康记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    const success = await healthApi.deleteHealthRecord(id)
    
    if (success) {
      ElMessage.success('健康记录删除成功')
      await fetchHealthRecords()
    } else {
      ElMessage.error('健康记录删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除健康记录失败:', error)
      ElMessage.error('删除健康记录失败')
    }
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 格式化血压
const formatBloodPressure = (high, low) => {
  if (!high || !low) return '-/-'
  return `${high}/${low}`
}

// 健康状态判断函数
const evaluateHealthStatus = (healthData) => {
  let status = 0 // 默认正常
  let reasons = []
  
  // BMI判断
  const bmi = calculateBMI(healthData.weight, healthData.height)
  if (bmi !== null) {
    const bmiValue = parseFloat(bmi)
    if (bmiValue < 18.5) {
      status = 1
      reasons.push('体重偏瘦')
    } else if (bmiValue >= 28.0) {
      status = 1
      reasons.push('肥胖')
    } else if (bmiValue >= 24.0) {
      status = 1
      reasons.push('超重')
    }
  }
  
  // 体温判断（腋温）
  if (healthData.temperature) {
    if (healthData.temperature < 36.0) {
      status = 1
      reasons.push('体温过低')
    } else if (healthData.temperature > 38.1) {
      status = 1
      reasons.push('中高热')
    } else if (healthData.temperature >= 37.3) {
      status = 1
      reasons.push('低热')
    }
  }
  
  // 血糖判断
  if (healthData.bloodSugar) {
    if (healthData.bloodSugarType === 'fasting') {
      if (healthData.bloodSugar >= 7.0) {
        status = 1
        reasons.push('空腹血糖过高')
      } else if (healthData.bloodSugar >= 6.1) {
        status = 1
        reasons.push('空腹血糖受损')
      }
    } else if (healthData.bloodSugarType === 'postprandial') {
      if (healthData.bloodSugar >= 11.1) {
        status = 1
        reasons.push('餐后血糖过高')
      } else if (healthData.bloodSugar >= 7.8) {
        status = 1
        reasons.push('糖耐量异常')
      }
    }
  }
  
  // 血压判断
  if (healthData.bloodPressureHigh && healthData.bloodPressureLow) {
    const high = parseInt(healthData.bloodPressureHigh)
    const low = parseInt(healthData.bloodPressureLow)
    
    if (high >= 160 || low >= 100) {
      status = 1
      reasons.push('高血压2级')
    } else if (high >= 140 || low >= 90) {
      status = 1
      reasons.push('高血压1级')
    } else if (high < 90 || low < 60) {
      status = 1
      reasons.push('低血压')
    } else if (high >= 130 || low >= 85) {
      status = 1
      reasons.push('正常高值')
    }
  }
  
  // 心率判断
  if (healthData.heartRate) {
    if (healthData.heartRate > 100) {
      status = 1
      reasons.push('心动过速')
    } else if (healthData.heartRate < 60) {
      status = 1
      reasons.push('心动过缓')
    }
  }
  
  return { status, reasons }
}

// 获取血压状态
const getBloodPressureStatus = (high, low) => {
  if (!high || !low) return ''
  
  const highVal = parseInt(high)
  const lowVal = parseInt(low)
  
  if (highVal >= 160 || lowVal >= 100) {
    return '高血压2级（≥160/100 mmHg）'
  } else if (highVal >= 140 || lowVal >= 90) {
    return '高血压1级（140-159/90-99 mmHg）'
  } else if (highVal < 90 || lowVal < 60) {
    return '低血压（<90/60 mmHg）'
  } else if (highVal >= 130 || lowVal >= 85) {
    return '正常高值（130-139/85-89 mmHg）'
  } else {
    return '理想状态（<130/85 mmHg）'
  }
}

// 获取血糖状态
const getBloodSugarStatus = (value, type) => {
  if (!value || !type) return ''
  
  const val = parseFloat(value)
  
  if (type === 'fasting') {
    if (val >= 7.0) {
      return '糖尿病可能（≥7.0 mmol/L）'
    } else if (val >= 6.1) {
      return '空腹血糖受损（6.1-7.0 mmol/L）'
    } else {
      return '正常（3.9-6.1 mmol/L）'
    }
  } else if (type === 'postprandial') {
    if (val >= 11.1) {
      return '糖尿病可能（≥11.1 mmol/L）'
    } else if (val >= 7.8) {
      return '糖耐量异常（7.8-11.1 mmol/L）'
    } else {
      return '正常（<7.8 mmol/L）'
    }
  }
  
  return ''
}

// 获取体温状态
const getTemperatureStatus = (value, type) => {
  if (!value || !type) return ''
  
  const val = parseFloat(value)
  
  // 不同测量部位的正常范围略有不同，这里以腋温为基准
  if (type === 'axillary') {
    if (val < 36.0) {
      return '低体温（<36.0℃）'
    } else if (val > 38.1) {
      return '中高热（>38.1℃）'
    } else if (val >= 37.3) {
      return '低热（37.3-38.0℃）'
    } else {
      return '正常（36.0-37.2℃）'
    }
  } else if (type === 'oral') {
    // 口腔温度通常比腋温高0.2-0.4℃
    if (val < 36.2) {
      return '体温偏低'
    } else if (val > 38.5) {
      return '中高热'
    } else if (val >= 37.5) {
      return '低热'
    } else {
      return '正常'
    }
  } else {
    // 其他测量部位
    return '请参考相应测量部位正常范围'
  }
}

// 获取心率状态
const getHeartRateStatus = (value) => {
  if (!value) return ''
  
  const val = parseInt(value)
  
  if (val > 100) {
    return '心动过速（>100 bpm）'
  } else if (val < 60) {
    return '心动过缓（<60 bpm）'
  } else if (val >= 50 && val <= 80) {
    return '更理想状态（50-80 bpm）'
  } else {
    return '健康范围（60-100 bpm）'
  }
}

// 获取心率状态文本（用于表格）
const getHeartRateStatusText = (value) => {
  if (!value) return ''
  const val = parseInt(value)
  if (val > 100) return '偏高'
  if (val < 60) return '偏低'
  return '正常'
}

// 获取BMI状态
const getBMIStatus = (bmi) => {
  if (!bmi) return ''
  
  const val = parseFloat(bmi)
  
  if (val < 18.5) {
    return '偏瘦'
  } else if (val >= 28.0) {
    return '肥胖'
  } else if (val >= 24.0) {
    return '超重'
  } else {
    return '正常'
  }
}

// 获取BMI状态文本（用于表格）
const getBMIStatusText = (weight, height) => {
  const bmi = calculateBMI(weight, height)
  if (!bmi) return ''
  const val = parseFloat(bmi)
  if (val < 18.5) return '偏瘦'
  if (val >= 28.0) return '肥胖'
  if (val >= 24.0) return '超重'
  return '正常'
}

// 获取血压状态文本（用于表格）
const getBloodPressureStatusText = (bloodPressure) => {
  if (!bloodPressure) return ''
  const parts = bloodPressure.split('/')
  if (parts.length !== 2) return ''
  const systolic = parseInt(parts[0])
  const diastolic = parseInt(parts[1])
  if (systolic < 90 || diastolic < 60) return '偏低'
  if (systolic > 140 || diastolic > 90) return '偏高'
  return '正常'
}

// 获取血糖状态文本（用于表格）
const getBloodSugarStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 3.9) return '偏低'
  if (val > 6.1) return '偏高'
  return '正常'
}

// 获取体温状态文本（用于表格）
const getTemperatureStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 36.0) return '偏低'
  if (val > 37.3) return '偏高'
  return '正常'
}

// 获取白细胞计数状态文本（用于表格）
const getWhiteBloodCellStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 3.5) return '偏低'
  if (val > 9.5) return '偏高'
  return '正常'
}

// 获取血红蛋白状态文本（用于表格）
const getHemoglobinStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 110) return '偏低'
  if (val > 160) return '偏高'
  return '正常'
}

// 获取红细胞压积状态文本（用于表格）
const getHematocritStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 35) return '偏低'
  if (val > 50) return '偏高'
  return '正常'
}

// 获取血小板计数状态文本（用于表格）
const getPlateletCountStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 125) return '偏低'
  if (val > 350) return '偏高'
  return '正常'
}

// 获取谷丙转氨酶状态文本（用于表格）
const getAltStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 7) return '偏低'
  if (val > 40) return '偏高'
  return '正常'
}

// 获取谷草转氨酶状态文本（用于表格）
const getAstStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 13) return '偏低'
  if (val > 35) return '偏高'
  return '正常'
}

// 获取γ-谷氨酰转移酶状态文本（用于表格）
const getGgtStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 7) return '偏低'
  if (val > 60) return '偏高'
  return '正常'
}

// 获取碱性磷酸酶状态文本（用于表格）
const getAlpStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 45) return '偏低'
  if (val > 125) return '偏高'
  return '正常'
}

// 获取白蛋白状态文本（用于表格）
const getAlbuminStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 40) return '偏低'
  if (val > 55) return '偏高'
  return '正常'
}

// 获取肌酐状态文本（用于表格）
const getCreatinineStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 45) return '偏低'
  if (val > 104) return '偏高'
  return '正常'
}

// 获取尿素氮状态文本（用于表格）
const getBloodUreaNitrogenStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 3.2) return '偏低'
  if (val > 7.1) return '偏高'
  return '正常'
}

// 获取eGFR状态文本（用于表格）
const getEgfrStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 90) return '偏低'
  return '正常'
}

// 获取尿酸状态文本（用于表格）
const getUricAcidStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 155) return '偏低'
  if (val > 428) return '偏高'
  return '正常'
}

// 获取总胆固醇状态文本（用于表格）
const getTotalCholesterolStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 3.0) return '偏低'
  if (val > 5.2) return '偏高'
  return '正常'
}

// 获取甘油三酯状态文本（用于表格）
const getTriglyceridesStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 0.5) return '偏低'
  if (val > 1.7) return '偏高'
  return '正常'
}

// 获取低密度脂蛋白胆固醇状态文本（用于表格）
const getLdlCholesterolStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 2.0) return '偏低'
  if (val > 3.4) return '偏高'
  return '正常'
}

// 获取高密度脂蛋白胆固醇状态文本（用于表格）
const getHdlCholesterolStatusText = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 1.0) return '偏低'
  if (val > 3.0) return '偏高'
  return '正常'
}

// 获取白细胞计数状态
const getWhiteBloodCellStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 3.5) return '偏低（<3.5×10^9/L）'
  if (val > 9.5) return '偏高（>9.5×10^9/L）'
  return '正常（3.5-9.5×10^9/L）'
}

// 获取血红蛋白状态
const getHemoglobinStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 110) return '偏低（<110 g/L）'
  if (val > 160) return '偏高（>160 g/L）'
  return '正常（110-160 g/L）'
}

// 获取红细胞压积状态
const getHematocritStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 35) return '偏低（<35%）'
  if (val > 50) return '偏高（>50%）'
  return '正常（35-50%）'
}

// 获取血小板计数状态
const getPlateletCountStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 125) return '偏低（<125×10^9/L）'
  if (val > 350) return '偏高（>350×10^9/L）'
  return '正常（125-350×10^9/L）'
}

// 获取谷丙转氨酶状态
const getAltStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 7) return '偏低（<7 U/L）'
  if (val > 40) return '偏高（>40 U/L）'
  return '正常（7-40 U/L）'
}

// 获取谷草转氨酶状态
const getAstStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 13) return '偏低（<13 U/L）'
  if (val > 35) return '偏高（>35 U/L）'
  return '正常（13-35 U/L）'
}

// 获取γ-谷氨酰转移酶状态
const getGgtStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 7) return '偏低（<7 U/L）'
  if (val > 60) return '偏高（>60 U/L）'
  return '正常（7-60 U/L）'
}

// 获取碱性磷酸酶状态
const getAlpStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 45) return '偏低（<45 U/L）'
  if (val > 125) return '偏高（>125 U/L）'
  return '正常（45-125 U/L）'
}

// 获取总蛋白状态
const getTotalProteinStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 60) return '偏低（<60 g/L）'
  if (val > 83) return '偏高（>83 g/L）'
  return '正常（60-83 g/L）'
}

// 获取白蛋白状态
const getAlbuminStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 40) return '偏低（<40 g/L）'
  if (val > 55) return '偏高（>55 g/L）'
  return '正常（40-55 g/L）'
}

// 获取肌酐状态
const getCreatinineStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 45) return '偏低（<45 μmol/L）'
  if (val > 104) return '偏高（>104 μmol/L）'
  return '正常（45-104 μmol/L）'
}

// 获取尿素氮状态
const getBloodUreaNitrogenStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 3.2) return '偏低（<3.2 mmol/L）'
  if (val > 7.1) return '偏高（>7.1 mmol/L）'
  return '正常（3.2-7.1 mmol/L）'
}

// 获取eGFR状态
const getEgfrStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 90) return '偏低（<90 mL/min/1.73m²）'
  return '正常（≥90 mL/min/1.73m²）'
}

// 获取尿酸状态
const getUricAcidStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 155) return '偏低（<155 μmol/L）'
  if (val > 428) return '偏高（>428 μmol/L）'
  return '正常（155-428 μmol/L）'
}

// 获取总胆固醇状态
const getTotalCholesterolStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 3.0) return '偏低（<3.0 mmol/L）'
  if (val > 5.2) return '偏高（>5.2 mmol/L）'
  return '正常（3.0-5.2 mmol/L）'
}

// 获取甘油三酯状态
const getTriglyceridesStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 0.5) return '偏低（<0.5 mmol/L）'
  if (val > 1.7) return '偏高（>1.7 mmol/L）'
  return '正常（0.5-1.7 mmol/L）'
}

// 获取低密度脂蛋白胆固醇状态
const getLdlCholesterolStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 2.0) return '偏低（<2.0 mmol/L）'
  if (val > 3.4) return '偏高（>3.4 mmol/L）'
  return '正常（2.0-3.4 mmol/L）'
}

// 获取高密度脂蛋白胆固醇状态
const getHdlCholesterolStatus = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 1.0) return '偏低（<1.0 mmol/L）'
  if (val > 3.0) return '偏高（>3.0 mmol/L）'
  return '正常（1.0-3.0 mmol/L）'
}

// 简化版状态函数（用于备注）
const getBloodPressureStatusSimple = (high, low) => {
  if (!high || !low) return ''
  const highVal = parseInt(high)
  const lowVal = parseInt(low)
  if (highVal >= 160 || lowVal >= 100) return '高血压2级'
  if (highVal >= 140 || lowVal >= 90) return '高血压1级'
  if (highVal < 90 || lowVal < 60) return '低血压'
  return '正常高值'
}

const getHeartRateStatusSimple = (value) => {
  if (!value) return ''
  const val = parseInt(value)
  if (val > 100) return '心动过速'
  if (val < 60) return '心动过缓'
  return '正常'
}

const getBloodSugarStatusSimple = (value, type) => {
  if (!value || !type) return ''
  const val = parseFloat(value)
  if (type === 'fasting') {
    if (val >= 7.0) return '糖尿病'
    if (val >= 6.1) return '空腹血糖受损'
    if (val < 3.9) return '低血糖'
    return '正常'
  } else {
    if (val >= 11.1) return '糖尿病'
    if (val >= 7.8) return '糖耐量受损'
    if (val < 3.9) return '低血糖'
    return '正常'
  }
}

const getTemperatureStatusSimple = (value, type) => {
  if (!value || !type) return ''
  const val = parseFloat(value)
  if (type === 'celsius') {
    if (val >= 38.1) return '发热'
    if (val < 36.0) return '体温偏低'
    return '正常'
  } else {
    const celsius = (val - 32) * 5 / 9
    if (celsius >= 38.1) return '发热'
    if (celsius < 36.0) return '体温偏低'
    return '正常'
  }
}

const getBMIStatusSimple = (bmi) => {
  if (!bmi) return ''
  const val = parseFloat(bmi)
  if (val >= 28) return '肥胖'
  if (val >= 24) return '超重'
  if (val < 18.5) return '体重过轻'
  return '正常'
}

const getWhiteBloodCellStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 3.5) return '偏低'
  if (val > 9.5) return '偏高'
  return '正常'
}

const getHemoglobinStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 110) return '偏低'
  if (val > 160) return '偏高'
  return '正常'
}

const getHematocritStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 35) return '偏低'
  if (val > 50) return '偏高'
  return '正常'
}

const getPlateletCountStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 125) return '偏低'
  if (val > 350) return '偏高'
  return '正常'
}

const getAltStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 7) return '偏低'
  if (val > 40) return '偏高'
  return '正常'
}

const getAstStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 13) return '偏低'
  if (val > 35) return '偏高'
  return '正常'
}

const getGgtStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 7) return '偏低'
  if (val > 60) return '偏高'
  return '正常'
}

const getAlpStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 45) return '偏低'
  if (val > 125) return '偏高'
  return '正常'
}

const getTotalProteinStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 60) return '偏低'
  if (val > 83) return '偏高'
  return '正常'
}

const getAlbuminStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 40) return '偏低'
  if (val > 55) return '偏高'
  return '正常'
}

const getCreatinineStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 45) return '偏低'
  if (val > 104) return '偏高'
  return '正常'
}

const getBloodUreaNitrogenStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 3.2) return '偏低'
  if (val > 7.1) return '偏高'
  return '正常'
}

const getEgfrStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 90) return '偏低'
  if (val > 120) return '偏高'
  return '正常'
}

const getUricAcidStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 150) return '偏低'
  if (val > 420) return '偏高'
  return '正常'
}

const getTotalCholesterolStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val > 5.2) return '偏高'
  return '正常'
}

const getTriglyceridesStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val > 1.7) return '偏高'
  return '正常'
}

const getLdlCholesterolStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val > 3.4) return '偏高'
  return '正常'
}

const getHdlCholesterolStatusSimple = (value) => {
  if (!value) return ''
  const val = parseFloat(value)
  if (val < 1.0) return '偏低'
  if (val > 3.0) return '偏高'
  return '正常'
}

// 评估当前表单健康状态
const evaluateCurrentHealthStatus = () => {
  currentHealthEvaluation.value = evaluateHealthStatus(healthRecordForm)
  
  // 自动设置健康状态
  if (currentHealthEvaluation.value.status === 1) {
    healthRecordForm.healthStatus = 1
  } else {
    healthRecordForm.healthStatus = 0
  }
}

// 获取健康状态文本
const getHealthStatusText = (status) => {
  switch (status) {
    case 0:
      return '正常'
    case 1:
    case 2:
      return '异常'
    default:
      return '未知'
  }
}

// 获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-normal'
    case 1:
    case 2: return 'status-abnormal'
    default: return ''
  }
}

// 获取体检项目汇总状态
const getLabTestSummary = (record, type) => {
  const hasData = (value) => value !== null && value !== undefined && value !== ''
  const isAbnormal = (value, min, max, reverse = false) => {
    if (value === null || value === undefined || value === '') return false
    const val = parseFloat(value)
    if (reverse) {
      return val < min
    }
    return val < min || val > max
  }
  
  if (type === 'basic') {
    const hasBasicData = hasData(record.bloodPressure) || hasData(record.heartRate) || 
                         hasData(record.bloodSugar) || hasData(record.weight) || 
                         hasData(record.height) || hasData(record.temperature)
    if (!hasBasicData) return '-'
    
    const isBasicAbnormal = isAbnormal(record.heartRate, 60, 100) ||
                            isAbnormal(record.bloodSugar, 3.9, 6.1) ||
                            isAbnormal(record.temperature, 36.0, 37.3) ||
                            isBloodPressureAbnormal(record.bloodPressure) ||
                            isBMIAbnormal(record.weight, record.height)
    return isBasicAbnormal ? '异常' : '正常'
  } else if (type === 'blood') {
    const hasBloodData = hasData(record.whiteBloodCell) || hasData(record.hemoglobin) || 
                         hasData(record.hematocrit) || hasData(record.plateletCount)
    if (!hasBloodData) return '-'
    
    const isBloodAbnormal = isAbnormal(record.whiteBloodCell, 3.5, 9.5) ||
                            isAbnormal(record.hemoglobin, 110, 160) ||
                            isAbnormal(record.hematocrit, 35, 50) ||
                            isAbnormal(record.plateletCount, 125, 350)
    return isBloodAbnormal ? '异常' : '正常'
  } else if (type === 'liver') {
    const hasLiverData = hasData(record.alt) || hasData(record.ast) || hasData(record.ggt) || 
                         hasData(record.alp) || hasData(record.albumin)
    if (!hasLiverData) return '-'
    
    const isLiverAbnormal = isAbnormal(record.alt, 7, 40) ||
                            isAbnormal(record.ast, 13, 35) ||
                            isAbnormal(record.ggt, 7, 60) ||
                            isAbnormal(record.alp, 45, 125) ||
                            isAbnormal(record.albumin, 40, 55)
    return isLiverAbnormal ? '异常' : '正常'
  } else if (type === 'kidney') {
    const hasKidneyData = hasData(record.creatinine) || hasData(record.bloodUreaNitrogen) || 
                          hasData(record.egfr) || hasData(record.uricAcid)
    if (!hasKidneyData) return '-'
    
    const isKidneyAbnormal = isAbnormal(record.creatinine, 45, 104) ||
                             isAbnormal(record.bloodUreaNitrogen, 3.2, 7.1) ||
                             isAbnormal(record.egfr, 90, 200, true) ||
                             isAbnormal(record.uricAcid, 155, 428)
    return isKidneyAbnormal ? '异常' : '正常'
  } else if (type === 'lipid') {
    const hasLipidData = hasData(record.totalCholesterol) || hasData(record.triglycerides) || 
                         hasData(record.ldlCholesterol) || hasData(record.hdlCholesterol)
    if (!hasLipidData) return '-'
    
    const isLipidAbnormal = isAbnormal(record.totalCholesterol, 0, 5.2) ||
                            isAbnormal(record.triglycerides, 0, 1.7) ||
                            isAbnormal(record.ldlCholesterol, 0, 3.4) ||
                            isAbnormal(record.hdlCholesterol, 1.0, 3.0, true)
    return isLipidAbnormal ? '异常' : '正常'
  }
  return '-'
}

// 判断血压是否异常
const isBloodPressureAbnormal = (bloodPressure) => {
  if (!bloodPressure) return false
  const parts = bloodPressure.split('/')
  if (parts.length !== 2) return false
  
  const systolic = parseFloat(parts[0])
  const diastolic = parseFloat(parts[1])
  
  if (isNaN(systolic) || isNaN(diastolic)) return false
  
  return systolic < 90 || systolic > 140 || diastolic < 60 || diastolic > 90
}

// 获取血压值的样式类
const getBloodPressureValueClass = (bloodPressure) => {
  return isBloodPressureAbnormal(bloodPressure) ? 'lab-value-abnormal' : 'lab-value-normal'
}

// 计算BMI
const calculateBMI = (weight, height) => {
  if (!weight || !height) return null
  const weightKg = parseFloat(weight)
  const heightM = parseFloat(height) / 100
  
  if (isNaN(weightKg) || isNaN(heightM) || heightM === 0) return null
  
  return (weightKg / (heightM * heightM)).toFixed(1)
}

// 判断BMI是否异常
const isBMIAbnormal = (weight, height) => {
  const bmi = calculateBMI(weight, height)
  if (!bmi) return false
  const bmiValue = parseFloat(bmi)
  return bmiValue < 18.5 || bmiValue >= 24
}

// 获取BMI值的样式类
const getBMIValueClass = (weight, height) => {
  const bmi = calculateBMI(weight, height)
  if (!bmi) return ''
  const bmiValue = parseFloat(bmi)
  
  if (bmiValue < 18.5 || bmiValue >= 28) return 'lab-value-abnormal'
  if (bmiValue >= 24) return 'lab-value-warning'
  return 'lab-value-normal'
}

// 获取体检项目状态样式类
const getLabTestStatusClass = (record, type) => {
  const isAbnormal = (value, min, max, reverse = false) => {
    if (value === null || value === undefined || value === '') return false
    const val = parseFloat(value)
    if (reverse) {
      return val < min
    }
    return val < min || val > max
  }
  
  let abnormal = false
  
  if (type === 'basic') {
    abnormal = isAbnormal(record.heartRate, 60, 100) ||
              isAbnormal(record.bloodSugar, 3.9, 6.1) ||
              isAbnormal(record.temperature, 36.0, 37.3) ||
              isBloodPressureAbnormal(record.bloodPressure) ||
              isBMIAbnormal(record.weight, record.height)
  } else if (type === 'blood') {
    abnormal = isAbnormal(record.whiteBloodCell, 3.5, 9.5) ||
              isAbnormal(record.hemoglobin, 110, 160) ||
              isAbnormal(record.hematocrit, 35, 50) ||
              isAbnormal(record.plateletCount, 125, 350)
  } else if (type === 'liver') {
    abnormal = isAbnormal(record.alt, 7, 40) ||
              isAbnormal(record.ast, 13, 35) ||
              isAbnormal(record.ggt, 7, 60) ||
              isAbnormal(record.alp, 45, 125) ||
              isAbnormal(record.albumin, 40, 55)
  } else if (type === 'kidney') {
    abnormal = isAbnormal(record.creatinine, 45, 104) ||
              isAbnormal(record.bloodUreaNitrogen, 3.2, 7.1) ||
              isAbnormal(record.egfr, 90, 200, true) ||
              isAbnormal(record.uricAcid, 155, 428)
  } else if (type === 'lipid') {
    abnormal = isAbnormal(record.totalCholesterol, 0, 5.2) ||
              isAbnormal(record.triglycerides, 0, 1.7) ||
              isAbnormal(record.ldlCholesterol, 0, 3.4) ||
              isAbnormal(record.hdlCholesterol, 1.0, 3.0, true)
  }
  
  return abnormal ? 'lab-test-abnormal' : 'lab-test-normal'
}

// 获取体检项目值样式类
const getLabTestValueClass = (value, min, max, reverse = false) => {
  if (value === null || value === undefined || value === '') return ''
  const val = parseFloat(value)
  
  if (reverse) {
    return val < min ? 'lab-value-abnormal' : 'lab-value-normal'
  }
  return (val < min || val > max) ? 'lab-value-abnormal' : 'lab-value-normal'
}

// 检查是否有血常规数据
const hasBloodTestData = () => {
  const record = currentHealthRecord.value
  return record && (
    record.whiteBloodCell || record.hemoglobin || 
    record.hematocrit || record.plateletCount
  )
}

// 检查是否有肝功能数据
const hasLiverTestData = () => {
  const record = currentHealthRecord.value
  return record && (
    record.alt || record.ast || record.ggt || 
    record.alp || record.albumin
  )
}

// 检查是否有肾功能数据
const hasKidneyTestData = () => {
  const record = currentHealthRecord.value
  return record && (
    record.creatinine || record.bloodUreaNitrogen || 
    record.egfr || record.uricAcid
  )
}

// 检查是否有血脂数据
const hasLipidTestData = () => {
  const record = currentHealthRecord.value
  return record && (
    record.totalCholesterol || record.triglycerides || 
    record.ldlCholesterol || record.hdlCholesterol
  )
}

// 判断血压是否异常（用于表单）- 超过理想状态（正常高值及以上）就显示加号
const isBloodPressureAbnormalForForm = (high, low) => {
  if (!high || !low) return false
  const highVal = parseInt(high)
  const lowVal = parseInt(low)
  // 低血压、正常高值、高血压都显示加号
  return highVal < 90 || highVal >= 130 || lowVal < 60 || lowVal >= 85
}

// 判断心率是否异常（用于表单）
const isHeartRateAbnormal = (value) => {
  if (!value) return false
  const val = parseInt(value)
  return val < 60 || val > 100
}

// 判断血糖是否异常（用于表单）
const isBloodSugarAbnormal = (value, type) => {
  if (!value || !type) return false
  const val = parseFloat(value)
  if (type === 'fasting') {
    return val < 3.9 || val > 6.1
  } else if (type === 'postprandial') {
    return val < 3.9 || val > 7.8
  }
  return false
}

// 判断体温是否异常（用于表单）
const isTemperatureAbnormal = (value, type) => {
  if (!value || !type) return false
  const val = parseFloat(value)
  if (type === 'axillary') {
    return val < 36.0 || val > 37.3
  } else if (type === 'oral') {
    return val < 36.2 || val > 37.5
  }
  return false
}

// 判断BMI是否异常（用于表单）
const isBMIAbnormalForForm = (weight, height) => {
  const bmi = calculateBMI(weight, height)
  if (!bmi) return false
  const bmiValue = parseFloat(bmi)
  return bmiValue < 18.5 || bmiValue >= 24
}

// 判断白细胞是否异常（用于表单）
const isWhiteBloodCellAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 3.5 || val > 9.5
}

// 判断血红蛋白是否异常（用于表单）
const isHemoglobinAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 110 || val > 160
}

// 判断红细胞压积是否异常（用于表单）
const isHematocritAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 35 || val > 50
}

// 判断血小板是否异常（用于表单）
const isPlateletCountAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 125 || val > 350
}

// 判断谷丙转氨酶是否异常（用于表单）
const isAltAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 7 || val > 40
}

// 判断谷草转氨酶是否异常（用于表单）
const isAstAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 13 || val > 35
}

// 判断γ-谷氨酰转移酶是否异常（用于表单）
const isGgtAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 7 || val > 60
}

// 判断碱性磷酸酶是否异常（用于表单）
const isAlpAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 45 || val > 125
}

// 判断总蛋白是否异常（用于表单）
const isTotalProteinAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 60 || val > 83
}

// 判断白蛋白是否异常（用于表单）
const isAlbuminAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 40 || val > 55
}

// 判断肌酐是否异常（用于表单）
const isCreatinineAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 45 || val > 104
}

// 判断尿素氮是否异常（用于表单）
const isBloodUreaNitrogenAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 3.2 || val > 7.1
}

// 判断eGFR是否异常（用于表单）
const isEgfrAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 90
}

// 判断尿酸是否异常（用于表单）
const isUricAcidAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 155 || val > 428
}

// 判断总胆固醇是否异常（用于表单）
const isTotalCholesterolAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 3.0 || val > 5.2
}

// 判断甘油三酯是否异常（用于表单）
const isTriglyceridesAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 0.5 || val > 1.7
}

// 判断低密度脂蛋白胆固醇是否异常（用于表单）
const isLdlCholesterolAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 2.0 || val > 3.4
}

// 判断高密度脂蛋白胆固醇是否异常（用于表单）
const isHdlCholesterolAbnormal = (value) => {
  if (!value) return false
  const val = parseFloat(value)
  return val < 1.0 || val > 3.0
}

// 一键添加所有异常指标到备注
const addAllAbnormalItemsToRemark = () => {
  const abnormalItems = []
  
  // 基础指标
  if (isBloodPressureAbnormalForForm(healthRecordForm.bloodPressureHigh, healthRecordForm.bloodPressureLow)) {
    abnormalItems.push(`血压：${getBloodPressureStatusSimple(healthRecordForm.bloodPressureHigh, healthRecordForm.bloodPressureLow)}`)
  }
  if (isHeartRateAbnormal(healthRecordForm.heartRate)) {
    abnormalItems.push(`心率：${getHeartRateStatusSimple(healthRecordForm.heartRate)}`)
  }
  if (isBloodSugarAbnormal(healthRecordForm.bloodSugar, healthRecordForm.bloodSugarType)) {
    abnormalItems.push(`血糖：${getBloodSugarStatusSimple(healthRecordForm.bloodSugar, healthRecordForm.bloodSugarType)}`)
  }
  if (isTemperatureAbnormal(healthRecordForm.temperature, healthRecordForm.temperatureType)) {
    abnormalItems.push(`体温：${getTemperatureStatusSimple(healthRecordForm.temperature, healthRecordForm.temperatureType)}`)
  }
  if (isBMIAbnormalForForm(healthRecordForm.weight, healthRecordForm.height)) {
    const bmi = calculateBMI(healthRecordForm.weight, healthRecordForm.height)
    abnormalItems.push(`BMI：${getBMIStatusSimple(bmi)}`)
  }
  
  // 血常规
  if (isWhiteBloodCellAbnormal(healthRecordForm.whiteBloodCell)) {
    abnormalItems.push(`白细胞计数：${getWhiteBloodCellStatusSimple(healthRecordForm.whiteBloodCell)}`)
  }
  if (isHemoglobinAbnormal(healthRecordForm.hemoglobin)) {
    abnormalItems.push(`血红蛋白：${getHemoglobinStatusSimple(healthRecordForm.hemoglobin)}`)
  }
  if (isHematocritAbnormal(healthRecordForm.hematocrit)) {
    abnormalItems.push(`红细胞压积：${getHematocritStatusSimple(healthRecordForm.hematocrit)}`)
  }
  if (isPlateletCountAbnormal(healthRecordForm.plateletCount)) {
    abnormalItems.push(`血小板计数：${getPlateletCountStatusSimple(healthRecordForm.plateletCount)}`)
  }
  
  // 肝功能
  if (isAltAbnormal(healthRecordForm.alt)) {
    abnormalItems.push(`谷丙转氨酶：${getAltStatusSimple(healthRecordForm.alt)}`)
  }
  if (isAstAbnormal(healthRecordForm.ast)) {
    abnormalItems.push(`谷草转氨酶：${getAstStatusSimple(healthRecordForm.ast)}`)
  }
  if (isGgtAbnormal(healthRecordForm.ggt)) {
    abnormalItems.push(`γ-谷氨酰转移酶：${getGgtStatusSimple(healthRecordForm.ggt)}`)
  }
  if (isAlpAbnormal(healthRecordForm.alp)) {
    abnormalItems.push(`碱性磷酸酶：${getAlpStatusSimple(healthRecordForm.alp)}`)
  }
  if (isTotalProteinAbnormal(healthRecordForm.totalProtein)) {
    abnormalItems.push(`总蛋白：${getTotalProteinStatusSimple(healthRecordForm.totalProtein)}`)
  }
  if (isAlbuminAbnormal(healthRecordForm.albumin)) {
    abnormalItems.push(`白蛋白：${getAlbuminStatusSimple(healthRecordForm.albumin)}`)
  }
  
  // 肾功能
  if (isCreatinineAbnormal(healthRecordForm.creatinine)) {
    abnormalItems.push(`肌酐：${getCreatinineStatusSimple(healthRecordForm.creatinine)}`)
  }
  if (isBloodUreaNitrogenAbnormal(healthRecordForm.bloodUreaNitrogen)) {
    abnormalItems.push(`尿素氮：${getBloodUreaNitrogenStatusSimple(healthRecordForm.bloodUreaNitrogen)}`)
  }
  if (isEgfrAbnormal(healthRecordForm.egfr)) {
    abnormalItems.push(`eGFR：${getEgfrStatusSimple(healthRecordForm.egfr)}`)
  }
  if (isUricAcidAbnormal(healthRecordForm.uricAcid)) {
    abnormalItems.push(`尿酸：${getUricAcidStatusSimple(healthRecordForm.uricAcid)}`)
  }
  
  // 血脂
  if (isTotalCholesterolAbnormal(healthRecordForm.totalCholesterol)) {
    abnormalItems.push(`总胆固醇：${getTotalCholesterolStatusSimple(healthRecordForm.totalCholesterol)}`)
  }
  if (isTriglyceridesAbnormal(healthRecordForm.triglycerides)) {
    abnormalItems.push(`甘油三酯：${getTriglyceridesStatusSimple(healthRecordForm.triglycerides)}`)
  }
  if (isLdlCholesterolAbnormal(healthRecordForm.ldlCholesterol)) {
    abnormalItems.push(`低密度脂蛋白胆固醇：${getLdlCholesterolStatusSimple(healthRecordForm.ldlCholesterol)}`)
  }
  if (isHdlCholesterolAbnormal(healthRecordForm.hdlCholesterol)) {
    abnormalItems.push(`高密度脂蛋白胆固醇：${getHdlCholesterolStatusSimple(healthRecordForm.hdlCholesterol)}`)
  }
  
  if (abnormalItems.length === 0) {
    ElMessage.info('没有异常指标需要添加')
    return
  }
  
  const abnormalText = abnormalItems.join('；')
  if (healthRecordForm.remark) {
    healthRecordForm.remark = healthRecordForm.remark + '；' + abnormalText
  } else {
    healthRecordForm.remark = abnormalText
  }
  
  ElMessage.success(`已添加 ${abnormalItems.length} 项异常指标到备注`)
}

// 初始化
onMounted(async () => {
  await fetchUsers()
  await fetchHealthRecords()
})
</script>

<style scoped>
/* 统计卡片样式 */
.system-stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.system-stats-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #ebeef5;
}

.system-stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.system-stats-title {
  font-size: 14px;
  color: #606266;
}

.system-stats-icon {
  color: #409EFF;
  font-size: 20px;
}

.system-stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.system-stats-change {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.system-stats-change.positive {
  color: #67C23A;
}

/* 健康状态标签样式 */
.status-normal {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-abnormal {
  background-color: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffa39e;
}

/* 健康记录详情卡片样式 */
.health-detail-container {
  display: flex;
  flex-direction: column;
  gap: var(--system-space-lg);
}

.detail-card {
  background-color: var(--system-bg-white);
  border-radius: var(--system-border-radius-lg);
  box-shadow: var(--system-shadow-sm);
  overflow: hidden;
  transition: all 0.3s ease;
}

.detail-card:hover {
  box-shadow: var(--system-shadow-md);
}

.detail-card-header {
  padding: var(--system-space-lg);
  border-bottom: 1px solid var(--system-border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-card-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--system-text-primary);
}

.detail-card-body {
  padding: var(--system-space-lg);
}

.detail-row {
  display: flex;
  margin-bottom: var(--system-space-md);
  align-items: center;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  width: 120px;
  font-weight: 500;
  color: var(--system-text-secondary);
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: var(--system-text-primary);
  display: flex;
  align-items: center;
}

.detail-value.lab-value-normal {
  color: #67C23A;
  font-weight: 600;
}

.detail-value.lab-value-abnormal {
  color: #F56C6C;
  font-weight: 600;
}

.detail-value.lab-value-warning {
  color: #E6A23C;
  font-weight: 600;
}

/* 对话框样式 */
.system-modal-overlay {
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

.system-modal {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  width: 500px;
  max-width: 90%;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  animation: modalFadeIn 0.3s ease-out;
}

.system-modal-large {
  width: 600px;
  max-width: 95%;
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.system-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.system-modal-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.system-modal-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #909399;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.system-modal-close:hover {
  background-color: #f2f6fc;
  color: #606266;
}

.system-modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.system-modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 表单样式 */
.system-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.system-form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 表单值容器样式 */
.value-container {
  background-color: #f5f7fa;
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  color: #303133;
  font-weight: 500;
}

.system-form-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.system-form-input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
  transition: border-color 0.2s;
}

.system-form-input:focus {
  outline: none;
  border-color: #409eff;
}

.system-form-input:disabled {
  background-color: #f5f7fa;
  color: #c0c4cc;
  cursor: not-allowed;
}

/* 状态标签 */
.status-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

/* 分页样式 */
.system-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--system-space-md) var(--system-space-lg);
  border-top: 1px solid var(--system-border-light);
  flex-wrap: wrap;
  gap: var(--system-space-md);
}

.system-pagination-info {
  color: var(--system-text-secondary);
  font-size: var(--system-font-size-sm);
  display: flex;
  align-items: center;
  gap: var(--system-space-md);
}

.system-pagination-size-selector {
  display: flex;
  align-items: center;
  gap: var(--system-space-xs);
  font-size: var(--system-font-size-sm);
}

.system-pagination-size-selector select {
  padding: var(--system-space-xs) var(--system-space-sm);
  border: 1px solid var(--system-border-color);
  border-radius: var(--system-border-radius-md);
  font-size: var(--system-font-size-sm);
  min-width: 80px;
}

.system-pagination-controls {
  display: flex;
  align-items: center;
  gap: var(--system-space-xs);
}

.system-pagination-jump {
  display: flex;
  align-items: center;
  gap: var(--system-space-xs);
  margin-left: var(--system-space-md);
  font-size: var(--system-font-size-sm);
}

.system-pagination-jump input {
  width: 50px;
  padding: var(--system-space-xs);
  border: 1px solid var(--system-border-color);
  border-radius: var(--system-border-radius-md);
  text-align: center;
  font-size: var(--system-font-size-sm);
}

.system-pagination-btn {
  min-width: 32px;
  height: 32px;
  padding: 0 var(--system-space-sm);
  border: 1px solid var(--system-border-color);
  background-color: var(--system-bg-white);
  color: var(--system-text-primary);
  border-radius: var(--system-border-radius-md);
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: var(--system-font-size-sm);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
}

.system-pagination-btn:hover:not(.system-pagination-btn-disabled):not(.system-pagination-btn-active) {
  border-color: var(--system-primary-color);
  color: var(--system-primary-color);
  background-color: var(--system-primary-light);
}

.system-pagination-btn-active {
  background-color: var(--system-primary-color);
  border-color: var(--system-primary-color);
  color: white;
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.3);
}

.system-pagination-btn-disabled {
  background-color: var(--system-bg-light);
  color: var(--system-text-disabled);
  border-color: var(--system-border-light);
  cursor: not-allowed;
}

.system-pagination-ellipsis {
  padding: 0 var(--system-space-sm);
  color: var(--system-text-tertiary);
  font-size: var(--system-font-size-sm);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
}

/* 分页器加载状态 */
.system-pagination-loading {
  position: relative;
  pointer-events: none;
}

.system-pagination-loading::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 16px;
  height: 16px;
  margin: -8px 0 0 -8px;
  border: 2px solid var(--system-border-color);
  border-top-color: var(--system-primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .system-pagination {
    flex-direction: column;
    align-items: stretch;
    gap: var(--system-space-sm);
  }
  
  .system-pagination-info {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .system-pagination-controls {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .system-pagination-jump {
    margin-left: 0;
    margin-top: var(--system-space-sm);
    justify-content: center;
  }
  
  .system-pagination-btn {
    min-width: 36px;
    height: 36px;
    font-size: var(--system-font-size-md);
  }
}

/* 血压输入样式 */
.blood-pressure-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.blood-pressure-input input {
  flex: 1;
}

.blood-pressure-separator {
  font-size: 18px;
  font-weight: bold;
  color: #606266;
}

/* 血糖输入样式 */
.blood-sugar-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.blood-sugar-input input,
.blood-sugar-input select {
  flex: 1;
}

.blood-sugar-separator {
  font-size: 18px;
  font-weight: bold;
  color: #606266;
}

/* 体温类型输入样式 */
.temperature-type-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.temperature-type-input input,
.temperature-type-input select {
  flex: 1;
}

.temperature-type-separator {
  font-size: 18px;
  font-weight: bold;
  color: #606266;
}

/* 身高体重输入样式 */
.height-weight-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.height-weight-input input {
  flex: 1;
}

.height-weight-separator {
  font-size: 18px;
  font-weight: bold;
  color: #606266;
}

/* 健康状态提示样式 */
.health-status-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #409EFF;
  background-color: #f0f9ff;
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid #b3d8ff;
}



/* 测试项目输入框容器样式 */
.test-item-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.test-item-input input {
  flex: 1;
}

/* 心率输入框容器样式 */
.heart-rate-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.heart-rate-input input {
  flex: 1;
}

/* 备注标签样式 */
.remark-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.add-all-abnormal-btn {
  margin-left: 8px;
}

/* 健康体检报告样式 */
.health-report-modal {
  width: 800px;
  max-width: 95%;
  max-height: 90vh;
}

.health-report-header {
  background: #ffffff;
  color: #333;
  padding: 24px 32px;
  border-radius: 8px 8px 0 0;
  border-bottom: 1px solid #e0e0e0;
}

.health-report-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.health-report-title h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0;
}

.close-button {
  font-size: 28px;
  font-weight: 300;
  color: #666;
  cursor: pointer;
  line-height: 1;
  padding: 4px;
  transition: color 0.3s;
}

.close-button:hover {
  color: #333;
}

.health-report-info {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-label {
  font-size: 14px;
  color: #666;
  font-weight: 400;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.info-value.status-tag {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
}

.info-value.status-tag.status-normal {
  background: #f0f9ff;
  color: #1890ff;
}

.info-value.status-tag.status-abnormal {
  background: #fff1f0;
  color: #ff4d4f;
}

.health-report-body {
  background-color: #ffffff;
  padding: 32px;
  border-radius: 0 0 8px 8px;
  overflow-y: auto;
  max-height: calc(90vh - 200px);
}

.report-section {
  margin-bottom: 24px;
}

.report-section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e0e0e0;
  display: inline-block;
}

.report-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #e0e0e0;
}

.report-table thead {
  background: #f5f5f5;
  color: #333;
}

.report-table thead th {
  padding: 12px 16px;
  text-align: left;
  font-weight: 500;
  font-size: 14px;
  border-right: 1px solid #e0e0e0;
}

.report-table thead th:last-child {
  border-right: none;
}

.report-table tbody tr {
  border-bottom: 1px solid #e0e0e0;
}

.report-table tbody tr:last-child {
  border-bottom: none;
}

.report-table tbody tr:hover {
  background-color: #fafafa;
}

.report-table td {
  padding: 12px 16px;
  font-size: 14px;
  color: #333;
}

.report-table .col-item {
  width: 25%;
  font-weight: 500;
  color: #333;
}

.report-table .col-result {
  width: 20%;
  text-align: center;
}

.report-table .col-result.lab-value-normal {
  color: #1890ff;
  font-weight: 500;
}

.report-table .col-result.lab-value-abnormal {
  color: #ff4d4f;
  font-weight: 500;
}

.report-table .col-result.lab-value-warning {
  color: #faad14;
  font-weight: 500;
}

.report-table .col-unit {
  width: 15%;
  color: #666;
  font-size: 13px;
}

.report-table .col-reference {
  width: 25%;
  color: #666;
  font-size: 13px;
}

.report-table .col-status {
  width: 15%;
  text-align: center;
  font-weight: 500;
}

.report-table .col-status.lab-value-normal {
  color: #1890ff;
}

.report-table .col-status.lab-value-abnormal {
  color: #ff4d4f;
}

.report-table .col-status.lab-value-warning {
  color: #faad14;
}

.report-remark {
  background: white;
  padding: 16px;
  border-radius: 4px;
  color: #333;
  font-size: 14px;
  line-height: 1.6;
  border: 1px solid #e0e0e0;
}

/* 操作按钮组样式 */
.action-btn-group {
  display: flex;
  gap: 8px;
}

.btn {
  padding: 4px 12px;
  border: 1px solid transparent;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.btn-primary {
  background-color: #1890ff;
  color: white;
  border-color: #1890ff;
}

.btn-primary:hover {
  background-color: #40a9ff;
  border-color: #40a9ff;
}

.btn-default {
  background-color: #fff;
  color: #333;
  border-color: #d9d9d9;
}

.btn-default:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.btn-danger {
  background-color: #ff4d4f;
  color: white;
  border-color: #ff4d4f;
}

.btn-danger:hover {
  background-color: #ff7875;
  border-color: #ff7875;
}

.btn-info {
  background-color: #17a2b8;
  color: white;
  border-color: #17a2b8;
}

.btn-info:hover {
  background-color: #138496;
  border-color: #138496;
}

/* 表单分区样式 */
.form-section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 24px 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #409EFF;
}

.form-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.form-section .system-form-item.full-width {
  grid-column: 1 / -1;
}

@media (max-width: 768px) {
  .form-section {
    grid-template-columns: 1fr;
  }
}

/* 体检项目详情样式 */
.lab-test-detail {
  padding: 12px;
}

.lab-test-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.lab-test-item:last-child {
  margin-bottom: 0;
}

.lab-test-label {
  width: 120px;
  color: #606266;
  font-weight: 500;
}

.lab-test-unit {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

.lab-value-normal {
  color: #67C23A;
  font-weight: 600;
}

.lab-value-abnormal {
  color: #F56C6C;
  font-weight: 600;
}

.lab-value-warning {
  color: #E6A23C;
  font-weight: 600;
}

.lab-test-normal {
  color: #67C23A;
  font-weight: 600;
  cursor: pointer;
}

.lab-test-abnormal {
  color: #F56C6C;
  font-weight: 600;
  cursor: pointer;
}

.detail-value-hint {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}
</style>