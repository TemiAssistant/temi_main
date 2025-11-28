package com.example.oliveyoung;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CheckoutFragment - 하이브리드 최종 버전
 * ✅ Temi 로봇: 완전한 기능 (음성 + 이동)
 * ✅ 에뮬레이터: UI 테스트 가능
 */
public class CheckoutFragment extends Fragment {

    private static final String TAG = "CheckoutFragment";
    private static final String BASE_LOCATION = "충전소";

    private Object robot; // Temi Robot
    private boolean isTemiAvailable = false;

    private Button buttonBack;
    private TextView textStatus;
    private TextView textTotalPrice;
    private RecyclerView recyclerCart;
    private ImageView imageQr;
    private Button buttonScan;
    private Button buttonPay;
    private Button buttonPaymentDone;
    private ProgressBar progressBar;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Temi SDK 초기화
        initTemiRobot();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        // View 초기화
        buttonBack = view.findViewById(R.id.buttonBack);
        textStatus = view.findViewById(R.id.textStatus);
        textTotalPrice = view.findViewById(R.id.textTotalPrice);
        recyclerCart = view.findViewById(R.id.recyclerCart);
        imageQr = view.findViewById(R.id.imageQr);
        buttonScan = view.findViewById(R.id.buttonScan);
        buttonPay = view.findViewById(R.id.buttonPay);
        buttonPaymentDone = view.findViewById(R.id.buttonPaymentDone);
        progressBar = view.findViewById(R.id.progressBar);

