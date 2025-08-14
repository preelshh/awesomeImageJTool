// Load an image from a file path and return it as an ImagePlus
package Image;

import ij.IJ;
import ij.ImagePlus;

public class ImageLoader {


    // Function: returns ImageJ
    public static ImagePlus load(String path) {
        ImagePlus imp = IJ.openImage(path);

        if (imp == null) {
            System.err.println("❌ Failed to load image from: " + path);
            throw new IllegalArgumentException("Image file not found or invalid format.");
        }

        System.out.println("✅ Loaded image: " + imp.getTitle());
        return imp;
    } // hard code pixel to cm ratio
}
