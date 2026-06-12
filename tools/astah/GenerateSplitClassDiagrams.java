import java.awt.geom.Point2D;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.editor.ClassDiagramEditor;
import com.change_vision.jude.api.inf.editor.ModelEditorFactory;
import com.change_vision.jude.api.inf.editor.TransactionManager;
import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IDependency;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

public class GenerateSplitClassDiagrams {
    private static final String OUT = "diagrams/class-diagrams";

    record Type(String name, String kind, String attrs, String ops, double x, double y) {}
    record Relation(String kind, String from, String to, String label) {}

    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of(OUT));
        overview();
        collection();
        preprocessing();
        sentiment();
        analysis();
        domainStorage();
        System.out.println("Created six logical class diagrams in " + OUT);
    }

    private static void overview() throws Exception {
        Type[] types = {
                t("HumanitarianApplication", "class", "", "start(stage): void", 40, 40),
                t("MainFrame", "class", "collectionService: CollectionService; preprocessingService: PreprocessingService; analysisService: AnalysisService; config: AppConfig",
                        "runAnalysis(analyzerId, posts): void", 380, 40),
                t("CollectionService", "class", "collectorFactory: DataCollectorFactory; config: AppConfig", "collectFrom(platform): List<SocialMediaPost>", 40, 300),
                t("PreprocessingService", "class", "chain: PreprocessorChain", "preprocess(posts): List<SocialMediaPost>", 380, 300),
                t("AnalysisService", "class", "registry: AnalyzerRegistry; sentimentFactory: SentimentModelFactory", "assignSentiment(posts): void; runAnalyzer(id, posts): T", 720, 300),
                t("AppConfig", "class", "instance: AppConfig", "getInstance(): AppConfig; reload(): void", 1060, 300),
                t("DataCollectorFactory", "class", "collectors: Map<String, DataCollector>", "get(key): DataCollector", 40, 570),
                t("PreprocessorChain", "class", "processors: List<TextPreprocessor>", "process(text): String", 380, 570),
                t("AnalyzerRegistry", "class", "analyzers: Map<String, Analyzer<?>>", "get(id): Analyzer<T>", 720, 570),
                t("SentimentModelFactory", "class", "providers: Map<String, SentimentModelProvider>", "getConfigured(): SentimentModelProvider", 1060, 570)
        };
        Relation[] relations = {
                d("HumanitarianApplication", "MainFrame"),
                cp("MainFrame", "CollectionService", "collectionService"), cp("MainFrame", "PreprocessingService", "preprocessingService"),
                cp("MainFrame", "AnalysisService", "analysisService"), a("MainFrame", "AppConfig", "config"),
                a("CollectionService", "DataCollectorFactory", "uses"), d("CollectionService", "AppConfig"),
                cp("PreprocessingService", "PreprocessorChain", "chain"),
                a("AnalysisService", "AnalyzerRegistry", "uses"), a("AnalysisService", "SentimentModelFactory", "uses")
        };
        create("01-application-overview.asta", "01 - Application Overview", types, relations);
    }

    private static void collection() throws Exception {
        Type[] types = {
                t("CollectionService", "class", "collectorFactory: DataCollectorFactory; config: AppConfig", "collectFrom(platform): List<SocialMediaPost>; collectFromAll(): List<SocialMediaPost>", 40, 40),
                t("DataCollectorFactory", "class", "instance: DataCollectorFactory; collectors: Map<String, DataCollector>", "register(key, collector): void; get(key): DataCollector; getAvailable(): Collection<DataCollector>", 420, 40),
                t("DataCollector", "interface", "", "collect(keywords, start, end): List<SocialMediaPost>; isAvailable(): boolean; getPlatformName(): String", 800, 40),
                t("AbstractCollector", "abstract", "platform: Platform", "collect(keywords, start, end): List<SocialMediaPost>; doCollect(...): List<SocialMediaPost>", 800, 300),
                t("CsvFileCollector", "class", "csvPath: Path", "doCollect(...): List<SocialMediaPost>", 40, 600),
                t("FacebookCollector", "class", "", "isAvailable(): boolean; doCollect(...): List<SocialMediaPost>", 300, 600),
                t("TwitterCollector", "class", "apiKey: String; apiSecret: String", "isAvailable(): boolean; doCollect(...): List<SocialMediaPost>", 560, 600),
                t("TiktokCollector", "class", "", "isAvailable(): boolean; doCollect(...): List<SocialMediaPost>", 820, 600),
                t("YoutubeCollector", "class", "", "isAvailable(): boolean; doCollect(...): List<SocialMediaPost>", 1080, 600),
                t("SocialMediaPost", "class", "id: String; platform: Platform; content: String; timestamp: LocalDateTime", "getDate(): LocalDate", 1180, 40),
                t("AppConfig", "class", "instance: AppConfig", "getKeywords(): List<String>; getDisasterStartDate(): LocalDate", 1180, 300)
        };
        Relation[] relations = {
                a("CollectionService", "DataCollectorFactory", "collectorFactory"), a("CollectionService", "AppConfig", "config"),
                ag("DataCollectorFactory", "DataCollector", "collectors"), r("AbstractCollector", "DataCollector"),
                g("CsvFileCollector", "AbstractCollector"), g("FacebookCollector", "AbstractCollector"),
                g("TwitterCollector", "AbstractCollector"), g("TiktokCollector", "AbstractCollector"), g("YoutubeCollector", "AbstractCollector"),
                d("DataCollector", "SocialMediaPost")
        };
        create("02-collection-subsystem.asta", "02 - Collection Subsystem", types, relations);
    }

    private static void preprocessing() throws Exception {
        Type[] types = {
                t("PreprocessingService", "class", "chain: PreprocessorChain", "preprocess(posts): List<SocialMediaPost>; preprocess(text): String", 40, 40),
                t("PreprocessorChain", "class", "processors: List<TextPreprocessor>; enabledNames: List<String>", "addPreprocessor(processor): void; setEnabledPreprocessors(names): void; process(text): String", 400, 40),
                t("TextPreprocessor", "interface", "", "getName(): String; getDescription(): String; process(text): String; getOrder(): int", 800, 40),
                t("HtmlCleanerPreprocessor", "class", "", "process(text): String; getOrder(): int", 40, 400),
                t("UrlRemover", "class", "", "process(text): String; getOrder(): int", 300, 400),
                t("EmojiHandler", "class", "emojiMap: Map<String, String>", "process(text): String; getOrder(): int", 560, 400),
                t("VietnameseNormalizer", "class", "", "process(text): String; getOrder(): int", 820, 400),
                t("StopWordRemover", "class", "stopWords: Set<String>", "process(text): String; getOrder(): int", 1080, 400),
                t("AppConfig", "class", "instance: AppConfig", "getEnabledPreprocessors(): List<String>", 1180, 40),
                t("SocialMediaPost", "class", "rawContent: String; content: String", "setContent(content): void", 1180, 680)
        };
        Relation[] relations = {
                cp("PreprocessingService", "PreprocessorChain", "chain"), d("PreprocessingService", "AppConfig"),
                d("PreprocessingService", "SocialMediaPost"), ag("PreprocessorChain", "TextPreprocessor", "processors"),
                r("HtmlCleanerPreprocessor", "TextPreprocessor"), r("UrlRemover", "TextPreprocessor"),
                r("EmojiHandler", "TextPreprocessor"), r("VietnameseNormalizer", "TextPreprocessor"), r("StopWordRemover", "TextPreprocessor")
        };
        create("03-preprocessing-subsystem.asta", "03 - Preprocessing Subsystem", types, relations);
    }

    private static void sentiment() throws Exception {
        Type[] types = {
                t("AnalysisService", "class", "sentimentFactory: SentimentModelFactory", "assignSentiment(posts): void", 40, 40),
                t("SentimentModelFactory", "class", "instance: SentimentModelFactory; providers: Map<String, SentimentModelProvider>", "register(name, provider): void; getConfigured(): SentimentModelProvider", 400, 40),
                t("SentimentModelProvider", "interface", "", "getModelName(): String; analyze(text): SentimentResult; analyzeBatch(texts): List<SentimentResult>; isAvailable(): boolean", 800, 40),
                t("DictionaryBasedSentimentProvider", "class", "positiveWords: Set<String>; negativeWords: Set<String>", "analyze(text): SentimentResult; isAvailable(): boolean", 400, 400),
                t("PythonApiSentimentProvider", "class", "httpClient: OkHttpClient; baseUrl: String", "analyze(text): SentimentResult; analyzeBatch(texts): List<SentimentResult>; isAvailable(): boolean", 800, 400),
                t("AppConfig", "class", "instance: AppConfig", "getSentimentProvider(): String; getPythonApiUrl(): String", 1180, 40),
                t("SentimentResult", "class", "sentiment: Sentiment; confidence: double; modelName: String", "getSentiment(): Sentiment", 40, 650),
                t("SocialMediaPost", "class", "content: String; sentiment: Sentiment; sentimentConfidence: double", "setSentiment(sentiment): void", 400, 650),
                t("Sentiment", "enum", "", "", 800, 650)
        };
        Relation[] relations = {
                a("AnalysisService", "SentimentModelFactory", "sentimentFactory"), d("AnalysisService", "SocialMediaPost"),
                ag("SentimentModelFactory", "SentimentModelProvider", "providers"), d("SentimentModelFactory", "AppConfig"),
                r("DictionaryBasedSentimentProvider", "SentimentModelProvider"), r("PythonApiSentimentProvider", "SentimentModelProvider"),
                d("PythonApiSentimentProvider", "AppConfig"), d("SentimentModelProvider", "SentimentResult"),
                a("SentimentResult", "Sentiment", "sentiment"), a("SocialMediaPost", "Sentiment", "sentiment")
        };
        create("04-sentiment-subsystem.asta", "04 - Sentiment Subsystem", types, relations);
    }

    private static void analysis() throws Exception {
        Type[] types = {
                t("AnalysisService", "class", "registry: AnalyzerRegistry", "runAnalyzer(id, posts): T; runAllAnalyzers(posts): Map<String, Object>", 40, 40),
                t("AnalyzerRegistry", "class", "instance: AnalyzerRegistry; analyzers: Map<String, Analyzer<?>>", "register(analyzer): void; get(id): Analyzer<T>; getEnabled(): Collection<Analyzer<?>>", 400, 40),
                t("Analyzer", "interface", "", "getId(): String; getName(): String; analyze(posts): T; isEnabled(): boolean", 800, 40),
                t("SentimentTimelineAnalyzer", "class", "", "analyze(posts): TimelineResult", 40, 400),
                t("DamageClassificationAnalyzer", "class", "", "analyze(posts): DamageResult", 330, 400),
                t("ReliefSatisfactionAnalyzer", "class", "", "analyze(posts): SatisfactionResult", 650, 400),
                t("ReliefSentimentTimelineAnalyzer", "class", "", "analyze(posts): ReliefTimelineResult", 970, 400),
                t("AppConfig", "class", "instance: AppConfig", "getEnabledAnalyzers(): List<String>; getDamageCategoryKeywords(): Map", 1180, 40),
                t("SocialMediaPost", "class", "content: String; sentiment: Sentiment", "getDate(): LocalDate", 40, 700),
                t("TimeSentimentData", "class", "positiveCount: int; negativeCount: int; neutralCount: int", "incrementPositive(): void; getTotal(): int", 330, 700),
                t("DamageReport", "class", "category: DamageCategory; sentiment: Sentiment; confidence: double", "getCategory(): DamageCategory", 650, 700),
                t("ReliefSentiment", "class", "category: ReliefCategory; satisfactionRate: double", "incrementPositive(): void; getTotalCount(): int", 970, 700)
        };
        Relation[] relations = {
                a("AnalysisService", "AnalyzerRegistry", "registry"), ag("AnalyzerRegistry", "Analyzer", "analyzers"),
                d("AnalyzerRegistry", "AppConfig"), d("Analyzer", "SocialMediaPost"),
                r("SentimentTimelineAnalyzer", "Analyzer"), r("DamageClassificationAnalyzer", "Analyzer"),
                r("ReliefSatisfactionAnalyzer", "Analyzer"), r("ReliefSentimentTimelineAnalyzer", "Analyzer"),
                d("SentimentTimelineAnalyzer", "TimeSentimentData"), d("DamageClassificationAnalyzer", "DamageReport"),
                d("ReliefSatisfactionAnalyzer", "ReliefSentiment"), d("ReliefSentimentTimelineAnalyzer", "TimeSentimentData")
        };
        create("05-analysis-subsystem.asta", "05 - Analysis Subsystem", types, relations);
    }

    private static void domainStorage() throws Exception {
        Type[] types = {
                t("SocialMediaPost", "class", "id: String; platform: Platform; rawContent: String; content: String; author: String; timestamp: LocalDateTime; sentiment: Sentiment; sentimentConfidence: double",
                        "getDate(): LocalDate", 40, 40),
                t("SentimentResult", "class", "sentiment: Sentiment; confidence: double; modelName: String", "getSentiment(): Sentiment", 400, 40),
                t("DamageReport", "class", "postId: String; category: DamageCategory; sentiment: Sentiment; confidence: double; excerpt: String", "getCategory(): DamageCategory", 760, 40),
                t("ReliefSentiment", "class", "category: ReliefCategory; positiveCount: int; negativeCount: int; neutralCount: int; satisfactionRate: double", "getTotalCount(): int", 1120, 40),
                t("TimeSentimentData", "class", "positiveCount: int; negativeCount: int; neutralCount: int", "getTotal(): int; getPositiveRate(): double", 40, 400),
                t("Platform", "enum", "", "", 400, 400), t("Sentiment", "enum", "", "", 650, 400),
                t("DamageCategory", "enum", "", "", 900, 400), t("ReliefCategory", "enum", "", "", 1150, 400),
                t("DataStorage", "interface", "", "savePosts(posts, filename): void; loadPosts(filename): List<SocialMediaPost>; saveResults(json, filename): void; loadResults(filename): String", 400, 700),
                t("JsonFileStorage", "class", "gson: Gson; baseDir: String", "savePosts(posts, filename): void; loadPosts(filename): List<SocialMediaPost>; exists(filename): boolean", 800, 700)
        };
        Relation[] relations = {
                a("SocialMediaPost", "Platform", "platform"), a("SocialMediaPost", "Sentiment", "sentiment"),
                a("SentimentResult", "Sentiment", "sentiment"), a("DamageReport", "DamageCategory", "category"),
                a("DamageReport", "Sentiment", "sentiment"), a("ReliefSentiment", "ReliefCategory", "category"),
                r("JsonFileStorage", "DataStorage"), d("DataStorage", "SocialMediaPost")
        };
        create("06-domain-and-storage.asta", "06 - Domain Model and Storage", types, relations);
    }

    private static void create(String filename, String diagramName, Type[] specs, Relation[] relations) throws Exception {
        Path path = Path.of(OUT, filename);
        Files.deleteIfExists(path);
        ProjectAccessor accessor = AstahAPI.getAstahAPI().getProjectAccessor();
        accessor.create(path.toString());
        TransactionManager.beginTransaction();
        try {
            BasicModelEditor editor = ModelEditorFactory.getBasicModelEditor();
            IModel project = accessor.getProject();
            IPackage pkg = editor.createPackage(project, "com.humanitarian");
            ClassDiagramEditor diagram = accessor.getDiagramEditorFactory().getClassDiagramEditor();
            diagram.createClassDiagram(pkg, diagramName);
            Map<String, IClass> classes = new LinkedHashMap<>();
            Map<String, INodePresentation> nodes = new LinkedHashMap<>();

            for (Type spec : specs) {
                IClass type = switch (spec.kind) {
                    // Astah renders native interfaces as lollipops by default. A rectangular
                    // classifier with the UML interface stereotype preserves the requested
                    // notation while still supporting realization relationships.
                    case "interface" -> {
                        IClass interfaceType = editor.createClass(pkg, spec.name);
                        // Capitalization avoids Astah's automatic lollipop rendering while
                        // keeping an explicit rectangular interface classifier.
                        interfaceType.addStereotype("Interface");
                        yield interfaceType;
                    }
                    case "enum" -> editor.createEnumeration(pkg, spec.name);
                    default -> editor.createClass(pkg, spec.name);
                };
                if ("abstract".equals(spec.kind)) type.setAbstract(true);
                classes.put(spec.name, type);
            }
            for (Type spec : specs) {
                IClass type = classes.get(spec.name);
                addMembers(editor, type, spec.attrs, spec.ops);
                INodePresentation node = diagram.createNodePresentation(type, new Point2D.Double(spec.x, spec.y));
                node.setProperty("fill.color", color(spec.name));
                nodes.put(spec.name, node);
            }

            for (Relation relation : relations) {
                IClass from = classes.get(relation.from);
                IClass to = classes.get(relation.to);
                IElement model = switch (relation.kind) {
                    case "generalization" -> editor.createGeneralization(from, to, relation.label);
                    case "realization" -> editor.createRealization(from, to, relation.label);
                    case "association", "aggregation", "composition" ->
                            association(editor, from, to, relation.label, relation.kind);
                    default -> editor.createDependency(from, to, relation.label);
                };
                diagram.createLinkPresentation(model, nodes.get(relation.from), nodes.get(relation.to));
            }
            TransactionManager.endTransaction();
            accessor.save();
        } catch (Throwable e) {
            TransactionManager.abortTransaction();
            throw e;
        } finally {
            accessor.close();
        }
    }

    private static void addMembers(BasicModelEditor editor, IClass type, String attrs, String ops) throws Exception {
        if (!attrs.isBlank()) {
            for (String item : attrs.split(";")) {
                String[] parts = item.trim().split(":", 2);
                editor.createAttribute(type, parts[0].trim(), parts.length == 2 ? parts[1].trim() : "Object");
            }
        }
        if (!ops.isBlank()) {
            for (String item : ops.split(";")) {
                String text = item.trim();
                int colon = text.lastIndexOf(':');
                String signature = colon >= 0 ? text.substring(0, colon).trim() : text;
                String result = colon >= 0 ? text.substring(colon + 1).trim() : "void";
                int paren = signature.indexOf('(');
                editor.createOperation(type, paren >= 0 ? signature.substring(0, paren) : signature, result);
            }
        }
    }

    private static Type t(String n, String k, String a, String o, double x, double y) { return new Type(n, k, a, o, x, y); }
    private static Relation a(String f, String t, String l) { return new Relation("association", f, t, l); }
    private static Relation ag(String f, String t, String l) { return new Relation("aggregation", f, t, l); }
    private static Relation cp(String f, String t, String l) { return new Relation("composition", f, t, l); }
    private static Relation d(String f, String t) { return new Relation("dependency", f, t, "uses"); }
    private static Relation g(String f, String t) { return new Relation("generalization", f, t, ""); }
    private static Relation r(String f, String t) { return new Relation("realization", f, t, ""); }

    private static IAssociation association(BasicModelEditor editor, IClass whole, IClass part,
                                            String label, String kind) throws Exception {
        IAssociation association = editor.createAssociation(whole, part, label, "", "");
        for (IAttribute end : association.getMemberEnds()) {
            if (end.getType().equals(whole)) {
                end.setMultiplicityString("1");
                if (!"association".equals(kind)) {
                    if ("composition".equals(kind)) end.setComposite();
                    else end.setAggregation();
                }
            } else if ("aggregation".equals(kind)) {
                end.setMultiplicityString("0..*");
            } else {
                end.setMultiplicityString("1");
            }
        }
        return association;
    }

    private static String color(String name) {
        if (name.endsWith("Service")) return "#D5F5E3";
        if (name.equals("AppConfig") || name.contains("Storage")) return "#E8DAEF";
        if (name.equals("MainFrame") || name.equals("HumanitarianApplication")) return "#D6EAF8";
        if (name.equals("SocialMediaPost") || name.endsWith("Result") || name.endsWith("Report")
                || name.endsWith("Data") || name.equals("ReliefSentiment") || name.equals("Platform")
                || name.equals("Sentiment") || name.endsWith("Category")) return "#FCF3CF";
        return "#FDEBD0";
    }
}
