<template>
  <div class="system-page">
    <div class="system-page-title">
      <div class="system-page-title-text">
        <span class="system-page-title-icon">🛠️</span>
        社区服务
      </div>
      <div class="system-page-title-actions">
        <el-button type="primary" size="default" @click="showAddServiceDialog = true">
          <span class="system-button-icon">+</span>
          新增服务
        </el-button>
      </div>
    </div>

    <!-- 服务统计卡片 -->
    <div class="system-stats-container">
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">服务总数</div>
          <div class="system-stats-icon">
            <el-icon><List /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.totalServices || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">启用服务</div>
          <div class="system-stats-icon">
            <el-icon><Check /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.activeServices || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">未启用服务</div>
          <div class="system-stats-icon">
            <el-icon><Close /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.inactiveServices || 0 }}</div>
      </div>
      
      <div class="system-stats-card">
        <div class="system-stats-header">
          <div class="system-stats-title">已下架服务</div>
          <div class="system-stats-icon">
            <el-icon><Warning /></el-icon>
          </div>
        </div>
        <div class="system-stats-value">{{ statsData.disabledServices || 0 }}</div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="system-search-bar">
      <div class="system-search-form">
        <div class="system-search-item">
          <div class="system-form-label">服务名称</div>
          <input class="system-form-input" type="text" v-model="searchForm.name" placeholder="请输入服务名称">
        </div>
        <div class="system-search-item">
          <div class="system-form-label">服务类别</div>
          <select class="system-form-input" v-model="searchForm.categoryId">
            <option value="">全部分类</option>
            <option v-for="category in serviceCategories" :key="category.id" :value="category.id">
              {{ category.name }}
            </option>
          </select>
        </div>
        <div class="system-search-item">
          <div class="system-form-label">服务状态</div>
          <select class="system-form-input" v-model="searchForm.status">
            <option value="">全部状态</option>
            <option value="0">未启用</option>
            <option value="1">启用中</option>
            <option value="2">已下架</option>
          </select>
        </div>
        <div class="system-search-actions">
          <el-button type="primary" size="default" @click="searchServices">搜索</el-button>
          <el-button size="default" @click="resetSearch">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 服务列表 -->
    <div class="system-table-container">
      <table class="system-table">
        <thead>
          <tr>
            <th>服务名称</th>
            <th>服务类别</th>
            <th>服务价格</th>
            <th>服务时长</th>
            <th>服务状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="service in services" :key="service.id">
            <td>{{ service.name }}</td>
            <td>{{ getCategoryName(service.categoryId) }}</td>
            <td>￥{{ formatPrice(service.price) }}</td>
            <td>{{ service.duration || '-' }} 分钟</td>
            <td>
              <span v-if="service.status === SERVICE_STATUS.INACTIVE" class="status-tag status-inactive">未启用</span>
              <span v-else-if="service.status === SERVICE_STATUS.ACTIVE" class="status-tag status-active">启用中</span>
              <span v-else-if="service.status === SERVICE_STATUS.DISABLED" class="status-tag status-disabled">已下架</span>
            </td>
            <td>
              <div class="action-btn-group">
                <!-- 查看按钮始终显示 -->
                <el-button size="small" type="info" @click="viewService(service.id)">查看</el-button>
                
                <!-- 所有状态都可以编辑和删除 -->
                <el-button size="small" type="primary" @click="editService(service.id)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteService(service.id)">删除</el-button>
                
                <!-- 根据状态显示不同的状态流转按钮 -->
                <!-- 未启用状态 -->
                <template v-if="service.status === SERVICE_STATUS.INACTIVE">
                  <el-button size="small" type="success" @click="enableService(service.id)">启用</el-button>
                </template>
                
                <!-- 启用中状态 -->
                <template v-else-if="service.status === SERVICE_STATUS.ACTIVE">
                  <el-button size="small" type="warning" @click="disableService(service.id)">下架</el-button>
                </template>
                
                <!-- 已下架状态 -->
                <template v-else-if="service.status === SERVICE_STATUS.DISABLED">
                  <el-button size="small" type="success" @click="enableService(service.id)">启用</el-button>
                </template>
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

    <!-- 添加服务对话框 -->
    <div v-if="showAddServiceDialog" class="system-modal-overlay" @click.self="closeServiceDialog">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">添加服务</div>
          <button class="system-modal-close" @click="closeServiceDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">服务名称</div>
              <input class="system-form-input" type="text" v-model="serviceForm.name" placeholder="请输入服务名称">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">服务类别</div>
              <select class="system-form-input" v-model="serviceForm.categoryId">
                <option value="">请选择服务类别</option>
                <option v-for="category in serviceCategories" :key="category.id" :value="category.id">
                  {{ category.name }}
                </option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">服务价格</div>
              <input class="system-form-input" type="number" step="0.01" v-model="serviceForm.price" placeholder="请输入服务价格">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">服务时长（分钟）</div>
              <input class="system-form-input" type="number" v-model="serviceForm.duration" placeholder="请输入服务时长">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">服务描述</div>
              <textarea class="system-form-input" rows="3" v-model="serviceForm.description" placeholder="请输入服务描述"></textarea>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">服务状态</div>
              <select class="system-form-input" v-model="serviceForm.status">
                <option value="0">未启用</option>
                <option value="1">启用中</option>
                <option value="2">已下架</option>
              </select>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <el-button size="default" @click="closeServiceDialog">取消</el-button>
          <el-button type="primary" size="default" @click="saveService">保存</el-button>
        </div>
      </div>
    </div>

    <!-- 编辑服务对话框 -->
    <div v-if="showEditServiceDialog" class="system-modal-overlay" @click.self="closeServiceDialog">
      <div class="system-modal">
        <div class="system-modal-header">
          <div class="system-modal-title">编辑服务</div>
          <button class="system-modal-close" @click="closeServiceDialog">×</button>
        </div>
        <div class="system-modal-body">
          <div class="system-form">
            <div class="system-form-item">
              <div class="system-form-label">服务名称</div>
              <input class="system-form-input" type="text" v-model="serviceForm.name" placeholder="请输入服务名称">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">服务类别</div>
              <select class="system-form-input" v-model="serviceForm.categoryId">
                <option value="">请选择服务类别</option>
                <option v-for="category in serviceCategories" :key="category.id" :value="category.id">
                  {{ category.name }}
                </option>
              </select>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">服务价格</div>
              <input class="system-form-input" type="number" step="0.01" v-model="serviceForm.price" placeholder="请输入服务价格">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">服务时长（分钟）</div>
              <input class="system-form-input" type="number" v-model="serviceForm.duration" placeholder="请输入服务时长">
            </div>
            <div class="system-form-item">
              <div class="system-form-label">服务描述</div>
              <textarea class="system-form-input" rows="3" v-model="serviceForm.description" placeholder="请输入服务描述"></textarea>
            </div>
            <div class="system-form-item">
              <div class="system-form-label">服务状态</div>
              <select class="system-form-input" v-model="serviceForm.status">
                <option value="0">未启用</option>
                <option value="1">启用中</option>
                <option value="2">已下架</option>
              </select>
            </div>
          </div>
        </div>
        <div class="system-modal-footer">
          <el-button size="default" @click="closeServiceDialog">取消</el-button>
          <el-button type="primary" size="default" @click="saveService">保存</el-button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { List, ArrowUp, ArrowDown, Check, Close, Warning } from '@element-plus/icons-vue'
