package com.humanitarian.model;

/**
 * Dữ liệu sentiment tổng hợp cho một khoảng thời gian (Bài toán 1 & 4).
 * Chứa số lượng bài đăng tích cực/tiêu cực/trung lập trong một ngày.
 */
public class TimeSentimentData {
    private int positiveCount;
    private int negativeCount;
    private int neutralCount;

    public TimeSentimentData() {
        this.positiveCount = 0;
        this.negativeCount = 0;
        this.neutralCount = 0;
    }

    public TimeSentimentData(int positiveCount, int negativeCount, int neutralCount) {
        this.positiveCount = positiveCount;
        this.negativeCount = negativeCount;
        this.neutralCount = neutralCount;
    }

    public void incrementPositive() { positiveCount++; }
    public void incrementNegative() { negativeCount++; }
    public void incrementNeutral() { neutralCount++; }

    public int getTotal() { return positiveCount + negativeCount + neutralCount; }

    public double getPositiveRate() {
        int total = getTotal();
        return total > 0 ? (double) positiveCount / total : 0.0;
    }

    public double getNegativeRate() {
        int total = getTotal();
        return total > 0 ? (double) negativeCount / total : 0.0;
    }

    // Getters and Setters
    public int getPositiveCount() { return positiveCount; }
    public void setPositiveCount(int positiveCount) { this.positiveCount = positiveCount; }

    public int getNegativeCount() { return negativeCount; }
    public void setNegativeCount(int negativeCount) { this.negativeCount = negativeCount; }

    public int getNeutralCount() { return neutralCount; }
    public void setNeutralCount(int neutralCount) { this.neutralCount = neutralCount; }

    @Override
    public String toString() {
        return String.format("(+%d / -%d / ~%d)", positiveCount, negativeCount, neutralCount);
    }
}
