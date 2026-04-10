package com.community.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import com.community.center.entity.Venue;

import java.util.Map;

/**
 * 场地服务接口
 */
public interface VenueService extends IService<Venue> {

    /**
     * 分页查询场地列表
     * @param pageNum 当前页码
     * @param pageSize 每页条数
     * @param name 场地名称（模糊查询）
     * @param status 状态
     * @return 分页结果
     */
    Page<Venue> getVenuePage(Integer pageNum, Integer pageSize, String name, Integer status);

    /**
     * 根据ID查询场地详情
     * @param id 场地ID
     * @return 场地详情
     */
    Venue getVenueById(Long id);

    /**
     * 创建场地
     * @param venue 场地信息
     * @return 是否成功
     */
    boolean createVenue(Venue venue);

    /**
     * 更新场地
     * @param id 场地ID
     * @param venue 场地信息
     * @return 是否成功
     */
    boolean updateVenue(Long id, Venue venue);

    /**
     * 删除场地
     * @param id 场地ID
     * @return 是否成功
     */
    boolean deleteVenue(Long id);
    
    /**
     * 批量更新场地状态
     * @param params 批量更新参数（场地ID列表、维护日期、时间段、状态）
     * @return 是否成功
     */
    boolean batchUpdateVenueStatus(Map<String, Object> params);
    
    /**
     * 获取场地可用时间段
     * @param venueId 场地ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 时间段列表
     */

}