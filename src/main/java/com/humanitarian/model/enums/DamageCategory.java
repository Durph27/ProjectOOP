package com.humanitarian.model.enums;

/**
 * Danh mục thiệt hại do thảm họa gây ra.
 * Các giá trị tương ứng với cấu hình trong damage-categories.json.
 */
public enum DamageCategory {
    PEOPLE_AFFECTED("Người bị ảnh hưởng", "People Affected"),
    ECONOMIC_DISRUPTION("Gián đoạn kinh tế sản xuất", "Economic/Production Disruption"),
    HOUSING_DAMAGE("Nhà cửa/tòa nhà bị hư hỏng", "Housing/Building Damage"),
    PERSONAL_PROPERTY_LOSS("Tài sản cá nhân bị mất", "Personal Property Loss"),
    INFRASTRUCTURE_DAMAGE("Cơ sở hạ tầng bị hư hỏng", "Infrastructure Damage"),
    OTHER("Khác", "Other");

    private final String nameVi;
    private final String nameEn;

    DamageCategory(String nameVi, String nameEn) {
        this.nameVi = nameVi;
        this.nameEn = nameEn;
    }

    public String getNameVi() { return nameVi; }
    public String getNameEn() { return nameEn; }

    public static DamageCategory fromId(String id) {
        try {
            return valueOf(id.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }

    @Override
    public String toString() { return nameVi; }
}
