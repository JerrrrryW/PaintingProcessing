package com.example.paintingprocessing;

import android.graphics.Bitmap;

public class PreviewInfo {
    private String num;
    private Bitmap image;
    private String title;
    private Boolean isProcessing;

    public PreviewInfo(){}
    public PreviewInfo(String num, Bitmap image, String title){
        this.num=num;
        this.image=image;
        this.title=title;
        isProcessing = false;
    }

    public String getNum() {
        return num;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public boolean isProcessing(){return isProcessing;}

    public void setNum(String s) {
        this.num = s;
    }

    public void setImage(Bitmap s) {
        this.image = s;
    }

    public void setTitle(String s) {
        this.title = s;
    }

    public void setProcessing(Boolean processing) {
        isProcessing = processing;
    }
}
