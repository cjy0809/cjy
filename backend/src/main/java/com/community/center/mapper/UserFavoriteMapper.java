package com.community.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户收藏Mapper接口
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
    
    /**
     * 分页查询用户收藏列表
     */
    IPage<UserFavorite> selectUserFavoritesWithDetails(Page<UserFavorite> page, @Param("userId") Long userId);
    
    /**
     * 根据用户ID和目标类型查询收藏列表
     */
    List<UserFavorite> selectByUserIdAndTargetType(@Param("userId") Long userId, @Param("targetType") String targetType);
    
    /**
     * 检查是否已收藏
     */
    UserFavorite selectByUserAndTarget(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);
    
    /**
     * 删除收藏
     */
    int deleteByUserAndTarget(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);
}