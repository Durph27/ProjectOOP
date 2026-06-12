# Hướng dẫn tư duy và thiết kế hệ thống phân tích dữ liệu mạng xã hội cho logistics nhân đạo

## 1. Mục tiêu của tài liệu

Tài liệu này mô tả cách đi từ yêu cầu đề tài đến một thiết kế hướng đối tượng có thể triển khai thành chương trình Java desktop.

Trọng tâm không chỉ là liệt kê các lớp đang có, mà còn giải thích:

- Bắt đầu phân tích đề bài từ đâu.
- Cách nhận diện các đối tượng, hành vi và thành phần dễ thay đổi.
- Cách chia package và trách nhiệm.
- Khi nào nên dùng interface, abstract class hoặc class thông thường.
- Vì sao các design pattern được lựa chọn.
- Cách các đối tượng phối hợp trong toàn bộ luồng xử lý.
- Cách thiết kế đáp ứng yêu cầu mở rộng và thay thế mô hình phân tích.

---

## 2. Phân tích đề bài trước khi thiết kế lớp

### 2.1. Không bắt đầu từ giao diện

Khi gặp đề tài này, không nên bắt đầu bằng việc tạo cửa sổ JavaFX hoặc viết ngay lớp thu thập Twitter.

Trước tiên cần trả lời các câu hỏi:

1. Hệ thống nhận dữ liệu gì?
2. Dữ liệu đi qua những bước xử lý nào?
3. Hệ thống tạo ra những kết quả gì?
4. Thành phần nào có khả năng thay đổi thường xuyên?
5. Thành phần nào cần được thay thế mà không ảnh hưởng phần còn lại?

Từ đề bài, có thể rút ra luồng nghiệp vụ tổng quát:

```text
Cấu hình thảm họa
    ↓
Thu thập dữ liệu từ nhiều nguồn
    ↓
Chuyển dữ liệu về định dạng chung
    ↓
Tiền xử lý nội dung
    ↓
Đánh giá sentiment
    ↓
Thực hiện các bài toán phân tích
    ↓
Lưu kết quả và hiển thị trên ứng dụng desktop
```

Luồng này là cơ sở để chia hệ thống thành các module và package.

### 2.2. Nhận diện danh từ và động từ trong đề bài

Một phương pháp đơn giản để tìm lớp ban đầu là phân tích danh từ và động từ.

Các danh từ quan trọng:

- Bài đăng mạng xã hội.
- Nền tảng mạng xã hội.
- Thảm họa.
- Từ khóa và hashtag.
- Sentiment.
- Loại thiệt hại.
- Loại hàng cứu trợ.
- Kết quả phân tích.
- Mô hình phân tích.

Các động từ quan trọng:

- Thu thập.
- Tiền xử lý.
- Phân loại.
- Đánh giá sentiment.
- Tổng hợp theo thời gian.
- Lưu trữ.
- Hiển thị.

Danh từ thường trở thành model hoặc đối tượng cấu hình. Động từ thường trở thành service, strategy hoặc phương thức.

Không phải danh từ nào cũng cần một lớp riêng. Chỉ tạo lớp khi đối tượng có dữ liệu, hành vi hoặc trách nhiệm rõ ràng.

### 2.3. Nhận diện các trục thay đổi

Đề bài nhấn mạnh tính linh động. Do đó cần tìm những phần có khả năng thay đổi:

| Thành phần có thể thay đổi | Ví dụ |
|---|---|
| Nguồn dữ liệu | Twitter, Facebook, TikTok, YouTube, CSV |
| Từ khóa tìm kiếm | Bão Yagi, lũ lụt, cứu trợ |
| Các bước tiền xử lý | Xóa URL, xử lý emoji, bỏ stopword |
| Mô hình sentiment | Từ điển Java, Python API, Java ONNX |
| Loại thiệt hại | Nhà cửa, con người, hạ tầng |
| Loại cứu trợ | Thực phẩm, y tế, tiền mặt |
| Bài toán phân tích | Timeline, thiệt hại, mức hài lòng |
| Kiểu lưu trữ | JSON, CSV, database |

Nguyên tắc thiết kế chính:

> Thành phần nào cần thay đổi hoặc có nhiều cách triển khai thì nên được đặt phía sau một interface.

---

## 3. Kiến trúc tổng thể

Hệ thống được chia theo trách nhiệm:

