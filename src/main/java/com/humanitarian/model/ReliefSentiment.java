package com.humanitarian.model;

import com.humanitarian.model.enums.ReliefCategory;

/**
 * Kết quả phân tích mức hài lòng theo loại hàng cứu trợ (Bài toán 3).
 */
public class
ReliefSentiment {
    private ReliefCategory category;
    private int positiveCount;
    private int negativeCount;
    private int neutralCount;
    private double satisfactionRate;  // Tỷ lệ hài lòng (0.0 - 1.0)

    public ReliefSentiment() {}

    public ReliefSentiment(ReliefCategory category) {
        this.category = category;
        this.positiveCount = 0;
        this.negativeCount = 0;
        this.neutralCount = 0;
    }

    public void incrementPositive() { positiveCount++; recalculate(); }
    public void incrementNegative() { negativeCount++; recalculate(); }
    public void incrementNeutral() { neutralCount++; recalculate(); }

    private void recalculate() {
        int total = positiveCount + negativeCount + neutralCount;
        satisfactionRate = total > 0 ? (double) positiveCount / total : 0.0;
    }

    public int getTotalCount() { return positiveCount + negativeCount + neutralCount; }

    // Getters and Setters
    public ReliefCategory getCategory() { return category; }
    public void setCategory(ReliefCategory category) { this.category = category; }

    public int getPositiveCount() { return positiveCount; }
    public void setPositiveCount(int positiveCount) { this.positiveCount = positiveCount; recalculate(); }

    public int getNegativeCount() { return negativeCount; }
    public void setNegativeCount(int negativeCount) { this.negativeCount = negativeCount; recalculate(); }

    public int getNeutralCount() { return neutralCount; }
    public void setNeutralCount(int neutralCount) { this.neutralCount = neutralCount; recalculate(); }

    public double getSatisfactionRate() { return satisfactionRate; }
}
