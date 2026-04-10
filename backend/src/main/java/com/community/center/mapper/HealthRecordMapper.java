package com.community.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.center.entity.HealthRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface HealthRecordMapper extends BaseMapper<HealthRecord> {
    /**
     * 根据ID查询健康记录
     * @param id 健康记录ID
     * @return 健康记录对象
     */
    HealthRecord selectById(@Param("id") Long id);

    /**
     * 查询健康记录列表
     * @param params 查询参数
     * @return 健康记录列表
     */
    List<HealthRecord> selectList(@Param("params") Map<String, Object> params);

    /**
     * 插入健康记录
     * @param healthRecord 健康记录对象
     * @return 插入影响的行数
     */
    int insert(HealthRecord healthRecord);

    /**
     * 更新健康记录
     * @param healthRecord 健康记录对象
     * @return 更新影响的行数
     */
    int updateById(HealthRecord healthRecord);

    /**
     * 删除健康记录
     * @param id 健康记录ID
     * @return 删除影响的行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据用户ID查询健康记录列表
     * @param userId 用户ID
     * @param params 查询参数
     * @return 健康记录列表
     */
    List<HealthRecord> selectByUserId(@Param("userId") Long userId, @Param("params") Map<String, Object> params);
}