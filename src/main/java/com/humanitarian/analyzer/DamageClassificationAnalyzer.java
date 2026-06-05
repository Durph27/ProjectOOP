package com.humanitarian.analyzer;

import com.humanitarian.config.AppConfig;
import com.humanitarian.model.DamageReport;
import com.humanitarian.model.SocialMediaPost;
import com.humanitarian.model.enums.DamageCategory;

import java.util.*;

/**
 * BÀI TOÁN 2: Xác định mức độ và loại thiệt hại phổ biến nhất.
 * 
 * Phân loại bài đăng thành các danh mục thiệt hại dựa trên từ khóa
 * được cấu hình trong damage-categories.json.
 * 
 * ĐẦU VÀO: List<SocialMediaPost> đã có sentiment
 * ĐẦU RA: Map<DamageCategory, List<DamageReport>> - báo cáo thiệt hại theo loại
 */
public class DamageClassificationAnalyzer implements Analyzer<Map<DamageCategory, List<DamageReport>>> {

    @Override
    public String getId() { return "damage_classification"; }

    @Override
    public String getName() { return "Phân loại thiệt hại"; }

    @Override
    public String getDescription() {
        return "Phân loại bài đăng thành các danh mục thiệt hại: Người bị ảnh hưởng, " +
               "Gián đoạn kinh tế, Nhà cửa hư hỏng, Tài sản mất, Cơ sở hạ tầng hư hỏng, Khác. " +
               "Giúp xác định loại thiệt hại được quan tâm nhiều nhất.";
    }

    @Override
    public Map<DamageCategory, List<DamageReport>> analyze(List<SocialMediaPost> posts) {
        Map<DamageCategory, List<DamageReport>> results = new LinkedHashMap<>();

        // Khởi tạo map cho tất cả categories
        for (DamageCategory cat : DamageCategory.values()) {
            results.put(cat, new ArrayList<>());
        }

        // Lấy từ khóa từ config
        Map<String, List<String>> categoryKeywords = AppConfig.getInstance().getDamageCategoryKeywords();

        for (SocialMediaPost post : posts) {
            String content = post.getContent() != null ? post.getContent().toLowerCase() : "";
            if (content.isEmpty()) continue;

            // Phân loại bài đăng vào các danh mục dựa trên từ khóa
            boolean classified = false;
            for (Map.Entry<String, List<String>> entry : categoryKeywords.entrySet()) {
                String categoryId = entry.getKey();
                List<String> keywords = entry.getValue();

                int matchCount = 0;
                String matchedExcerpt = "";

                for (String keyword : keywords) {
                    if (content.contains(keyword.toLowerCase())) {
                        matchCount++;
                        // Trích xuất đoạn văn chứa từ khóa
                        int idx = content.indexOf(keyword.toLowerCase());
                        int start = Math.max(0, idx - 30);
                        int end = Math.min(content.length(), idx + keyword.length() + 30);
                        matchedExcerpt = "..." + content.substring(start, end) + "...";
                    }
                }

                if (matchCount > 0) {
                    DamageCategory category = DamageCategory.fromId(categoryId);
                    double confidence = Math.min(0.95, 0.3 + matchCount * 0.15);

                    DamageReport report = new DamageReport(
                            post.getId(), category, post.getSentiment(),
                            confidence, matchedExcerpt
                    );
                    results.get(category).add(report);
                    classified = true;
                }
            }

            // Nếu không khớp category nào → OTHER
            if (!classified && post.getSentiment() != null) {
                DamageReport report = new DamageReport(
                        post.getId(), DamageCategory.OTHER, post.getSentiment(),
                        0.3, content.length() > 60 ? content.substring(0, 60) + "..." : content
                );
                results.get(DamageCategory.OTHER).add(report);
            }
        }

        return results;
    }
}
