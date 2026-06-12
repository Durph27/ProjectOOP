package com.humanitarian.analyzer;

import com.humanitarian.config.AppConfig;
import com.humanitarian.model.SocialMediaPost;
import com.humanitarian.model.TimeSentimentData;

import java.time.LocalDate;
import java.util.*;

/**
 * BÀI TOÁN 1: Theo dõi sự thay đổi tâm lý công chúng theo thời gian.
 * 
 * Phân tích số lượng post tích cực/tiêu cực/trung lập theo từng ngày
 * trong suốt và sau thảm họa.
 * 
 * ĐẦU VÀO: List<SocialMediaPost> đã có sentiment
 * ĐẦU RA: Map<LocalDate, TimeSentimentData> - dữ liệu sentiment theo ngày
 */
public class SentimentTimelineAnalyzer implements Analyzer<Map<LocalDate, TimeSentimentData>> {

    @Override
    public String getId() { return "sentiment_timeline"; }

    @Override
    public String getName() { return "Theo dõi tâm lý theo thời gian"; }

    @Override
    public String getDescription() {
        return "Phân tích sự thay đổi tâm lý công chúng (tích cực/tiêu cực) theo thời gian " +
               "trong suốt và sau thảm họa. Giúp xác định đỉnh điểm tiêu cực và thời điểm phục hồi.";
    }

    @Override
    public Map<LocalDate, TimeSentimentData> analyze(List<SocialMediaPost> posts) {
        Map<LocalDate, TimeSentimentData> timeline = new TreeMap<>();
        AppConfig config = AppConfig.getInstance();

        for (SocialMediaPost post : posts) {
            if (post.getDate() == null || post.getSentiment() == null) continue;

            LocalDate date = post.getDate();
            TimeSentimentData data = timeline.computeIfAbsent(date, k -> new TimeSentimentData());

            if (config.getPositiveSentimentId().equals(post.getSentiment())) data.incrementPositive();
            else if (config.getNegativeSentimentId().equals(post.getSentiment())) data.incrementNegative();
            else data.incrementNeutral();
        }

        return timeline;
    }
}
