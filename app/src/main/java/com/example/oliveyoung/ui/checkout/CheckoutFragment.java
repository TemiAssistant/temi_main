package com.example.oliveyoung.ui.checkout;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.robotemi.sdk.Robot;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oliveyoung.R;
import com.example.oliveyoung.api.CartApi;
import com.example.oliveyoung.api.CartResponse;
import com.example.oliveyoung.api.OrderResponse;
import com.example.oliveyoung.api.PaymentApi;
import com.example.oliveyoung.api.PaymentInitiateRequest;
import com.example.oliveyoung.api.PaymentInitiateResponse;
import com.example.oliveyoung.api.PaymentItem;
import com.example.oliveyoung.api.Product;
import com.example.oliveyoung.api.RemoteCartItem;
import com.example.oliveyoung.api.RetrofitClient;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutFragment extends Fragment {

    private static final String TAG = "CheckoutFragment";

    // ✅ 선택지 A: Temi 1대지만 owner_id는 고정값으로 유지
    private static final String OWNER_ID = "temi";

    // UI
    private TextView textStatus;
    private TextView textTotalPrice;
    private TextView textItemCount;
    private RecyclerView recyclerCart;
    private LinearLayout buttonScan;
    private LinearLayout buttonPay;
    private LinearLayout buttonPaymentDone;

    // ✅ QR 오버레이
    private View qrOverlay;
    private ImageView imageQrLarge;
    private ImageView buttonCloseQr;

    private ProgressBar progressBar;

    // 장바구니 화면용
    private final List<CartItem> cartItems = new ArrayList<>();
    private CartAdapter cartAdapter;

    // API
    private CartApi cartApi;
    private PaymentApi paymentApi;

    // 결제 상태
    private String currentOrderId;
    private String currentCheckoutUrl;

    private Robot temiRobot;

    private final NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);

    private boolean isPolling = false;

    private void startCartPolling() {
        if (isPolling) return;
        isPolling = true;
        handler.post(cartPollingTask); // 즉시 1번 실행 후 3초마다 반복
    }

    private void stopCartPolling() {
        isPolling = false;
        handler.removeCallbacks(cartPollingTask);
    }


    // ✅ Polling
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable cartPollingTask = new Runnable() {
        @Override
        public void run() {
            // QR 오버레이가 떠있을 땐 화면이 결제 모드니까 폴링을 멈추는 게 안전함
            if (!isQrOverlayVisible()) {
                fetchCartFromServer(false);
            }
            handler.postDelayed(this, 3000); // 3초
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        // 뷰 바인딩
        textStatus = view.findViewById(R.id.textStatus);
        textTotalPrice = view.findViewById(R.id.textTotalPrice);
        textItemCount = view.findViewById(R.id.textItemCount);
        recyclerCart = view.findViewById(R.id.recyclerCart);
        buttonScan = view.findViewById(R.id.buttonScan);
        buttonPay = view.findViewById(R.id.buttonPay);
        buttonPaymentDone = view.findViewById(R.id.buttonPaymentDone);

        // ✅ QR 오버레이 바인딩
        qrOverlay = view.findViewById(R.id.qrOverlay);
        imageQrLarge = view.findViewById(R.id.imageQrLarge);
        buttonCloseQr = view.findViewById(R.id.buttonCloseQr);

        temiRobot = Robot.getInstance();

        progressBar = view.findViewById(R.id.progressBar);

        View buttonBack = view.findViewById(R.id.buttonBack);
        if (buttonBack != null) {
            buttonBack.setOnClickListener(v -> requireActivity().onBackPressed());
        }

        // Retrofit
        cartApi = RetrofitClient.getClient().create(CartApi.class);
        paymentApi = RetrofitClient.getClient().create(PaymentApi.class);

        // RecyclerView
        cartAdapter = new CartAdapter();
        recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerCart.setAdapter(cartAdapter);

        // ✅ QR 오버레이: 바깥 터치로 닫히지 않도록 (아무 동작 X)
        if (qrOverlay != null) {
            qrOverlay.setOnClickListener(v -> {
                // do nothing
            });
        }

        // ✅ X 버튼으로만 닫기
        if (buttonCloseQr != null) {
            buttonCloseQr.setOnClickListener(v -> hideQrOverlay());
        }

        // ✅ “X 버튼으로만 닫히게” → 뒤로가기 버튼(하드웨어/제스처)로 오버레이 닫히지 않게 막기
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (isQrOverlayVisible()) {
                            // 오버레이 떠있으면 뒤로가기 무시 (X로만 닫힘)
                            return;
                        }
                        // 오버레이 없으면 정상 뒤로가기
                        setEnabled(false);
                        requireActivity().onBackPressed();
                    }
                }
        );

        // 버튼 리스너
        buttonScan.setOnClickListener(v -> {
            Toast.makeText(
                    getContext(),
                    "바코드 스캔 중입니다.\n장바구니를 자동으로 갱신합니다.",
                    Toast.LENGTH_SHORT
            ).show();

            startCartPolling();
        });


        buttonPay.setOnClickListener(v -> startPayment());
        buttonPaymentDone.setOnClickListener(v -> {
            stopCartPolling();
            if (qrOverlay != null) qrOverlay.setVisibility(View.GONE);
            goToBase();
        });

        // 최초 1회 (초기 진입은 로딩 보여주기)
        fetchCartFromServer(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // handler.post(cartPollingTask);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCartPolling();
        // handler.removeCallbacks(cartPollingTask);
    }

    private void goToBase() {
        try {
            // 위치 저장이 "base" 라는 이름으로 되어 있어야 함
            // (temi에서 Save location으로 base 등록 필요)
            temiRobot.goTo("홈베이스");
            Toast.makeText(getContext(), "베이스로 이동합니다.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "goToBase 실패: " + e.getMessage(), e);
            Toast.makeText(getContext(), "베이스 이동 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isQrOverlayVisible() {
        return qrOverlay != null && qrOverlay.getVisibility() == View.VISIBLE;
    }

    // ==============================
    // 1) Cart 조회: GET /api/cart/{owner_id}
    // ==============================
    private void fetchCartFromServer(boolean showSpinner) {
        if (showSpinner) showLoading(true);

        cartApi.getCart(OWNER_ID).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call,
                                   @NonNull Response<CartResponse> response) {
                if (showSpinner) showLoading(false);

                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "장바구니 조회 실패: HTTP " + response.code());
                    if (!isQrOverlayVisible()) {
                        textStatus.setText("장바구니를 불러오지 못했습니다.");
                    }
                    return;
                }

                CartResponse body = response.body();
                if (!body.isSuccess() || body.getCart() == null || body.getCart().getItems() == null) {
                    if (!isQrOverlayVisible()) {
                        textStatus.setText("장바구니가 비어 있습니다.");
                    }
                    cartItems.clear();
                    cartAdapter.setItems(cartItems);
                    updateTotalPriceAndCount();
                    return;
                }

                List<RemoteCartItem> remoteItems = body.getCart().getItems();

                cartItems.clear();
                for (RemoteCartItem r : remoteItems) {

                    Product p = new Product(
                            r.getProductId(),
                            r.getName(),
                            r.getPrice(),
                            null, // brand
                            null, // category
                            null  // imageUrl
                    );

                    CartItem ci = new CartItem(p);

                    // 수량 반영 (CartItem 기본 1)
                    int q = r.getQuantity();
                    for (int i = 1; i < q; i++) ci.increase();

                    cartItems.add(ci);
                }

                cartAdapter.setItems(cartItems);
                updateTotalPriceAndCount();

                if (!isQrOverlayVisible()) {
                    textStatus.setText("바코드를 스캔해서 상품을 담아주세요");
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call,
                                  @NonNull Throwable t) {
                if (showSpinner) showLoading(false);
                Log.e(TAG, "장바구니 조회 통신 오류: " + t.getMessage(), t);
                if (!isQrOverlayVisible()) {
                    textStatus.setText("장바구니 통신 오류");
                }
            }
        });
    }

    private void updateTotalPriceAndCount() {
        long total = 0;
        int count = 0;
        for (CartItem item : cartItems) {
            total += item.getLineTotal();
            count += item.getQuantity();
        }
        textTotalPrice.setText("₩" + nf.format(total));
        if (textItemCount != null) textItemCount.setText("Item " + count);
    }

    // ==============================
    // 2) 결제 시작: POST /api/payments/initiate
    // ==============================
    private void startPayment() {
        // ✅ 장바구니 비어있어도 결제 흐름(테스트/데모) 되게 하려면 막지 말기
        // (백엔드가 items 빈 배열/amount 0을 허용해야 함)

        List<PaymentItem> paymentItems = new ArrayList<>();
        int totalAmount = 0;

        for (CartItem item : cartItems) {
            Product p = item.getProduct();
            int quantity = item.getQuantity();

            String productId = p.getProductId();
            String name = p.getName();
            int price = p.getPrice();

            totalAmount += price * quantity;

            paymentItems.add(new PaymentItem(
                    productId,
                    name,
                    quantity,
                    price
            ));
        }

        PaymentInitiateRequest request = new PaymentInitiateRequest(
                "매장고객",
                "00000000000",
                paymentItems,
                totalAmount,
                0,
                totalAmount
        );

        showLoading(true);
        textStatus.setText("결제 요청 중입니다...");

        paymentApi.initiatePayment(request).enqueue(new Callback<PaymentInitiateResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaymentInitiateResponse> call,
                                   @NonNull Response<PaymentInitiateResponse> response) {
                showLoading(false);

                if (!response.isSuccessful() || response.body() == null) {
                    String msg = "결제 시작 실패 (HTTP " + response.code() + ")";
                    Log.e(TAG, msg);
                    textStatus.setText("결제 시작 실패");
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    return;
                }

                PaymentInitiateResponse body = response.body();
                if (!body.isSuccess()) {
                    textStatus.setText("결제 시작 실패");
                    Toast.makeText(getContext(), "결제 시작 실패", Toast.LENGTH_SHORT).show();
                    return;
                }

                currentOrderId = body.getOrder_id();
                currentCheckoutUrl = body.getCheckout_url();

                String qrData = body.getQr_data();
                if (TextUtils.isEmpty(qrData)) qrData = currentCheckoutUrl;

                if (TextUtils.isEmpty(qrData)) {
                    textStatus.setText("QR 데이터가 없습니다. 서버 응답을 확인해주세요.");
                    return;
                }

                Bitmap qrBitmap = generateQrCode(qrData, 700);
                if (qrBitmap == null) {
                    textStatus.setText("QR 생성 실패");
                    return;
                }

                // ✅ 여기서부터 “오버레이 방식”으로 표시
                showQrOverlay(qrBitmap);

                // 상태 텍스트는 화면 뒤에 있지만 유지해도 됨
                textStatus.setText("고객 앱에서 QR을 스캔해 결제를 진행해주세요.\n결제가 끝나면 '결제 완료' 버튼을 눌러주세요.");
            }

            @Override
            public void onFailure(@NonNull Call<PaymentInitiateResponse> call,
                                  @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "결제 시작 통신 오류: " + t.getMessage(), t);
                textStatus.setText("결제 요청 실패");
                Toast.makeText(getContext(), "결제 요청 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ QR 오버레이 표시 (X로만 닫힘)
    private void showQrOverlay(Bitmap qrBitmap) {
        if (qrOverlay == null || imageQrLarge == null) return;

        imageQrLarge.setImageBitmap(qrBitmap);
        stopCartPolling();
        qrOverlay.setVisibility(View.VISIBLE);

        // QR 떠있는 동안 조작 방지
        if (buttonScan != null) buttonScan.setEnabled(false);
        if (buttonPay != null) buttonPay.setEnabled(false);
        if (buttonPaymentDone != null) buttonPaymentDone.setEnabled(false);
    }

    private void hideQrOverlay() {
        if (qrOverlay == null) return;

        qrOverlay.setVisibility(View.GONE);
        startCartPolling();

        // 다시 버튼 활성화
        if (buttonScan != null) buttonScan.setEnabled(true);
        if (buttonPay != null) buttonPay.setEnabled(true);
        if (buttonPaymentDone != null) buttonPaymentDone.setEnabled(true);
    }

    // ==============================
    // 3) 주문 조회: GET /api/payments/orders/{order_id}
    // ==============================
    private void checkPaymentStatus() {
        if (TextUtils.isEmpty(currentOrderId)) {
            Toast.makeText(getContext(), "먼저 '결제하기'를 눌러 결제를 시작하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        textStatus.setText("결제 상태를 확인하는 중입니다...");

        paymentApi.getOrder(currentOrderId).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderResponse> call,
                                   @NonNull Response<OrderResponse> response) {
                showLoading(false);

                if (!response.isSuccessful() || response.body() == null) {
                    textStatus.setText("주문 조회 실패");
                    return;
                }

                OrderResponse body = response.body();
                if (!body.isSuccess() || body.getOrder() == null) {
                    textStatus.setText("주문 정보 없음");
                    return;
                }

                String paymentStatus = body.getOrder().getPayment_status();
                if ("DONE".equals(paymentStatus)) {
                    textStatus.setText("결제가 완료되었습니다. 감사합니다!");
                    Toast.makeText(getContext(), "결제 완료!", Toast.LENGTH_SHORT).show();

                    // ✅ QR 오버레이 닫기(열려있다면)
                    if (qrOverlay != null) qrOverlay.setVisibility(View.GONE);

                    // ✅ 폴링 중지
                    stopCartPolling();

                    // ✅ (옵션) 장바구니 비우기
                    clearCartAfterPayment();

                    // ✅ 베이스로 이동
                    goToBase();

                } else {
                    textStatus.setText("현재 결제 상태: " + paymentStatus);
                }

            }

            @Override
            public void onFailure(@NonNull Call<OrderResponse> call,
                                  @NonNull Throwable t) {
                showLoading(false);
                textStatus.setText("주문 조회 통신 오류");
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        // QR 오버레이가 떠있을 땐 버튼은 이미 막혀있으므로 건드리지 않음
        if (isQrOverlayVisible()) return;

        boolean enable = !show;
        if (buttonScan != null) buttonScan.setEnabled(enable);
        if (buttonPay != null) buttonPay.setEnabled(enable);
        if (buttonPaymentDone != null) buttonPaymentDone.setEnabled(enable);
    }

    private Bitmap generateQrCode(String text, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);

            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            Log.e(TAG, "QR 생성 실패: " + e.getMessage(), e);
            return null;
        }
    }

    // (옵션) 결제 완료 후 장바구니 비우기
    private void clearCartAfterPayment() {
        cartApi.clearCart(OWNER_ID).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                fetchCartFromServer(true);
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "장바구니 비우기 실패: " + t.getMessage(), t);
            }
        });
    }
}
