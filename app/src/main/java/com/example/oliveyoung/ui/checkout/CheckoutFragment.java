package com.example.oliveyoung.ui.checkout;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oliveyoung.R;
import com.example.oliveyoung.api.Product;
import com.example.oliveyoung.ui.checkout.CartAdapter;
import com.example.oliveyoung.ui.checkout.CartItem;
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

    private static final String ORDER_ID = "test_order_1";      // 임시: 현재 세션의 주문 ID
    private static final String BASE_LOCATION_NAME = "충전소";   // Temi 베이스 위치 이름

    private Robot robot;
    private FirebaseFirestore db;

    private TextView textStatus;
    private TextView textTotalPrice;
    private RecyclerView recyclerCart;
    private ImageView imageQr;
    private LinearLayout buttonScan;
    private LinearLayout buttonPay;
    private LinearLayout buttonPaymentDone;
    private LinearLayout btnClearCart;
    private Button buttonBack;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        robot = Robot.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        textStatus = view.findViewById(R.id.textStatus);
        textTotalPrice = view.findViewById(R.id.textTotalPrice);
        recyclerCart = view.findViewById(R.id.recyclerCart);
        imageQr = view.findViewById(R.id.imageQr);
        buttonScan = view.findViewById(R.id.buttonScan);
        buttonPay = view.findViewById(R.id.buttonPay);
        buttonPaymentDone = view.findViewById(R.id.buttonPaymentDone);
        buttonBack = view.findViewById(R.id.buttonBack);
        btnClearCart = view.findViewById(R.id.btnClearCart);

        // 뒤로가기 버튼: MainActivity.onBackPressed() → 홈으로
        buttonBack.setOnClickListener(v -> requireActivity().onBackPressed());

        recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));
        cartAdapter = new CartAdapter();
        recyclerCart.setAdapter(cartAdapter);

        // 어댑터 리스너 설정
        cartAdapter.setOnItemChangeListener(new CartAdapter.OnItemChangeListener() {
            @Override
            public void onItemRemoved(int position) {
                updateTotalPrice();
            }

            @Override
            public void onQuantityChanged() {
                updateTotalPrice();
            }
        });

        // orders/{ORDER_ID} 문서를 실시간으로 구독
        subscribeOrder();

        // 바코드 스캔 버튼 클릭 리스너
        buttonScan.setOnClickListener(v -> {
            textStatus.setText("바코드를 스캔해주세요.");
            speak("바코드를 스캔해주세요.");
            // TODO: 실제 바코드 스캔 기능 구현
        });

        // 전체 비우기 버튼
        btnClearCart.setOnClickListener(v -> {
            cartItems.clear();
            cartAdapter.clearAll();
            updateTotalPrice();
            textStatus.setText("장바구니를 비웠습니다.");
            speak("장바구니를 비웠습니다.");
        });

        buttonPay.setOnClickListener(v -> generatePaymentQr());
        buttonPaymentDone.setOnClickListener(v -> finishPayment());

        return view;
    }

    /**
     * orders/{ORDER_ID} 문서를 snapshot listener로 구독
     */
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

    /**
     * orders 문서의 items 배열을 읽어와서 cartItems를 갱신
     */
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

        // 임시: Firestore join 단순화를 위해 한 번 비우고 다시 로딩
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

    /**
     * products 컬렉션에서 product_id로 상품 정보를 가져와 CartItem으로 추가
     */
    private void loadProductAndAddToCart(String productId, int quantity) {
        // 여기서는 문서 ID == product_id 라고 가정 (추측입니다)
        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    Product product = doc.toObject(Product.class);
                    if (product == null) return;

                    CartItem item = new CartItem(product);
                    // CartItem 기본 quantity=1 이라서 맞춰줌
                    for (int i = 1; i < quantity; i++) {
                        item.increase();
                    }
                    cartItems.add(item);

                    cartAdapter.setItems(cartItems);
                    updateTotalPrice();
                })
                .addOnFailureListener(e -> {
                    // 특정 상품 하나 못 불러온 건 굳이 사용자에게 말 안 해도 됨. 필요하면 로그만.
                });
    }

    private void updateTotalPrice() {
        long total = 0;
        for (CartItem item : cartItems) {
            total += item.getLineTotal();
        }
        NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
        textTotalPrice.setText("₩" + nf.format(total));
    }

    /**
     * 결제하기 버튼: 총 금액 기준으로 QR 생성
     */
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

        // 결제 서버/PG에서 원하는 형식으로 payload 구성
        String payload = "orderId=" + ORDER_ID + "&amount=" + total;

        // TODO: ZXing 등으로 payload → QR Bitmap 생성
        // Bitmap qrBitmap = createQrCodeBitmap(payload);
        // imageQr.setImageBitmap(qrBitmap);

        // (선택) orders/{ORDER_ID}.status = "pending_payment" 로 업데이트
        db.collection("orders").document(ORDER_ID)
                .update("status", "pending_payment");
    }

    /**
     * 결제 완료 버튼: Firestore 상태 변경 + Temi 안내 + 베이스 이동
     */
    private void finishPayment() {
        textStatus.setText("결제가 완료되었습니다. 베이스로 복귀합니다.");
        speak("이용해 주셔서 감사합니다. 이제 베이스로 돌아가겠습니다.");

        // 주문 상태 업데이트
        db.collection("orders").document(ORDER_ID)
                .update("isPaid", true, "status", "paid");

        // 로컬 UI 정리
        cartItems.clear();
        cartAdapter.setItems(cartItems);
        updateTotalPrice();
        imageQr.setImageBitmap(null);

        // Temi 베이스로 이동
        robot.goTo(BASE_LOCATION_NAME);
    }

    private void speak(String text) {
        if (robot == null) return;
        TtsRequest ttsRequest = TtsRequest.create(text, false);
        robot.speak(ttsRequest);
    }
}