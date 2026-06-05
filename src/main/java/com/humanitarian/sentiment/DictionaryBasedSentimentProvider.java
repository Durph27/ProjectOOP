package com.humanitarian.sentiment;

import com.humanitarian.model.SentimentResult;
import com.humanitarian.model.enums.Sentiment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Phân tích sentiment dựa trên từ điển tiếng Việt (rule-based).
 * Hoạt động hoàn toàn offline, không cần Python hoặc API bên ngoài.
 * 
 * Ưu điểm: Nhanh, đơn giản, chạy ngay
 * Nhược điểm: Không hiểu ngữ cảnh phức tạp, không xử lý tốt phủ định/mỉa mai
 * 
 * Có thể thay thế bằng PythonApiSentimentProvider để có độ chính xác cao hơn.
 */
public class DictionaryBasedSentimentProvider implements SentimentModelProvider {
    private static final Logger logger = LoggerFactory.getLogger(DictionaryBasedSentimentProvider.class);

    private final Set<String> positiveWords = new HashSet<>();
    private final Set<String> negativeWords = new HashSet<>();
    private final Set<String> negationWords = new HashSet<>();
    private final Set<String> intensifierWords = new HashSet<>();

    public DictionaryBasedSentimentProvider() {
        initDictionaries();
    }

    private void initDictionaries() {
        // === Từ TÍCH CỰC ===
        positiveWords.addAll(Arrays.asList(
                // Cảm xúc tích cực
                "vui", "mừng", "hạnh phúc", "phấn khởi", "cảm ơn", "biết ơn", "cảm động",
                "hy vọng", "lạc quan", "tin tưởng", "yêu thương", "đoàn kết", "ấm áp",
                "tuyệt vời", "xuất sắc", "tốt", "hay", "đẹp", "giỏi", "tốt lắm",
                // Hành động tích cực
                "cứu trợ", "hỗ trợ", "giúp đỡ", "quyên góp", "ủng hộ", "tình nguyện",
                "khắc phục", "phục hồi", "tái thiết", "cải thiện", "nỗ lực", "cố gắng",
                "kịp thời", "nhanh chóng", "hiệu quả", "thành công", "an toàn",
                // Đánh giá tích cực cứu trợ
                "đầy đủ", "chu đáo", "tận tình", "nhiệt tình", "chuyên nghiệp",
                "sẻ chia", "đồng lòng", "chung tay", "san sẻ", "chia sẻ",
                "khen ngợi", "đánh giá cao", "hài lòng", "phù hợp", "đảm bảo",
                // Từ chỉ cứu trợ tốt
                "kịp thời", "phân phối tốt", "đúng người", "minh bạch", "công bằng",
                // Emoji keywords (từ EmojiHandler)
                "vui", "yêu_thương", "tốt", "khen_ngợi", "cảm_ơn", "mạnh_mẽ",
                "vui_mừng", "đồng_ý"
        ));

        // === Từ TIÊU CỰC ===
        negativeWords.addAll(Arrays.asList(
                // Cảm xúc tiêu cực
                "buồn", "đau", "khổ", "lo lắng", "sợ hãi", "hoang mang", "thất vọng",
                "tức giận", "bức xúc", "bất bình", "phẫn nộ", "chán nản", "tuyệt vọng",
                "đau lòng", "xót xa", "thương tâm", "kinh hoàng", "khủng khiếp",
                // Thiệt hại
                "chết", "tử vong", "thiệt mạng", "mất tích", "thương vong",
                "thiệt hại", "hư hỏng", "phá hủy", "sập", "đổ", "ngập", "lũ",
                "sạt lở", "vỡ đê", "tàn phá", "tan hoang", "hoang tàn",
                // Vấn đề cứu trợ
                "chậm", "thiếu", "không đủ", "chưa đến", "không có",
                "tham nhũng", "ăn chặn", "không minh bạch", "bất công", "không công bằng",
                "mất mát", "khốn khổ", "cơ cực", "khó khăn", "nghiêm trọng",
                // Hạ tầng hư hỏng
                "mất điện", "mất nước", "mất sóng", "tê liệt", "gián đoạn",
                "cô lập", "chia cắt", "kẹt", "tắc", "nguy hiểm",
                // Emoji keywords
                "buồn", "khóc", "giận", "đau_lòng", "lo_lắng", "sợ_hãi",
                "bực_bội", "tệ", "thất_vọng", "cứu_giúp"
        ));

        // === Từ PHỦ ĐỊNH ===
        negationWords.addAll(Arrays.asList(
                "không", "chẳng", "chưa", "đừng", "hết", "mất",
                "thiếu", "không còn", "không thể", "chả"
        ));

        // === Từ TĂNG CƯỜNG ===
        intensifierWords.addAll(Arrays.asList(
                "rất", "quá", "cực", "vô cùng", "hết sức",
                "đặc biệt", "cực kỳ", "siêu", "lắm", "ghê"
        ));
    }

