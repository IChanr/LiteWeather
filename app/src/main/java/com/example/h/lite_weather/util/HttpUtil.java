package com.example.h.lite_weather.util;

import android.util.Log;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static android.content.ContentValues.TAG;

/**
 * code...
 * Created by H on 2018/2/2.
 */

public class HttpUtil {
    public static String Address = "http://guolin.tech/api/china";
    public static String key = "&key=aa51961b9ab24cee8536259aae4fd6bf";
    /**
     * key 2   21bbdda3aa5a4257b2af3dbc9384dc1c
     */
    private static String WeatherIp = "https://free-api.heweather.com";
    public static String SuggestionIp = WeatherIp + "/s6/weather/lifestyle?location=";
    public static String NowIp = WeatherIp + "/s6/weather/now?location=";
    public static String ForecastIp = WeatherIp + "/s6/weather/forecast?location=";
    public static String GetBingUrl = "http://guolin.tech/api/bing_pic";
    //接口地址 默认返回省份

    //发送请求
    public static void sendOkHttpRequest(String address, Callback callback) {
        Log.d(TAG, "sendOkHttpRequest: 地址+" + address);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
