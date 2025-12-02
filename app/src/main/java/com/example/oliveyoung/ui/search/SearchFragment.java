package com.example.oliveyoung.ui.search;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oliveyoung.R;
import com.example.oliveyoung.api.Product;
import com.example.oliveyoung.api.ProductApi;
import com.example.oliveyoung.api.ProductSearchResponse;
import com.example.oliveyoung.api.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private View buttonBack;
    private EditText editQuery;
    private Button buttonSearch;
    private RecyclerView recyclerProducts;

    private ProductApi productApi;
    private ProductAdapter productAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrofit API 인터페이스 생성
        productApi = RetrofitClient
                .getClient()
                .create(ProductApi.class);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(
            View view,
            Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        // 상단 뒤로가기 버튼
        buttonBack = view.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                // MainActivity.onBackPressed() 호출 → 홈 화면으로
                getActivity().onBackPressed();
            }
        });

        // 검색 관련 뷰 연결 (XML id와 꼭 일치해야 함)
        editQuery = view.findViewById(R.id.editQuery);
        buttonSearch = view.findViewById(R.id.buttonSearch);
        recyclerProducts = view.findViewById(R.id.recyclerProducts);

        // RecyclerView + Adapter 설정
        recyclerProducts.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        productAdapter = new ProductAdapter(
                new ArrayList<>(),
                product -> {
                    // TODO: 나중에 장바구니/결제와 연동할 때 여기서 처리
                    if (getContext() != null) {
                        Toast.makeText(
                                getContext(),
                                "선택한 상품: " + product.getName(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
        recyclerProducts.setAdapter(productAdapter);

        // 검색 버튼 클릭 리스너
        buttonSearch.setOnClickListener(v -> search());
    }

    private void search() {
        String keyword = editQuery.getText().toString().trim();

        if (TextUtils.isEmpty(keyword)) {
            if (getContext() != null) {
                Toast.makeText(getContext(),
                        "검색어를 입력해주세요.",
                        Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Call<ProductSearchResponse> call = productApi.searchProducts(
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
            public void onResponse(
                    Call<ProductSearchResponse> call,
                    Response<ProductSearchResponse> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    if (getContext() != null) {
                        Toast.makeText(
                                getContext(),
                                "서버 오류: " + response.code(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                    return;
                }

                ProductSearchResponse body = response.body();
                if (!body.isSuccess()) {
                    if (getContext() != null) {
                        Toast.makeText(
                                getContext(),
                                "검색 결과가 없습니다.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                    return;
                }

                List<Product> products = body.getProducts();
                if (products == null) products = new ArrayList<>();

                productAdapter.setItems(products);
            }

            @Override
            public void onFailure(
                    Call<ProductSearchResponse> call,
                    Throwable t
            ) {
                if (getContext() != null) {
                    Toast.makeText(
                            getContext(),
                            "서버 통신에 실패했습니다.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }
}
