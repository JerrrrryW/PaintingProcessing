package com.example.paintingprocessing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.paintingprocessing.view.ZoomImageView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener{
    static final int REQUEST_IMAGE = 1;
    RecyclerView recyclerView;
    List<PreviewInfo> datas;
    Bitmap inputBM;
    GalleryAdapter galleryAdapter;
    private ZoomImageView mImagevieqw,detail_image;
    private LinearLayout gallery_layout,detail_layout;

    //图像处理算法的配置
    static final int ALGORITHM_NUM = 7;
    String[] algName = new String[]{"SIFT特征点","小波变换","多级小波变换","Sobel轮廓检测","Canny轮廓检测","人脸识别","霍夫直线检测"};
    private Bitmap processImage(Bitmap input,int algorithm){
        switch (algorithm){
            case 0: return OpencvAlgorithm.sift(input);
            case 1: return OpencvAlgorithm.wavelet(input,false);
            case 2: return OpencvAlgorithm.wavelet(input,true);
            case 3: return OpencvAlgorithm.sobelContourDetection(input);
            case 4: return OpencvAlgorithm.cannyContourDetection(input);
            case 5: return OpencvAlgorithm.haarFaceDetection(input,getApplicationContext());
            case 6: return OpencvAlgorithm.houghLines(input);
            default: return input;
        }
    }

    private int MODE;//当前状态
    public static final int MODE_NONE = 0;//无操作
    public static final int MODE_DRAG = 1;//单指操作
    public static final int MODE_SCALE = 2;//双指操作
    private Matrix startMatrix = new Matrix();//初始矩阵
    private Matrix endMatrix = new Matrix();//变化后的矩阵
    private PointF startPointF = new PointF();//初始坐标
    private float distance;//初始距离
    private float scaleMultiple;//缩放倍数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });

        initGallery();

        mImagevieqw=findViewById(R.id.iv_preview);
        detail_image=findViewById(R.id.iv_detail);
//        detail_image.setOnTouchListener(this);
//        detail_image.setScaleType(ImageView.ScaleType.MATRIX);

        OpencvAlgorithm.initLoadOpenCV();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //获取图片路径
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK && intent != null) {
            Uri selectedImage = intent.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);
            showImage(imagePath);
            c.close();
            //设置缩放触控监听
//            mImagevieqw.setOnTouchListener(this);
//            mImagevieqw.setScaleType(ImageView.ScaleType.MATRIX);
        }
    }
    //加载图片
    private void showImage(String imagePath){
        inputBM = resizeImage(imagePath,800,400);
        mImagevieqw.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mImagevieqw.setImageBitmap(inputBM);
        refreshDataSet();
        galleryAdapter.notifyDataSetChanged();
    }
    //调用各种算法生成展示图
    private void refreshDataSet(){
        for(int i=0;i<ALGORITHM_NUM;i++){
            datas.get(i).setProcessing(true);
            galleryAdapter.notifyItemChanged(i);
            ProcessingThread pt = new ProcessingThread(i);
            pt.start();
        }
        Toast.makeText(this,"Dataset Refreshed!",Toast.LENGTH_LONG).show();
    }

    private class ProcessingThread extends Thread {
        private int index;
        private Bitmap resultBM;
        public ProcessingThread(int i){
            this.index=i;
        }
        @Override
        public void run(){
            resultBM = processImage(inputBM,index);
            datas.get(index).setProcessing(false);
            datas.get(index).setImage(resultBM);
            mHandler.sendEmptyMessage(index);
        }
    }
    private Handler mHandler = new Handler() {//主线程用于接受处理子线程的刷新UI请求
        public void handleMessage(Message msg) {
            galleryAdapter.notifyItemChanged(msg.what);
        }
    };

    //初始化gallery数据
    private void initGallery(){
        initData();
        gallery_layout=findViewById(R.id.gallery_layout);
        detail_layout=findViewById(R.id.detail_layout);

        recyclerView = findViewById(R.id.rv_gallery);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        galleryAdapter = new GalleryAdapter(datas);
        recyclerView.setAdapter(galleryAdapter);
        galleryAdapter.setOnItemClickListener(new GalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                detail_image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                detail_image.setImageBitmap(datas.get(position).getImage());
                gallery_layout.setVisibility(GONE);
                detail_layout.setVisibility(View.VISIBLE);
            }
        });
//        System.out.println("Init RecyclerView Successfully!");

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detail_layout.setVisibility(GONE);
                gallery_layout.setVisibility(View.VISIBLE);
            }
        });

    }
    private void initData(){
        datas = new ArrayList<>();
        for(int i=1;i<=ALGORITHM_NUM;i++){
            Bitmap bm = BitmapFactory.decodeResource(getResources(),R.drawable.painting_icon);
            //bm = ThumbnailUtils.extractThumbnail(bm, bm.getWidth()/2, bm.getHeight()/2);//压缩图片
            PreviewInfo previewInfo = new PreviewInfo("算法 "+i,bm,algName[i-1]);
            datas.add(previewInfo);
        }
//        System.out.println("Init Data Successfully!");
    }

    //使用BitmapFactory.Options的inSampleSize参数来缩放
    public Bitmap resizeImage(String path,
                                        int width, int height)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//不加载bitmap到内存中
        BitmapFactory.decodeFile(path,options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 1;

        if (outWidth != 0 && outHeight != 0 && width != 0 && height != 0)
        {
            int sampleSize=(outWidth/width+outHeight/height)/4;
            Log.d("ThumbnailUtils", "sampleSize = " + sampleSize);
            options.inSampleSize = sampleSize;
        }

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView im = (ImageView)v;
        switch (event.getAction()&event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN://单指触碰

                //起始矩阵先获取ImageView的当前状态
                startMatrix.set(im.getImageMatrix());
                //获取起始坐标
                startPointF.set(event.getX(), event.getY());
                //此时状态是单指操作
                MODE = MODE_DRAG;

                break;
            case MotionEvent.ACTION_POINTER_DOWN://双指触碰

                //最后的状态传给起始状态
                startMatrix.set(endMatrix);
                //获取距离
                distance = getDistance(event);
                //状态改为双指操作
                MODE = MODE_SCALE;

                break;
            case MotionEvent.ACTION_MOVE://滑动（单+双）
                if (MODE == MODE_DRAG) {//单指滑动时
                    //获取初始矩阵
                    endMatrix.set(startMatrix);
                    //向矩阵传入位移距离
                    endMatrix.postTranslate(event.getX() - startPointF.x, event.getY() - startPointF.y);
                } else if (MODE == MODE_SCALE) {//双指滑动时
                    //计算缩放倍数
                    scaleMultiple = getDistance(event)/distance;
                    //获取初始矩阵
                    endMatrix.set(startMatrix);
                    //向矩阵传入缩放倍数
                    endMatrix.postScale(scaleMultiple, scaleMultiple,startPointF.x,startPointF.y);
                }
                break;
            case MotionEvent.ACTION_UP://单指离开
            case MotionEvent.ACTION_POINTER_UP://双指离开
                //手指离开后，重置状态
                MODE = MODE_NONE;
                break;

        }
        //事件结束后，把矩阵的变化同步到ImageView上

        im.setImageMatrix(endMatrix);
        return true;

    }
    //获取距离
    private static float getDistance(MotionEvent event) {//获取两点间距离
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
}