        // 뒤로가기 버튼
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        // RecyclerView 설정
        recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));
        cartAdapter = new CartAdapter();
        recyclerCart.setAdapter(cartAdapter);

        // 초기 상태 메시지
        if (isTemiAvailable) {
            textStatus.setText("✅ Temi 로봇 연결됨 - 준비 완료");
            speak("올리브영 결제 시스템이 준비되었습니다.");
        } else {
            textStatus.setText("ℹ️ 에뮬레이터 모드 - UI 테스트 가능");
        }

        // 버튼 이벤트
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBarcodeScanning();
            }
        });

        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePaymentQr();
            }
        });

        buttonPaymentDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishPayment();
            }
        });

        return view;
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
        } catch (Exception e) {
            robot = null;
            isTemiAvailable = false;
            Log.d(TAG, "ℹ️ Temi SDK 없음 - 에뮬레이터 모드");
        }
    }

    private void updateTotalPrice() {
        long total = 0;
        for (CartItem item : cartItems) {
            total += item.getLineTotal();
        }
        NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
        textTotalPrice.setText("총 금액: ₩" + nf.format(total));
    }

    // ==================== 바코드 스캔 기능 ====================

    private void startBarcodeScanning() {
        textStatus.setText("바코드 스캔: 설화수 자음생 에센스 추가 중...");

        if (isTemiAvailable) {
            speak("설화수 자음생 에센스 상품을 장바구니에 담았습니다.");
        }

        addMockProduct("8801234567890");
    }

    /**
     * Mock 상품 추가
     */
    private void addMockProduct(String barcode) {
        Product mockProduct = createMockProduct(barcode);

        if (mockProduct == null) {
            textStatus.setText("해당 바코드의 상품을 찾을 수 없습니다: " + barcode);
            if (getContext() != null) {
                Toast.makeText(getContext(), "등록되지 않은 바코드", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // 기존 상품 있으면 수량 증가
        boolean found = false;
        for (CartItem item : cartItems) {
            if (item.getProduct().getProduct_id().equals(mockProduct.getProduct_id())) {
                item.increase();
                found = true;
                break;
            }
        }

        // 새 상품이면 추가
        if (!found) {
            CartItem newItem = new CartItem(mockProduct);
            cartItems.add(newItem);
        }

        cartAdapter.setItems(cartItems);
        updateTotalPrice();

        String message = mockProduct.getName() + " 상품이 장바구니에 추가되었습니다.";
        textStatus.setText(message);

        if (getContext() != null) {
            Toast.makeText(getContext(), "✅ 장바구니에 추가됨", Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "✅ 상품 추가: " + mockProduct.getName());
    }

    /**
     * Mock 상품 데이터베이스
     */
    private Product createMockProduct(String barcode) {
        if (barcode.equals("8801234567890")) {
            return createProductObject("8801234567890", "설화수 자음생 에센스",
                    "설화수", "스킨케어", "에센스", 85000L, 95000L, 10L, "A");
        } else if (barcode.equals("8801234567891")) {
            return createProductObject("8801234567891", "라네즈 워터 슬리핑 마스크",
                    "라네즈", "스킨케어", "마스크팩", 22000L, 25000L, 12L, "B");
        } else if (barcode.equals("8801234567892")) {
            return createProductObject("8801234567892", "헤라 블랙 쿠션",
                    "헤라", "메이크업", "쿠션", 45000L, 52000L, 13L, "C");
        } else if (barcode.equals("8801234567893")) {
            return createProductObject("8801234567893", "이니스프리 그린티 세럼",
                    "이니스프리", "스킨케어", "세럼", 28000L, 32000L, 12L, "A");
        } else if (barcode.equals("1234567890123")) {
            return createProductObject("1234567890123", "테스트 상품",
                    "테스트 브랜드", "기타", "테스트", 10000L, 15000L, 33L, "D");
        }

        return null;
    }

    /**
     * Product 객체 생성
     */
    private Product createProductObject(String productId, String name, String brand,
                                        String category, String subCategory, long price,
                                        long originalPrice, long discountRate, String zone) {
        Product product = new Product();
        try {
            Field field;

            field = Product.class.getDeclaredField("product_id");
            field.setAccessible(true);
            field.set(product, productId);

            field = Product.class.getDeclaredField("name");
            field.setAccessible(true);
            field.set(product, name);

            field = Product.class.getDeclaredField("brand");
            field.setAccessible(true);
            field.set(product, brand);

            field = Product.class.getDeclaredField("category");
            field.setAccessible(true);
            field.set(product, category);

            field = Product.class.getDeclaredField("sub_category");
            field.setAccessible(true);
            field.set(product, subCategory);

            field = Product.class.getDeclaredField("price");
            field.setAccessible(true);
            field.set(product, price);

            field = Product.class.getDeclaredField("original_price");
            field.setAccessible(true);
            field.set(product, originalPrice);

            field = Product.class.getDeclaredField("discount_rate");
            field.setAccessible(true);
            field.set(product, discountRate);

            Map<String, Object> location = new HashMap<>();
            location.put("zone", zone);
            field = Product.class.getDeclaredField("location");
            field.setAccessible(true);
            field.set(product, location);

        } catch (Exception e) {
            Log.e(TAG, "Product 객체 생성 실패", e);
            return null;
        }
        return product;
    }

    // ==================== QR 코드 생성 ====================

    private void generatePaymentQr() {
        long total = 0;
        for (CartItem item : cartItems) {
            total += item.getLineTotal();
        }

        if (total == 0) {
            textStatus.setText("장바구니에 상품이 없습니다.");
            speak("장바구니에 상품이 없습니다.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        buttonPay.setEnabled(false);

        NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
        String message = "총 금액 " + nf.format(total) + "원";
        textStatus.setText("QR 코드 생성 중... " + message);

        if (isTemiAvailable) {
            speak(message + " 결제용 큐알 코드를 생성합니다.");
        }

        // QR 생성 시뮬레이션
        progressBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                buttonPay.setEnabled(true);
                textStatus.setText("✅ QR 코드 생성 완료");

                if (isTemiAvailable) {
                    speak("큐알 코드 생성이 완료되었습니다.");
                }

                Log.d(TAG, "✅ QR 생성 완료");
            }
        }, 1000);
    }

    private void finishPayment() {
        progressBar.setVisibility(View.VISIBLE);
        buttonPaymentDone.setEnabled(false);

        textStatus.setText("결제 처리 중...");

        if (isTemiAvailable) {
            speak("이용해 주셔서 감사합니다. 이제 베이스로 돌아가겠습니다.");
        }

        // 결제 완료 시뮬레이션
        progressBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                finishPaymentCleanup();
            }
        }, 1000);
    }

    private void finishPaymentCleanup() {
        progressBar.setVisibility(View.GONE);
        buttonPaymentDone.setEnabled(true);

        cartItems.clear();
        cartAdapter.setItems(cartItems);
        updateTotalPrice();
        imageQr.setImageBitmap(null);

        textStatus.setText("✅ 결제가 완료되었습니다. 감사합니다!");

        // Temi 로봇 충전소로 이동
        if (isTemiAvailable && robot != null) {
            try {
                Class<?> robotClass = robot.getClass();
                java.lang.reflect.Method goTo = robotClass.getMethod("goTo", String.class);
                goTo.invoke(robot, BASE_LOCATION);
                Log.d(TAG, "✅ [로봇 이동] " + BASE_LOCATION);
            } catch (Exception e) {
                Log.e(TAG, "로봇 이동 실패", e);
            }
        }

        Log.d(TAG, "✅ 결제 완료");
    }

    /**
     * TTS 음성 (Temi 전용)
     */
    private void speak(String text) {
        if (!isTemiAvailable || robot == null) {
            Log.d(TAG, "ℹ️ [에뮬레이터] TTS 건너뜀: " + text);
            return;
        }

        try {
            Class<?> ttsRequestClass = Class.forName("com.robotemi.sdk.TtsRequest");
            java.lang.reflect.Method create = ttsRequestClass.getMethod("create", String.class, boolean.class);
            Object ttsRequest = create.invoke(null, text, false);

            Class<?> robotClass = robot.getClass();
            java.lang.reflect.Method speak = robotClass.getMethod("speak", ttsRequestClass);
            speak.invoke(robot, ttsRequest);

            Log.d(TAG, "✅ [TTS 음성] " + text);
        } catch (Exception e) {
            Log.e(TAG, "TTS 실패", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "CheckoutFragment 종료");
    }
}