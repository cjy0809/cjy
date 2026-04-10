# 社区老年活动中心管理系统 - 前端

## 项目简介

这是社区老年活动中心管理系统的前端部分，基于Vue 3 + Element Plus + Vite构建。

## 技术栈

- Vue 3
- Element Plus
- Vue Router 4
- Pinia
- Axios
- Vite

## 项目结构

```
frontend/
├── public/              # 静态资源
├── src/
│   ├── assets/          # 资源文件
│   ├── components/      # 公共组件
│   ├── router/          # 路由配置
│   ├── stores/          # 状态管理
│   ├── utils/           # 工具函数
│   ├── views/           # 页面组件
│   │   ├── auth/        # 认证相关页面
│   │   └── DashboardView.vue  # 仪表板页面
│   ├── App.vue          # 根组件
│   └── main.js          # 入口文件
├── index.html           # HTML模板
├── package.json         # 项目配置
└── vite.config.js       # Vite配置
```

## 安装与运行

### 前置条件

- Node.js 16+
- 后端服务已启动（默认端口8080）

### 安装依赖

```bash
cd frontend
npm install
```

### 开发环境运行

```bash
npm run dev
```

应用将在 http://localhost:3001 启动

### 生产环境构建

```bash
npm run build
```

## 功能说明

### 已实现功能

1. **用户认证**
   - 用户登录
   - 用户注册
   - 登录状态管理

2. **基础页面**
   - 登录页面
   - 注册页面
   - 仪表板页面

### 待实现功能

1. **活动管理**
   - 活动列表
   - 活动详情
   - 活动报名

2. **场地预约**
   - 场地列表
   - 场地预约
   - 预约管理

3. **健康服务**
   - 健康档案
   - 健康数据录入

4. **新闻公告**
   - 新闻列表
   - 公告管理

## 开发说明

### API请求

所有API请求通过`src/utils/request.js`中的axios实例发送，已配置请求拦截器和响应拦截器。

### 状态管理

使用Pinia进行状态管理，用户状态存储在`src/stores/user.js`中。

### 路由

路由配置在`src/router/index.js`中，包含路由守卫，实现登录状态检查。

### 样式

使用Element Plus组件库，自定义样式在`src/assets/main.css`中。

## 注意事项

1. 确保后端服务已启动并运行在8080端口
2. 前端默认运行在3000端口，已配置代理转发API请求到后端
3. 登录成功后会跳转到仪表板页面
4. 未登录状态下访问需要权限的页面会重定向到登录页