package com.humanitarian.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.reflect.TypeToken;
import com.humanitarian.model.SocialMediaPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lưu trữ dữ liệu dưới dạng JSON files.
 */
public class JsonFileStorage implements DataStorage {
    private static final Logger logger = LoggerFactory.getLogger(JsonFileStorage.class);
    private final Gson gson;
    private final String baseDir;

    public JsonFileStorage(String baseDir) {
        this.baseDir = baseDir;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    @Override
    public void savePosts(List<SocialMediaPost> posts, String filename) {
        String json = gson.toJson(posts);
        saveResults(json, filename);
        logger.info("Đã lưu {} bài đăng vào {}", posts.size(), filename);
    }

    @Override
    public List<SocialMediaPost> loadPosts(String filename) {
        String json = loadResults(filename);
        if (json == null || json.isEmpty()) return new ArrayList<>();

        Type listType = new TypeToken<List<SocialMediaPost>>(){}.getType();
        List<SocialMediaPost> posts = gson.fromJson(json, listType);
        logger.info("Đã tải {} bài đăng từ {}", posts.size(), filename);
        return posts;
    }

    @Override
    public void saveResults(String jsonContent, String filename) {
        try {
            Path filePath = Paths.get(baseDir, filename);
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, jsonContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Lỗi lưu file: {}", filename, e);
        }
    }

    @Override
    public String loadResults(String filename) {
        try {
            Path filePath = Paths.get(baseDir, filename);
            if (Files.exists(filePath)) {
                return Files.readString(filePath, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            logger.error("Lỗi đọc file: {}", filename, e);
        }
        return null;
    }

    @Override
    public boolean exists(String filename) {
        return Files.exists(Paths.get(baseDir, filename));
    }

    /**
     * TypeAdapter cho LocalDateTime để Gson serialize/deserialize.
     */
    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            out.value(value != null ? value.format(FMT) : null);
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            String str = in.nextString();
            return str != null ? LocalDateTime.parse(str, FMT) : null;
        }
    }
}
