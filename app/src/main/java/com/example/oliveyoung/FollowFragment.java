package com.example.oliveyoung;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class FollowFragment extends Fragment {

    private Button buttonBack;
    private Button buttonFollow;
    private Button buttonStop;
    private Button buttonGoHome;
    private TextView textStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_follow, container, false);

        buttonBack = view.findViewById(R.id.buttonBack);
        buttonFollow = view.findViewById(R.id.buttonFollow);
        buttonStop = view.findViewById(R.id.buttonStop);
        buttonGoHome = view.findViewById(R.id.buttonGoHome);
        textStatus = view.findViewById(R.id.textStatus);

        // 초기 버튼 상태
        buttonStop.setEnabled(false);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        buttonFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼 상태 변경
                buttonFollow.setEnabled(false);
                buttonStop.setEnabled(true);
                buttonGoHome.setEnabled(false);

                textStatus.setText("따라가기 기능은 Temi 로봇에서만 작동합니다.");
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Temi 로봇 필요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼 상태 변경
                buttonFollow.setEnabled(true);
                buttonStop.setEnabled(false);
                buttonGoHome.setEnabled(true);

                textStatus.setText("정지되었습니다.");
            }
        });

        buttonGoHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼 상태 변경
                buttonFollow.setEnabled(false);
                buttonStop.setEnabled(true);
                buttonGoHome.setEnabled(false);

                textStatus.setText("베이스 이동 기능은 Temi 로봇에서만 작동합니다.");
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Temi 로봇 필요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}