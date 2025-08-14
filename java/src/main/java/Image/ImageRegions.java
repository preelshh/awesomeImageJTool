package Image;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.TextRoi;
import ij.process.ImageProcessor;
import inra.ijpb.binary.conncomp.FloodFillComponentsLabeling;

import java.awt.*;
import java.util.List;

public class ImageRegions {

    public static ImagePlus labelRegions(ImagePlus imp) {
        if (imp == null) {
            throw new IllegalArgumentException("Input image is null.");
        }

        FloodFillComponentsLabeling labeller = new FloodFillComponentsLabeling(8); // 8-connectivity
        ImageProcessor labeledProcessor = labeller.computeLabels(imp.getProcessor());
        ImagePlus labeledImp = new ImagePlus("Labeled Regions", labeledProcessor);

        return labeledImp;
    }
}
