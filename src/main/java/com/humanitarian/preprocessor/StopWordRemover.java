package com.humanitarian.preprocessor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Loại bỏ stop words tiếng Việt phổ biến.
 * Giữ lại các từ có ý nghĩa cho phân tích sentiment.
 */
public class StopWordRemover implements TextPreprocessor {
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            // Đại từ, liên từ, giới từ thường gặp
            "và", "của", "là", "có", "cho", "được", "với", "các", "một",
            "này", "đã", "từ", "trong", "đến", "như", "những", "khi",
            "về", "hay", "hoặc", "mà", "cũng", "vì", "bởi", "nếu",
            "thì", "tại", "trên", "dưới", "sau", "trước", "giữa",
            "ra", "vào", "lên", "xuống", "đi", "lại", "đó", "đây",
            "ở", "bị", "do", "theo", "qua", "cùng", "bằng", "để",
            // Trạng từ không mang ý nghĩa sentiment
            "rồi", "vậy", "nào", "sao", "thế", "gì", "nào",
            "ai", "đâu", "bao", "mấy", "hết", "hơn", "nhất",
            // Từ đệm
            "ạ", "à", "ơi", "nhỉ", "nhé", "nha", "hen", "ha"
    ));

    @Override
    public String getName() { return "stopword_remover"; }

    @Override
    public String getDescription() { return "Loại bỏ stop words tiếng Việt"; }

    @Override
    public String process(String text) {
        String[] words = text.split("\\s+");
        StringJoiner joiner = new StringJoiner(" ");
        for (String word : words) {
            if (!STOP_WORDS.contains(word.toLowerCase())) {
                joiner.add(word);
            }
        }
        return joiner.toString();
    }

    @Override
    public int getOrder() { return 50; }
}
