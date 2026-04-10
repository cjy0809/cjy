package com.community.center.controller;

import com.community.center.entity.ServiceCategory;
import com.community.center.service.ServiceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 服务分类Controller
 */
@RestController
@RequestMapping("/api/service-category")
public class ServiceCategoryController {
    
    @Autowired
    private ServiceCategoryService serviceCategoryService;
    
    /**
     * 获取所有服务分类
     */
    @GetMapping
    public List<ServiceCategory> getAllCategories() {
        return serviceCategoryService.getAllCategories();
    }
    
    /**
     * 根据ID获取服务分类
     */
    @GetMapping("/{id}")
    public ServiceCategory getCategoryById(@PathVariable Long id) {
        return serviceCategoryService.getCategoryById(id);
    }
    
    /**
     * 创建服务分类
     */
    @PostMapping
    public boolean createCategory(@RequestBody ServiceCategory category) {
        return serviceCategoryService.createCategory(category);
    }
    
    /**
     * 更新服务分类
     */
    @PutMapping("/{id}")
    public boolean updateCategory(@PathVariable Long id, @RequestBody ServiceCategory category) {
        category.setId(id);
        return serviceCategoryService.updateCategory(category);
    }
    
    /**
     * 删除服务分类
     */
    @DeleteMapping("/{id}")
    public boolean deleteCategory(@PathVariable Long id) {
        return serviceCategoryService.deleteCategory(id);
    }
}
