package com.example.save_city_pet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth; // 引入 Firebase

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 新增：檢查使用者是否已登入 ---
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // 如果已經登入，直接去 MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish(); // 關閉此頁，讓使用者無法按返回鍵回來
            return;
        }
        // ------------------------------

        setContentView(R.layout.activity_first);

        Button btnGoLogin = findViewById(R.id.btnGoLogin);
        Button btnGoRegister = findViewById(R.id.btnGoRegister);

        btnGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(FirstActivity.this, LoginActivity.class));
        });

        btnGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(FirstActivity.this, RegisterActivity.class));
        });
    }
}
