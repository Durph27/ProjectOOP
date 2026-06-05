package com.humanitarian.preprocessor;

import java.util.regex.Pattern;

/**
 * Loại bỏ HTML tags khỏi văn bản.
 */
public class HtmlCleanerPreprocessor implements TextPreprocessor {
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern HTML_ENTITY_PATTERN = Pattern.compile("&[a-zA-Z]+;|&#\\d+;");

    @Override
    public String getName() { return "html_cleaner"; }

    @Override
    public String getDescription() { return "Loại bỏ HTML tags và entities"; }

    @Override
    public String process(String text) {
        String result = HTML_TAG_PATTERN.matcher(text).replaceAll(" ");
        result = HTML_ENTITY_PATTERN.matcher(result).replaceAll(" ");
        return result.replaceAll("\\s+", " ").trim();
    }

    @Override
    public int getOrder() { return 10; }
}
