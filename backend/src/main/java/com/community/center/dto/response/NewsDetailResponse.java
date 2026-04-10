package com.community.center.dto.response;

import com.community.center.entity.News;
import lombok.Data;

/**
 * 新闻详情响应类
 */
@Data
public class NewsDetailResponse {
    
    private Long id;
    
    private String title;
    
    private String content;
    
    private Integer type;
    
    private Long publisherId;
    
    private String publisherName;
    
    private String publishTime;
    
    private Integer status;
    
    private Integer viewCount;
    
    private String createTime;
    
    private String updateTime;
    
    /**
     * 从News实体转换
     */
    public static NewsDetailResponse fromNews(News news) {
        NewsDetailResponse response = new NewsDetailResponse();
        response.setId(news.getId());
        response.setTitle(news.getTitle());
        response.setContent(news.getContent());
        response.setType(news.getType());
        response.setPublisherId(news.getPublisherId());
        response.setPublishTime(news.getPublishTime().toString());
        response.setStatus(news.getStatus());
        response.setViewCount(news.getViewCount());
        response.setCreateTime(news.getCreateTime().toString());
        response.setUpdateTime(news.getUpdateTime().toString());
        return response;
    }
}