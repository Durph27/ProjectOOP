package com.humanitarian.preprocessor;

/**
 * Interface Strategy cho các bước tiền xử lý văn bản.
 * 
 * DESIGN PATTERN: Strategy + Chain of Responsibility
 * - Mỗi bước tiền xử lý là một strategy độc lập
 * - Các strategy được kết nối thành chain qua PreprocessorChain
 * - Bật/tắt từng bước qua config, thêm bước mới chỉ cần implement interface
 */
public interface TextPreprocessor {

    /**
     * Tên định danh của preprocessor (dùng trong config).
     */
    String getName();

    /**
     * Mô tả chức năng.
     */
    String getDescription();

    /**
     * Xử lý văn bản đầu vào và trả về kết quả.
     */
    String process(String text);

    /**
     * Thứ tự ưu tiên (số nhỏ chạy trước).
     */
    int getOrder();
}
