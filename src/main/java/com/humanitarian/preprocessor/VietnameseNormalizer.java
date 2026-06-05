package com.humanitarian.preprocessor;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Chuẩn hóa văn bản tiếng Việt:
 * - Chuẩn hóa Unicode (NFC)
 * - Loại bỏ ký tự đặc biệt thừa
 * - Chuẩn hóa khoảng trắng
 * - Chuyển chữ thường
 */
public class VietnameseNormalizer implements TextPreprocessor {
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[^\\p{L}\\p{N}\\s.,!?;:_#@]");
    private static final Pattern REPEATED_CHARS = Pattern.compile("(.)\\1{3,}");
    private static final Pattern REPEATED_PUNCTUATION = Pattern.compile("([!?.])\\1+");

    @Override
    public String getName() { return "vietnamese_normalizer"; }

    @Override
    public String getDescription() { return "Chuẩn hóa văn bản tiếng Việt (Unicode NFC, lowercase, ký tự đặc biệt)"; }

    @Override
    public String process(String text) {
        // Chuẩn hóa Unicode NFC
        String result = Normalizer.normalize(text, Normalizer.Form.NFC);

        // Chuyển chữ thường
        result = result.toLowerCase();

        // Rút gọn ký tự lặp (vd: "quáaaaa" -> "quáa")
        result = REPEATED_CHARS.matcher(result).replaceAll("$1$1");

        // Rút gọn dấu câu lặp (vd: "!!!" -> "!")
        result = REPEATED_PUNCTUATION.matcher(result).replaceAll("$1");

        // Loại bỏ ký tự đặc biệt (giữ letters, numbers, spaces, dấu câu cơ bản)
        result = SPECIAL_CHARS.matcher(result).replaceAll(" ");

        // Chuẩn hóa khoảng trắng
        result = MULTIPLE_SPACES.matcher(result).replaceAll(" ");

        return result.trim();
    }

    @Override
    public int getOrder() { return 40; }
}