```text
com.humanitarian
├── model          Dữ liệu nghiệp vụ và kết quả
├── collector      Thu thập dữ liệu
├── preprocessor   Tiền xử lý văn bản
├── sentiment      Đánh giá sentiment
├── analyzer       Các bài toán phân tích
├── service        Điều phối các bước nghiệp vụ
├── storage        Lưu và đọc dữ liệu
├── config         Quản lý cấu hình
└── ui             Giao diện desktop
```

Không chia package theo từng bài toán hoặc từng nền tảng vì sẽ làm các trách nhiệm bị trộn lẫn. Ví dụ, package `twitter` có thể chứa collector, model, cấu hình và UI, khiến khó thay thế hoặc tái sử dụng.

### 3.1. Quy tắc phụ thuộc

Phụ thuộc mong muốn:

```text
UI → Service → Interface nghiệp vụ → Implementation
                  ↓
                Model
```

- UI gọi service, không tự đọc file hoặc phân tích dữ liệu.
- Service điều phối, không chứa thuật toán chi tiết của từng nền tảng.
- Analyzer chỉ phân tích dữ liệu, không biết dữ liệu được thu thập như thế nào.
- Collector chỉ thu thập và chuyển đổi dữ liệu, không biết dữ liệu sẽ được phân tích ra sao.

---

## 4. Thiết kế package `model`

## 4.1. Lớp trung tâm `SocialMediaPost`

Mọi nền tảng có cấu trúc dữ liệu riêng. Tuy nhiên, các bước phân tích phía sau cần một kiểu dữ liệu thống nhất.

Vì vậy, tạo lớp:

```java
public class SocialMediaPost {
    private String id;
    private Platform platform;
    private String rawContent;
    private String content;
    private String author;
    private LocalDateTime timestamp;
    private String url;
    private int likes;
    private int shares;
    private int comments;
    private String location;
    private Sentiment sentiment;
    private double sentimentConfidence;
}
```

### Lý do có `rawContent` và `content`

- `rawContent` giữ nội dung gốc để kiểm tra, hiển thị hoặc chạy lại pipeline.
- `content` chứa nội dung đã tiền xử lý để đưa vào mô hình.

Nếu ghi đè trực tiếp nội dung gốc, hệ thống không thể kiểm tra tiền xử lý có làm mất thông tin quan trọng hay không.

### Lý do sentiment được lưu trong bài đăng

Các analyzer đều cần sentiment. Sau khi đánh giá một lần, kết quả được gán vào bài đăng để tránh gọi lại mô hình cho từng analyzer.

Thiết kế này đơn giản cho đồ án. Với hệ thống lớn hơn, có thể tách kết quả phân tích sang một entity riêng để hỗ trợ nhiều phiên bản mô hình.

## 4.2. Các model kết quả

### `SentimentResult`

Đại diện kết quả trực tiếp của mô hình:

```java
public class SentimentResult {
    private Sentiment sentiment;
    private double confidence;
    private String modelName;
}
```

Không nên để provider chỉ trả về `Sentiment`, vì hệ thống còn cần độ tin cậy và thông tin mô hình đã sử dụng.

### `TimeSentimentData`

Lưu số lượng sentiment trong một khoảng thời gian:

```java
public class TimeSentimentData {
    private int positiveCount;
    private int negativeCount;
    private int neutralCount;
}
```

Lớp này tự cung cấp các hành vi như:

```java
incrementPositive();
incrementNegative();
getPositiveRate();
getNegativeRate();
```

Đây là đóng gói dữ liệu và hành vi liên quan trong cùng một lớp.

### `DamageReport`

Đại diện một kết quả phân loại thiệt hại:

```java
public class DamageReport {
    private String postId;
    private DamageCategory category;
    private Sentiment sentiment;
    private double confidence;
    private String excerpt;
}
```

`excerpt` giúp người dùng hiểu vì sao bài viết được phân loại vào danh mục đó.

### `ReliefSentiment`

Tổng hợp mức độ hài lòng cho một loại cứu trợ:

```java
public class ReliefSentiment {
    private ReliefCategory category;
    private int positiveCount;
    private int negativeCount;
    private int neutralCount;
    private double satisfactionRate;
}
```

`satisfactionRate` được tính trong lớp để tránh nhiều nơi tự tính theo các công thức khác nhau.

---

## 5. Thiết kế package `collector`

## 5.1. Xác định hợp đồng chung

Twitter, Facebook và CSV có cách lấy dữ liệu khác nhau, nhưng đều thực hiện cùng một nhiệm vụ:

```text
Nhận điều kiện tìm kiếm → trả về danh sách SocialMediaPost
```

Do đó tạo interface:

```java
public interface DataCollector {
    String getPlatformName();

    List<SocialMediaPost> collect(
        List<String> keywords,
        LocalDate startDate,
        LocalDate endDate
    );

    boolean isAvailable();
}
```

### Tại sao dùng interface?

Interface thể hiện khả năng thu thập dữ liệu mà không quy định cách thực hiện.

`CollectionService` chỉ làm việc với `DataCollector`. Vì vậy nó không cần biết collector đang đọc CSV, gọi REST API hay truy vấn database.

Đây là Strategy Pattern và Dependency Inversion Principle.

## 5.2. Tạo `AbstractCollector`

Các collector có một số logic giống nhau:

- Ghi log bắt đầu và kết thúc.
- Kiểm tra từ khóa.
- Lưu thông tin nền tảng.
- Chuẩn hóa quy trình gọi collector.

Logic chung được đưa vào abstract class:

```java
public abstract class AbstractCollector implements DataCollector {
    protected final Platform platform;

    @Override
    public List<SocialMediaPost> collect(...) {
        validateInput();
        return doCollect(...);
    }

    protected abstract List<SocialMediaPost> doCollect(...);
}
```

Các lớp con chỉ triển khai phần đặc thù:

```java
public class YoutubeCollector extends AbstractCollector {
    @Override
    protected List<SocialMediaPost> doCollect(...) {
        // Gọi YouTube API và ánh xạ kết quả
    }
}
```

Đây là Template Method Pattern.

### Khi nào dùng interface và abstract class?

| Tình huống | Lựa chọn |
|---|---|
| Chỉ cần quy định hợp đồng | Interface |
| Có trạng thái hoặc logic dùng chung | Abstract class |
| Lớp hoàn chỉnh, tạo được đối tượng | Class thông thường |

`DataCollector` là hợp đồng. `AbstractCollector` là khung triển khai tùy chọn. Một collector đặc biệt vẫn có thể implement trực tiếp `DataCollector`.

## 5.3. Factory và Registry

Nếu service tự viết:

```java
if (platform.equals("twitter")) {
    collector = new TwitterCollector();
} else if (...) {
}
```

thì mỗi lần thêm nguồn mới phải sửa service.

Thay vào đó, `DataCollectorFactory` giữ registry:

```java
private final Map<String, DataCollector> collectors;
```

Các thao tác:

```java
register("twitter", new TwitterCollector());
get("twitter");
unregister("twitter");
getAvailable();
```

Khi thêm nguồn mới, chỉ cần tạo implementation và đăng ký nó.

## 5.4. Cải tiến đề xuất cho collector

Danh sách tham số của `collect()` có thể tăng theo yêu cầu. Nên gom thành một request object:

```java
public class CollectionRequest {
    private List<String> keywords;
    private List<String> hashtags;
    private List<String> excludeKeywords;
    private LocalDate startDate;
    private LocalDate endDate;
    private String region;
    private int maxPosts;
}
```

Interface sau khi cải tiến:

```java
List<SocialMediaPost> collect(CollectionRequest request);
```

Ưu điểm:

- Dễ thêm điều kiện tìm kiếm.
- Không làm thay đổi chữ ký phương thức liên tục.
- Có thể validate request tại một vị trí.

---

## 6. Thiết kế package `preprocessor`

## 6.1. Vì sao mỗi bước là một lớp?

Tiền xử lý có nhiều bước độc lập:

- Xóa HTML.
- Xóa URL.
- Chuyển emoji thành từ khóa.
- Chuẩn hóa tiếng Việt.
- Xóa stopword.

Không nên đặt tất cả vào một phương thức lớn:

```java
String preprocess(String text);
```

Nếu làm vậy, khó bật/tắt, kiểm thử và thay đổi thứ tự từng bước.

Mỗi bước triển khai interface:

```java
public interface TextPreprocessor {
    String getName();
    String getDescription();
    String process(String text);
    int getOrder();
}
```

Các implementation:

```text
HtmlCleanerPreprocessor
UrlRemover
EmojiHandler
VietnameseNormalizer
StopWordRemover
```

## 6.2. `PreprocessorChain`

`PreprocessorChain` chứa danh sách các bộ xử lý:

```java
private final List<TextPreprocessor> processors;
```

Khi xử lý:

