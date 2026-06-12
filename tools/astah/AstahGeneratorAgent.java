import java.lang.instrument.Instrumentation;

public class AstahGeneratorAgent {
    public static void premain(String args, Instrumentation instrumentation) {
        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(8000);
                GenerateAstahDiagrams.main(new String[0]);
                GenerateSplitClassDiagrams.main(new String[0]);
                System.exit(0);
            } catch (Throwable e) {
                e.printStackTrace();
                System.exit(2);
            }
        }, "astah-diagram-generator");
        worker.setDaemon(false);
        worker.start();
    }
}
