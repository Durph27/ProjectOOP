package com.humanitarian.analyzer;

import com.humanitarian.model.SocialMediaPost;
import java.util.List;

/**
 * Interface Strategy cho các bài toán phân tích.
 * 
 * DESIGN PATTERN: Strategy
 * - Mỗi bài toán là một implementation riêng
 * - Generic type T: kiểu dữ liệu kết quả phân tích
 * - Thêm bài toán mới: implement interface + đăng ký vào AnalyzerRegistry
 * - Bỏ bài toán: hủy đăng ký khỏi AnalyzerRegistry
 * 
 * @param <T> Kiểu kết quả phân tích
 */
public interface Analyzer<T> {

    /**
     * ID định danh duy nhất (dùng trong config).
     */
    String getId();

    /**
     * Tên hiển thị bài toán.
     */
    String getName();

    /**
     * Mô tả chi tiết bài toán.
     */
    String getDescription();

    /**
     * Thực hiện phân tích trên danh sách bài đăng.
     *
     * @param posts Danh sách bài đăng đã tiền xử lý và gán sentiment
     * @return Kết quả phân tích
     */
    T analyze(List<SocialMediaPost> posts);

    /**
     * Kiểm tra bài toán có được bật trong config không.
     */
    default boolean isEnabled() {
        return true;
    }
}
