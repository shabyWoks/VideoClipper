package com.example.shabywoks.videoclipper;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;


public class ImageHolder {
    private static ArrayList<Bitmap> uris       = new ArrayList<>();
    private static ArrayList<String> uriString  = new ArrayList<>();

    public static void addImage(Bitmap imageUri, String uriString) {
        ImageHolder.uris.add(imageUri);
        ImageHolder.uriString.add(uriString);
    }

    public static String getImageUri(int pos) {
        if (pos >= uris.size()) return null;
        return uriString.get(pos);
    }

    public static void setBitmap(int pos, Bitmap bmp) {
        uris.set(pos, bmp);
    }

    public static String removeImage(int index) {
        if (index < ImageHolder.uris.size()) {

            ImageHolder.uris.remove(index);
            return ImageHolder.uriString.remove(index);
        }
        return null;
    }

    public static int getCount() {
        return ImageHolder.uris.size();
    }

    public static int findImage(String imageUri) {
        if (imageUri == null) return -1;
        return ImageHolder.uriString.indexOf(imageUri);
    }

    public static ArrayList<Bitmap> getAll() {
        return ImageHolder.uris;
    }

    public static void clearList() {
        ImageHolder.uris.clear();
    }

}
