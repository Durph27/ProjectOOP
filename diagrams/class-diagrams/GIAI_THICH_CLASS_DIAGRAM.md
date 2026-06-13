# GIẢI THÍCH CÁC CLASS DIAGRAM

## 1. Mục đích của bộ class diagram

Hệ thống có nhiều nhóm lớp với trách nhiệm khác nhau: giao diện, thu thập dữ liệu,
tiền xử lý, xác định sentiment, chạy các bài toán phân tích và lưu kết quả.
Nếu đưa tất cả lớp vào một class diagram duy nhất, số lượng đường quan hệ sẽ lớn,
các đường dễ giao nhau và khó giải thích khi trình bày.

Vì vậy, class diagram tổng được tách thành sáu sơ đồ logic:

| STT | File | Phạm vi thể hiện |
|---:|---|---|
| 1 | `01-application-overview.asta` | Tổng quan ứng dụng và các lớp điều phối chính |
| 2 | `02-collection-subsystem.asta` | Phân hệ thu thập dữ liệu |
| 3 | `03-preprocessing-subsystem.asta` | Phân hệ tiền xử lý văn bản |
| 4 | `04-sentiment-subsystem.asta` | Phân hệ xác định sentiment |
| 5 | `05-analysis-subsystem.asta` | Phân hệ thực hiện các bài toán phân tích |
| 6 | `06-domain-model.asta` | Các model dữ liệu cốt lõi |

Mỗi sơ đồ chỉ thể hiện các thuộc tính, phương thức và quan hệ quan trọng đối với
phạm vi đang giải thích. Đây là **class diagram logic**, không phải bản sao đầy đủ
của toàn bộ source code.

---

## 2. Cách đọc ký hiệu UML

### 2.1. Class, abstract class và interface

- **Class** biểu diễn một lớp có thể tạo object.
- **Abstract class** biểu diễn lớp cơ sở chưa hoàn chỉnh, dùng để chia sẻ logic
  cho các lớp con.
- **Interface** mô tả hợp đồng hành vi mà nhiều implementation có thể thực hiện.

Các interface được vẽ dưới dạng hình chữ nhật có stereotype `«Interface»` thay vì
dạng lollipop để có thể nhìn rõ các phương thức trong interface.

### 2.2. Các loại quan hệ

| Quan hệ | Ký hiệu | Ý nghĩa |
|---|---|---|
| Generalization | Đường liền, tam giác rỗng hướng về lớp cha | Quan hệ kế thừa `extends` |
| Realization | Đường đứt, tam giác rỗng hướng về interface | Class thực thi interface bằng `implements` |
| Association | Đường liền | Một object giữ tham chiếu lâu dài tới object khác |
| Aggregation | Hình thoi rỗng tại phía quản lý | Object quản lý một tập thành phần nhưng thành phần có thể tồn tại độc lập |
| Composition | Hình thoi đặc tại phía sở hữu | Object sở hữu và quản lý vòng đời thành phần |
| Dependency | Mũi tên đứt | Chỉ sử dụng tạm thời qua tham số, kiểu trả về hoặc biến cục bộ |

### 2.3. Tại sao không nối mọi class với mọi model?

Một class có thể sử dụng nhiều kiểu dữ liệu trong source code, nhưng nếu tất cả
đều được vẽ, sơ đồ sẽ khó đọc. Bộ sơ đồ chỉ giữ những quan hệ giúp giải thích:

- trách nhiệm của class;
- hướng phụ thuộc;
- điểm mở rộng bằng interface;
- object nào sở hữu hoặc quản lý object nào;
- dữ liệu đầu vào và đầu ra quan trọng.

---

# 3. Sơ đồ 01 - Application Overview

## 3.1. Mục đích

Sơ đồ này cho biết chương trình được khởi động và điều phối ở mức cao nhất như
thế nào. Nó không đi sâu vào từng collector, preprocessor hay analyzer cụ thể.

Luồng tổng quát:

