import Analysis.SummaryViewer;
import Image.PipelineManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiLauncher {

    public static void main(String[] args) {
       // System.out.println("[debug] classpath:\n" + System.getProperty("java.class.path"));
       // System.out.println("[debug] IJ from: " + ij.IJ.class.getProtectionDomain().getCodeSource().getLocation());


        JFrame frame = new JFrame("🧪 Image Processor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLocationRelativeTo(null);

        // Prompt once at the start
        int showImagesChoice = JOptionPane.showConfirmDialog(
                frame,
                "Do you want to see ImageJ output images during processing?",
                "Show Images?",
                JOptionPane.YES_NO_OPTION
        );
        boolean showImages = (showImagesChoice == JOptionPane.YES_OPTION);


        JButton chooseButton = new JButton("Choose Folder");
        chooseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser dirChooser = new JFileChooser();
                dirChooser.setDialogTitle("Select a Folder with Images");
                dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (dirChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File folder = dirChooser.getSelectedFile();

                    File[] imageFiles = folder.listFiles((dir, name) ->
                            name.toLowerCase().endsWith(".png") ||
                                    name.toLowerCase().endsWith(".jpg") ||
                                    name.toLowerCase().endsWith(".jpeg"));

                    if (imageFiles == null || imageFiles.length == 0) {
                        JOptionPane.showMessageDialog(frame,
                                "⚠️ No PNG or JPG images found in this folder.",
                                "Nothing to Process", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    List<String> csvPaths = new ArrayList<>();

                    for (File imageFile : imageFiles) {
                        try {
                            String csvPath = new PipelineManager().run(imageFile.getAbsolutePath(), showImages);
                            System.out.println("✔ CSV created: " + csvPath);
                            csvPaths.add(csvPath);
                        } catch (Exception ex) {
                            System.err.println("❌ Failed to process: " + imageFile.getName());
                            ex.printStackTrace();
                        }
                    }

                    // ✳️ Prompt user for output file save path
                    JFileChooser saveChooser = new JFileChooser();
                    saveChooser.setDialogTitle("Where would you like to save the final summary CSV?");
                    saveChooser.setSelectedFile(new File("summary_output.csv")); // default name

                    if (saveChooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
                        JOptionPane.showMessageDialog(frame,
                                "❌ No output path chosen. Process canceled.",
                                "Canceled", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    File outputCsvFile = saveChooser.getSelectedFile();

                    //Issue #1: Writing the csv path in a place where python can access it - PATCHED
                    try {
                        String workingDir = System.getProperty("user.dir");
                        File csvListFile = new File(workingDir, "csv_List.txt");
                        try (FileWriter fw = new FileWriter(csvListFile)) {
                            for (String path : csvPaths) {
                                fw.write(path);
                                fw.write(System.lineSeparator());
                            }
                        }

                        // Call Python with input list and output path

                        //Issue #2: Call python
                        // In your actionPerformed (or wherever you run it)
                        //Issue #2: Call python
                        String pythonScriptPath = Paths.get("python", "MLTerminal.py").toString();
                        ProcessBuilder pb = new ProcessBuilder(
                                "python",
                                pythonScriptPath,
                                csvListFile.getAbsolutePath(),
                                outputCsvFile.getAbsolutePath()
                        ).inheritIO();
                        pb.start().waitFor();


                        // Show the results in a summary table

                       SummaryViewer.showSummary(outputCsvFile.getAbsolutePath());


                    } catch (Exception ex) {
                        System.err.println("❌ Error calling Python or writing CSV list");
                        ex.printStackTrace();
                    }

                    JOptionPane.showMessageDialog(frame,
                            "✅ All images processed.\nSummary saved to:\n" + outputCsvFile.getAbsolutePath(),
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    frame.dispose();
                    // System.exit(0);

                }
            }
        });

        frame.getContentPane().add(chooseButton);
        frame.setVisible(true);
    }
}
