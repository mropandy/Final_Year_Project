package com.example.save_city_pet;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchManager {

    private DatabaseReference itemsRef;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvNoResults;

    // 💡 1. 修正建構子：傳入 3 個參數，正確接收 tvNoResults
    public SearchManager(RecyclerView recyclerView, ProgressBar progressBar, TextView tvNoResults) {
        this.recyclerView = recyclerView;
        this.progressBar = progressBar;
        this.tvNoResults = tvNoResults; // 💡 修正自我賦值錯誤
        this.itemsRef = FirebaseDatabase.getInstance().getReference("Items");
    }

    public void search(String query) {
        // 💡 2. 安全防護：如果搜尋字串為空，直接不處理
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<PetDomain> resultList = new ArrayList<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    PetDomain pet = ds.getValue(PetDomain.class);

                    if (pet != null) {
                        pet.setCaseID(ds.getKey());

                        // 將所有欄位轉為小寫以便比對
                        String lowerQuery = query.toLowerCase().trim();

                        // 🔍 多維度安全檢查
                        boolean matchesName = pet.getTitle() != null && pet.getTitle().toLowerCase().contains(lowerQuery);
                        boolean matchesBreed = pet.getBreed() != null && pet.getBreed().toLowerCase().contains(lowerQuery);

                        // 💡 新增：地區搜尋 (例如輸入 "Tuen Mun")
                        boolean matchesDistrict = pet.getDistrict() != null && pet.getDistrict().toLowerCase().contains(lowerQuery);

                        // 💡 新增：性別搜尋 (例如輸入 "Male" 或 "Female")
                        boolean matchesGender = pet.getGender() != null && pet.getGender().toLowerCase().equalsIgnoreCase(lowerQuery);

                        // 💡 新增：狀態/分類搜尋 (選做)
                        boolean matchesCategory = pet.getCategoryId() != null && pet.getCategoryId().toLowerCase().contains(lowerQuery);

                        // 只要符合其中一項就放入結果清單
                        if (matchesName || matchesBreed || matchesDistrict || matchesGender || matchesCategory) {
                            resultList.add(pet);
                        }
                    }
                }


                // 💡 3. 處理搜尋結果
                if (resultList.isEmpty()) {
                    // 為了確保畫面清空，先給空 Adapter
                    recyclerView.setAdapter(new PetListAdapter(new ArrayList<>()));

                    // 🎯 找不到資料：隱藏列表，顯示提示
                    recyclerView.setVisibility(View.GONE);
                    if (tvNoResults != null) {
                        tvNoResults.setVisibility(View.VISIBLE);
                        tvNoResults.bringToFront(); // 確保它在最上層
                    }
                } else {
                    // 🎯 找到資料：隱藏提示，顯示列表
                    if (tvNoResults != null) tvNoResults.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    recyclerView.setAdapter(new PetListAdapter(resultList));
                }

                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
    }
}