```text
HumanitarianApplication
        ↓
MainFrame
        ├── CollectionService
        ├── PreprocessingService
        ├── AnalysisService
        └── AppConfig
```

## 3.2. Vai trò các lớp

| Lớp | Vai trò |
|---|---|
| `HumanitarianApplication` | Điểm khởi động vòng đời JavaFX, tạo cửa sổ chính |
| `MainFrame` | Xây dựng UI, nhận thao tác người dùng và gọi service phù hợp |
| `CollectionService` | Điều phối việc thu thập dữ liệu |
| `PreprocessingService` | Điều phối chuỗi tiền xử lý |
| `AnalysisService` | Gán sentiment và chạy analyzer |
| `AppConfig` | Cung cấp cấu hình dùng chung |
| `DataCollectorFactory` | Quản lý và trả collector theo khóa |
| `PreprocessorChain` | Quản lý thứ tự các bước tiền xử lý |
| `AnalyzerRegistry` | Quản lý analyzer theo ID |
| `SentimentModelFactory` | Chọn sentiment provider theo cấu hình |

## 3.3. Giải thích các quan hệ

### `HumanitarianApplication` phụ thuộc `MainFrame`

`HumanitarianApplication.start()` tạo `MainFrame` để hiển thị giao diện. Quan hệ
này được vẽ là dependency vì application chỉ cần tạo và sử dụng cửa sổ khi khởi
động, không mô tả một cấu trúc nghiệp vụ phức tạp hơn.

### `MainFrame` composition với các service

`MainFrame` tự tạo `CollectionService`, `PreprocessingService` và
`AnalysisService` trong constructor, sau đó giữ chúng trong các thuộc tính
`private final`.

Vì các service này được tạo để phục vụ vòng đời của cửa sổ chính, sơ đồ dùng
composition để nhấn mạnh `MainFrame` sở hữu các object điều phối này.

### `MainFrame` association với `AppConfig`

UI giữ tham chiếu tới singleton `AppConfig` để đọc tên thảm họa, thời gian và từ
khóa. `AppConfig` không thuộc vòng đời của `MainFrame`; nó tồn tại độc lập và có
thể được nhiều component cùng sử dụng. Vì vậy quan hệ là association, không phải
composition.

### Service liên kết với factory, registry và chain

- `CollectionService` giữ `DataCollectorFactory`.
- `PreprocessingService` sở hữu `PreprocessorChain`.
- `AnalysisService` giữ `AnalyzerRegistry` và `SentimentModelFactory`.

Thiết kế này giúp UI chỉ giao tiếp với service. UI không cần biết cách collector,
provider hoặc analyzer được tạo và lựa chọn.

## 3.4. Tại sao vẽ sơ đồ theo dạng tầng?

Các lớp được bố trí từ trên xuống:

```text
Application/UI
    ↓
Service
    ↓
Factory / Registry / Chain
```

Cách bố trí này làm rõ hướng gọi của chương trình và nguyên tắc phân tách trách
nhiệm: UI không trực tiếp thực hiện nghiệp vụ, còn service không trực tiếp phụ
thuộc vào từng implementation cụ thể.

---

# 4. Sơ đồ 02 - Collection Subsystem

## 4.1. Mục đích

Sơ đồ mô tả cách chương trình hỗ trợ nhiều nguồn dữ liệu mà không phải sửa
`CollectionService` mỗi khi thêm nền tảng mới.

```text
CollectionService
    ↓
DataCollectorFactory
    ↓ quản lý
DataCollector
    ↑ implements
AbstractCollector
    ↑ extends
Các collector cụ thể
```

## 4.2. Tại sao cần `DataCollector` interface?

Mỗi nguồn dữ liệu có cách thu thập khác nhau:

- CSV đọc file;
- Twitter cần API key;
- Facebook, TikTok và YouTube cần API hoặc adapter riêng.

Tuy nhiên, tất cả đều phải cung cấp cùng các hành vi:

