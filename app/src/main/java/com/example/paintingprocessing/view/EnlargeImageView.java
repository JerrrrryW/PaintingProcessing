package com.example.paintingprocessing.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.appcompat.widget.AppCompatImageView;

/**
*
* author tangxianfeng
* created 2021.8.8
**/
public class EnlargeImageView extends AppCompatImageView implements ViewTreeObserver.OnGlobalLayoutListener, ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener {
    @SuppressWarnings("unused")
    private static final String TAG = "EnlargeImageView";

    /**
     * 最大放大倍数
     */
    public static final float mMaxScale = 5.0f;

    public PointF pointF = new PointF();//初始坐标

    public int MODE;//一开始的状态
    /**
     * 默认缩放
     */
    private float mInitScale = 1.0f;
    /**
     * 双击放大比例
     */
    private final float mMidScale = 2.5f;
    /**
     * 检测缩放手势 多点触控手势识别 独立的类不是GestureDetector的子类
     */
    ScaleGestureDetector mScaleGestureDetector = null;//检测缩放的手势
    /**
     * 检测类似长按啊 轻按啊 拖动 快速滑动 双击啊等等 OnTouch方法虽然也可以
     * 但是对于一些复杂的手势需求自己去通过轨迹时间等等判断很复杂,因此我们采用系统
     * 提供的手势类进行处理
     */
    private final GestureDetector mGestureDetector;
    /**
     * 如果正在缩放中就不向下执行,防止多次双击
     */
    private boolean mIsAutoScaling;
    /**
     * Matrix的对图像的处理
     * Translate 平移变换
     * Rotate 旋转变换
     * Scale 缩放变换
     * Skew 错切变换
     */
    Matrix mScaleMatrix = new Matrix();

    /**
     * 处理矩阵的9个值
     */
    float[] mMartixValue = new float[9];
    private EnlargeClickLister enlargeClickLister;

    public EnlargeImageView(Context context) {
        this(context, null);
    }

    public EnlargeImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setEnlargeClickLister(EnlargeClickLister enlargeClickLister){
        this.enlargeClickLister=enlargeClickLister;
    }

