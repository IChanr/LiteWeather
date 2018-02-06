package com.example.h.lite_weather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.example.h.lite_weather.util.HttpUtil;
import com.example.h.lite_weather.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateBingPic();
        updateWeather();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int Hours = 8 * 60 * 60 * 1000;//八小时刷新一次
        //int Hours = 1000;//八小时刷新一次
        long triggerAtTime = SystemClock.elapsedRealtime() + Hours;
        Intent intent1 = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, intent1, 0);
        alarmManager.cancel(pi);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        String locationId = sharedPreferences.getString("LocationId", null);
        if (locationId != null) {
            HttpUtil.sendOkHttpRequest(HttpUtil.NowIp + locationId + HttpUtil.key, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "onFailure: 后台更新当前天气失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
                        Utility.handlerWeatherNowResponse(jsonArray.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            HttpUtil.sendOkHttpRequest(HttpUtil.ForecastIp + locationId + HttpUtil.key, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "onFailure: 后台更新天气预报失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
                        Utility.handlerWeatherForecastResponse(jsonArray.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            HttpUtil.sendOkHttpRequest(HttpUtil.SuggestionIp + locationId + HttpUtil.key, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "onFailure: 后台更新生活建议失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
                        Utility.handlerWeatherSuggestionResponse(jsonArray.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            Log.d(TAG, "updateWeather: 后台天气更新方法执行完毕");
        }
    }

    private void updateBingPic() {
        HttpUtil.sendOkHttpRequest(HttpUtil.GetBingUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: 后台更新图片失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                SharedPreferences.Editor s = getSharedPreferences("config", MODE_PRIVATE).edit();
                String data = response.body().string();
                if (data != null) {
                    s.putString("bing_pic", data);
                    s.commit();
                    Log.d(TAG, "onResponse: 后台更新图片完成");
                }

            }
        });
    }
}