```java
collect(keywords, startDate, endDate)
isAvailable()
getPlatformName()
```

Vì vậy `DataCollector` được vẽ là interface. `CollectionService` chỉ làm việc với
interface này, không phụ thuộc trực tiếp vào từng nền tảng.

## 4.3. Tại sao có `AbstractCollector`?

Các collector đều cần logic chung như:

- lưu tên nền tảng;
- ghi log bắt đầu và kết thúc;
- kiểm tra danh sách từ khóa;
- gọi phần thu thập riêng của từng nguồn.

`AbstractCollector` triển khai quy trình `collect()` chung, sau đó yêu cầu lớp con
override `doCollect()`. Đây là Template Method:

```text
collect() chung
    ↓
doCollect() riêng của từng nền tảng
```

Vì `AbstractCollector` chưa thể tự thu thập dữ liệu, nó được vẽ là abstract class.

## 4.4. Quan hệ giữa các collector

- `AbstractCollector` realization `DataCollector`: lớp trừu tượng thực thi hợp
  đồng collector.
- `CsvFileCollector`, `FacebookCollector`, `TwitterCollector`,
  `TiktokCollector`, `YoutubeCollector` generalization tới `AbstractCollector`:
  các lớp này kế thừa logic chung và triển khai phần riêng.

Hai loại mũi tên khác nhau được dùng để phân biệt rõ:

```text
implements interface ≠ extends class
```

## 4.5. Tại sao Factory aggregation với `DataCollector`?

`DataCollectorFactory` giữ:

```java
Map<String, DataCollector> collectors
```

Factory quản lý các collector đã đăng ký, nhưng collector có thể được tạo bên
ngoài rồi truyền vào qua `register()`. Vì vậy collector có thể tồn tại độc lập
với factory; quan hệ phù hợp là aggregation thay vì composition.

Factory trong project đồng thời đóng vai trò Registry:

- đăng ký collector;
- tra cứu bằng khóa;
- liệt kê collector khả dụng;
- thay thế collector tại runtime.

## 4.6. Vì sao có `AppConfig` và `SocialMediaPost` trong sơ đồ?

- `CollectionService` dùng `AppConfig` để lấy từ khóa và khoảng thời gian.
- `DataCollector` trả về `List<SocialMediaPost>`.

Hai class này nằm ở biên của phân hệ: một class cung cấp cấu hình đầu vào, class
còn lại là dữ liệu đầu ra. Chúng được đưa vào sơ đồ để người đọc hiểu phân hệ
nhận gì và trả gì.

---

# 5. Sơ đồ 03 - Preprocessing Subsystem

## 5.1. Mục đích

Sơ đồ mô tả pipeline xử lý văn bản trước khi phân tích:

```text
PreprocessingService
    ↓
PreprocessorChain
    ↓
List<TextPreprocessor>
    ├── HtmlCleanerPreprocessor
    ├── UrlRemover
    ├── EmojiHandler
    ├── VietnameseNormalizer
    └── StopWordRemover
```

## 5.2. Tại sao dùng `TextPreprocessor` interface?

Mỗi bước biến đổi văn bản theo một cách khác nhau, nhưng đều có cùng hợp đồng:

```java
process(text)
getName()
getDescription()
getOrder()
```

Nhờ interface, `PreprocessorChain` không cần viết `if/else` để phân biệt từng
processor. Chain chỉ duyệt danh sách và gọi:

```java
result = processor.process(result);
```

Đây là Strategy Pattern: mỗi processor là một chiến lược biến đổi văn bản.

## 5.3. Tại sao `PreprocessingService` composition với `PreprocessorChain`?

`PreprocessingService` tự tạo chain, đăng ký processor và cấu hình các bước được
bật. Chain được xây dựng để phục vụ chính service này, nên được vẽ là composition.

## 5.4. Tại sao `PreprocessorChain` aggregation với `TextPreprocessor`?

