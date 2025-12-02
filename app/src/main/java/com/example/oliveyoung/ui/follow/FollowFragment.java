package com.example.oliveyoung.ui.follow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.oliveyoung.R;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

public class FollowFragment extends Fragment {

    private Robot robot;

    private View followWrapper;
    private View stopWrapper;
    private View goHomeWrapper;
    private View buttonBack;
    private TextView textStatus;

    // Temi에서 저장해 둔 베이스(충전소) 위치 이름
    private static final String BASE_LOCATION_NAME = "충전소";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        robot = Robot.getInstance();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_follow, container, false);
    }

    @Override
    public void onViewCreated(
            View view,
            Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        buttonBack = view.findViewById(R.id.buttonBack);
        followWrapper = view.findViewById(R.id.followWrapper);
        stopWrapper = view.findViewById(R.id.stopWrapper);
        goHomeWrapper = view.findViewById(R.id.goHomeWrapper);
        textStatus = view.findViewById(R.id.textStatus);

        buttonBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        followWrapper.setOnClickListener(v -> startFollow());
        stopWrapper.setOnClickListener(v -> stopFollow());
        goHomeWrapper.setOnClickListener(v -> goHome());
    }

    private void startFollow() {
        textStatus.setText("손님을 따라가는 중...");
        speak("고객님을 따라갈게요.");
        if (robot != null) {
            robot.beWithMe();
        } else {
            Toast.makeText(getContext(), "로봇 연결이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopFollow() {
        textStatus.setText("정지했습니다.");
        speak("멈췄습니다.");
        if (robot != null) {
            robot.stopMovement();
        }
    }

    private void goHome() {
        textStatus.setText("충전소로 이동 중...");
        speak("충전소로 이동할게요.");
        if (robot != null) {
            robot.goTo(BASE_LOCATION_NAME);
        }
    }

    private void speak(String text) {
        if (robot == null) return;
        TtsRequest request = TtsRequest.create(text, false);
        robot.speak(request);
    }
}
