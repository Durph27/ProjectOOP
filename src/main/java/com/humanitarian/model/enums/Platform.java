package com.humanitarian.model.enums;

/**
 * Nền tảng mạng xã hội được hỗ trợ thu thập dữ liệu.
 * Dễ dàng mở rộng thêm nền tảng mới bằng cách thêm giá trị enum.
 */
public enum Platform {
    TWITTER("Twitter/X", "twitter"),
    FACEBOOK("Facebook", "facebook"),
    TIKTOK("TikTok", "tiktok"),
    YOUTUBE("YouTube", "youtube"),
    CSV_IMPORT("CSV Import", "csv"),
    OTHER("Khác", "other");

    private final String displayName;
    private final String configKey;

    Platform(String displayName, String configKey) {
        this.displayName = displayName;
        this.configKey = configKey;
    }

    public String getDisplayName() { return displayName; }
    public String getConfigKey() { return configKey; }

    public static Platform fromConfigKey(String key) {
        for (Platform p : values()) {
            if (p.configKey.equalsIgnoreCase(key)) return p;
        }
        return OTHER;
    }

    @Override
    public String toString() { return displayName; }
}
