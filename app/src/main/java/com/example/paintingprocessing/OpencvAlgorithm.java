package com.example.paintingprocessing;

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
import org.opencv.core.Scalar;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.SIFT;
import org.opencv.imgproc.Imgproc;

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

        Imgproc.cvtColor(source,source,Imgproc.COLOR_BGR2GRAY);//转换为灰度图
        SIFT sift = SIFT.create(500);
        MatOfKeyPoint pt = new MatOfKeyPoint();
        sift.detect(source,pt);
        Features2d.drawKeypoints(source,pt,result,new Scalar(0,0,225),Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);

        Utils.matToBitmap(result,output);

        source.release();
        result.release();
        return output;
    }

    public static Bitmap wavelet (Bitmap bm){
        Bitmap input, output;
        input = ThumbnailUtils.extractThumbnail(bm, bm.getWidth()/SCALE_NUM, bm.getHeight()/SCALE_NUM);//压缩图片
        Mat source = new Mat(),result = new Mat();
        Utils.bitmapToMat(input,source);
        //input.recycle();

        double[][][] inputArray = matGray2Array(source);
        double[][][] outputArray = haarWaveletTransform_3D(inputArray);
        result = array2Mat(outputArray);

        output= Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);;
        Utils.matToBitmap(result,output);
        source.release();
        result.release();

        return output;
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
        Mat mat = new Mat(height, width, CvType.CV_8UC3);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                mat.put(i, j, data[i][j]);
            }
        }
        return mat;
    }

    private static double[][][] haarWaveletTransform_3D(double[][][] input){
        int height = input.length;
        int width = input[0].length;
        double[][][] input3D = new double[3][height][width];
        double[][][] output3D = new double[3][][];
        double[][][] output = new double[height][width][3];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                for (int k=0;k<3;k++){
                    input3D[k][i][j]=input[i][j][k];
                }
            }
        }

        for (int k=0;k<3;k++){
            output3D[k] = DiscreteHaarWaveletTransform_2D(input3D[k]);
        }


        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                for (int k=0;k<3;k++){
                    output[i][j][k]=output3D[k][i][j];
                }
            }
        }
        return output;
    }


    private static double[][] DiscreteHaarWaveletTransform_2D(double[][] input) {

        double[][] output = new double[input.length][input[0].length];
        //对每一行做小波变换
        for (int i = 0; i < input.length; i++) {
            output[i]= DiscreteHaarWaveletTransform_1D(input[i]);
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


}
