package com.example.oliveyoung.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ProductApi {

    @GET("/api/products/search")
    Call<ProductSearchResponse> searchProducts(

            @Query("query") String query,             // 🔥 검색어 (중요)

            @Query("category") String category,       // 선택값
            @Query("sub_category") String subCategory,
            @Query("brand") String brand,

            @Query("min_price") Integer minPrice,
            @Query("max_price") Integer maxPrice,

            @Query("skin_type") String skinType,

            @Query("in_stock") Boolean inStock,

            @Query("sort_by") String sortBy,          // popularity, price_low, price_high …

            @Query("page") Integer page,
            @Query("page_size") Integer pageSize
    );
}
