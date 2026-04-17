package com.example.save_city_pet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化 Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 💡 1. 偵測自動登入：如果已經登入，直接跳轉 MainActivity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return; // 結束 onCreate，不載入後續 UI
        }

        setContentView(R.layout.activity_login);

        // 綁定 UI 元件
        EditText edEmail = findViewById(R.id.loginEmail);
        EditText edPassword = findViewById(R.id.loginPassword);
        Button btnDoLogin = findViewById(R.id.btnDoLogin);
        ImageButton btnArrow = findViewById(R.id.btnArrow);

        // 登入按鈕邏輯
        btnDoLogin.setOnClickListener(v -> {
            String email = edEmail.getText().toString().trim();
            String password = edPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "請輸入帳號密碼", Toast.LENGTH_SHORT).show();
                return;
            }

            // 💡 2. 點擊後立即禁用按鈕，防止狂點造成重複請求
            btnDoLogin.setEnabled(false);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "登入成功！", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // 💡 登入失敗時，重新啟用按鈕，讓用戶可以修改密碼重試
                            btnDoLogin.setEnabled(true);
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
