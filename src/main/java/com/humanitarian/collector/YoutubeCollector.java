package com.humanitarian.collector;

import com.humanitarian.model.SocialMediaPost;

import java.time.LocalDate;
import java.util.List;

/**
 * Collector mẫu cho YouTube Data API.
 */
public class YoutubeCollector extends AbstractCollector {

    public YoutubeCollector() {
        super("youtube");
    }

    @Override
    public boolean isAvailable() {
        return false; // Cần YouTube Data API key
    }

    @Override
    protected List<SocialMediaPost> doCollect(List<String> keywords,
                                               LocalDate startDate, LocalDate endDate) {
        logger.info("YouTube collector chưa được implement. Sử dụng CSV collector thay thế.");
        return List.of();
    }
}
