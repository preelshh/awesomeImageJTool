package Image;

import java.io.File;

import ij.ImagePlus;


import Analysis.RegionAnalyzer;


public class PipelineManager {
    public static String run(String imagePath, boolean showImages) throws Exception {
        System.out.println("[Pipeline] loading image"); System.out.flush();
        ImagePlus img = ImageLoader.load(imagePath);
        System.out.println("[Pipeline] image loaded"); System.out.flush();


       System.out.println("[Pipeline] start cropping"); System.out.flush();
    //   ImagePlus croppedImg = SolidImageCropper.RectangleCrop(img);
       System.out.println("[Pipeline] cropped"); System.out.flush();


        System.out.println("[Pipeline] running watershed"); System.out.flush();
        ImagePlus binaryImg = ImageBinaryWatershed.runAll(img);

        String imageName = new File(imagePath).getName();
        String imageBaseName = imageName.replaceFirst("[.][^.]+$", "");
        binaryImg.setTitle("Watershed - " + imageBaseName);
        if (showImages) {
            binaryImg.show();
        }
        System.out.println("[Pipeline] watershed done"); System.out.flush();


        // Pixel measurements
        System.out.println("[Pipeline] labeling regions"); System.out.flush();
        ImagePlus regionsImg = ImageRegions.labelRegions(binaryImg);
       // if (showImages) {
        //    regionsImg.show();
        // }
        System.out.println("[Pipeline] regions labeled"); System.out.flush();

        System.out.println("[Pipeline] generating CSV"); System.out.flush();
        String csvPath = RegionAnalyzer.generateCSV(regionsImg, new File(imagePath).getName());
        System.out.println("[Pipeline] CSV at " + csvPath); System.out.flush();

        return csvPath;
    }
}