import { servicesApi } from '@/api/services'

const router = useRouter()

// 服务状态常量定义
const SERVICE_STATUS = {
  INACTIVE: 0, // 未启用
  ACTIVE: 1,   // 启用中
  DISABLED: 2  // 已下架
};

// 搜索表单
const searchForm = reactive({
  name: '',
  categoryId: '',
  status: ''
})

// 服务分类列表
const serviceCategories = ref([])

// 服务列表
const services = ref([])
const total = ref(0) // 初始化为0，将根据API返回动态更新
const loading = ref(false)
const allServices = ref([]) // 存储所有服务数据
const servicesError = ref(false) // 服务列表获取错误状态

// 统计数据
const statsData = reactive({
  totalServices: 0,
  activeServices: 0,
  inactiveServices: 0,
  disabledServices: 0,
  lastMonthTotalServices: 0,
  lastMonthActiveServices: 0,
  lastMonthInactiveServices: 0,
  lastMonthDisabledServices: 0
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const jumpPage = ref('')

// 对话框状态
const showAddServiceDialog = ref(false)
const showEditServiceDialog = ref(false)

// 服务表单
const serviceForm = reactive({
  id: '',
  name: '',
  categoryId: '',
  price: '',
  duration: '',
  description: '',
  status: 1
})

// 获取服务分类列表
const fetchServiceCategories = async () => {
  try {
    console.log('开始获取服务分类列表');
    const response = await servicesApi.getServiceCategories();
    console.log('从后端获取的服务分类数据:', JSON.stringify(response));
    
    // 检查不同的数据结构格式
    if (Array.isArray(response)) {
      console.log('响应是数组格式，直接使用');
      serviceCategories.value = response;
    } else if (response && response.data) {
      console.log('响应包含data字段，使用response.data');
      if (Array.isArray(response.data)) {
        serviceCategories.value = response.data;
      } else {
        console.warn('response.data不是数组格式:', typeof response.data);
        serviceCategories.value = [];
      }
    } else {
      console.warn('未接收到有效的分类数据');
      serviceCategories.value = [];
    }
    
    console.log('最终服务分类数据数量:', serviceCategories.value.length);
    console.log('服务分类数据内容:', JSON.stringify(serviceCategories.value));
  } catch (error) {
    console.error('获取服务分类失败:', error);
    serviceCategories.value = [];
  }
};

// 获取分类名称
const getCategoryName = (categoryId) => {
  if (!categoryId) {
    console.log('categoryId为空，返回"未分类"');
    return '未分类';
  }
  
  console.log('根据categoryId:', categoryId, '查找分类名称');
  console.log('当前可用的服务分类数量:', serviceCategories.value.length);
  
  // 确保比较时类型一致
  const category = serviceCategories.value.find(c => 
    String(c.id) === String(categoryId)
  );
  
  const result = category ? category.name : '未知分类';
  console.log('分类查找结果:', result);
  return result;
};

const formatPrice = (price) => {
  if (!price && price !== 0) return '0.00'
  return parseFloat(price).toFixed(2)
};

// 重置服务表单
const resetServiceForm = () => {
  serviceForm.id = '';
  serviceForm.name = '';
  serviceForm.categoryId = '';
  serviceForm.price = '';
  serviceForm.duration = '';
  serviceForm.description = '';
  serviceForm.status = SERVICE_STATUS.ACTIVE;
};

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(total.value / pageSize.value)
})

