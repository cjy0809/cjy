package com.community.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.community.center.entity.Venue;
import com.community.center.service.VenueService;
import com.community.center.mapper.VenueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;


/**
 * 场地服务实现类
 */
@Service
public class VenueServiceImpl extends ServiceImpl<VenueMapper, Venue> implements VenueService {

    @Autowired
    private VenueMapper venueMapper;

    /**
     * 分页查询场地列表
     */
    @Override
    public Page<Venue> getVenuePage(Integer pageNum, Integer pageSize, String name, Integer status) {
        Page<Venue> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Venue> queryWrapper = new QueryWrapper<>();
        
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        
        if (status != null && !status.toString().isEmpty()) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByDesc("create_time");
        
        Page<Venue> resultPage = venueMapper.selectPage(page, queryWrapper);
        
        LocalDateTime now = LocalDateTime.now();
        
        for (Venue venue : resultPage.getRecords()) {
            if (venue.getMaintenanceDate() != null && venue.getMaintenanceStartTime() != null && venue.getMaintenanceEndTime() != null) {
                LocalDate maintenanceDate = venue.getMaintenanceDate().toLocalDate();
                LocalDate currentDate = now.toLocalDate();
                LocalDate maintenanceDateOnly = maintenanceDate;
                
                Integer newStatus = null;
                
                System.out.println("场地ID: " + venue.getId() + ", 场地名称: " + venue.getName());
                System.out.println("当前时间: " + now);
                System.out.println("维护日期: " + maintenanceDateOnly);
                System.out.println("维护时间段: " + venue.getMaintenanceStartTime() + "-" + venue.getMaintenanceEndTime());
                System.out.println("当前状态: " + venue.getStatus());
                
                if (currentDate.isBefore(maintenanceDateOnly)) {
                    newStatus = 1;
                    System.out.println("当前日期在维护日期之前，设置为可用");
                } else if (currentDate.isAfter(maintenanceDateOnly)) {
                    newStatus = 1;
                    System.out.println("当前日期在维护日期之后，设置为可用");
                } else {
                    String startTimeStr = venue.getMaintenanceStartTime();
                    String endTimeStr = venue.getMaintenanceEndTime();
                    
                    if (startTimeStr != null && !startTimeStr.isEmpty() && endTimeStr != null && !endTimeStr.isEmpty()) {
                        try {
                            LocalTime startTime = LocalTime.parse(startTimeStr);
                            LocalTime endTime = LocalTime.parse(endTimeStr);
                            LocalTime currentTime = now.toLocalTime();
                            
                            System.out.println("当前时间: " + currentTime + ", 维护开始时间: " + startTime + ", 维护结束时间: " + endTime);
                            
                            if (currentTime.isBefore(startTime) || currentTime.isAfter(endTime)) {
                                newStatus = 1;
                                System.out.println("当前时间在维护时间段之外，设置为可用");
                            } else {
                                newStatus = 2;
                                System.out.println("当前时间在维护时间段之内，设置为维护中");
                            }
                        } catch (Exception e) {
                            System.out.println("解析维护时间失败: " + e.getMessage());
                            newStatus = 1;
                        }
                    } else {
                        newStatus = 1;
                    }
                }
                
                if (newStatus != null && !newStatus.equals(venue.getStatus())) {
                    System.out.println("状态需要更新: 从 " + venue.getStatus() + " 更新为 " + newStatus);
                    venue.setStatus(newStatus);
                    
                    Venue updateVenue = new Venue();
                    updateVenue.setId(venue.getId());
                    updateVenue.setStatus(newStatus);
                    venueMapper.updateById(updateVenue);
                    System.out.println("数据库更新完成");
                } else {
                    System.out.println("状态无需更新");
                }
            }
        }
        
        return resultPage;
    }

    /**
     * 根据ID查询场地详情
     */
    @Override
    public Venue getVenueById(Long id) {
        return venueMapper.selectById(id);
    }

    /**
     * 创建场地
     */
    @Override
    public boolean createVenue(Venue venue) {
        return venueMapper.insert(venue) > 0;
    }

    /**
     * 更新场地
     */
    @Override
    public boolean updateVenue(Long id, Venue venue) {
        venue.setId(id);
        return venueMapper.updateById(venue) > 0;
    }

    /**
     * 删除场地
     */
    @Override
    public boolean deleteVenue(Long id) {
        return venueMapper.deleteById(id) > 0;
    }
    
    /**
     * 批量更新场地状态
     */
    @Override
    public boolean batchUpdateVenueStatus(Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> venueIds = (List<Long>) params.get("venueIds");
            String maintenanceDate = (String) params.get("maintenanceDate");
            String startTime = (String) params.get("startTime");
            String endTime = (String) params.get("endTime");
            
            if (venueIds == null || venueIds.isEmpty()) {
                System.out.println("批量更新失败：场地ID列表为空");
                return false;
            }
            
            if ((maintenanceDate == null || maintenanceDate.isEmpty()) || 
                (startTime == null || startTime.isEmpty()) || 
                (endTime == null || endTime.isEmpty())) {
                System.out.println("批量更新失败：必须同时提供维护日期和时间段");
                return false;
            }
            
            System.out.println("批量更新：maintenanceDate = " + maintenanceDate);
            System.out.println("批量更新：startTime = " + startTime);
            System.out.println("批量更新：endTime = " + endTime);
            
            Venue venue = new Venue();
            
            if (maintenanceDate != null && !maintenanceDate.isEmpty()) {
                try {
                    LocalDateTime maintenanceDateTime = LocalDateTime.parse(maintenanceDate + "T00:00:00");
                    venue.setMaintenanceDate(maintenanceDateTime);
                } catch (Exception e) {
                    System.out.println("批量更新：维护日期解析失败");
                    return false;
                }
            }
            
            if (startTime != null && !startTime.isEmpty()) {
                venue.setMaintenanceStartTime(startTime);
            }
            
            if (endTime != null && !endTime.isEmpty()) {
                venue.setMaintenanceEndTime(endTime);
            }
            
            QueryWrapper<Venue> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("id", venueIds);
            
            int updateCount = venueMapper.update(venue, queryWrapper);
            
            System.out.println("批量更新成功：更新了 " + updateCount + " 个场地");
            
            return updateCount > 0;
        } catch (Exception e) {
            System.out.println("批量更新失败：" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}