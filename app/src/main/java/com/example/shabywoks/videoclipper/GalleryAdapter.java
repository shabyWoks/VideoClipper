package com.example.shabywoks.videoclipper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private List<GalleryImageHolder>    imageList;
    private Dimension       dimension;
    private Activity        activity;

    GalleryViewHolder.OnItemClickListener itemClickListener;

    public GalleryAdapter(Activity activity,
                            List<GalleryImageHolder> imageList,
                          Dimension d,
                          GalleryViewHolder.OnItemClickListener itemClickListener) {

        this.itemClickListener  = itemClickListener;
        this.imageList          = imageList;
        this.dimension          = d;
        this.activity           = activity;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v                  = inflater.inflate(R.layout.gallery_grid, viewGroup, false);
        GalleryViewHolder gvh   = new GalleryViewHolder(v, this.itemClickListener1);
        return gvh;
    }

    GalleryViewHolder.OnItemClickListener itemClickListener1 = new GalleryViewHolder.OnItemClickListener() {
        @Override
        public void onItemClicked(ImageView imageView, int position) {
            itemClickListener.onItemClicked(imageView, position);
        }
    };


    @Override
    public void onBindViewHolder(@NonNull final GalleryViewHolder holder, final int position) {
//        new Thread() {
//            @Override
//            public void run() {
//                File imgFile = new File(imageList.get(position));
//                if (imgFile.exists()) {
//                    final Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
                            holder.imageView.setImageBitmap(imageList.get(position).bmp);
//                        }
//                    });
//
//                }
//            }
//        }.start();


        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.height = dimension.getHeight();
        params.width  = dimension.getWidth();

        holder.relativeLayout.setLayoutParams(params);
        holder.setPosition(position);

    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }



    public static class GalleryViewHolder extends RecyclerView.ViewHolder {

        ImageView           imageView;
        RelativeLayout      relativeLayout;
        RelativeLayout      selectLayer;
        int                 position;
        Boolean             checked         = false;

        OnItemClickListener onItemClickListener;

        public GalleryViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);

            this.position               = position;
            this.onItemClickListener    = onItemClickListener;

            imageView       = (ImageView)itemView.findViewById(R.id.gallery_grid_image);
            relativeLayout  = (RelativeLayout)itemView.findViewById(R.id.gallery_grid_rel_lay);
            selectLayer     = (RelativeLayout)itemView.findViewById(R.id.select_layer);

            relativeLayout.setOnClickListener(layoutClickListener);
        }

        View.OnClickListener layoutClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClicked(imageView, position);
            }
        };

        public void setPosition(int position) {
            this.position = position;
        }

        private void setChecked() {
            checked = true;
            selectLayer.setBackgroundColor(Color.argb(150, 255, 255, 255));
        }

        private void setUnChecked() {
            checked = false;
            selectLayer.setBackground(null);
        }

        public interface OnItemClickListener {
            void onItemClicked(ImageView imageView, int position);
        }
    }

}
