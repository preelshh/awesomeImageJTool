
package Analysis;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import inra.ijpb.measure.region2d.*;

import java.io.IOException;

public class RegionAnalyzer {

    /**
     * Computes features and saves to CSV at the given path.
     */
    public static String generateCSV(ImagePlus labeledImp, String orgImgName) {
        ResultsTable master = generateFeaturesTable(labeledImp);
        try {
            String outputPath = "C:/Users/priya/IdeaProjects/ima722/" + orgImgName + "_features.csv";
            master.saveAs(outputPath);
            System.out.println("✔ Saved features to " + outputPath);
          //  System.out.println(outputPath);
            return outputPath;
        } catch (IOException e) {
            System.err.println("❌ Failed to save CSV: " + e.getMessage());
        }
        return null;
    }

    public static String generateCSV(ImagePlus labeledImp) {
        ResultsTable master = generateFeaturesTable(labeledImp);
        try {
            String imageName = "image"; // remove extension
            String outputPath = "C:/Users/priya/IdeaProjects/ima722/" + imageName + "_features.csv";  // ✅ now includes filename
            master.saveAs(outputPath);
            System.out.println("✔ Saved features to " + outputPath);
            return outputPath;
         //   System.out.println(outputPath);
        } catch (IOException e) {
            System.err.println("❌ Failed to save CSV: " + e.getMessage());
        }

        return null;
    }




    /**
     * Merges outputs from multiple RegionAnalyzer2D analyzers into one ResultsTable.
     */
    private static ResultsTable generateFeaturesTable(ImagePlus labeledImp) {
        labeledImp.getCalibration().pixelWidth = 1.0 / 243.0;  // cm per pixel
        labeledImp.getCalibration().pixelHeight = 1.0 / 243.0;
        labeledImp.getCalibration().setUnit("cm");


        RegionAnalyzer2D<?>[] analyzers = new RegionAnalyzer2D<?>[]{
                new IntrinsicVolumesAnalyzer2D(),
                new EquivalentEllipse(),
                new LargestInscribedCircle(),
                new MaxFeretDiameter(),
                new Convexity(),
                new GeodesicDiameter(),
                new BoundingBox(),
                new Centroid()
        };

        ResultsTable master = analyzers[0].computeTable(labeledImp);

        for (int i = 1; i < analyzers.length; i++) {
            ResultsTable t = analyzers[i].computeTable(labeledImp);
            for (String col : t.getHeadings()) {
                if (col.equals("Label")) continue;
                for (int row = 0; row < t.getCounter(); row++) {
                    master.setValue(col, row, t.getValue(col, row));
                }
            }
        }

        return master;
    }
}
