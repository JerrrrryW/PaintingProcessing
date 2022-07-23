package com.example.paintingprocessing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.SIFT;
import org.opencv.imgproc.Imgproc;

public class OpencvAlgorithm {
    public static final int SCALE_NUM = 8;

    public static void initLoadOpenCV(){
        boolean success = OpenCVLoader.initDebug();
        if(success){
            Log.println(Log.INFO,"OpenCV","OpenCV Libraries Loaded");
        }else{
            Log.println(Log.ERROR,"OpenCV","Could not load OpenCV Libraries!");
        }
    }

    public static Bitmap sift(String path){
        Bitmap bm = BitmapFactory.decodeFile(path);
        bm = ThumbnailUtils.extractThumbnail(bm, bm.getWidth()/SCALE_NUM, bm.getHeight()/SCALE_NUM);//压缩图片
        Mat source = new Mat(),result = new Mat();
        Utils.bitmapToMat(bm,source);

        Imgproc.cvtColor(source,result,Imgproc.COLOR_BGR2GRAY);//转换为灰度图
        SIFT sift = SIFT.create(1000);
        MatOfKeyPoint pt = new MatOfKeyPoint();
        sift.detect(source,pt);
        Features2d.drawKeypoints(source,pt,result,new Scalar(0,0,225),Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);

        Utils.matToBitmap(result,bm);

        source.release();
        result.release();
        return bm;
    }
}
