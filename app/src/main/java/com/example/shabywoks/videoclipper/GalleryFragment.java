package com.example.shabywoks.videoclipper;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class GalleryImageHolder {
    Bitmap bmp;
    String path;
}

public class GalleryFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter pAdapter;

    ArrayList<GalleryImageHolder> galleryImageHolders = new ArrayList<>();

    private int MIN_WIDTH   = 240;
    private int MIN_HEIGHT  = 240;

    final private ArrayList<String> imagePaths = new ArrayList<>();

    Dimension rd, sd;

    public GalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_gallery, container, false);

        rd = this.getCorrectDimensions();
        sd = this.getScreenDimensions();

        recyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(getContext(), sd.getWidth() / rd.getWidth());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new GalleryAdapter(this.getActivity(), this.galleryImageHolders, rd, listener);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        this.getImages();

        return v;
    }

    GalleryAdapter.GalleryViewHolder.OnItemClickListener listener = new GalleryAdapter.GalleryViewHolder.OnItemClickListener() {
        @Override
        public void onItemClicked(ImageView imageView, int position) {

            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            if (ImageHolder.findImage(imagePaths.get(position)) != -1) {
                ImageHolder.removeImage(ImageHolder.findImage(imagePaths.get(position)));
            }
            else {
                File imgFile = new File(imagePaths.get(position));

                if(imgFile.exists()){
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    ImageHolder.addImage(myBitmap, imagePaths.get(position));
                }

            }

            ((ImageSelector)getActivity()).update();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    public Dimension getCorrectDimensions() {

        Dimension dd = Device.getDeviceDimension(this.getActivity());

        int count       = 2;
        int checkCount  = 2;
        int testWidth   = dd.getWidth() - 20;
        while((testWidth / checkCount) > this.MIN_WIDTH) {
            if (dd.getWidth() % checkCount == 0) {
                count = checkCount;
            }
            checkCount ++;
        }
        return new Dimension(testWidth/count, testWidth/count);
    }

    public void getImages() {


        new Thread() {
            @Override
            public void run() {
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;


                Cursor cursor;
                int column_index_data, column_index_folder_name;


                String[] projection = { MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

                cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);

                column_index_data           = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                column_index_folder_name    = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

                int count = 0;
                while (cursor.moveToNext()) {
                    count ++;
                    final String PathOfImage = cursor.getString(column_index_data);
                    imagePaths.add(PathOfImage);
                }


                for(int i=0; i<imagePaths.size(); i++) {
                    File imgFile = new File(imagePaths.get(i));
                    if (imgFile.exists()) {
                        final Bitmap myBitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imgFile.getAbsolutePath()), 200, 200);
                        GalleryImageHolder galleryImageHolder = new GalleryImageHolder();
                        galleryImageHolder.bmp = myBitmap;
                        galleryImageHolder.path = imagePaths.get(i);
                        galleryImageHolders.add(galleryImageHolder);
                    }
                    if(i % 50 == 0) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                        }

                    }
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }


            }
        }.start();

    }

    public Dimension getScreenDimensions() {
        return Device.getDeviceDimension(this.getActivity());
    }

}