// 获取服务统计数据
const fetchServiceStats = async () => {
  try {
    loading.value = true
    // 确保服务分类数据已加载
    if (serviceCategories.value.length === 0) {
      await fetchServiceCategories()
    }
    
    // 如果已经获取了服务列表数据，则从数据中计算统计信息
    if (allServices.value && allServices.value.length > 0) {
      // 计算服务总数
      statsData.totalServices = allServices.value.length
      
      // 计算启用服务数量
      statsData.activeServices = allServices.value.filter(service => service.status === SERVICE_STATUS.ACTIVE).length
      
      // 计算未启用服务数量
      statsData.inactiveServices = allServices.value.filter(service => service.status === SERVICE_STATUS.INACTIVE).length
      
      // 计算已下架服务数量
      statsData.disabledServices = allServices.value.filter(service => service.status === SERVICE_STATUS.DISABLED).length
      
      // 计算上月服务数据（简单起见使用90%的当前值）
      statsData.lastMonthTotalServices = Math.floor(statsData.totalServices * 0.9)
      statsData.lastMonthActiveServices = Math.floor(statsData.activeServices * 0.9)
      statsData.lastMonthInactiveServices = Math.floor(statsData.inactiveServices * 0.9)
      statsData.lastMonthDisabledServices = Math.floor(statsData.disabledServices * 0.9)
    } else {
      // 如果没有服务数据，则获取服务数据后再更新统计信息
      await fetchServices()
      if (allServices.value && allServices.value.length > 0) {
        statsData.totalServices = allServices.value.length
        statsData.activeServices = allServices.value.filter(service => service.status === SERVICE_STATUS.ACTIVE).length
        statsData.inactiveServices = allServices.value.filter(service => service.status === SERVICE_STATUS.INACTIVE).length
        statsData.disabledServices = allServices.value.filter(service => service.status === SERVICE_STATUS.DISABLED).length
        
        statsData.lastMonthTotalServices = Math.floor(statsData.totalServices * 0.9)
        statsData.lastMonthActiveServices = Math.floor(statsData.activeServices * 0.9)
        statsData.lastMonthInactiveServices = Math.floor(statsData.inactiveServices * 0.9)
        statsData.lastMonthDisabledServices = Math.floor(statsData.disabledServices * 0.9)
      }
    }
  } catch (error) {
    console.error('获取服务统计数据失败:', error)
    // 不显示错误消息，避免影响用户体验
  } finally {
    loading.value = false
  }
};

