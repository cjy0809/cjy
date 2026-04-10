package com.community.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.center.common.ResultCode;
import com.community.center.dto.request.UserLoginRequest;
import com.community.center.dto.request.UserRegisterRequest;
import com.community.center.dto.request.UserUpdateRequest;
import com.community.center.dto.response.UserInfoResponse;
import com.community.center.dto.response.UserLoginResponse;
import com.community.center.dto.response.RegistrationListResponse;
import com.community.center.entity.User;
import com.community.center.entity.HealthRecord;
import com.community.center.entity.Reservation;
import com.community.center.entity.ServiceReservation;
import com.community.center.exception.BusinessException;
import com.community.center.mapper.UserMapper;
import com.community.center.service.UserService;
import com.community.center.common.JwtUtil;
import com.community.center.common.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import com.community.center.service.HealthService;
import com.community.center.service.RegistrationService;
import com.community.center.service.ReservationService;
import com.community.center.service.ServiceReservationService;
import com.community.center.service.ActivityService;

/**
 * 用户服务实现类
 * 实现用户相关的业务逻辑，包括用户认证、信息管理、余额操作等
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${upload.path:./uploads/}")
    private String uploadPath;
    
    @Value("${server.domain:http://localhost:8080}")
    private String serverDomain;

    @Resource
    private PasswordEncoder passwordEncoder;
    
    @Resource
    private JwtUtil jwtUtil;
    
    @Resource
    @Lazy
    private HealthService healthService;
    
    @Resource
    @Lazy
    private RegistrationService registrationService;
    
    @Resource
    @Lazy
    private ReservationService reservationService;
    
    @Resource
    @Lazy
    private ServiceReservationService serviceReservationService;
    
    @Resource
    @Lazy
    private ActivityService activityService;

    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        System.out.println("=== 登录请求开始 ===");
        System.out.println("手机号: " + request.getPhone());
        System.out.println("密码: " + request.getPassword());
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, request.getPhone());
        User user = this.getOne(queryWrapper);
        
        if (user == null) {
            System.out.println("用户不存在");
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        System.out.println("找到用户: " + user.getUsername());
        System.out.println("用户ID: " + user.getId());
        System.out.println("用户角色: " + user.getRole());
        System.out.println("用户状态: " + user.getStatus());
        System.out.println("数据库密码哈希: " + user.getPassword());
        System.out.println("输入密码长度: " + request.getPassword().length());
        
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        System.out.println("密码匹配结果: " + passwordMatches);
        
        if (!passwordMatches) {
            System.out.println("密码验证失败");
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        
        if (user.getStatus() != 1) {
            System.out.println("用户状态异常: " + user.getStatus());
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());
        
        UserLoginResponse response = new UserLoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setRole(user.getRole());
        
        return response;
    }

    @Override
    @Transactional
    public void register(UserRegisterRequest request) {
        System.out.println("开始注册用户，请求数据: " + request);
        
        // 获取当前用户角色
        String currentUserRole = SecurityUtil.getCurrentUserRole();
        boolean isAdmin = "ADMIN".equals(currentUserRole);
        
        // 权限检查：只有管理员可以创建用户
        if (!isAdmin) {
            throw new BusinessException(ResultCode.NO_PERMISSION, "只有管理员可以创建用户");
        }
        
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, request.getUsername());
        User existUser = this.getOne(queryWrapper);
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }
        
        // 检查手机号是否已存在
        LambdaQueryWrapper<User> phoneQueryWrapper = new LambdaQueryWrapper<>();
        phoneQueryWrapper.eq(User::getPhone, request.getPhone());
        User existPhoneUser = this.getOne(phoneQueryWrapper);
        if (existPhoneUser != null) {
            throw new BusinessException(ResultCode.PHONE_ALREADY_EXISTS);
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender() != null ? request.getGender() : 0); // 默认为0
        user.setAge(request.getAge());
        user.setAddress(request.getAddress());
        user.setRole(StringUtils.hasText(request.getRole()) ? 
            (request.getRole().toUpperCase().equals("ELDERLY") || request.getRole().toUpperCase().equals("STAFF") || request.getRole().toUpperCase().equals("ADMIN") ? 
                request.getRole().toUpperCase() : "ELDERLY") : "ELDERLY"); // 默认为ELDERLY
        // 设置默认头像
        user.setAvatar("/uploads/avatars/dafault-avatar.jpg");
        
        user.setStatus(1); // 默认为已激活状态
        user.setEmergencyContact(request.getEmergencyContact());
        user.setEmergencyPhone(request.getEmergencyPhone());
        if (StringUtils.hasText(request.getHealthStatus())) {
            user.setHealthStatus(request.getHealthStatus());
        }
        
        // 保存用户
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ResultCode.USER_REGISTER_FAILED);
        }
        
        System.out.println("用户注册成功，用户ID: " + user.getId());
    }

    @Override
    public UserInfoResponse getCurrentUser() {
        // 从SecurityUtil获取当前用户ID
        Long userId = SecurityUtil.getCurrentUserId();
        
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        UserInfoResponse response = new UserInfoResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    @Override
    public void updateUser(UserUpdateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 更新用户信息
        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (StringUtils.hasText(request.getAddress())) {
            user.setAddress(request.getAddress());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }
        if (StringUtils.hasText(request.getEmergencyContact())) {
            user.setEmergencyContact(request.getEmergencyContact());
        }
        if (StringUtils.hasText(request.getEmergencyPhone())) {
            user.setEmergencyPhone(request.getEmergencyPhone());
        }
        if (StringUtils.hasText(request.getHealthStatus())) {
            user.setHealthStatus(request.getHealthStatus());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (StringUtils.hasText(request.getAddress())) {
            user.setAddress(request.getAddress());
        }
        
        this.updateById(user);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    @Override
    public Page<UserInfoResponse> getUserPage(Integer page, Integer size, String username, String name, Integer status, String role, LocalDateTime startTime, LocalDateTime endTime) {
        Page<User> userPage = new Page<>(page, size);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(username)) {
            queryWrapper.like(User::getUsername, username);
        }
        if (StringUtils.hasText(name)) {
            queryWrapper.like(User::getName, name);
        }
        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }
        if (StringUtils.hasText(role)) {
            // 直接使用前端传递的角色字符串，数据库中存储的是字符串值
            queryWrapper.eq(User::getRole, role);
        }
        if (startTime != null) {
            queryWrapper.ge(User::getCreateTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(User::getCreateTime, endTime);
        }
        
        queryWrapper.orderByAsc(User::getId);
        this.page(userPage, queryWrapper);
        
        // 创建新的Page<UserInfoResponse>对象
        Page<UserInfoResponse> responsePage = new Page<>(page, size);
        responsePage.setTotal(userPage.getTotal());
        responsePage.setSize(userPage.getSize());
        responsePage.setCurrent(userPage.getCurrent());
        responsePage.setPages(userPage.getPages());
        
        // 转换User对象为UserInfoResponse对象
        List<UserInfoResponse> responseList = userPage.getRecords().stream().map((@NonNull User user) -> {
            UserInfoResponse response = new UserInfoResponse();
            BeanUtils.copyProperties(user, response);
            return response;
        }).collect(Collectors.toList());
        
        responsePage.setRecords(responseList);
        
        return responsePage;
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        // 获取当前用户角色
        String currentUserRole = SecurityUtil.getCurrentUserRole();
        boolean isAdmin = "ADMIN".equals(currentUserRole);
        
        // 权限检查：只有管理员可以修改用户状态
        if (!isAdmin) {
            throw new BusinessException(ResultCode.NO_PERMISSION, "只有管理员可以修改用户状态");
        }
        
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        user.setStatus(status);
        this.updateById(user);
    }
    
    @Override
    public void updateUserById(Long userId, UserUpdateRequest request) {
        System.out.println("开始更新用户信息，用户ID: " + userId);
        System.out.println("更新请求数据: " + request);
        
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 获取当前用户角色
        String currentUserRole = SecurityUtil.getCurrentUserRole();
        boolean isAdmin = "ADMIN".equals(currentUserRole);
        boolean isStaff = "STAFF".equals(currentUserRole);
        
        // 权限检查：管理员可以编辑所有用户，工作人员只能编辑老年用户
        if (!isAdmin && isStaff && !"ELDERLY".equals(user.getRole())) {
            throw new BusinessException(ResultCode.NO_PERMISSION, "工作人员只能编辑老年用户信息");
        }
        
        // 权限检查：工作人员不能修改用户角色
        if (!isAdmin && StringUtils.hasText(request.getRole())) {
            throw new BusinessException(ResultCode.NO_PERMISSION, "工作人员无权修改用户角色");
        }
        
        // 权限检查：工作人员不能修改用户状态
        if (!isAdmin && request.getStatus() != null) {
            throw new BusinessException(ResultCode.NO_PERMISSION, "工作人员无权修改用户状态");
        }
        
        // 更新用户信息
        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName());
        }
        if (StringUtils.hasText(request.getUsername())) {
            user.setUsername(request.getUsername());
        }
        if (StringUtils.hasText(request.getPassword())) {
            // 密码需要加密
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        // 只有管理员可以修改用户状态
        if (isAdmin && request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        // 只有管理员可以修改用户角色
        if (isAdmin && StringUtils.hasText(request.getRole())) {
            user.setRole(request.getRole());
        }
        if (StringUtils.hasText(request.getHealthStatus())) {
            user.setHealthStatus(request.getHealthStatus());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (StringUtils.hasText(request.getAddress())) {
            user.setAddress(request.getAddress());
        }
        
        // 更新紧急联系人信息
        if (StringUtils.hasText(request.getEmergencyContact())) {
            System.out.println("更新紧急联系人: " + request.getEmergencyContact());
            user.setEmergencyContact(request.getEmergencyContact());
        }
        if (StringUtils.hasText(request.getEmergencyPhone())) {
            System.out.println("更新紧急联系电话: " + request.getEmergencyPhone());
            user.setEmergencyPhone(request.getEmergencyPhone());
        }
        
        this.updateById(user);
        System.out.println("用户信息更新完成");
    }
    
    @Override
    @Transactional
    public void deleteUser(Long userId) {
        System.out.println("开始删除用户，用户ID: " + userId);
        
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        try {
            System.out.println("开始删除用户ID " + userId + " 的用户记录");
            boolean userDeleteResult = this.removeById(userId);
            System.out.println("用户记录删除完成，结果: " + userDeleteResult);
            
            if (!userDeleteResult) {
                System.err.println("删除用户失败：用户记录删除失败，用户ID: " + userId);
                throw new BusinessException("删除用户失败：用户记录删除失败");
            }
            
            System.out.println("用户ID " + userId + " 删除成功");
        } catch (Exception e) {
            System.err.println("删除用户失败: " + e.getMessage());
            e.printStackTrace();
            throw new BusinessException("删除用户失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void deleteUserWithCascade(Long userId) {
        System.out.println("开始级联删除用户，用户ID: " + userId);
        
        // 获取当前用户角色
        String currentUserRole = SecurityUtil.getCurrentUserRole();
        boolean isAdmin = "ADMIN".equals(currentUserRole);
        
        // 权限检查：只有管理员可以删除用户
        if (!isAdmin) {
            throw new BusinessException(ResultCode.NO_PERMISSION, "只有管理员可以删除用户");
        }
        
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        try {
            // 1. 删除健康记录
            System.out.println("开始删除用户ID " + userId + " 的健康记录");
            boolean healthDeleteResult = healthService.deleteHealthRecordsByUserId(userId);
            System.out.println("健康记录删除完成，结果: " + healthDeleteResult);
            
            // 2. 删除活动报名记录
            System.out.println("开始删除用户ID " + userId + " 的活动报名记录");
            boolean registrationDeleteResult = registrationService.deleteRegistrationsByUserId(userId);
            System.out.println("活动报名记录删除完成，结果: " + registrationDeleteResult);
            
            // 3. 删除场地预约记录
            System.out.println("开始删除用户ID " + userId + " 的场地预约记录");
            boolean reservationDeleteResult = reservationService.deleteReservationsByUserId(userId);
            System.out.println("场地预约记录删除完成，结果: " + reservationDeleteResult);
            
            // 4. 删除服务预约记录
            System.out.println("开始删除用户ID " + userId + " 的服务预约记录");
            boolean serviceReservationDeleteResult = serviceReservationService.deleteServiceReservationsByUserId(userId);
            System.out.println("服务预约记录删除完成，结果: " + serviceReservationDeleteResult);
            
            // 5. 删除用户组织的活动
            System.out.println("开始删除用户ID " + userId + " 组织的活动");
            try {
                activityService.deleteActivitiesByOrganizerId(userId);
                System.out.println("用户组织的活动删除完成");
            } catch (Exception e) {
                System.err.println("删除用户组织的活动失败: " + e.getMessage());
                throw new BusinessException("删除用户组织的活动失败: " + e.getMessage());
            }
            
            // 7. 删除用户记录
            System.out.println("开始删除用户ID " + userId + " 的用户记录");
            boolean userDeleteResult = this.removeById(userId);
            System.out.println("用户记录删除完成，结果: " + userDeleteResult);
            
            if (!userDeleteResult) {
                System.err.println("级联删除用户失败：用户记录删除失败，用户ID: " + userId);
                throw new BusinessException("级联删除用户失败：用户记录删除失败");
            }
            
            System.out.println("用户ID " + userId + " 级联删除成功");
        } catch (Exception e) {
            System.err.println("级联删除用户失败: " + e.getMessage());
            e.printStackTrace();
            throw new BusinessException("级联删除用户失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Integer> getUserRelatedDataCount(Long userId) {
        Map<String, Integer> countMap = new HashMap<>();
        
        try {
            // 统计健康记录数量
            List<HealthRecord> healthRecords = healthService.getHealthRecordsByUserId(userId, new HashMap<>());
            countMap.put("healthRecords", healthRecords.size());
            
            // 统计活动报名记录数量
            List<RegistrationListResponse> registrations = registrationService.getUserRegistrations(userId);
            countMap.put("registrations", registrations.size());
            
            // 统计场地预约记录数量
            LambdaQueryWrapper<Reservation> reservationQueryWrapper = new LambdaQueryWrapper<>();
            reservationQueryWrapper.eq(Reservation::getUserId, userId);
            long reservationCount = reservationService.count(reservationQueryWrapper);
            countMap.put("reservations", (int) reservationCount);
            
            // 统计服务预约记录数量
            List<ServiceReservation> serviceReservations = serviceReservationService.getReservationsByUserId(userId);
            countMap.put("serviceReservations", serviceReservations.size());
            
        } catch (Exception e) {
            System.err.println("获取用户关联数据统计失败: " + e.getMessage());
            e.printStackTrace();
            // 返回空统计，不影响删除流程
        }
        
        return countMap;
    }
    
    @Override
    public UserInfoResponse getUserById(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            return null;
        }
        
        UserInfoResponse response = new UserInfoResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }
    
    @Override
    public String uploadAvatar(MultipartFile file) throws Exception {
        // 添加日志记录
        System.out.println("开始上传头像，文件名: " + file.getOriginalFilename() + ", 文件大小: " + file.getSize());
        
        // 检查文件是否为空
        if (file.isEmpty()) {
            System.out.println("上传文件为空");
            throw new Exception("上传文件不能为空");
        }
        
        // 检查文件大小（限制为5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            System.out.println("文件大小超过限制: " + file.getSize());
            throw new Exception("文件大小不能超过5MB");
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        System.out.println("文件类型: " + contentType);
        if (contentType == null || !contentType.startsWith("image/")) {
            System.out.println("文件类型不合法: " + contentType);
            throw new Exception("只能上传图片文件");
        }
        
        // 创建上传目录（如果不存在）
        Path uploadDir = Paths.get(uploadPath, "avatars");
        System.out.println("上传目录: " + uploadDir.toString());
        if (!Files.exists(uploadDir)) {
            try {
                Files.createDirectories(uploadDir);
                System.out.println("创建上传目录成功");
            } catch (IOException e) {
                System.out.println("创建上传目录失败: " + e.getMessage());
                throw new Exception("创建上传目录失败: " + e.getMessage());
            }
        }
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String fileName = UUID.randomUUID().toString() + extension;
        Path filePath = uploadDir.resolve(fileName);
        System.out.println("生成文件名: " + fileName + ", 完整路径: " + filePath.toString());
        
        // 保存文件
        try {
            File targetFile = filePath.toFile();
            if (targetFile != null) {
                FileCopyUtils.copy(file.getBytes(), targetFile);
                System.out.println("文件保存成功");
            } else {
                throw new Exception("无法创建目标文件");
            }
        } catch (Exception e) {
            System.out.println("文件保存失败: " + e.getMessage());
            throw new Exception("文件保存失败: " + e.getMessage());
        }
        
        // 返回文件访问URL
        String avatarUrl = serverDomain + "/uploads/avatars/" + fileName;
        System.out.println("生成访问URL: " + avatarUrl);
        
        // 更新当前用户的头像URL
        Long currentUserId = SecurityUtil.getCurrentUserId();
        System.out.println("获取当前用户ID: " + currentUserId);
        if (currentUserId != null) {
            User user = this.getById(currentUserId);
            if (user != null) {
                String oldAvatar = user.getAvatar();
                user.setAvatar(avatarUrl);
                boolean updateResult = this.updateById(user);
                System.out.println("更新用户头像结果: " + updateResult);
                
                // 删除旧头像文件（如果存在且不是默认头像）
                if (oldAvatar != null && !oldAvatar.isEmpty() && !oldAvatar.contains("default-avatar")) {
                    try {
                        String oldFileName = oldAvatar.substring(oldAvatar.lastIndexOf("/") + 1);
                        Path oldFilePath = uploadDir.resolve(oldFileName);
                        if (Files.exists(oldFilePath)) {
                            Files.delete(oldFilePath);
                            System.out.println("删除旧头像文件成功: " + oldFileName);
                        }
                    } catch (Exception e) {
                        System.out.println("删除旧头像文件失败: " + e.getMessage());
                        // 不抛出异常，不影响主流程
                    }
                }
            } else {
                System.out.println("未找到用户信息，用户ID: " + currentUserId);
                throw new Exception("未找到用户信息");
            }
        } else {
            System.out.println("无法获取当前用户ID");
            throw new Exception("用户未登录或认证失败");
        }
        
        return avatarUrl;
    }
    
    /**
     * 检查用户是否为老年用户
     * @param userId 用户ID
     * @return 是否为老年用户
     */
    public boolean isUserElderly(Long userId) {
        User user = this.getById(userId);
        return user != null && "ELDERLY".equals(user.getRole());
    }
    
    @Override
    public void updateUserBalance(Long userId, java.math.BigDecimal amount) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setBalance(user.getBalance().add(amount));
        this.updateById(user);
    }
}