Chain giữ một danh sách processor:

```java
List<TextPreprocessor> processors
```

Processor có thể được tạo và đăng ký từ bên ngoài bằng `addPreprocessor()`. Vì
processor không bắt buộc phải bị hủy cùng chain, quan hệ được vẽ là aggregation.

## 5.5. Ý nghĩa các implementation

| Class | Trách nhiệm |
|---|---|
| `HtmlCleanerPreprocessor` | Loại bỏ HTML |
| `UrlRemover` | Loại bỏ URL |
| `EmojiHandler` | Chuyển emoji thành từ mang nghĩa |
| `VietnameseNormalizer` | Chuẩn hóa văn bản tiếng Việt |
| `StopWordRemover` | Loại bỏ stop word |

Các class đều realization tới `TextPreprocessor`, thể hiện rằng chúng có thể được
thêm, bỏ hoặc thay đổi thứ tự mà không sửa `PreprocessorChain`.

## 5.6. Vì sao vẽ dependency tới `AppConfig` và `SocialMediaPost`?

- `PreprocessingService` đọc danh sách processor được bật từ `AppConfig`.
- Service nhận danh sách `SocialMediaPost`, giữ `rawContent` và cập nhật
  `content`.

Service chỉ sử dụng các object này trong quá trình xử lý, nên được biểu diễn bằng
dependency thay vì sở hữu.

---

# 6. Sơ đồ 04 - Sentiment Subsystem

## 6.1. Mục đích

Sơ đồ mô tả cách chương trình có thể thay đổi mô hình sentiment mà không sửa
`AnalysisService`.

```text
AnalysisService
    ↓
SentimentModelFactory
    ↓ chọn provider
SentimentModelProvider
    ├── DictionaryBasedSentimentProvider
    └── PythonApiSentimentProvider
```

## 6.2. Tại sao có `SentimentModelProvider` interface?

Chương trình hiện hỗ trợ hai cách xác định sentiment:

- chạy từ điển trực tiếp bằng Java;
- gọi Flask API để sử dụng PhoBERT hoặc mô hình Python.

Hai cách triển khai rất khác nhau, nhưng đều trả về `SentimentResult`. Interface
chuẩn hóa hành vi:

```java
analyze(text)
analyzeBatch(texts)
isAvailable()
getModelName()
```

Do đó `AnalysisService` chỉ gọi `provider.analyze(text)` và không cần biết kết
quả đến từ Java hay Python.

## 6.3. Tại sao Factory aggregation với provider?

`SentimentModelFactory` giữ:

```java
Map<String, SentimentModelProvider> providers
```

Factory quản lý các provider đã đăng ký và chọn provider theo `AppConfig`.
Provider có thể được tạo bên ngoài rồi đăng ký thêm, nên quan hệ là aggregation.

Factory còn chứa chính sách fallback:

```text
Provider cấu hình khả dụng
    → sử dụng provider đó

Provider không khả dụng
    → chuyển sang dictionary
```

Việc đặt chính sách lựa chọn trong factory giúp `AnalysisService` không chứa
logic kiểm tra Python API hoặc fallback.

## 6.4. Tại sao hai provider realization cùng interface?

- `DictionaryBasedSentimentProvider` dùng `Set<String>` để kiểm tra từ tích cực,
  tiêu cực, phủ định và tăng cường.
- `PythonApiSentimentProvider` dùng `OkHttpClient` để chuyển lời gọi Java thành
  HTTP request tới Flask API.

Hai class cùng realization `SentimentModelProvider` vì chúng là hai Strategy có
thể thay thế cho nhau.

`PythonApiSentimentProvider` đồng thời thể hiện Adapter Pattern: nó che giấu
HTTP/JSON phía sau interface Java.

## 6.5. Vai trò của `SentimentResult` và `SocialMediaPost`

- `SentimentResult` là kết quả tạm thời do provider trả về.
- `AnalysisService` lấy sentiment và confidence từ kết quả rồi ghi vào
  `SocialMediaPost`.

