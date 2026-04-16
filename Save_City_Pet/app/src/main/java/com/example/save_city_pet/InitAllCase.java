package com.example.save_city_pet;

import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class InitAllCase {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;

    public InitAllCase(RecyclerView recyclerView, ProgressBar progressBar) {
        this.recyclerView = recyclerView;
        this.progressBar = progressBar;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("Items");
    }

    public void loadAllCases() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // 設定 RecyclerView 為垂直排列，並禁止嵌套捲動（因為外層已有 ScrollView）
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<PetDomain> list = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        PetDomain pet = issue.getValue(PetDomain.class);
                        if (pet != null) {
                            pet.setCaseID(issue.getKey()); // 存入私有鍵
                            list.add(pet);
                        }
                    }
                    if (list.size() > 0) {
                        recyclerView.setAdapter(new PetListAdapter(list));
                    }
                }
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
    }
    // 在 InitAllCase.java 裡面新增這個方法
    public void loadCasesByCategory(String categoryId) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // 💡 關鍵：只查詢 categoryId 等於我們傳入的值
        databaseReference.orderByChild("categoryId").equalTo(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<PetDomain> list = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            PetDomain pet = ds.getValue(PetDomain.class);
                            if (pet != null) {
                                pet.setCaseID(ds.getKey());
                                list.add(pet);
                            }
                        }
                        // 更新下方的 RecyclerView
                        recyclerView.setAdapter(new PetListAdapter(list));
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
