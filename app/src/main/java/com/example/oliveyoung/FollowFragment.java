package com.example.oliveyoung;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

public class FollowFragment extends Fragment {

    private Robot robot;

    private Button buttonFollow;
    private Button buttonStop;
    private Button buttonGoHome;
    private TextView textStatus;

    // Temi에서 "베이스(충전소)"로 저장해 둔 위치 이름
    // Temi Settings → Locations 에서 실제 이름을 이 문자열과 맞춰줘야 함
    private static final String BASE_LOCATION_NAME = "충전소"; // 예: "충전소", "home base" 등 네가 저장한 이름으로 바꿔도 됨

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Temi SDK 싱글톤 객체
        robot = Robot.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // fragment_follow.xml 을 inflate
        View view = inflater.inflate(R.layout.fragment_follow, container, false);

        buttonFollow = view.findViewById(R.id.buttonFollow);
        buttonStop = view.findViewById(R.id.buttonStop);
        buttonGoHome = view.findViewById(R.id.buttonGoHome);
        textStatus = view.findViewById(R.id.textStatus);

        // 1) 따라오기
        buttonFollow.setOnClickListener(v -> {
            // Temi가 사람 따라오기 시작
            robot.beWithMe();

            textStatus.setText("테미가 당신을 따라가기 시작했어요.");
            speak("제가 지금부터 고객님을 따라갈게요.");
        });

        // 2) 멈추기
        buttonStop.setOnClickListener(v -> {
            robot.stopMovement();

            textStatus.setText("테미가 움직임을 멈췄어요.");
            speak("움직임을 멈출게요.");
        });

        // 3) 베이스로 돌아가기
        buttonGoHome.setOnClickListener(v -> {
            textStatus.setText("베이스로 돌아가는 중입니다. 위치 이름: " + BASE_LOCATION_NAME);
            speak("이제 베이스로 돌아갈게요.");

            // Temi에 미리 저장해 둔 위치 이름으로 이동
            robot.goTo(BASE_LOCATION_NAME);
        });

        return view;
    }

    private void speak(String text) {
        if (robot == null) return;
        TtsRequest ttsRequest = TtsRequest.create(text, false);
        robot.speak(ttsRequest);
    }
}
