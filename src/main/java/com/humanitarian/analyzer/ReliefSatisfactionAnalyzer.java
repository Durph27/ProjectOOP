package com.humanitarian.analyzer;

import com.humanitarian.config.AppConfig;
import com.humanitarian.model.ReliefSentiment;
import com.humanitarian.model.SocialMediaPost;
import com.humanitarian.model.enums.ReliefCategory;
import com.humanitarian.model.enums.Sentiment;

import java.util.*;

/**
 * BÀI TOÁN 3: Xác định mức độ hài lòng và không hài lòng theo loại hàng cứu trợ.
 * 
 * Phân tích tâm lý tích cực/tiêu cực liên quan đến các loại hàng cứu trợ:
 * nhà ở, giao thông, thực phẩm, y tế, tiền mặt.
 * 
 * Ý nghĩa: Hỗ trợ ưu tiên phân bổ nguồn lực vào lĩnh vực có nhu cầu cấp bách nhất.
 * 
 * ĐẦU VÀO: List<SocialMediaPost> đã có sentiment
 * ĐẦU RA: Map<ReliefCategory, ReliefSentiment> - mức hài lòng theo loại cứu trợ
 */
public class ReliefSatisfactionAnalyzer implements Analyzer<Map<ReliefCategory, ReliefSentiment>> {

    @Override
    public String getId() { return "relief_satisfaction"; }

    @Override
    public String getName() { return "Mức hài lòng theo loại cứu trợ"; }

    @Override
    public String getDescription() {
        return "Đánh giá tâm lý tích cực/tiêu cực liên quan đến từng loại hàng cứu trợ " +
               "(nhà ở, giao thông, thực phẩm, y tế, tiền mặt). " +
               "Giúp xác định lĩnh vực cần ưu tiên phân bổ nguồn lực.";
    }

    @Override
    public Map<ReliefCategory, ReliefSentiment> analyze(List<SocialMediaPost> posts) {
        Map<ReliefCategory, ReliefSentiment> results = new LinkedHashMap<>();

        // Khởi tạo cho tất cả categories
        for (ReliefCategory cat : ReliefCategory.values()) {
            results.put(cat, new ReliefSentiment(cat));
        }

        // Lấy từ khóa từ config
        Map<String, List<String>> categoryKeywords = AppConfig.getInstance().getReliefCategoryKeywords();

        for (SocialMediaPost post : posts) {
            String content = post.getContent() != null ? post.getContent().toLowerCase() : "";
            if (content.isEmpty() || post.getSentiment() == null) continue;

            // Phân loại bài đăng vào các danh mục cứu trợ
            for (Map.Entry<String, List<String>> entry : categoryKeywords.entrySet()) {
                String categoryId = entry.getKey();
                List<String> keywords = entry.getValue();

                boolean matches = keywords.stream()
                        .anyMatch(kw -> content.contains(kw.toLowerCase()));

                if (matches) {
                    ReliefCategory category = ReliefCategory.fromId(categoryId);
                    if (category == null) continue;

                    ReliefSentiment relief = results.get(category);
                    switch (post.getSentiment()) {
                        case POSITIVE -> relief.incrementPositive();
                        case NEGATIVE -> relief.incrementNegative();
                        case NEUTRAL -> relief.incrementNeutral();
                    }
                }
            }
        }

        return results;
    }
}
