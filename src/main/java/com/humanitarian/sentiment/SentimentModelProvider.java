package com.humanitarian.sentiment;

import com.humanitarian.model.SentimentResult;
import java.util.List;

/**
 * Interface Strategy cho mô hình phân tích sentiment.
 * 
 * DESIGN PATTERN: Strategy + Adapter
 * - DictionaryBasedSentimentProvider: Implementation Java rule-based (offline)
 * - PythonApiSentimentProvider: Adapter gọi Python Flask API (PhoBERT/ViSoBERT)
 * - Dễ dàng thêm provider mới: implement interface này
 * - Chuyển đổi provider qua config: thay đổi "sentiment.provider"
 * 
 * ĐẦU VÀO: Chuỗi văn bản (đã tiền xử lý)
 * ĐẦU RA: SentimentResult (sentiment, confidence, modelName)
 */
public interface SentimentModelProvider {

    /**
     * Tên mô hình (để hiển thị và logging).
     */
    String getModelName();

    /**
     * Phân tích sentiment cho một đoạn văn bản.
     */
    SentimentResult analyze(String text);

    /**
     * Phân tích sentiment cho một batch văn bản (tối ưu hiệu suất).
     * Implementation mặc định gọi analyze() từng cái.
     */
    default List<SentimentResult> analyzeBatch(List<String> texts) {
        return texts.stream()
                .map(this::analyze)
                .toList();
    }

    /**
     * Kiểm tra mô hình có sẵn sàng sử dụng không.
     */
    boolean isAvailable();
}
