package com.example.oliveyoung;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * MainActivity - 하이브리드 버전
 * - Temi 로봇: 완전한 기능
 * - 에뮬레이터: UI만 작동
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Object robot; // Temi Robot (리플렉션)
    private boolean isTemiAvailable = false;

    private ViewPager2 viewPager;
    private LinearLayout logoLayout;
    private LinearLayout buttonContainer;

    // 더블 탭 종료
    private long backPressedTime = 0;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 화면 꺼짐 방지
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Temi SDK 초기화 (있으면 사용)
        initTemiRobot();

        viewPager = findViewById(R.id.viewPager);
        logoLayout = findViewById(R.id.logoLayout);
        buttonContainer = findViewById(R.id.buttonContainer);

        FragmentAdapter fragmentAdapter = new FragmentAdapter(this);
        viewPager.setAdapter(fragmentAdapter);

        CardView cardFollow = findViewById(R.id.cardFollow);
        CardView cardSearch = findViewById(R.id.cardSearch);
        CardView cardCheckout = findViewById(R.id.cardCheckout);

        cardFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoLayout.setVisibility(View.GONE);
                buttonContainer.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                viewPager.setCurrentItem(0);
            }
        });

        cardSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoLayout.setVisibility(View.GONE);
                buttonContainer.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                viewPager.setCurrentItem(1);
            }
        });

        cardCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoLayout.setVisibility(View.GONE);
                buttonContainer.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                viewPager.setCurrentItem(2);
            }
        });
    }

    /**
     * Temi SDK 초기화 (리플렉션)
     */
    private void initTemiRobot() {
        try {
            Class<?> robotClass = Class.forName("com.robotemi.sdk.Robot");
            java.lang.reflect.Method getInstance = robotClass.getMethod("getInstance");
            robot = getInstance.invoke(null);
            isTemiAvailable = true;

            Log.d(TAG, "✅ Temi SDK 감지 성공!");
            Toast.makeText(this, "✅ Temi 로봇 연결됨", Toast.LENGTH_SHORT).show();

            // 환영 인사
            speak("안녕하세요! 올리브영에 오신 걸 환영합니다.");

        } catch (Exception e) {
            robot = null;
            isTemiAvailable = false;
            Log.d(TAG, "ℹ️ Temi SDK 없음 - 에뮬레이터 모드");
            Toast.makeText(this, "ℹ️ 에뮬레이터 모드", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * TTS 음성 (Temi 전용)
     */
    private void speak(String text) {
        if (!isTemiAvailable || robot == null) {
            return;
        }

        try {
            Class<?> ttsRequestClass = Class.forName("com.robotemi.sdk.TtsRequest");
            java.lang.reflect.Method create = ttsRequestClass.getMethod("create", String.class, boolean.class);
            Object ttsRequest = create.invoke(null, text, false);

            Class<?> robotClass = robot.getClass();
            java.lang.reflect.Method speak = robotClass.getMethod("speak", ttsRequestClass);
            speak.invoke(robot, ttsRequest);

            Log.d(TAG, "✅ [TTS] " + text);
        } catch (Exception e) {
            Log.e(TAG, "TTS 실패", e);
        }
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getVisibility() == View.VISIBLE) {
            // Fragment에서 홈으로
            viewPager.setVisibility(View.GONE);
            logoLayout.setVisibility(View.VISIBLE);
            buttonContainer.setVisibility(View.VISIBLE);
        } else {
            // 홈 화면에서 더블 탭으로 종료
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                if (backToast != null) backToast.cancel();
                super.onBackPressed();
                finish();
            } else {
                backToast = Toast.makeText(this, "뒤로가기 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT);
                backToast.show();
            }
            backPressedTime = System.currentTimeMillis();
        }
    }

    public boolean isTemiAvailable() {
        return isTemiAvailable;
    }

    public Object getRobot() {
        return robot;
    }
}