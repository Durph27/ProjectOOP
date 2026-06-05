package com.humanitarian.model.enums;

/**
 * Danh mục hàng cứu trợ thiết yếu.
 * Các giá trị tương ứng với cấu hình trong relief-categories.json.
 */
public enum ReliefCategory {
    SHELTER("Nhà ở", "Shelter/Housing"),
    TRANSPORTATION("Giao thông", "Transportation"),
    FOOD("Thực phẩm", "Food"),
    MEDICAL("Hỗ trợ y tế", "Medical Support"),
    CASH("Tiền mặt", "Cash/Financial Aid");

    private final String nameVi;
    private final String nameEn;

    ReliefCategory(String nameVi, String nameEn) {
        this.nameVi = nameVi;
        this.nameEn = nameEn;
    }

    public String getNameVi() { return nameVi; }
    public String getNameEn() { return nameEn; }

    public static ReliefCategory fromId(String id) {
        try {
            return valueOf(id.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString() { return nameVi; }
}