    @SuppressLint("ClickableViewAccessibility")
    public EnlargeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.MATRIX);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        setOnTouchListener(this); //缩放的捕获要建立在setOnTouchListener上
        //符合滑动的距离 它获得的是触发移动事件的最短距离，如果小于这个距离就不触发移动控件，
        //监听双击事件 SimpleOnGestureListener是OnGestureListener接口实现类,
        //使用这个复写需要的方法就可以不用复写所有的方法
        mGestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        //如果正在缩放中就不向下执行,防止多次双击
                        if (mIsAutoScaling) {
                            return true;
                        }
                        //缩放的中心点
                        float x = e.getX();
                        float y = e.getY();
                        if (checkClieckArea(x,y)){
                            return true;
                        }
                        //如果当前缩放值小于这个临界值 则进行放大
                        if (getScale() < mMidScale) {
                            mIsAutoScaling = true;
                            //view中的方法 已x,y为坐标点放大到mMidScale 延时10ms
                            postDelayed(new AutoScaleRunble(mMidScale, x, y), 16);
                        } else {
                            //如果当前缩放值大于这个临界值 则进行缩小操作 缩小到mInitScale
                            mIsAutoScaling = true;
                            postDelayed(new AutoScaleRunble(mInitScale, x, y), 16);
                        }
                        return true;
                    }

                });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    //suppress deprecate warning because i have dealt with it
    @Override
    @SuppressWarnings("deprecation")
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
        getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }


    //--------------------------implement OnTouchListener----------------------------/

    public Matrix StartMatrix = new Matrix();//当前矩阵

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //双击事件进行关联
        if (mGestureDetector.onTouchEvent(event)) {
            //如果是双击的话就直接不向下执行了
            return true;
        }
        //将事件传递给ScaleGestureDetector
        mScaleGestureDetector.onTouchEvent(event);

        switch (event.getAction() & event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG,"onTouch ACTION_DOWN"+event.getX());
                /*按下屏幕触发该事件*/
                MODE = 1;
                StartMatrix.set(getImageMatrix());
                pointF.set(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Log.e(TAG,"onTouch ACTION_POINTER_DOWN");
                /*第二个手指触碰屏幕焦点*/
                MODE = 0;
                break;

            case MotionEvent.ACTION_MOVE:
                Log.e(TAG,"onTouch ACTION_MOVE");
                if (checkClieckArea(event.getX(),event.getY())){
                    return true;
                }
                /*移动*/
                if (MODE == 1) {
                    mScaleMatrix.set(StartMatrix);
                    mScaleMatrix.postTranslate(event.getX() - pointF.x, event.getY() - pointF.y);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (MODE==1&&enlargeClickLister!=null){
                    if (checkClieckArea(event.getX(),event.getY())){
                        enlargeClickLister.clickInBlank();
                    }else {
                        enlargeClickLister.clickInImageView();
                    }

                }
            case MotionEvent.ACTION_POINTER_UP:
                /*手指离开屏幕 失去焦点*/
                MODE = 0;
                pointF.set(event.getX(), event.getY());
                break;
        }
        setImageMatrix(mScaleMatrix);
        return true;
    }

    //----------------------手势implement OnScaleGestureListener------------------------//

    /**
     * 处理图片缩放
     */
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scale = getScale();//当前相对于初始尺寸的缩放（之前matrix中获得）
        Log.e(TAG, "matrix scale---->" + scale);
        float scaleFactor = detector.getScaleFactor();//这个时刻缩放的/当前缩放尺度 （现在手势获取）
        Log.e(TAG, "scaleFactor---->" + scaleFactor);

        if (checkClieckArea(detector.getFocusX(),detector.getFocusY())){
            return true;
        }
        if (getDrawable() == null)
            return true;
        if ((scale < mMaxScale && scaleFactor > 1.0f) //放大
                || (scale > mInitScale && scaleFactor < 1.0f)) {//缩小
            //如果要缩放的值比初始化还要小的话,就按照最小可以缩放的值进行缩放
            if (scaleFactor * scale < mInitScale) {
                scaleFactor = mInitScale / scale;
                Log.e(TAG, "进来了1" + scaleFactor);
            }
            ///如果要缩放的值比最大缩放值还要大,就按照最大可以缩放的值进行缩放
            if (scaleFactor * scale > mMaxScale) {
                scaleFactor = mMaxScale / scale;
                Log.e(TAG, "进来了2---->" + scaleFactor);
            }
            Log.e(TAG, "scaleFactor2---->" + scaleFactor+"        "+detector.getFocusX()+":"+detector.getFocusY());
            //设置缩放比例
            mScaleMatrix.postScale(scaleFactor, scaleFactor,
                    getCenterPonit().x,  getCenterPonit().y);//缩放中心是两手指之间
            setImageMatrix(mScaleMatrix);//通过手势给图片设置缩放
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        //缩放开始一定要返回true该detector是否处理后继的缩放事件。返回false时，不会执行onScale()
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        //缩放结束时
    }

    boolean once = true;

    /**
     * 图片初始化其大小 必须在onAttachedToWindow方法后才能获取宽高
     */
    @Override
    public void onGlobalLayout() {
        if (!once)
            return;
        Drawable d = getDrawable();
        if (d == null)
            return;
        //获取imageview宽高
        int width = getWidth();
        int height = getHeight();

        //获取图片宽高
        int imgWidth = d.getIntrinsicWidth();
        int imgHeight = d.getIntrinsicHeight();

        float scale = 1.0f;

        //如果图片的宽或高大于屏幕，缩放至屏幕的宽或者高
        if (imgWidth > width && imgHeight <= height)
            scale = (float) width / imgWidth;
        if (imgHeight > height && imgWidth <= width)
            scale = (float) height / imgHeight;
        //如果图片宽高都大于屏幕，按比例缩小
        if (imgWidth > width && imgHeight > height)
            scale = Math.min((float) imgWidth / width, (float) imgHeight / height);
        mInitScale = scale;
        //将图片移动至屏幕中心
        mScaleMatrix.postTranslate((width - imgWidth) / 2, (height - imgHeight) / 2);
        mScaleMatrix.postScale(scale, scale, getWidth() / 2, getHeight() / 2);
        setImageMatrix(mScaleMatrix);
        once = false;
    }

    /**
     * 获取当前缩放比例
     */
    public float getScale() {
        //Matrix为一个3*3的矩阵，一共9个值,复制到这个数组当中
        mScaleMatrix.getValues(mMartixValue);
        return mMartixValue[Matrix.MSCALE_X];//取出图片宽度的缩放比例
    }

    /**
     * 获得图片放大缩小以后的宽和高，以及l,r,t,b
     */
    private RectF getMatrixRectF() {
        Matrix rMatrix = mScaleMatrix;//获得当前图片的矩阵
        RectF rectF = new RectF();//创建一个空矩形
        Drawable d = getDrawable();

        if (d != null) {
            //使这个矩形的宽和高同当前图片一致
            //设置坐标位置(l和r是左边矩形的坐标点 tb是右边矩形的坐标点 lr设置为0就是设置为原宽高)
            rectF.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            //将矩阵映射到矩形上面，之后我们可以通过获取到矩阵的上下左右坐标以及宽高
            //来得到缩放后图片的上下左右坐标和宽高
            rMatrix.mapRect(rectF);//把坐标位置放入矩阵
        }
        return rectF;
    }
    /**
     * 缩放以图片中心为主
     */
    private PointF getCenterPonit() {
        RectF rectF=getMatrixRectF();
        return new PointF((rectF.right+rectF.left)/2,(rectF.top+rectF.bottom)/2);
    }

    //点击区域的判断和监听
    private boolean checkClieckArea(float x,float y){
        RectF rectF =getMatrixRectF();
        if ( x < rectF.left || x > rectF.right || y < rectF.top || y > rectF.bottom){
            if (enlargeClickLister!=null){
                enlargeClickLister.touchInBlank();
            }
            return true;
        }
        if (enlargeClickLister!=null){
            enlargeClickLister.touchInImageView();
        }
        return false;
    }


    /**
     * View.postDelay()方法延时执行双击放大缩小 在主线程中运行 没隔16ms给用户产生过渡的效果的
     */
    private class AutoScaleRunble implements Runnable {
        private float mTrgetScale;//缩放目标值
        private float tempScale;//可能是BIGGER可能是SMALLER
        private float BIGGER = 1.07f;
        private float SMALLER = 0.93f;

        //构造传入缩放目标值,缩放的中心点
        public AutoScaleRunble(float mTrgetScale, float x, float y) {
            this.mTrgetScale = mTrgetScale;
            if (getScale() < mTrgetScale) {//双击放大
                //这个缩放比1f大就行 随便取个1.07
                tempScale = BIGGER;
            }
            if (getScale() > mTrgetScale) {//双击缩小
                //这个缩放比1f小就行 随便取个0.93
                tempScale = SMALLER;
            }
        }

        @Override
        public void run() {
            //执行缩放
            mScaleMatrix.postScale(tempScale, tempScale, getCenterPonit().x,  getCenterPonit().y);
            //在缩放时，解决上下左右留白的情况
            //checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);
            //获取当前的缩放值
            float currentScale = getScale();
            //如果当前正在放大操作并且当前的放大尺度小于缩放的目标值,或者正在缩小并且缩小的尺度大于目标值
            //则再次延时16ms递归调用直到缩放到目标值
            if ((tempScale > 1.0f && currentScale < mTrgetScale) || (tempScale <
                    1.0f && currentScale > mTrgetScale)) {
                postDelayed(this, 16);
            } else {
                //代码走到这儿来说明不能再进行缩放了，可能放大的尺寸超过了mTrgetScale，
                //也可能缩小的尺寸小于mTrgetScale
                //所以这里我们mTrgetScale / currentScale 用目标缩放尺寸除以当前的缩放尺寸
                //得到缩放比，重新执行缩放到
                //mMidScale或者mInitScale
                float scale = mTrgetScale / currentScale;
                mScaleMatrix.postScale(scale, scale, getCenterPonit().x,  getCenterPonit().y);
                //checkBorderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);
                //执行完成后重置
                mIsAutoScaling = false;
            }
        }
    }
    public interface EnlargeClickLister{
        void touchInImageView();//触碰绘制的图片
        void touchInBlank();//触碰其他空白区域
        void clickInBlank();//点击其他空白区域
        void clickInImageView();//点击绘制的图片
    }
}