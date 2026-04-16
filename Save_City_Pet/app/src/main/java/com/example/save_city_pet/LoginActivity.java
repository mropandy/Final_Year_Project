package com.example.save_city_pet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化 Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 綁定 UI 元件
        EditText edEmail = findViewById(R.id.loginEmail);
        EditText edPassword = findViewById(R.id.loginPassword);
        Button btnDoLogin = findViewById(R.id.btnDoLogin);
        ImageButton btnArrow = findViewById(R.id.btnArrow); // 你的自定義箭頭

        // 登入按鈕邏輯
        btnDoLogin.setOnClickListener(v -> {
            String email = edEmail.getText().toString().trim();
            String password = edPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "請輸入帳號密碼", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "登入成功！", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "登入失敗: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // 自定義箭頭返回邏輯
        btnArrow.setOnClickListener(v -> {
            finish(); // 關閉目前頁面，回到 FirstActivity
        });
    }
}

