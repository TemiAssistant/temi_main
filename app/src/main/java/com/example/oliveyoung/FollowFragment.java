package com.example.oliveyoung;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FollowFragment extends Fragment {

    private static final String TAG = "FollowFragment";
    private static final String BASE_LOCATION = "충전소";

    private Object robot;
    private boolean isTemiAvailable = false;

    private View buttonBack;
    private TextView textStatus;

    private View followWrapper;
    private View stopWrapper;
    private View goHomeWrapper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTemiRobot();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_follow, container, false);

        buttonBack = view.findViewById(R.id.buttonBack);
        textStatus = view.findViewById(R.id.textStatus);

        followWrapper = view.findViewById(R.id.followWrapper);
        stopWrapper = view.findViewById(R.id.stopWrapper);
        goHomeWrapper = view.findViewById(R.id.goHomeWrapper);

        // 뒤로가기
        buttonBack.setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().onBackPressed();
        });

        // 초기 상태 메시지
        if (isTemiAvailable) {
            textStatus.setText("✅ Temi 로봇 연결됨 - 따라가기 준비 완료");
            speak("테미가 따라가기 준비를 마쳤습니다.");
        } else {
            textStatus.setText("ℹ️ 에뮬레이터 모드 - 동작은 표시만 됩니다.");
        }

        // 따라가기 실행
        followWrapper.setOnClickListener(v -> startFollowing());

        // 정지
        stopWrapper.setOnClickListener(v -> stopFollowing());

        // 베이스 이동
        goHomeWrapper.setOnClickListener(v -> goToBase());

        return view;
    }

    // Temi SDK 초기화
    private void initTemiRobot() {
        try {
            Class<?> robotClass = Class.forName("com.robotemi.sdk.Robot");
            robot = robotClass.getMethod("getInstance").invoke(null);
            isTemiAvailable = true;
            Log.d(TAG, "✅ Temi SDK 감지 성공");
        } catch (Exception e) {
            robot = null;
            isTemiAvailable = false;
            Log.d(TAG, "ℹ️ Temi 없음 - 에뮬레이터 모드");
        }
    }

    // 따라가기 시작
    private void startFollowing() {
        textStatus.setText("👣 따라가기 시작");

        if (!isTemiAvailable) return;

        try {
            Class<?> robotClass = robot.getClass();
            robotClass.getMethod("startFollowMe").invoke(robot);
            speak("따라가기 시작합니다.");
        } catch (Exception e) {
            Log.e(TAG, "따라가기 시작 실패", e);
        }
    }

    // 정지
    private void stopFollowing() {
        textStatus.setText("⏸ 따라가기 정지");

        if (!isTemiAvailable) return;

        try {
            Class<?> robotClass = robot.getClass();
            robotClass.getMethod("stopMovement").invoke(robot);
            speak("정지합니다.");
        } catch (Exception e) {
            Log.e(TAG, "정지 실패", e);
        }
    }

    // 베이스 이동
    private void goToBase() {
        textStatus.setText("🏠 베이스로 이동중...");

        if (!isTemiAvailable) return;

        try {
            Class<?> robotClass = robot.getClass();
            robotClass.getMethod("goTo", String.class).invoke(robot, BASE_LOCATION);
            speak("베이스로 이동합니다.");
        } catch (Exception e) {
            Log.e(TAG, "베이스 이동 실패", e);
        }
    }

    // TTS
    private void speak(String message) {
        if (!isTemiAvailable || robot == null) return;

        try {
            Class<?> ttsClass = Class.forName("com.robotemi.sdk.TtsRequest");
            Object request = ttsClass
                    .getMethod("create", String.class, boolean.class)
                    .invoke(null, message, false);

            robot.getClass()
                    .getMethod("speak", ttsClass)
                    .invoke(robot, request);

        } catch (Exception e) {
            Log.e(TAG, "TTS 오류", e);
        }
    }
}
