package com.humanitarian.analyzer;

import com.humanitarian.config.AppConfig;
import com.humanitarian.model.CategoryDefinition;
import com.humanitarian.model.ReliefSentiment;
import com.humanitarian.model.SocialMediaPost;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates satisfaction per relief category loaded from relief-categories.json.
 */
public class ReliefSatisfactionAnalyzer
        implements Analyzer<Map<CategoryDefinition, ReliefSentiment>> {

    @Override
    public String getId() { return "relief_satisfaction"; }

    @Override
    public String getName() { return "Mức hài lòng theo loại cứu trợ"; }

    @Override
    public String getDescription() {
        return "Đánh giá phản hồi theo các danh mục cứu trợ được cấu hình trong JSON.";
    }

    @Override
    public Map<CategoryDefinition, ReliefSentiment> analyze(List<SocialMediaPost> posts) {
        AppConfig config = AppConfig.getInstance();
        List<CategoryDefinition> categories = config.getReliefCategories();
        Map<CategoryDefinition, ReliefSentiment> results = new LinkedHashMap<>();
        categories.forEach(category -> results.put(category, new ReliefSentiment(category)));

        for (SocialMediaPost post : posts) {
            String content = post.getContent() != null ? post.getContent().toLowerCase() : "";
            if (content.isEmpty() || post.getSentiment() == null) continue;

            for (CategoryDefinition category : categories) {
                boolean matches = category.getKeywords().stream()
                        .anyMatch(keyword -> content.contains(keyword.toLowerCase()));
                if (!matches) continue;

                ReliefSentiment relief = results.get(category);
                if (config.getPositiveSentimentId().equals(post.getSentiment())) relief.incrementPositive();
                else if (config.getNegativeSentimentId().equals(post.getSentiment())) relief.incrementNegative();
                else relief.incrementNeutral();
            }
        }

        return results;
    }
}
