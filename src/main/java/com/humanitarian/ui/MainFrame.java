package com.humanitarian.ui;

import com.humanitarian.config.AppConfig;
import com.humanitarian.model.*;
import com.humanitarian.model.enums.*;
import com.humanitarian.service.*;
import com.humanitarian.collector.CsvFileCollector;
import com.humanitarian.collector.DataCollectorFactory;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cửa sổ chính của ứng dụng JavaFX.
 * Sử dụng TabPane với các tab cho từng chức năng:
 * - Dashboard (Tổng quan)
 * - Thu thập dữ liệu
 * - Tiền xử lý
 * - Bài toán 1-4 (Phân tích)
 */
public class MainFrame extends BorderPane {

    private final CollectionService collectionService;
    private final PreprocessingService preprocessingService;
    private final AnalysisService analysisService;
    private final AppConfig config;

    private List<SocialMediaPost> currentPosts = new ArrayList<>();
    private Map<String, Object> analysisResults = new LinkedHashMap<>();

    // UI Components
    private final TabPane tabPane;
    private final Label statusLabel;
    private final ProgressBar progressBar;
    private final TextArea logArea;

    public MainFrame() {
        this.collectionService = new CollectionService();
        this.preprocessingService = new PreprocessingService();
        this.analysisService = new AnalysisService();
        this.config = AppConfig.getInstance();

        // Status bar
        statusLabel = new Label("Sẵn sàng");
        statusLabel.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 13px;");
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        progressBar.setVisible(false);

        HBox statusBar = new HBox(10, statusLabel, progressBar);
        statusBar.setPadding(new Insets(8, 15, 8, 15));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle("-fx-background-color: #2c3e50;");

        // Log area
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(4);
        logArea.setStyle("-fx-control-inner-background: #1e272e; -fx-text-fill: #a4b0be; " +
                "-fx-font-family: 'Consolas'; -fx-font-size: 12px;");

        // Tab pane
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-font-size: 14px;");

        tabPane.getTabs().addAll(
                createTab("📊 Tổng quan", createDashboardPanel()),
                createTab("📥 Thu thập", createCollectionPanel()),
                createTab("⚙️ Tiền xử lý", createPreprocessingPanel()),
                createTab("📈 BT1: Tâm lý theo thời gian", createProblem1Panel()),
                createTab("🏚️ BT2: Phân loại thiệt hại", createProblem2Panel()),
                createTab("😊 BT3: Hài lòng cứu trợ", createProblem3Panel()),
                createTab("📉 BT4: Cứu trợ theo thời gian", createProblem4Panel())
        );

        // Header
        VBox header = createHeader();

        // Layout
        VBox bottomBox = new VBox(logArea, statusBar);
        setTop(header);
        setCenter(tabPane);
        setBottom(bottomBox);

        log("Ứng dụng khởi động thành công. Thảm họa: " + config.getDisasterName());
    }

