package com.example.save_city_pet;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 1. 接收傳過來的物件
        PetDomain pet = (PetDomain) getIntent().getSerializableExtra("object");

        if (pet != null) {
            // 2. 綁定元件
            TextView tvTitle = findViewById(R.id.detailTitle);
            TextView tvDesc = findViewById(R.id.detailDescription);
            TextView tvCase = findViewById(R.id.tvCaseID);
            ImageView imgPic = findViewById(R.id.detailPic);
            TextView tvInfo = findViewById(R.id.tvDetailInfo);
            Button btnContact = findViewById(R.id.btnContact);

            // 3. 填入基礎資料
            String petBreed = pet.getBreed();
            if (petBreed != null && !petBreed.isEmpty()) {
                tvTitle.setText(pet.getTitle() + " (" + petBreed + ")");
            } else {
                tvTitle.setText(pet.getTitle());
            }

            tvDesc.setText(pet.getDescription());
            tvCase.setText("ID: #" + pet.getCaseID());
            tvInfo.setText("年齡: " + pet.getAge() + " 歲 | 性別: " + pet.getGender() + " | 地區: " + pet.getDistrict());

            // 4. 顯示圖片
            Glide.with(this).load(pet.getPicUrl()).into(imgPic);

            // 💡 5. 權限判斷：Admin_01 vs 普通用戶
            if (AdminConfig.isAdmin()) {
                // --- 管理員模式 (IVE TM) ---
                btnContact.setText("管理員操作 (已歸還 / 刪除)");
                btnContact.setBackgroundTintList(ColorStateList.valueOf(Color.RED));

                btnContact.setOnClickListener(v -> {
                    String[] options = {"標記為已歸還 (保存數據)", "強制刪除案件", "取消"};
                    new AlertDialog.Builder(this)
                            .setTitle("Admin_01 權限操作")
                            .setItems(options, (dialog, which) -> {
                                if (which == 0) {
                                    archiveCase(pet); // 搬移至 FoundHistory
                                } else if (which == 1) {
                                    deleteCase(pet.getCaseID()); // 直接移除
                                }
                            }).show();
                });
            } else {
                // --- 普通用戶模式 (WhatsApp 聯絡) ---
                btnContact.setOnClickListener(v -> {
                    String phoneNumber = pet.getPhone();
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        String rawMessage = "你好，我想查詢關於案件 #" + pet.getCaseID() + " (" + pet.getTitle() + ") 的資訊。";
                        String urlEncodedText = Uri.encode(rawMessage);
                        String url = "https://wa.me" + phoneNumber + "?text=" + urlEncodedText;

                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            startActivity(intent);
                        } catch (Exception e) {
                            // 備案：簡訊
                            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                            smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
                            smsIntent.putExtra("sms_body", rawMessage);
                            startActivity(smsIntent);
                        }
                    } else {
                        Toast.makeText(this, "無法取得聯絡電話", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        // 返回按鈕
        findViewById(R.id.btnArrow).setOnClickListener(v -> finish());
    }

    // 💡 標記為已歸還：先複製到 FoundHistory 節點，再刪除 Items 節點
    private void archiveCase(PetDomain pet) {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("Items");

        // 💡 做法：不移除，只將 status 改為 Found
        itemsRef.child(pet.getCaseID()).child("status").setValue("Found")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "成功！該案件已在首頁標記為「已找回」", Toast.LENGTH_SHORT).show();
                    finish(); // 返回首頁，你會發現它變綠了
                });
    }


    // 💡 強制刪除案件
    private void deleteCase(String caseId) {
        FirebaseDatabase.getInstance().getReference("Items").child(caseId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "案件已強制刪除", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
