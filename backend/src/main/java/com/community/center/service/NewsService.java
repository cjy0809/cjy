package com.community.center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.entity.News;
import com.community.center.dto.response.NewsDetailResponse;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 新闻服务接口
 */
public interface NewsService extends IService<News> {

    /**
     * 分页查询新闻列表
     * @param pageNum 当前页码
     * @param pageSize 每页条数
     * @param title 标题（模糊查询）
     * @param type 类型
     * @param status 状态
     * @return 分页结果
     */
    Page<News> getNewsPage(Integer pageNum, Integer pageSize, String title, Integer type, Integer status);

    /**
     * 创建新闻
     * @param news 新闻信息
     * @return 是否成功
     */
    boolean createNews(News news);

    /**
     * 更新新闻
     * @param id 新闻ID
     * @param news 新闻信息
     * @return 是否成功
     */
    boolean updateNews(Long id, News news);

    /**
     * 删除新闻
     * @param id 新闻ID
     * @return 是否成功
     */
    boolean deleteNews(Long id);

    /**
     * 更新新闻浏览次数
     * @param id 新闻ID
     * @return 是否成功
     */
    boolean updateViewCount(Long id);

    /**
     * 获取新闻详情
     * @param id 新闻ID
     * @return 新闻详情
     */
    NewsDetailResponse getNewsById(Long id);

    /**
     * 获取公告新闻列表（用于公告区域显示）
     * @param limit 返回数量限制
     * @return 公告新闻列表
     */
    List<News> getAnnouncements(Integer limit);
}