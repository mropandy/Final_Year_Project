package com.example.save_city_pet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 接收傳過來的物件
        PetDomain pet = (PetDomain) getIntent().getSerializableExtra("object");

        if (pet != null) {
            // 綁定元件
            TextView tvTitle = findViewById(R.id.detailTitle);
            TextView tvDesc = findViewById(R.id.detailDescription);
            TextView tvCase = findViewById(R.id.tvCaseID);
            ImageView imgPic = findViewById(R.id.detailPic);
            TextView tvInfo = findViewById(R.id.tvDetailInfo);
            Button btnContact = findViewById(R.id.btnContact);

            // 💡 填入基礎資料 (優化：防範當 Breed 為空時出現空括號)
            String petBreed = pet.getBreed();
            if (petBreed != null && !petBreed.isEmpty()) {
                tvTitle.setText(pet.getTitle() + " (" + petBreed + ")");
            } else {
                tvTitle.setText(pet.getTitle());
            }

            tvDesc.setText(pet.getDescription());
            tvCase.setText("ID: #" + pet.getCaseID());

            tvInfo.setText("年齡: " + pet.getAge() + " 歲 | 性別: " + pet.getGender() + " | 地區: " + pet.getDistrict());

            // 顯示大圖
            Glide.with(this).load(pet.getPicUrl()).into(imgPic);

            btnContact.setOnClickListener(v -> {
                String phoneNumber = pet.getPhone();

                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    // 1. 定義預先填妥的訊息
                    String rawMessage = "你好，我想查詢關於案件 #" + pet.getCaseID() + " (" + pet.getTitle() + ") 的資訊。";

                    // 2. 使用 Uri.encode 將中文訊息轉為 urlencodedtext
                    String urlEncodedText = android.net.Uri.encode(rawMessage);

                    // 3. 按照 WhatsApp 官方格式拼接連結
                    String url = "https://wa.me/" + phoneNumber + "?text=" + urlEncodedText;

                    // 4. 啟動 Intent
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(android.net.Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        // 防止沒裝 WhatsApp 的閃退，改用簡訊
                        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                        smsIntent.setData(android.net.Uri.parse("smsto:" + phoneNumber));
                        smsIntent.putExtra("sms_body", rawMessage);
                        startActivity(smsIntent);
                    }
                } else {
                    Toast.makeText(this, "無法取得聯絡電話", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 返回按鈕
        findViewById(R.id.btnArrow).setOnClickListener(v -> finish());
    }
}
