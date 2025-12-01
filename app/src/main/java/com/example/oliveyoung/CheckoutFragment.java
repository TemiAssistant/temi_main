package com.example.oliveyoung;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckoutFragment extends Fragment {

    private static final String TAG = "CheckoutFragment";
    private static final String BASE_LOCATION = "충전소";

    // 테스트용 결제 URL
    private static final String PAYMENT_BASE_URL = "https://www.naver.com";

    private Object robot;      // Temi Robot
    private boolean isTemiAvailable = false;

    private View buttonBack;
    private TextView textStatus;
    private TextView textTotalPrice;
    private RecyclerView recyclerCart;
    private ImageView imageQr;
    private ProgressBar progressBar;

    // 하단 버튼 3개(LinearLayout wrapper)
    private View scanWrapper;
    private View payWrapper;
    private View doneWrapper;

    private CartAdapter cartAdapter;
    private final List<CartItem> cartItems = new ArrayList<>();

    // Firestore
    private FirebaseFirestore db;

    // QR 상태 관리용
    private boolean hasActiveQr = false;
    private long lastQrTotal = 0L;
    private String lastOrderId = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTemiRobot();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        // View 초기화
        buttonBack     = view.findViewById(R.id.buttonBack);
        textStatus     = view.findViewById(R.id.textStatus);
        textTotalPrice = view.findViewById(R.id.textTotalPrice);
        recyclerCart   = view.findViewById(R.id.recyclerCart);
        imageQr        = view.findViewById(R.id.imageQr);
        progressBar    = view.findViewById(R.id.progressBar);

        scanWrapper = view.findViewById(R.id.scanWrapper);
        payWrapper  = view.findViewById(R.id.payWrapper);
        doneWrapper = view.findViewById(R.id.doneWrapper);

        // 뒤로가기
        buttonBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        // 장바구니 리스트
        recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));
        cartAdapter = new CartAdapter(this::updateTotalPrice);
        recyclerCart.setAdapter(cartAdapter);

        // 초기 상태 메시지
        if (isTemiAvailable) {
            textStatus.setText("✅ Temi 로봇 연결됨 - 결제 준비 완료");
            speak("올리브영 결제 시스템이 준비되었습니다.");
        } else {
            textStatus.setText("ℹ️ 에뮬레이터 모드 - UI 동작 확인용입니다.");
        }

        // 하단 버튼 클릭 이벤트
        scanWrapper.setOnClickListener(v -> startBarcodeScanning());
        payWrapper.setOnClickListener(v -> generatePaymentQr());
        doneWrapper.setOnClickListener(v -> finishPayment());

        return view;
    }

    // Temi SDK 초기화
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

        // ✅ 장바구니 총액이 바뀌었는데 QR이 이미 있으면 → QR 무효화
        if (hasActiveQr && total != lastQrTotal) {
            clearQrState();
            textStatus.setText("장바구니가 변경되었습니다. 다시 QR을 생성해 주세요.");
        }
    }

    // ✅ QR 관련 상태/뷰 초기화
    private void clearQrState() {
        hasActiveQr = false;
        lastQrTotal = 0L;
        lastOrderId = null;

        if (imageQr != null) {
            imageQr.setImageBitmap(null);
            imageQr.setVisibility(View.GONE);
        }
    }

    // ================= 바코드 스캔 =================

    private void startBarcodeScanning() {
        textStatus.setText("바코드를 스캔해 주세요...");

        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("상품 바코드를 카메라에 맞춰주세요");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                textStatus.setText("바코드 스캔이 취소되었습니다.");
            } else {
                String barcode = result.getContents();
                Log.d(TAG, "✅ 바코드 스캔 결과: " + barcode);
                textStatus.setText("스캔된 바코드: " + barcode);

                // ✅ Firestore에서 상품 찾고 → 장바구니 추가 (없으면 Mock로 fallback)
                handleBarcode(barcode);
            }
        }
    }

    /**
     * 바코드 값으로 Firestore products 컬렉션에서 상품을 찾고,
     * 있으면 실제 상품을 장바구니에 추가,
     * 없으면 mock 매핑으로 시도.
     */
    private void handleBarcode(final String barcode) {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        textStatus.setText("상품 정보를 불러오는 중입니다...");

        db.collection("products")
                .whereEqualTo("barcode", barcode)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                            Product product = createProductFromDocument(doc);

                            if (product != null) {
                                addProductToCart(product);
                            } else {
                                Log.w(TAG, "Firestore 문서 → Product 변환 실패, Mock 사용");
                                addMockProduct(barcode);
                            }
                        } else {
                            Log.d(TAG, "Firestore에 해당 바코드 상품 없음, Mock 사용");
                            addMockProduct(barcode);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "상품 조회 오류, Mock 사용", e);
                        addMockProduct(barcode);
                    }
                });
    }

    /**
     * Firestore DocumentSnapshot → Product 객체로 변환
     * (필드 이름은 Firestore 구조에 맞게 필요하면 수정)
     */
    private Product createProductFromDocument(DocumentSnapshot doc) {
        try {
            Product product = new Product();

            Field field;

            String productId = doc.contains("product_id")
                    ? doc.getString("product_id")
                    : doc.getId();

            field = Product.class.getDeclaredField("product_id");
            field.setAccessible(true);
            field.set(product, productId);

            field = Product.class.getDeclaredField("name");
            field.setAccessible(true);
            field.set(product, doc.getString("name"));

            field = Product.class.getDeclaredField("brand");
            field.setAccessible(true);
            field.set(product, doc.getString("brand"));

            field = Product.class.getDeclaredField("category");
            field.setAccessible(true);
            field.set(product, doc.getString("category"));

            field = Product.class.getDeclaredField("sub_category");
            field.setAccessible(true);
            field.set(product, doc.getString("sub_category"));

            Long price = doc.getLong("price");
            Long originalPrice = doc.getLong("original_price");
            Long discountRate = doc.getLong("discount_rate");

            field = Product.class.getDeclaredField("price");
            field.setAccessible(true);
            field.set(product, price != null ? price : 0L);

            field = Product.class.getDeclaredField("original_price");
            field.setAccessible(true);
            field.set(product, originalPrice != null ? originalPrice : 0L);

            field = Product.class.getDeclaredField("discount_rate");
            field.setAccessible(true);
            field.set(product, discountRate != null ? discountRate : 0L);

            // location 정보가 있으면 그대로 넣고, 없으면 zone만 넣기
            Map<String, Object> location = null;
            Object locObj = doc.get("location");
            if (locObj instanceof Map) {
                //noinspection unchecked
                location = (Map<String, Object>) locObj;
            }
            if (location == null) {
                location = new HashMap<>();
                location.put("zone", "A");
            }

            field = Product.class.getDeclaredField("location");
            field.setAccessible(true);
            field.set(product, location);

            return product;
        } catch (Exception e) {
            Log.e(TAG, "Product 객체 생성 실패 (Firestore 기반)", e);
            return null;
        }
    }

    /**
     * 실제 장바구니에 Product를 추가하는 공통 로직
     */
    private void addProductToCart(Product product) {
        if (product == null) return;

        boolean found = false;
        for (CartItem item : cartItems) {
            if (item.getProduct().getProduct_id().equals(product.getProduct_id())) {
                item.increase();
                found = true;
                break;
            }
        }

        if (!found) {
            cartItems.add(new CartItem(product));
        }

        cartAdapter.setItems(cartItems);
        updateTotalPrice();

        String message = product.getName() + " 상품이 장바구니에 추가되었습니다.";
        textStatus.setText(message);

        if (getContext() != null) {
            Toast.makeText(getContext(), "✅ 장바구니에 추가됨", Toast.LENGTH_SHORT).show();
        }

        if (isTemiAvailable) {
            speak(product.getName() + " 상품을 장바구니에 담았습니다.");
        }

        Log.d(TAG, "✅ 상품 추가: " + product.getName());
    }

    // ================= Mock 상품 (Firestore에 없을 때 fallback) =================

    private void addMockProduct(String barcode) {
        Product mockProduct = createMockProduct(barcode);

        if (mockProduct == null) {
            textStatus.setText("해당 바코드의 상품을 찾을 수 없습니다: " + barcode);
            if (getContext() != null) {
                Toast.makeText(getContext(), "등록되지 않은 바코드", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        addProductToCart(mockProduct);
    }

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

    // ================= QR 코드 생성 =================

    private void generatePaymentQr() {
        long total = 0;
        for (CartItem item : cartItems) {
            total += item.getLineTotal();
        }

        if (total == 0) {
            textStatus.setText("장바구니에 상품이 없습니다.");
            if (isTemiAvailable) {
                speak("장바구니에 상품이 없어서 큐알 코드를 만들 수 없습니다.");
            }
            if (getContext() != null) {
                Toast.makeText(getContext(), "장바구니가 비어 있습니다.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // ✅ 이미 같은 금액으로 QR이 생성되어 있으면, 다시 만들지 않고 안내만
        if (hasActiveQr && total == lastQrTotal) {
            textStatus.setText("이미 이 장바구니 금액으로 생성된 결제 QR 코드가 있습니다.");
            if (isTemiAvailable) {
                speak("이미 생성된 결제 큐알 코드가 있습니다.");
            }
            return;
        }

        setViewEnabled(payWrapper, false);
        progressBar.setVisibility(View.VISIBLE);

        NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
        String message = "총 금액 " + nf.format(total) + "원";
        textStatus.setText("QR 코드 생성 중... " + message);

        if (isTemiAvailable) {
            speak(message + " 결제용 큐알 코드를 생성합니다.");
        }

        // ✅ 새 주문 ID 생성 & 상태 저장
        String orderId = String.valueOf(System.currentTimeMillis());
        lastOrderId = orderId;
        lastQrTotal = total;

        String qrData = PAYMENT_BASE_URL
                + "?orderId=" + orderId
                + "&total=" + total;

        Log.d(TAG, "✅ QR에 들어갈 URL: " + qrData);

        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    qrData,
                    BarcodeFormat.QR_CODE,
                    600,
                    600
            );

            int width = matrix.getWidth();
            int height = matrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            imageQr.setImageBitmap(bitmap);
            imageQr.setVisibility(View.VISIBLE);

            textStatus.setText(
                    "✅ 결제용 QR 코드가 생성되었습니다.\n" +
                            "휴대폰 카메라로 스캔해 결제를 진행해 주세요."
            );

            if (isTemiAvailable) {
                speak("큐알 코드 생성이 완료되었습니다. 휴대폰 카메라로 스캔해 결제를 진행해 주세요.");
            }

            hasActiveQr = true;   // ✅ QR 활성화 상태 기록
            Log.d(TAG, "✅ QR 생성 완료: " + qrData);

        } catch (WriterException e) {
            Log.e(TAG, "QR 코드 생성 실패", e);
            textStatus.setText("QR 코드 생성 중 오류가 발생했습니다.");
            hasActiveQr = false;
            lastQrTotal = 0L;
            lastOrderId = null;
        } finally {
            progressBar.setVisibility(View.GONE);
            setViewEnabled(payWrapper, true);
        }
    }

    private void finishPayment() {
        progressBar.setVisibility(View.VISIBLE);
        setViewEnabled(doneWrapper, false);

        textStatus.setText("결제 처리 중...");

        if (isTemiAvailable) {
            speak("이용해 주셔서 감사합니다. 이제 베이스로 돌아가겠습니다.");
        }

        progressBar.postDelayed(this::finishPaymentCleanup, 1000);
    }

    private void finishPaymentCleanup() {
        progressBar.setVisibility(View.GONE);
        setViewEnabled(doneWrapper, true);

        cartItems.clear();
        cartAdapter.setItems(cartItems);
        updateTotalPrice();

        // ✅ QR 관련 상태 전체 초기화
        clearQrState();

        textStatus.setText("✅ 결제가 완료되었습니다. 감사합니다!");

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

    // 공통: 뷰 활성/비활성 + alpha
    private void setViewEnabled(View v, boolean enabled) {
        if (v == null) return;
        v.setEnabled(enabled);
        v.setAlpha(enabled ? 1f : 0.5f);
    }

    // Temi TTS
    private void speak(String text) {
        if (!isTemiAvailable || robot == null) {
            Log.d(TAG, "ℹ️ [에뮬레이터] TTS 건너뜀: " + text);
            return;
        }

        try {
            Class<?> ttsRequestClass = Class.forName("com.robotemi.sdk.TtsRequest");
            java.lang.reflect.Method create =
                    ttsRequestClass.getMethod("create", String.class, boolean.class);
            Object ttsRequest = create.invoke(null, text, false);

            Class<?> robotClass = robot.getClass();
            java.lang.reflect.Method speak =
                    robotClass.getMethod("speak", ttsRequestClass);
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
