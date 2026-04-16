package com.example.save_city_pet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView tvUserName;

    private RecyclerView recyclerViewCategory;
    private RecyclerView.Adapter adapterCategory;
    private ProgressBar progressBar, progressBarSlider;

    private InitAllCase initAllCaseManager;
    private SearchManager searchManager;

    private DatabaseReference database;
    private ViewPager2 viewPagerSlider;
    private View layoutSlider; // 包裹 ViewPager2 的父容器

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 綁定 UI 元件
        mAuth = FirebaseAuth.getInstance();
        tvUserName = findViewById(R.id.tvUserName);
        recyclerViewCategory = findViewById(R.id.categoryView);
        progressBar = findViewById(R.id.progressBarCategory);
        viewPagerSlider = findViewById(R.id.viewPagerSlider);
        progressBarSlider = findViewById(R.id.progressBarSlider);
        layoutSlider = findViewById(R.id.layoutSlider);
        ImageView imgSettings = findViewById(R.id.imgSettings);
        EditText editTextSearch = findViewById(R.id.editTextSearch);
        ImageView imgProfile = findViewById(R.id.imgProfile);



        // 2. 顯示用戶資訊
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            tvUserName.setText(currentUser.getEmail());
        }

        // 3. 初始化各類管理器 (傳入對應元件)
        // 在 MainActivity 的 onCreate 裡面修改這一行
        searchManager = new SearchManager(findViewById(R.id.allCasesView), findViewById(R.id.progressBarAllCases));
        initAllCaseManager = new InitAllCase(findViewById(R.id.allCasesView), findViewById(R.id.progressBarAllCases));

        // 4. 執行初始載入資料
        initCategories(); // 載入橫向分類
        initBanner();     // 載入海報
        initAllCaseManager.loadAllCases(); // 載入底部所有案件

        // 5. 搜尋框監聽
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    // 搜尋時隱藏海報區，讓結果更清楚
                    if (layoutSlider != null) layoutSlider.setVisibility(View.GONE);
                    searchManager.search(query);
                } else {
                    // 清空搜尋則恢復海報
                    initBanner();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 6. 設定按鈕點擊
        imgSettings.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });
        imgProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    /**
     * 載入中間橫向的分類按鈕
     */
    private void initCategories() {
        recyclerViewCategory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        database = FirebaseDatabase.getInstance().getReference("Category");
        progressBar.setVisibility(View.VISIBLE);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<CategoryDomain> list = new ArrayList<>();
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        CategoryDomain domain = issue.getValue(CategoryDomain.class);
                        if (domain != null) {
                            domain.setId(issue.getKey());
                            list.add(domain);
                        }
                    }
                    adapterCategory = new CategoryAdapter(list);
                    recyclerViewCategory.setAdapter(adapterCategory);
                    progressBar.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 初始化海報輪播區 (點擊「首頁」或清空搜尋時呼叫)
     */
    public void initBanner() {
        // 顯示海報父容器，並恢復底部列表為「所有案件」
        if (layoutSlider != null) layoutSlider.setVisibility(View.VISIBLE);
        if (initAllCaseManager != null) initAllCaseManager.loadAllCases();

        DatabaseReference bannerRef = FirebaseDatabase.getInstance().getReference("Banner");
        if (progressBarSlider != null) progressBarSlider.setVisibility(View.VISIBLE);

        bannerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<PetDomain> bannerList = new ArrayList<>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        PetDomain banner = new PetDomain();
                        banner.setPicUrl(ds.child("url").getValue(String.class));
                        banner.setTitle("最新公告");
                        banner.setBreed("");
                        banner.setDistrict("");
                        bannerList.add(banner);
                    }
                    if (viewPagerSlider != null) {
                        viewPagerSlider.setAdapter(new SliderAdapter(bannerList));
                    }
                    if (progressBarSlider != null) progressBarSlider.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressBarSlider != null) progressBarSlider.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 點擊特定分類時呼叫 (隱藏海報區，並連動底部列表過濾資料)
     */
    public void loadPetsByCategory(String categoryId) {
        // 隱藏整個海報父容器，消除空格
        if (layoutSlider != null) {
            layoutSlider.setVisibility(View.GONE);
        }

        // 叫底部列表經理只載入該分類的數據
        if (initAllCaseManager != null) {
            initAllCaseManager.loadCasesByCategory(categoryId);
        }
    }
}
