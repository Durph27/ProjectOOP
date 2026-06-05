package com.humanitarian.preprocessor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Xử lý emoji: chuyển đổi emoji phổ biến thành từ khóa cảm xúc tiếng Việt
 * để hỗ trợ phân tích sentiment.
 */
public class EmojiHandler implements TextPreprocessor {
    private static final Map<String, String> EMOJI_MAP = new HashMap<>();
    private static final Pattern EMOJI_PATTERN;

    static {
        // Emoji tích cực
        EMOJI_MAP.put("😀", " vui ");
        EMOJI_MAP.put("😃", " vui ");
        EMOJI_MAP.put("😄", " vui ");
        EMOJI_MAP.put("😊", " vui ");
        EMOJI_MAP.put("🥰", " yêu_thương ");
        EMOJI_MAP.put("😍", " yêu_thương ");
        EMOJI_MAP.put("❤️", " yêu_thương ");
        EMOJI_MAP.put("❤", " yêu_thương ");
        EMOJI_MAP.put("💕", " yêu_thương ");
        EMOJI_MAP.put("👍", " tốt ");
        EMOJI_MAP.put("👏", " khen_ngợi ");
        EMOJI_MAP.put("🙏", " cảm_ơn ");
        EMOJI_MAP.put("💪", " mạnh_mẽ ");
        EMOJI_MAP.put("🎉", " vui_mừng ");
        EMOJI_MAP.put("✅", " đồng_ý ");

        // Emoji tiêu cực
        EMOJI_MAP.put("😢", " buồn ");
        EMOJI_MAP.put("😭", " khóc ");
        EMOJI_MAP.put("😡", " giận ");
        EMOJI_MAP.put("😠", " giận ");
        EMOJI_MAP.put("💔", " đau_lòng ");
        EMOJI_MAP.put("😰", " lo_lắng ");
        EMOJI_MAP.put("😱", " sợ_hãi ");
        EMOJI_MAP.put("😤", " bực_bội ");
        EMOJI_MAP.put("👎", " tệ ");
        EMOJI_MAP.put("😞", " thất_vọng ");
        EMOJI_MAP.put("😔", " buồn ");
        EMOJI_MAP.put("🆘", " cứu_giúp ");

        // Build regex pattern cho tất cả emoji còn lại (Unicode)
        EMOJI_PATTERN = Pattern.compile("[\\x{1F600}-\\x{1F64F}\\x{1F300}-\\x{1F5FF}" +
                "\\x{1F680}-\\x{1F6FF}\\x{1F1E0}-\\x{1F1FF}\\x{2600}-\\x{26FF}\\x{2700}-\\x{27BF}]");
    }

    @Override
    public String getName() { return "emoji_handler"; }

    @Override
    public String getDescription() { return "Chuyển đổi emoji thành từ khóa cảm xúc tiếng Việt"; }

    @Override
    public String process(String text) {
        String result = text;

        // Thay thế emoji đã biết bằng từ khóa
        for (Map.Entry<String, String> entry : EMOJI_MAP.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        // Loại bỏ emoji còn lại
        result = EMOJI_PATTERN.matcher(result).replaceAll(" ");

        return result.replaceAll("\\s+", " ").trim();
    }

    @Override
    public int getOrder() { return 30; }
}