Sơ đồ vẽ dependency vì provider và service sử dụng các model trong quá trình gọi,
nhưng không sở hữu vòng đời của chúng.

---

# 7. Sơ đồ 05 - Analysis Subsystem

## 7.1. Mục đích

Sơ đồ mô tả kiến trúc mở rộng cho bốn bài toán phân tích:

```text
AnalysisService
    ↓
AnalyzerRegistry
    ↓ quản lý
Analyzer<T>
    ├── SentimentTimelineAnalyzer
    ├── DamageClassificationAnalyzer
    ├── ReliefSatisfactionAnalyzer
    └── ReliefSentimentTimelineAnalyzer
```

## 7.2. Tại sao `Analyzer<T>` là generic interface?

Các analyzer cùng nhận:

```java
List<SocialMediaPost>
```

nhưng trả về các kiểu kết quả khác nhau:

| Analyzer | Kiểu kết quả |
|---|---|
| `SentimentTimelineAnalyzer` | `Map<LocalDate, TimeSentimentData>` |
| `DamageClassificationAnalyzer` | `Map<CategoryDefinition, List<DamageReport>>` |
| `ReliefSatisfactionAnalyzer` | `Map<CategoryDefinition, ReliefSentiment>` |
| `ReliefSentimentTimelineAnalyzer` | `Map<CategoryDefinition, Map<LocalDate, TimeSentimentData>>` |

Generic type `T` cho phép một interface mô tả tất cả analyzer mà vẫn giữ được
kiểu kết quả của từng bài toán:

```java
public interface Analyzer<T> {
    T analyze(List<SocialMediaPost> posts);
}
```

## 7.3. Tại sao cần `AnalyzerRegistry`?

Registry giữ:

```java
Map<String, Analyzer<?>> analyzers
```

Nó cho phép:

- đăng ký analyzer theo ID;
- lấy analyzer khi UI gửi ID;
- hủy đăng ký;
- chạy toàn bộ analyzer;
- mở rộng bài toán mới mà ít sửa `AnalysisService`.

Registry aggregation với `Analyzer` vì analyzer có thể được tạo bên ngoài và
đăng ký vào registry. Registry quản lý tập analyzer nhưng không bắt buộc sở hữu
vòng đời tuyệt đối của chúng.

## 7.4. Tại sao các analyzer realization tới `Analyzer`?

Mỗi analyzer triển khai một thuật toán khác nhau nhưng tuân theo cùng hợp đồng.
Quan hệ realization thể hiện đa hình:

```java
Analyzer<T> analyzer = registry.get(id);
T result = analyzer.analyze(posts);
```

`AnalysisService` không cần biết class cụ thể nào đang chạy.

## 7.5. Quan hệ với các model đầu ra

- `SentimentTimelineAnalyzer` tạo `TimeSentimentData`.
- `DamageClassificationAnalyzer` tạo `DamageReport`.
- `ReliefSatisfactionAnalyzer` tạo `ReliefSentiment`.
- `ReliefSentimentTimelineAnalyzer` tạo `TimeSentimentData` theo từng loại cứu trợ
  và từng ngày.

Các quan hệ này được vẽ là dependency vì analyzer tạo hoặc sử dụng model trong
phương thức `analyze()`, nhưng model không phải thuộc tính sở hữu lâu dài của
analyzer.

## 7.6. Tại sao `DamageReport` và `ReliefSentiment` association với
`CategoryDefinition`?

Hai model này giữ `CategoryDefinition` trong thuộc tính:

```java
private CategoryDefinition category;
```

Đây là tham chiếu bền vững trong trạng thái object nên được vẽ là association,
không phải dependency.

`CategoryDefinition` tồn tại độc lập và được tải từ JSON; nó không bị tạo hoặc
hủy theo `DamageReport` hay `ReliefSentiment`. Vì vậy không dùng composition.

