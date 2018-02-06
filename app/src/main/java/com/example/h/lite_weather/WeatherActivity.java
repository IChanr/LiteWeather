package com.example.h.lite_weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.h.lite_weather.gson.Forecast_Weather;
import com.example.h.lite_weather.gson.Now_Weather;
import com.example.h.lite_weather.gson.Suggestion_Weather;
import com.example.h.lite_weather.util.HttpUtil;
import com.example.h.lite_weather.util.ToastHelper;
import com.example.h.lite_weather.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public final static int WEATHER_TYPE_NOW = 0;
    public final static int WRATHER_TYPE_FORECAST = 1;
    public final static int WRATHER_TYPE_SUGGESTION = 2;
    public String LocationId = null;
    private Forecast_Weather mForecast_weather;
    private Now_Weather mNow_weather;
    private Suggestion_Weather mSuggestion_weather;

    private TextView tvCityName, tvUpdateTime;
    private TextView tvWeatherC, tvWeatherC2;

    private LinearLayout mLinearLayoutForecast;

    private TextView tvAqi, tvPm2_5;

    private TextView tvSuggestionComfort, tvSuggestionCarWash, tvSuggestionSport;

    private ImageView bing_Pic_img;

    public SwipeRefreshLayout mSwipeRefreshLayout;
    public DrawerLayout mDrawerLayout;
    private Button btnHome;
    private String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            //判断版本是否大于5.0
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //表示活动的布局回显示在状态栏上面
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            //设置状态栏透明
        }
        setContentView(R.layout.activity_weather);
        initView();
        LocationId = getIntent().getStringExtra("locationid");
        loadData();
    }

    private void loadData() {
        SharedPreferences editor = getSharedPreferences("config", MODE_PRIVATE);
        Gson gson = new Gson();
        String weatherNow = editor.getString("now_weather_data", null);
        String weatherSuggestion = editor.getString("suggestion_data", null);
        String weatherForecast = editor.getString("forecast_data", null);
//        String bingUrl = editor.getString("bing_pic", null);
//        if (bingUrl != null) {
//            Glide.with(WeatherActivity.this).load(bingUrl).into(bing_Pic_img);
        //不管有没有都去服务器获取最新的地址
//        } else {
//            loadImg();
//        }
        loadImg();
        if (weatherNow != null) {
            List<Now_Weather> now = gson.fromJson(weatherNow, new TypeToken<List<Now_Weather>>() {
            }.getType());
            if (now.size() > 0) {
                mNow_weather = now.get(0);
                showInfo(WEATHER_TYPE_NOW);
            }
        } else {
            queryServer(WEATHER_TYPE_NOW);
        }

        if (weatherForecast != null) {
            List<Forecast_Weather> forecast = gson.fromJson(weatherForecast, new TypeToken<List<Forecast_Weather>>() {
            }.getType());
            if (forecast.size() > 0) {
                mForecast_weather = forecast.get(0);
                showInfo(WRATHER_TYPE_FORECAST);
            }
        } else {
            queryServer(WRATHER_TYPE_FORECAST);
        }

        if (weatherSuggestion != null) {
            List<Suggestion_Weather> suggestion = gson.fromJson(weatherSuggestion, new
                    TypeToken<List<Suggestion_Weather>>() {
                    }.getType());
            if (suggestion.size() > 0) {
                mSuggestion_weather = suggestion.get(0);
                showInfo(WRATHER_TYPE_SUGGESTION);
            }
        } else {
            queryServer(WRATHER_TYPE_SUGGESTION);
        }

    }

    public void queryServer(final int type) {
        Log.d("TAG", "queryServer: " + "进入查询");
        String ip = null;
        switch (type) {
            case WEATHER_TYPE_NOW:
                ip = HttpUtil.NowIp + LocationId + HttpUtil.key;
                break;
            case WRATHER_TYPE_SUGGESTION:
                ip = HttpUtil.SuggestionIp + LocationId + HttpUtil.key;
                break;
            case WRATHER_TYPE_FORECAST:
                ip = HttpUtil.ForecastIp + LocationId + HttpUtil.key;
                break;
            default:
                break;
        }
        if (ip != null) {
            HttpUtil.sendOkHttpRequest(ip, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "onFailure: 请求获取天气失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
                        boolean result = false;
                        switch (type) {
                            case WRATHER_TYPE_SUGGESTION:
                                result = Utility.handlerWeatherSuggestionResponse(jsonArray.toString());
                                break;
                            case WRATHER_TYPE_FORECAST:
                                result = Utility.handlerWeatherForecastResponse(jsonArray.toString());
                                break;
                            case WEATHER_TYPE_NOW:
                                result = Utility.handlerWeatherNowResponse(jsonArray.toString());
                                break;
                            default:
                                break;
                        }
                        if (result) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "run: 联网查询成功 调用显示数据的方法");
                                    loadData();
                                }
                            });
                        } else {
                            Log.d(TAG, "onResponse: 获取是爱");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void initView() {
        tvCityName = findViewById(R.id.tv_title_city);
        tvUpdateTime = findViewById(R.id.tv_update_time);
        tvWeatherC = findViewById(R.id.tv_weather_c);
        tvWeatherC2 = findViewById(R.id.tv_weather_c2);
        bing_Pic_img = findViewById(R.id.bing_pic_img);
        mLinearLayoutForecast = findViewById(R.id.forecast_layout);
        tvAqi = findViewById(R.id.tv_aqi);
        tvPm2_5 = findViewById(R.id.tv_pm2_5);
        tvSuggestionCarWash = findViewById(R.id.tv_suggestion_carwash);
        tvSuggestionSport = findViewById(R.id.tv_suggestion_sport);
        tvSuggestionComfort = findViewById(R.id.tv_suggestion_comfort);
        mSwipeRefreshLayout = findViewById(R.id.swip_fresh);
        mDrawerLayout = findViewById(R.id.drawerlayout);
        btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryServer(WEATHER_TYPE_NOW);
                queryServer(WRATHER_TYPE_SUGGESTION);
                queryServer(WRATHER_TYPE_FORECAST);

            }
        });
    }

    private void loadImg() {
        HttpUtil.sendOkHttpRequest(HttpUtil.GetBingUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: 获取图片地址失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String url = response.body().string();
                if (url != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: " + url);
                            SharedPreferences.Editor editor = getSharedPreferences("config", MODE_PRIVATE).edit();
                            editor.putString("bing_pic", url);
                            editor.commit();
                            Glide.with(WeatherActivity.this).load(url).into(bing_Pic_img);
                        }
                    });
                }
            }
        });
    }

    private void showInfo(int type) {
        Log.d(TAG, "showInfo: " + "显示信息方法" + type);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
        switch (type) {

            case WRATHER_TYPE_SUGGESTION:
                if (mSuggestion_weather.getStatus().equals("ok")) {
                    String suggestionComf = "舒适度:" + mSuggestion_weather.getLifestyle().get(0).getTxt();
                    String suggestionSport = "运动建议:" + mSuggestion_weather.getLifestyle().get(3).getTxt();
                    String suggestionCarWash = "洗车指数:" + mSuggestion_weather.getLifestyle().get(6).getTxt();
                    tvSuggestionComfort.setText(suggestionComf);
                    tvSuggestionSport.setText(suggestionSport);
                    tvSuggestionCarWash.setText(suggestionCarWash);
                }
                //生活建议
                break;
            case WRATHER_TYPE_FORECAST:
                //天气预报
                if (mForecast_weather.getStatus().equals("ok")) {
                    mLinearLayoutForecast.removeAllViews();
                    for (Forecast_Weather.DailyForecastBean dailyForecastBean : mForecast_weather.getDaily_forecast()) {
                        View view = LayoutInflater.from(this).inflate(R.layout.forecast_item_layout,
                                mLinearLayoutForecast, false);
                        TextView tvTime = view.findViewById(R.id.tv_forecast_item_date);
                        TextView tvMax = view.findViewById(R.id.tv_forecast_item_max);
                        TextView tvMin = view.findViewById(R.id.tv_forecast_item_min);
                        TextView tvInfo = view.findViewById(R.id.tv_forecast_item_info);
                        tvTime.setText(dailyForecastBean.getDate());
                        tvInfo.setText(dailyForecastBean.getCond_txt_n());
                        tvMax.setText(dailyForecastBean.getTmp_max());
                        tvMin.setText(dailyForecastBean.getTmp_min());
                        mLinearLayoutForecast.addView(view);
                    }
                }
                mSwipeRefreshLayout.setRefreshing(false);

                break;
            case WEATHER_TYPE_NOW:
                //当前天气
                if (mNow_weather.getStatus().equals("ok")) {
                    tvCityName.setText(mNow_weather.getBasic().getLocation());
                    String time = mNow_weather.getUpdate().getLoc();
                    tvUpdateTime.setText(time.substring(time.length() - 5));
                    tvWeatherC.setText(mNow_weather.getNow().getTmp());
                    tvWeatherC2.setText(mNow_weather.getNow().getCond_txt());
                }
                break;
            default:
                break;
        }
    }
}
