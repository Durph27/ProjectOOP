package com.humanitarian.analyzer;

import com.humanitarian.config.AppConfig;
import com.humanitarian.model.CategoryDefinition;
import com.humanitarian.model.SocialMediaPost;
import com.humanitarian.model.TimeSentimentData;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Builds sentiment timelines for relief categories loaded from JSON.
 */
public class ReliefSentimentTimelineAnalyzer
        implements Analyzer<Map<CategoryDefinition, Map<LocalDate, TimeSentimentData>>> {

    @Override
    public String getId() { return "relief_sentiment_timeline"; }

    @Override
    public String getName() { return "Tâm lý theo loại cứu trợ theo thời gian"; }

    @Override
    public String getDescription() {
        return "Theo dõi phản hồi theo thời gian cho các danh mục cứu trợ cấu hình trong JSON.";
    }

    @Override
    public Map<CategoryDefinition, Map<LocalDate, TimeSentimentData>> analyze(List<SocialMediaPost> posts) {
        AppConfig config = AppConfig.getInstance();
        List<CategoryDefinition> categories = config.getReliefCategories();
        Map<CategoryDefinition, Map<LocalDate, TimeSentimentData>> results = new LinkedHashMap<>();
        categories.forEach(category -> results.put(category, new TreeMap<>()));

        for (SocialMediaPost post : posts) {
            String content = post.getContent() != null ? post.getContent().toLowerCase() : "";
            if (content.isEmpty() || post.getSentiment() == null || post.getDate() == null) continue;

            for (CategoryDefinition category : categories) {
                boolean matches = category.getKeywords().stream()
                        .anyMatch(keyword -> content.contains(keyword.toLowerCase()));
                if (!matches) continue;

                TimeSentimentData data = results.get(category)
                        .computeIfAbsent(post.getDate(), key -> new TimeSentimentData());
                if (config.getPositiveSentimentId().equals(post.getSentiment())) data.incrementPositive();
                else if (config.getNegativeSentimentId().equals(post.getSentiment())) data.incrementNegative();
                else data.incrementNeutral();
            }
        }

        return results;
    }
}
