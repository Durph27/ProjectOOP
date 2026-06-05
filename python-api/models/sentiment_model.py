"""
Sentiment Model Wrapper.

Hỗ trợ 2 chế độ:
1. PhoBERT/ViSoBERT (nếu có GPU và đã cài transformers)
2. Rule-based fallback (nếu không có model ML)

Dễ dàng thay thế: Chỉ cần đổi implementation trong class này.
Java adapter (PythonApiSentimentProvider) chỉ cần biết API contract.
"""

import logging
import re

logger = logging.getLogger(__name__)

# Từ điển sentiment tiếng Việt
POSITIVE_WORDS = {
    'vui', 'mừng', 'hạnh phúc', 'phấn khởi', 'cảm ơn', 'biết ơn', 'cảm động',
    'hy vọng', 'lạc quan', 'tin tưởng', 'yêu thương', 'đoàn kết', 'ấm áp',
    'tuyệt vời', 'xuất sắc', 'tốt', 'hay', 'đẹp', 'giỏi',
    'cứu trợ', 'hỗ trợ', 'giúp đỡ', 'quyên góp', 'ủng hộ', 'tình nguyện',
    'khắc phục', 'phục hồi', 'tái thiết', 'cải thiện', 'nỗ lực', 'cố gắng',
    'kịp thời', 'nhanh chóng', 'hiệu quả', 'thành công', 'an toàn',
    'đầy đủ', 'chu đáo', 'tận tình', 'nhiệt tình', 'chuyên nghiệp',
    'hài lòng', 'minh bạch', 'công bằng', 'kiên cường', 'anh hùng',
    'sẻ chia', 'đồng lòng', 'chung tay', 'san sẻ', 'tương thân'
}

NEGATIVE_WORDS = {
    'buồn', 'đau', 'khổ', 'lo lắng', 'sợ hãi', 'hoang mang', 'thất vọng',
    'tức giận', 'bức xúc', 'bất bình', 'phẫn nộ', 'chán nản', 'tuyệt vọng',
    'đau lòng', 'xót xa', 'thương tâm', 'kinh hoàng', 'khủng khiếp',
    'chết', 'tử vong', 'thiệt mạng', 'mất tích', 'thương vong',
    'thiệt hại', 'hư hỏng', 'phá hủy', 'sập', 'đổ', 'ngập',
    'sạt lở', 'vỡ đê', 'tàn phá', 'tan hoang', 'hoang tàn',
    'chậm', 'thiếu', 'không đủ', 'tham nhũng', 'ăn chặn',
    'mất mát', 'khốn khổ', 'cơ cực', 'khó khăn', 'nghiêm trọng',
    'mất điện', 'mất nước', 'tê liệt', 'gián đoạn', 'cô lập',
    'nguy hiểm', 'bực bội', 'đau đớn', 'trắng tay'
}

NEGATION_WORDS = {'không', 'chẳng', 'chưa', 'đừng', 'thiếu', 'chả'}
INTENSIFIERS = {'rất', 'quá', 'cực', 'vô cùng', 'hết sức', 'cực kỳ', 'siêu', 'lắm'}


