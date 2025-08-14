package Analysis;
import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PythonBridge {

    // Prefer the frozen EXE; fall back to "python MLTerminal.py" for dev
    public static void runPythonStep(Path csvListFile, Path outputCsvFile) throws Exception {
        // Directory where the app is running (works for JAR and for classes)
        Path appDir = Paths.get(new File(
                PythonBridge.class.getProtectionDomain().getCodeSource().getLocation().toURI()
        ).getParent());

        // 1) Try bundled Windows binary (next to the JAR)
        Path exe = appDir.resolve("ml_pipeline.exe");

        List<String> cmd;
        if (Files.exists(exe)) {
            cmd = Arrays.asList(
                    exe.toString(),
                    csvListFile.toAbsolutePath().toString(),
                    outputCsvFile.toAbsolutePath().toString()
            );
        } else {
            // 2) Dev mode: run the .py using the system Python
            //    - Choose python3 if python isn't found on mac/linux
            String py = guessPythonCmd();
            Path script = appDir.resolve("python").resolve("MLTerminal.py"); // adjust if needed
            if (!Files.exists(script)) {
                // also allow running from project root
                script = Paths.get("python", "MLTerminal.py").toAbsolutePath();
            }
            cmd = Arrays.asList(
                    py,
                    script.toString(),
                    csvListFile.toAbsolutePath().toString(),
                    outputCsvFile.toAbsolutePath().toString()
            );
        }

        ProcessBuilder pb = new ProcessBuilder(cmd)
                .directory(appDir.toFile())   // safe working dir
                .redirectErrorStream(true)    // merge stderr into stdout
                .inheritIO();                 // show logs in Java console

        Process p = pb.start();

        // Optional: timeout so we don't hang forever
        boolean finished = p.waitFor(30, TimeUnit.MINUTES);
        if (!finished) {
            p.destroyForcibly();
            throw new RuntimeException("Python step timed out.");
        }

        int code = p.exitValue();
        if (code != 0) {
            throw new RuntimeException("Python step failed with exit code " + code);
        }
    }

    private static String guessPythonCmd() {
        // Windows usually "python"; *nix sometimes "python3"
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win") ? "python" : "python3";
    }
}
