package com.humanitarian.analyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collection;

/**
 * Registry quản lý các Analyzer instances.
 * 
 * DESIGN PATTERN: Registry
 * - Đăng ký analyzer mới tại runtime
 * - Hủy đăng ký analyzer không cần thiết
 * - Liệt kê tất cả analyzer
 * - Tích hợp với config để bật/tắt analyzer
 */
public class AnalyzerRegistry {
    private static final Logger logger = LoggerFactory.getLogger(AnalyzerRegistry.class);
    private static AnalyzerRegistry instance;
    private final Map<String, Analyzer<?>> analyzers = new LinkedHashMap<>();

    private AnalyzerRegistry() {}

    public static synchronized AnalyzerRegistry getInstance() {
        if (instance == null) {
            instance = new AnalyzerRegistry();
        }
        return instance;
    }

    /**
     * Đăng ký một analyzer mới.
     */
    public void register(Analyzer<?> analyzer) {
        analyzers.put(analyzer.getId(), analyzer);
        logger.info("Đăng ký analyzer: {} ({})", analyzer.getId(), analyzer.getName());
    }

    /**
     * Hủy đăng ký một analyzer.
     */
    public void unregister(String id) {
        Analyzer<?> removed = analyzers.remove(id);
        if (removed != null) {
            logger.info("Hủy đăng ký analyzer: {}", id);
        }
    }

    /**
     * Lấy analyzer theo ID.
     */
    @SuppressWarnings("unchecked")
    public <T> Analyzer<T> get(String id) {
        return (Analyzer<T>) analyzers.get(id);
    }

    /**
     * Trả về tất cả analyzer đã đăng ký.
     */
    public Collection<Analyzer<?>> getAll() {
        return analyzers.values();
    }

    /**
     * Trả về danh sách analyzer được bật.
     */
    public Collection<Analyzer<?>> getEnabled() {
        return analyzers.values().stream()
                .filter(Analyzer::isEnabled)
                .toList();
    }
}
