package com.humanitarian.collector;

import com.humanitarian.model.SocialMediaPost;

import java.time.LocalDate;
import java.util.List;

/**
 * Collector sample for Tiktok
 */
public class TiktokCollector extends AbstractCollector {

    public TiktokCollector() {
        super("tiktok");
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    protected List<SocialMediaPost> doCollect(List<String> keywords,
                                               LocalDate startDate, LocalDate endDate) {
        logger.info("TikTok collector chưa được implement. Sử dụng CSV collector thay thế.");
        return List.of();
    }
}
