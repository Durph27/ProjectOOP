# Tài liệu kiến trúc và cách hoạt động của chương trình

## 1. Tổng quan

Đây là ứng dụng desktop phân tích dữ liệu mạng xã hội phục vụ logistics nhân đạo trong bối cảnh thiên tai. Phiên bản hiện tại tập trung vào dữ liệu liên quan đến bão Yagi và giải quyết bốn bài toán:

1. Theo dõi tâm lý công chúng theo thời gian.
2. Phân loại loại thiệt hại được đề cập.
3. Đánh giá mức độ hài lòng theo loại cứu trợ.
4. Theo dõi tâm lý đối với từng loại cứu trợ theo thời gian.

Ứng dụng được chia thành hai phần:

- **Java application:** giao diện JavaFX, thu thập dữ liệu, tiền xử lý, điều phối nghiệp vụ, phân loại thiệt hại/cứu trợ và trực quan hóa.
- **Python API:** cung cấp REST API phân tích sentiment bằng mô hình PhoBERT; nếu không tải được mô hình thì tự chuyển sang thuật toán từ điển.

Kiến trúc tổng quát:

```text
Người dùng
    |
    v
JavaFX MainFrame
    |
    +--> CollectionService --> DataCollector --> CSV / nguồn mạng xã hội
    |
    +--> PreprocessingService --> PreprocessorChain --> các bước làm sạch
    |
    +--> AnalysisService
           |
           +--> SentimentModelProvider
           |      +--> PythonApiSentimentProvider --> Flask API --> PhoBERT
           |      +--> DictionaryBasedSentimentProvider
           |
           +--> AnalyzerRegistry --> 4 Analyzer nghiệp vụ
    |
    v
Biểu đồ, nhận xét và kết quả tổng hợp
```

## 2. Công nghệ sử dụng

| Công nghệ | Vai trò |
|---|---|
| Java 17 | Ngôn ngữ chính cho ứng dụng desktop và nghiệp vụ |
| JavaFX 21 | Xây dựng giao diện, bảng dữ liệu và biểu đồ |
| Maven | Quản lý dependency, build và đóng gói JAR |
| Gson | Đọc cấu hình JSON, chuyển đổi dữ liệu JSON |
| OkHttp | Java gọi Python REST API qua HTTP |
| OpenCSV | Đọc dữ liệu bài đăng từ file CSV |
| SLF4J + Logback | Ghi log ứng dụng |
| Python + Flask | Cung cấp dịch vụ REST cho sentiment |
| Transformers + PyTorch | Tải và chạy mô hình PhoBERT cục bộ |
| Hugging Face Hub | Nguồn tải mô hình `wonrax/phobert-base-vietnamese-sentiment` |
| JSON configuration | Tách từ khóa, danh mục và thiết lập khỏi mã nguồn |

Mô hình PhoBERT chạy cục bộ trên máy. Hugging Face chỉ được dùng để tải mô hình; chương trình không gửi từng bài đăng tới một AI API trả phí như OpenAI hay Gemini.

## 3. Cấu trúc thư mục

```text
ProjectOOP/
├── config/                  # Cấu hình nghiệp vụ và từ khóa
├── data/                    # Dữ liệu đầu vào/đầu ra
├── logs/                    # Log ứng dụng
├── python-api/              # Flask API và mô hình sentiment
├── src/main/java/
│   └── com/humanitarian/
│       ├── analyzer/        # Các bài toán phân tích
│       ├── collector/       # Thu thập dữ liệu
│       ├── config/          # Đọc cấu hình
│       ├── model/           # Mô hình dữ liệu
│       ├── preprocessor/    # Tiền xử lý văn bản
│       ├── sentiment/       # Các chiến lược sentiment
│       ├── service/         # Điều phối nghiệp vụ
│       ├── storage/         # Lưu/đọc dữ liệu
│       └── ui/              # Giao diện JavaFX
├── src/main/resources/      # CSS và cấu hình logging
└── pom.xml                  # Cấu hình Maven
```

Việc chia package theo trách nhiệm giúp mỗi nhóm lớp có một lý do thay đổi riêng:

- Thay nguồn dữ liệu chủ yếu tác động `collector`.
- Thay thuật toán làm sạch chủ yếu tác động `preprocessor`.
- Thay mô hình sentiment chủ yếu tác động `sentiment` hoặc `python-api`.
- Thêm bài toán phân tích chủ yếu tác động `analyzer`.
- Thay giao diện chủ yếu tác động `ui`.

Đây là cách áp dụng nguyên tắc **Separation of Concerns** và hỗ trợ **Single Responsibility Principle**.

