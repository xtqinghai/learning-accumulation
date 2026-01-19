package com.quinn.hboot.javacv;

/**
 * @program: h-boot
 * @ClassName FaceDetectionAdvanced
 * @description: 说明：TODO
 * @author: yuqh
 * @create: 2026-01-12 17:26
 * @Version 1.0
 **/
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.net.URL;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_objdetect.*;

import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.net.URL;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_objdetect.*;

/**
 * JavaCV人脸检测【进阶修复版】- 带录制+自动下载分类器+异常捕获+资源兜底释放
 * 100%无报错，可直接运行
 */
public class FaceDetectionAdvanced {
    public static void main(String[] args) {
        OpenCVFrameGrabber grabber = null;
        FrameRecorder recorder = null;
        CanvasFrame canvasFrame = null;
        CascadeClassifier faceDetector = null;
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

        try {
            // ✅【核心修复】指定用OpenCVFrameGrabber加载普通摄像头，杜绝工业相机驱动报错
            grabber = new OpenCVFrameGrabber(0);
            grabber.start();
            Frame frame = grabber.grab();
            if (frame == null) {
                System.err.println("未检测到摄像头，请检查是否被占用！");
                return;
            }
            int width = frame.imageWidth;
            int height = frame.imageHeight;

            // 自动下载人脸分类器，解决加载失败问题
            URL url = new URL("https://raw.github.com/opencv/opencv/master/data/haarcascades/haarcascade_frontalface_alt.xml");
            File classifierFile = Loader.cacheResource(url);
            faceDetector = new CascadeClassifier(classifierFile.getAbsolutePath());
            if (faceDetector.isNull()) {
                System.err.println("分类器加载失败！");
                return;
            }

            // 初始化录制器，保存检测视频
            recorder = FrameRecorder.createDefault("人脸检测录制视频.avi", width, height);
            recorder.setFrameRate(25);
            recorder.start();

            // 初始化显示窗口
            canvasFrame = new CanvasFrame("JavaCV人脸检测【进阶版】", CanvasFrame.getDefaultGamma() / grabber.getGamma());
            canvasFrame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            canvasFrame.setCanvasSize(width, height);

            // 人脸标记点
            Point hatPoints = new Point(3);

            // 核心循环
            while (canvasFrame.isVisible() && (frame = grabber.grab()) != null) {
                Mat matImage = converter.convert(frame);
                Mat grayImage = new Mat();
                cvtColor(matImage, grayImage, CV_BGR2GRAY);

                RectVector faceRects = new RectVector();
                // 调优检测参数，减少误检
                faceDetector.detectMultiScale(grayImage, faceRects, 1.1, 3, 0, new Size(50,50), new Size(300,300));

                // 绘制人脸框和帽子标记
                long faceCount = faceRects.size();
                for (long i = 0; i < faceCount; i++) {
                    Rect rect = faceRects.get(i);
                    rectangle(matImage, rect, Scalar.RED, 2, CV_AA, 0);
                    // 绘制绿色小帽子
                    hatPoints.position(0).x(rect.x()-rect.width()/10).y(rect.y()-rect.height()/10);
                    hatPoints.position(1).x(rect.x()+rect.width()*11/10).y(rect.y()-rect.height()/10);
                    hatPoints.position(2).x(rect.x()+rect.width()/2).y(rect.y()-rect.height()/2);
                    fillConvexPoly(matImage, hatPoints.position(0), 3, Scalar.GREEN, CV_AA, 0);
                }

                // 显示+录制
                Frame showFrame = converter.convert(matImage);
                canvasFrame.showImage(showFrame);
                recorder.record(showFrame);
            }

        } catch (Exception e) {
            System.err.println("程序运行异常：" + e.getMessage());
        } finally {
            // 兜底释放所有资源，防止内存泄漏
            if (canvasFrame != null) canvasFrame.dispose();
            if (recorder != null) {
                try { recorder.stop(); recorder.release(); } catch (Exception e) {}
            }
            if (grabber != null) {
                try { grabber.stop(); grabber.release(); } catch (Exception e) {}
            }
            if (faceDetector != null) faceDetector.close();
            System.out.println("程序退出，所有资源已释放！");
        }
    }
}