```java
String result = text;
for (TextPreprocessor processor : processors) {
    result = processor.process(result);
}
return result;
```

Các processor được sắp xếp theo `getOrder()` và có thể bật/tắt từ cấu hình.

Thiết kế này kết hợp Strategy Pattern và Pipeline/Chain of Responsibility.

### Tại sao thứ tự quan trọng?

Ví dụ:

```text
"Cảm ơn ❤️ xem tại https://example.com"
```

Pipeline có thể chạy:

```text
Xóa URL
    ↓
Chuyển emoji thành từ "yêu_thương"
    ↓
Chuẩn hóa văn bản
```

Nếu xóa ký tự đặc biệt trước khi xử lý emoji, thông tin cảm xúc có thể bị mất.

---

## 7. Thiết kế package `sentiment`

## 7.1. Tách mô hình khỏi phần còn lại

Đề bài cho phép mô hình được viết bằng Java hoặc Python, đồng thời yêu cầu dễ dàng thay thế.

Phần còn lại của chương trình không nên phụ thuộc trực tiếp vào Flask hoặc PhoBERT. Nó chỉ cần biết:

```text
Đưa văn bản vào → nhận SentimentResult
```

Vì vậy tạo interface:

```java
public interface SentimentModelProvider {
    String getModelName();
    SentimentResult analyze(String text);
    List<SentimentResult> analyzeBatch(List<String> texts);
    boolean isAvailable();
}
```

## 7.2. Các implementation

### `DictionaryBasedSentimentProvider`

- Viết hoàn toàn bằng Java.
- Chạy offline.
- Phù hợp làm fallback.
- Độ chính xác thấp hơn mô hình học máy.

### `PythonApiSentimentProvider`

- Gọi REST API Python.
- Chuyển JSON response thành `SentimentResult`.
- Là Adapter giữa interface Java và HTTP API.

Java chỉ biết API contract:

```json
POST /api/sentiment
{
  "text": "Đội cứu trợ đã đến kịp thời"
}
```

Response:

```json
{
  "sentiment": "POSITIVE",
  "confidence": 0.94
}
```

### Lợi ích của Adapter

Sau này có thể tạo:

```java
public class JavaOnnxSentimentProvider
        implements SentimentModelProvider {
}
```

Hoặc gọi một dịch vụ AI khác mà không sửa analyzer hay UI.

## 7.3. Factory chọn mô hình

`SentimentModelFactory` quản lý các provider và chọn provider theo config:

```json
{
  "sentiment": {
    "provider": "python-api"
  }
}
```

Nếu Python API không khả dụng, factory có thể trả về provider từ điển.

## 7.4. Tối ưu batch

Khi phân tích nhiều bài viết, không nên gọi HTTP một lần cho từng bài:

```text
10.000 bài viết = 10.000 HTTP request
```

Nên dùng:

```java
List<SentimentResult> analyzeBatch(List<String> texts);
```

Và chia dữ liệu theo `batchSize` từ config.

---

## 8. Thiết kế package `analyzer`

## 8.1. Xác định điểm chung của các bài toán

Tất cả bài toán đều:

- Có ID.
- Có tên và mô tả.
- Nhận danh sách bài đăng đã xử lý.
- Trả về kết quả.

Kết quả mỗi bài toán khác nhau. Vì vậy sử dụng generic interface:

```java
public interface Analyzer<T> {
    String getId();
    String getName();
    String getDescription();
    T analyze(List<SocialMediaPost> posts);
}
```

### Tại sao dùng generic?

Nếu trả về `Object`, code gọi analyzer sẽ mất an toàn kiểu dữ liệu.

Với generic:

```java
Analyzer<Map<LocalDate, TimeSentimentData>>
```

trình biên dịch biết chính xác kiểu kết quả của analyzer.

## 8.2. Bài toán 1: sentiment theo thời gian

Lớp:

```java
SentimentTimelineAnalyzer
```

Đầu vào:

```java
List<SocialMediaPost>
```

Đầu ra:

```java
Map<LocalDate, TimeSentimentData>
```

Thuật toán:

```text
Duyệt từng bài viết
    ↓
Lấy ngày đăng
    ↓
Lấy sentiment
    ↓
Tăng bộ đếm tương ứng của ngày đó
```

Sử dụng `TreeMap` để ngày tự động được sắp xếp.

## 8.3. Bài toán 2: phân loại thiệt hại

Lớp:

```java
DamageClassificationAnalyzer
```

Đầu ra:

```java
Map<DamageCategory, List<DamageReport>>
```

Analyzer đọc từ khóa danh mục từ file cấu hình, sau đó kiểm tra nội dung bài viết.

Một bài viết có thể thuộc nhiều loại thiệt hại. Vì vậy kết quả không nhất thiết chỉ có một category.

## 8.4. Bài toán 3: mức hài lòng theo loại cứu trợ

Lớp:

```java
ReliefSatisfactionAnalyzer
```

Thuật toán:

```text
Xác định bài viết đề cập loại cứu trợ nào
    ↓
Đọc sentiment đã được gán
    ↓
Tăng positive/negative/neutral của loại cứu trợ
    ↓
Tính satisfactionRate
```

## 8.5. Bài toán 4: sentiment cứu trợ theo thời gian

Lớp:

```java
ReliefSentimentTimelineAnalyzer
```

Đây là sự kết hợp của bài toán 1 và bài toán 3:

```java
Map<ReliefCategory, Map<LocalDate, TimeSentimentData>>
```

## 8.6. Vì sao analyzer không cần abstract class?

Các analyzer có cùng hợp đồng nhưng chưa có đủ logic dùng chung đáng kể.

Nếu tạo abstract class chỉ để chứa `getId()` hoặc `getName()`, thiết kế sẽ phức tạp hơn nhưng không giảm được trách nhiệm.

Chỉ nên tạo `AbstractAnalyzer` khi xuất hiện logic chung thực sự, ví dụ:

- Kiểm tra dữ liệu đầu vào.
- Đo thời gian chạy.
- Ghi log.
- Xử lý lỗi thống nhất.
- Quản lý metadata chung.

## 8.7. `AnalyzerRegistry`

Registry quản lý các analyzer:

```java
register(new SentimentTimelineAnalyzer());
register(new DamageClassificationAnalyzer());
unregister("damage_classification");
```

Muốn thêm bài toán mới:

```java
public class UrgencyByLocationAnalyzer
        implements Analyzer<Map<String, UrgencyScore>> {
}
```

Sau đó đăng ký vào registry. Không cần sửa các analyzer hiện tại.

---

## 9. Thiết kế package `service`

Service chịu trách nhiệm phối hợp các component, không thực hiện thuật toán chi tiết.

## 9.1. `CollectionService`

Trách nhiệm:

- Đọc từ khóa và thời gian từ config.
- Chọn collector.
- Gọi collector.
- Hợp nhất dữ liệu từ nhiều nguồn.

Service không biết cách gọi API YouTube hoặc parse CSV.

## 9.2. `PreprocessingService`

Trách nhiệm:

- Tạo và cấu hình `PreprocessorChain`.
- Chạy pipeline trên danh sách bài đăng.
- Ghi kết quả vào `content`.

## 9.3. `AnalysisService`

Trách nhiệm:

1. Chọn sentiment provider.
2. Gán sentiment cho bài đăng.
3. Chạy các analyzer đã đăng ký.
4. Trả về tập kết quả cho UI hoặc storage.

Luồng sử dụng điển hình:

```java
List<SocialMediaPost> posts =
        collectionService.collectFrom("csv");

preprocessingService.preprocess(posts);

analysisService.assignSentiment(posts);

Map<String, Object> results =
        analysisService.runAllAnalyzers(posts);
```

### Vì sao cần service?

Nếu UI trực tiếp gọi collector, preprocessor và analyzer:

- UI sẽ chứa nhiều logic nghiệp vụ.
- Khó kiểm thử mà không mở giao diện.
- Khó tái sử dụng luồng xử lý cho CLI hoặc API.

Service đóng vai trò Facade/Application Service.

---

## 10. Thiết kế package `storage`

Hệ thống hiện có interface:

```java
public interface DataStorage {
    void savePosts(List<SocialMediaPost> posts, String filename);
    List<SocialMediaPost> loadPosts(String filename);
    void saveResults(String jsonContent, String filename);
    String loadResults(String filename);
    boolean exists(String filename);
}
```

Implementation hiện tại:

```java
JsonFileStorage
```

Nhờ interface, sau này có thể thêm:

```java
CsvStorage
DatabaseStorage
CloudStorage
```

Mã nghiệp vụ không cần biết dữ liệu được lưu trong file hay database.

---

## 11. Thiết kế package `config`

Các giá trị thường xuyên thay đổi được đưa ra file JSON:

