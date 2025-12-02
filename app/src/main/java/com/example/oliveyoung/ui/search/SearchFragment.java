package com.example.oliveyoung.ui.search;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oliveyoung.ui.search.ProductAdapter;
import com.example.oliveyoung.R;
import com.example.oliveyoung.api.Product;
import com.example.oliveyoung.api.ProductApi;
import com.example.oliveyoung.api.ProductSearchResponse;
import com.example.oliveyoung.api.RetrofitClient;
import com.robotemi.sdk.Robot;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private EditText editKeyword;
    private Button btnSearch;
    private RecyclerView recyclerProducts;

    private ProductApi productApi;
    private ProductAdapter productAdapter;
    private final List<Product> productList = new ArrayList<>();

    private Robot robot;    // Temi 인스턴스

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // View 연결
        editKeyword = view.findViewById(R.id.editKeyword);
        btnSearch = view.findViewById(R.id.btnSearch);
        recyclerProducts = view.findViewById(R.id.recyclerProducts);

        // Temi 인스턴스
        robot = Robot.getInstance();

        // Retrofit API 인터페이스 생성
        productApi = RetrofitClient.getClient().create(ProductApi.class);

        // RecyclerView + Adapter 설정
        productAdapter = new ProductAdapter(product -> moveToProduct(product));
        recyclerProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerProducts.setAdapter(productAdapter);

        // 검색 버튼 클릭
        btnSearch.setOnClickListener(v -> {
            String keyword = editKeyword.getText().toString().trim();
            if (TextUtils.isEmpty(keyword)) {
                Toast.makeText(getContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }
            searchProducts(keyword);
        });
    }

    /**
     * /api/products/search 호출
     */
    private void searchProducts(String keyword) {
        // ProductApi 인터페이스 정의:
        // searchProducts(String query, String category, String subCategory,
        //                String brand, Integer minPrice, Integer maxPrice,
        //                String skinType, Boolean inStock,
        //                String sortBy, Integer page, Integer pageSize)
        Call<ProductSearchResponse> call =
                productApi.searchProducts(
                        keyword,    // query
                        null,       // category
                        null,       // sub_category
                        null,       // brand
                        null,       // min_price
                        null,       // max_price
                        null,       // skin_type
                        true,       // in_stock (재고 있는 상품만)
                        "popularity", // sort_by
                        1,          // page
                        20          // page_size
                );

        call.enqueue(new Callback<ProductSearchResponse>() {
            @Override
            public void onResponse(Call<ProductSearchResponse> call,
                                   Response<ProductSearchResponse> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Log.d("PRODUCT/SEARCH", "HTTP ERROR: " + response.code());
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                "서버 오류: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                ProductSearchResponse body = response.body();

                if (!body.isSuccess()) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                "요청 실패 (success=false)",
                                Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                List<Product> products = body.getProducts();
                if (products == null) products = new ArrayList<>();

                productList.clear();
                productList.addAll(products);
                productAdapter.setItems(productList);

                Log.d("PRODUCT/SEARCH",
                        "검색 결과 개수 = " + products.size());
            }

            @Override
            public void onFailure(Call<ProductSearchResponse> call, Throwable t) {
                Log.d("PRODUCT/SEARCH", "FAIL: " + t.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(),
                            "통신 실패: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 상품 클릭 시 Temi가 해당 zone으로 이동
     * zone 값은 백엔드에서 "A1" ~ "D5" 형태로 온다고 가정.
     * Temi에 저장된 로케이션 이름도 동일하다고 보고 robot.goTo(zone) 호출. (추측입니다)
     */
    private void moveToProduct(Product product) {
        if (robot == null || getContext() == null) return;

        String zone = product.getZone();

        if (zone == null || zone.isEmpty()) {
            Toast.makeText(getContext(),
                    "이 상품의 위치 정보가 없습니다.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 Temi에 저장된 위치 목록 확인
        List<String> locations = robot.getLocations();
        Log.d("TEMI/NAV", "Saved locations = " + locations);
        Log.d("TEMI/NAV", "zone from server = '" + zone + "'");

        if (!locations.contains(zone)) {
            // 이름이 정확히 일치하지 않는 경우
            Toast.makeText(getContext(),
                    "'" + zone + "' 위치가 로봇에 정확히 저장돼 있지 않습니다.\n" +
                            "저장된 위치: " + locations,
                    Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(getContext(),
                "상품 위치로 이동합니다 (zone: " + zone + ")",
                Toast.LENGTH_SHORT).show();

        try {
            robot.goTo(zone);
        } catch (Exception e) {
            Log.e("TEMI/NAV", "goTo 실패", e);
            Toast.makeText(getContext(),
                    "이동 중 오류가 발생했습니다.",
                    Toast.LENGTH_SHORT).show();
        }
    }

}
