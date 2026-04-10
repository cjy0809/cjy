package com.community.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.center.entity.Comment;
import com.community.center.entity.News;
import com.community.center.dto.response.NewsDetailResponse;
import com.community.center.dto.response.UserInfoResponse;
import com.community.center.service.NewsService;
import com.community.center.service.UserService;
import com.community.center.mapper.CommentMapper;
import com.community.center.mapper.NewsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 新闻服务实现类
 */
@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements NewsService {

    @Autowired
    private NewsMapper newsMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CommentMapper commentMapper;

    /**
     * 分页查询新闻列表
     */
    @Override
    public Page<News> getNewsPage(Integer pageNum, Integer pageSize, String title, Integer type, Integer status) {
        System.out.println("分页参数：pageNum=" + pageNum + ", pageSize=" + pageSize);
        Page<News> page = new Page<>(pageNum, pageSize);
        QueryWrapper<News> queryWrapper = new QueryWrapper<>();
        
        // 标题模糊查询
        if (title != null && !title.isEmpty()) {
            queryWrapper.like("title", title);
        }
        
        // 类型筛选
        if (type != null) {
            queryWrapper.eq("type", type);
        }
        
        // 状态筛选
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        // 按发布时间倒序排序
        queryWrapper.orderByDesc("publish_time");
        
        return newsMapper.selectPage(page, queryWrapper);
    }

    /**
     * 创建新闻
     */
    @Override
    public boolean createNews(News news) {
        // 设置发布时间为当前时间
        news.setPublishTime(LocalDateTime.now());
        // 初始化浏览次数为0
        news.setViewCount(0);
        
        // 如果没有设置发布者ID，则使用当前登录用户的ID
        if (news.getPublisherId() == null) {
            try {
                news.setPublisherId(com.community.center.common.SecurityUtil.getCurrentUserId());
            } catch (Exception e) {
                // 如果获取当前用户ID失败，使用默认值1
                news.setPublisherId(1L);
            }
        }
        
        return newsMapper.insert(news) > 0;
    }

    /**
     * 更新新闻
     */
    @Override
    public boolean updateNews(Long id, News news) {
        news.setId(id);
        return newsMapper.updateById(news) > 0;
    }

    /**
     * 删除新闻
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNews(Long id) {
        // 先删除该新闻的所有评论
        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.eq("target_type", "news");
        commentQueryWrapper.eq("target_id", id);
        commentMapper.delete(commentQueryWrapper);
        
        // 再删除新闻
        return newsMapper.deleteById(id) > 0;
    }

    /**
     * 更新新闻浏览次数
     */
    @Override
    public boolean updateViewCount(Long id) {
        News news = newsMapper.selectById(id);
        if (news != null) {
            news.setViewCount(news.getViewCount() + 1);
            return newsMapper.updateById(news) > 0;
        }
        return false;
    }

    /**
     * 获取新闻详情
     */
    @Override
    public NewsDetailResponse getNewsById(Long id) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            return null;
        }
        
        NewsDetailResponse response = NewsDetailResponse.fromNews(news);
        
        // 获取发布者信息
        if (news.getPublisherId() != null) {
            UserInfoResponse publisher = userService.getUserById(news.getPublisherId());
            if (publisher != null) {
                response.setPublisherName(publisher.getName());
            }
        }
        
        return response;
    }

    /**
     * 获取公告新闻列表（用于公告区域显示）
     */
    @Override
    public List<News> getAnnouncements(Integer limit) {
        QueryWrapper<News> queryWrapper = new QueryWrapper<>();
        
        // 只获取已发布的新闻
        queryWrapper.eq("status", 1);
        
        // 只获取公告类型的新闻（type=1）
        queryWrapper.eq("type", 1);
        
        // 按发布时间倒序排序
        queryWrapper.orderByDesc("publish_time");
        
        // 限制返回数量
        queryWrapper.last("LIMIT " + limit);
        
        return newsMapper.selectList(queryWrapper);
    }
}