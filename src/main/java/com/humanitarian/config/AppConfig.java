package com.humanitarian.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton quản lý toàn bộ cấu hình ứng dụng.
 * Đọc từ file JSON bên ngoài, cho phép thay đổi cấu hình mà không cần biên dịch lại.
 * 
 * DESIGN: Singleton + Externalized Configuration
 * - Cho phép thay đổi từ khóa, danh mục thiệt hại, danh mục cứu trợ tại runtime
 * - Tất cả component đọc config từ đây, đảm bảo nhất quán
 */
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final String CONFIG_DIR = "config";
    private static AppConfig instance;

    private final Gson gson = new Gson();
    private JsonObject appConfig;
    private JsonObject keywordsConfig;
    private JsonObject damageCategoriesConfig;
    private JsonObject reliefCategoriesConfig;

    private AppConfig() {
        reload();
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    /**
     * Tải lại toàn bộ cấu hình từ file. Cho phép cập nhật runtime.
     */
    public void reload() {
        appConfig = loadJsonFile("app-config.json");
        keywordsConfig = loadJsonFile("keywords.json");
        damageCategoriesConfig = loadJsonFile("damage-categories.json");
        reliefCategoriesConfig = loadJsonFile("relief-categories.json");
        logger.info("Đã tải cấu hình ứng dụng thành công");
    }

    private JsonObject loadJsonFile(String filename) {
        Path filePath = Paths.get(CONFIG_DIR, filename);
        try {
            if (Files.exists(filePath)) {
                String content = Files.readString(filePath, StandardCharsets.UTF_8);
                return gson.fromJson(content, JsonObject.class);
            } else {
                // Thử đọc từ resources
                InputStream is = getClass().getClassLoader().getResourceAsStream("config/" + filename);
                if (is != null) {
                    return gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);
                }
            }
        } catch (IOException e) {
            logger.error("Lỗi đọc file cấu hình: " + filename, e);
        }
        logger.warn("Không tìm thấy file cấu hình: {}. Sử dụng cấu hình mặc định.", filename);
        return new JsonObject();
    }

    // ===== Disaster Config =====
    public String getDisasterName() {
        return getNestedString(appConfig, "disaster", "name", "Bão Yagi");
    }

    public LocalDate getDisasterStartDate() {
        String date = getNestedString(appConfig, "disaster", "startDate", "2024-09-01");
        return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public LocalDate getDisasterEndDate() {
        String date = getNestedString(appConfig, "disaster", "endDate", "2024-09-30");
        return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    // ===== Sentiment Config =====
    public String getSentimentProvider() {
        return getNestedString(appConfig, "sentiment", "provider", "dictionary");
    }

    public String getPythonApiUrl() {
        return getNestedString(appConfig, "sentiment", "pythonApiUrl", "http://localhost:5000");
    }

    public int getSentimentBatchSize() {
        return getNestedInt(appConfig, "sentiment", "batchSize", 50);
    }

    public int getSentimentTimeout() {
        return getNestedInt(appConfig, "sentiment", "timeout", 30000);
    }

    // ===== Keywords Config =====
    public List<String> getKeywords() {
        return getStringList(keywordsConfig, "keywords");
    }

    public List<String> getHashtags() {
        return getStringList(keywordsConfig, "hashtags");
    }

    public List<String> getExcludeKeywords() {
        return getStringList(keywordsConfig, "excludeKeywords");
    }

    // ===== Damage Categories Config =====
    /**
     * Trả về map: categoryId -> List<keyword> cho phân loại thiệt hại.
     */
    public Map<String, List<String>> getDamageCategoryKeywords() {
        return getCategoryKeywords(damageCategoriesConfig);
    }

    /**
     * Trả về map: categoryId -> nameVi cho hiển thị tên loại thiệt hại.
     */
    public Map<String, String> getDamageCategoryNames() {
        return getCategoryNames(damageCategoriesConfig);
    }

    // ===== Relief Categories Config =====
    /**
     * Trả về map: categoryId -> List<keyword> cho phân loại hàng cứu trợ.
     */
    public Map<String, List<String>> getReliefCategoryKeywords() {
        return getCategoryKeywords(reliefCategoriesConfig);
    }

    public Map<String, String> getReliefCategoryNames() {
        return getCategoryNames(reliefCategoriesConfig);
    }

    // ===== Preprocessing Config =====
    public List<String> getEnabledPreprocessors() {
        JsonObject preprocessing = appConfig.getAsJsonObject("preprocessing");
        if (preprocessing != null) {
            return getStringList(preprocessing, "enabledSteps");
        }
        return List.of("html_cleaner", "url_remover", "emoji_handler",
                "vietnamese_normalizer", "stopword_remover");
    }

    // ===== Analyzer Config =====
    public List<String> getEnabledAnalyzers() {
        JsonObject analyzers = appConfig.getAsJsonObject("analyzers");
        if (analyzers != null) {
            return getStringList(analyzers, "enabled");
        }
        return List.of("sentiment_timeline", "damage_classification",
                "relief_satisfaction", "relief_sentiment_timeline");
    }

    // ===== Storage Config =====
    public String getRawDataDir() {
        return getNestedString(appConfig, "storage", "rawDataDir", "data/raw");
    }

    public String getProcessedDataDir() {
        return getNestedString(appConfig, "storage", "processedDataDir", "data/processed");
    }

    public String getResultsDir() {
        return getNestedString(appConfig, "storage", "resultsDir", "data/results");
    }

    // ===== Helper Methods =====
    private String getNestedString(JsonObject obj, String key1, String key2, String defaultValue) {
        if (obj != null && obj.has(key1)) {
            JsonObject nested = obj.getAsJsonObject(key1);
            if (nested != null && nested.has(key2)) {
                return nested.get(key2).getAsString();
            }
        }
        return defaultValue;
    }

    private int getNestedInt(JsonObject obj, String key1, String key2, int defaultValue) {
        if (obj != null && obj.has(key1)) {
            JsonObject nested = obj.getAsJsonObject(key1);
            if (nested != null && nested.has(key2)) {
                return nested.get(key2).getAsInt();
            }
        }
        return defaultValue;
    }

    private List<String> getStringList(JsonObject obj, String key) {
        List<String> result = new ArrayList<>();
        if (obj != null && obj.has(key)) {
            JsonArray arr = obj.getAsJsonArray(key);
            for (JsonElement el : arr) {
                result.add(el.getAsString());
            }
        }
        return result;
    }

    private Map<String, List<String>> getCategoryKeywords(JsonObject configObj) {
        Map<String, List<String>> result = new HashMap<>();
        if (configObj != null && configObj.has("categories")) {
            JsonArray categories = configObj.getAsJsonArray("categories");
            for (JsonElement el : categories) {
                JsonObject cat = el.getAsJsonObject();
                String id = cat.get("id").getAsString();
                List<String> keywords = new ArrayList<>();
                if (cat.has("keywords")) {
                    for (JsonElement kw : cat.getAsJsonArray("keywords")) {
                        keywords.add(kw.getAsString());
                    }
                }
                result.put(id, keywords);
            }
        }
        return result;
    }

    private Map<String, String> getCategoryNames(JsonObject configObj) {
        Map<String, String> result = new HashMap<>();
        if (configObj != null && configObj.has("categories")) {
            JsonArray categories = configObj.getAsJsonArray("categories");
            for (JsonElement el : categories) {
                JsonObject cat = el.getAsJsonObject();
                String id = cat.get("id").getAsString();
                String name = cat.has("nameVi") ? cat.get("nameVi").getAsString() : id;
                result.put(id, name);
            }
        }
        return result;
    }
}
