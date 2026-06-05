package com.humanitarian.model;

import com.humanitarian.model.enums.Sentiment;

/**
 * Kết quả phân tích sentiment cho một đoạn văn bản.
 * Được trả về bởi SentimentModelProvider.
 */
public class SentimentResult {
    private Sentiment sentiment;
    private double confidence;       // Độ tin cậy (0.0 - 1.0)
    private String modelName;        // Tên model đã sử dụng

    public SentimentResult() {}

    public SentimentResult(Sentiment sentiment, double confidence, String modelName) {
        this.sentiment = sentiment;
        this.confidence = confidence;
        this.modelName = modelName;
    }

    public Sentiment getSentiment() { return sentiment; }
    public void setSentiment(Sentiment sentiment) { this.sentiment = sentiment; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    @Override
    public String toString() {
        return String.format("%s (%.1f%% - %s)", sentiment.getNameVi(), confidence * 100, modelName);
    }
}
