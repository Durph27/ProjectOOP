package com.humanitarian.sentiment;

import com.humanitarian.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory cho SentimentModelProvider.
 * 
 * Chọn provider dựa trên config hoặc đăng ký provider mới.
 * Dễ dàng thay đổi mô hình:
 * - Config: sentiment.provider = "dictionary" hoặc "python-api"
 * - Code: register("my_model", new MySentimentProvider())
 */
public class SentimentModelFactory {
    private static final Logger logger = LoggerFactory.getLogger(SentimentModelFactory.class);
    private static SentimentModelFactory instance;
    private final Map<String, SentimentModelProvider> providers = new HashMap<>();

    private SentimentModelFactory() {
        // Đăng ký providers mặc định
        register("dictionary", new DictionaryBasedSentimentProvider());
        register("python-api", new PythonApiSentimentProvider());
    }

    public static synchronized SentimentModelFactory getInstance() {
        if (instance == null) {
            instance = new SentimentModelFactory();
        }
        return instance;
    }

    /**
     * Đăng ký một provider mới.
     */
    public void register(String name, SentimentModelProvider provider) {
        providers.put(name.toLowerCase(), provider);
        logger.info("Đăng ký sentiment provider: {} ({})", name, provider.getModelName());
    }

    /**
     * Lấy provider theo tên.
     */
    public SentimentModelProvider get(String name) {
        return providers.get(name.toLowerCase());
    }

    /**
     * Lấy provider được cấu hình trong app-config.json.
     * Fallback sang dictionary nếu provider được chọn không khả dụng.
     */
    public SentimentModelProvider getConfigured() {
        String providerName = AppConfig.getInstance().getSentimentProvider();
        SentimentModelProvider provider = get(providerName);

        if (provider == null || !provider.isAvailable()) {
            logger.warn("Provider '{}' không khả dụng. Sử dụng dictionary-based.", providerName);
            provider = get("dictionary");
        }

        return provider;
    }

    /**
     * Trả về tất cả providers đã đăng ký.
     */
    public Map<String, SentimentModelProvider> getAll() {
        return new HashMap<>(providers);
    }
}
