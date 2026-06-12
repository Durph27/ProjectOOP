package com.humanitarian.model;

/**
 * Kết quả phân loại thiệt hại cho một bài đăng (Bài toán 2).
 */
public class DamageReport {
    private String postId;
    private CategoryDefinition category;
    private String sentiment;
    private double confidence;
    private String excerpt;          // Trích đoạn liên quan

    public DamageReport() {}

    public DamageReport(String postId, CategoryDefinition category, String sentiment,
                        double confidence, String excerpt) {
        this.postId = postId;
        this.category = category;
        this.sentiment = sentiment;
        this.confidence = confidence;
        this.excerpt = excerpt;
    }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public CategoryDefinition getCategory() { return category; }
    public void setCategory(CategoryDefinition category) { this.category = category; }

    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
}
