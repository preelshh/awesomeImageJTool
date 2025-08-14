package Analysis;// File: Analysis.SummaryViewer.java

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SummaryViewer {

    public static void showSummary(String csvPath) {
        SwingUtilities.invokeLater(() -> {
            try {

                Object[][] data = readCsv(csvPath);
                String[] columns = {"Image_Source", "Score", "Score_Description"};

                DefaultTableModel model = new DefaultTableModel(data, columns);
                JTable table = new JTable(model);

                JScrollPane scrollPane = new JScrollPane(table);
                table.setFillsViewportHeight(true);

                JFrame frame = new JFrame("📊 Summary Output");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(600, 400);
                frame.add(scrollPane, BorderLayout.CENTER);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Failed to read summary CSV:\n" + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static Object[][] readCsv(String csvPath) throws IOException {
        List<Object[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] tokens = line.split(",", -1);
                if (tokens.length >= 3) {
                    rows.add(new Object[]{
                            tokens[0].trim(),
                            tokens[64].trim(),
                            tokens[65].trim()
                    });
                }
            }
        }
        return rows.toArray(new Object[0][]);
    }
}
