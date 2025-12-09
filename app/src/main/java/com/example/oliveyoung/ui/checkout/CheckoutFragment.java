package com.example.oliveyoung.ui.checkout;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
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

import android.os.Handler;
import android.os.Looper;


public class CheckoutFragment extends Fragment {

    private static final String TAG = "CheckoutFragment";


    // UI
    private TextView textStatus;
    private TextView textTotalPrice;
    private TextView textItemCount;
    private RecyclerView recyclerCart;
    private LinearLayout buttonScan;
    private LinearLayout buttonPay;
    private LinearLayout buttonPaymentDone;
    private ImageView imageQr;
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

    private final NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);

    // ==============================
    // 자동 갱신 Polling 추가
    // ==============================
    private Handler handler = new Handler();
    private Runnable cartPollingTask = new Runnable() {
        @Override
        public void run() {
            fetchCartFromServer();
            handler.postDelayed(this, 3000); // 3초마다 반복
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        // -------- 뷰 바인딩 --------
        textStatus = view.findViewById(R.id.textStatus);
        textTotalPrice = view.findViewById(R.id.textTotalPrice);
        textItemCount = view.findViewById(R.id.textItemCount);
        recyclerCart = view.findViewById(R.id.recyclerCart);
        buttonScan = view.findViewById(R.id.buttonScan);
        buttonPay = view.findViewById(R.id.buttonPay);
        buttonPaymentDone = view.findViewById(R.id.buttonPaymentDone);
        imageQr = view.findViewById(R.id.imageQr);
        progressBar = view.findViewById(R.id.progressBar);

        View buttonBack = view.findViewById(R.id.buttonBack);
        if (buttonBack != null) {
            buttonBack.setOnClickListener(v -> requireActivity().onBackPressed());
        }

        // -------- Retrofit APIs --------
        cartApi = RetrofitClient.getClient().create(CartApi.class);
        paymentApi = RetrofitClient.getClient().create(PaymentApi.class);

        // -------- RecyclerView --------
        cartAdapter = new CartAdapter();
        recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerCart.setAdapter(cartAdapter);

        // 처음 진입할 때 서버에서 장바구니 불러오기
        fetchCartFromServer();

        // 버튼 리스너
        buttonScan.setOnClickListener(v -> {
            // 바코드 스캔은 외부 아두이노가 담당
            Toast.makeText(getContext(),
                    "바코드 스캔은 외부 스캐너에서 처리됩니다.",
                    Toast.LENGTH_SHORT).show();
        });

        buttonPay.setOnClickListener(v -> startPayment());

        buttonPaymentDone.setOnClickListener(v -> checkPaymentStatus());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(cartPollingTask);  // 화면 보이면 polling 시작
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(cartPollingTask);  // 화면 벗어나면 polling 종료
    }

    // ==============================
    // 1) 서버에서 장바구니 불러오기
    // ==============================
    private void fetchCartFromServer() {
        showLoading(true);
        textStatus.setText("장바구니를 불러오는 중입니다...");

        cartApi.getCurrentCart().enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call,
                                   @NonNull Response<CartResponse> response) {
                showLoading(false);

                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "장바구니 조회 실패: HTTP " + response.code());
                    Toast.makeText(getContext(),
                            "장바구니 조회 실패 (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                    textStatus.setText("장바구니를 불러오지 못했습니다.");
                    return;
                }

                CartResponse body = response.body();
                if (!body.isSuccess() || body.getItems() == null) {
                    Toast.makeText(getContext(),
                            "장바구니가 비어있거나 오류가 발생했습니다.",
                            Toast.LENGTH_SHORT).show();
                    textStatus.setText("장바구니가 비어 있습니다.");
                    cartItems.clear();
                    cartAdapter.setItems(cartItems);
                    updateTotalPriceAndCount();
                    return;
                }

                // RemoteCartItem -> 화면용 CartItem 으로 변환
                cartItems.clear();
                for (RemoteCartItem r : body.getItems()) {

                    // UI에서 사용할 Product 객체 생성
                    Product p = new Product(
                            r.getProductId(),
                            r.getName(),
                            r.getPrice(),
                            null,   // brand  (필요하면 API에 추가)
                            null,   // category
                            null    // imageUrl
                    );

                    CartItem ci = new CartItem(p);
                    // CartItem 은 기본 quantity=1 이라, remote quantity 반영 필요
                    for (int i = 1; i < r.getQuantity(); i++) {
                        ci.increase();
                    }
                    cartItems.add(ci);
                }

                cartAdapter.setItems(cartItems);
                updateTotalPriceAndCount();

                textStatus.setText("바코드를 스캔해서 상품을 담아주세요");
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call,
                                  @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "장바구니 조회 통신 오류: " + t.getMessage(), t);
                Toast.makeText(getContext(),
                        "장바구니 조회 중 오류가 발생했습니다.",
                        Toast.LENGTH_SHORT).show();
                textStatus.setText("장바구니를 불러오는 중 오류가 발생했습니다.");
            }
        });
    }

    /** 합계 & Item n 텍스트 갱신 */
    private void updateTotalPriceAndCount() {
        long total = 0;
        int count = 0;
        for (CartItem item : cartItems) {
            total += item.getLineTotal();
            count += item.getQuantity();
        }

        textTotalPrice.setText("₩" + nf.format(total));
        if (textItemCount != null) {
            textItemCount.setText("Item " + count);
        }
    }

    // ==============================
    // 2) 결제 시작 (/api/payments/initiate)
    // ==============================
    private void startPayment() {
//        if (cartItems.isEmpty()) {
//            Toast.makeText(getContext(), "장바구니가 비어 있습니다.", Toast.LENGTH_SHORT).show();
//            return;
//        }

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
                null,             // customer_id (Temi ID 또는 고객 ID)
                "매장고객",             // customer_name
                null,                  // email
                "00000000000",         // phone
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
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    textStatus.setText("결제 시작에 실패했습니다.");
                    return;
                }

                PaymentInitiateResponse body = response.body();
                if (!body.isSuccess()) {
                    Log.e(TAG, "결제 시작 실패: success=false");
                    Toast.makeText(getContext(), "결제 시작 실패", Toast.LENGTH_SHORT).show();
                    textStatus.setText("결제 시작에 실패했습니다.");
                    return;
                }

                currentOrderId = body.getOrder_id();
                currentCheckoutUrl = body.getCheckout_url();

                textStatus.setText("고객 앱에서 결제를 진행해주세요.\n결제가 끝나면 '결제 완료' 버튼을 눌러주세요.");
                Toast.makeText(getContext(), "결제 QR/URL이 생성되었습니다.", Toast.LENGTH_SHORT).show();

                // ---- QR 코드 생성 & 표시 ----
                String qrData = body.getQr_data();
                if (qrData == null || qrData.isEmpty()) {
                    qrData = currentCheckoutUrl;  // 혹시 qr_data 없으면 URL로 생성
                }

                if (!TextUtils.isEmpty(qrData)) {
                    Bitmap qrBitmap = generateQrCode(qrData, 600);
                    if (qrBitmap != null) {
                        imageQr.setImageBitmap(qrBitmap);
                        imageQr.setVisibility(View.VISIBLE);
                    } else {
                        textStatus.setText("QR 코드 생성 중 오류가 발생했습니다.");
                    }
                } else {
                    textStatus.setText("QR 데이터가 없습니다.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaymentInitiateResponse> call,
                                  @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "결제 시작 통신 오류: " + t.getMessage(), t);
                Toast.makeText(getContext(), "결제 요청 실패: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                textStatus.setText("결제 시작 중 오류가 발생했습니다.");
            }
        });
    }

    // ==============================
    // 3) 결제 완료 확인 (/api/payments/orders/{orderId})
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
                    Log.e(TAG, "주문 조회 실패: HTTP " + response.code());
                    Toast.makeText(getContext(),
                            "주문 조회 실패 (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                OrderResponse body = response.body();
                if (!body.isSuccess() || body.getOrder() == null) {
                    Toast.makeText(getContext(), "주문 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String paymentStatus = body.getOrder().getPayment_status();

                if ("DONE".equals(paymentStatus)) {
                    textStatus.setText("결제가 완료되었습니다. 감사합니다!");
                    Toast.makeText(getContext(), "결제 완료!", Toast.LENGTH_SHORT).show();
                    // TODO: 장바구니 초기화, 홈 이동, /api/cart/clear 호출 등
                } else {
                    textStatus.setText("현재 결제 상태: " + paymentStatus);
                    Toast.makeText(getContext(),
                            "아직 결제가 완료되지 않았습니다.\n상태: " + paymentStatus,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderResponse> call,
                                  @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "주문 조회 통신 오류: " + t.getMessage(), t);
                Toast.makeText(getContext(),
                        "주문 조회 중 오류가 발생했습니다.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==============================
    // 공통 유틸
    // ==============================

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        boolean enable = !show;
        buttonScan.setEnabled(enable);
        buttonPay.setEnabled(enable);
        buttonPaymentDone.setEnabled(enable);
    }

    /** QR 코드 비트맵 생성 */
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
            Log.e(TAG, "QR 코드 생성 실패: " + e.getMessage(), e);
            return null;
        }
    }
}
