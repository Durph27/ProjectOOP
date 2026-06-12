import java.lang.instrument.Instrumentation;

public class AstahGeneratorAgent {
    public static void premain(String args, Instrumentation instrumentation) {
        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(8000);
                if ("split-only".equals(args)) {
                    GenerateSplitClassDiagrams.main(new String[0]);
                    System.exit(0);
                }
                boolean customPackageOutput = args != null && !args.isBlank();
                GenerateAstahDiagrams.main(customPackageOutput ? new String[]{args} : new String[0]);
                if (!customPackageOutput) GenerateSplitClassDiagrams.main(new String[0]);
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