## 4. Luồng hoạt động chi tiết

### 4.1. Khởi động ứng dụng

```text
Main.main()
    -> Application.launch(HumanitarianApplication.class)
    -> HumanitarianApplication.start(Stage)
    -> new MainFrame()
```

- `Main` là điểm chạy chính.
- `Launcher` ủy quyền cho `Main`, được giữ lại để tương thích với các run configuration cũ và fat JAR.
- `HumanitarianApplication` quản lý vòng đời JavaFX, tạo `Scene`, nạp CSS và hiển thị cửa sổ.
- `MainFrame` tạo các service, tab chức năng, bảng và biểu đồ.

### 4.2. Đọc cấu hình

`AppConfig` đọc các file:

- `config/app-config.json`: thiên tai, provider sentiment, thời gian, storage và các bước xử lý.
- `config/keywords.json`: từ khóa/hashtag dùng khi thu thập.
- `config/damage-categories.json`: danh mục thiệt hại và từ khóa.
- `config/relief-categories.json`: danh mục cứu trợ và từ khóa.

`AppConfig` là Singleton nên toàn ứng dụng dùng chung một nguồn cấu hình nhất quán.

### 4.3. Thu thập dữ liệu

Luồng hiện tại trên giao diện:

```text
Người dùng chọn CSV
    -> MainFrame cấu hình đường dẫn cho CsvFileCollector
    -> CollectionService.collectFrom("csv")
    -> DataCollectorFactory.get("csv")
    -> CsvFileCollector.collect(...)
    -> AbstractCollector kiểm tra đầu vào
    -> CsvFileCollector.doCollect(...)
    -> List<SocialMediaPost>
```

`CsvFileCollector`:

- Đọc CSV UTF-8 bằng OpenCSV.
- Bỏ dòng tiêu đề.
- Chuyển từng dòng thành `SocialMediaPost`.
- Hỗ trợ nhiều định dạng thời gian.
- Chỉ giữ bài đăng nằm trong khoảng ngày cấu hình.
- Gán cả `rawContent` và `content` bằng nội dung ban đầu.

Các collector Twitter, Facebook, TikTok và YouTube hiện chỉ là khung mở rộng. Chúng chưa gọi API thật và trả về danh sách rỗng.

### 4.4. Tiền xử lý văn bản

`PreprocessingService` tạo một `PreprocessorChain`, đăng ký các bước và bật/tắt chúng theo `app-config.json`.

Thứ tự được xác định bằng `getOrder()` của từng preprocessor:

```text
Nội dung gốc
    -> HtmlCleanerPreprocessor
    -> UrlRemover
    -> EmojiHandler
    -> VietnameseNormalizer
    -> StopWordRemover
    -> Nội dung đã xử lý
```

Vai trò các bước:

- `HtmlCleanerPreprocessor`: xóa HTML tag và HTML entity.
- `UrlRemover`: xóa URL.
- `EmojiHandler`: chuyển emoji phổ biến thành từ khóa cảm xúc rồi xóa emoji còn lại.
- `VietnameseNormalizer`: chuẩn hóa Unicode, chữ thường, ký tự lặp, dấu câu và khoảng trắng.
- `StopWordRemover`: xóa một số từ phổ biến ít giá trị phân tích.

`rawContent` được giữ nguyên; kết quả làm sạch được ghi vào `content`. Thiết kế này giúp vừa hiển thị dữ liệu gốc vừa dùng dữ liệu sạch cho thuật toán.

### 4.5. Gán sentiment

Trước khi chạy một analyzer từ giao diện, `MainFrame` gọi:

```text
AnalysisService.assignSentiment(posts)
    -> SentimentModelFactory.getConfigured()
    -> provider.analyze(post.getContent())
    -> gán sentiment và confidence vào SocialMediaPost
```

Provider hiện được cấu hình:

```json
"sentiment": {
  "provider": "python-api",
  "pythonApiUrl": "http://localhost:5000"
}
```

`SentimentModelFactory` kiểm tra Python API bằng `/api/health`:

- Nếu API hoạt động: dùng `PythonApiSentimentProvider`.
- Nếu API không hoạt động: chuyển sang `DictionaryBasedSentimentProvider`.

Lưu ý: nếu Python API hoạt động nhưng bản thân Python không tải được PhoBERT, Python API vẫn trả health thành công và tự sử dụng rule-based Python.

### 4.6. Chạy bốn bài toán phân tích

Sau khi mỗi bài đăng có sentiment, `AnalysisService` lấy analyzer từ `AnalyzerRegistry` và gọi `analyze(posts)`.

#### Bài toán 1: Sentiment theo thời gian