## 7.7. Tại sao `AppConfig` xuất hiện trong sơ đồ?

Các analyzer đọc danh mục thiệt hại, danh mục cứu trợ và nhãn sentiment từ cấu
hình. Registry cũng có định hướng lọc analyzer được bật theo config. `AppConfig`
được vẽ ở rìa sơ đồ để thể hiện dữ liệu phân loại đến từ cấu hình ngoài thay vì
được viết cứng trong analyzer.

---

# 8. Sơ đồ 06 - Domain Model

## 8.1. Mục đích

Sơ đồ này chỉ tập trung vào dữ liệu nghiệp vụ, không thể hiện service, factory,
registry hoặc UI. Nó trả lời câu hỏi:

> Hệ thống lưu những loại dữ liệu nào và các model liên hệ với nhau ra sao?

## 8.2. `SocialMediaPost`

Đây là model trung tâm của pipeline. Nó đi qua các trạng thái:

```text
Dữ liệu thu thập
    → rawContent
    → content sau tiền xử lý
    → sentiment và sentimentConfidence
    → đầu vào cho analyzer
```

`SocialMediaPost` chứa cả dữ liệu nguồn và kết quả trung gian để các bước sau có
thể tái sử dụng mà không phải đọc lại CSV hoặc phân tích lại.

## 8.3. `SentimentResult`

Model biểu diễn kết quả một lần dự đoán:

- nhãn sentiment;
- confidence;
- tên mô hình.

Nó được tách khỏi `SocialMediaPost` vì provider cần trả một kết quả độc lập trước
khi service quyết định ghi kết quả vào bài đăng.

## 8.4. `TimeSentimentData`

Model này đóng gói ba bộ đếm:

```text
positiveCount
negativeCount
neutralCount
```

Nó được dùng lại cho Bài toán 1 và Bài toán 4. Việc đưa bộ đếm và các phép tính tỷ
lệ vào model tránh lặp logic trong analyzer và UI.

## 8.5. `CategoryDefinition`

Model biểu diễn danh mục được tải từ JSON:

- ID ổn định dùng trong code;
- tên tiếng Việt và tiếng Anh để hiển thị;
- danh sách từ khóa để phân loại.

Việc mô hình hóa danh mục thành object giúp chương trình thay đổi danh mục bằng
file cấu hình mà không phải sửa hoặc biên dịch lại analyzer.

## 8.6. `DamageReport`

Mỗi `DamageReport` biểu diễn kết quả phân loại một bài đăng vào một loại thiệt
hại, gồm:

- ID bài đăng;
- danh mục thiệt hại;
- sentiment;
- confidence;
- trích đoạn khớp.

Nó association với `CategoryDefinition` vì giữ tham chiếu tới danh mục đã phân
loại.

## 8.7. `ReliefSentiment`

Model tổng hợp sentiment cho một loại cứu trợ. Nó giữ:

- `CategoryDefinition`;
- số phản hồi tích cực, tiêu cực, trung lập;
- tỷ lệ hài lòng.

Nó association với `CategoryDefinition` vì kết quả chỉ có ý nghĩa khi gắn với một
loại cứu trợ cụ thể.

## 8.8. Tại sao Domain Model có ít đường quan hệ?

Các model chủ yếu là các cấu trúc dữ liệu độc lập được truyền giữa các tầng.
Chỉ những model thật sự giữ tham chiếu tới model khác mới được nối bằng
association.

Ví dụ, `SocialMediaPost` không giữ `SentimentResult`; service chỉ sao chép nhãn và
confidence từ `SentimentResult` vào post. Vì vậy hai model không được vẽ
association trực tiếp.

---

# 9. Lý do thiết kế chung của toàn bộ bộ sơ đồ

## 9.1. Tách phần ổn định khỏi phần dễ thay đổi

Các interface được đặt tại những điểm hệ thống có khả năng thay đổi:

