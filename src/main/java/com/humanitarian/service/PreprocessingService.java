package com.humanitarian.service;

import com.humanitarian.config.AppConfig;
import com.humanitarian.model.SocialMediaPost;
import com.humanitarian.preprocessor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service điều phối quá trình tiền xử lý dữ liệu.
 * Tạo PreprocessorChain từ config và áp dụng lên danh sách bài đăng.
 */
public class PreprocessingService {
    private static final Logger logger = LoggerFactory.getLogger(PreprocessingService.class);
    private final PreprocessorChain chain;

    public PreprocessingService() {
        this.chain = new PreprocessorChain();

        // Đăng ký tất cả preprocessors có sẵn
        chain.addPreprocessor(new HtmlCleanerPreprocessor());
        chain.addPreprocessor(new UrlRemover());
        chain.addPreprocessor(new EmojiHandler());
        chain.addPreprocessor(new VietnameseNormalizer());
        chain.addPreprocessor(new StopWordRemover());

        // Cấu hình bật/tắt từ config
        List<String> enabled = AppConfig.getInstance().getEnabledPreprocessors();
        chain.setEnabledPreprocessors(enabled);
    }

    /**
     * Tiền xử lý danh sách bài đăng.
     * Content gốc được giữ trong rawContent, kết quả tiền xử lý ghi vào content.
     */
    public List<SocialMediaPost> preprocess(List<SocialMediaPost> posts) {
        logger.info("Bắt đầu tiền xử lý {} bài đăng...", posts.size());

        int processed = 0;
        for (SocialMediaPost post : posts) {
            if (post.getRawContent() != null) {
                String processedContent = chain.process(post.getRawContent());
                post.setContent(processedContent);
                processed++;
            }
        }

        logger.info("Đã tiền xử lý {} bài đăng", processed);
        return posts;
    }

    /**
     * Tiền xử lý một chuỗi văn bản đơn lẻ.
     */
    public String preprocess(String text) {
        return chain.process(text);
    }

    /**
     * Lấy chain để kiểm tra hoặc cấu hình thêm.
     */
    public PreprocessorChain getChain() {
        return chain;
    }
}
