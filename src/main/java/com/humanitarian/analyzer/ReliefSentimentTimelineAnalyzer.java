package com.humanitarian.analyzer;

import com.humanitarian.config.AppConfig;
import com.humanitarian.model.SocialMediaPost;
import com.humanitarian.model.TimeSentimentData;
import com.humanitarian.model.enums.ReliefCategory;

import java.time.LocalDate;
import java.util.*;

/**
 * BÀI TOÁN 4: Theo dõi tâm lý theo từng loại hàng cứu trợ theo thời gian.
 * 
 * Kết hợp bài toán 1 (timeline) và bài toán 3 (relief categories):
 * Phân tích sentiment thay đổi theo thời gian cho từng loại cứu trợ.
 * 
 * Ý nghĩa: Xác định hiệu quả cứu trợ theo từng lĩnh vực theo thời gian.
 * Ví dụ: Tiền mặt/thực phẩm tích cực tăng nhanh, giao thông/nhà ở tiêu cực kéo dài.
 * 
 * ĐẦU VÀO: List<SocialMediaPost> đã có sentiment
 * ĐẦU RA: Map<ReliefCategory, Map<LocalDate, TimeSentimentData>>
 */
public class ReliefSentimentTimelineAnalyzer
        implements Analyzer<Map<ReliefCategory, Map<LocalDate, TimeSentimentData>>> {

    @Override
    public String getId() { return "relief_sentiment_timeline"; }

    @Override
    public String getName() { return "Tâm lý theo loại cứu trợ theo thời gian"; }

    @Override
    public String getDescription() {
        return "Theo dõi tâm lý công chúng thay đổi theo thời gian đối với từng danh mục " +
               "cứu trợ (tiền mặt, y tế, nhà ở, thực phẩm, giao thông). " +
               "Giúp xác định hiệu quả logistics nhân đạo theo từng lĩnh vực.";
    }

    @Override
    public Map<ReliefCategory, Map<LocalDate, TimeSentimentData>> analyze(List<SocialMediaPost> posts) {
        Map<ReliefCategory, Map<LocalDate, TimeSentimentData>> results = new LinkedHashMap<>();

        // Khởi tạo
        for (ReliefCategory cat : ReliefCategory.values()) {
            results.put(cat, new TreeMap<>());
        }

        // Lấy từ khóa từ config
        Map<String, List<String>> categoryKeywords = AppConfig.getInstance().getReliefCategoryKeywords();

        for (SocialMediaPost post : posts) {
            String content = post.getContent() != null ? post.getContent().toLowerCase() : "";
            if (content.isEmpty() || post.getSentiment() == null || post.getDate() == null) continue;

            LocalDate date = post.getDate();

            // Phân loại và thêm vào timeline
            for (Map.Entry<String, List<String>> entry : categoryKeywords.entrySet()) {
                String categoryId = entry.getKey();
                List<String> keywords = entry.getValue();

                boolean matches = keywords.stream()
                        .anyMatch(kw -> content.contains(kw.toLowerCase()));

                if (matches) {
                    ReliefCategory category = ReliefCategory.fromId(categoryId);
                    if (category == null) continue;

                    Map<LocalDate, TimeSentimentData> timeline = results.get(category);
                    TimeSentimentData data = timeline.computeIfAbsent(date, k -> new TimeSentimentData());

                    switch (post.getSentiment()) {
                        case POSITIVE -> data.incrementPositive();
                        case NEGATIVE -> data.incrementNegative();
                        case NEUTRAL -> data.incrementNeutral();
                    }
                }
            }
        }

        return results;
    }
}
