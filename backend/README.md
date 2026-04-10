# 社区中心管理系统

这是一个基于Spring Boot + MyBatis Plus + MySQL的社区中心管理系统，提供用户管理、活动管理、场地预约、健康档案和新闻发布等功能。

## 项目结构

```
backend/
├── src/main/java/com/community/center/
│   ├── config/              # 配置类
│   │   ├── SecurityConfig.java
│   │   ├── SwaggerConfig.java
│   │   └── WebMvcConfig.java
│   ├── controller/          # 控制器
│   │   ├── UserController.java
│   │   ├── ActivityController.java
│   │   ├── VenueController.java
│   │   ├── ReservationController.java
│   │   ├── HealthRecordController.java
│   │   └── NewsController.java
│   ├── service/             # 服务接口
│   │   ├── UserService.java
│   │   ├── ActivityService.java
│   │   ├── VenueService.java
│   │   ├── ReservationService.java
│   │   ├── HealthRecordService.java
│   │   └── NewsService.java
│   ├── service/impl/        # 服务实现
│   │   ├── UserServiceImpl.java
│   │   ├── ActivityServiceImpl.java
│   │   ├── VenueServiceImpl.java
│   │   ├── ReservationServiceImpl.java
│   │   ├── HealthRecordServiceImpl.java
│   │   └── NewsServiceImpl.java
│   ├── mapper/              # 数据访问层
│   │   ├── UserMapper.java
│   │   ├── ActivityMapper.java
│   │   ├── VenueMapper.java
│   │   ├── ReservationMapper.java
│   │   ├── HealthRecordMapper.java
│   │   └── NewsMapper.java
│   ├── entity/              # 实体类
│   │   ├── User.java
│   │   ├── Activity.java
│   │   ├── Venue.java
│   │   ├── Reservation.java
│   │   ├── ActivityRegistration.java
│   │   ├── HealthRecord.java
│   │   └── News.java
│   ├── dto/                 # 数据传输对象
│   │   ├── request/         # 请求DTO
│   │   └── response/        # 响应DTO
│   ├── common/              # 公共类
│   │   ├── Result.java
│   │   ├── ResultCode.java
│   │   ├── PageResult.java
│   │   └── JwtUtil.java
│   ├── exception/           # 异常处理
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   └── AuthenticationException.java
│   └── interceptor/         # 拦截器
│       └── AuthenticationInterceptor.java
└── src/main/resources/
    ├── application.yml      # 应用配置
    ├── mapper/              # MyBatis映射文件
    └── db/migration/        # 数据库初始化脚本
```

## 技术栈

- **框架**: Spring Boot 2.7.0
- **数据库**: MySQL 8.0
- **ORM**: MyBatis Plus 3.5.2
- **安全**: Spring Security + JWT
- **文档**: Swagger 3.0
- **工具**: Lombok, Hutool

## 功能模块

### 1. 用户管理
- 用户注册、登录
- 用户信息查询、更新
- 密码修改
- 用户状态管理

### 2. 活动管理
- 活动创建、更新、删除
- 活动详情查询
- 活动分页查询
- 活动报名、取消报名
- 活动状态管理

### 3. 场地管理
- 场地创建、更新、删除
- 场地详情查询
- 场地分页查询
- 场地预约、取消预约
- 场地状态管理

### 4. 预约管理
- 预约详情查询
- 预约分页查询
- 预约取消
- 批量取消过期预约

### 5. 健康档案
- 健康档案创建、更新、删除
- 健康档案详情查询
- 健康档案分页查询

### 6. 新闻管理
- 新闻创建、更新、删除
- 新闻详情查询
- 新闻分页查询
- 新闻状态管理

## 快速开始

### 1. 环境准备
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库初始化
执行 `src/main/resources/db/migration/V1__Create_initial_tables.sql` 脚本创建数据库和表结构。

### 3. 配置修改
修改 `application.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/community_center?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: your_username
    password: your_password
```

### 4. 启动应用
```bash
mvn spring-boot:run
```

### 5. 访问接口
- 应用地址: http://localhost:8081
- Swagger文档: http://localhost:8081/swagger-ui.html

## 默认账号

- 管理员账号: admin / 123456
- 普通用户账号: user1 / 123456

## API文档

启动应用后，访问 http://localhost:8081/swagger-ui.html 查看完整的API文档。

## 开发规范

1. 统一使用Result类封装返回结果
2. 使用DTO进行数据传输
3. 使用MyBatis Plus简化数据库操作
4. 使用JWT进行身份验证
5. 使用Swagger生成API文档

## 注意事项

1. 密码使用BCrypt加密存储
2. 所有删除操作均为逻辑删除
3. 分页查询使用MyBatis Plus提供的分页插件
4. 时间字段统一使用DateTime类型
5. 图片URL使用字符串存储