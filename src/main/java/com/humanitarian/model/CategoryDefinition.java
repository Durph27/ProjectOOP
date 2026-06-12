package com.humanitarian.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Dynamic category loaded from a JSON configuration file.
 */
public class CategoryDefinition {
    private String id;
    private String nameVi;
    private String nameEn;
    private List<String> keywords = new ArrayList<>();

    public CategoryDefinition() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNameVi() { return nameVi != null ? nameVi : id; }
    public void setNameVi(String nameVi) { this.nameVi = nameVi; }

    public String getNameEn() { return nameEn != null ? nameEn : id; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public List<String> getKeywords() {
        return keywords != null ? keywords : List.of();
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords != null ? keywords : new ArrayList<>();
    }

    @Override
    public String toString() { return getNameVi(); }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CategoryDefinition category)) return false;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
