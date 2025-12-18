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

    // ✅ 변경: OWNER_ID → DEVICE_ID
    private static final String DEVICE_ID = "temi-001";

    // UI
    private TextView textStatus;
    private TextView textTotalPrice;
    private TextView textItemCount;
    private RecyclerView recyclerCart;
    private LinearLayout buttonScan;
    private LinearLayout buttonPay;
    private LinearLayout buttonPaymentDone;

    // ✅ 변경: QR 오버레이 제거, 단순 ImageView 사용
    private ImageView imageQr;

    // ✅ 추가: 전체 비우기 버튼
    private LinearLayout btnClearCart;

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
        handler.post(cartPollingTask);
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
            fetchCartFromServer(false);
            handler.postDelayed(this, 3000);
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

        // ✅ 변경: 단순 QR ImageView
        imageQr = view.findViewById(R.id.imageQr);

        // ✅ 추가: 전체 비우기 버튼
        btnClearCart = view.findViewById(R.id.btnClearCart);

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
            hideQr();
            goToBase();
        });

        // ✅ 추가: 전체 비우기 버튼 클릭
        if (btnClearCart != null) {
            btnClearCart.setOnClickListener(v -> {
                // 로컬 장바구니만 비우기 (서버 API 없음)
                cartItems.clear();
                cartAdapter.setItems(cartItems);
                updateTotalPriceAndCount();
                hideQr();
                Toast.makeText(getContext(), "장바구니를 비웠습니다.", Toast.LENGTH_SHORT).show();
            });
        }

        // 최초 1회
        fetchCartFromServer(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCartPolling();
    }

    private void goToBase() {
        try {
            temiRobot.goTo("홈베이스");
            Toast.makeText(getContext(), "베이스로 이동합니다.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "goToBase 실패: " + e.getMessage(), e);
            Toast.makeText(getContext(), "베이스 이동 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ==============================
    // 1) Cart 조회: GET /api/cart/current?device_id=temi-001
    // ==============================
    private void fetchCartFromServer(boolean showSpinner) {
        if (showSpinner) showLoading(true);

        // ✅ 변경: getCart() → getCurrentCart()
        cartApi.getCurrentCart(DEVICE_ID).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call,
                                   @NonNull Response<CartResponse> response) {
                if (showSpinner) showLoading(false);

                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "장바구니 조회 실패: HTTP " + response.code());
                    textStatus.setText("장바구니를 불러오지 못했습니다.");
                    return;
                }

                CartResponse body = response.body();

                // ✅ 변경: body.getCart() 제거, 직접 접근
                if (!body.isSuccess() || body.getItems() == null) {
                    textStatus.setText("장바구니가 비어 있습니다.");
                    cartItems.clear();
                    cartAdapter.setItems(cartItems);
                    updateTotalPriceAndCount();
                    return;
                }

                // ✅ 변경: body.getCart().getItems() → body.getItems()
                List<RemoteCartItem> remoteItems = body.getItems();

                cartItems.clear();
                for (RemoteCartItem r : remoteItems) {

                    // ✅ 변경: r.getProductId() → r.getProduct_id()
                    Product p = new Product(
                            r.getProduct_id(),
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

                textStatus.setText("바코드를 스캔해서 상품을 담아주세요");
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call,
                                  @NonNull Throwable t) {
                if (showSpinner) showLoading(false);
                Log.e(TAG, "장바구니 조회 통신 오류: " + t.getMessage(), t);
                textStatus.setText("장바구니 통신 오류");
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

        // ✅ 변경: 새로운 생성자 사용 (customer_id, customer_email 추가)
        PaymentInitiateRequest request = new PaymentInitiateRequest(
                "guest_" + DEVICE_ID,   // customer_id
                "매장고객",              // customer_name
                "guest@oliveyoung.com", // customer_email
                "00000000000",          // customer_phone
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

                Bitmap qrBitmap = generateQrCode(qrData, 500);
                if (qrBitmap == null) {
                    textStatus.setText("QR 생성 실패");
                    return;
                }

                // ✅ 변경: 오버레이 대신 단순 ImageView 표시
                showQr(qrBitmap);
                textStatus.setText("고객 앱에서 QR을 스캔해 결제를 진행해주세요.\n결제가 끝나면 '쇼핑 완료' 버튼을 눌러주세요.");
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

    // ✅ 변경: 단순 QR 이미지 표시
    private void showQr(Bitmap qrBitmap) {
        if (imageQr == null) return;
        imageQr.setImageBitmap(qrBitmap);
        imageQr.setVisibility(View.VISIBLE);
    }

    private void hideQr() {
        if (imageQr == null) return;
        imageQr.setVisibility(View.GONE);
    }

    // ==============================
    // 3) 주문 조회: GET /api/payments/orders/{orderId}
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

                    hideQr();
                    stopCartPolling();

                    // 결제 완료 후 장바구니 새로고침
                    fetchCartFromServer(true);

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

        boolean enable = !show;
        if (buttonScan != null) buttonScan.setEnabled(enable);
        if (buttonPay != null) buttonPay.setEnabled(enable);
        if (buttonPaymentDone != null) buttonPaymentDone.setEnabled(enable);
        if (btnClearCart != null) btnClearCart.setEnabled(enable);
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
}