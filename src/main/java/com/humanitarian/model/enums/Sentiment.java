package com.humanitarian.model.enums;

/**
 * Loại cảm xúc/tâm lý trong phân tích sentiment.
 * Được sử dụng xuyên suốt ứng dụng để phân loại tâm lý công chúng.
 */
public enum Sentiment {
    POSITIVE("Tích cực", "Positive"),
    NEGATIVE("Tiêu cực", "Negative"),
    NEUTRAL("Trung lập", "Neutral");

    private final String nameVi;
    private final String nameEn;

    Sentiment(String nameVi, String nameEn) {
        this.nameVi = nameVi;
        this.nameEn = nameEn;
    }

    public String getNameVi() { return nameVi; }
    public String getNameEn() { return nameEn; }

    @Override
    public String toString() { return nameVi; }
}
