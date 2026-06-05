package com.humanitarian.collector;

import com.humanitarian.model.SocialMediaPost;
import com.humanitarian.model.enums.Platform;

import java.time.LocalDate;
import java.util.List;

/**
 * Collector mẫu cho YouTube Data API.
 */
public class YoutubeCollector extends AbstractCollector {

    public YoutubeCollector() {
        super(Platform.YOUTUBE);
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
