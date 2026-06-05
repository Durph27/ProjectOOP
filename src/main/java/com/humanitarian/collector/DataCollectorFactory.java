package com.humanitarian.collector;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

/**
 * Factory pattern để tạo và quản lý các DataCollector.
 * 
 * DESIGN PATTERN: Factory + Registry
 * - Đăng ký collector mới: register("platform_name", new MyCollector())
 * - Lấy collector: get("platform_name")
 * - Liệt kê tất cả: getAll()
 * 
 * Để thêm nguồn dữ liệu mới:
 * 1. Implement DataCollector interface (hoặc extend AbstractCollector)
 * 2. Gọi DataCollectorFactory.getInstance().register("name", instance)
 */
public class DataCollectorFactory {
    private static DataCollectorFactory instance;
    private final Map<String, DataCollector> collectors = new HashMap<>();

    private DataCollectorFactory() {
        // Đăng ký các collector mặc định
        register("csv", new CsvFileCollector());
        register("twitter", new TwitterCollector());
        register("facebook", new FacebookCollector());
        register("tiktok", new TiktokCollector());
        register("youtube", new YoutubeCollector());
    }

    public static synchronized DataCollectorFactory getInstance() {
        if (instance == null) {
            instance = new DataCollectorFactory();
        }
        return instance;
    }

    /**
     * Đăng ký một collector mới hoặc thay thế collector hiện tại.
     */
    public void register(String platformKey, DataCollector collector) {
        collectors.put(platformKey.toLowerCase(), collector);
    }

    /**
     * Lấy collector theo platform key.
     */
    public DataCollector get(String platformKey) {
        return collectors.get(platformKey.toLowerCase());
    }

    /**
     * Hủy đăng ký một collector.
     */
    public void unregister(String platformKey) {
        collectors.remove(platformKey.toLowerCase());
    }

    /**
     * Trả về tất cả collector đã đăng ký.
     */
    public Collection<DataCollector> getAll() {
        return collectors.values();
    }

    /**
     * Trả về danh sách collector có sẵn (isAvailable = true).
     */
    public Collection<DataCollector> getAvailable() {
        return collectors.values().stream()
                .filter(DataCollector::isAvailable)
                .toList();
    }
}
