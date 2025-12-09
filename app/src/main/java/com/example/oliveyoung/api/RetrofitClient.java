package com.example.oliveyoung.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://172.17.81.139:8000/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {

        if (retrofit == null) {

            // ✅ TIMEOUT 설정이 매우 중요!
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)  // 서버 연결 대기
                    .readTimeout(60, TimeUnit.SECONDS)     // 서버 응답 대기
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)         // 재시도 허용
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)  // 👈 반드시 추가!
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}
