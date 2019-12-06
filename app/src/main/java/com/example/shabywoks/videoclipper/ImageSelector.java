package com.example.shabywoks.videoclipper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImageSelector extends AppCompatActivity implements
        SelectImageDisplayAdapter.SelectImageDisplayHolder.closeClickListener,
        SelectImageDisplayAdapter.SelectImageDisplayHolder.imageClickListener
{

    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    HashMap<String, Fragment> fragmentHashMap = new HashMap<>();

    RecyclerView selectImgRV;
    LinearLayoutManager layoutManager;
    SelectImageDisplayAdapter mAdapter;

    Button proceed;

    private List<String> imagePaths;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_selector);

        viewPager       = (ViewPager) findViewById(R.id.viewPager);
        tabLayout       = (TabLayout) findViewById(R.id.tabLayout);
        proceed         = (Button) findViewById(R.id.proceed);
        proceed.setOnClickListener(proceedListener);

        selectImgRV     = (RecyclerView) findViewById(R.id.select_img_holder_rv);
        selectImgRV.setHasFixedSize(true);

        layoutManager   = new LinearLayoutManager(this, 0, false);
        selectImgRV.setLayoutManager(layoutManager);

        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);

    }

    public void setTabs() {
        GalleryFragment gf = new GalleryFragment();
        CameraFragment cf = new CameraFragment();

        fragmentHashMap.put("Camera", cf);
        fragmentHashMap.put("Gallery", gf);

        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(cf, "Camera");
        adapter.addFragment(gf, "Gallery");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void setSelectedImagesDisplay() {
        Dimension d = Device.getDeviceDimension(this);
        d.setHeight(Math.max(d.getHeight()/4, 100));
        d.setWidth(d.getHeight());

        System.out.println("Selected Image: " + selectImgRV.getHeight());
        mAdapter = new SelectImageDisplayAdapter(ImageHolder.getAll(), d, this, this);
        selectImgRV.setAdapter(mAdapter);
    }

    public void update() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        PermissionManager pm = new PermissionManager(this);
        if(!pm.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, null)) {
            String[] s = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE };
            pm.getPermissions(s, null, null, 213);
        } else {
            this.setTabs();
            this.setSelectedImagesDisplay();
        }

        sharedPreferences       = this.getPreferences(Context.MODE_PRIVATE);
        String editedImage      = sharedPreferences.getString("edit_image", null);
        int editedImageIndex    = sharedPreferences.getInt("edit_image_index", -1);


        if (editedImage != null) {
            try {
                Bitmap bmp = this.getImageFromUri(Uri.parse(editedImage));
                ImageHolder.setBitmap(editedImageIndex, bmp);
            } catch (Exception e) {

            }

        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 213:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    this.setTabs();
                    this.setSelectedImagesDisplay();

                } else {
                    Toast.makeText(this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void closeClicked(int pos) {

        String uri = ImageHolder.removeImage(pos);
        this.update();

    }

    Uri pUri;

    @Override
    public void imageClicked(int pos) {
        String  path = ImageHolder.getImageUri(pos);
                pUri = path.contains("content") ? Uri.parse(path) : null;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("edit_image_index", pos);
        editor.commit();
        if(pUri == null) {
            getURI(path);
        } else {
            openAction(pUri);
        }

    }

    public void getURI(final String path) {
        new Thread() {
            @Override
            public void run() {
                MediaScannerConnection
                    .scanFile(getApplicationContext(),
                    new String[] { path }, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            pUri = uri;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    openAction(pUri);
                                }
                            });
                        }
                    });
            }
        }.start();

    }

    private void openAction(Uri uri) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("edit_image", uri.toString());
        editor.commit();

        Intent editIntent = new Intent(Intent.ACTION_EDIT);
        editIntent.setDataAndType(uri, "image/*");
        editIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(editIntent, null));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("edit_image");
        editor.remove("edit_image_index");
        editor.commit();

    }

    private Bitmap getImageFromUri(Uri uri) throws Exception {
        return MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

    }

    View.OnClickListener proceedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "Implement proceed", Toast.LENGTH_SHORT).show();
        }
    };
}
