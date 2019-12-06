package com.example.shabywoks.videoclipper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class SelectImageDisplayAdapter extends RecyclerView.Adapter<SelectImageDisplayAdapter.SelectImageDisplayHolder> {

    private List<Bitmap> images;
    private Dimension dimension;
    SelectImageDisplayHolder.closeClickListener closeClickListener1;
    SelectImageDisplayHolder.imageClickListener imageClickListener1;

    public SelectImageDisplayAdapter(List<Bitmap> images, Dimension dimension,
                                     SelectImageDisplayHolder.closeClickListener closeClickListener1,
                                     SelectImageDisplayHolder.imageClickListener imageClickListener1) {
        this.images = images;
        this.dimension = dimension;
        this.closeClickListener1 = closeClickListener1;
        this.imageClickListener1 = imageClickListener1;
    }

    @NonNull
    @Override
    public SelectImageDisplayHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context         = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v                  = inflater.inflate(R.layout.layout_gallery_image_cancelable, viewGroup, false);
        SelectImageDisplayHolder svdh = new SelectImageDisplayHolder(v, closeClickListener, imageClickListener);
        return svdh;
    }

    @Override
    public void onBindViewHolder(@NonNull SelectImageDisplayHolder selectImageDisplayHolder, int i) {

        selectImageDisplayHolder.setViewPosition(i);
        selectImageDisplayHolder.imageView.setImageBitmap(images.get(i));

        selectImageDisplayHolder.imageView.getLayoutParams().height = dimension.getHeight();
        selectImageDisplayHolder.imageView.getLayoutParams().width = dimension.getWidth();

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.width  = dimension.getWidth();

        RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) selectImageDisplayHolder.relativeLayoutLayer.getLayoutParams();
        params1.width  = dimension.getWidth();
        params1.height = dimension.getHeight();


        selectImageDisplayHolder.relativeLayout.setLayoutParams(params);
        selectImageDisplayHolder.relativeLayoutLayer.setLayoutParams(params1);
    }

    SelectImageDisplayHolder.imageClickListener imageClickListener = new SelectImageDisplayHolder.imageClickListener() {
        @Override
        public void imageClicked(int pos) {
            imageClickListener1.imageClicked(pos);
        }
    };

    SelectImageDisplayHolder.closeClickListener closeClickListener = new SelectImageDisplayHolder.closeClickListener() {
        @Override
        public void closeClicked(int pos) {
            closeClickListener1.closeClicked(pos);
        }
    };

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class SelectImageDisplayHolder extends RecyclerView.ViewHolder {

        ImageView       imageView;
        RelativeLayout  relativeLayout;
        RelativeLayout  relativeLayoutLayer;
        Button close;

        int position;

        closeClickListener listener;
        imageClickListener imageClickListener;

        public SelectImageDisplayHolder(@NonNull View itemView, final closeClickListener listener, final imageClickListener listener1) {
            super(itemView);

            imageView               = (ImageView)itemView.findViewById(R.id.gic_image);
            close                   = (Button)itemView.findViewById(R.id.gic_cancel);
            relativeLayout          = (RelativeLayout)itemView.findViewById(R.id.gic_rel_lay);
            relativeLayoutLayer     = (RelativeLayout)itemView.findViewById(R.id.gic_layer);

            close.setVisibility(View.VISIBLE);

            this.listener           = listener;
            this.imageClickListener = listener1;

            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.closeClicked(position);
                }
            });

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener1.imageClicked(position);
                }
            });
        }

        public void setViewPosition(int position) {
            this.position = position;
        }

        public interface closeClickListener {
            public void closeClicked(int pos);
        }

        public interface imageClickListener {
            public void imageClicked(int pos);
        }
    }
}
