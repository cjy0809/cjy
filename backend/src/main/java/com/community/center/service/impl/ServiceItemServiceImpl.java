package com.community.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.center.common.ServiceStatusEnum;
import com.community.center.entity.ServiceItem;
import com.community.center.mapper.ServiceItemMapper;
import com.community.center.service.ServiceItemService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 服务项目服务实现类
 * 支持三种服务状态：未启用(0)、启用中(1)、已下架(2)
 */
@Service
public class ServiceItemServiceImpl extends ServiceImpl<ServiceItemMapper, ServiceItem> implements ServiceItemService {
    
    /**
     * 分页获取服务项目列表
     * @param page 页码
     * @param size 每页大小
     * @param keyword 关键词
     * @param categoryId 分类ID
     * @return 分页结果
     */
    @Override
    public IPage<ServiceItem> getServiceItemPage(Integer page, Integer size, String keyword, Long categoryId) {
        // 调用带状态参数的方法，传入null表示不按状态筛选
        return getServiceItemPage(page, size, keyword, categoryId, null);
    }
    
    /**
     * 分页获取服务项目列表（支持状态筛选）
     * @param page 页码
     * @param size 每页大小
     * @param keyword 关键词
     * @param categoryId 分类ID
     * @param status 服务状态（0-未启用，1-启用中，2-已下架）
     * @return 分页结果
     */
    @Override
    public IPage<ServiceItem> getServiceItemPage(Integer page, Integer size, String keyword, Long categoryId, Integer status) {
        // 确保page和size不为空，设置默认值
        int pageNum = page != null && page > 0 ? page : 1;
        int pageSize = size != null && size > 0 ? size : 10;
        
        // 创建分页参数对象
        Page<ServiceItem> pageParam = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        QueryWrapper<ServiceItem> wrapper = new QueryWrapper<>();
        
        // 添加关键词搜索条件
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("name", keyword);
        }
        
        // 添加分类筛选条件
        if (categoryId != null && categoryId > 0) {
            wrapper.eq("category_id", categoryId);
        }
        
        // 添加状态筛选条件（如果指定了状态）
        if (status != null) {
            // 验证状态值是否有效（0-未启用，1-启用中，2-已下架）
            if (status >= 0 && status <= 2) {
                wrapper.eq("status", status);
            }
        }
        
        // 按创建时间倒序排列
        wrapper.orderByDesc("create_time");
        
        // 执行查询
        return baseMapper.selectPage(pageParam, wrapper);
    }
    
    @Override
    public List<ServiceItem> getServiceItemsByCategory(Long categoryId) {
        QueryWrapper<ServiceItem> wrapper = new QueryWrapper<>();
        wrapper.eq("category_id", categoryId);
        // 移除状态过滤条件，允许查询所有状态
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public boolean createServiceItem(ServiceItem item) {
        // 服务项目默认为启用状态
        if (item.getStatus() == null) {
            item.setStatus(ServiceStatusEnum.ACTIVE.getCode());
        }
        return baseMapper.insert(item) > 0;
    }
    
    @Override
    public boolean updateServiceItem(ServiceItem item) {
        // 确保更新时状态值有效
        if (item.getStatus() != null && (item.getStatus() < 0 || item.getStatus() > 2)) {
            // 状态值无效时设置为默认值
            item.setStatus(ServiceStatusEnum.ACTIVE.getCode());
        }
        return baseMapper.updateById(item) > 0;
    }
    
    @Override
    public boolean deleteServiceItem(Long id) {
        // 检查服务项目是否存在
        ServiceItem item = baseMapper.selectById(id);
        if (item == null) {
            throw new RuntimeException("服务项目不存在");
        }
        
        // 删除服务项目（物理删除）
        int result = baseMapper.deleteById(id);
        return result > 0;
    }
    
    /**
     * 获取所有服务项目（适配前端API调用）
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @Override
    public IPage<ServiceItem> getServiceItems(Integer page, Integer size) {
        // 调用带状态参数的方法，传入null表示不按状态筛选
        return getServiceItemPage(page, size, null, null, null);
    }
    
    /**
     * 获取服务项目详情
     * @param id 服务项目ID
     * @return 服务项目详情
     */
    @Override
    public ServiceItem getServiceItemDetail(Long id) {
        // 添加更详细的参数验证
        if (id == null) {
            System.err.println("服务项目ID不能为空");
            throw new RuntimeException("服务项目ID不能为空");
        }
        
        if (id <= 0) {
            System.err.println("服务项目ID不能小于等于0，当前ID: " + id);
            throw new RuntimeException("服务项目ID不能小于等于0");
        }
        
        // 添加日志记录
        System.out.println("查询服务项目详情，ID: " + id);
        
        try {
            ServiceItem item = baseMapper.selectById(id);
            if (item == null) {
                System.err.println("服务项目不存在，ID: " + id);
                throw new RuntimeException("服务项目不存在，ID: " + id);
            }
            
            // 添加查询成功日志
            System.out.println("成功获取服务项目详情，ID: " + id + ", 名称: " + item.getName());
            return item;
        } catch (Exception e) {
            // 捕获并记录数据库查询异常
            System.err.println("查询服务项目详情时发生数据库异常，ID: " + id + ", 错误信息: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("查询服务项目详情失败: " + e.getMessage());
        }
    }
}