package com.example.oliveyoung.ui.checkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oliveyoung.R;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutFragment extends Fragment {

    private View buttonBack;
    private View payWrapper;
    private View doneWrapper;

    private RecyclerView recyclerCart;
    private CartAdapter cartAdapter;

    private TextView textTotalPrice;
    private TextView textStatus;
    private ProgressBar progressBar;

    private Robot robot;
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
        return inflater.inflate(R.layout.fragment_checkout, container, false);
    }

    @Override
    public void onViewCreated(
            View view,
            Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        buttonBack = view.findViewById(R.id.buttonBack);
        payWrapper = view.findViewById(R.id.payWrapper);
        doneWrapper = view.findViewById(R.id.doneWrapper);
        recyclerCart = view.findViewById(R.id.recyclerCart);
        textTotalPrice = view.findViewById(R.id.textTotalPrice);
        textStatus = view.findViewById(R.id.textStatus);
        progressBar = view.findViewById(R.id.progressBar);

        buttonBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        recyclerCart.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        cartAdapter = new CartAdapter(new CartAdapter.OnCartChangeListener() {
            @Override
            public void onCartUpdated() {
                updateTotalPrice();
            }
        });
        recyclerCart.setAdapter(cartAdapter);

        // TODO: 지금은 더미 데이터. 나중에 Search 쪽 장바구니 / 서버 데이터 연결
        cartAdapter.setItems(new ArrayList<CartItem>());

        payWrapper.setOnClickListener(v -> onPayClicked());
        doneWrapper.setOnClickListener(v -> onDoneClicked());

        updateTotalPrice();
    }

    private void updateTotalPrice() {
        long total = cartAdapter.getTotalPrice();
        NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
        String totalStr = "총 결제 금액: ₩" + nf.format(total);
        textTotalPrice.setText(totalStr);
    }

    private void onPayClicked() {
        if (cartAdapter.getItemCount() == 0) {
            Toast.makeText(getContext(),
                    "장바구니에 상품이 없습니다.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        textStatus.setText("결제 QR을 생성 중입니다...");
        speak("결제 QR을 생성할게요. 고객님의 휴대폰으로 결제해주세요.");

        // TODO: 실제 QR 이미지 생성 / 결제 서버 연동은 여기에서 구현
        progressBar.setVisibility(View.VISIBLE);

        // 데모용: 바로 로딩 끄기
        progressBar.setVisibility(View.GONE);
    }

    private void onDoneClicked() {
        textStatus.setText("결제가 완료되었습니다. 충전소로 이동할게요.");
        speak("결제가 완료되었습니다. 충전소로 이동하겠습니다.");

        if (robot != null) {
            robot.goTo(BASE_LOCATION_NAME);
        }
    }

    private void speak(String text) {
        if (robot == null) return;
        TtsRequest request = TtsRequest.create(text, false);
        robot.speak(request);
    }

    // 외부(액티비티나 다른 Fragment)에서 장바구니를 넘겨주고 싶을 때 쓸 수 있는 메서드
    public void setCartItems(List<CartItem> items) {
        if (cartAdapter != null) {
            cartAdapter.setItems(items);
        }
    }
}
