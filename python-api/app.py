"""
Flask REST API Server cho phân tích sentiment tiếng Việt.

API Endpoints:
  GET  /api/health          → Health check
  POST /api/sentiment       → Phân tích sentiment đơn lẻ
  POST /api/sentiment/batch → Phân tích sentiment hàng loạt
  POST /api/classify/damage → Phân loại loại thiệt hại
  POST /api/classify/relief → Phân loại loại cứu trợ

Input/Output Contract rõ ràng - có thể dễ dàng thay thế bằng API khác.
"""

from flask import Flask, request, jsonify
from models.sentiment_model import SentimentModel
import logging

app = Flask(__name__)
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Khởi tạo model
model = SentimentModel()


@app.route('/api/health', methods=['GET'])
def health_check():
    """Health check endpoint."""
    return jsonify({
        'status': 'ok',
        'model': model.get_model_name(),
        'version': '1.0.0'
    })


@app.route('/api/sentiment', methods=['POST'])
def analyze_sentiment():
    """
    Phân tích sentiment cho một đoạn văn bản.

    Input:  {"text": "Cảm ơn đội cứu trợ đã giúp đỡ kịp thời!"}
    Output: {"sentiment": "POSITIVE", "confidence": 0.92}
    """
    data = request.get_json()
    if not data or 'text' not in data:
        return jsonify({'error': 'Missing "text" field'}), 400

    text = data['text']
    result = model.predict(text)

    return jsonify({
        'sentiment': result['sentiment'],
        'confidence': result['confidence']
    })


@app.route('/api/sentiment/batch', methods=['POST'])
def analyze_sentiment_batch():
    """
    Phân tích sentiment cho nhiều đoạn văn bản.

    Input:  {"texts": ["text1", "text2", ...]}
    Output: [{"sentiment": "POSITIVE", "confidence": 0.92}, ...]
    """
    data = request.get_json()
    if not data or 'texts' not in data:
        return jsonify({'error': 'Missing "texts" field'}), 400

    texts = data['texts']
    results = model.predict_batch(texts)

    return jsonify(results)


@app.route('/api/classify/damage', methods=['POST'])
def classify_damage():
    """
    Phân loại loại thiệt hại.

    Input:  {"text": "Nhà cửa bị tốc mái...", "categories": [...]}
    Output: {"category": "HOUSING_DAMAGE", "confidence": 0.87}
    """
    data = request.get_json()
    if not data or 'text' not in data:
        return jsonify({'error': 'Missing "text" field'}), 400

    text = data['text']
    categories = data.get('categories', [])
    result = model.classify_category(text, categories)

    return jsonify(result)


@app.route('/api/classify/relief', methods=['POST'])
def classify_relief():
    """
    Phân loại loại cứu trợ.

    Input:  {"text": "Gạo và mì tôm đã được phát...", "categories": [...]}
    Output: {"category": "FOOD", "confidence": 0.91}
    """
    data = request.get_json()
    if not data or 'text' not in data:
        return jsonify({'error': 'Missing "text" field'}), 400

    text = data['text']
    categories = data.get('categories', [])
    result = model.classify_category(text, categories)

    return jsonify(result)


if __name__ == '__main__':
    logger.info(f"Starting sentiment API server with model: {model.get_model_name()}")
    app.run(host='0.0.0.0', port=5000, debug=True)
