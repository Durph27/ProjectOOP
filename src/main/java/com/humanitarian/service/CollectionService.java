package com.humanitarian.service;

import com.humanitarian.collector.DataCollector;
import com.humanitarian.collector.DataCollectorFactory;
import com.humanitarian.config.AppConfig;
import com.humanitarian.model.SocialMediaPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service điều phối quá trình thu thập dữ liệu.
 * Kết hợp DataCollectorFactory với AppConfig để thu thập tự động.
 */
public class CollectionService {
    private static final Logger logger = LoggerFactory.getLogger(CollectionService.class);
    private final DataCollectorFactory collectorFactory;
    private final AppConfig config;

    public CollectionService() {
        this.collectorFactory = DataCollectorFactory.getInstance();
        this.config = AppConfig.getInstance();
    }

    /**
     * Thu thập dữ liệu từ một nguồn cụ thể.
     */
    public List<SocialMediaPost> collectFrom(String platformKey) {
        DataCollector collector = collectorFactory.get(platformKey);
        if (collector == null) {
            logger.error("Không tìm thấy collector cho platform: {}", platformKey);
            return List.of();
        }

        List<String> keywords = new ArrayList<>();
        keywords.addAll(config.getKeywords());
        keywords.addAll(config.getHashtags());

        LocalDate startDate = config.getDisasterStartDate();
        LocalDate endDate = config.getDisasterEndDate();

        return collector.collect(keywords, startDate, endDate);
    }

    /**
     * Thu thập dữ liệu từ tất cả nguồn có sẵn.
     */
    public List<SocialMediaPost> collectFromAll() {
        List<SocialMediaPost> allPosts = new ArrayList<>();

        for (DataCollector collector : collectorFactory.getAvailable()) {
            List<SocialMediaPost> posts = collectFrom(
                    collector.getPlatformName().toLowerCase().replace("/", "_"));
            allPosts.addAll(posts);
        }

        logger.info("Tổng cộng thu thập được {} bài đăng từ tất cả nguồn", allPosts.size());
        return allPosts;
    }

    /**
     * Lấy danh sách tất cả collector.
     */
    public DataCollectorFactory getCollectorFactory() {
        return collectorFactory;
    }
}
