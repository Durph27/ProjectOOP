package com.humanitarian.ui;

import com.humanitarian.config.AppConfig;
import com.humanitarian.model.*;
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
                createTab("Thu thập", createCollectionPanel()),
                createTab("Tiền xử lý", createPreprocessingPanel()),
                createTab("BT1: Tâm lý theo thời gian", createProblem1Panel()),
                createTab("BT2: Phân loại thiệt hại", createProblem2Panel()),
                createTab("BT3: Hài lòng cứu trợ", createProblem3Panel()),
                createTab("BT4: Cứu trợ theo thời gian", createProblem4Panel())
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
        return createAnalysisPanel(
                "Bài toán 1: Theo dõi tâm lý công chúng theo thời gian",
                "Phân tích số lượng bài đăng tích cực, tiêu cực và trung lập theo từng ngày.",
                "sentiment_timeline", "#3498db");
    }

    // ==================== BÀI TOÁN 2 ====================
    private Node createProblem2Panel() {
        return createAnalysisPanel(
                "Bài toán 2: Phân loại mức độ và loại thiệt hại",
                "Phân loại các bài đăng theo nhóm thiệt hại và thống kê mức độ được đề cập.",
                "damage_classification", "#e67e22");
    }

    // ==================== BÀI TOÁN 3 ====================
    private Node createProblem3Panel() {
        return createAnalysisPanel(
                "Bài toán 3: Mức hài lòng theo loại cứu trợ",
                "Đánh giá phản hồi tích cực, tiêu cực và trung lập theo từng loại cứu trợ.",
                "relief_satisfaction", "#27ae60");
    }

    // ==================== BÀI TOÁN 4 ====================
    private Node createProblem4Panel() {
        return createAnalysisPanel(
                "Bài toán 4: Tâm lý theo loại cứu trợ theo thời gian",
                "Theo dõi sự thay đổi của phản hồi đối với từng loại cứu trợ theo thời gian.",
                "relief_sentiment_timeline", "#8e44ad");
    }

    private Node createAnalysisPanel(String title, String description, String analyzerId, String buttonColor) {
        Label titleLabel = sectionTitle(title);
        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4b5563;");

        StackPane chartContainer = new StackPane();
        chartContainer.setMinHeight(480);
        chartContainer.setPrefHeight(560);
        chartContainer.setStyle("-fx-background-color: white; -fx-border-color: #d8dee9; " +
                "-fx-border-radius: 6; -fx-background-radius: 6;");
        HBox.setHgrow(chartContainer, Priority.ALWAYS);

        Label insightTitle = new Label("Nhận xét phân tích");
        insightTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #243447;");

        TextArea insightArea = new TextArea();
        insightArea.setPromptText("Nhận xét sẽ được hiển thị sau khi phân tích.");
        insightArea.setEditable(false);
        insightArea.setWrapText(true);
        insightArea.setPrefWidth(430);
        insightArea.setMinWidth(340);
        insightArea.setMaxWidth(500);
        insightArea.setPrefHeight(520);
        insightArea.setStyle("-fx-font-size: 14px; -fx-control-inner-background: white;");
        VBox.setVgrow(insightArea, Priority.ALWAYS);

        VBox insightBox = new VBox(10, insightTitle, insightArea);
        insightBox.setPadding(new Insets(16));
        insightBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #d8dee9; " +
                "-fx-border-radius: 6; -fx-background-radius: 6;");

        HBox resultBox = new HBox(20, chartContainer, insightBox);
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setFillHeight(true);
        VBox.setVgrow(resultBox, Priority.ALWAYS);

        Button analyzeBtn = new Button("Phân tích");
        analyzeBtn.setStyle(btnStyle(buttonColor));
        analyzeBtn.setPrefWidth(200);
        analyzeBtn.setOnAction(e -> {
            if (!ensureDataReady()) return;
            runAnalysis(analyzerId, chartContainer, insightArea);
        });

        VBox content = new VBox(16, titleLabel, descLabel, analyzeBtn, new Separator(), resultBox);
        content.setPadding(new Insets(24));
        content.setMaxWidth(1500);
        content.setFillWidth(true);

        StackPane centered = new StackPane(content);
        centered.setAlignment(Pos.TOP_CENTER);
        centered.setPadding(new Insets(0, 20, 20, 20));

        ScrollPane scrollPane = new ScrollPane(centered);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
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
                        Map<CategoryDefinition, List<DamageReport>> data =
                                (Map<CategoryDefinition, List<DamageReport>>) result;
                        chartContainer.getChildren().add(createDamageChart(data));
                        insightArea.setText(generateDamageInsight(data));
                    }
                    case "relief_satisfaction" -> {
                        Map<CategoryDefinition, ReliefSentiment> data =
                                (Map<CategoryDefinition, ReliefSentiment>) result;
                        chartContainer.getChildren().add(createReliefSatisfactionChart(data));
                        insightArea.setText(generateReliefInsight(data));
                    }
                    case "relief_sentiment_timeline" -> {
                        Map<CategoryDefinition, Map<LocalDate, TimeSentimentData>> data =
                                (Map<CategoryDefinition, Map<LocalDate, TimeSentimentData>>) result;
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

    private Node createDamageChart(Map<CategoryDefinition, List<DamageReport>> data) {
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

    private Node createReliefSatisfactionChart(Map<CategoryDefinition, ReliefSentiment> data) {
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

        for (Map.Entry<CategoryDefinition, ReliefSentiment> entry : data.entrySet()) {
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
            Map<CategoryDefinition, Map<LocalDate, TimeSentimentData>> data) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Ngày");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Sentiment Score (Tích cực - Tiêu cực)");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Tâm lý theo loại cứu trợ theo thời gian");
        chart.setCreateSymbols(false);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

        for (Map.Entry<CategoryDefinition, Map<LocalDate, TimeSentimentData>> entry : data.entrySet()) {
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
        if (data.isEmpty()) return noDataInsight();

        int totalPos = data.values().stream().mapToInt(TimeSentimentData::getPositiveCount).sum();
        int totalNeg = data.values().stream().mapToInt(TimeSentimentData::getNegativeCount).sum();
        int totalNeu = data.values().stream().mapToInt(TimeSentimentData::getNeutralCount).sum();
        int total = totalPos + totalNeg + totalNeu;
        if (total == 0) return noDataInsight();

        Map.Entry<LocalDate, TimeSentimentData> peakNegative = data.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().getNegativeCount()))
                .orElse(null);
        Map.Entry<LocalDate, TimeSentimentData> first = data.entrySet().iterator().next();
        Map.Entry<LocalDate, TimeSentimentData> last = data.entrySet().stream().reduce((a, b) -> b).orElse(first);
        int firstScore = sentimentScore(first.getValue());
        int lastScore = sentimentScore(last.getValue());

        StringBuilder sb = new StringBuilder("NHẬN XÉT TỔNG HỢP\n\n");
        sb.append("Phạm vi phân tích gồm ").append(total).append(" bài đăng trong ")
                .append(data.size()).append(" ngày có dữ liệu.\n\n");
        sb.append("Cơ cấu cảm xúc:\n")
                .append("- Tích cực: ").append(totalPos).append(" bài (").append(percent(totalPos, total)).append(").\n")
                .append("- Tiêu cực: ").append(totalNeg).append(" bài (").append(percent(totalNeg, total)).append(").\n")
                .append("- Trung lập: ").append(totalNeu).append(" bài (").append(percent(totalNeu, total)).append(").\n\n");
        sb.append("Nhìn chung, ").append(dominantSentiment(totalPos, totalNeg, totalNeu)).append(". ");
        sb.append("So sánh ngày đầu và ngày cuối có dữ liệu, xu hướng cảm xúc ")
                .append(scoreTrend(firstScore, lastScore)).append(".\n");

        if (peakNegative != null && peakNegative.getValue().getNegativeCount() > 0) {
            sb.append("\nSố bài tiêu cực cao nhất được ghi nhận vào ngày ")
                    .append(peakNegative.getKey().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .append(", với ").append(peakNegative.getValue().getNegativeCount()).append(" bài. ")
                    .append("Mốc này cần được đối chiếu với diễn biến thực tế trước khi đưa ra kết luận nguyên nhân.");
        }
        return sb.toString();
    }

    private String generateDamageInsight(Map<CategoryDefinition, List<DamageReport>> data) {
        List<Map.Entry<CategoryDefinition, List<DamageReport>>> sorted = data.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .sorted((a, b) -> b.getValue().size() - a.getValue().size())
                .toList();
        int total = sorted.stream().mapToInt(e -> e.getValue().size()).sum();
        if (total == 0) return noDataInsight();

        StringBuilder sb = new StringBuilder("NHẬN XÉT TỔNG HỢP\n\n");
        sb.append("Hệ thống ghi nhận ").append(total)
                .append(" lượt phân loại thuộc ").append(sorted.size()).append(" nhóm thiệt hại.\n\n");
        sb.append("Các nhóm được đề cập nhiều nhất:\n");
        int rank = 1;
        for (Map.Entry<CategoryDefinition, List<DamageReport>> entry : sorted) {
            sb.append(rank++).append(". ").append(entry.getKey().getNameVi())
                    .append(": ").append(entry.getValue().size()).append(" lượt (")
                    .append(percent(entry.getValue().size(), total)).append(").\n");
        }

        Map.Entry<CategoryDefinition, List<DamageReport>> leading = sorted.get(0);
        sb.append("\nNhóm ").append(leading.getKey().getNameVi())
                .append(" có tần suất đề cập cao nhất trong tập dữ liệu. ")
                .append("Kết quả phản ánh mức độ xuất hiện trong bài đăng, không trực tiếp đại diện cho mức độ thiệt hại thực tế. ")
                .append("Cần kết hợp với dữ liệu hiện trường trước khi xác định thứ tự ưu tiên.");
        return sb.toString();
    }

    private String generateReliefInsight(Map<CategoryDefinition, ReliefSentiment> data) {
        List<Map.Entry<CategoryDefinition, ReliefSentiment>> available = data.entrySet().stream()
                .filter(e -> e.getValue().getTotalCount() > 0)
                .sorted(Comparator.comparingDouble((Map.Entry<CategoryDefinition, ReliefSentiment> e) ->
                        e.getValue().getSatisfactionRate()).reversed())
                .toList();
        if (available.isEmpty()) return noDataInsight();

        StringBuilder sb = new StringBuilder("NHẬN XÉT TỔNG HỢP\n\n");
        sb.append("Có ").append(available.size()).append(" nhóm cứu trợ đủ dữ liệu để đánh giá.\n\n");
        for (Map.Entry<CategoryDefinition, ReliefSentiment> entry : available) {
            ReliefSentiment rs = entry.getValue();
            sb.append(entry.getKey().getNameVi()).append(": ")
                    .append(rs.getPositiveCount()).append(" tích cực, ")
                    .append(rs.getNegativeCount()).append(" tiêu cực, ")
                    .append(rs.getNeutralCount()).append(" trung lập; tỷ lệ phản hồi tích cực ")
                    .append(String.format("%.1f%%", rs.getSatisfactionRate() * 100)).append(".\n");
        }

        Map.Entry<CategoryDefinition, ReliefSentiment> highest = available.get(0);
        Map.Entry<CategoryDefinition, ReliefSentiment> lowest = available.get(available.size() - 1);
        sb.append("\nTrong phạm vi dữ liệu hiện có, ").append(highest.getKey().getNameVi())
                .append(" có tỷ lệ phản hồi tích cực cao nhất, trong khi ")
                .append(lowest.getKey().getNameVi()).append(" có tỷ lệ thấp nhất. ")
                .append("Kết quả nên được xem là tín hiệu phản hồi để khảo sát thêm, không phải kết luận trực tiếp về hiệu quả cứu trợ.");
        return sb.toString();
    }

    private String generateReliefTimelineInsight(
            Map<CategoryDefinition, Map<LocalDate, TimeSentimentData>> data) {
        List<Map.Entry<CategoryDefinition, Map<LocalDate, TimeSentimentData>>> available = data.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .toList();
        if (available.isEmpty()) return noDataInsight();

        StringBuilder sb = new StringBuilder("NHẬN XÉT TỔNG HỢP\n\n");
        sb.append("Xu hướng phản hồi theo từng nhóm cứu trợ:\n");
        for (Map.Entry<CategoryDefinition, Map<LocalDate, TimeSentimentData>> entry : available) {
            Map<LocalDate, TimeSentimentData> timeline = entry.getValue();
            int totalPos = timeline.values().stream().mapToInt(TimeSentimentData::getPositiveCount).sum();
            int totalNeg = timeline.values().stream().mapToInt(TimeSentimentData::getNegativeCount).sum();
            TimeSentimentData first = timeline.entrySet().iterator().next().getValue();
            TimeSentimentData last = timeline.entrySet().stream().reduce((a, b) -> b).orElseThrow().getValue();
            sb.append("- ").append(entry.getKey().getNameVi()).append(": ")
                    .append(totalPos).append(" phản hồi tích cực và ").append(totalNeg)
                    .append(" phản hồi tiêu cực; xu hướng ")
                    .append(scoreTrend(sentimentScore(first), sentimentScore(last))).append(".\n");
        }

        sb.append("\nCác nhóm có xu hướng giảm hoặc duy trì nhiều phản hồi tiêu cực cần được kiểm tra thêm bằng dữ liệu vận hành và phản hồi trực tiếp. ")
                .append("Biến động cảm xúc trên mạng xã hội chỉ phản ánh quan điểm trong tập bài đăng đã thu thập.");
        return sb.toString();
    }

    private String noDataInsight() {
        return "Chưa có đủ dữ liệu phù hợp để đưa ra nhận xét. Vui lòng kiểm tra nguồn dữ liệu, khoảng thời gian và kết quả phân loại.";
    }

    private String percent(int value, int total) {
        return total == 0 ? "0,0%" : String.format("%.1f%%", value * 100.0 / total);
    }

    private int sentimentScore(TimeSentimentData data) {
        return data.getPositiveCount() - data.getNegativeCount();
    }

    private String scoreTrend(int firstScore, int lastScore) {
        int difference = lastScore - firstScore;
        if (difference > 0) return "cải thiện";
        if (difference < 0) return "giảm";
        return "ổn định";
    }

    private String dominantSentiment(int positive, int negative, int neutral) {
        int max = Math.max(positive, Math.max(negative, neutral));
        int ties = (positive == max ? 1 : 0) + (negative == max ? 1 : 0) + (neutral == max ? 1 : 0);
        if (ties > 1) return "các nhóm cảm xúc có tỷ trọng tương đối cân bằng";
        if (max == positive) return "phản hồi tích cực chiếm tỷ trọng cao nhất";
        if (max == negative) return "phản hồi tiêu cực chiếm tỷ trọng cao nhất";
        return "phản hồi trung lập chiếm tỷ trọng cao nhất";
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
                cd.getValue().getPlatform() != null
                        ? config.getPlatformDisplayName(cd.getValue().getPlatform()) : ""));
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
                cd.getValue().getSentiment() != null
                        ? config.getSentimentNameVi(cd.getValue().getSentiment()) : "—"));
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
