package com.humanitarian.model;

import com.humanitarian.model.enums.DamageCategory;
import com.humanitarian.model.enums.Sentiment;

/**
 * Kết quả phân loại thiệt hại cho một bài đăng (Bài toán 2).
 */
public class DamageReport {
    private String postId;
    private DamageCategory category;
    private Sentiment sentiment;
    private double confidence;
    private String excerpt;          // Trích đoạn liên quan

    public DamageReport() {}

    public DamageReport(String postId, DamageCategory category, Sentiment sentiment,
                        double confidence, String excerpt) {
        this.postId = postId;
        this.category = category;
        this.sentiment = sentiment;
        this.confidence = confidence;
        this.excerpt = excerpt;
    }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public DamageCategory getCategory() { return category; }
    public void setCategory(DamageCategory category) { this.category = category; }

    public Sentiment getSentiment() { return sentiment; }
    public void setSentiment(Sentiment sentiment) { this.sentiment = sentiment; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
}
