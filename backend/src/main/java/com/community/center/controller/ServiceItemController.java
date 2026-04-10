package com.community.center.controller;

import com.community.center.common.Result;
import com.community.center.common.ResultCode;
import com.community.center.entity.ServiceItem;
import com.community.center.entity.ServiceCategory;
import com.community.center.service.ServiceItemService;
import com.community.center.service.ServiceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 服务项目Controller
 * 支持三种服务状态：未启用(0)、启用中(1)、已下架(2)
 */
@RestController
@RequestMapping("/api/service-item")
public class ServiceItemController {
    
    @Autowired
    private ServiceItemService serviceItemService;
    
    @Autowired
    private ServiceCategoryService serviceCategoryService;
    
    /**
     * 分页获取服务项目列表
     * 支持按服务名称、分类ID和状态进行筛选
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> getServiceItemPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name, // 使用name代替keyword
            @RequestParam(required = false) Long type, // 使用type代替categoryId
            @RequestParam(required = false) Integer status) { // 添加状态筛选参数
        try {
            // 调用服务层方法，将name作为keyword，type作为categoryId，同时传递status参数
            IPage<ServiceItem> serviceItemPage = serviceItemService.getServiceItemPage(page, size, name, type, status);
            
            // 构建前端期望的响应格式
            Map<String, Object> result = new HashMap<>();
            result.put("records", serviceItemPage.getRecords());
            result.put("total", serviceItemPage.getTotal());
            result.put("size", serviceItemPage.getSize());
            result.put("current", serviceItemPage.getCurrent());
            result.put("pages", serviceItemPage.getPages());
            
            // 返回数据直接包装在data字段中
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "获取服务项目列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据分类ID获取服务项目列表
     */
    @GetMapping("/category/{categoryId}")
    public Result<List<ServiceItem>> getServiceItemsByCategory(@PathVariable Long categoryId) {
        try {
            List<ServiceItem> items = serviceItemService.getServiceItemsByCategory(categoryId);
            return Result.success(items);
        } catch (Exception e) {
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "获取服务项目失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有服务项目
     */
    @GetMapping("/all")
    public Result<List<ServiceItem>> getAllServiceItems() {
        try {
            // 调用分页查询获取所有服务项目，增加获取数量以确保包含所有服务
            IPage<ServiceItem> page = serviceItemService.getServiceItems(1, 1000);
            return Result.success(page.getRecords());
        } catch (Exception e) {
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "获取服务项目列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 适配前端API调用的方法
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getServiceItems(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 调用服务层新方法
            IPage<ServiceItem> serviceItemPage = serviceItemService.getServiceItems(page, size);
            
            // 构建前端期望的响应格式
            Map<String, Object> result = new HashMap<>();
            result.put("records", serviceItemPage.getRecords());
            result.put("total", serviceItemPage.getTotal());
            result.put("size", serviceItemPage.getSize());
            result.put("current", serviceItemPage.getCurrent());
            result.put("pages", serviceItemPage.getPages());
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "获取服务项目列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 新增服务项目
     * 服务状态默认为启用中(1)
     */
    @PostMapping
    public Result<Void> createServiceItem(@RequestBody ServiceItem item) {
        try {
            boolean success = serviceItemService.createServiceItem(item);
            if (success) {
                return Result.success();
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "新增服务项目失败");
            }
        } catch (Exception e) {
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "新增服务项目失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新服务项目
     * 支持修改服务状态(0-未启用，1-启用中，2-已下架)
     */
    @PutMapping("/{id}")
    public Result<Void> updateServiceItem(@PathVariable Long id, @RequestBody ServiceItem item) {
        try {
            item.setId(id);
            boolean success = serviceItemService.updateServiceItem(item);
            if (success) {
                return Result.success();
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "更新服务项目失败");
            }
        } catch (Exception e) {
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "更新服务项目失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除服务项目
     * 物理删除：从数据库中彻底删除记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteServiceItem(@PathVariable Long id) {
        try {
            boolean success = serviceItemService.deleteServiceItem(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "删除服务项目失败");
            }
        } catch (Exception e) {
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "删除服务项目失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取服务项目详情
     */
    @GetMapping("/{id}")
    public Result<ServiceItem> getServiceItemDetail(@PathVariable Long id) {
        try {
            // 添加参数验证
            if (id == null || id <= 0) {
                return Result.error(ResultCode.PARAM_VALIDATION_ERROR.getCode(), "服务项目ID不能为空或小于等于0");
            }
            
            // 添加日志记录
            System.out.println("获取服务项目详情，ID: " + id);
            
            ServiceItem item = serviceItemService.getServiceItemDetail(id);
            
            // 获取分类名称并添加到服务项中
            if (item != null && item.getCategoryId() != null) {
                ServiceCategory category = serviceCategoryService.getCategoryById(item.getCategoryId());
                if (category != null) {
                    item.setCategoryName(category.getName());
                }
            }
            
            return Result.success(item);
        } catch (Exception e) {
            // 添加详细的错误日志
            System.err.println("获取服务项目详情失败，ID: " + id + ", 错误信息: " + e.getMessage());
            e.printStackTrace();
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "获取服务项目详情失败: " + e.getMessage());
        }
    }
}