package com.example.oliveyoung;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckoutFragment extends Fragment {

    private static final String ORDER_ID = "test_order_1";
    private static final String BASE_LOCATION_NAME = "충전소";

    private Robot robot;
    private FirebaseFirestore db;

    private Button buttonBack;
    private TextView textStatus;
    private TextView textTotalPrice;
    private RecyclerView recyclerCart;
    private ImageView imageQr;
    private Button buttonScan;
    private Button buttonPay;
    private Button buttonPaymentDone;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        robot = Robot.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        buttonBack = view.findViewById(R.id.buttonBack);
        textStatus = view.findViewById(R.id.textStatus);
        textTotalPrice = view.findViewById(R.id.textTotalPrice);
        recyclerCart = view.findViewById(R.id.recyclerCart);
        imageQr = view.findViewById(R.id.imageQr);
        buttonScan = view.findViewById(R.id.buttonScan);
        buttonPay = view.findViewById(R.id.buttonPay);
        buttonPaymentDone = view.findViewById(R.id.buttonPaymentDone);

        // 뒤로가기 버튼
        buttonBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));
        cartAdapter = new CartAdapter();
        recyclerCart.setAdapter(cartAdapter);

        subscribeOrder();

        buttonScan.setOnClickListener(v -> startBarcodeScanning());
        buttonPay.setOnClickListener(v -> generatePaymentQr());
        buttonPaymentDone.setOnClickListener(v -> finishPayment());

        return view;
    }

    private void subscribeOrder() {
        db.collection("orders")
                .document(ORDER_ID)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        textStatus.setText("주문 정보를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
                        speak("주문 정보를 불러오는 중 오류가 발생했습니다.");
                        return;
                    }
                    if (snapshot == null || !snapshot.exists()) {
                        textStatus.setText("현재 진행 중인 주문이 없습니다.");
                        return;
                    }

                    updateCartFromOrder(snapshot);
                });
    }

    private void updateCartFromOrder(DocumentSnapshot snapshot) {
        Object itemsObj = snapshot.get("items");
        if (!(itemsObj instanceof List)) {
            cartItems.clear();
            cartAdapter.setItems(cartItems);
            updateTotalPrice();
            textStatus.setText("장바구니가 비어 있습니다.");
            return;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemList = (List<Map<String, Object>>) itemsObj;

        cartItems.clear();

        for (Map<String, Object> itemMap : itemList) {
            String productId = null;
            Long quantityLong = 0L;

            if (itemMap.get("product_id") != null) {
                productId = itemMap.get("product_id").toString();
            }
            if (itemMap.get("quantity") instanceof Long) {
                quantityLong = (Long) itemMap.get("quantity");
            } else if (itemMap.get("quantity") instanceof Integer) {
                quantityLong = ((Integer) itemMap.get("quantity")).longValue();
            }

            int quantity = quantityLong.intValue();
            if (TextUtils.isEmpty(productId) || quantity <= 0) continue;

            loadProductAndAddToCart(productId, quantity);
        }
    }

    private void loadProductAndAddToCart(String productId, int quantity) {
        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    Product product = doc.toObject(Product.class);
                    if (product == null) return;

                    CartItem item = new CartItem(product);
                    for (int i = 1; i < quantity; i++) {
                        item.increase();
                    }
                    cartItems.add(item);

                    cartAdapter.setItems(cartItems);
                    updateTotalPrice();
                })
                .addOnFailureListener(e -> {
                    // 로그만
                });
    }

    private void updateTotalPrice() {
        long total = 0;
        for (CartItem item : cartItems) {
            total += item.getLineTotal();
        }
        NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
        textTotalPrice.setText("총 금액: ₩" + nf.format(total));
    }

    private void startBarcodeScanning() {
        textStatus.setText("바코드를 스캔해주세요...");
        textStatus.setVisibility(View.VISIBLE);
        speak("바코드를 스캔해주세요.");
        // TODO: 실제 바코드 스캔 기능 구현
    }

    private void generatePaymentQr() {
        long total = 0;
        for (CartItem item : cartItems) {
            total += item.getLineTotal();
        }

        if (total == 0) {
            textStatus.setText("장바구니에 상품이 없습니다.");
            speak("장바구니에 담긴 상품이 없습니다.");
            return;
        }

        textStatus.setText("총 금액 " + total + "원 결제용 QR을 생성합니다.");
        speak("총 금액 " + total + "원 결제용 QR 코드를 생성합니다.");

        String payload = "orderId=" + ORDER_ID + "&amount=" + total;

        // TODO: ZXing 등으로 payload → QR Bitmap 생성

        db.collection("orders").document(ORDER_ID)
                .update("status", "pending_payment");
    }

    private void finishPayment() {
        textStatus.setText("결제가 완료되었습니다. 베이스로 복귀합니다.");
        speak("이용해 주셔서 감사합니다. 이제 베이스로 돌아가겠습니다.");

        db.collection("orders").document(ORDER_ID)
                .update("isPaid", true, "status", "paid");

        cartItems.clear();
        cartAdapter.setItems(cartItems);
        updateTotalPrice();
        imageQr.setImageBitmap(null);

        robot.goTo(BASE_LOCATION_NAME);
    }

    private void speak(String text) {
        if (robot == null) return;
        TtsRequest ttsRequest = TtsRequest.create(text, false);
        robot.speak(ttsRequest);
    }
}