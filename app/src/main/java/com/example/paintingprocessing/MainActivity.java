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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE = 1;
    RecyclerView recyclerView;
    List<PreviewInfo> datas;
    Bitmap inputBM;
    GalleryAdapter galleryAdapter;

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
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
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
            }
        }
    }
    //加载图片
    private void showImage(String imagePath){
        inputBM = resizeImage(imagePath,800,400);
        ((ImageView)findViewById(R.id.iv_preview)).setImageBitmap(inputBM);
        refreshDataSet();
        galleryAdapter.notifyDataSetChanged();
    }

    //初始化gallery数据
    private void initGallery(){
        initData();
//        System.out.println("Init Data Successfully!");
        recyclerView = findViewById(R.id.rv_gallery);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        galleryAdapter = new GalleryAdapter(datas);
        recyclerView.setAdapter(galleryAdapter);
//        System.out.println("Init RecyclerView Successfully!");
    }

    private void initData(){
        datas = new ArrayList<>();
        for(int i=1;i<=5;i++){
            Bitmap bm = BitmapFactory.decodeResource(getResources(),R.mipmap.demo);
            bm = ThumbnailUtils.extractThumbnail(bm, bm.getWidth()/2, bm.getHeight()/2);//压缩图片
            PreviewInfo previewInfo = new PreviewInfo("算法 "+i,bm,"XX算法");
            datas.add(previewInfo);
        }
    }

    private void refreshDataSet(){
        for(int i=1;i<=5;i++){
//            Bitmap bm = BitmapFactory.decodeResource(getResources(),R.mipmap.demo);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bm = ThumbnailUtils.extractThumbnail(inputBM, inputBM.getWidth()/2, inputBM.getHeight()/2);//压缩图片
            datas.get(i-1).setImage(bm);
        }
        Toast.makeText(this,"Dataset Refreshed!",Toast.LENGTH_LONG).show();
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
            int sampleSize=(outWidth/width+outHeight/height)/2;
            Log.d("ThumbnailUtils", "sampleSize = " + sampleSize);
            options.inSampleSize = sampleSize;
        }

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }
}