package com.footcv.utils;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* @Description: footutil
* @Date: 2025年03月30日 Sunday
* @Author liuyuqi.gov@msn.
*/
public class FootUtil {

    // 浮雕效果
    public static Mat processFootImage(Mat image) {
        // 检查是否有 Alpha 通道
        List<Mat> channels = new ArrayList<Mat>();
        Core.split(image, channels);
        Mat bgr;
        Mat alpha = new Mat();
        if (channels.size() == 4) {
            bgr = new Mat();
            Core.merge(Arrays.asList(channels.get(0), channels.get(1), channels.get(2)), bgr);
            alpha = channels.get(3);
        } else {
            System.out.println("图像没有 Alpha 通道！");
            return null;
        }

        Mat mask = new Mat();
        Imgproc.threshold(alpha, mask, 0, 255, Imgproc.THRESH_BINARY);
        Core.bitwise_and(bgr, bgr, bgr, mask);

        // 转换为灰度图像
        Mat gray = new Mat();
        Imgproc.cvtColor(bgr, gray, Imgproc.COLOR_BGR2GRAY);

        // 定义浮雕卷积核
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        kernel.put(0, 0, -10, -8, 0);
        kernel.put(1, 0, -8, 1, 8);
        kernel.put(2, 0, 0, 8, 10);

        // 应用滤波
        Mat embossed = new Mat();
        Imgproc.filter2D(gray, embossed, -1, kernel);

        // 归一化浮雕图像
        Core.normalize(embossed, embossed, 0, 255, Core.NORM_MINMAX);
        embossed.convertTo(embossed, CvType.CV_8U);

        // 转换为 BGR
        Mat embossedBGR = new Mat();
        Imgproc.cvtColor(embossed, embossedBGR, Imgproc.COLOR_GRAY2BGR);

        // 设置前景色和背景色
        Scalar foregroundColor = new Scalar(111, 193, 222); // 金黄色
        Scalar backgroundColor = new Scalar(0, 0, 0);       // 黑色

//        Mat embossedColor = new Mat(embossedBGR.size(), embossedBGR.type());
        Mat embossedColor = new Mat();
        Core.inRange(embossedBGR, new Scalar(128, 128, 128), new Scalar(255, 255, 255), embossedColor);

        Mat foreground = new Mat();
        Mat background = new Mat();
        embossedBGR.setTo(foregroundColor, embossedColor);
        Core.bitwise_not(embossedColor, embossedColor);
        embossedBGR.setTo(backgroundColor, embossedColor);

        // 仅保留原始 Alpha 通道中的透明区域
        Mat transparentMask = new Mat();
        Imgproc.threshold(alpha, transparentMask, 0, 255, Imgproc.THRESH_BINARY);
        Core.bitwise_and(embossedBGR, embossedBGR, embossedBGR, transparentMask);
        Core.bitwise_and(alpha, alpha, alpha, transparentMask);

        // 合并 BGR 和 Alpha 通道
        List<Mat> finalChannels = new ArrayList<Mat>(Arrays.asList(embossedBGR, alpha));
        Mat result = new Mat();
        Core.merge(finalChannels, result);


        return result;
    }

    //    删除黑色纹理
    public static Mat deleteBlack(Mat image) {
        System.out.println("image.channels() = " + image.channels());
        if (image.channels() != 4) {
            System.out.println("图像没有 Alpha 通道");
            return null;
        }

        // 分离通道
        List<Mat> bgrAlpha = new ArrayList<Mat>();
        Core.split(image, bgrAlpha);
        Mat bgr = new Mat(image.rows(), image.cols(), CvType.CV_8UC3);
        Mat alpha = bgrAlpha.get(3);


        // 合并BGR通道
        Core.merge(bgrAlpha.subList(0, 3), bgr);
        // 定义黑色阈值范围
        Scalar lowerBlack = new Scalar(0, 0, 0);
        Scalar upperBlack = new Scalar(30, 30, 30);
        // 创建黑色掩码
        Mat blackMask = new Mat();
        Core.inRange(bgr, lowerBlack, upperBlack, blackMask);

        // 将黑色区域的 Alpha 设置为 0 (透明)
        for (int i = 0; i < alpha.rows(); i++) {
            for (int j = 0; j < alpha.cols(); j++) {
                if (blackMask.get(i, j)[0] > 0) {
                    alpha.put(i, j, 0); // 设置透明度为0
                }
            }
        }
        // 合并BGR和更新后的Alpha通道
        List<Mat> resultChannels = new ArrayList<Mat>(bgrAlpha);
        resultChannels.set(3, alpha);
        Mat result = new Mat();
        Core.merge(resultChannels, result);

        return result;
    }

    public static Mat mergeImage(Mat foregroundImage, Mat backgroundImage) {
        System.out.println("前景图像: " + foregroundImage.width() + "x" + foregroundImage.height() + ", 通道: "
                + foregroundImage.channels());
        System.out.println("背景图像: " + backgroundImage.width() + "x" + backgroundImage.height() + ", 通道: "
                + backgroundImage.channels());

        if (foregroundImage.channels() != 4) {
            System.err.println("前景图像需要有透明通道(4通道)");
            return backgroundImage.clone();
        }

        Mat bgImage;
        if (backgroundImage.channels() != 3 && backgroundImage.channels() != 4) {
            System.err.println("背景图像格式不支持，将创建白色背景");
            bgImage = new Mat(backgroundImage.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        } else {
            bgImage = backgroundImage.clone();
        }

        if (bgImage.channels() == 4) {
            Mat bgImageBGR = new Mat();
            Imgproc.cvtColor(bgImage, bgImageBGR, Imgproc.COLOR_BGRA2BGR);
            bgImage = bgImageBGR;
        }

        int targetWidth = bgImage.width() / 2;
        int targetHeight = (int) (foregroundImage.height() * (targetWidth / (double) foregroundImage.width()));

        if (targetHeight > bgImage.height() / 2) {
            targetHeight = bgImage.height() / 2;
            targetWidth = (int) (foregroundImage.width() * (targetHeight / (double) foregroundImage.height()));
        }

        Mat resizedForeground = new Mat();
        Imgproc.resize(foregroundImage, resizedForeground, new Size(targetWidth, targetHeight), 0, 0, Imgproc.INTER_CUBIC);

        int x = (bgImage.width() - targetWidth) / 2;
        int y = (bgImage.height() - targetHeight) / 2;

        Mat result = bgImage.clone();
        for (int i = 0; i < targetHeight; i++) {
            for (int j = 0; j < targetWidth; j++) {
                double[] fgPixel = resizedForeground.get(i, j);
                if (fgPixel.length == 4) {
                    double alpha = fgPixel[3] / 255.0;

                    if (alpha > 0) {
                        int destY = y + i;
                        int destX = x + j;

                        if (destY >= 0 && destY < result.height() && destX >= 0 && destX < result.width()) {
                            if (alpha >= 1.0) {
                                result.put(destY, destX, new double[] { fgPixel[0], fgPixel[1], fgPixel[2] });
                            } else {
                                // 半透明，混合前景和背景
                                double[] bgPixel = result.get(destY, destX);
                                for (int c = 0; c < 3; c++) { // 只处理BGR通道
                                    double blended = fgPixel[c] * alpha + bgPixel[c] * (1 - alpha);
                                    bgPixel[c] = blended;
                                }
                                result.put(destY, destX, bgPixel);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }
}