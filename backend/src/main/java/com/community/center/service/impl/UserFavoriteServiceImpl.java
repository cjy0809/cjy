package com.community.center.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.center.entity.UserFavorite;
import com.community.center.mapper.UserFavoriteMapper;
import com.community.center.service.UserFavoriteService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户收藏服务实现类
 */
@Service
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> implements UserFavoriteService {
    
    @Override
    public IPage<UserFavorite> getUserFavoritesWithDetails(Page<UserFavorite> page, Long userId) {
        return baseMapper.selectUserFavoritesWithDetails(page, userId);
    }
    
    @Override
    public List<UserFavorite> getByUserIdAndTargetType(Long userId, String targetType) {
        return baseMapper.selectByUserIdAndTargetType(userId, targetType);
    }
    
    @Override
    public boolean addFavorite(Long userId, String targetType, Long targetId) {
        // 检查是否已收藏
        if (isFavorited(userId, targetType, targetId)) {
            return true; // 已收藏，直接返回成功
        }
        
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setTargetType(targetType);
        favorite.setTargetId(targetId);
        
        return save(favorite);
    }
    
    @Override
    public boolean removeFavorite(Long userId, String targetType, Long targetId) {
        return baseMapper.deleteByUserAndTarget(userId, targetType, targetId) > 0;
    }
    
    @Override
    public boolean isFavorited(Long userId, String targetType, Long targetId) {
        UserFavorite favorite = baseMapper.selectByUserAndTarget(userId, targetType, targetId);
        return favorite != null;
    }
    
    @Override
    public boolean toggleFavorite(Long userId, String targetType, Long targetId) {
        if (isFavorited(userId, targetType, targetId)) {
            return removeFavorite(userId, targetType, targetId);
        } else {
            return addFavorite(userId, targetType, targetId);
        }
    }
}