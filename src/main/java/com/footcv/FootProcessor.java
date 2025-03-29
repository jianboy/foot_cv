package com.footcv;

import com.footcv.utils.FootUtil;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

public class FootProcessor {
    static {
        nu.pattern.OpenCV.loadLocally();
    }

    public static void main(String[] args) {

        // Load the input image
        String inputImagePath = "F:\\workspace\\foot_cv\\docs\\foot_cv\\01_foreground.png";
        String outputImagePath = "F:\\workspace\\foot_cv\\docs\\foot_cv\\resul00t2.png";
        String backendImagePath ="F:\\workspace\\foot_cv\\docs\\foot_cv\\background.png";

        Mat srcImage = Imgcodecs.imread(inputImagePath, Imgcodecs.IMREAD_UNCHANGED);
        Mat backgroundImage = Imgcodecs.imread(backendImagePath, Imgcodecs.IMREAD_UNCHANGED);

//        Mat jpgImage = new Mat();
//        Mat pngImage =new Mat();
//        Imgproc.cvtColor(pngImage, jpgImage, Imgproc.COLOR_BGRA2BGR);

        if (srcImage.empty()) {
            System.err.println("Error: Could not load image " + inputImagePath);
            return;
        }

        System.out.println("Image loaded successfully. Size: " + srcImage.width() + "x" + srcImage.height());

        // Process the image
        Mat processedImage = FootUtil.processFootImage(srcImage);
        // 删除背景
        Mat resultImage = FootUtil.deleteBlack(processedImage);

        // 图片合并
        Mat mergedImage = FootUtil.mergeImage(resultImage, backgroundImage);

    // png转为 jpg
    //        Mat jpgImage = new Mat();
    //        Imgproc.cvtColor(mergedImage, jpgImage, Imgproc.COLOR_BGRA2BGR);

        // Save the result
        boolean success = Imgcodecs.imwrite(outputImagePath, mergedImage);
        if (success) {
            System.out.println("Result image saved to " + outputImagePath);
        } else {
            System.err.println("Error: Could not save result image");
        }
    }

}