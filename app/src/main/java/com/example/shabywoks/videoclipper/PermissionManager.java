package com.example.shabywoks.videoclipper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;

import java.util.HashMap;

class PermissionCode {
    private static HashMap<String, Integer> requestCodeStringMap    = new HashMap<>();
    private static HashMap<Integer, String> requestCodeIntegerMap   = new HashMap<>();

    private static int newCodeAvailable = 1;


    public static int getCodeForPermission(String permissionString) {
        if (requestCodeStringMap.containsKey(permissionString)) return requestCodeStringMap.get(permissionString);
        return Integer.MIN_VALUE;
    }

    public static String getCodeForPermissionValue(int permissionCode) {
        if (requestCodeIntegerMap.containsKey(permissionCode)) return requestCodeIntegerMap.get(permissionCode);
        return null;
    }

    public static int createNewCode(String permissionString) {
        if (requestCodeStringMap.containsKey(permissionString)) {
            return Integer.MIN_VALUE;
        } else {
            requestCodeIntegerMap.put(newCodeAvailable, permissionString);
            requestCodeStringMap.put(permissionString, newCodeAvailable ++);
            return requestCodeStringMap.get(permissionString);
        }
    }
}

public class PermissionManager {

    private Context context;

    public PermissionManager(Context context) {
        this.context = context;
    }

    public boolean checkPermission(String permissionString, Context context) {
        Context sbContext = context == null ? this.context : context;
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(sbContext, permissionString) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            return true;

        }
        return false;
    }

    public boolean getPermission(String permissionString, Context context, Fragment fragment) {
        Context sbContext = context == null ? this.context : context;
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int code = PermissionCode.getCodeForPermission(permissionString);
            if (code == Integer.MIN_VALUE) code = PermissionCode.createNewCode(permissionString);
            if (fragment != null) {
                fragment.requestPermissions(new String[] { permissionString }, code);
            }
            else {
                ActivityCompat.requestPermissions((Activity)sbContext, new String[] { permissionString }, code);
            }
        }
        return false;
    }

    public boolean getPermissions(String[] permissionStrings, Context context, Fragment fragment, int code) {
        Context sbContext = context == null ? this.context : context;
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (fragment != null) {
                fragment.requestPermissions(permissionStrings, code);
            }
            else {
                ActivityCompat.requestPermissions((Activity)sbContext, permissionStrings, code);
            }
        }
        return false;
    }

}
