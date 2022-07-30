package com.example.paintingprocessing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.SIFT;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class OpencvAlgorithm {
    public static final int SCALE_NUM = 1;

    public static void initLoadOpenCV(){
        boolean success = OpenCVLoader.initDebug();
        if(success){
            Log.println(Log.INFO,"OpenCV","OpenCV Libraries Loaded");
        }else{
            Log.println(Log.ERROR,"OpenCV","Could not load OpenCV Libraries!");
        }
    }

    public static Bitmap sift(Bitmap bm){
        Bitmap input=bm,output= Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);;
        input = ThumbnailUtils.extractThumbnail(bm, bm.getWidth()/SCALE_NUM, bm.getHeight()/SCALE_NUM);//压缩图片
        Mat source = new Mat(),result = new Mat();
        Utils.bitmapToMat(input,source);

        //Imgproc.cvtColor(source,source,Imgproc.COLOR_BGR2GRAY);//转换为灰度图
        SIFT sift = SIFT.create(500);
        MatOfKeyPoint pt = new MatOfKeyPoint();
        sift.detect(source,pt);
        Features2d.drawKeypoints(source,pt,result,new Scalar(0,0,225),Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);

        Utils.matToBitmap(result,output);

        source.release();
        result.release();
        return output;
    }

    public static Bitmap wavelet (Bitmap bm,boolean isMulti){//小波变换
        Bitmap input, output;
        input = ThumbnailUtils.extractThumbnail(bm, bm.getWidth()/SCALE_NUM, bm.getHeight()/SCALE_NUM);//压缩图片
        Mat source = new Mat(),result = new Mat();
        Utils.bitmapToMat(input,source);
        Imgproc.cvtColor(source,source,Imgproc.COLOR_BGR2GRAY);
        //input.recycle();

        double[][][] inputArray = matGray2Array(source);
        double[][][] outputArray = haarWaveletTransform_3D(inputArray,isMulti);
        result = array2Mat(outputArray);

        output= Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);;
        Utils.matToBitmap(result,output);
        source.release();
        result.release();

        return output;
    }

    public static Bitmap sobelContourDetection(Bitmap bm){
        Bitmap inBm, outBm;
        inBm = ThumbnailUtils.extractThumbnail(bm, bm.getWidth()/SCALE_NUM, bm.getHeight()/SCALE_NUM);//压缩图片
        Mat source = new Mat(),result = new Mat();
        Utils.bitmapToMat(inBm,source);
        Imgproc.cvtColor(source,source,Imgproc.COLOR_BGR2GRAY);//转换为灰度图

        Imgproc.Sobel(source,result,-1,0,1);

        outBm= Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);;
        Utils.matToBitmap(result,outBm);
        source.release();
        result.release();
        return outBm;
    }

    public static Bitmap cannyContourDetection(Bitmap bm){
        Bitmap inBm, outBm;
        inBm = ThumbnailUtils.extractThumbnail(bm, bm.getWidth()/SCALE_NUM, bm.getHeight()/SCALE_NUM);//压缩图片
        Mat source = new Mat(),result = new Mat();
        Utils.bitmapToMat(inBm,source);
        Imgproc.cvtColor(source,source,Imgproc.COLOR_BGR2GRAY);//转换为灰度图

        Imgproc.GaussianBlur(source,result,new Size(31,5),80,3);
        Imgproc.Laplacian(source,result,0);
        Imgproc.Canny(source,result,60,200);

        outBm= Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);;
        Utils.matToBitmap(result,outBm);
        source.release();
        result.release();
        return outBm;
    }

    public static Bitmap houghLines(Bitmap bm){
        Bitmap inBm, outBm;
        inBm = ThumbnailUtils.extractThumbnail(bm, bm.getWidth()/SCALE_NUM, bm.getHeight()/SCALE_NUM);//压缩图片
        Mat source = new Mat(),cResult = new Mat(),result = new Mat();
        Utils.bitmapToMat(inBm,source);
        Imgproc.cvtColor(source,source,Imgproc.COLOR_BGR2GRAY);//转换为灰度图

        Mat lines = new Mat();
        Imgproc.Canny(source,result,50,100,3,false);
        Imgproc.cvtColor(result,cResult,Imgproc.COLOR_GRAY2BGR);
        Imgproc.HoughLines(result,lines,1,Math.PI/180,300);//higher threshold, less lines
        for (int x= 0; x<lines.rows();x++) {
            double rho = lines.get(x, 0)[0], theta = lines.get(x, 0)[1];
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a * rho, y0 = b * rho;
            Point pt1 = new Point(Math.round(x0 + 1000 * (-b)), Math.round(y0 + 1000 * (a)));
            Point pt2 = new Point(Math.round(x0 - 1000 * (-b)), Math.round(y0 - 1000 * (a)));
            Imgproc.line(cResult, pt1, pt2, new Scalar(0, 0, 200), 3);
        }

        outBm= Bitmap.createBitmap(cResult.cols(), cResult.rows(), Bitmap.Config.ARGB_8888);;
        Utils.matToBitmap(cResult,outBm);
        source.release();
        result.release();
        return outBm;
    }

    public static Bitmap haarFaceDetection(Bitmap bm,Context context){
        Bitmap inBm, outBm;
        inBm = ThumbnailUtils.extractThumbnail(bm, bm.getWidth()/SCALE_NUM, bm.getHeight()/SCALE_NUM);//压缩图片
        Mat source = new Mat(),result = new Mat();
        Utils.bitmapToMat(inBm,source);
        //Imgproc.cvtColor(source,source,Imgproc.COLOR_BGR2GRAY);//转换为灰度图

        result = SubDetect(source,context);

        outBm= Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);;
        Utils.matToBitmap(result,outBm);
        source.release();
        result.release();
        return outBm;
    }


    public static double[][][] matGray2Array(Mat mat) {
        int width = mat.cols();
        int height = mat.rows();
        double[][][] array = new double[height][width][3];
        byte[] grayData = new byte[height * width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                array[i][j] = mat.get(i, j);
            }
        }
        return array;
    }
    public static Mat array2Mat(double[][][] data) {

        int height = data.length;
        int width = data[0].length;
        Mat mat = new Mat(height, width, CvType.CV_8UC1);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                mat.put(i, j, data[i][j]);
            }
        }
        return mat;
    }

    private static double[][][] haarWaveletTransform_3D(double[][][] input,boolean isMulti){
        int height = input.length;
        int width = input[0].length;
        double[][] input2D = new double[height][width];
        double[][] output2D ;
        double[][][] output = new double[height][width][1];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                input2D[i][j]=input[i][j][0];
            }
        }

        output2D = DiscreteHaarWaveletTransform_2D(input2D,isMulti);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                output[i][j][0]=output2D[i][j];
            }
        }
        return output;
    }


    private static double[][] DiscreteHaarWaveletTransform_2D(double[][] input,boolean isMulti) {

        double[][] output = new double[input.length][input[0].length];
        //对每一行做小波变换
        for (int i = 0; i < input.length; i++) {
            if(isMulti) output[i] = DiscreteHaarWaveletTransform_1D_Mul(input[i]);
            else
                output[i] = DiscreteHaarWaveletTransform_1D (input[i]);
        }
        double[] temp = new double[input.length];
        double[] res=null;
        //对每一列做小波变换
        for (int i = 0; i < output[0].length; i++) {
            for (int j = 0; j < output.length; j++) {
                temp[j]=output[j][i];
            }
            res = DiscreteHaarWaveletTransform_1D(temp);
            for (int j = 0; j < output.length; j++) {
                output[j][i]=res[j];
            }
        }
        return output;
    }

    private static double[] DiscreteHaarWaveletTransform_1D(double[] input) {
        double[] output = new double[input.length];
        for (int i = 0; i < input.length/2; i++) {
            double sum = input[i * 2] + input[i * 2 + 1];
            double difference = input[i * 2] - input[i * 2 + 1];
            output[i] = sum/Math.sqrt(2);
            output[input.length/2 + i] = difference/Math.sqrt(2);
        }
        return output;
    }
    private static double[] DiscreteHaarWaveletTransform_1D_Mul(double[] input) {
        double[] output = new double[input.length];
        for (int length = input.length / 2;; length = length / 2) {
            for (int i = 0; i < length; i++) {
                double sum = input[i * 2] + input[i * 2 + 1];
                double difference = input[i * 2] - input[i * 2 + 1];
                output[i] = sum/Math.sqrt(2);
                output[length + i] = difference/Math.sqrt(2);
            }
            if (length == 1) {
                return output;
            }
            System.arraycopy(output, 0, input, 0, length);
        }
    }

    public static Mat SubDetect(Mat source,Context context){
        CascadeClassifier Detector = null;
        try {//加载识别模型
            InputStream is = context.getResources()
                    .openRawResource(R.raw.haarcascade_frontalface_default);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");
            FileOutputStream os = new FileOutputStream(cascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            Detector = new CascadeClassifier(cascadeFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MatOfRect DetectResult = new MatOfRect();
        Detector.detectMultiScale(source,DetectResult);
        if(DetectResult.toArray().length<=0){
            return source;
        }
        for(Rect rect: DetectResult.toArray()){
            Imgproc.rectangle(source,new Point(rect.x,rect.y), new Point(rect.x+rect.width, rect.y+ rect.height),new Scalar(0,0,255),2);
        }
        return source;
    }

}
