package com.humanitarian.collector;

import com.humanitarian.model.SocialMediaPost;

import java.time.LocalDate;
import java.util.List;

/**
 * Collector mẫu cho Facebook Graph API.
 */
public class FacebookCollector extends AbstractCollector {

    public FacebookCollector() {
        super("facebook");
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    protected List<SocialMediaPost> doCollect(List<String> keywords,
                                               LocalDate startDate, LocalDate endDate) {
        logger.info("Facebook collector chưa được implement. Sử dụng CSV collector thay thế.");
        return List.of();
    }
}