// 获取服务列表
const fetchServices = async () => {
  try {
    loading.value = true;
    servicesError.value = false;
    console.log('开始获取服务项目数据...');
    
    // 构建查询参数
    const params = {
      page: 1,
      size: 1000
    }
    
    // 如果有分类筛选条件，添加到参数中
    if (searchForm.categoryId) {
      params.categoryId = searchForm.categoryId
    }
    
    // 使用分页API获取所有服务项目（包括状态为0的数据）
    const response = await servicesApi.getServiceItems(params);
    console.log('API响应数据:', response);
    
    if (response.data && response.data.records) {
      // 获取所有服务记录
      let allServiceRecords = response.data.records;
      console.log('获取到的服务记录总数:', allServiceRecords.length);
      
      // 检查并记录状态为0的服务项目
      const statusZeroItems = allServiceRecords.filter(item => item.status === 0);
      console.log('状态为0的服务项目数量:', statusZeroItems.length);
      if (statusZeroItems.length > 0) {
        console.log('状态为0的服务项目详情:', statusZeroItems);
      }
      
      // 按id排序
      const sortedServices = [...allServiceRecords].sort((a, b) => a.id - b.id);
      console.log('排序后的服务项目数量:', sortedServices.length);
      
      // 保存所有服务数据
      allServices.value = sortedServices;
      
      // 更新统计数据中的服务总数
      statsData.totalServices = sortedServices.length;
      statsData.lastMonthTotalServices = Math.floor(statsData.totalServices * 0.9);
      
      // 应用分页和搜索过滤
      applyPagination();
      
      // 更新总数（使用响应中的total或实际记录数）
      total.value = response.data.total || sortedServices.length;
      console.log('更新后的总数:', total.value);
    } else {
      console.error('API响应格式不正确:', response);
      servicesError.value = true;
    }
  } catch (error) {
    console.error('获取服务项目失败:', error);
    servicesError.value = true;
  } finally {
    loading.value = false;
  }
};

