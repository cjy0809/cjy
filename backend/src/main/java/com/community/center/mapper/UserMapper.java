package com.community.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.center.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 更新用户默认头像
     */
    @Update("UPDATE user SET avatar = '/uploads/avatars/dafault-avatar.jpg' WHERE avatar IS NULL OR avatar = ''")
    int updateDefaultAvatar();
}