| Điểm thay đổi | Interface |
|---|---|
| Nguồn thu thập dữ liệu | `DataCollector` |
| Bước tiền xử lý | `TextPreprocessor` |
| Mô hình sentiment | `SentimentModelProvider` |
| Bài toán phân tích | `Analyzer<T>` |

Service phụ thuộc vào interface thay vì class cụ thể. Đây là nền tảng của đa hình
và Dependency Inversion.

## 9.2. Factory, Registry và Chain được vẽ ở trung tâm phân hệ

- Factory/Registry giải quyết việc đăng ký, lựa chọn và thay thế Strategy.
- Chain giải quyết việc sắp xếp và chạy nhiều Strategy liên tiếp.
- Service điều phối use case nhưng không chứa chi tiết thuật toán.

Cách vẽ làm nổi bật các điểm mở rộng quan trọng thay vì chỉ liệt kê class.

## 9.3. Model được đặt ở biên hoặc phía dưới

Model là dữ liệu được tạo, truyền và tổng hợp bởi các lớp nghiệp vụ. Đặt model ở
phía dưới hoặc rìa sơ đồ giúp phân biệt:

```text
Class thực hiện hành vi
        ↓
Model chứa dữ liệu
```

## 9.4. Màu sắc

Trình tạo sơ đồ sử dụng màu để phân nhóm trực quan:

| Màu | Nhóm lớp |
|---|---|
| Xanh dương | Application và UI |
| Xanh lá | Service |
| Tím | `AppConfig` |
| Vàng nhạt | Model dữ liệu |
| Cam nhạt | Factory, registry, strategy và các lớp còn lại |

Màu sắc không mang ý nghĩa UML chính thức; nó chỉ giúp người xem nhận biết vai
trò lớp nhanh hơn.

## 9.5. Tại sao chỉ hiện một số thuộc tính và phương thức?

Class diagram được dùng để giải thích kiến trúc, không thay thế source code. Chỉ
các thành viên thể hiện rõ trách nhiệm hoặc quan hệ thiết kế mới được đưa vào.

Ví dụ:

- Factory hiện `Map` registry và phương thức `get/register`.
- Strategy hiện phương thức hành vi chính.
- Model hiện dữ liệu quan trọng.
- Getter/setter thông thường được lược bỏ để giảm nhiễu.

---

# 10. Luồng kết nối giữa sáu sơ đồ

Sáu sơ đồ không tách biệt hoàn toàn mà ghép lại thành một pipeline:

```text
Application Overview
    MainFrame gọi các service
        ↓
Collection Subsystem
    tạo List<SocialMediaPost>
        ↓
Preprocessing Subsystem
    cập nhật content
        ↓
Sentiment Subsystem
    cập nhật sentiment và confidence
        ↓
Analysis Subsystem
    tạo kết quả của bốn bài toán
        ↓
Domain Model
    cung cấp các kiểu dữ liệu dùng xuyên suốt
```

Việc lặp lại một số class như `AnalysisService`, `AppConfig` và
`SocialMediaPost` ở nhiều sơ đồ là có chủ ý. Chúng là các class nằm tại ranh giới
giữa nhiều phân hệ; lặp lại chúng giúp mỗi sơ đồ có thể được đọc và trình bày độc
lập.

---

# 11. Kết luận

Bộ class diagram được vẽ để nhấn mạnh kiến trúc hướng đối tượng của chương trình:

- interface mô tả hợp đồng;
- abstract class chia sẻ quy trình chung;
- implementation có thể thay thế bằng đa hình;
- factory và registry quản lý các implementation;
- service điều phối luồng nghiệp vụ;
- model đóng gói dữ liệu và kết quả;
- dependency, association, aggregation và composition được phân biệt theo mức độ
  sử dụng và sở hữu object.

Tách sơ đồ theo phân hệ giúp mỗi sơ đồ có một câu chuyện rõ ràng, giảm số đường
giao nhau và thuận tiện khi giải thích thiết kế trong báo cáo hoặc thuyết trình.
