package com.humanitarian.analyzer;

import com.humanitarian.config.AppConfig;
import com.humanitarian.model.CategoryDefinition;
import com.humanitarian.model.DamageReport;
import com.humanitarian.model.SocialMediaPost;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Classifies damage reports using categories loaded from damage-categories.json.
 */
public class DamageClassificationAnalyzer
        implements Analyzer<Map<CategoryDefinition, List<DamageReport>>> {

    private static final int NEGATION_WINDOW_WORDS = 6;
    private static final Set<String> NEGATION_WORDS = Set.of(
            "không", "chưa", "chẳng", "chả"
    );
    private static final List<String> CLAUSE_BOUNDARIES = List.of(
            ".", ",", ";", ":", "!", "?", "\n",
            " nhưng ", " tuy nhiên ", " song ", " còn "
    );
    private static final Set<String> NON_NEGATING_FOLLOWERS = Set.of("chỉ", "những");

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
            String sourceContent = post.getRawContent() != null
                    ? post.getRawContent()
                    : post.getContent();
            String content = sourceContent != null ? sourceContent.toLowerCase() : "";
            if (content.isEmpty()) continue;

            boolean classified = false;
            for (CategoryDefinition category : categories) {
                int matchCount = 0;
                String matchedExcerpt = "";

                for (String keyword : category.getKeywords()) {
                    String normalizedKeyword = keyword.toLowerCase();
                    int matchIndex = findAffirmedKeywordIndex(content, normalizedKeyword);
                    if (matchIndex >= 0) {
                        matchCount++;
                        int start = Math.max(0, matchIndex - 30);
                        int end = Math.min(content.length(), matchIndex + normalizedKeyword.length() + 30);
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

    /**
     * Returns the first occurrence of a keyword that is not negated.
     * All occurrences are checked so "không mất điện nhưng sau đó mất điện"
     * is still classified from the second occurrence.
     */
    private int findAffirmedKeywordIndex(String content, String keyword) {
        int searchFrom = 0;
        while (searchFrom < content.length()) {
            int matchIndex = content.indexOf(keyword, searchFrom);
            if (matchIndex < 0) return -1;
            if (!isNegated(content, matchIndex)) return matchIndex;
            searchFrom = matchIndex + keyword.length();
        }
        return -1;
    }

    /**
     * Detects Vietnamese negation words within a short window in the same clause.
     */
    private boolean isNegated(String content, int keywordIndex) {
        int clauseStart = 0;
        for (String boundary : CLAUSE_BOUNDARIES) {
            int boundaryIndex = content.lastIndexOf(boundary, keywordIndex - 1);
            if (boundaryIndex >= 0) {
                clauseStart = Math.max(clauseStart, boundaryIndex + boundary.length());
            }
        }

        String prefix = content.substring(clauseStart, keywordIndex).trim();
        if (prefix.isEmpty()) return false;

        String[] words = prefix.split("\\s+");
        int start = Math.max(0, words.length - NEGATION_WINDOW_WORDS);
        String[] normalizedWords = Arrays.stream(words)
                .map(word -> word.replaceAll("[^\\p{L}]", ""))
                .toArray(String[]::new);

        for (int i = start; i < normalizedWords.length; i++) {
            if (!NEGATION_WORDS.contains(normalizedWords[i])) continue;

            boolean isNonNegatingConstruction = i + 1 < normalizedWords.length
                    && NON_NEGATING_FOLLOWERS.contains(normalizedWords[i + 1]);
            if (!isNonNegatingConstruction) return true;
        }
        return false;
    }
}
