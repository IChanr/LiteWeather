package com.example.h.lite_weather;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.h.lite_weather.db.City;
import com.example.h.lite_weather.db.County;
import com.example.h.lite_weather.db.Province;
import com.example.h.lite_weather.util.HttpUtil;
import com.example.h.lite_weather.util.ToastHelper;
import com.example.h.lite_weather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * code...
 * Created by H on 2018/2/3.
 */

public class ChooseAreaFragment extends Fragment {
    private Button mButton;
    private TextView mTextView;
    private ListView mListView;

    private ProgressDialog mProgressDialog;

    private final static int CHOOSE_TYPE_Province = 0;
    private final static int CHOOSE_TYPE_City = 1;
    private final static int CHOOSE_TYPE_County = 2;
    private int CHOOSE_TYPE_Now = 0;

    private ArrayAdapter<String> mStringArrayAdapter;

    private Province chooseProvince;
    private City chooseCity;
    private County chooseCounty;

    private List<String> dataList = new ArrayList<>();
    private List<Province> mProvinceList;
    private List<City> mCityList;
    private List<County> mCountyList;
    private boolean result = false;
    private String LocationID = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        if (getActivity() instanceof MainActivity) {
            LocationID = loadLocationId();
            if (LocationID != null) {
                Intent intent = new Intent(getActivity(), WeatherActivity.class);
                intent.putExtra("locationid", LocationID);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        }
        View view = inflater.inflate(R.layout.choose_area_layout, container, false);
        mButton = view.findViewById(R.id.btn_back);
        mTextView = view.findViewById(R.id.tv_title);
        mListView = view.findViewById(R.id.listview);
        mStringArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, dataList);
        mListView.setAdapter(mStringArrayAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: ");
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CHOOSE_TYPE_Now == CHOOSE_TYPE_County) {
                    queryCity();
                    CHOOSE_TYPE_Now = CHOOSE_TYPE_City;
                } else if (CHOOSE_TYPE_Now == CHOOSE_TYPE_City) {
                    queryProvince();
                    CHOOSE_TYPE_Now = CHOOSE_TYPE_Province;
                }
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (CHOOSE_TYPE_Now == CHOOSE_TYPE_Province) {
                    chooseProvince = mProvinceList.get(i);
                    queryCity();
                } else if (CHOOSE_TYPE_Now == CHOOSE_TYPE_City) {
                    chooseCity = mCityList.get(i);
                    queryCounty();
                } else if (CHOOSE_TYPE_Now == CHOOSE_TYPE_County) {
                    chooseCounty = mCountyList.get(i);
                    savaLocationId();
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("locationid", chooseCounty.getCountyCode());
                        getActivity().startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity weatherActivity = (WeatherActivity) getActivity();
                        weatherActivity.mDrawerLayout.closeDrawers();
                        weatherActivity.mSwipeRefreshLayout.setRefreshing(true);
                        weatherActivity.LocationId=chooseCounty.getCountyCode();
                        weatherActivity.queryServer(weatherActivity.WRATHER_TYPE_SUGGESTION);
                        weatherActivity.queryServer(weatherActivity.WEATHER_TYPE_NOW);
                        weatherActivity.queryServer(weatherActivity.WRATHER_TYPE_FORECAST);
                        Log.d(TAG, "onItemClick: 侧栏碎片");
                    }
                }
            }
        });
        queryProvince();
    }

    private void savaLocationId() {
        SharedPreferences.Editor sharedPreferences = getActivity().getSharedPreferences("config", Context
                .MODE_PRIVATE).edit();
        sharedPreferences.putString("LocationId", chooseCounty.getCountyCode());
        sharedPreferences.commit();
    }

    private String loadLocationId() {
        SharedPreferences editor = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        return editor.getString("LocationId", null);
    }

    public void queryProvince() {
        mButton.setVisibility(View.GONE);
        mTextView.setText("中国");
        mProvinceList = DataSupport.findAll(Province.class);
        if (mProvinceList.size() > 0) {
            dataList.clear();
            Log.d(TAG, "queryProvince: 找到了省份数据" + mProvinceList.size());
            for (Province p : mProvinceList) {
                dataList.add(p.getProvinceName());
            }
            mStringArrayAdapter.notifyDataSetChanged();
            CHOOSE_TYPE_Now = CHOOSE_TYPE_Province;
        } else {
            quertServer(HttpUtil.Address, CHOOSE_TYPE_Province);
        }

        Log.d(TAG, "queryProvince: 执行了查询方法");
    }

    public void queryCity() {
        mButton.setVisibility(View.VISIBLE);
        mTextView.setText(chooseProvince.getProvinceName());
        mCityList = DataSupport.where("provinceId=?", String.valueOf(chooseProvince.getProvinceCode())).find(City
                .class);
        if (mCityList.size() > 0) {
            dataList.clear();
            Log.d(TAG, "queryCity: 市的数量" + mCityList.size());
            for (City c : mCityList) {
                dataList.add(c.getCityName());
            }
            mStringArrayAdapter.notifyDataSetChanged();
            CHOOSE_TYPE_Now = CHOOSE_TYPE_City;
        } else {
            String address = HttpUtil.Address + File.separator + chooseProvince.getProvinceCode();
            quertServer(address, CHOOSE_TYPE_City);
        }

    }

    public void queryCounty() {
        mButton.setVisibility(View.VISIBLE);
        mTextView.setText(chooseCity.getCityName());
        mCountyList = DataSupport.where("cityId=?", String.valueOf(chooseCity.getCityCode())).find(County.class);
        if (mCountyList.size() > 0) {
            Log.d(TAG, "queryCounty: 县的数量" + mCountyList.size());
            dataList.clear();
            for (County c : mCountyList) {
                dataList.add(c.getCountyName());
            }
            mStringArrayAdapter.notifyDataSetChanged();
            CHOOSE_TYPE_Now = CHOOSE_TYPE_County;
        } else {
            String address = HttpUtil.Address + File.separator + chooseProvince.getProvinceCode() + File.separator +
                    chooseCity.getCityCode();
            quertServer(address, CHOOSE_TYPE_County);
        }

    }

    public void quertServer(final String address, final int type) {
        showProgress();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastHelper.showToast("获取失败");
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                switch (type) {
                    case CHOOSE_TYPE_County:
                        result = Utility.handlerCountyResponse(response.body().string(), chooseCity.getCityCode());
                        break;
                    case CHOOSE_TYPE_City:
                        result = Utility.handlerCityResponse(response.body().string(), chooseProvince.getProvinceCode
                                ());
                        break;
                    case CHOOSE_TYPE_Province:
                        result = Utility.handlerProvinceResponce(response.body().string());
                        break;
                    default:
                        break;
                }
                if (result) {
                    dismissProgress();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (type) {
                                case CHOOSE_TYPE_County:
                                    queryCounty();
                                    break;
                                case CHOOSE_TYPE_City:
                                    queryCity();
                                    break;
                                case CHOOSE_TYPE_Province:
                                    queryProvince();
                                    break;
                                default:
                                    break;
                            }
                        }
                    });

                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastHelper.showToast("应该是省市县的解析出了问题");
                            dismissProgress();
                        }
                    });
                }
            }
        });
    }

    public void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
        }
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    public void dismissProgress() {
        mProgressDialog.dismiss();
    }
}
