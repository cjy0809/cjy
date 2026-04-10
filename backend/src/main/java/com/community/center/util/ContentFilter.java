package com.community.center.util;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 内容安全过滤工具类
 * 提供内容安全检查功能，包括敏感词过滤、恶意链接检测、脚本注入检测等
 * 用于保护社区平台的内容安全，防止不良信息和恶意代码传播
 */
@Component
public class ContentFilter {
    
    // 敏感词列表
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
        "违法", "欺诈", "诈骗", "赌博", "色情", "暴力", "毒品", 
        "反动", "恐怖", "谣言", "传销", "非法", "违禁",
        "垃圾", "广告", "推销", "兼职", "赚钱",
        "中奖", "抽奖", "红包", "福利", "领取", "点击", "链接"
    );
    
    // 恶意链接正则
    private static final Pattern MALICIOUS_URL_PATTERN = Pattern.compile(
        "(https?://)?([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(/[^\\s]*)?"
    );
    
    // 脚本注入正则
    private static final Pattern SCRIPT_INJECTION_PATTERN = Pattern.compile(
        "<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE
    );
    
    /**
     * 检查内容是否包含敏感词
     * @param content 待检查内容
     * @return 是否包含敏感词
     */
    public static boolean containsSensitiveWords(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        String lowerContent = content.toLowerCase();
        for (String word : SENSITIVE_WORDS) {
            if (lowerContent.contains(word)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查内容是否包含恶意链接
     * @param content 待检查内容
     * @return 是否包含恶意链接
     */
    public static boolean containsMaliciousUrl(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        return MALICIOUS_URL_PATTERN.matcher(content).find();
    }
    
    /**
     * 检查内容是否包含脚本注入
     * @param content 待检查内容
     * @return 是否包含脚本注入
     */
    public static boolean containsScriptInjection(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        return SCRIPT_INJECTION_PATTERN.matcher(content).find();
    }
    
    /**
     * 过滤敏感词，用*替换
     * @param content 原始内容
     * @return 过滤后的内容
     */
    public static String filterSensitiveWords(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }
        
        String result = content;
        for (String word : SENSITIVE_WORDS) {
            result = result.replaceAll("(?i)" + Pattern.quote(word), "***");
        }
        return result;
    }
    
    /**
     * 检查内容是否安全
     * 综合检查敏感词、恶意链接、脚本注入等安全问题
     * @param content 待检查内容
     * @return 检查结果
     */
    public static ContentCheckResult checkContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new ContentCheckResult(false, "内容不能为空");
        }
        
        // 检查敏感词
        if (containsSensitiveWords(content)) {
            return new ContentCheckResult(false, "内容包含敏感词，请修改后重新提交");
        }
        
        // 检查恶意链接
        if (containsMaliciousUrl(content)) {
            return new ContentCheckResult(false, "内容包含可疑链接，请修改后重新提交");
        }
        
        // 检查脚本注入
        if (containsScriptInjection(content)) {
            return new ContentCheckResult(false, "内容包含不安全代码，请修改后重新提交");
        }
        
        return new ContentCheckResult(true, "内容安全");
    }
    
    /**
     * 内容检查结果类
     * 封装内容安全检查的结果信息
     */
    public static class ContentCheckResult {
        private boolean safe;
        private String message;
        
        public ContentCheckResult(boolean safe, String message) {
            this.safe = safe;
            this.message = message;
        }
        
        public boolean isSafe() {
            return safe;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
