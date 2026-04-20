package com.example.save_city_pet;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.database.*;
import java.util.ArrayList;

public class PetMatchManager {
    private Context context;
    private DatabaseReference databaseReference;

    public PetMatchManager(Context context) {
        this.context = context;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("Items");
    }

    public void findMatch(PetDomain newPet) {
        String breed = newPet.getBreed();
        String district = newPet.getDistrict();
        String gender = newPet.getGender();
        String categoryId = newPet.getCategoryId();
        // 撿到 (01, 02) -> 找 尋找 (03, 04)
        // 尋找 (03, 04) -> 找 撿到 (01, 02)
        String targetPrefix = (categoryId.startsWith("01") || categoryId.startsWith("02")) ? "0" : "0";
        // 簡單邏輯：如果是 01/02，目標搜 03/04；反之亦然
        boolean lookingForMissing = categoryId.contains("found");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<PetDomain> matchList = new ArrayList<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    PetDomain otherPet = ds.getValue(PetDomain.class);
                    if (otherPet == null || ds.getKey().equals(newPet.getCaseID())) continue;

                    // 💡 2. 交叉檢查分類
                    boolean isOpposite = false;
                    if (lookingForMissing && otherPet.getCategoryId().contains("missing")) isOpposite = true;
                    if (!lookingForMissing && otherPet.getCategoryId().contains("found")) isOpposite = true;

                    if (isOpposite) {
                        // 💡 3. 三重模糊比對 (忽略大小寫與空格)
                        boolean bMatch = breed != null && otherPet.getBreed() != null &&
                                breed.trim().equalsIgnoreCase(otherPet.getBreed().trim());

                        boolean dMatch = district != null && otherPet.getDistrict() != null &&
                                district.trim().equalsIgnoreCase(otherPet.getDistrict().trim());

                        // 性別處理「公 (Male)」與「Male」的相容
                        String g1 = gender.toLowerCase();
                        String g2 = otherPet.getGender().toLowerCase();
                        boolean gMatch = g1.contains(g2) || g2.contains(g1);

                        if (bMatch && dMatch && gMatch) {
                            otherPet.setCaseID(ds.getKey());
                            matchList.add(otherPet);
                        }
                    }
                }

                if (!matchList.isEmpty()) {
                    showMatchResultDialog(matchList);
                } else {
                    Log.d("PetMatch", "沒有找到匹配案件");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showMatchResultDialog(ArrayList<PetDomain> matches) {
        PetDomain topMatch = matches.get(0);
        new AlertDialog.Builder(context)
                .setTitle("✨ 發現潛在匹配！")
                .setMessage("系統發現一則與您發布非常吻合的個案：\n\n" +
                        "品種: " + topMatch.getBreed() + "\n" +
                        "地區: " + topMatch.getDistrict() + "\n" +
                        "性別: " + topMatch.getGender() + "\n\n" +
                        "是否立即查看詳情？")
                .setPositiveButton("查看詳情", (dialog, which) -> {
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra("object", topMatch);
                    context.startActivity(intent);
                })
                .setNegativeButton("關閉", null)
                .show();
    }
}
