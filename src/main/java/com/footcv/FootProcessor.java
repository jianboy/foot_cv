package com.footcv;

import com.footcv.utils.FootUtil;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

/**
* @Description: enter point
* @Date: 2025年03月30日 Sunday
* @Author liuyuqi.gov@msn.
*/
public class FootProcessor {
    static {
        nu.pattern.OpenCV.loadLocally();
    }

    public static void main(String[] args) {
        
        String file_path = "/workspace/foot_cv_java/";
        // Load the input image
        String inputImagePath = file_path + "01_foreground.png";
        String outputImagePath = file_path + "result00t2.png";
        String backendImagePath = file_path + "background.png";

        Mat srcImage = Imgcodecs.imread(inputImagePath, Imgcodecs.IMREAD_UNCHANGED);
        Mat backgroundImage = Imgcodecs.imread(backendImagePath, Imgcodecs.IMREAD_UNCHANGED);

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

        // Save the result
        boolean success = Imgcodecs.imwrite(outputImagePath, mergedImage);
        if (success) {
            System.out.println("Result image saved to " + outputImagePath);
        } else {
            System.err.println("Error: Could not save result image");
        }
    }

}