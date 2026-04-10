package com.community.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.center.entity.ServiceCategory;
import java.util.List;

/**
 * 服务分类服务接口
 */
public interface ServiceCategoryService extends IService<ServiceCategory> {
    
    /**
     * 获取所有服务分类（状态为启用）
     */
    List<ServiceCategory> getAllCategories();
    
    /**
     * 根据ID获取服务分类
     */
    ServiceCategory getCategoryById(Long id);
    
    /**
     * 新增服务分类
     */
    boolean createCategory(ServiceCategory category);
    
    /**
     * 更新服务分类
     */
    boolean updateCategory(ServiceCategory category);
    
    /**
     * 删除服务分类
     */
    boolean deleteCategory(Long id);
}
