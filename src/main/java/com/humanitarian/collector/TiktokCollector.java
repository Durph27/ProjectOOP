package com.humanitarian.collector;

import com.humanitarian.model.SocialMediaPost;
import com.humanitarian.model.enums.Platform;

import java.time.LocalDate;
import java.util.List;

/**
 * Collector mẫu cho TikTok API.
 */
public class TiktokCollector extends AbstractCollector {

    public TiktokCollector() {
        super(Platform.TIKTOK);
    }

    @Override
    public boolean isAvailable() {
        return false; // Cần TikTok API key
    }

    @Override
    protected List<SocialMediaPost> doCollect(List<String> keywords,
                                               LocalDate startDate, LocalDate endDate) {
        logger.info("TikTok collector chưa được implement. Sử dụng CSV collector thay thế.");
        return List.of();
    }
}