class SentimentModel:
    """Wrapper cho mô hình phân tích sentiment."""

    def __init__(self):
        self.model_name = "Rule-Based Vietnamese"
        self._try_load_ml_model()

    def _try_load_ml_model(self):
        """Thử tải model ML (PhoBERT). Fallback sang rule-based nếu thất bại."""
        try:
            from transformers import AutoTokenizer, AutoModelForSequenceClassification
            import torch

            model_name = "wonrax/phobert-base-vietnamese-sentiment"
            self.tokenizer = AutoTokenizer.from_pretrained(model_name)
            self.ml_model = AutoModelForSequenceClassification.from_pretrained(model_name)
            self.ml_model.eval()
            self.model_name = "PhoBERT Vietnamese Sentiment"
            self.use_ml = True
            logger.info(f"Loaded ML model: {model_name}")
        except Exception as e:
            logger.warning(f"Cannot load ML model, using rule-based: {e}")
            self.use_ml = False

    def get_model_name(self):
        return self.model_name

    def predict(self, text: str) -> dict:
        """
        Predict sentiment for a single text.

        Returns: {"sentiment": "POSITIVE|NEGATIVE|NEUTRAL", "confidence": float}
        """
        if self.use_ml:
            return self._predict_ml(text)
        return self._predict_rule_based(text)

    def predict_batch(self, texts: list) -> list:
        """Predict sentiment for a batch of texts."""
        return [self.predict(text) for text in texts]

    def classify_category(self, text: str, categories: list) -> dict:
        """
        Classify text into a category based on keyword matching.

        Returns: {"category": str, "confidence": float}
        """
        text_lower = text.lower()
        best_category = "OTHER"
        best_score = 0

        for cat in categories:
            cat_id = cat.get('id', '')
            keywords = cat.get('keywords', [])
            score = sum(1 for kw in keywords if kw.lower() in text_lower)
            if score > best_score:
                best_score = score
                best_category = cat_id

        confidence = min(0.95, 0.3 + best_score * 0.15) if best_score > 0 else 0.1
        return {"category": best_category, "confidence": confidence}

    def _predict_ml(self, text: str) -> dict:
        """Predict using ML model (PhoBERT)."""
        import torch

        inputs = self.tokenizer(text, return_tensors="pt",
                                truncation=True, max_length=256, padding=True)
        with torch.no_grad():
            outputs = self.ml_model(**inputs)
            probs = torch.nn.functional.softmax(outputs.logits, dim=-1)

        # PhoBERT sentiment: 0=NEG, 1=POS, 2=NEU
        labels = ["NEGATIVE", "POSITIVE", "NEUTRAL"]
        pred_idx = torch.argmax(probs, dim=-1).item()
        confidence = probs[0][pred_idx].item()

        return {
            "sentiment": labels[pred_idx],
            "confidence": round(confidence, 4)
        }

    def _predict_rule_based(self, text: str) -> dict:
        """Predict using rule-based dictionary approach."""
        text_lower = text.lower()
        words = text_lower.split()

        pos_score = 0
        neg_score = 0
        negated = False
        intensifier = 1.0

        for i, word in enumerate(words):
            if word in NEGATION_WORDS:
                negated = True
                continue
            if word in INTENSIFIERS:
                intensifier = 1.5
                continue

            is_positive = word in POSITIVE_WORDS
            is_negative = word in NEGATIVE_WORDS

            # Check bigrams
            if i < len(words) - 1:
                bigram = f"{word} {words[i+1]}"
                if bigram in POSITIVE_WORDS:
                    is_positive = True
                if bigram in NEGATIVE_WORDS:
                    is_negative = True

            if is_positive:
                if negated:
                    neg_score += 1.0 * intensifier
                else:
                    pos_score += 1.0 * intensifier

            if is_negative:
                if negated:
                    pos_score += 0.5 * intensifier
                else:
                    neg_score += 1.0 * intensifier

            if not (word in NEGATION_WORDS or word in INTENSIFIERS):
                negated = False
                intensifier = 1.0

        # Check phrases
        for phrase in POSITIVE_WORDS:
            if ' ' in phrase and phrase in text_lower:
                pos_score += 1.5
        for phrase in NEGATIVE_WORDS:
            if ' ' in phrase and phrase in text_lower:
                neg_score += 1.5

        total = pos_score + neg_score
        if total == 0:
            return {"sentiment": "NEUTRAL", "confidence": 0.5}

        if pos_score > neg_score:
            confidence = min(0.95, 0.5 + (pos_score - neg_score) / (total * 2))
            return {"sentiment": "POSITIVE", "confidence": round(confidence, 4)}
        elif neg_score > pos_score:
            confidence = min(0.95, 0.5 + (neg_score - pos_score) / (total * 2))
            return {"sentiment": "NEGATIVE", "confidence": round(confidence, 4)}
        else:
            return {"sentiment": "NEUTRAL", "confidence": 0.5}
