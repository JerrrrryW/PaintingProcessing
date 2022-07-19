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
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE = 1;
    RecyclerView recyclerView;
    List<PreviewInfo> datas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        Bitmap bm = BitmapFactory.decodeFile(imagePath);
        ((ImageView)findViewById(R.id.iv_preview)).setImageBitmap(bm);
    }

    //初始化gallery数据
    private void initGallery(){
        initData();
        System.out.println("Init Data Successfully!");
        recyclerView = findViewById(R.id.rv_gallery);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        recyclerView.setAdapter(new GalleryAdapter(datas));
        System.out.println("Init RecyclerView Successfully!");
    }

    private void initData(){
        datas = new ArrayList<>();
        for(int i=1;i<=5;i++){
            Bitmap bm = BitmapFactory.decodeResource(getResources(),R.drawable.demo);
            PreviewInfo previewInfo = new PreviewInfo("算法 "+i,bm,"XX算法");
            datas.add(previewInfo);
        }
    }

}