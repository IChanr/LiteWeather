package com.example.h.lite_weather.db;

import org.litepal.crud.DataSupport;

/**
 * code...
 * Created by H on 2018/2/2.
 */

public class Province extends DataSupport {
    int id;
    String provinceName;
    int provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
