package com.example.oliveyoung.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // 에뮬레이터 기준 (PC에서 FastAPI 서버가 돌아간다고 가정)
    // "localhost" 대신 10.0.2.2 사용
    private static final String BASE_URL = "http://172.17.67.53:8000/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