    @Override
    public String getModelName() {
        return "Dictionary-Based (Vietnamese)";
    }

    @Override
    public SentimentResult analyze(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new SentimentResult(Sentiment.NEUTRAL, 0.5, getModelName());
        }

        String lowerText = text.toLowerCase();
        String[] words = lowerText.split("\\s+");

        double positiveScore = 0;
        double negativeScore = 0;
        boolean negated = false;
        double intensifier = 1.0;

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            String bigram = (i < words.length - 1) ? word + " " + words[i + 1] : "";

            // Kiểm tra từ phủ định
            if (negationWords.contains(word)) {
                negated = true;
                continue;
            }

            // Kiểm tra từ tăng cường
            if (intensifierWords.contains(word)) {
                intensifier = 1.5;
                continue;
            }

            // Tính điểm
            boolean isPositive = positiveWords.contains(word) || positiveWords.contains(bigram);
            boolean isNegative = negativeWords.contains(word) || negativeWords.contains(bigram);

            if (isPositive) {
                if (negated) {
                    negativeScore += 1.0 * intensifier;
                } else {
                    positiveScore += 1.0 * intensifier;
                }
            }
            if (isNegative) {
                if (negated) {
                    positiveScore += 0.5 * intensifier; // Phủ định tiêu cực -> yếu hơn tích cực thường
                } else {
                    negativeScore += 1.0 * intensifier;
                }
            }

            // Reset negation và intensifier sau 2 từ
            if (i > 0 && !negationWords.contains(word) && !intensifierWords.contains(word)) {
                negated = false;
                intensifier = 1.0;
            }
        }

        // Kiểm tra thêm các cụm từ dài hơn trong text
        for (String phrase : positiveWords) {
            if (phrase.contains(" ") && lowerText.contains(phrase)) {
                positiveScore += 1.5;
            }
        }
        for (String phrase : negativeWords) {
            if (phrase.contains(" ") && lowerText.contains(phrase)) {
                negativeScore += 1.5;
            }
        }

        // Xác định sentiment
        double totalScore = positiveScore + negativeScore;
        if (totalScore == 0) {
            return new SentimentResult(Sentiment.NEUTRAL, 0.5, getModelName());
        }

        double confidence;
        Sentiment sentiment;

        if (positiveScore > negativeScore) {
            sentiment = Sentiment.POSITIVE;
            confidence = Math.min(0.95, 0.5 + (positiveScore - negativeScore) / (totalScore * 2));
        } else if (negativeScore > positiveScore) {
            sentiment = Sentiment.NEGATIVE;
            confidence = Math.min(0.95, 0.5 + (negativeScore - positiveScore) / (totalScore * 2));
        } else {
            sentiment = Sentiment.NEUTRAL;
            confidence = 0.5;
        }

        return new SentimentResult(sentiment, confidence, getModelName());
    }

    @Override
    public boolean isAvailable() {
        return true; // Luôn sẵn sàng vì không phụ thuộc bên ngoài
    }
}
