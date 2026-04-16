package com.example.save_city_pet;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

import android.widget.ImageButton;
public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化 Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 綁定 UI 元件 (請確保 XML 中的 ID 正確)
        EditText edEmail = findViewById(R.id.regEmail);
        EditText edPassword = findViewById(R.id.regPassword);
        Button btnDoRegister = findViewById(R.id.btnDoRegister);
        ImageButton btnArrow = findViewById(R.id.btnArrow); // 自定義返回按鈕

        // 1. 執行註冊邏輯
        btnDoRegister.setOnClickListener(v -> {
            String email = edEmail.getText().toString().trim();
            String password = edPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "請輸入電子郵件和密碼", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "密碼至少需要 6 位數", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase 註冊指令
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "註冊成功！請登入", Toast.LENGTH_SHORT).show();
                            finish(); // 註冊成功後關閉此頁，回到 FirstActivity
                        } else {
                            // 顯示具體失敗原因
                            Toast.makeText(this, "註冊失敗: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // 2. 自定義箭頭點擊：回到 FirstActivity
        btnArrow.setOnClickListener(v -> {
            finish();
        });
    }
}
