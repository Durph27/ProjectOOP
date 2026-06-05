package com.humanitarian.storage;

import com.humanitarian.model.SocialMediaPost;
import java.util.List;

/**
 * Interface cho lưu trữ dữ liệu.
 * 
 * DESIGN: Strategy pattern cho storage backend.
 * Có thể triển khai JSON, CSV, hoặc Database.
 */
public interface DataStorage {

    /**
     * Lưu danh sách bài đăng.
     */
    void savePosts(List<SocialMediaPost> posts, String filename);

    /**
     * Đọc danh sách bài đăng.
     */
    List<SocialMediaPost> loadPosts(String filename);

    /**
     * Lưu kết quả phân tích dạng JSON string.
     */
    void saveResults(String jsonContent, String filename);

    /**
     * Đọc kết quả phân tích.
     */
    String loadResults(String filename);

    /**
     * Kiểm tra file tồn tại.
     */
    boolean exists(String filename);
}
