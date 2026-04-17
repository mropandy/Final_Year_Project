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

        // 💡 1. 移除了重複實例化 searchManager 的程式碼
// 在 MainActivity 的 onCreate 中，補上第三個參數（這需要您在 activity_main.xml 中放一個 TextView 顯示找不到結果）
        searchManager = new SearchManager(
                findViewById(R.id.allCasesView),
                findViewById(R.id.progressBarAllCases),
                findViewById(R.id.tvNoResults) // 💡 補上這個用於提示找不到的 TextView
        );


        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            tvUserName.setText(currentUser.getEmail());
        }

        // 初始化各類管理器
        initAllCaseManager = new InitAllCase(findViewById(R.id.allCasesView), findViewById(R.id.progressBarAllCases));

        initCategories(); // 載入橫向分類
        initBanner();     // 載入海報
        initAllCaseManager.loadAllCases(); // 載入底部所有案件

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    if (layoutSlider != null) layoutSlider.setVisibility(View.GONE);
                    searchManager.search(query);
                } else {
                    if (layoutSlider != null) layoutSlider.setVisibility(View.VISIBLE);
                    if (initAllCaseManager != null) {
                        initAllCaseManager.loadAllCases();
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        imgSettings.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });
        imgProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.layoutReport).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReportActivity.class);
            startActivity(intent);
        });
    }

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

                    // 💡 2. 修正建構子：將點擊介面對接至 MainActivity 的方法
                    adapterCategory = new CategoryAdapter(list, item -> {
                        // 💡 安全的字串比對，防範 null
                        if ("00_home".equals(item.getId())) {
                            initBanner();
                        } else {
                            loadPetsByCategory(item.getId());
                        }
                    });

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

    public void initBanner() {
        if (layoutSlider != null) layoutSlider.setVisibility(View.VISIBLE);
        if (initAllCaseManager != null) {
            initAllCaseManager.loadAllCases(); // 確保回到首頁時顯示所有最新案件
        }
        DatabaseReference bannerRef = FirebaseDatabase.getInstance().getReference("Banner");
        if (progressBarSlider != null) progressBarSlider.setVisibility(View.VISIBLE);

        // 💡 3. addListenerForSingleValueEvent 是正確的，廣告海報不需要實時監聽，讀一次即可！
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

    public void loadPetsByCategory(String categoryId) {
        if (layoutSlider != null) {
            layoutSlider.setVisibility(View.GONE);
        }
        if (initAllCaseManager != null) {
            initAllCaseManager.loadCasesByCategory(categoryId);
        }
    }
}
