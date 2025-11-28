package com.example.oliveyoung;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private Robot robot;
    private FirebaseFirestore db;

    private Button buttonBack;
    private EditText editQuery;
    private Button buttonSearch;
    private TextView textStatus;
    private RecyclerView recyclerProducts;
    private ProductAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        robot = Robot.getInstance();
        db = FirebaseFirestore.getInstance();
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

        // 뒤로가기 버튼
        buttonBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        recyclerProducts.setLayoutManager(new LinearLayoutManager(getContext()));

        // 상품 클릭 시 Temi 이동
        adapter = new ProductAdapter(product -> {
            String zone = product.getLocationZone();
            if (TextUtils.isEmpty(zone)) {
                textStatus.setText("이 상품은 존 정보가 없습니다.");
                speak("이 상품은 위치 정보가 없어요.");
                return;
            }

            String speakText = product.getName() +
                    " 진열대가 있는 " + zone + " 존으로 안내하겠습니다.";
            textStatus.setText("테미가 " + zone + " 존으로 이동 중입니다.");
            speak(speakText);

            robot.goTo(zone);
        });

        recyclerProducts.setAdapter(adapter);

        buttonSearch.setOnClickListener(v -> searchProducts());

        return view;
    }

    private void searchProducts() {
        String query = editQuery.getText().toString().trim();

        if (TextUtils.isEmpty(query)) {
            textStatus.setText("상품명을 입력해 주세요. 예: 설화수 자음생 에센스");
            speak("상품명을 입력해 주세요.");
            return;
        }

        textStatus.setText("상품명 \"" + query + "\" 으로 상품을 검색 중입니다...");

        Task<QuerySnapshot> task = db.collection("products")
                .whereEqualTo("name", query)
                .get();

        task.addOnSuccessListener(queryDocumentSnapshots -> {
            List<Product> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Product p = doc.toObject(Product.class);
                list.add(p);
            }

            adapter.setItems(list);

            if (list.isEmpty()) {
                String msg = "상품명 \"" + query + "\" 에 해당하는 상품을 찾지 못했습니다.";
                textStatus.setText(msg);
                speak(msg);
            } else {
                String msg = "상품명 \"" + query + "\" 상품 " + list.size() +
                        "개를 찾았습니다. 이동하고 싶은 상품을 선택해 주세요.";
                textStatus.setText(msg);
                speak(msg);
            }
        });

        task.addOnFailureListener(e -> {
            String msg = "상품 검색 중 오류가 발생했습니다: " + e.getMessage();
            textStatus.setText(msg);
            speak("상품 검색 중 오류가 발생했습니다.");
        });
    }

    private void speak(String text) {
        if (robot == null) return;
        TtsRequest ttsRequest = TtsRequest.create(text, false);
        robot.speak(ttsRequest);
    }
}