```text
config/app-config.json
config/keywords.json
config/damage-categories.json
config/relief-categories.json
```

Ví dụ cấu hình thảm họa:

```json
{
  "disaster": {
    "name": "Bão Yagi",
    "startDate": "2024-09-01",
    "endDate": "2024-09-30"
  }
}
```

Lợi ích:

- Đổi thảm họa mà không sửa code.
- Thêm từ khóa mà không biên dịch lại.
- Bật/tắt bước tiền xử lý.
- Chọn sentiment provider.
- Chọn analyzer cần chạy.

`AppConfig` hiện được thiết kế dưới dạng Singleton để các component sử dụng cùng một bộ cấu hình.

### Hạn chế của Singleton

Singleton đơn giản cho đồ án nhưng làm tăng phụ thuộc toàn cục và khó kiểm thử.

Thiết kế tốt hơn cho hệ thống lớn là truyền cấu hình qua constructor:

```java
public DamageClassificationAnalyzer(CategoryConfig config) {
    this.config = config;
}
```

---

## 12. Luồng hoạt động chi tiết

## 12.1. Luồng khởi động

```text
Người dùng mở ứng dụng
    ↓
Ứng dụng tải AppConfig
    ↓
Khởi tạo các service
    ↓
Đăng ký collector, provider và analyzer
    ↓
Hiển thị giao diện
```

## 12.2. Luồng thu thập dữ liệu

```text
UI tạo yêu cầu thu thập
    ↓
CollectionService đọc cấu hình
    ↓
DataCollectorFactory trả về collector phù hợp
    ↓
Collector gọi nguồn dữ liệu hoặc đọc file
    ↓
Collector ánh xạ dữ liệu thành SocialMediaPost
    ↓
Trả danh sách bài đăng cho service
```

## 12.3. Luồng tiền xử lý

```text
PreprocessingService nhận danh sách bài đăng
    ↓
Đọc rawContent của từng bài
    ↓
Chạy qua PreprocessorChain
    ↓
Ghi kết quả vào content
```

## 12.4. Luồng sentiment

```text
AnalysisService yêu cầu SentimentModelFactory chọn provider
    ↓
Factory kiểm tra provider theo config
    ↓
Nếu Python API khả dụng: dùng PythonApiSentimentProvider
    ↓
Nếu không khả dụng: fallback sang DictionaryBasedSentimentProvider
    ↓
Gán sentiment và confidence vào bài đăng
```

## 12.5. Luồng chạy analyzer

```text
AnalysisService lấy danh sách analyzer từ registry
    ↓
Mỗi analyzer nhận cùng danh sách SocialMediaPost
    ↓
Analyzer tạo kết quả riêng
    ↓
Kết quả được gom theo analyzer ID
    ↓
UI hiển thị biểu đồ hoặc storage lưu kết quả
```

---

## 13. Cách thiết kế đáp ứng yêu cầu đề tài

| Yêu cầu | Cách giải quyết |
|---|---|
| Dễ thêm nguồn dữ liệu | Tạo implementation mới của `DataCollector` |
| Dễ thay đổi từ khóa | Sửa file `keywords.json` |
| Dễ đổi thời gian thảm họa | Sửa `app-config.json` |
| Nhiều bộ tiền xử lý | Mỗi bước implement `TextPreprocessor` |
| Bật/tắt tiền xử lý | Cấu hình `enabledSteps` |
| Dễ đổi mô hình sentiment | Implement `SentimentModelProvider` |
| Python chỉ dùng cho mô hình | Java gọi Python qua REST API |
| Dễ thay Python bằng Java | Hai bên cùng tuân theo interface/provider contract |
| Dễ thêm bài toán | Implement `Analyzer<T>` và đăng ký |
| Dễ bỏ bài toán | Hủy đăng ký hoặc tắt trong config |
| Dễ đổi lưu trữ | Implement `DataStorage` |

---

## 14. Design pattern và nguyên tắc OOP

## 14.1. Strategy Pattern

Áp dụng tại:

- `DataCollector`
- `TextPreprocessor`
- `SentimentModelProvider`
- `Analyzer`
- `DataStorage`

Mỗi interface đại diện cho một nhóm thuật toán hoặc cách triển khai có thể thay thế.

## 14.2. Template Method Pattern

Áp dụng tại `AbstractCollector`.

Lớp cha định nghĩa quy trình chung, lớp con triển khai bước cụ thể `doCollect()`.

## 14.3. Factory và Registry Pattern

