package com.example.paintingprocessing;

import android.content.Context;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.VH> {

    private List<PreviewInfo> mDatas;
    private Bitmap bm;
    private OnItemClickListener listener;

    public GalleryAdapter( List<PreviewInfo> datas) {
        this.mDatas = datas;
    }
    protected static class VH extends RecyclerView.ViewHolder {
        public TextView tv_alg_num, tv_alg_title;
        public ImageView im_alg_mini;
        public Context context;
        public ProgressBar progressBar;

        public VH(View v) {
            super(v);
            tv_alg_num = v.findViewById(R.id.tv_alg_num);
            tv_alg_title = v.findViewById(R.id.tv_alg_title);
            im_alg_mini = v.findViewById(R.id.im_alg_mini);
            progressBar = v.findViewById(R.id.waiting_circular);
            context = v.getContext();
        }

    }


    @Override
    public void onBindViewHolder(GalleryAdapter.VH holder, int position) {
        int p=position;
        holder.tv_alg_num.setText(mDatas.get(p).getNum());
        holder.tv_alg_title.setText(mDatas.get(p).getTitle());

        if(mDatas.get(position).isProcessing()){//展现加载进度条
            holder.im_alg_mini.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.itemView.setClickable(false);
        }else {
            holder.progressBar.setVisibility(View.GONE);
            holder.im_alg_mini.setVisibility(View.VISIBLE);
            holder.itemView.setClickable(true);
            bm = mDatas.get(p).getImage();
            bm = ThumbnailUtils.extractThumbnail(bm, bm.getWidth() / 2, bm.getHeight() / 2);//压缩图片
            holder.im_alg_mini.setImageBitmap(bm);
            holder.im_alg_mini.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

//        //根据bitmap长宽比设置imageview大小
//        ViewGroup.LayoutParams params= holder.im_alg_mini.getLayoutParams();
//        params.height = (bm.getHeight()/bm.getWidth())* params.width;
//        holder.im_alg_mini.setLayoutParams(params);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendToast(holder.context, "item"+p+"clicked!",Toast.LENGTH_SHORT);
                if(listener!=null){
                    Log.e("Gallery","ItemClicked!");
                    listener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public GalleryAdapter.VH onCreateViewHolder(ViewGroup parent, int viewType) {
        //LayoutInflater.from指定写法
//         FragmentHomeBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),R.layout.fragment_home,parent,false);
        View v = View.inflate(parent.getContext(), R.layout.gallery_item, null);
        return new GalleryAdapter.VH(v);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listenser) {
        this.listener = listenser;
    }

    void sendToast(Context mContext,String str, int showTime)
    {
        Toast toast = Toast.makeText(mContext, str, showTime);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL , 0, 0);  //设置显示位置
        toast.show();
    }
}
