package User;

import java.util.Scanner;
import java.nio.file.*;

public class User {

    public static void Introduce() {
        System.out.println("=========================================");
        System.out.println("     🧪 Pacifico Morphology Tool 🧪"     );
        System.out.println("=========================================");
        System.out.println();
        System.out.println("  ➤ Developed for analyzing particle");
        System.out.println("    morphology using ImageJ + Python.");
        System.out.println();
        System.out.println("  ➤ Automatically extracts and processes");
        System.out.println("    shape features from segmented images.");
        System.out.println();
        System.out.println("=========================================");
        System.out.println();
    }

    public static String getImagePath() {
        Scanner scanner = new Scanner(System.in);  // create scanner outside loop
        while (true) {
            System.out.println();
            System.out.println("🖼️ Please copy and paste your image's full path below:");
            System.out.print("📂 Path: ");

            String path = scanner.nextLine().trim().replace("\"", "");

            if (isValidFilePath(path)) {
                scanner.close();
                return path;
            } else {
                System.out.println("❌ Invalid path. Please try again.");
            }
        }

    }


    private static boolean isValidFilePath(String pathStr) {
        Path path  = Paths.get(pathStr);
        return Files.exists(path) && Files.isRegularFile(path) && Files.isReadable(path);
    }
}
