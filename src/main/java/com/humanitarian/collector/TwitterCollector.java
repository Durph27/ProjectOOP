package com.humanitarian.collector;

import com.humanitarian.model.SocialMediaPost;
import com.humanitarian.model.enums.Platform;

import java.time.LocalDate;
import java.util.List;

/**
 * Collector mẫu cho Twitter/X API.
 * Cần API key để hoạt động. Hiện tại trả về danh sách rỗng.
 * 
 * Để sử dụng: Cung cấp API key trong app-config.json và implement doCollect().
 */
public class TwitterCollector extends AbstractCollector {
    private String apiKey;
    private String apiSecret;

    public TwitterCollector() {
        super(Platform.TWITTER);
    }

    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    protected List<SocialMediaPost> doCollect(List<String> keywords,
                                               LocalDate startDate, LocalDate endDate) {
        if (!isAvailable()) {
            logger.warn("Twitter API key chưa được cấu hình. Vui lòng cung cấp API key.");
            return List.of();
        }

        // TODO: Implement Twitter API v2 search
        // Sử dụng OkHttp để gọi Twitter API:
        // GET https://api.twitter.com/2/tweets/search/recent
        // Query: keywords joined by " OR "
        // start_time / end_time: ISO 8601
        logger.info("Twitter collector chưa được implement. Sử dụng CSV collector thay thế.");
        return List.of();
    }
}
