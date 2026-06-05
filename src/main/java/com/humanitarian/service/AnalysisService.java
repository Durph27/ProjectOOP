package com.humanitarian.service;

import com.humanitarian.analyzer.*;
import com.humanitarian.model.SocialMediaPost;
import com.humanitarian.model.SentimentResult;
import com.humanitarian.sentiment.SentimentModelFactory;
import com.humanitarian.sentiment.SentimentModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service điều phối quá trình phân tích.
 * 
 * Luồng xử lý:
 * 1. Gán sentiment cho tất cả bài đăng (sử dụng SentimentModelProvider)
 * 2. Chạy các Analyzer đã đăng ký
 * 3. Trả về kết quả tổng hợp
 */
public class AnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);
    private final AnalyzerRegistry registry;
    private final SentimentModelFactory sentimentFactory;

    public AnalysisService() {
        this.registry = AnalyzerRegistry.getInstance();
        this.sentimentFactory = SentimentModelFactory.getInstance();

        // Đăng ký 4 bài toán mặc định
        registry.register(new SentimentTimelineAnalyzer());
        registry.register(new DamageClassificationAnalyzer());
        registry.register(new ReliefSatisfactionAnalyzer());
        registry.register(new ReliefSentimentTimelineAnalyzer());
    }

    /**
     * Gán sentiment cho tất cả bài đăng.
     */
    public void assignSentiment(List<SocialMediaPost> posts) {
        SentimentModelProvider provider = sentimentFactory.getConfigured();
        logger.info("Đang phân tích sentiment bằng: {}", provider.getModelName());

        int count = 0;
        for (SocialMediaPost post : posts) {
            if (post.getContent() != null && !post.getContent().isEmpty()) {
                SentimentResult result = provider.analyze(post.getContent());
                post.setSentiment(result.getSentiment());
                post.setSentimentConfidence(result.getConfidence());
                count++;
            }
        }

        logger.info("Đã gán sentiment cho {} bài đăng", count);
    }

    /**
     * Chạy một analyzer cụ thể.
     */
    @SuppressWarnings("unchecked")
    public <T> T runAnalyzer(String analyzerId, List<SocialMediaPost> posts) {
        Analyzer<T> analyzer = registry.get(analyzerId);
        if (analyzer == null) {
            logger.error("Không tìm thấy analyzer: {}", analyzerId);
            return null;
        }

        logger.info("Chạy analyzer: {} - {}", analyzer.getId(), analyzer.getName());
        T result = analyzer.analyze(posts);
        logger.info("Analyzer {} hoàn thành", analyzer.getId());
        return result;
    }

    /**
     * Chạy tất cả analyzer đã đăng ký và trả về map kết quả.
     */
    public Map<String, Object> runAllAnalyzers(List<SocialMediaPost> posts) {
        Map<String, Object> results = new LinkedHashMap<>();

        for (Analyzer<?> analyzer : registry.getAll()) {
            try {
                logger.info("Chạy: {} - {}", analyzer.getId(), analyzer.getName());
                Object result = analyzer.analyze(posts);
                results.put(analyzer.getId(), result);
            } catch (Exception e) {
                logger.error("Lỗi chạy analyzer {}: {}", analyzer.getId(), e.getMessage());
            }
        }

        return results;
    }

    public AnalyzerRegistry getRegistry() { return registry; }
    public SentimentModelFactory getSentimentFactory() { return sentimentFactory; }
}