// 应用分页和搜索过滤
const applyPagination = () => {
  let filteredServices = [...allServices.value];
  
  // 应用搜索过滤
  if (searchForm.name.trim()) {
    filteredServices = filteredServices.filter(service => 
      service.name.toLowerCase().includes(searchForm.name.toLowerCase().trim())
    );
  }
  
  if (searchForm.categoryId) {
    filteredServices = filteredServices.filter(service => 
      service.categoryId === searchForm.categoryId
    );
  }
  
  if (searchForm.status !== '') {
    filteredServices = filteredServices.filter(service => 
      service.status === parseInt(searchForm.status)
    );
  }
  
  // 更新总数
  total.value = filteredServices.length;
  
  // 应用分页
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  services.value = filteredServices.slice(start, end);
};

// 搜索服务
const searchServices = () => {
  currentPage.value = 1;
  applyPagination();
};

// 重置搜索
const resetSearch = () => {
  searchForm.name = '';
  searchForm.categoryId = '';
  searchForm.status = '';
  currentPage.value = 1;
  applyPagination();
};

// 获取页码列表
const getPageNumbers = () => {
  const pages = [];
  const total = totalPages.value;
  const current = currentPage.value;
  
  if (total <= 7) {
    for (let i = 1; i <= total; i++) {
      pages.push(i);
    }
  } else {
    if (current <= 4) {
      for (let i = 1; i <= 5; i++) {
        pages.push(i);
      }
      pages.push('...');
      pages.push(total);
    } else if (current >= total - 3) {
      pages.push(1);
      pages.push('...');
      for (let i = total - 4; i <= total; i++) {
        pages.push(i);
      }
    } else {
      pages.push(1);
      pages.push('...');
      for (let i = current - 1; i <= current + 1; i++) {
        pages.push(i);
      }
      pages.push('...');
      pages.push(total);
    }
  }
  
  return pages;
};

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

// 关闭服务对话框
const closeServiceDialog = () => {
  showAddServiceDialog.value = false;
  showEditServiceDialog.value = false;
  resetServiceForm();
};

// 查看服务
const viewService = (id) => {
  router.push(`/system/services/${id}`)
}

// 编辑服务
const editService = async (id) => {
  try {
    loading.value = true;
    // 先从本地数据中查找服务详情，如果存在则直接使用，避免重复调用API
    const serviceItem = allServices.value.find(item => item.id === id);
    if (serviceItem) {
      serviceForm.id = serviceItem.id;
      serviceForm.name = serviceItem.name;
      serviceForm.categoryId = serviceItem.categoryId || '';
      serviceForm.price = serviceItem.price;
      serviceForm.duration = serviceItem.duration || '';
      serviceForm.description = serviceItem.description || '';
      serviceForm.status = serviceItem.status;
      showEditServiceDialog.value = true;
    } else {
      // 如果本地数据中找不到，则尝试调用API
      try {
        const response = await servicesApi.getServiceItemDetail(id);
        if (response.code === 200 && response.data) {
          serviceForm.id = response.data.id;
          serviceForm.name = response.data.name;
          serviceForm.categoryId = response.data.categoryId || '';
          serviceForm.price = response.data.price;
          serviceForm.duration = response.data.duration || '';
          serviceForm.description = response.data.description || '';
          serviceForm.status = response.data.status;
          showEditServiceDialog.value = true;
        } else {
          ElMessage.error('获取服务详情失败: ' + (response.message || '未知错误'));
        }
      } catch (apiError) {
        ElMessage.error('获取服务详情失败');
      }
    }
  } catch (error) {
    console.error('获取服务详情失败:', error);
    ElMessage.error('获取服务详情失败');
  } finally {
    loading.value = false;
  }
};

