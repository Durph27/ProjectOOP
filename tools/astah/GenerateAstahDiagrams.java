import java.awt.geom.Point2D;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.editor.ClassDiagramEditor;
import com.change_vision.jude.api.inf.editor.ModelEditorFactory;
import com.change_vision.jude.api.inf.editor.TransactionManager;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IDependency;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IGeneralization;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IOperation;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IRealization;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

public class GenerateAstahDiagrams {
    private static final String OUT_DIR = "diagrams";
    private static BasicModelEditor modelEditor;
    private static final List<IAssociation> associations = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of(OUT_DIR));
        String outputName = args.length > 0 ? args[0] : "package-diagram.asta";
        createPackageDiagram(outputName);
        System.out.println("Created diagrams/" + outputName);
    }

    private static void createPackageDiagram(String outputName) throws Exception {
        String path = OUT_DIR + "/" + outputName;
        Files.deleteIfExists(Path.of(path));
        ProjectAccessor accessor = AstahAPI.getAstahAPI().getProjectAccessor();
        accessor.create(path);
        IModel project = accessor.getProject();
        TransactionManager.beginTransaction();
        try {
            modelEditor = ModelEditorFactory.getBasicModelEditor();
            ClassDiagramEditor diagram = accessor.getDiagramEditorFactory().getClassDiagramEditor();
            diagram.createClassDiagram(project, "Package Diagram - Humanitarian Analytics");

            Map<String, IPackage> packages = new LinkedHashMap<>();
            String[] names = {"ui", "service", "collector", "preprocessor", "sentiment",
                    "analyzer", "config", "model"};
            for (String name : names) {
                IPackage pkg = modelEditor.createPackage(project, "com.humanitarian." + name);
                pkg.setDefinition(packageDefinition(name));
                packages.put(name, pkg);
            }

            double[][] positions = {
                    {650, 30}, {650, 220}, {50, 430}, {300, 430}, {550, 430},
                    {800, 430}, {1050, 430}, {650, 650}
            };
            Map<String, INodePresentation> nodes = new LinkedHashMap<>();
            for (int i = 0; i < names.length; i++) {
                INodePresentation node = diagram.createNodePresentation(packages.get(names[i]),
                        new Point2D.Double(positions[i][0], positions[i][1]));
                node.setProperty("fill.color", packageColor(names[i]));
                nodes.put(names[i], node);
            }

            dependency(diagram, packages, nodes, "ui", "service");
            dependency(diagram, packages, nodes, "ui", "config");
            dependency(diagram, packages, nodes, "ui", "model");
            dependency(diagram, packages, nodes, "service", "collector");
            dependency(diagram, packages, nodes, "service", "preprocessor");
            dependency(diagram, packages, nodes, "service", "sentiment");
            dependency(diagram, packages, nodes, "service", "analyzer");
            dependency(diagram, packages, nodes, "service", "config");
            dependency(diagram, packages, nodes, "service", "model");
            dependency(diagram, packages, nodes, "collector", "model");
            dependency(diagram, packages, nodes, "collector", "config");
            dependency(diagram, packages, nodes, "sentiment", "config");
            dependency(diagram, packages, nodes, "sentiment", "model");
            dependency(diagram, packages, nodes, "analyzer", "config");
            dependency(diagram, packages, nodes, "analyzer", "model");
            dependency(diagram, packages, nodes, "config", "model");

            TransactionManager.endTransaction();
            accessor.save();
        } catch (Throwable e) {
            TransactionManager.abortTransaction();
            throw e;
        } finally {
            accessor.close();
        }
    }

    private static void createClassDiagram() throws Exception {
        String path = OUT_DIR + "/class-diagram-detailed.asta";
        Files.deleteIfExists(Path.of(path));
        ProjectAccessor accessor = AstahAPI.getAstahAPI().getProjectAccessor();
        accessor.create(path);
        associations.clear();
        IModel project = accessor.getProject();
        TransactionManager.beginTransaction();
        try {
            modelEditor = ModelEditorFactory.getBasicModelEditor();
            ClassDiagramEditor diagram = accessor.getDiagramEditorFactory().getClassDiagramEditor();
            diagram.createClassDiagram(project, "Detailed Class Diagram - Humanitarian Analytics");

            Map<String, IPackage> pkgs = createClassPackages(project);
            Map<String, IClass> c = new LinkedHashMap<>();

            cls(c, pkgs, "app", "Main", null, "main(args): void");
            cls(c, pkgs, "app", "Launcher", null, "main(args): void");
            cls(c, pkgs, "app", "HumanitarianApplication", null, "start(primaryStage): void");
            cls(c, pkgs, "config", "AppConfig",
                    "instance: AppConfig; appConfig: JsonObject; keywordsConfig: JsonObject",
                    "getInstance(): AppConfig; reload(): void; getKeywords(): List<String>; getEnabledAnalyzers(): List<String>");

            iface(c, pkgs, "collector", "DataCollector",
                    "collect(keywords, startDate, endDate): List<SocialMediaPost>; isAvailable(): boolean; getPlatformName(): String");
            abs(c, pkgs, "collector", "AbstractCollector", "platform: String",
                    "collect(keywords, startDate, endDate): List<SocialMediaPost>; doCollect(...): List<SocialMediaPost>");
            cls(c, pkgs, "collector", "CsvFileCollector", "csvPath: Path", "doCollect(...): List<SocialMediaPost>");
            for (String name : new String[]{"FacebookCollector", "TwitterCollector", "TiktokCollector", "YoutubeCollector"}) {
                cls(c, pkgs, "collector", name, null, "isAvailable(): boolean; doCollect(...): List<SocialMediaPost>");
            }
            cls(c, pkgs, "collector", "DataCollectorFactory", "instance: DataCollectorFactory; collectors: Map<String, DataCollector>",
                    "getInstance(): DataCollectorFactory; register(key, collector): void; get(key): DataCollector; getAvailable(): Collection<DataCollector>");

            iface(c, pkgs, "preprocessor", "TextPreprocessor",
                    "getName(): String; getDescription(): String; process(text): String; getOrder(): int");
            cls(c, pkgs, "preprocessor", "PreprocessorChain", "processors: List<TextPreprocessor>; enabledNames: List<String>",
                    "addPreprocessor(processor): void; setEnabledPreprocessors(names): void; process(text): String");
            for (String name : new String[]{"HtmlCleanerPreprocessor", "UrlRemover", "EmojiHandler", "VietnameseNormalizer", "StopWordRemover"}) {
                cls(c, pkgs, "preprocessor", name, null, "getName(): String; process(text): String; getOrder(): int");
            }

            iface(c, pkgs, "sentiment", "SentimentModelProvider",
                    "getModelName(): String; analyze(text): SentimentResult; analyzeBatch(texts): List<SentimentResult>; isAvailable(): boolean");
            cls(c, pkgs, "sentiment", "DictionaryBasedSentimentProvider", "positiveWords: Set<String>; negativeWords: Set<String>",
                    "analyze(text): SentimentResult; isAvailable(): boolean");
            cls(c, pkgs, "sentiment", "PythonApiSentimentProvider", "httpClient: OkHttpClient; baseUrl: String",
                    "analyze(text): SentimentResult; analyzeBatch(texts): List<SentimentResult>; isAvailable(): boolean");
            cls(c, pkgs, "sentiment", "SentimentModelFactory", "instance: SentimentModelFactory; providers: Map<String, SentimentModelProvider>",
                    "getInstance(): SentimentModelFactory; register(name, provider): void; getConfigured(): SentimentModelProvider");

            iface(c, pkgs, "analyzer", "Analyzer",
                    "getId(): String; getName(): String; getDescription(): String; analyze(posts): T; isEnabled(): boolean");
            for (String name : new String[]{"SentimentTimelineAnalyzer", "DamageClassificationAnalyzer",
                    "ReliefSatisfactionAnalyzer", "ReliefSentimentTimelineAnalyzer"}) {
                cls(c, pkgs, "analyzer", name, null, "getId(): String; analyze(posts): Result");
            }
            cls(c, pkgs, "analyzer", "AnalyzerRegistry", "instance: AnalyzerRegistry; analyzers: Map<String, Analyzer<?>>",
                    "getInstance(): AnalyzerRegistry; register(analyzer): void; get(id): Analyzer<T>; getEnabled(): Collection<Analyzer<?>>");

            cls(c, pkgs, "service", "CollectionService", "collectorFactory: DataCollectorFactory; config: AppConfig",
                    "collectFrom(platformKey): List<SocialMediaPost>; collectFromAll(): List<SocialMediaPost>");
            cls(c, pkgs, "service", "PreprocessingService", "chain: PreprocessorChain",
                    "preprocess(posts): List<SocialMediaPost>; preprocess(text): String");
            cls(c, pkgs, "service", "AnalysisService", "registry: AnalyzerRegistry; sentimentFactory: SentimentModelFactory",
                    "assignSentiment(posts): void; runAnalyzer(id, posts): T; runAllAnalyzers(posts): Map<String, Object>");

            cls(c, pkgs, "ui", "MainFrame", "collectionService: CollectionService; preprocessingService: PreprocessingService; analysisService: AnalysisService; config: AppConfig",
                    "createDashboardPanel(): Node; runAnalysis(analyzerId, chartContainer, insightArea): void");

            cls(c, pkgs, "model", "SocialMediaPost",
                    "id: String; platform: String; rawContent: String; content: String; author: String; timestamp: LocalDateTime; sentiment: String; sentimentConfidence: double",
                    "getDate(): LocalDate; getContent(): String; setSentiment(sentiment): void");
            cls(c, pkgs, "model", "CategoryDefinition", "id: String; nameVi: String; nameEn: String; keywords: List<String>", "getId(): String; getKeywords(): List<String>");
            cls(c, pkgs, "model", "SentimentResult", "sentiment: String; confidence: double; modelName: String", "getSentiment(): String");
            cls(c, pkgs, "model", "DamageReport", "postId: String; category: CategoryDefinition; sentiment: String; confidence: double; excerpt: String", "getCategory(): CategoryDefinition");
            cls(c, pkgs, "model", "ReliefSentiment", "category: CategoryDefinition; positiveCount: int; negativeCount: int; neutralCount: int; satisfactionRate: double", "incrementPositive(): void; getTotalCount(): int");
            cls(c, pkgs, "model", "TimeSentimentData", "positiveCount: int; negativeCount: int; neutralCount: int", "incrementPositive(): void; getTotal(): int");

            relations(modelEditor, c);
            presentations(diagram, c);

            TransactionManager.endTransaction();
            accessor.save();
        } catch (Throwable e) {
            TransactionManager.abortTransaction();
            throw e;
        } finally {
            accessor.close();
        }
    }

    private static Map<String, IPackage> createClassPackages(IModel project) throws Exception {
        Map<String, IPackage> result = new LinkedHashMap<>();
        for (String name : new String[]{"app", "ui", "service", "collector", "preprocessor", "sentiment", "analyzer", "config", "model"}) {
            result.put(name, modelEditor.createPackage(project, "com.humanitarian." + name));
        }
        return result;
    }

    private static void cls(Map<String, IClass> c, Map<String, IPackage> p, String pkg, String name,
                            String attrs, String ops) throws Exception {
        IClass type = modelEditor.createClass(p.get(pkg), name);
        addDetails(type, attrs, ops);
        c.put(name, type);
    }

    private static void iface(Map<String, IClass> c, Map<String, IPackage> p, String pkg, String name, String ops) throws Exception {
        IClass type = modelEditor.createInterface(p.get(pkg), name);
        addDetails(type, null, ops);
        c.put(name, type);
    }

    private static void abs(Map<String, IClass> c, Map<String, IPackage> p, String pkg, String name,
                            String attrs, String ops) throws Exception {
        cls(c, p, pkg, name, attrs, ops);
        c.get(name).setAbstract(true);
    }

    private static void enumeration(Map<String, IClass> c, Map<String, IPackage> p, String pkg, String name) throws Exception {
        IClass type = modelEditor.createEnumeration(p.get(pkg), name);
        c.put(name, type);
    }

    private static void addDetails(IClass type, String attrs, String ops) throws Exception {
        if (attrs != null) {
            for (String item : attrs.split(";")) {
                String[] parts = item.trim().split(":", 2);
                modelEditor.createAttribute(type, parts[0].trim(), parts.length > 1 ? parts[1].trim() : "Object");
            }
        }
        if (ops != null) {
            for (String item : ops.split(";")) {
                String text = item.trim();
                int colon = text.lastIndexOf(':');
                String signature = colon >= 0 ? text.substring(0, colon).trim() : text;
                String returnType = colon >= 0 ? text.substring(colon + 1).trim() : "void";
                int paren = signature.indexOf('(');
                String name = paren >= 0 ? signature.substring(0, paren) : signature;
                IOperation op = modelEditor.createOperation(type, name, returnType);
                if (paren >= 0) op.setDefinition(signature);
            }
        }
    }

    private static void relations(BasicModelEditor e, Map<String, IClass> c) throws Exception {
        realize(e, c, "AbstractCollector", "DataCollector");
        for (String name : new String[]{"CsvFileCollector", "FacebookCollector", "TwitterCollector", "TiktokCollector", "YoutubeCollector"}) {
            generalize(e, c, name, "AbstractCollector");
        }
        for (String name : new String[]{"HtmlCleanerPreprocessor", "UrlRemover", "EmojiHandler", "VietnameseNormalizer", "StopWordRemover"}) {
            realize(e, c, name, "TextPreprocessor");
        }
        realize(e, c, "DictionaryBasedSentimentProvider", "SentimentModelProvider");
        realize(e, c, "PythonApiSentimentProvider", "SentimentModelProvider");
        for (String name : new String[]{"SentimentTimelineAnalyzer", "DamageClassificationAnalyzer", "ReliefSatisfactionAnalyzer", "ReliefSentimentTimelineAnalyzer"}) {
            realize(e, c, name, "Analyzer");
        }
        assoc(e, c, "MainFrame", "CollectionService", "uses");
        assoc(e, c, "MainFrame", "PreprocessingService", "uses");
        assoc(e, c, "MainFrame", "AnalysisService", "uses");
        assoc(e, c, "CollectionService", "DataCollectorFactory", "uses");
        assoc(e, c, "PreprocessingService", "PreprocessorChain", "owns");
        assoc(e, c, "PreprocessorChain", "TextPreprocessor", "processors");
        assoc(e, c, "AnalysisService", "AnalyzerRegistry", "uses");
        assoc(e, c, "AnalysisService", "SentimentModelFactory", "uses");
        assoc(e, c, "AnalyzerRegistry", "Analyzer", "registered");
        assoc(e, c, "SentimentModelFactory", "SentimentModelProvider", "providers");
        assoc(e, c, "DataCollectorFactory", "DataCollector", "collectors");
        assoc(e, c, "DamageReport", "CategoryDefinition", "category");
        assoc(e, c, "ReliefSentiment", "CategoryDefinition", "category");

        dep(e, c, "CollectionService", "AppConfig");
        dep(e, c, "PythonApiSentimentProvider", "AppConfig");
        dep(e, c, "SentimentModelFactory", "AppConfig");
        dep(e, c, "AnalyzerRegistry", "AppConfig");
        dep(e, c, "DamageClassificationAnalyzer", "AppConfig");
        dep(e, c, "ReliefSatisfactionAnalyzer", "AppConfig");
        dep(e, c, "ReliefSentimentTimelineAnalyzer", "AppConfig");
        dep(e, c, "DataCollector", "SocialMediaPost");
        dep(e, c, "Analyzer", "SocialMediaPost");
    }

    private static void presentations(ClassDiagramEditor diagram, Map<String, IClass> c) throws Exception {
        Map<String, INodePresentation> nodes = new LinkedHashMap<>();
        int index = 0;
        for (Map.Entry<String, IClass> entry : c.entrySet()) {
            int col = index % 6;
            int row = index / 6;
            INodePresentation node = diagram.createNodePresentation(entry.getValue(),
                    new Point2D.Double(30 + col * 300, 30 + row * 270));
            node.setProperty("fill.color", classColor(entry.getValue()));
            nodes.put(entry.getKey(), node);
            index++;
        }
        for (IClass type : c.values()) {
            for (IGeneralization relation : type.getGeneralizations()) {
                diagram.createLinkPresentation(relation, nodes.get(relation.getSubType().getName()), nodes.get(relation.getSuperType().getName()));
            }
            for (IRealization relation : type.getClientRealizations()) {
                diagram.createLinkPresentation(relation, nodes.get(relation.getClient().getName()), nodes.get(relation.getSupplier().getName()));
            }
            for (IDependency relation : type.getClientDependencies()) {
                String client = relation.getClient().getName();
                String supplier = relation.getSupplier().getName();
                if (nodes.containsKey(client) && nodes.containsKey(supplier)) {
                    diagram.createLinkPresentation(relation, nodes.get(client), nodes.get(supplier));
                }
            }
        }
        for (IAssociation association : associations) {
            String a = association.getMemberEnds()[0].getType().getName();
            String b = association.getMemberEnds()[1].getType().getName();
            diagram.createLinkPresentation(association, nodes.get(a), nodes.get(b));
        }
    }

    private static void dependency(ClassDiagramEditor d, Map<String, IPackage> p, Map<String, INodePresentation> n,
                                   String from, String to) throws Exception {
        IDependency dep = modelEditor.createDependency(p.get(from), p.get(to), "\u00abuse\u00bb");
        d.createLinkPresentation(dep, n.get(from), n.get(to));
    }

    private static void generalize(BasicModelEditor e, Map<String, IClass> c, String sub, String sup) throws Exception {
        e.createGeneralization(c.get(sub), c.get(sup), "");
    }
    private static void realize(BasicModelEditor e, Map<String, IClass> c, String impl, String iface) throws Exception {
        e.createRealization(c.get(impl), c.get(iface), "");
    }
    private static void assoc(BasicModelEditor e, Map<String, IClass> c, String a, String b, String name) throws Exception {
        associations.add(e.createAssociation(c.get(a), c.get(b), name, "", ""));
    }
    private static void dep(BasicModelEditor e, Map<String, IClass> c, String from, String to) throws Exception {
        e.createDependency(c.get(from), c.get(to), "uses");
    }

    private static String packageDefinition(String name) {
        return switch (name) {
            case "ui" -> "JavaFX user interface";
            case "service" -> "Application orchestration services";
            case "collector" -> "Social media and CSV data collection";
            case "preprocessor" -> "Configurable text preprocessing pipeline";
            case "sentiment" -> "Sentiment provider strategies and factory";
            case "analyzer" -> "Business analysis strategies and registry";
            case "config" -> "Singleton JSON configuration";
            case "model" -> "Domain entities and analysis results";
            default -> "Domain entities and analysis results";
        };
    }

    private static String packageColor(String name) {
        return switch (name) {
            case "ui" -> "#D6EAF8";
            case "service" -> "#D5F5E3";
            case "model" -> "#FCF3CF";
            case "config" -> "#E8DAEF";
            default -> "#FDEBD0";
        };
    }

    private static String classColor(IClass type) {
        String namespace = type.getFullNamespace(".");
        if (namespace.contains("service")) return "#D5F5E3";
        if (namespace.contains("model")) return "#FCF3CF";
        if (namespace.contains("config")) return "#E8DAEF";
        if (namespace.contains("ui")) return "#D6EAF8";
        return "#FDEBD0";
    }
}