`SentimentTimelineAnalyzer` nhóm bài đăng theo ngày bằng `TreeMap<LocalDate, TimeSentimentData>`, sau đó tăng bộ đếm positive, negative hoặc neutral.

Đầu ra:

```text
Ngày -> {positiveCount, negativeCount, neutralCount}
```

#### Bài toán 2: Phân loại thiệt hại

`DamageClassificationAnalyzer` không dùng AI. Nó:

1. Lấy từ khóa từng danh mục từ `damage-categories.json`.
2. Kiểm tra `content.contains(keyword)`.
3. Đếm số từ khóa khớp.
4. Tạo `DamageReport` cho mỗi danh mục có ít nhất một từ khóa khớp.
5. Tính confidence:

```text
confidence = min(0.95, 0.3 + matchCount * 0.15)
```

Một bài đăng có thể thuộc nhiều danh mục thiệt hại. Nếu không khớp danh mục nào và đã có sentiment, bài được đưa vào `OTHER`.

#### Bài toán 3: Hài lòng theo loại cứu trợ

`ReliefSatisfactionAnalyzer` kết hợp:

- **Phân loại loại cứu trợ bằng từ khóa** từ `relief-categories.json`.
- **Sentiment đã gán trước đó** bằng PhoBERT hoặc từ điển.

Ví dụ:

```text
"Gạo cứu trợ đến rất nhanh và đầy đủ"
    -> từ khóa "gạo" => FOOD
    -> sentiment POSITIVE
    -> FOOD.positiveCount tăng 1
```

Tỷ lệ hài lòng:

```text
satisfactionRate = positiveCount / (positiveCount + negativeCount + neutralCount)
```

Một bài đăng có thể được tính vào nhiều loại cứu trợ nếu chứa từ khóa của nhiều danh mục.

#### Bài toán 4: Sentiment cứu trợ theo thời gian

`ReliefSentimentTimelineAnalyzer` kết hợp bài toán 1 và 3:

```text
Loại cứu trợ -> Ngày -> số positive/negative/neutral
```

Giao diện biểu diễn mỗi loại cứu trợ bằng một đường với điểm:

```text
sentimentScore = positiveCount - negativeCount
```

### 4.7. Hiển thị kết quả

`MainFrame` nhận kết quả từ analyzer và:

- Tạo `LineChart`, `BarChart` hoặc `StackedBarChart`.
- Sinh nhận xét tổng hợp.
- Hiển thị trạng thái và log.
- Chạy tác vụ thu thập/phân tích trong thread nền; cập nhật UI bằng `Platform.runLater`.

## 5. Python API và thuật toán sentiment

### 5.1. Vai trò của `app.py`

`python-api/app.py` là lớp biên HTTP giữa Java và Python:

| Endpoint | Vai trò |
|---|---|
| `GET /api/health` | Cho biết API và model đang hoạt động |
| `POST /api/sentiment` | Phân tích một văn bản |
| `POST /api/sentiment/batch` | Phân tích danh sách văn bản |
| `POST /api/classify/damage` | Phân loại keyword phía Python, hiện Java không sử dụng |
| `POST /api/classify/relief` | Phân loại keyword phía Python, hiện Java không sử dụng |

Model được tạo một lần khi Flask khởi động:

```python
model = SentimentModel()
```

Cache Hugging Face được đặt tại `python-api/.cache/huggingface` để tránh lỗi quyền ghi vào cache người dùng.

### 5.2. Sentiment có AI: PhoBERT

Khi khởi tạo, `SentimentModel` thử tải:

```text
wonrax/phobert-base-vietnamese-sentiment
```

Nếu thành công:

```python
self.use_ml = True
self.model_name = "PhoBERT Vietnamese Sentiment"
```

Luồng dự đoán:

```text
Văn bản
    -> tokenizer chuyển thành token tensor
    -> cắt tối đa 256 token
    -> PhoBERT sinh logits
    -> softmax chuyển logits thành xác suất
    -> argmax chọn nhãn có xác suất lớn nhất
```

Nhãn đang được code giả định theo thứ tự:

```python
["NEGATIVE", "POSITIVE", "NEUTRAL"]
```

`torch.no_grad()` được sử dụng vì đây là inference, không phải huấn luyện; nhờ đó giảm bộ nhớ và chi phí tính toán.

### 5.3. Sentiment không AI: rule-based fallback

Nếu thiếu dependency, lỗi tải model, lỗi quyền cache hoặc lỗi mạng ở lần tải đầu, Python chuyển sang `_predict_rule_based`.

Thuật toán dùng:

