package Image;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.OvalRoi;
import ij.process.ImageProcessor;
import ij.io.FileSaver;

public class SolidImageCropper {

    public static ImagePlus RectangleCrop(ImagePlus imp) {

        //Rectangular ROI
        int x = 1700;
        int y = 1100;
        int w = 2000;
        int h = 1750;
        imp.setRoi(new Roi(x, y, w, h));

        ImageProcessor croppedProcessor = imp.getProcessor().crop();
        ImagePlus croppedImp = new ImagePlus("Cropped", croppedProcessor);

        System.out.println("[Crop] Image cropped (Rectangle) successfully");
        return croppedImp;
    }
    public static ImagePlus OvalCrop(ImagePlus imp) {

        int x = 1750;
        int y = 1940;
        int w = 2100;
        int h = 1700;
        imp.setRoi(new OvalRoi(x, y, w, h));

        ImageProcessor croppedProcessor = imp.getProcessor().crop();
        ImagePlus croppedImp = new ImagePlus("Cropped", croppedProcessor);

        System.out.println("[Crop] Image cropped (Oval) successfully");
        return croppedImp;

    }
}
