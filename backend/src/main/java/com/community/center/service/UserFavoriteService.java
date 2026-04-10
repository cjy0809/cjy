package com.community.center.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.center.entity.UserFavorite;

import java.util.List;

/**
 * 用户收藏服务接口
 */
public interface UserFavoriteService extends IService<UserFavorite> {
    
    /**
     * 分页查询用户收藏列表
     */
    IPage<UserFavorite> getUserFavoritesWithDetails(Page<UserFavorite> page, Long userId);
    
    /**
     * 根据用户ID和目标类型查询收藏列表
     */
    List<UserFavorite> getByUserIdAndTargetType(Long userId, String targetType);
    
    /**
     * 添加收藏
     */
    boolean addFavorite(Long userId, String targetType, Long targetId);
    
    /**
     * 取消收藏
     */
    boolean removeFavorite(Long userId, String targetType, Long targetId);
    
    /**
     * 检查是否已收藏
     */
    boolean isFavorited(Long userId, String targetType, Long targetId);
    
    /**
     * 切换收藏状态
     */
    boolean toggleFavorite(Long userId, String targetType, Long targetId);
}