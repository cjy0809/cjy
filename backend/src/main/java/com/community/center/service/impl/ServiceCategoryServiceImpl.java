package com.community.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.center.entity.ServiceCategory;
import com.community.center.mapper.ServiceCategoryMapper;
import com.community.center.service.ServiceCategoryService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 服务分类服务实现类
 */
@Service
public class ServiceCategoryServiceImpl extends ServiceImpl<ServiceCategoryMapper, ServiceCategory> implements ServiceCategoryService {
    
    @Override
    public List<ServiceCategory> getAllCategories() {
        QueryWrapper<ServiceCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 只查询状态为启用的分类
        wrapper.orderByAsc("sort_order");
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public ServiceCategory getCategoryById(Long id) {
        return baseMapper.selectById(id);
    }
    
    @Override
    public boolean createCategory(ServiceCategory category) {
        category.setStatus(1); // 默认启用状态
        return baseMapper.insert(category) > 0;
    }
    
    @Override
    public boolean updateCategory(ServiceCategory category) {
        return baseMapper.updateById(category) > 0;
    }
    
    @Override
    public boolean deleteCategory(Long id) {
        // 软删除可以通过更新状态实现
        ServiceCategory category = new ServiceCategory();
        category.setId(id);
        category.setStatus(0); // 0表示禁用/删除
        return baseMapper.updateById(category) > 0;
    }
}