- Tập từ tích cực.
- Tập từ tiêu cực.
- Từ phủ định.
- Từ tăng cường.
- Kiểm tra từ đơn, bigram và cụm từ.

Quy tắc chính:

```text
từ tích cực             -> positiveScore += 1
từ tiêu cực             -> negativeScore += 1
"rất" + từ cảm xúc      -> nhân 1.5
"không" + từ tích cực   -> cộng vào negativeScore
"không" + từ tiêu cực   -> cộng 0.5 vào positiveScore
cụm từ khớp             -> cộng thêm 1.5
```

Nếu không có từ nào khớp, kết quả là `NEUTRAL` với confidence `0.5`.

Confidence rule-based là điểm quy ước, không phải xác suất được học từ dữ liệu:

```text
0.5 + abs(positiveScore - negativeScore) / (totalScore * 2)
```

Giá trị bị giới hạn tối đa ở `0.95`.

### 5.4. Hai tầng fallback sentiment

Hệ thống có hai tầng dự phòng:

```text
Java chọn python-api
    |
    +--> Python API không chạy
    |       -> Java dùng DictionaryBasedSentimentProvider
    |
    +--> Python API chạy
            |
            +--> PhoBERT tải được -> dùng AI
            +--> PhoBERT lỗi      -> Python dùng rule-based
```

Điều này giúp chương trình vẫn hoạt động khi AI không sẵn sàng, đổi lại độ chính xác có thể giảm.

## 6. Ý nghĩa từng package và lớp

### 6.1. Package gốc `com.humanitarian`

| Lớp | Vai trò |
|---|---|
| `Main` | Điểm vào chuẩn của chương trình |
| `Launcher` | Điểm vào tương thích cho cấu hình cũ/fat JAR |
| `HumanitarianApplication` | Quản lý vòng đời JavaFX và tạo cửa sổ |

### 6.2. Package `ui`

| Lớp | Vai trò |
|---|---|
| `MainFrame` | Cửa sổ chính, tạo tab, nhận sự kiện người dùng, gọi service, tạo biểu đồ và nhận xét |

`MainFrame` là lớp composition root gần giao diện: nó kết nối các service nhưng không tự triển khai chi tiết thu thập hoặc sentiment.

### 6.3. Package `config`

| Lớp | Vai trò |
|---|---|
| `AppConfig` | Đọc và cung cấp toàn bộ cấu hình JSON qua một instance dùng chung |

Tách cấu hình khỏi code giúp thay đổi từ khóa, khoảng thời gian và provider mà không cần biên dịch lại.

### 6.4. Package `model`

| Lớp | Vai trò |
|---|---|
| `SocialMediaPost` | Entity trung tâm chứa bài đăng, nội dung gốc/sạch và sentiment |
| `SentimentResult` | DTO kết quả sentiment |
| `DamageReport` | DTO kết quả phân loại thiệt hại cho một bài |
| `ReliefSentiment` | Tổng hợp sentiment và tỷ lệ hài lòng của một loại cứu trợ |
| `TimeSentimentData` | Tổng hợp số lượng sentiment trong một ngày |

Các giá trị phân loại không còn được khai báo bằng enum. Platform, nhãn sentiment
và các category được lấy từ JSON để có thể thay đổi mà không cần biên dịch lại.

| Cấu hình/model | Vai trò |
|---|---|
| `app-config.json` | Khai báo platform và nhãn sentiment |
| `damage-categories.json` | Khai báo danh mục thiệt hại và từ khóa |
| `relief-categories.json` | Khai báo danh mục cứu trợ và từ khóa |
| `CategoryDefinition` | Model Java được Gson tạo từ category trong JSON |

Enum giúp tránh dùng chuỗi tùy ý trong toàn hệ thống và giảm lỗi chính tả.

### 6.5. Package `collector`

| Lớp/interface | Vai trò |
|---|---|
| `DataCollector` | Hợp đồng chung cho mọi nguồn dữ liệu |
| `AbstractCollector` | Chứa luồng thu thập chung và để lớp con triển khai `doCollect` |
| `CsvFileCollector` | Đọc dữ liệu thực tế từ CSV |
| `TwitterCollector` | Khung mở rộng Twitter/X, chưa triển khai API |
| `FacebookCollector` | Khung mở rộng Facebook, chưa triển khai API |
| `TiktokCollector` | Khung mở rộng TikTok, chưa triển khai API |
| `YoutubeCollector` | Khung mở rộng YouTube, chưa triển khai API |
| `DataCollectorFactory` | Đăng ký và trả collector theo key |

### 6.6. Package `preprocessor`

