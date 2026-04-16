package com.example.save_city_pet;

import android.view.View;
import android.widget.ProgressBar;
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
    private RecyclerView recyclerView; // 💡 修正：改用 RecyclerView 來顯示列表
    private ProgressBar progressBar;

    // 💡 修正：傳入的是底部的 RecyclerView，而不是 ViewPager2
    public SearchManager(RecyclerView recyclerView, ProgressBar progressBar) {
        this.recyclerView = recyclerView;
        this.progressBar = progressBar;
        this.itemsRef = FirebaseDatabase.getInstance().getReference("Items");
    }

    public void search(String query) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<PetDomain> resultList = new ArrayList<>();
                String lowerQuery = query.toLowerCase().trim();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    PetDomain pet = ds.getValue(PetDomain.class);

                    if (pet != null) {
                        pet.setCaseID(ds.getKey()); // 💡 確保存入 CaseID，方便點擊看詳情

                        // 🔍 合理化搜尋條件：同時進行 null 安全檢查
                        boolean matchesName = pet.getTitle() != null && pet.getTitle().toLowerCase().contains(lowerQuery);
                        boolean matchesBreed = pet.getBreed() != null && pet.getBreed().toLowerCase().contains(lowerQuery);

                        if (matchesName || matchesBreed) {
                            resultList.add(pet);
                        }
                    }
                }

                // 💡 關鍵：直接將搜尋過濾後的結果，塞給底部的 PetListAdapter
                if (resultList.isEmpty()) {
                    // 如果找不到資料，可以塞一個空的 List (畫面會顯示空白)
                    recyclerView.setAdapter(new PetListAdapter(new ArrayList<>()));
                } else {
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
