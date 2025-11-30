package com.example.oliveyoung.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProductSearchResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("total")
    private int total;

    @SerializedName("page")
    private int page;

    @SerializedName("page_size")
    private int pageSize;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("products")
    private List<Product> products;

    public boolean isSuccess() { return success; }
    public int getTotal() { return total; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public int getTotalPages() { return totalPages; }
    public List<Product> getProducts() { return products; }
}