// 保存服务
const saveService = async () => {
  try {
    // 表单验证
    if (!serviceForm.name.trim()) {
      ElMessage.warning('请输入服务名称');
      return;
    }
    
    if (!serviceForm.categoryId) {
      ElMessage.warning('请选择服务类别');
      return;
    }
    
    if (!serviceForm.price || parseFloat(serviceForm.price) < 0) {
      ElMessage.warning('请输入有效的服务价格');
      return;
    }
    
    loading.value = true;
    let response;
    
    if (showEditServiceDialog.value) {
      // 编辑服务
      response = await servicesApi.updateServiceItem(serviceForm.id, serviceForm);
    } else {
      // 新增服务
      response = await servicesApi.createServiceItem(serviceForm);
    }
    
    if (response.code === 200) {
      ElMessage.success(showEditServiceDialog.value ? '服务更新成功' : '服务添加成功');
      closeServiceDialog();
      fetchServices(); // 重新获取服务列表
    } else {
      ElMessage.error((response.message || '操作失败'));
    }
  } catch (error) {
    console.error('保存服务失败:', error);
    ElMessage.error('保存服务失败');
  } finally {
    loading.value = false;
  }
};

// 删除服务
const deleteService = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该服务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    loading.value = true;
    const response = await servicesApi.deleteServiceItem(id);
    if (response.code === 200) {
      ElMessage.success('服务删除成功');
      fetchServices(); // 重新获取服务列表
    } else {
      ElMessage.error((response.message || '删除失败'));
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') { // 不是用户取消操作
      console.error('删除服务失败:', error);
      ElMessage.error('删除服务失败');
    }
  } finally {
    loading.value = false;
  }
};

// 启用服务（未启用/已下架 → 启用中）
const enableService = async (id) => {
  try {
    await ElMessageBox.confirm('确定要启用该服务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    });
    
    loading.value = true;
    // 先从本地数据中查找服务详情
    const serviceItem = allServices.value.find(item => item.id === id);
    if (serviceItem) {
      // 更新服务状态
      const updatedService = { ...serviceItem, status: SERVICE_STATUS.ACTIVE };
      const response = await servicesApi.updateServiceItem(id, updatedService);
      if (response.code === 200) {
        ElMessage.success('服务启用成功');
        fetchServices(); // 重新获取服务列表
      } else {
        ElMessage.error((response.message || '启用失败'));
      }
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('启用服务失败:', error);
      ElMessage.error('启用服务失败');
    }
  } finally {
    loading.value = false;
  }
};

// 下架服务（启用中 → 已下架）
const disableService = async (id) => {
  try {
    await ElMessageBox.confirm('确定要下架该服务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    loading.value = true;
    // 先从本地数据中查找服务详情
    const serviceItem = allServices.value.find(item => item.id === id);
    if (serviceItem) {
      // 更新服务状态
      const updatedService = { ...serviceItem, status: SERVICE_STATUS.DISABLED };
      const response = await servicesApi.updateServiceItem(id, updatedService);
      if (response.code === 200) {
        ElMessage.success('服务下架成功');
        fetchServices(); // 重新获取服务列表
      } else {
        ElMessage.error((response.message || '下架失败'));
      }
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('下架服务失败:', error);
      ElMessage.error('下架服务失败');
    }
  } finally {
    loading.value = false;
  }
};

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  
  const date = new Date(dateTime);
  if (isNaN(date.getTime())) return '';
  
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

// 监听页码变化，应用分页
watch(currentPage, () => {
  applyPagination();
});

// 监听每页显示条数变化，重置页码并应用分页
watch(pageSize, () => {
  currentPage.value = 1;
  applyPagination();
});

// 初始化
// 组件挂载时初始化数据
onMounted(async () => {
  try {
    console.log('组件挂载，开始初始化数据');
    // 先获取服务分类
    console.log('即将调用fetchServiceCategories函数');
    await fetchServiceCategories();
    console.log('fetchServiceCategories函数调用完成');
    // 然后获取服务统计和服务列表
    console.log('即将并行调用fetchServiceStats和fetchServices');
    await Promise.all([
      fetchServiceStats(),
      fetchServices()
    ]);
    console.log('初始化数据加载完成');
  } catch (error) {
    console.error('初始化数据加载失败:', error);
  }
});
</script>

<style scoped>
.system-page {
  padding: 20px;
}

.system-page-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.system-page-title-text {
  font-size: 20px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
}

.system-page-title-icon {
  font-size: 24px;
}

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

.system-search-bar {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #ebeef5;
}

.system-search-form {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.system-search-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 150px;
}

.system-form-label {
  font-size: 14px;
  color: #606266;
}

.system-form-input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  width: 100%;
  transition: border-color 0.2s;
}

.system-form-input:focus {
  outline: none;
  border-color: #409EFF;
}

.system-search-actions {
  display: flex;
  gap: 12px;
  margin-top: auto;
}

/* 移除自定义button相关样式 */
/* .system-button { */
/*   padding: 8px 16px; */
/*   border: none; */
/*   border-radius: 4px; */
/*   font-size: 14px; */
/*   cursor: pointer; */
/*   transition: all 0.3s; */
/* } */

/* .system-button-default { */
/*   background-color: #ffffff; */
/*   border: 1px solid #dcdfe6; */
/*   color: #606266; */
/* } */

/* .system-button-default:hover { */
/*   border-color: #c6e2ff; */
/*   color: #409EFF; */
/*   background-color: #ecf5ff; */
/* } */

/* .system-button-primary { */
/*   background-color: #409EFF; */
/*   color: #ffffff; */
/* } */

/* .system-button-primary:hover { */
/*   background-color: #66b1ff; */
/* } */

/* .btn { */
/*   padding: 4px 12px; */
/*   border: none; */
/*   border-radius: 4px; */
/*   font-size: 12px; */
/*   cursor: pointer; */
/*   transition: all 0.3s; */
/* } */

/* .btn-primary { */
/*   background-color: #409EFF; */
/*   color: #ffffff; */
/* } */

/* .btn-primary:hover { */
/*   background-color: #66b1ff; */
/* } */

/* .btn-default { */
/*   background-color: #ffffff; */
/*   border: 1px solid #dcdfe6; */
/*   color: #606266; */
/* } */

/* .btn-default:hover { */
/*   border-color: #c6e2ff; */
/*   color: #409EFF; */
/*   background-color: #ecf5ff; */
/* } */

/* .btn-danger { */
/*   background-color: #f56c6c; */
/*   color: #ffffff; */
/* } */

/* .btn-danger:hover { */
/*   background-color: #f78989; */
/* } */

/* .btn-sm { */
/*   padding: 2px 8px; */
/*   font-size: 12px; */
/* } */

.system-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-top: 1px solid #ebeef5;
}

