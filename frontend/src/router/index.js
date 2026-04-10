import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

/**
 * 路由配置数组
 * 定义了应用的所有路由，包括公共路由、系统管理路由和老年用户路由
 */
const routes = [
  // 根路由，重定向到登录页
  {
    path: '/',
    name: 'Root',
    redirect: '/login'
  },
  
  // 登录页面
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { requiresGuest: true }
  },
  
  // 注册页面
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { requiresGuest: true }
  },

  // 系统管理模块路由（工作人员和管理员）
  {
    path: '/system',
    name: 'System',
    redirect: '/system/analytics-dashboard',
    meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] },
    component: () => import('@/views/system/SystemLayout.vue'),
    children: [
      // 活动管理
      {
        path: 'activities',
        name: 'Activities',
        component: () => import('@/views/system/ActivitiesView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
      {
        path: 'activities/:id',
        name: 'ActivityDetail',
        component: () => import('@/views/system/ActivityDetailView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
      {
        path: 'activity-registrations',
        name: 'ActivityRegistrations',
        component: () => import('@/views/system/ActivityRegistrationView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
      {
        path: 'activity-calendar',
        name: 'ActivityCalendar',
        component: () => import('@/views/system/ActivityCalendarView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
      
      // 社区服务
      {
        path: 'services',
        name: 'Services',
        component: () => import('@/views/system/ServicesView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
      {
        path: 'services/:id',
        name: 'ServiceDetail',
        component: () => import('@/views/system/ServiceDetailView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
      
      // 服务预约管理
      {
        path: 'service-reservations',
        name: 'ServiceReservations',
        component: () => import('@/views/system/ServiceBookingView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
      
      // 金额管理
      {
        path: 'payment-management',
        name: 'PaymentManagement',
        component: () => import('@/views/system/PaymentManagementView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
      
      // 场地列表管理
      {
        path: 'venues',
        name: 'VenueList',
        component: () => import('@/views/system/VenueListView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
      
      // 场地预约管理
      {
        path: 'venue-reservations',
        name: 'VenueReservationList',
        component: () => import('@/views/system/VenueReservationView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
      
      // 预约日历
      {
        path: 'venue-calendar',
        name: 'VenueCalendar',
        component: () => import('@/views/system/ReservationCalendarView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
      
      // 新闻管理
      { path: 'news',      
        name: 'NewsList',    
        component: () => import('@/views/system/NewsListView.vue'),       
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }     
      },
      {
        path: 'news/:id',
        name: 'NewsDetail',
        component: () => import('@/views/system/NewsDetailView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
      
      // 数据分析
      {
        path: 'analytics-dashboard',
        name: 'AnalyticsDashboard',
        component: () => import('@/views/system/AnalyticsDashboardView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
      
      // 用户管理
      {
        path: 'user-management',
        name: 'UserManagement',
        component: () => import('@/views/system/UserManagementView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
      
      // 充值管理
      {
        path: 'recharge-management',
        name: 'RechargeManagement',
        component: () => import('@/views/system/RechargeManagementView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
      
      // 健康档案管理
      {  
        path: 'health-records',
        name: 'HealthRecords',
        component: () => import('@/views/system/HealthRecordView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },

      // 评论管理
      {
        path: 'comment-management',
        name: 'CommentManagement',
        component: () => import('@/views/system/CommentManagementView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
      {
        path: 'rating-list',
        name: 'RatingList',
        component: () => import('@/views/system/RatingListView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
      
      // 个人资料
      {
        path: 'profile',
        name: 'SystemProfile',
        component: () => import('@/views/system/SystemProfileView.vue'),
        meta: { requiresAuth: true, role: ['STAFF', 'ADMIN'] }
      },
    ]
  },
  
  // 老年用户模块路由
  {
    path: '/elderly',
    name: 'Elderly',
    redirect: '/elderly/home',
    meta: { requiresAuth: true, role: ['ELDERLY'] },
    component: () => import('@/views/elderly/ElderlyLayout.vue'),
    children: [
      // 首页
      {
        path: 'home',
        name: 'ElderlyHome',
        component: () => import('@/views/elderly/ElderlyHomeView.vue'),
        meta: { requiresAuth: true, role: ['ELDERLY'] }
      },
      
      // 活动相关
      {
        path: 'activities',
        name: 'ElderlyActivities',
        component: () => import('@/views/elderly/ElderlyActivitiesView.vue'),
        meta: { requiresAuth: true, role: ['ELDERLY'] }
      },
      {
        path: 'activities/:id',
        name: 'ElderlyActivityDetail',
        component: () => import('@/views/elderly/ElderlyActivityDetailView.vue'),
        meta: { requiresAuth: true, role: ['ELDERLY'] }
      },
      
      // 新闻相关
      {
        path: 'news',
        name: 'ElderlyNews',
        component: () => import('@/views/elderly/ElderlyNewsView.vue'),
        meta: { requiresAuth: true, role: ['ELDERLY'] }
      },
      {
        path: 'news/:id',
        name: 'ElderlyNewsDetail',
        component: () => import('@/views/elderly/ElderlyNewsDetailView.vue'),
        meta: { requiresAuth: true, role: ['ELDERLY'] }
      },
      
      // 服务相关
      {
        path: 'services',
        name: 'ElderlyServices',
        component: () => import('@/views/elderly/ElderlyServicesView.vue'),
        meta: { requiresAuth: true, role: ['ELDERLY'] }
      },
      {
        path: 'services/:id',
        name: 'ElderlyServiceDetail',
        component: () => import('@/views/elderly/ElderlyServiceDetailView.vue'),
        meta: { requiresAuth: true, role: ['ELDERLY'] }
      },
      
      // 健康档案
      {
        path: 'health',
        name: 'ElderlyHealth',
        component: () => import('@/views/elderly/ElderlyHealthView.vue'),
        meta: { requiresAuth: true, role: ['ELDERLY'] }
      },
      
      // 场地相关
      {
        path: 'venues',
        name: 'ElderlyVenues',
        component: () => import('@/views/elderly/ElderlyVenuesView.vue'),
        meta: { requiresAuth: true, role: ['ELDERLY'] }
      },
      {
        path: 'venues/:id',
        name: 'ElderlyVenueDetail',
        component: () => import('@/views/elderly/ElderlyVenueDetailView.vue'),
        meta: { requiresAuth: true, role: ['ELDERLY'] }
      },
      {
        path: 'venues/:id/schedule',
        name: 'ElderlyVenueSchedule',
        component: () => import('@/views/elderly/ElderlyVenueScheduleView.vue'),
        meta: { requiresAuth: true, role: ['ELDERLY'] }
      },

      // 个人资料
      {
        path: 'profile',
        name: 'ElderlyProfile',
        component: () => import('@/views/elderly/ElderlyProfileView.vue'),
        meta: { requiresAuth: true, role: ['ELDERLY'] }
      },
      
      // 我的预约
      {
        path: 'bookings',
        name: 'ElderlyMyBookings',
        component: () => import('@/views/elderly/ElderlyMyBookingsView.vue'),
        meta: { requiresAuth: true, role: ['ELDERLY'] }
      },
      
      // 收藏
      {
        path: 'favorites',
        name: 'ElderlyFavorites',
        component: () => import('@/views/elderly/ElderlyFavoritesView.vue'),
        meta: { requiresAuth: true, role: ['ELDERLY'] }
      },
      
      // 场地预约
      {
        path: 'venue-bookings',
        name: 'ElderlyVenueBookings',
        component: () => import('@/views/elderly/components/ElderlyVenueBookings.vue'),
        meta: { requiresAuth: true, role: ['ELDERLY'] }
      },
      
      // 支付
      {
        path: 'payment',
        name: 'ElderlyPayment',
        component: () => import('@/views/elderly/PaymentView.vue'),
        meta: { requiresAuth: true, role: ['ELDERLY'] }
      },
      
      // 充值
      {
        path: 'recharge',
        name: 'ElderlyRecharge',
        component: () => import('@/views/elderly/ElderlyRechargeView.vue'),
        meta: { requiresAuth: true, role: ['ELDERLY'] }
      }
    ]
  }
]

/**
 * 创建路由实例
 * 配置路由模式、路由列表和滚动行为
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 如果有保存的位置（如使用浏览器后退按钮），返回到保存的位置
    if (savedPosition) {
      return savedPosition
    }
    
    // 延迟执行，确保 DOM 已更新
    return new Promise((resolve) => {
      setTimeout(() => {
        // 尝试滚动到 .elderly-main 容器（老年用户端）
        const elderlyMain = document.querySelector('.elderly-main')
        if (elderlyMain) {
          elderlyMain.scrollTop = 0
          resolve({ top: 0, left: 0 })
        } else {
          // 如果没有找到 .elderly-main，使用默认滚动
          resolve({ top: 0, left: 0 })
        }
      }, 100)
    })
  }
})

/**
 * 路由守卫
 * 在路由跳转前进行权限检查和用户角色验证
 */
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isAuthenticated = !!userStore.token
  const userRole = userStore.userRole || ''  
  const isAdmin = userStore.isAdmin
  const isStaff = userStore.isStaff
  const isElderly = userStore.isElderly

  // 1. 处理根路径
  if (to.path === '/') {
    next('/login')
    return
  }

  // 2. 检查是否为访客页面（已登录用户不应访问登录/注册页）
  if (to.meta.requiresGuest) {
    if (isAuthenticated && from.path !== '/') {
      if (isElderly) {
        next('/elderly/home')
      } else if (isAdmin || isStaff) {
        next('/system/analytics-dashboard')
      } else {
        next('/login')
      }
      return
    }
  }

  // 3. 检查是否需要登录认证
  if (to.meta.requiresAuth) {
    if (!isAuthenticated) {
      ElMessage.warning('请先登录')
      next('/login')
      return
    }

    // 4. 检查角色权限
    if (to.meta.role) {
      let hasPermission = false
      
      if (Array.isArray(to.meta.role)) {
        hasPermission = to.meta.role.includes(userRole)
      } else {
        hasPermission = userRole === to.meta.role
      }
      
      if (!hasPermission) {
        ElMessage.error('没有权限访问此页面')
        if (isElderly) {
          next('/elderly/home')
        } else if (isAdmin || isStaff) {
          next('/system/analytics-dashboard')
        } else {
          next('/login')
        }
        return
      }
    }

    // 5. 检查管理员权限
    if (to.meta.requiresAdmin && !isAdmin) {
      ElMessage.error('需要管理员权限才能访问此页面')
      next(from.path || (isElderly ? '/elderly/home' : '/system/analytics-dashboard'))
      return
    }

    // 6. 检查工作人员权限（管理员也可以访问）
    if (to.meta.requiresStaff && !isStaff && !isAdmin) {
      ElMessage.error('需要工作人员或管理员权限才能访问此页面')
      next(from.path || (isElderly ? '/elderly/home' : '/system/analytics-dashboard'))
      return
    }
  }

  next()
})

export default router
