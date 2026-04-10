package com.community.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.center.entity.ServiceItem;
import java.util.List;

/**
 * 服务项目服务接口
 * 支持三种服务状态：未启用(0)、启用中(1)、已下架(2)
 */
public interface ServiceItemService extends IService<ServiceItem> {
    
    /**
     * 分页获取服务项目列表
     * @param page 页码
     * @param size 每页大小
     * @param keyword 关键词
     * @param categoryId 分类ID
     * @return 分页结果
     */
    IPage<ServiceItem> getServiceItemPage(Integer page, Integer size, String keyword, Long categoryId);
    
    /**
     * 分页获取服务项目列表（支持状态筛选）
     * @param page 页码
     * @param size 每页大小
     * @param keyword 关键词
     * @param categoryId 分类ID
     * @param status 服务状态（0-未启用，1-启用中，2-已下架）
     * @return 分页结果
     */
    IPage<ServiceItem> getServiceItemPage(Integer page, Integer size, String keyword, Long categoryId, Integer status);
    
    /**
     * 根据分类ID获取服务项目列表
     */
    List<ServiceItem> getServiceItemsByCategory(Long categoryId);
    
    /**
     * 新增服务项目
     * 服务状态默认为启用中(1)
     */
    boolean createServiceItem(ServiceItem item);
    
    /**
     * 更新服务项目
     * 支持修改服务状态(0-未启用，1-启用中，2-已下架)
     */
    boolean updateServiceItem(ServiceItem item);
    
    /**
     * 删除服务项目
     * 物理删除：从数据库中彻底删除记录
     */
    boolean deleteServiceItem(Long id);
    
    /**
     * 获取所有服务项目（适配前端API调用）
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    IPage<ServiceItem> getServiceItems(Integer page, Integer size);
    
    /**
     * 获取服务项目详情
     * @param id 服务项目ID
     * @return 服务项目详情
     */
    ServiceItem getServiceItemDetail(Long id);
}