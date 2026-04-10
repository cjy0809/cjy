package com.community.center.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.center.entity.News;
import com.community.center.dto.response.NewsDetailResponse;
import com.community.center.common.Result;
import com.community.center.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 新闻控制器
 */
@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    /**
     * 分页查询新闻列表
     */
    @GetMapping("/page")
    public Page<News> getNewsPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        Page<News> page = newsService.getNewsPage(pageNum, pageSize, title, type, status);
        System.out.println("NewsController返回的Page对象：");
        System.out.println("  总记录数：" + page.getTotal());
        System.out.println("  当前页：" + page.getCurrent());
        System.out.println("  每页大小：" + page.getSize());
        System.out.println("  总页数：" + page.getPages());
        System.out.println("  是否有前一页：" + page.hasPrevious());
        System.out.println("  是否有后一页：" + page.hasNext());
        System.out.println("  记录列表是否为空：" + page.getRecords().isEmpty());
        System.out.println("  记录数：" + page.getRecords().size());
        
        // 打印前3条记录的标题
        for (int i = 0; i < Math.min(3, page.getRecords().size()); i++) {
            News news = page.getRecords().get(i);
            System.out.println("  记录[" + i + "]标题：" + news.getTitle());
        }
        
        return page;
    }

    /**
     * 获取公告新闻列表（用于公告区域显示）
     */
    @GetMapping("/announcements")
    public List<News> getAnnouncements(@RequestParam(defaultValue = "5") Integer limit) {
        return newsService.getAnnouncements(limit);
    }

    /**
     * 获取新闻详情
     */
    @GetMapping("/{id}")
    public Result<NewsDetailResponse> getNewsById(@PathVariable Long id) {
        NewsDetailResponse news = newsService.getNewsById(id);
        if (news != null) {
            return Result.success(news);
        } else {
            return Result.error("新闻不存在");
        }
    }

    /**
     * 更新新闻浏览次数
     */
    @GetMapping("/{id}/view")
    public Boolean updateViewCount(@PathVariable Long id) {
        return newsService.updateViewCount(id);
    }

    /**
     * 创建新闻
     */
    @PostMapping
    public Boolean createNews(@RequestBody News news) {
        return newsService.createNews(news);
    }

    /**
     * 更新新闻
     */
    @PutMapping("/{id}")
    public Boolean updateNews(@PathVariable Long id, @RequestBody News news) {
        return newsService.updateNews(id, news);
    }

    /**
     * 删除新闻
     */
    @DeleteMapping("/{id}")
    public Boolean deleteNews(@PathVariable Long id) {
        return newsService.deleteNews(id);
    }
}