| Lớp/interface | Vai trò |
|---|---|
| `TextPreprocessor` | Hợp đồng cho một bước xử lý văn bản |
| `PreprocessorChain` | Quản lý danh sách bước, sắp xếp và chạy tuần tự |
| `HtmlCleanerPreprocessor` | Xóa HTML |
| `UrlRemover` | Xóa URL |
| `EmojiHandler` | Chuyển emoji thành từ khóa cảm xúc |
| `VietnameseNormalizer` | Chuẩn hóa tiếng Việt và ký tự |
| `StopWordRemover` | Xóa stop word |

### 6.7. Package `sentiment`

| Lớp/interface | Vai trò |
|---|---|
| `SentimentModelProvider` | Hợp đồng chung cho mọi phương pháp sentiment |
| `DictionaryBasedSentimentProvider` | Sentiment rule-based hoàn toàn bằng Java |
| `PythonApiSentimentProvider` | Adapter gọi Flask API bằng HTTP |
| `SentimentModelFactory` | Đăng ký, chọn provider và fallback |

### 6.8. Package `analyzer`

| Lớp/interface | Vai trò |
|---|---|
| `Analyzer<T>` | Hợp đồng generic cho một bài toán phân tích |
| `AnalyzerRegistry` | Quản lý các analyzer theo ID |
| `SentimentTimelineAnalyzer` | Tổng hợp sentiment theo ngày |
| `DamageClassificationAnalyzer` | Phân loại thiệt hại bằng từ khóa |
| `ReliefSatisfactionAnalyzer` | Kết hợp loại cứu trợ và sentiment |
| `ReliefSentimentTimelineAnalyzer` | Kết hợp loại cứu trợ, sentiment và thời gian |

Generic `Analyzer<T>` cho phép mỗi analyzer trả một kiểu kết quả riêng nhưng vẫn được quản lý qua cùng một interface.

### 6.9. Package `service`

| Lớp | Vai trò |
|---|---|
| `CollectionService` | Điều phối cấu hình và collector |
| `PreprocessingService` | Tạo chain và xử lý danh sách bài đăng |
| `AnalysisService` | Chọn sentiment provider, gán sentiment và chạy analyzer |

Service là lớp use-case/application layer: UI chỉ cần yêu cầu “thu thập”, “tiền xử lý”, “phân tích” thay vì biết chi tiết thực hiện.

### 6.10. Package `storage`

| Lớp/interface | Vai trò |
|---|---|
| `DataStorage` | Hợp đồng backend lưu trữ |
| `JsonFileStorage` | Lưu/đọc bài đăng và kết quả bằng JSON |

`JsonFileStorage` có `LocalDateTimeAdapter` để Gson chuyển đổi `LocalDateTime` đúng định dạng ISO.

Lưu ý: storage đã được thiết kế nhưng chưa được kết nối đáng kể vào luồng giao diện hiện tại.

## 7. Các kỹ thuật lập trình hướng đối tượng

### 7.1. Đóng gói - Encapsulation

Các model giữ trường ở mức `private` và truy cập qua getter/setter:

```java
private String sentiment;
private double sentimentConfidence;
```

`ReliefSentiment` còn đóng gói quy tắc tính lại `satisfactionRate`: khi bộ đếm thay đổi, `recalculate()` được gọi để giữ dữ liệu nhất quán.

Vai trò:

- Bảo vệ trạng thái đối tượng.
- Gom quy tắc liên quan đến dữ liệu vào đúng lớp.
- Cho phép thay đổi implementation mà ít ảnh hưởng nơi sử dụng.

### 7.2. Trừu tượng hóa - Abstraction

Các interface mô tả “cần làm gì”, không buộc caller biết “làm thế nào”:

- `DataCollector`
- `TextPreprocessor`
- `SentimentModelProvider`
- `Analyzer<T>`
- `DataStorage`

Ví dụ, `AnalysisService` chỉ gọi `provider.analyze(text)`, không cần biết provider đang dùng từ điển Java, Flask hay PhoBERT.

### 7.3. Kế thừa - Inheritance

`AbstractCollector` triển khai phần chung của quy trình thu thập:

```text
validate -> log -> doCollect -> log kết quả
```

Các collector con chỉ override `doCollect`.

Đây đồng thời là **Template Method pattern**: lớp cha cố định khung thuật toán, lớp con cung cấp bước cụ thể.

`HumanitarianApplication extends Application` và `MainFrame extends BorderPane` dùng kế thừa để tham gia framework JavaFX.

### 7.4. Đa hình - Polymorphism

Nhiều implementation được dùng qua cùng interface:

```java
DataCollector collector = collectorFactory.get(platformKey);
SentimentModelProvider provider = sentimentFactory.getConfigured();
Analyzer<T> analyzer = registry.get(analyzerId);
```

