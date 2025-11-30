package com.example.oliveyoung.api;

import com.google.gson.annotations.SerializedName;

public class Stock {

    @SerializedName("current")
    private int current;

    @SerializedName("threshold")
    private int threshold;

    @SerializedName("unit_weight")
    private int unitWeight;

    public int getCurrent() {
        return current;
    }

    public int getThreshold() {
        return threshold;
    }

    public int getUnitWeight() {
        return unitWeight;
    }
}
