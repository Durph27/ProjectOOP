package com.humanitarian.analyzer;

import com.humanitarian.config.AppConfig;
import com.humanitarian.model.CategoryDefinition;
import com.humanitarian.model.DamageReport;
import com.humanitarian.model.SocialMediaPost;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Classifies damage reports using categories loaded from damage-categories.json.
 */
public class DamageClassificationAnalyzer
        implements Analyzer<Map<CategoryDefinition, List<DamageReport>>> {

    @Override
    public String getId() { return "damage_classification"; }

    @Override
    public String getName() { return "Phân loại thiệt hại"; }

    @Override
    public String getDescription() {
        return "Phân loại bài đăng theo các danh mục thiệt hại được cấu hình trong JSON.";
    }

    @Override
    public Map<CategoryDefinition, List<DamageReport>> analyze(List<SocialMediaPost> posts) {
        List<CategoryDefinition> categories = AppConfig.getInstance().getDamageCategories();
        Map<CategoryDefinition, List<DamageReport>> results = new LinkedHashMap<>();
        categories.forEach(category -> results.put(category, new ArrayList<>()));

        CategoryDefinition other = categories.stream()
                .filter(category -> "OTHER".equalsIgnoreCase(category.getId()))
                .findFirst()
                .orElse(null);

        for (SocialMediaPost post : posts) {
            String content = post.getContent() != null ? post.getContent().toLowerCase() : "";
            if (content.isEmpty()) continue;

            boolean classified = false;
            for (CategoryDefinition category : categories) {
                int matchCount = 0;
                String matchedExcerpt = "";

                for (String keyword : category.getKeywords()) {
                    String normalizedKeyword = keyword.toLowerCase();
                    if (content.contains(normalizedKeyword)) {
                        matchCount++;
                        int index = content.indexOf(normalizedKeyword);
                        int start = Math.max(0, index - 30);
                        int end = Math.min(content.length(), index + normalizedKeyword.length() + 30);
                        matchedExcerpt = "..." + content.substring(start, end) + "...";
                    }
                }

                if (matchCount > 0) {
                    double confidence = Math.min(0.95, 0.3 + matchCount * 0.15);
                    results.get(category).add(new DamageReport(
                            post.getId(), category, post.getSentiment(), confidence, matchedExcerpt));
                    classified = true;
                }
            }

            if (!classified && post.getSentiment() != null && other != null) {
                String excerpt = content.length() > 60 ? content.substring(0, 60) + "..." : content;
                results.get(other).add(new DamageReport(
                        post.getId(), other, post.getSentiment(), 0.3, excerpt));
            }
        }

        return results;
    }
}
