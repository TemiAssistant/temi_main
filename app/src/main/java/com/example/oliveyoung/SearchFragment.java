package com.example.oliveyoung;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * SearchFragment - 하이브리드 버전
 * Temi 있으면: 음성 안내 + 로봇 이동
 * Temi 없으면: UI만 작동
 */
public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";

    private Object robot; // Temi Robot
    private boolean isTemiAvailable = false;

    private Button buttonBack;
    private EditText editQuery;
    private Button buttonSearch;
    private TextView textStatus;
    private RecyclerView recyclerProducts;
    private ProgressBar progressBar;
    private ProductAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Temi SDK 초기화
        initTemiRobot();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        buttonBack = view.findViewById(R.id.buttonBack);
        editQuery = view.findViewById(R.id.editQuery);
        buttonSearch = view.findViewById(R.id.buttonSearch);
        textStatus = view.findViewById(R.id.textStatus);
        recyclerProducts = view.findViewById(R.id.recyclerProducts);
        progressBar = view.findViewById(R.id.progressBar);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        recyclerProducts.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProductAdapter(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                String zone = product.getLocationZone();
                if (TextUtils.isEmpty(zone)) {
                    textStatus.setText("이 상품은 존 정보가 없습니다.");
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "위치 정보 없음", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                String msg = product.getName() + "은(는) " + zone + " 존에 있습니다.";
                textStatus.setText(msg);

                if (isTemiAvailable) {
                    // Temi: 음성 안내 + 이동
                    String speakText = product.getName() + " 진열대가 있는 " + zone + " 존으로 안내하겠습니다.";
                    speak(speakText);
                    goToLocation(zone);
                } else {
                    // 에뮬레이터: Toast만
                    if (getContext() != null) {
                        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        recyclerProducts.setAdapter(adapter);

        // 검색 버튼 클릭
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchProducts();
            }
        });

        // Enter 키로 검색
        editQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, android.view.KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE) {
                    searchProducts();
                    return true;
                }
                return false;
            }
        });

        // 입력 감지로 버튼 활성화/비활성화
        editQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonSearch.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 초기 상태: 검색 버튼 비활성화
        buttonSearch.setEnabled(false);

        return view;
    }

    /**
     * Temi SDK 초기화
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

    /**
     * 상품 검색 (Mock 데이터)
     */
    private void searchProducts() {
        String query = editQuery.getText().toString().trim();

        if (TextUtils.isEmpty(query)) {
            textStatus.setText("상품명을 입력해 주세요. 예: 설화수 자음생 에센스");
            return;
        }

        // 키보드 숨기기
        hideKeyboard();

        // 로딩 시작
        progressBar.setVisibility(View.VISIBLE);
        buttonSearch.setEnabled(false);
        textStatus.setText("상품명 \"" + query + "\" 으로 상품을 검색 중입니다...");

        // Mock 검색 시뮬레이션
        progressBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Product> searchResults = performMockSearch(query);

                progressBar.setVisibility(View.GONE);
                buttonSearch.setEnabled(true);

                adapter.setItems(searchResults);

                if (searchResults.isEmpty()) {
                    String msg = "상품명 \"" + query + "\" 에 해당하는 상품을 찾지 못했습니다.";
                    textStatus.setText(msg);
                } else {
                    String msg = "상품명 \"" + query + "\" 상품 " + searchResults.size() +
                            "개를 찾았습니다. 이동하고 싶은 상품을 선택해 주세요.";
                    textStatus.setText(msg);

                    if (isTemiAvailable) {
                        speak(msg);
                    }
                }
            }
        }, 1000);
    }

    /**
     * Mock 검색
     */
    private List<Product> performMockSearch(String query) {
        List<Product> results = new ArrayList<>();

        // 간단한 Mock 검색 로직
        if (query.contains("설화수")) {
            results.add(createMockProduct("설화수 자음생 에센스", "A"));
        }
        if (query.contains("라네즈")) {
            results.add(createMockProduct("라네즈 워터 슬리핑 마스크", "B"));
        }
        if (query.contains("헤라")) {
            results.add(createMockProduct("헤라 블랙 쿠션", "C"));
        }

        return results;
    }

    /**
     * Mock Product 생성
     */
    private Product createMockProduct(String name, String zone) {
        Product product = new Product();
        try {
            java.lang.reflect.Field field;

            field = Product.class.getDeclaredField("product_id");
            field.setAccessible(true);
            field.set(product, "MOCK_" + zone);

            field = Product.class.getDeclaredField("name");
            field.setAccessible(true);
            field.set(product, name);

            field = Product.class.getDeclaredField("brand");
            field.setAccessible(true);
            field.set(product, name.split(" ")[0]);

            field = Product.class.getDeclaredField("category");
            field.setAccessible(true);
            field.set(product, "스킨케어");

            field = Product.class.getDeclaredField("sub_category");
            field.setAccessible(true);
            field.set(product, "에센스");

            field = Product.class.getDeclaredField("price");
            field.setAccessible(true);
            field.set(product, 85000L);

            java.util.Map<String, Object> location = new java.util.HashMap<>();
            location.put("zone", zone);
            field = Product.class.getDeclaredField("location");
            field.setAccessible(true);
            field.set(product, location);

        } catch (Exception e) {
            Log.e(TAG, "Product 생성 실패", e);
        }
        return product;
    }

    /**
     * TTS 음성 (Temi 전용)
     */
    private void speak(String text) {
        if (!isTemiAvailable || robot == null) {
            return;
        }

        try {
            Class<?> ttsRequestClass = Class.forName("com.robotemi.sdk.TtsRequest");
            java.lang.reflect.Method create = ttsRequestClass.getMethod("create", String.class, boolean.class);
            Object ttsRequest = create.invoke(null, text, false);

            Class<?> robotClass = robot.getClass();
            java.lang.reflect.Method speak = robotClass.getMethod("speak", ttsRequestClass);
            speak.invoke(robot, ttsRequest);

            Log.d(TAG, "✅ [TTS] " + text);
        } catch (Exception e) {
            Log.e(TAG, "TTS 실패", e);
        }
    }

    /**
     * 로봇 이동 (Temi 전용)
     */
    private void goToLocation(String location) {
        if (!isTemiAvailable || robot == null) {
            return;
        }

        try {
            Class<?> robotClass = robot.getClass();
            java.lang.reflect.Method goTo = robotClass.getMethod("goTo", String.class);
            goTo.invoke(robot, location);
            Log.d(TAG, "✅ [로봇 이동] " + location);
        } catch (Exception e) {
            Log.e(TAG, "로봇 이동 실패", e);
        }
    }

    private void hideKeyboard() {
        if (getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && getView() != null) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }
}