package com.humanitarian.model;

import com.humanitarian.model.enums.Platform;
import com.humanitarian.model.enums.Sentiment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model chính đại diện cho một bài đăng mạng xã hội.
 * Lưu trữ cả nội dung gốc (rawContent) và nội dung đã xử lý (content).
 */
public class SocialMediaPost {
    private String id;
    private Platform platform;
    private String rawContent;       // Nội dung gốc trước tiền xử lý
    private String content;          // Nội dung sau tiền xử lý
    private String author;
    private LocalDateTime timestamp;
    private String url;
    private int likes;
    private int shares;
    private int comments;
    private String location;         // Vị trí (nếu có)

    // Kết quả phân tích (được gán sau khi phân tích)
    private Sentiment sentiment;
    private double sentimentConfidence;

    public SocialMediaPost() {}

    public SocialMediaPost(String id, Platform platform, String content,
                           String author, LocalDateTime timestamp) {
        this.id = id;
        this.platform = platform;
        this.rawContent = content;
        this.content = content;
        this.author = author;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Platform getPlatform() { return platform; }
    public void setPlatform(Platform platform) { this.platform = platform; }

    public String getRawContent() { return rawContent; }
    public void setRawContent(String rawContent) { this.rawContent = rawContent; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; }

    public int getComments() { return comments; }
    public void setComments(int comments) { this.comments = comments; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Sentiment getSentiment() { return sentiment; }
    public void setSentiment(Sentiment sentiment) { this.sentiment = sentiment; }

    public double getSentimentConfidence() { return sentimentConfidence; }
    public void setSentimentConfidence(double sentimentConfidence) {
        this.sentimentConfidence = sentimentConfidence;
    }

    /**
     * Trả về ngày đăng bài (không bao gồm giờ).
     */
    public java.time.LocalDate getDate() {
        return timestamp != null ? timestamp.toLocalDate() : null;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s: %s",
                platform,
                timestamp != null ? timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A",
                author,
                content != null && content.length() > 80 ? content.substring(0, 80) + "..." : content);
    }
}