Caller không phụ thuộc lớp cụ thể. Việc đổi implementation không yêu cầu đổi luồng nghiệp vụ chính.

## 8. Design pattern được áp dụng

### 8.1. Strategy Pattern

Áp dụng tại:

- `DataCollector`
- `TextPreprocessor`
- `SentimentModelProvider`
- `Analyzer<T>`
- `DataStorage`

Vai trò:

- Đóng gói các thuật toán có thể thay thế.
- Cho phép thêm implementation mới.
- Giảm `if/else` phụ thuộc lớp cụ thể.

Ví dụ đổi sentiment từ AI sang từ điển chỉ cần đổi:

```json
"provider": "dictionary"
```

### 8.2. Factory + Registry Pattern

Áp dụng tại:

- `DataCollectorFactory`
- `SentimentModelFactory`
- `AnalyzerRegistry`

Các lớp này giữ `Map<String, implementation>` và trả implementation theo key/ID.

Vai trò:

- Tập trung việc tạo/đăng ký đối tượng.
- Cho phép thêm hoặc thay thế implementation tại runtime.
- UI/service không cần gọi constructor của từng lớp cụ thể.

Tên `DataCollectorFactory` và `SentimentModelFactory` mang vai trò kết hợp Factory với Registry; chúng quản lý instance đã đăng ký nhiều hơn là luôn tạo instance mới.

### 8.3. Singleton Pattern

Áp dụng tại:

- `AppConfig`
- `DataCollectorFactory`
- `SentimentModelFactory`
- `AnalyzerRegistry`

Vai trò:

- Đảm bảo một registry/cấu hình dùng chung.
- Tránh đăng ký hoặc tải cấu hình lặp lại.

Hạn chế:

- Tạo global state.
- Khó cô lập khi unit test.
- Phụ thuộc bị lấy trực tiếp thay vì truyền qua constructor.

### 8.4. Chain of Responsibility / Pipeline

Áp dụng tại `PreprocessorChain`.

Văn bản lần lượt đi qua nhiều handler độc lập. Mỗi handler nhận kết quả của handler trước và trả kết quả cho handler sau.

Vai trò:

- Thêm/bỏ/sắp xếp bước xử lý dễ dàng.
- Mỗi preprocessor có một trách nhiệm nhỏ.
- Có thể bật/tắt bước qua cấu hình.

Về bản chất implementation hiện tại gần với **processing pipeline** hơn Chain of Responsibility cổ điển, vì mọi bước được bật đều xử lý dữ liệu thay vì một bước xử lý rồi dừng chuỗi.

### 8.5. Adapter Pattern

Áp dụng tại `PythonApiSentimentProvider`.

Java mong đợi interface:

```java
SentimentResult analyze(String text)
```

Python cung cấp HTTP JSON API. Adapter chuyển đổi:

```text
Java method call
    -> JSON request
    -> HTTP POST
    -> JSON response
    -> SentimentResult
```

Vai trò:

- Cách ly Java khỏi chi tiết Flask/Python.
- Có thể thay Python bằng dịch vụ khác nếu vẫn giữ hoặc chuyển đổi contract.

### 8.6. Template Method Pattern

Áp dụng tại `AbstractCollector.collect`.

Lớp cha định nghĩa quy trình chung; lớp con chỉ triển khai `doCollect`.

Vai trò:

- Tránh lặp code kiểm tra/logging.
- Đảm bảo collector tuân theo cùng một luồng.

### 8.7. Facade/Application Service

Các lớp `CollectionService`, `PreprocessingService`, `AnalysisService` đóng vai trò facade cho UI.

Vai trò:

- Giảm độ phức tạp mà `MainFrame` phải biết.
- Gom các bước của một use case.
- Tạo ranh giới giữa presentation layer và domain/infrastructure.

## 9. Nguyên tắc SOLID thể hiện trong thiết kế

### Single Responsibility Principle

Mỗi lớp nhỏ thường có một trách nhiệm rõ ràng: đọc CSV, xóa URL, gọi Python API, tổng hợp timeline...

Điểm chưa tốt: `MainFrame` hiện khá lớn, vừa tạo giao diện, điều phối sự kiện, tạo biểu đồ và sinh nhận xét. Có thể tách controller/view-model/chart builder.

### Open/Closed Principle

Hệ thống mở rộng qua interface và registry:

- Thêm collector mới bằng cách implement `DataCollector`.
- Thêm analyzer mới bằng cách implement `Analyzer<T>`.
- Thêm sentiment provider mới bằng cách implement `SentimentModelProvider`.

Phần lõi ít cần sửa khi thêm implementation.

