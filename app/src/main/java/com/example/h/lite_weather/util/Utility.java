package com.example.h.lite_weather.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.example.h.lite_weather.db.City;
import com.example.h.lite_weather.db.County;
import com.example.h.lite_weather.db.Province;
import com.example.h.lite_weather.gson.Now_Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePalApplication;

import static android.content.ContentValues.TAG;

/**
 * code...
 * Created by H on 2018/2/2.
 * 工具类
 */

public class Utility {
    public static boolean handlerProvinceResponce(String data) {
        //判断字符串是否为空  不为空进入循环
        if (!TextUtils.isEmpty(data)) {
            try {
                Log.d(TAG, "handlerProvinceResponce: 数据为" + data);
                JSONArray allProvince = new JSONArray(data);
                for (int i = 0; i < allProvince.length(); i++) {
                    JSONObject jsonObject = allProvince.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(jsonObject.getString("name"));
                    province.setProvinceCode(jsonObject.getInt("id"));
                    province.save();
                    //litepal 的方法  save直接保存到数据库
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handlerCityResponse(String data, int provinceid) {
        if (!TextUtils.isEmpty(data)) {
            try {
                JSONArray jsonArray = new JSONArray(data);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    City city = new City();
                    city.setCityName(jsonObject.getString("name"));
                    city.setCityCode(jsonObject.getInt("id"));
                    city.setProvinceId(provinceid);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handlerCountyResponse(String data, int cityid) {
        if (!TextUtils.isEmpty(data)) {
            try {
                Log.d(TAG, "handlerCountyResponse: 获取到县的数据" + data);
                JSONArray jsonArray = new JSONArray(data);
                Log.d(TAG, "handlerCountyResponse: 县的个数" + jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(jsonObject.getString("name"));
                    county.setCountyCode(jsonObject.getString("weather_id"));
                    county.setCityId(cityid);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handlerWeatherNowResponse(String data) {
        if (!TextUtils.isEmpty(data)) {
            SharedPreferences.Editor editor = LitePalApplication.getContext().getSharedPreferences("config", Context
                    .MODE_PRIVATE).edit();
            editor.putString("now_weather_data", data);
            editor.commit();
            Log.d(TAG, "handlerWeatherNowResponse: 保存信息成功");
            return true;
        }
        return false;
    }

    public static boolean handlerWeatherForecastResponse(String data) {
        if (!TextUtils.isEmpty(data)) {
            SharedPreferences.Editor editor = LitePalApplication.getContext().getSharedPreferences("config", Context
                    .MODE_PRIVATE).edit();
            editor.putString("forecast_data", data);
            editor.commit();
            return true;
        }
        return false;
    }

    public static boolean handlerWeatherSuggestionResponse(String data) {
        if (!TextUtils.isEmpty(data)) {
            SharedPreferences.Editor editor = LitePalApplication.getContext().getSharedPreferences("config", Context
                    .MODE_PRIVATE).edit();
            editor.putString("suggestion_data", data);
            editor.commit();
            return true;
        }
        return false;
    }
}
