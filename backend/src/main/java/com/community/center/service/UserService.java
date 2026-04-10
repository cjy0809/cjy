package com.community.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.center.dto.request.UserLoginRequest;
import com.community.center.dto.request.UserRegisterRequest;
import com.community.center.dto.request.UserUpdateRequest;
import com.community.center.dto.response.UserInfoResponse;
import com.community.center.dto.response.UserLoginResponse;
import com.community.center.entity.User;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户服务接口
 * 定义用户相关的业务操作，包括登录、注册、信息管理、余额管理等
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户登录
     * @param request 登录请求，包含手机号和密码
     * @return 登录响应，包含用户信息和JWT令牌
     */
    UserLoginResponse login(UserLoginRequest request);
    
    /**
     * 用户注册
     * @param request 注册请求，包含用户基本信息
     */
    void register(UserRegisterRequest request);
    
    /**
     * 获取当前登录用户信息
     * @return 用户信息响应对象
     */
    UserInfoResponse getCurrentUser();
    
    /**
     * 更新当前用户信息
     * @param request 更新请求，包含需要更新的用户信息
     */
    void updateUser(UserUpdateRequest request);
    
    /**
     * 修改用户密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(String oldPassword, String newPassword);
    
    /**
     * 分页查询用户列表
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名（可选）
     * @param name 姓名（可选）
     * @param status 状态（可选）
     * @param role 角色（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     */
    Page<UserInfoResponse> getUserPage(Integer page, Integer size, String username, String name, Integer status, String role, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 新状态
     */
    void updateUserStatus(Long userId, Integer status);
    
    /**
     * 根据用户ID更新用户信息
     * @param userId 用户ID
     * @param request 更新请求
     */
    void updateUserById(Long userId, UserUpdateRequest request);
    
    /**
     * 删除用户
     * @param userId 用户ID
     */
    void deleteUser(Long userId);
    
    /**
     * 级联删除用户及其关联数据
     * 包括健康档案、活动报名、场地预约、服务预约等
     * @param userId 用户ID
     */
    void deleteUserWithCascade(Long userId);
    
    /**
     * 获取用户关联数据的统计信息
     * @param userId 用户ID
     * @return 统计信息Map，包含各类关联数据的数量
     */
    Map<String, Integer> getUserRelatedDataCount(Long userId);
    
    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息响应对象
     */
    UserInfoResponse getUserById(Long userId);
    
    /**
     * 上传用户头像
     * @param file 头像文件
     * @return 头像URL
     * @throws Exception 上传失败时抛出异常
     */
    String uploadAvatar(MultipartFile file) throws Exception;
    
    /**
     * 检查用户是否为老年用户
     * @param userId 用户ID
     * @return 是否为老年用户
     */
    boolean isUserElderly(Long userId);
    
    /**
     * 更新用户余额
     * @param userId 用户ID
     * @param amount 金额变化量（正数为增加，负数为减少）
     */
    void updateUserBalance(Long userId, java.math.BigDecimal amount);
}
