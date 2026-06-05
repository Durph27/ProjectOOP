package com.humanitarian.preprocessor;

import java.util.regex.Pattern;

/**
 * Loại bỏ URLs khỏi văn bản.
 */
public class UrlRemover implements TextPreprocessor {
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)", Pattern.CASE_INSENSITIVE);

    @Override
    public String getName() { return "url_remover"; }

    @Override
    public String getDescription() { return "Loại bỏ URLs"; }

    @Override
    public String process(String text) {
        return URL_PATTERN.matcher(text).replaceAll(" ").replaceAll("\\s+", " ").trim();
    }

    @Override
    public int getOrder() { return 20; }
}
