package com.example.paintingprocessing;

import android.content.Context;

import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.recyclerview.widget.RecyclerView;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.VH> {

    public GalleryAdapter( List<PreviewInfo> datas) {
        this.mDatas = datas;
    }
    public static class VH extends RecyclerView.ViewHolder{
        public TextView tv_alg_num, tv_alg_title;
        public ImageView im_alg_mini;
        public Context context;
        public VH(View v) {
            super(v);
            tv_alg_num = v.findViewById(R.id.tv_alg_num);
            tv_alg_title = v.findViewById(R.id.tv_alg_title);
            im_alg_mini = v.findViewById(R.id.im_alg_mini);
            context = v.getContext();
        }
    }

    private List<PreviewInfo> mDatas;
    private Bitmap bm;

    @Override
    public void onBindViewHolder(GalleryAdapter.VH holder, int position) {
        int p=position;
        holder.tv_alg_num.setText(mDatas.get(p).getNum());
        holder.tv_alg_title.setText(mDatas.get(p).getTitle());
        bm = mDatas.get(p).getImage();
        holder.im_alg_mini.setImageBitmap(bm);
//        //根据bitmap长宽比设置imageview大小
//        ViewGroup.LayoutParams params= holder.im_alg_mini.getLayoutParams();
//        params.height = (bm.getHeight()/bm.getWidth())* params.width;
//        holder.im_alg_mini.setLayoutParams(params);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO set an intent for jumping to detail view
                sendToast(holder.context, "item"+p+"clicked!",Toast.LENGTH_SHORT);
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

    void sendToast(Context mContext,String str, int showTime)
    {
        Toast toast = Toast.makeText(mContext, str, showTime);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL , 0, 0);  //设置显示位置
        toast.show();
    }


}
