package com.humanitarian.collector;

import com.humanitarian.config.AppConfig;
import com.humanitarian.model.SocialMediaPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

/**
 * Lớp trừu tượng cơ sở cho các collector.
 * Cung cấp chức năng chung: logging, validate, filter.
 */
public abstract class AbstractCollector implements DataCollector {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final String platform;

    protected AbstractCollector(String platform) {
        this.platform = platform;
    }

    @Override
    public String getPlatformName() {
        return AppConfig.getInstance().getPlatformDisplayName(platform);
    }

    @Override
    public List<SocialMediaPost> collect(List<String> keywords, LocalDate startDate, LocalDate endDate) {
        logger.info("Bắt đầu thu thập từ {} ({} -> {})", getPlatformName(), startDate, endDate);

        if (keywords == null || keywords.isEmpty()) {
            logger.warn("Không có từ khóa nào được cung cấp");
            return List.of();
        }

        List<SocialMediaPost> posts = doCollect(keywords, startDate, endDate);
        logger.info("Thu thập được {} bài đăng từ {}", posts.size(), getPlatformName());
        return posts;
    }

    /**
     * Phương thức thực hiện thu thập cụ thể cho từng nền tảng.
     * Các lớp con cần override phương thức này.
     */
    protected abstract List<SocialMediaPost> doCollect(List<String> keywords,
                                                        LocalDate startDate, LocalDate endDate);
}