.system-pagination-info {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: #909399;
}

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

.system-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.system-modal {
  background-color: #ffffff;
  border-radius: 8px;
  width: 500px;
  max-width: 90%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.system-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.system-modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.system-modal-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #909399;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 4px;
  transition: all 0.3s;
}

.system-modal-close:hover {
  background-color: #f5f7fa;
  color: #606266;
}

.system-modal-body {
  padding: 20px;
}

.system-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
}

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

/* 服务状态样式 */
.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1;
  text-align: center;
  transition: all 0.3s ease;
}

/* 启用中状态 */
.status-active {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

/* 未启用状态 */
.status-inactive {
  background-color: #f5f7fa;
  color: #909399;
  border: 1px solid #dcdfe6;
}

/* 已下架状态 */
.status-disabled {
  background-color: #fff2f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

/* 操作按钮组样式 */
.action-btn-group {
  display: flex;
  gap: 8px;
}

.btn {
  padding: 6px 16px;
  border: 1px solid transparent;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  height: 32px;
  line-height: 1;
  white-space: nowrap;
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

/* 统一Element Plus按钮大小 */
.el-button {
  height: 32px;
  padding: 6px 16px;
  font-size: 14px;
  line-height: 1;
}

/* 统一Element Plus Select大小 */
.el-select {
  height: 32px;
}

.el-select .el-input__wrapper {
  height: 32px;
}
</style>