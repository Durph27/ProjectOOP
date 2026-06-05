package com.humanitarian.collector;

import com.humanitarian.model.SocialMediaPost;
import com.humanitarian.model.enums.Platform;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Collector đọc dữ liệu từ file CSV.
 * Đây là collector chính để demo/import dữ liệu mẫu hoặc dữ liệu đã thu thập sẵn.
 * 
 * Format CSV: id, platform, content, author, timestamp, url, likes, shares, comments, location
 */
public class CsvFileCollector extends AbstractCollector {
    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    private String filePath;

    public CsvFileCollector() {
        super(Platform.CSV_IMPORT);
        this.filePath = "data/sample/yagi_sample.csv";
    }

    public CsvFileCollector(String filePath) {
        super(Platform.CSV_IMPORT);
        this.filePath = filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public boolean isAvailable() {
        return Files.exists(Paths.get(filePath));
    }

    @Override
    protected List<SocialMediaPost> doCollect(List<String> keywords,
                                               LocalDate startDate, LocalDate endDate) {
        List<SocialMediaPost> posts = new ArrayList<>();
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            logger.error("File CSV không tồn tại: {}", filePath);
            return posts;
        }

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8))) {
            // Bỏ qua header
            String[] header = reader.readNext();
            if (header == null) return posts;

            String[] line;
            int lineNum = 1;
            while ((line = reader.readNext()) != null) {
                lineNum++;
                try {
                    SocialMediaPost post = parseLine(line);
                    if (post != null && isInDateRange(post, startDate, endDate)) {
                        posts.add(post);
                    }
                } catch (Exception e) {
                    logger.warn("Lỗi parse dòng {}: {}", lineNum, e.getMessage());
                }
            }
        } catch (IOException | CsvValidationException e) {
            logger.error("Lỗi đọc file CSV: {}", filePath, e);
        }

        return posts;
    }

    private SocialMediaPost parseLine(String[] fields) {
        if (fields.length < 3) return null;

        SocialMediaPost post = new SocialMediaPost();
        post.setId(getField(fields, 0, "csv_" + System.nanoTime()));
        post.setPlatform(Platform.fromConfigKey(getField(fields, 1, "csv")));
        post.setRawContent(getField(fields, 2, ""));
        post.setContent(post.getRawContent());
        post.setAuthor(getField(fields, 3, "unknown"));
        post.setTimestamp(parseTimestamp(getField(fields, 4, "")));
        post.setUrl(getField(fields, 5, ""));

        if (fields.length > 6) post.setLikes(parseIntSafe(fields[6]));
        if (fields.length > 7) post.setShares(parseIntSafe(fields[7]));
        if (fields.length > 8) post.setComments(parseIntSafe(fields[8]));
        if (fields.length > 9) post.setLocation(fields[9].trim());

        return post;
    }

    private String getField(String[] fields, int index, String defaultValue) {
        return (index < fields.length && fields[index] != null && !fields[index].trim().isEmpty())
                ? fields[index].trim() : defaultValue;
    }

    private LocalDateTime parseTimestamp(String text) {
        if (text == null || text.isEmpty()) return LocalDateTime.now();

        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                // Thử parse dạng LocalDateTime trước
                return LocalDateTime.parse(text, fmt);
            } catch (DateTimeParseException e) {
                try {
                    // Thử parse dạng LocalDate rồi chuyển thành đầu ngày
                    return LocalDate.parse(text, fmt).atStartOfDay();
                } catch (DateTimeParseException e2) {
                    // Tiếp tục thử format khác
                }
            }
        }
        logger.warn("Không parse được timestamp: {}", text);
        return LocalDateTime.now();
    }

    private boolean isInDateRange(SocialMediaPost post, LocalDate start, LocalDate end) {
        if (post.getTimestamp() == null) return true;
        LocalDate postDate = post.getTimestamp().toLocalDate();
        return !postDate.isBefore(start) && !postDate.isAfter(end);
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