    private VBox createHeader() {
        Text title = new Text("🌏 Phân Tích Mạng Xã Hội - Logistics Nhân Đạo");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setFill(Color.WHITE);

        Text subtitle = new Text("Thảm họa: " + config.getDisasterName() +
                " | Thời gian: " + config.getDisasterStartDate() + " → " + config.getDisasterEndDate());
        subtitle.setFont(Font.font("System", 13));
        subtitle.setFill(Color.web("#bdc3c7"));

        VBox header = new VBox(5, title, subtitle);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: linear-gradient(to right, #2c3e50, #3498db);");
        return header;
    }

    private Tab createTab(String title, Node content) {
        Tab tab = new Tab(title, content);
        return tab;
    }

    // ==================== DASHBOARD ====================
    private Node createDashboardPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));

        Label titleLabel = sectionTitle("Tổng Quan Hệ Thống");

        // Info cards
        GridPane cards = new GridPane();
        cards.setHgap(15);
        cards.setVgap(15);

        cards.add(createInfoCard("📦 Dữ liệu", "Chưa tải", "#3498db"), 0, 0);
        cards.add(createInfoCard("🔧 Tiền xử lý", "5 bước", "#2ecc71"), 1, 0);
        cards.add(createInfoCard("🤖 Mô hình", config.getSentimentProvider(), "#e74c3c"), 2, 0);
        cards.add(createInfoCard("📊 Bài toán", "4 bài toán", "#f39c12"), 3, 0);

        // Architecture info
        TextArea archInfo = new TextArea();
        archInfo.setEditable(false);
        archInfo.setPrefRowCount(12);
        archInfo.setStyle("-fx-font-size: 13px;");
        archInfo.setText("""
                === KIẾN TRÚC ỨNG DỤNG ===
                
                📐 Design Patterns sử dụng:
                  • Strategy Pattern: DataCollector, TextPreprocessor, SentimentModelProvider, Analyzer
                  • Factory Pattern: DataCollectorFactory, SentimentModelFactory
                  • Chain of Responsibility: PreprocessorChain
                  • Registry Pattern: AnalyzerRegistry
                  • Singleton: AppConfig
                  • Adapter: PythonApiSentimentProvider
                
                🔌 Tính mở rộng:
                  • Thêm nguồn dữ liệu: Implement DataCollector + đăng ký vào Factory
                  • Thêm bước tiền xử lý: Implement TextPreprocessor + thêm vào Chain
                  • Thêm bài toán: Implement Analyzer<T> + đăng ký vào Registry
                  • Đổi mô hình sentiment: Thay đổi "sentiment.provider" trong config
                  • Đổi Python → Java: Implement SentimentModelProvider bằng Java
                
                📁 Cấu hình linh hoạt:
                  • config/app-config.json: Cấu hình chính
                  • config/keywords.json: Từ khóa, hashtags
                  • config/damage-categories.json: Danh mục thiệt hại + từ khóa
                  • config/relief-categories.json: Danh mục cứu trợ + từ khóa
                """);

        panel.getChildren().addAll(titleLabel, cards, new Separator(), archInfo);
        return new ScrollPane(panel);
    }

    private VBox createInfoCard(String title, String value, String color) {
        Label titleL = new Label(title);
        titleL.setStyle("-fx-font-size: 13px; -fx-text-fill: white;");
        Label valueL = new Label(value);
        valueL.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        VBox card = new VBox(5, titleL, valueL);
        card.setPadding(new Insets(15));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10;");
        return card;
    }

    // ==================== COLLECTION ====================
    private Node createCollectionPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));

        Label titleLabel = sectionTitle("Thu Thập Dữ Liệu");

        // File chooser
        HBox fileBox = new HBox(10);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        TextField fileField = new TextField("data/sample/yagi_sample.csv");
        fileField.setPrefWidth(400);
        Button browseBtn = new Button("📁 Chọn file CSV");
        browseBtn.setStyle(btnStyle("#3498db"));
        browseBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fc.showOpenDialog(getScene().getWindow());
            if (file != null) fileField.setText(file.getAbsolutePath());
        });
        fileBox.getChildren().addAll(new Label("File dữ liệu:"), fileField, browseBtn);

        // Keywords display
        TextArea keywordsArea = new TextArea(String.join(", ", config.getKeywords()));
        keywordsArea.setPrefRowCount(3);
        keywordsArea.setEditable(false);

        // Collect button
        Button collectBtn = new Button("▶ Thu thập dữ liệu");
        collectBtn.setStyle(btnStyle("#2ecc71"));
        collectBtn.setPrefWidth(200);

        // Results table
        TableView<SocialMediaPost> table = createPostsTable();

        collectBtn.setOnAction(e -> {
            String filePath = fileField.getText();
            log("Đang thu thập từ: " + filePath);

            CsvFileCollector csvCollector = (CsvFileCollector) DataCollectorFactory.getInstance().get("csv");
            csvCollector.setFilePath(filePath);

            new Thread(() -> {
                List<SocialMediaPost> posts = collectionService.collectFrom("csv");
                Platform.runLater(() -> {
                    currentPosts = new ArrayList<>(posts);
                    table.setItems(FXCollections.observableArrayList(posts));
                    log("Thu thập thành công: " + posts.size() + " bài đăng");
                    setStatus("Đã tải " + posts.size() + " bài đăng");
                });
            }).start();
        });

        panel.getChildren().addAll(titleLabel, fileBox,
                new Label("Từ khóa:"), keywordsArea,
                collectBtn, new Separator(),
                new Label("Dữ liệu thu thập:"), table);
        return new ScrollPane(panel);
    }

    // ==================== PREPROCESSING ====================
    private Node createPreprocessingPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));

        Label titleLabel = sectionTitle("Tiền Xử Lý Dữ Liệu");

        // Preprocessor list with checkboxes
        VBox checkboxes = new VBox(8);
        List<String> enabled = config.getEnabledPreprocessors();
        Map<String, CheckBox> cbMap = new LinkedHashMap<>();

        Map<String, String> descriptions = Map.of(
                "html_cleaner", "Loại bỏ HTML tags và entities",
                "url_remover", "Loại bỏ URLs",
                "emoji_handler", "Chuyển đổi emoji thành từ khóa cảm xúc",
                "vietnamese_normalizer", "Chuẩn hóa Unicode, lowercase, ký tự lặp",
                "stopword_remover", "Loại bỏ stop words tiếng Việt"
        );

        for (Map.Entry<String, String> desc : descriptions.entrySet()) {
            CheckBox cb = new CheckBox(desc.getKey() + " - " + desc.getValue());
            cb.setSelected(enabled.contains(desc.getKey()));
            cbMap.put(desc.getKey(), cb);
            checkboxes.getChildren().add(cb);
        }

        // Preview
        TextArea beforeArea = new TextArea();
        beforeArea.setPromptText("Nội dung trước tiền xử lý...");
        beforeArea.setPrefRowCount(3);

        TextArea afterArea = new TextArea();
        afterArea.setPromptText("Nội dung sau tiền xử lý...");
        afterArea.setPrefRowCount(3);
        afterArea.setEditable(false);

        Button testBtn = new Button("🔍 Thử nghiệm");
        testBtn.setStyle(btnStyle("#9b59b6"));
        testBtn.setOnAction(e -> {
            String result = preprocessingService.preprocess(beforeArea.getText());
            afterArea.setText(result);
        });

        // Process all button
        Button processBtn = new Button("⚙️ Tiền xử lý toàn bộ dữ liệu");
        processBtn.setStyle(btnStyle("#2ecc71"));
        processBtn.setPrefWidth(250);
        processBtn.setOnAction(e -> {
            if (currentPosts.isEmpty()) {
                showAlert("Chưa có dữ liệu. Vui lòng thu thập dữ liệu trước.");
                return;
            }
            preprocessingService.preprocess(currentPosts);
            log("Tiền xử lý hoàn tất: " + currentPosts.size() + " bài đăng");
            setStatus("Đã tiền xử lý " + currentPosts.size() + " bài đăng");
        });

        panel.getChildren().addAll(titleLabel,
                new Label("Các bước tiền xử lý:"), checkboxes,
                new Separator(),
                new Label("Thử nghiệm:"), beforeArea, testBtn, afterArea,
                new Separator(),
                processBtn);
        return new ScrollPane(panel);
    }

    // ==================== BÀI TOÁN 1 ====================
    private Node createProblem1Panel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));

        Label titleLabel = sectionTitle("Bài toán 1: Theo dõi tâm lý công chúng theo thời gian");
        Label descLabel = new Label("Phân tích số lượng post tích cực/tiêu cực theo từng ngày.");
        descLabel.setWrapText(true);

        StackPane chartContainer = new StackPane();
        chartContainer.setPrefHeight(400);
        chartContainer.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        TextArea insightArea = new TextArea();
        insightArea.setPromptText("Nhận xét sẽ hiển thị ở đây...");
        insightArea.setPrefRowCount(6);
        insightArea.setEditable(false);

        Button analyzeBtn = new Button("📈 Phân tích");
        analyzeBtn.setStyle(btnStyle("#3498db"));
        analyzeBtn.setPrefWidth(200);
        analyzeBtn.setOnAction(e -> {
            if (!ensureDataReady()) return;
            runAnalysis("sentiment_timeline", chartContainer, insightArea);
        });

        panel.getChildren().addAll(titleLabel, descLabel, analyzeBtn,
                new Separator(), chartContainer, insightArea);
        return new ScrollPane(panel);
    }

    // ==================== BÀI TOÁN 2 ====================
    private Node createProblem2Panel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));

        Label titleLabel = sectionTitle("Bài toán 2: Phân loại mức độ và loại thiệt hại");
        Label descLabel = new Label("Phân loại bài đăng theo 6 danh mục thiệt hại chính.");
        descLabel.setWrapText(true);

        StackPane chartContainer = new StackPane();
        chartContainer.setPrefHeight(400);
        chartContainer.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        TextArea insightArea = new TextArea();
        insightArea.setPromptText("Nhận xét sẽ hiển thị ở đây...");
        insightArea.setPrefRowCount(6);
        insightArea.setEditable(false);

        Button analyzeBtn = new Button("🏚️ Phân tích");
        analyzeBtn.setStyle(btnStyle("#e67e22"));
        analyzeBtn.setPrefWidth(200);
        analyzeBtn.setOnAction(e -> {
            if (!ensureDataReady()) return;
            runAnalysis("damage_classification", chartContainer, insightArea);
        });

        panel.getChildren().addAll(titleLabel, descLabel, analyzeBtn,
                new Separator(), chartContainer, insightArea);
        return new ScrollPane(panel);
    }

    // ==================== BÀI TOÁN 3 ====================
    private Node createProblem3Panel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));

        Label titleLabel = sectionTitle("Bài toán 3: Mức hài lòng theo loại cứu trợ");
        Label descLabel = new Label("Đánh giá tâm lý tích cực/tiêu cực theo 5 loại hàng cứu trợ.");
        descLabel.setWrapText(true);

        StackPane chartContainer = new StackPane();
        chartContainer.setPrefHeight(400);
        chartContainer.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        TextArea insightArea = new TextArea();
        insightArea.setPromptText("Nhận xét sẽ hiển thị ở đây...");
        insightArea.setPrefRowCount(6);
        insightArea.setEditable(false);

        Button analyzeBtn = new Button("😊 Phân tích");
        analyzeBtn.setStyle(btnStyle("#27ae60"));
        analyzeBtn.setPrefWidth(200);
        analyzeBtn.setOnAction(e -> {
            if (!ensureDataReady()) return;
            runAnalysis("relief_satisfaction", chartContainer, insightArea);
        });

        panel.getChildren().addAll(titleLabel, descLabel, analyzeBtn,
                new Separator(), chartContainer, insightArea);
        return new ScrollPane(panel);
    }

    // ==================== BÀI TOÁN 4 ====================
    private Node createProblem4Panel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));

        Label titleLabel = sectionTitle("Bài toán 4: Tâm lý theo loại cứu trợ theo thời gian");
        Label descLabel = new Label("Theo dõi tâm lý thay đổi theo thời gian cho từng loại cứu trợ.");
        descLabel.setWrapText(true);

        StackPane chartContainer = new StackPane();
        chartContainer.setPrefHeight(450);
        chartContainer.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        TextArea insightArea = new TextArea();
        insightArea.setPromptText("Nhận xét sẽ hiển thị ở đây...");
        insightArea.setPrefRowCount(6);
        insightArea.setEditable(false);

        Button analyzeBtn = new Button("📉 Phân tích");
        analyzeBtn.setStyle(btnStyle("#8e44ad"));
        analyzeBtn.setPrefWidth(200);
        analyzeBtn.setOnAction(e -> {
            if (!ensureDataReady()) return;
            runAnalysis("relief_sentiment_timeline", chartContainer, insightArea);
        });

        panel.getChildren().addAll(titleLabel, descLabel, analyzeBtn,
                new Separator(), chartContainer, insightArea);
        return new ScrollPane(panel);
    }

    // ==================== ANALYSIS RUNNER ====================
    @SuppressWarnings("unchecked")
    private void runAnalysis(String analyzerId, StackPane chartContainer, TextArea insightArea) {
        setStatus("Đang phân tích: " + analyzerId + "...");
        log("Bắt đầu phân tích: " + analyzerId);

        new Thread(() -> {
            // Gán sentiment nếu chưa có
            boolean needsSentiment = currentPosts.stream()
                    .anyMatch(p -> p.getSentiment() == null);
            if (needsSentiment) {
                Platform.runLater(() -> log("Đang gán sentiment..."));
                analysisService.assignSentiment(currentPosts);
            }

            // Chạy analyzer
            Object result = analysisService.runAnalyzer(analyzerId, currentPosts);
            analysisResults.put(analyzerId, result);

            Platform.runLater(() -> {
                chartContainer.getChildren().clear();

                switch (analyzerId) {
                    case "sentiment_timeline" -> {
                        Map<LocalDate, TimeSentimentData> data =
                                (Map<LocalDate, TimeSentimentData>) result;
                        chartContainer.getChildren().add(createSentimentTimelineChart(data));
                        insightArea.setText(generateTimelineInsight(data));
                    }
                    case "damage_classification" -> {
                        Map<DamageCategory, List<DamageReport>> data =
                                (Map<DamageCategory, List<DamageReport>>) result;
                        chartContainer.getChildren().add(createDamageChart(data));
                        insightArea.setText(generateDamageInsight(data));
                    }
                    case "relief_satisfaction" -> {
                        Map<ReliefCategory, ReliefSentiment> data =
                                (Map<ReliefCategory, ReliefSentiment>) result;
                        chartContainer.getChildren().add(createReliefSatisfactionChart(data));
                        insightArea.setText(generateReliefInsight(data));
                    }
                    case "relief_sentiment_timeline" -> {
                        Map<ReliefCategory, Map<LocalDate, TimeSentimentData>> data =
                                (Map<ReliefCategory, Map<LocalDate, TimeSentimentData>>) result;
                        chartContainer.getChildren().add(createReliefTimelineChart(data));
                        insightArea.setText(generateReliefTimelineInsight(data));
                    }
                }

                log("Phân tích " + analyzerId + " hoàn thành");
                setStatus("Phân tích hoàn thành: " + analyzerId);
            });
        }).start();
    }

    // ==================== CHART CREATION ====================
    private Node createSentimentTimelineChart(Map<LocalDate, TimeSentimentData> data) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Ngày");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Số lượng bài đăng");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Thay đổi tâm lý công chúng theo thời gian");
        chart.setCreateSymbols(true);

        XYChart.Series<String, Number> posSeries = new XYChart.Series<>();
        posSeries.setName("Tích cực");
        XYChart.Series<String, Number> negSeries = new XYChart.Series<>();
        negSeries.setName("Tiêu cực");
        XYChart.Series<String, Number> neuSeries = new XYChart.Series<>();
        neuSeries.setName("Trung lập");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        for (Map.Entry<LocalDate, TimeSentimentData> entry : data.entrySet()) {
            String dateStr = entry.getKey().format(fmt);
            TimeSentimentData d = entry.getValue();
            posSeries.getData().add(new XYChart.Data<>(dateStr, d.getPositiveCount()));
            negSeries.getData().add(new XYChart.Data<>(dateStr, d.getNegativeCount()));
            neuSeries.getData().add(new XYChart.Data<>(dateStr, d.getNeutralCount()));
        }

        chart.getData().addAll(posSeries, negSeries, neuSeries);
        chart.setAnimated(true);
        return chart;
    }

    private Node createDamageChart(Map<DamageCategory, List<DamageReport>> data) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Loại thiệt hại");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Số lượng báo cáo");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Phân loại thiệt hại");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Số bài đăng");

        data.entrySet().stream()
                .sorted((a, b) -> b.getValue().size() - a.getValue().size())
                .forEach(entry -> {
                    if (!entry.getValue().isEmpty()) {
                        series.getData().add(new XYChart.Data<>(
                                entry.getKey().getNameVi(), entry.getValue().size()));
                    }
                });

        chart.getData().add(series);
        chart.setAnimated(true);
        return chart;
    }

    private Node createReliefSatisfactionChart(Map<ReliefCategory, ReliefSentiment> data) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Loại cứu trợ");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Số lượng bài đăng");

        StackedBarChart<String, Number> chart = new StackedBarChart<>(xAxis, yAxis);
        chart.setTitle("Mức hài lòng theo loại cứu trợ");

        XYChart.Series<String, Number> posSeries = new XYChart.Series<>();
        posSeries.setName("Tích cực");
        XYChart.Series<String, Number> negSeries = new XYChart.Series<>();
        negSeries.setName("Tiêu cực");
        XYChart.Series<String, Number> neuSeries = new XYChart.Series<>();
        neuSeries.setName("Trung lập");

        for (Map.Entry<ReliefCategory, ReliefSentiment> entry : data.entrySet()) {
            String name = entry.getKey().getNameVi();
            ReliefSentiment rs = entry.getValue();
            posSeries.getData().add(new XYChart.Data<>(name, rs.getPositiveCount()));
            negSeries.getData().add(new XYChart.Data<>(name, rs.getNegativeCount()));
            neuSeries.getData().add(new XYChart.Data<>(name, rs.getNeutralCount()));
        }

        chart.getData().addAll(posSeries, negSeries, neuSeries);
        chart.setAnimated(true);
        return chart;
    }

    private Node createReliefTimelineChart(
            Map<ReliefCategory, Map<LocalDate, TimeSentimentData>> data) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Ngày");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Sentiment Score (Tích cực - Tiêu cực)");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Tâm lý theo loại cứu trợ theo thời gian");
        chart.setCreateSymbols(false);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

        for (Map.Entry<ReliefCategory, Map<LocalDate, TimeSentimentData>> entry : data.entrySet()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(entry.getKey().getNameVi());

            for (Map.Entry<LocalDate, TimeSentimentData> dateEntry : entry.getValue().entrySet()) {
                String dateStr = dateEntry.getKey().format(fmt);
                TimeSentimentData d = dateEntry.getValue();
                int score = d.getPositiveCount() - d.getNegativeCount();
                series.getData().add(new XYChart.Data<>(dateStr, score));
            }

            if (!series.getData().isEmpty()) {
                chart.getData().add(series);
            }
        }

        chart.setAnimated(true);
        return chart;
    }

    // ==================== INSIGHT GENERATION ====================
    private String generateTimelineInsight(Map<LocalDate, TimeSentimentData> data) {
        if (data.isEmpty()) return "Không có dữ liệu để phân tích.";

        StringBuilder sb = new StringBuilder();
        sb.append("=== NHẬN XÉT BÀI TOÁN 1: Thay đổi tâm lý theo thời gian ===\n\n");

        // Tìm ngày đỉnh tiêu cực
        LocalDate peakNegDay = null;
        int peakNeg = 0;
        int totalPos = 0, totalNeg = 0, totalNeu = 0;

        for (Map.Entry<LocalDate, TimeSentimentData> entry : data.entrySet()) {
            TimeSentimentData d = entry.getValue();
            totalPos += d.getPositiveCount();
            totalNeg += d.getNegativeCount();
            totalNeu += d.getNeutralCount();
            if (d.getNegativeCount() > peakNeg) {
                peakNeg = d.getNegativeCount();
                peakNegDay = entry.getKey();
            }
        }

        sb.append("📊 Tổng cộng: ").append(totalPos + totalNeg + totalNeu).append(" bài đăng\n");
        sb.append("   ✅ Tích cực: ").append(totalPos).append(" (")
                .append(String.format("%.1f%%", totalPos * 100.0 / (totalPos + totalNeg + totalNeu))).append(")\n");
        sb.append("   ❌ Tiêu cực: ").append(totalNeg).append(" (")
                .append(String.format("%.1f%%", totalNeg * 100.0 / (totalPos + totalNeg + totalNeu))).append(")\n");
        sb.append("   ⚪ Trung lập: ").append(totalNeu).append("\n\n");

        if (peakNegDay != null) {
            sb.append("📌 Đỉnh tiêu cực: ngày ").append(peakNegDay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .append(" (").append(peakNeg).append(" bài tiêu cực)\n");
            sb.append("   → Phản ánh thời điểm bão đổ bộ mạnh nhất, thiệt hại nghiêm trọng.\n\n");
        }

        sb.append("💡 Ý nghĩa: Phân tích cho thấy tâm lý tiêu cực chiếm ưu thế trong giai đoạn đầu thảm họa, ")
                .append("sau đó chuyển dần sang tích cực khi các hoạt động cứu trợ và phục hồi được triển khai.");

        return sb.toString();
    }

    private String generateDamageInsight(Map<DamageCategory, List<DamageReport>> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== NHẬN XÉT BÀI TOÁN 2: Phân loại thiệt hại ===\n\n");

        List<Map.Entry<DamageCategory, List<DamageReport>>> sorted = data.entrySet().stream()
                .sorted((a, b) -> b.getValue().size() - a.getValue().size())
                .toList();

        sb.append("📊 Xếp hạng loại thiệt hại (theo số lượng đề cập):\n");
        int rank = 1;
        for (Map.Entry<DamageCategory, List<DamageReport>> entry : sorted) {
            if (!entry.getValue().isEmpty()) {
                sb.append("   ").append(rank++).append(". ").append(entry.getKey().getNameVi())
                        .append(": ").append(entry.getValue().size()).append(" bài đăng\n");
            }
        }

        if (!sorted.isEmpty() && !sorted.get(0).getValue().isEmpty()) {
            sb.append("\n📌 Loại thiệt hại được đề cập nhiều nhất: ")
                    .append(sorted.get(0).getKey().getNameVi()).append("\n");
            sb.append("   → Cho thấy đây là vấn đề nghiêm trọng nhất cần ưu tiên khắc phục.\n");
        }

        sb.append("\n💡 Ý nghĩa: Phân tích giúp các cơ quan nhân đạo xác định loại thiệt hại ")
                .append("được công chúng quan tâm nhiều nhất, từ đó ưu tiên phân bổ nguồn lực.");

        return sb.toString();
    }

    private String generateReliefInsight(Map<ReliefCategory, ReliefSentiment> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== NHẬN XÉT BÀI TOÁN 3: Hài lòng theo loại cứu trợ ===\n\n");

        for (Map.Entry<ReliefCategory, ReliefSentiment> entry : data.entrySet()) {
            ReliefSentiment rs = entry.getValue();
            if (rs.getTotalCount() > 0) {
                sb.append("📦 ").append(entry.getKey().getNameVi()).append(":\n");
                sb.append("   Tích cực: ").append(rs.getPositiveCount())
                        .append(" | Tiêu cực: ").append(rs.getNegativeCount())
                        .append(" | Tỷ lệ hài lòng: ")
                        .append(String.format("%.1f%%", rs.getSatisfactionRate() * 100)).append("\n");
            }
        }

        // Tìm lĩnh vực ít hài lòng nhất
        Optional<Map.Entry<ReliefCategory, ReliefSentiment>> leastSatisfied = data.entrySet().stream()
                .filter(e -> e.getValue().getTotalCount() > 0)
                .min(Comparator.comparingDouble(e -> e.getValue().getSatisfactionRate()));

        Optional<Map.Entry<ReliefCategory, ReliefSentiment>> mostSatisfied = data.entrySet().stream()
                .filter(e -> e.getValue().getTotalCount() > 0)
                .max(Comparator.comparingDouble(e -> e.getValue().getSatisfactionRate()));

        sb.append("\n📌 Lĩnh vực cần ưu tiên cải thiện: ");
        leastSatisfied.ifPresent(e -> sb.append(e.getKey().getNameVi())
                .append(" (").append(String.format("%.1f%%", e.getValue().getSatisfactionRate() * 100)).append(" hài lòng)\n"));

        sb.append("📌 Lĩnh vực hiệu quả nhất: ");
        mostSatisfied.ifPresent(e -> sb.append(e.getKey().getNameVi())
                .append(" (").append(String.format("%.1f%%", e.getValue().getSatisfactionRate() * 100)).append(" hài lòng)\n"));

        sb.append("\n💡 Ý nghĩa: Giúp ưu tiên phân bổ nguồn lực vào lĩnh vực có nhu cầu cấp bách nhất.");

        return sb.toString();
    }

    private String generateReliefTimelineInsight(
            Map<ReliefCategory, Map<LocalDate, TimeSentimentData>> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== NHẬN XÉT BÀI TOÁN 4: Tâm lý theo cứu trợ theo thời gian ===\n\n");

        for (Map.Entry<ReliefCategory, Map<LocalDate, TimeSentimentData>> entry : data.entrySet()) {
            Map<LocalDate, TimeSentimentData> timeline = entry.getValue();
            if (timeline.isEmpty()) continue;

            int totalPos = timeline.values().stream().mapToInt(TimeSentimentData::getPositiveCount).sum();
            int totalNeg = timeline.values().stream().mapToInt(TimeSentimentData::getNegativeCount).sum();

            sb.append("📦 ").append(entry.getKey().getNameVi()).append(": ");
            if (totalPos > totalNeg) {
                sb.append("Xu hướng TÍCH CỰC (tổng +").append(totalPos).append(" / -").append(totalNeg).append(")\n");
            } else {
                sb.append("Xu hướng TIÊU CỰC (tổng +").append(totalPos).append(" / -").append(totalNeg).append(")\n");
            }
        }

        sb.append("\n💡 Ý nghĩa: Phân tích cho thấy hiệu quả logistics nhân đạo theo từng lĩnh vực qua thời gian. ")
                .append("Lĩnh vực có tâm lý tiêu cực kéo dài cho thấy cần thêm nỗ lực cải thiện.");

        return sb.toString();
    }

    // ==================== HELPER METHODS ====================
    private boolean ensureDataReady() {
        if (currentPosts.isEmpty()) {
            showAlert("Chưa có dữ liệu. Vui lòng thu thập dữ liệu trước (Tab 'Thu thập').");
            return false;
        }
        // Auto preprocess if needed
        boolean needsPreprocess = currentPosts.stream()
                .anyMatch(p -> p.getContent() != null && p.getContent().equals(p.getRawContent()));
        if (needsPreprocess) {
            preprocessingService.preprocess(currentPosts);
            log("Tự động tiền xử lý dữ liệu");
        }
        return true;
    }

    private TableView<SocialMediaPost> createPostsTable() {
        TableView<SocialMediaPost> table = new TableView<>();
        table.setPrefHeight(300);

        TableColumn<SocialMediaPost, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getId()));
        idCol.setPrefWidth(50);

        TableColumn<SocialMediaPost, String> platformCol = new TableColumn<>("Nền tảng");
        platformCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getPlatform() != null ? cd.getValue().getPlatform().getDisplayName() : ""));
        platformCol.setPrefWidth(80);

        TableColumn<SocialMediaPost, String> contentCol = new TableColumn<>("Nội dung");
        contentCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getRawContent()));
        contentCol.setPrefWidth(400);

        TableColumn<SocialMediaPost, String> dateCol = new TableColumn<>("Ngày");
        dateCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getTimestamp() != null ?
                        cd.getValue().getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""));
        dateCol.setPrefWidth(90);

        TableColumn<SocialMediaPost, String> authorCol = new TableColumn<>("Tác giả");
        authorCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getAuthor()));
        authorCol.setPrefWidth(80);

        TableColumn<SocialMediaPost, String> sentimentCol = new TableColumn<>("Sentiment");
        sentimentCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getSentiment() != null ? cd.getValue().getSentiment().getNameVi() : "—"));
        sentimentCol.setPrefWidth(80);

        table.getColumns().addAll(idCol, platformCol, contentCol, dateCol, authorCol, sentimentCol);
        return table;
    }

    private void log(String message) {
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        Platform.runLater(() -> {
            logArea.appendText("[" + timestamp + "] " + message + "\n");
        });
    }

    private void setStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 18));
        label.setStyle("-fx-text-fill: #2c3e50;");
        return label;
    }

    private String btnStyle(String color) {
        return "-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 5; -fx-cursor: hand;";
    }
}