Áp dụng tại:

- `DataCollectorFactory`
- `SentimentModelFactory`
- `AnalyzerRegistry`

Giúp chọn, đăng ký và thay thế implementation theo tên.

## 14.4. Adapter Pattern

`PythonApiSentimentProvider` chuyển REST API Python thành interface Java `SentimentModelProvider`.

## 14.5. Pipeline/Chain of Responsibility

`PreprocessorChain` chạy văn bản qua nhiều bước độc lập theo thứ tự.

## 14.6. Facade/Application Service

Các service cung cấp thao tác cấp cao cho UI và che giấu chi tiết triển khai.

## 14.7. SOLID

### Single Responsibility Principle

Mỗi lớp có một trách nhiệm chính:

- Collector thu thập.
- Preprocessor xử lý văn bản.
- Provider đánh giá sentiment.
- Analyzer giải quyết bài toán.
- Service điều phối.

### Open/Closed Principle

Có thể mở rộng bằng implementation mới mà hạn chế sửa lớp cũ.

### Liskov Substitution Principle

Mọi implementation của `SentimentModelProvider` phải có thể thay thế nhau mà không làm thay đổi hành vi mong đợi của service.

### Interface Segregation Principle

Các interface nhỏ và tập trung vào một nhiệm vụ.

### Dependency Inversion Principle

Service phụ thuộc vào abstraction như `DataCollector` và `SentimentModelProvider`, không phụ thuộc trực tiếp vào implementation.

---

## 15. Những điểm cần cải tiến trong thiết kế hiện tại

## 15.1. Enum làm giảm khả năng thêm category bằng config

`DamageCategory` và `ReliefCategory` hiện là enum. Điều này mâu thuẫn một phần với yêu cầu thêm danh mục linh động.

Khi thêm category mới trong JSON nhưng enum chưa có giá trị tương ứng, Java không thể sử dụng category đó đầy đủ.

Thiết kế linh động hơn:

```java
public class CategoryDefinition {
    private String id;
    private String name;
    private List<String> keywords;
}
```

Analyzer sử dụng `String categoryId` hoặc `CategoryDefinition` thay cho enum.

## 15.2. Tách bộ phân loại category khỏi analyzer

Hiện analyzer tự thực hiện keyword matching. Nếu muốn thay bằng AI, phải sửa analyzer.

Nên tạo interface:

```java
public interface CategoryClassifier {
    List<CategoryMatch> classify(
        String text,
        List<CategoryDefinition> categories
    );
}
```

Các implementation:

```text
KeywordCategoryClassifier
PythonApiCategoryClassifier
JavaMlCategoryClassifier
```

Analyzer chỉ tổng hợp kết quả, không chịu trách nhiệm quyết định thuật toán phân loại.

## 15.3. Dùng batch sentiment trong `AnalysisService`

`AnalysisService` hiện gọi `analyze()` cho từng bài. Nên chia dữ liệu thành các batch và gọi `analyzeBatch()`.

Điều này giảm đáng kể thời gian khi sử dụng Python API.

## 15.4. Kiểm tra analyzer được bật

Registry có phương thức `getEnabled()`, nhưng cần đảm bảo trạng thái bật/tắt thực sự được lấy từ `AppConfig`.

Có thể đưa trạng thái vào registry hoặc tạo `AnalyzerExecutionPolicy`.

## 15.5. Thay `Map<String, Object>` bằng kiểu kết quả rõ ràng

`Map<String, Object>` linh động nhưng không an toàn kiểu dữ liệu.

Có thể tạo:

```java
public class AnalysisResult<T> {
    private String analyzerId;
    private String analyzerName;
    private LocalDateTime executedAt;
    private T data;
}
```

Hoặc tạo một `AnalysisReport` chứa các kết quả đã định kiểu.

## 15.6. Thêm kiểm thử

Các lớp nên được kiểm thử độc lập:

- Collector: ánh xạ dữ liệu và lọc ngày.
- Preprocessor: đầu vào và đầu ra từng bước.
- Sentiment provider: parse API response và fallback.
- Analyzer: dữ liệu mẫu cho từng bài toán.
- Config: thiếu file, sai category và reload.

---

## 16. Quy trình triển khai chương trình từ đầu

Nếu xây dựng lại hệ thống từ đầu, nên thực hiện theo thứ tự sau.

### Bước 1: Xây dựng model trung tâm

Tạo `SocialMediaPost`, các enum cơ bản và model kết quả.