### Liskov Substitution Principle

Các implementation có thể thay cho interface tương ứng. Ví dụ mọi `SentimentModelProvider` phải trả `SentimentResult` và cho biết `isAvailable`.

### Interface Segregation Principle

Các interface tương đối nhỏ và tập trung. Tuy nhiên `DataStorage` chứa cả lưu bài đăng lẫn kết quả; nếu hệ thống phát triển lớn có thể tách nhỏ hơn.

### Dependency Inversion Principle

Service thường làm việc qua abstraction như `DataCollector`, `Analyzer` và `SentimentModelProvider`.

Điểm chưa hoàn chỉnh: service tự lấy Singleton thay vì được inject dependency qua constructor, nên mức độ đảo ngược phụ thuộc và khả năng test chưa tối ưu.

## 10. Thuật toán và cấu trúc dữ liệu đáng chú ý

### 10.1. Keyword matching

Phân loại thiệt hại và cứu trợ dùng tìm chuỗi con:

```java
content.contains(keyword.toLowerCase())
```

Ưu điểm:

- Dễ hiểu, dễ giải thích.
- Nhanh với tập dữ liệu nhỏ.
- Không cần model hay API.
- Dễ thay đổi từ khóa trong JSON.

Nhược điểm:

- Không hiểu ngữ cảnh, phủ định hoặc mỉa mai.
- Có thể khớp sai do chuỗi con.
- Không xử lý tốt từ đồng nghĩa ngoài cấu hình.
- Một bài có thể vào nhiều danh mục.

Độ phức tạp xấp xỉ:

```text
O(số bài * số danh mục * số từ khóa * chi phí tìm chuỗi)
```

### 10.2. Tổng hợp theo thời gian

`TreeMap` được sử dụng để ngày luôn có thứ tự tăng dần. `computeIfAbsent` tạo bộ đếm khi gặp ngày mới.

Ưu điểm:

- Code ngắn gọn.
- Dữ liệu sẵn sàng để vẽ timeline.

### 10.3. PhoBERT inference

PhoBERT là mô hình Transformer được fine-tune cho sentiment tiếng Việt. Nó tạo biểu diễn ngữ cảnh cho token và dự đoán ba lớp sentiment.

Ưu điểm:

- Hiểu ngữ cảnh tốt hơn từ điển.
- Có thể xử lý cách diễn đạt chưa xuất hiện trong danh sách từ khóa.

Nhược điểm:

- Tốn RAM và thời gian khởi động.
- Chạy CPU chậm hơn rule-based.
- Bị cắt sau 256 token.
- Không trực tiếp phân loại danh mục thiệt hại/cứu trợ trong implementation hiện tại.

### 10.4. Confidence

Có hai loại confidence khác nhau:

- **PhoBERT:** xác suất softmax của lớp được chọn.
- **Rule-based/keyword:** điểm quy ước do lập trình viên thiết kế.

Không nên so sánh trực tiếp hai loại confidence như cùng một thước đo đã hiệu chỉnh.

## 11. Lý do thiết kế theo kiến trúc hiện tại

### Tách Java và Python

Java phù hợp với ứng dụng desktop, kiến trúc OOP và JavaFX. Python có hệ sinh thái AI/ML mạnh với Transformers và PyTorch. REST API là ranh giới đơn giản giúp hai bên giao tiếp mà không phụ thuộc runtime của nhau.

### Tách analyzer khỏi sentiment

Sentiment là một thuộc tính đầu vào dùng lại cho nhiều bài toán. Gán sentiment một lần rồi để các analyzer tổng hợp giúp tránh lặp logic và cho phép thay model sentiment mà không viết lại analyzer.

### Dùng cấu hình JSON cho danh mục

Danh mục thiệt hại/cứu trợ và từ khóa có thể thay đổi theo thiên tai hoặc nghiệp vụ. Đặt chúng trong JSON giúp chuyên gia nghiệp vụ chỉnh sửa mà không cần thay code.

### Dùng interface và registry

Dự án có nhiều điểm biến đổi: nguồn dữ liệu, bước xử lý, model sentiment, bài toán phân tích, backend lưu trữ. Interface + registry làm các điểm này có thể mở rộng độc lập.

### Giữ `rawContent` và `content`

Nội dung gốc cần cho kiểm tra/hiển thị; nội dung sạch cần cho thuật toán. Giữ cả hai đảm bảo truy vết và tránh làm mất dữ liệu gốc.

## 12. Hành vi thực tế và giới hạn hiện tại

Các điểm cần hiểu đúng khi sử dụng hoặc thuyết trình:

