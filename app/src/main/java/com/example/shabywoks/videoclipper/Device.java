package com.example.shabywoks.videoclipper;

import android.app.Activity;
import android.util.DisplayMetrics;

public class Device {

    public static Dimension getDeviceDimension(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return new Dimension(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }
}

class Dimension {
    private int width;
    private int height;
    private int depth;

    public Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
