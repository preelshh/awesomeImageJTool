package Image;

import ij.IJ;
import ij.ImagePlus;

public class ImageBinaryWatershed {


    // Convert to 8-bit grayscale
    private static ImagePlus convertTo8Bit(ImagePlus imp) {
        IJ.run(imp, "8-bit", "");
        return imp;
    }

    // Enhance contrast with optional saturation level
    private static ImagePlus enhanceContrast(ImagePlus imp, double saturation) {
        IJ.run(imp, "Enhance Contrast...", "saturated=" + saturation + " normalize");
        return imp;
    }

    private static ImagePlus enhanceContrast(ImagePlus imp) {
        return enhanceContrast(imp, 0.35);
    }

    // Subtract background with optional rolling ball radius
    private static ImagePlus subtractBackground(ImagePlus imp, double radius) {
        IJ.run(imp, "Subtract Background...", "rolling=" + radius);
        return imp;
    }

    private static ImagePlus subtractBackground(ImagePlus imp) {
        return subtractBackground(imp, 100);
    }

    // Add Despeckle
    // Convert to binary (threshold)
    private static ImagePlus makeBinary(ImagePlus imp) {
        IJ.setAutoThreshold(imp, "Li");
        IJ.run(imp, "Convert to Mask", "");
        IJ.run(imp, "Invert", "");
        return imp;
    }

    // Apply watershed
    private static ImagePlus watershed(ImagePlus imp) {
     //   IJ.run(imp, "Despeckle", "");
        //   IJ.run(imp, "Despeckle", "");
        IJ.run(imp, "Watershed", "");
        return imp;
    }
    

    /**
     * Full pipeline using default parameters for contrast and background subtraction.
     */
    public static ImagePlus runAll(ImagePlus imp) {
        ImagePlus copy = imp.duplicate();  // avoid modifying original

        convertTo8Bit(copy);
        enhanceContrast(copy);          // uses default saturation
 //       subtractBackground(copy);       // uses default radius
        makeBinary(copy);
        watershed(copy);

        return copy;
    }

    /**
     * Overloaded pipeline using custom saturation and background subtraction values.
     */
    public static ImagePlus runAll(ImagePlus imp, double saturation, double bgRadius) {
        ImagePlus copy = imp.duplicate();

        convertTo8Bit(copy);
        enhanceContrast(copy, saturation);
   //     subtractBackground(copy, bgRadius); //This can sometimes help but also be very bad for image pre-processing
        // Could be beneficial to create an algorithm based off an initial screening of the image to tell if using this is good or not
        makeBinary(copy);
        watershed(copy);

        return copy;
    }
}
