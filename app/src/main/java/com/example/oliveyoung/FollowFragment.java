package com.example.oliveyoung;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

public class FollowFragment extends Fragment {

    private Robot robot;

    private Button buttonBack;
    private Button buttonFollow;
    private Button buttonStop;
    private Button buttonGoHome;
    private TextView textStatus;

    private static final String BASE_LOCATION_NAME = "충전소";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        robot = Robot.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_follow, container, false);

        buttonBack = view.findViewById(R.id.buttonBack);
        buttonFollow = view.findViewById(R.id.buttonFollow);
        buttonStop = view.findViewById(R.id.buttonStop);
        buttonGoHome = view.findViewById(R.id.buttonGoHome);
        textStatus = view.findViewById(R.id.textStatus);

        // 뒤로가기 버튼
        buttonBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // 따라오기
        buttonFollow.setOnClickListener(v -> {
            robot.beWithMe();
            textStatus.setText("테미가 당신을 따라가기 시작했어요.");
            speak("제가 지금부터 고객님을 따라갈게요.");
        });

        // 멈추기
        buttonStop.setOnClickListener(v -> {
            robot.stopMovement();
            textStatus.setText("테미가 움직임을 멈췄어요.");
            speak("움직임을 멈출게요.");
        });

        // 베이스로 돌아가기
        buttonGoHome.setOnClickListener(v -> {
            textStatus.setText("베이스로 돌아가는 중입니다. 위치 이름: " + BASE_LOCATION_NAME);
            speak("이제 베이스로 돌아갈게요.");
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