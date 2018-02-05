package com.example.h.lite_weather.util;

import android.app.Application;
import android.widget.Toast;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

/**
 * code...
 * Created by H on 2018/2/3.
 */

public class ToastHelper {
    public static void showToast(String data) {
        Toast.makeText(LitePalApplication.getContext(), data, Toast.LENGTH_SHORT).show();
    }
}
