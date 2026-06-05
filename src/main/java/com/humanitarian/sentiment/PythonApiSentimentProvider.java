package com.humanitarian.sentiment;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.humanitarian.config.AppConfig;
import com.humanitarian.model.SentimentResult;
import com.humanitarian.model.enums.Sentiment;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Adapter gọi Python Flask API cho phân tích sentiment.
 * 
 * DESIGN PATTERN: Adapter
 * - Chuyển đổi interface SentimentModelProvider sang HTTP REST API calls
 * - API endpoints được mô hình hóa rõ ràng (xem Python API docs)
 * - Dễ dàng đổi sang API khác: chỉ cần thay đổi URL và format request/response
 * 
 * API Contract:
 * POST /api/sentiment
 *   Input:  {"text": "..."}
 *   Output: {"sentiment": "POSITIVE|NEGATIVE|NEUTRAL", "confidence": 0.95}
 * 
 * POST /api/sentiment/batch
 *   Input:  {"texts": ["...", "..."]}
 *   Output: [{"sentiment": "...", "confidence": ...}, ...]
 */
public class PythonApiSentimentProvider implements SentimentModelProvider {
    private static final Logger logger = LoggerFactory.getLogger(PythonApiSentimentProvider.class);
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final Gson gson = new Gson();
    private final String baseUrl;

    public PythonApiSentimentProvider() {
        AppConfig config = AppConfig.getInstance();
        this.baseUrl = config.getPythonApiUrl();

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getSentimentTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getSentimentTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getSentimentTimeout(), TimeUnit.MILLISECONDS)
                .build();
    }

    public PythonApiSentimentProvider(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String getModelName() {
        return "Python API (PhoBERT)";
    }

    @Override
    public SentimentResult analyze(String text) {
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("text", text);

            Request request = new Request.Builder()
                    .url(baseUrl + "/api/sentiment")
                    .post(RequestBody.create(gson.toJson(requestBody), JSON_MEDIA_TYPE))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject result = gson.fromJson(response.body().string(), JsonObject.class);
                    return parseResult(result);
                } else {
                    logger.error("Python API lỗi: HTTP {}", response.code());
                }
            }
        } catch (IOException e) {
            logger.error("Không thể kết nối Python API: {}", e.getMessage());
        }

        // Fallback: trả về NEUTRAL nếu API không khả dụng
        return new SentimentResult(Sentiment.NEUTRAL, 0.0, getModelName() + " (fallback)");
    }

    @Override
    public List<SentimentResult> analyzeBatch(List<String> texts) {
        try {
            JsonObject requestBody = new JsonObject();
            JsonArray textsArray = new JsonArray();
            texts.forEach(textsArray::add);
            requestBody.add("texts", textsArray);

            Request request = new Request.Builder()
                    .url(baseUrl + "/api/sentiment/batch")
                    .post(RequestBody.create(gson.toJson(requestBody), JSON_MEDIA_TYPE))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonArray results = gson.fromJson(response.body().string(), JsonArray.class);
                    List<SentimentResult> sentimentResults = new ArrayList<>();
                    for (JsonElement el : results) {
                        sentimentResults.add(parseResult(el.getAsJsonObject()));
                    }
                    return sentimentResults;
                }
            }
        } catch (IOException e) {
            logger.error("Batch API lỗi: {}", e.getMessage());
        }

        // Fallback: phân tích từng cái
        return texts.stream().map(this::analyze).toList();
    }

    @Override
    public boolean isAvailable() {
        try {
            Request request = new Request.Builder()
                    .url(baseUrl + "/api/health")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (IOException e) {
            return false;
        }
    }

    private SentimentResult parseResult(JsonObject json) {
        String sentimentStr = json.get("sentiment").getAsString();
        double confidence = json.get("confidence").getAsDouble();
        Sentiment sentiment = Sentiment.valueOf(sentimentStr.toUpperCase());
        return new SentimentResult(sentiment, confidence, getModelName());
    }
}
