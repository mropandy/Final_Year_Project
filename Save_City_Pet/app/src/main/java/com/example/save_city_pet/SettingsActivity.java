package com.example.save_city_pet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 點擊左上角箭頭返回 MainActivity
        ImageButton btnArrow = findViewById(R.id.btnArrow);
        btnArrow.setOnClickListener(v -> finish());

        // 登出邏輯
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // 💡 1. 加上 AlertDialog 防呆機制，避免用戶誤觸直接登出
            new AlertDialog.Builder(this)
                    .setTitle("登出確認")
                    .setMessage("您確定要登出當前帳號嗎？")
                    .setPositiveButton("確定登出", (dialog, which) -> {
                        // 執行登出
                        FirebaseAuth.getInstance().signOut();

                        // 跳轉回 FirstActivity 並清空 Activity 堆疊 [1]
                        Intent intent = new Intent(SettingsActivity.this, FirstActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }
}
