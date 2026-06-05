package com.humanitarian.preprocessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Chain of Responsibility - kết nối các bước tiền xử lý thành chuỗi.
 * 
 * Cho phép:
 * - Thêm/bỏ bước tiền xử lý tại runtime
 * - Tự động sắp xếp theo thứ tự (order)
 * - Bật/tắt từng bước
 */
public class PreprocessorChain {
    private static final Logger logger = LoggerFactory.getLogger(PreprocessorChain.class);
    private final List<TextPreprocessor> processors = new ArrayList<>();
    private final List<String> enabledNames = new ArrayList<>();

    /**
     * Thêm preprocessor vào chain.
     */
    public void addPreprocessor(TextPreprocessor preprocessor) {
        processors.add(preprocessor);
        processors.sort(Comparator.comparingInt(TextPreprocessor::getOrder));
    }

    /**
     * Đặt danh sách preprocessor được bật (theo tên).
     */
    public void setEnabledPreprocessors(List<String> names) {
        enabledNames.clear();
        enabledNames.addAll(names);
    }

    /**
     * Xử lý văn bản qua toàn bộ chain.
     */
    public String process(String text) {
        if (text == null || text.isEmpty()) return "";

        String result = text;
        for (TextPreprocessor processor : processors) {
            if (isEnabled(processor)) {
                try {
                    result = processor.process(result);
                } catch (Exception e) {
                    logger.error("Lỗi trong preprocessor {}: {}", processor.getName(), e.getMessage());
                }
            }
        }
        return result;
    }

    private boolean isEnabled(TextPreprocessor processor) {
        // Nếu không có config, bật tất cả
        if (enabledNames.isEmpty()) return true;
        return enabledNames.contains(processor.getName());
    }

    /**
     * Trả về danh sách tất cả preprocessor đã đăng ký.
     */
    public List<TextPreprocessor> getPreprocessors() {
        return new ArrayList<>(processors);
    }
}
