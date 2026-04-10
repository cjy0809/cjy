package com.community.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.common.Result;

import com.community.center.entity.Venue;
import com.community.center.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 场地控制器
 * 处理社区中心场地的创建、查询、更新、删除等操作
 */
@Tag(name = "场地管理")
@RestController
@RequestMapping("/api/venue")
public class VenueController {

    @Autowired
    private VenueService venueService;

    /**
     * 分页查询场地列表
     */
    @Operation(summary = "分页查询场地列表")
    @GetMapping("/page")
    public Result<Page<Venue>> getVenuePage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "场地名称") @RequestParam(required = false) String name,
            @Parameter(description = "场地状态") @RequestParam(required = false) Integer status) {
        Page<Venue> page = venueService.getVenuePage(pageNum, pageSize, name, status);
        return Result.success(page);
    }

    /**
     * 根据ID查询场地详情
     */
    @Operation(summary = "根据ID查询场地详情")
    @GetMapping("/{id}")
    public Result<Venue> getVenueById(@Parameter(description = "场地ID", required = true) @PathVariable Long id) {
        Venue venue = venueService.getVenueById(id);
        return Result.success(venue);
    }

    /**
     * 创建场地
     */
    @Operation(summary = "创建场地")
    @PostMapping
    public Result<Boolean> createVenue(@Parameter(description = "场地信息", required = true) @RequestBody Venue venue) {
        boolean success = venueService.createVenue(venue);
        return Result.success(success);
    }

    /**
     * 批量更新场地状态
     */
    @Operation(summary = "批量更新场地状态")
    @PutMapping("/batch-status")
    public Result<Boolean> batchUpdateVenueStatus(@Parameter(description = "批量更新参数", required = true) @RequestBody Map<String, Object> params) {
        boolean success = venueService.batchUpdateVenueStatus(params);
        return Result.success(success);
    }

    /**
     * 更新场地
     */
    @Operation(summary = "更新场地")
    @PutMapping("/{id}")
    public Result<Boolean> updateVenue(@Parameter(description = "场地ID", required = true) @PathVariable Long id, 
                                      @Parameter(description = "场地信息", required = true) @RequestBody Venue venue) {
        boolean success = venueService.updateVenue(id, venue);
        return Result.success(success);
    }

    /**
     * 删除场地
     */
    @Operation(summary = "删除场地")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteVenue(@Parameter(description = "场地ID", required = true) @PathVariable Long id) {
        boolean success = venueService.deleteVenue(id);
        return Result.success(success);
    }
    

}