1. **AI hiện chỉ dùng để phân loại sentiment.** Phân loại thiệt hại và loại cứu trợ vẫn dùng từ khóa.
2. **Mức hài lòng không phải một model AI riêng.** Nó là tỷ lệ sentiment positive trong các bài khớp từ khóa loại cứu trợ.
3. **Collector mạng xã hội chưa triển khai.** Chỉ CSV hoạt động thực tế.
4. **Python endpoint phân loại damage/relief tồn tại nhưng Java analyzer không gọi chúng; hơn nữa chúng cũng chỉ dùng từ khóa.**
5. **Batch sentiment chưa phải true batching.** Python chạy `[self.predict(text) for text in texts]`; Java `AnalysisService` hiện cũng gọi từng bài một.
6. **Fallback Java khi một request Python lỗi chỉ trả NEUTRAL confidence 0.0**, không chuyển từng request sang dictionary Java. Factory chỉ chuyển provider khi health check ban đầu thất bại.
7. **Cấu hình `analyzers.enabled` chưa thực sự điều khiển analyzer.** `Analyzer.isEnabled()` mặc định luôn `true`, và `runAllAnalyzers()` dùng `registry.getAll()`.
8. **Storage được thiết kế nhưng chưa tích hợp rõ vào UI/use case hiện tại.**
9. **Một số trách nhiệm UI còn tập trung trong `MainFrame`, làm lớp lớn và khó test.**
10. **Các file nguồn phải được đọc/ghi UTF-8.** Nếu terminal dùng encoding khác, tiếng Việt có thể hiển thị sai dù file đúng.

## 13. Hướng mở rộng đề xuất

### Thêm nguồn dữ liệu thật

1. Implement `doCollect()` trong collector tương ứng.
2. Đọc API key từ biến môi trường hoặc secret manager.
3. Chuyển response thành `SocialMediaPost`.
4. Thêm xử lý rate limit, retry và pagination.

### Thêm AI cho damage/relief

Có thể chọn một trong các hướng:

- Fine-tune model multi-label classification.
- Dùng zero-shot classification.
- Dùng embedding + similarity với mô tả danh mục.
- Dùng hosted LLM API với structured output.

Nên tạo abstraction mới như:

```java
interface CategoryClassifier<C> {
    List<CategoryPrediction<C>> classify(String text);
}
```

Sau đó cung cấp `KeywordCategoryClassifier` và `AiCategoryClassifier` để giữ nguyên khả năng fallback.

### Cải thiện sentiment

- Dùng true batching trong Python.
- Đọc label mapping từ `model.config.id2label` thay vì hardcode.
- Bắt lỗi theo từng prediction và fallback rule-based.
- Thêm model version vào API response.
- Thêm bộ test tiếng Việt cho phủ định, emoji và câu hỗn hợp.

### Cải thiện kiến trúc Java

- Dùng constructor injection thay Singleton trực tiếp.
- Tách `MainFrame` thành view, controller và chart/insight builder.
- Kết nối `DataStorage` vào use case lưu/tải.
- Làm `analyzers.enabled` hoạt động thực sự.
- Thêm unit test cho từng strategy và integration test cho Java-Python API.

## 14. Cách chạy hệ thống

### Chạy Python sentiment API

```powershell
cd D:\ProjectOOP\python-api
..\.venv\Scripts\python.exe app.py
```

Kiểm tra model đang dùng:

```powershell
Invoke-RestMethod http://localhost:5000/api/health
```

Nếu trả:

```json
{"model":"PhoBERT Vietnamese Sentiment","status":"ok","version":"1.0.0"}
```

thì Python đang dùng AI. Nếu model là `Rule-Based Vietnamese`, Python đang dùng fallback từ điển.

### Chạy Java application

```powershell
cd D:\ProjectOOP
mvn javafx:run
```

Với cấu hình hiện tại là `"provider": "python-api"`, nên khởi động Python API trước. Nếu Python API không khả dụng, Java tự chuyển sang sentiment từ điển.

## 15. Kết luận

Thiết kế hiện tại ưu tiên khả năng mở rộng và khả năng tiếp tục hoạt động khi thành phần AI không sẵn sàng. Java đảm nhiệm luồng nghiệp vụ, phân loại dựa trên cấu hình và giao diện; Python cô lập phần machine learning. Các interface, factory/registry, chain và adapter giúp thay thế từng thành phần mà không phải viết lại toàn bộ hệ thống.

Điểm quan trọng nhất là phân biệt hai loại phân tích:

- **Sentiment:** có thể dùng PhoBERT AI hoặc rule-based fallback.
- **Damage/relief category:** hiện dùng keyword matching, chưa dùng AI.
