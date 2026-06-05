package com.humanitarian.collector;

import com.humanitarian.model.SocialMediaPost;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface Strategy cho thu thập dữ liệu từ các nguồn mạng xã hội.
 * 
 * DESIGN PATTERN: Strategy
 * - Mỗi nguồn dữ liệu implement interface này
 * - Dễ dàng thêm nguồn mới: implement interface + đăng ký vào Factory
 * - CollectionService không cần biết chi tiết của từng nguồn
 */
public interface DataCollector {

    /**
     * Tên nền tảng mạng xã hội.
     */
    String getPlatformName();

    /**
     * Thu thập bài đăng từ nguồn dữ liệu.
     *
     * @param keywords  Danh sách từ khóa tìm kiếm
     * @param startDate Ngày bắt đầu thu thập
     * @param endDate   Ngày kết thúc thu thập
     * @return Danh sách bài đăng thu thập được
     */
    List<SocialMediaPost> collect(List<String> keywords, LocalDate startDate, LocalDate endDate);

    /**
     * Kiểm tra xem nguồn dữ liệu có sẵn sàng (có API key, kết nối) hay không.
     */
    boolean isAvailable();

    /**
     * Mô tả ngắn về collector.
     */
    default String getDescription() {
        return "Thu thập dữ liệu từ " + getPlatformName();
    }
}