Mục tiêu là xác định định dạng dữ liệu chung trước khi viết collector hoặc analyzer.

### Bước 2: Xây dựng một collector đơn giản

Tạo `DataCollector`, `AbstractCollector` và `CsvFileCollector`.

CSV nên được làm trước vì dễ kiểm thử và không phụ thuộc API bên ngoài.

### Bước 3: Xây dựng pipeline tiền xử lý

Tạo `TextPreprocessor`, một vài implementation và `PreprocessorChain`.

Kiểm thử từng bước bằng các câu tiếng Việt mẫu.

### Bước 4: Xây dựng sentiment provider Java

Tạo `SentimentModelProvider` và `DictionaryBasedSentimentProvider`.

Provider Java giúp hoàn thiện luồng chương trình trước khi tích hợp Python.

### Bước 5: Xây dựng từng analyzer

Nên triển khai theo thứ tự:

1. `SentimentTimelineAnalyzer`.
2. `DamageClassificationAnalyzer`.
3. `ReliefSatisfactionAnalyzer`.
4. `ReliefSentimentTimelineAnalyzer`.

Bài toán đơn giản được làm trước để kiểm tra model và pipeline.

### Bước 6: Tạo các service điều phối

Sau khi từng component chạy độc lập, tạo service để nối chúng thành luồng hoàn chỉnh.

### Bước 7: Tích hợp Python API

Định nghĩa API contract trước, sau đó tạo `PythonApiSentimentProvider`.

Không để UI hoặc analyzer gọi HTTP trực tiếp.

### Bước 8: Xây dựng storage

Lưu dữ liệu thô, dữ liệu đã xử lý và kết quả phân tích để có thể chạy lại hoặc kiểm tra.

### Bước 9: Xây dựng UI

UI được làm sau cùng và chỉ gọi service.

Các màn hình chính có thể gồm:

- Cấu hình thảm họa và từ khóa.
- Thu thập/import dữ liệu.
- Xem dữ liệu trước và sau tiền xử lý.
- Chạy phân tích.
- Hiển thị biểu đồ bốn bài toán.

### Bước 10: Tối ưu và kiểm thử

- Chuyển sentiment sang batch.
- Thêm xử lý lỗi.
- Kiểm tra dữ liệu trùng lặp.
- Kiểm tra encoding tiếng Việt.
- Đo thời gian xử lý.
- So sánh độ chính xác các provider.

---

## 17. Câu hỏi tự kiểm tra khi thiết kế thêm chức năng

Trước khi thêm một lớp hoặc sửa kiến trúc, nên tự hỏi:

1. Lớp này có đúng một trách nhiệm chính không?
2. Thành phần này có nhiều cách triển khai không?
3. Nếu thêm implementation mới, có phải sửa service hiện tại không?
4. Dữ liệu cấu hình có đang bị viết cứng trong code không?
5. UI có đang chứa logic nghiệp vụ không?
6. Analyzer có đang tự thu thập hoặc tự gọi mô hình không?
7. Có thể kiểm thử lớp này độc lập không?
8. Có abstraction nào được tạo ra nhưng không giải quyết sự thay đổi thực tế không?

Không nên tạo interface cho mọi lớp. Interface chỉ có giá trị khi tồn tại nhu cầu thay thế, mở rộng hoặc tách phụ thuộc.

---

## 18. Kết luận

Thiết kế của hệ thống bắt đầu từ việc nhận diện pipeline nghiệp vụ và các thành phần dễ thay đổi.

Ý tưởng cốt lõi là:

```text
Mỗi bước xử lý có một trách nhiệm rõ ràng.
Mỗi thành phần cần thay thế được đặt sau một interface.
Service chịu trách nhiệm nối các thành phần.
Model tạo ngôn ngữ dữ liệu chung cho toàn hệ thống.
Cấu hình thay thế các giá trị thường xuyên thay đổi.
```

Nhờ cách thiết kế này, hệ thống có thể:

- Thêm nguồn dữ liệu mới.
- Thay đổi thảm họa, từ khóa và thời gian.
- Bật/tắt hoặc thêm bước tiền xử lý.
- Thay mô hình Python bằng mô hình Java.
- Thêm hoặc loại bỏ bài toán phân tích.
- Thay đổi cơ chế lưu trữ.

Quan trọng nhất, mỗi thay đổi được giới hạn trong đúng module chịu trách nhiệm, giảm ảnh hưởng tới toàn bộ chương trình.
