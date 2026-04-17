package com.example.save_city_pet;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化 Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 綁定 UI 元件
        EditText edEmail = findViewById(R.id.regEmail);
        EditText edPassword = findViewById(R.id.regPassword);
        Button btnDoRegister = findViewById(R.id.btnDoRegister);
        ImageButton btnArrow = findViewById(R.id.btnArrow);

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

            // 💡 體驗優化：點擊後立即禁用按鈕，防止狂點造成重複請求
            btnDoRegister.setEnabled(false);

            // Firebase 註冊指令
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // 💡 關鍵修正：註冊成功後，在 Database 同步建立 Users 節點
                            String uid = mAuth.getCurrentUser().getUid();
                            DatabaseReference userRef = FirebaseDatabase.getInstance()
                                    .getReference("Users").child(uid);

                            HashMap<String, Object> userData = new HashMap<>();
                            userData.put("username", "新用戶"); // 預設名稱
                            userData.put("phone", "待補充");     // 預設電話

                            userRef.setValue(userData).addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful()) {
                                    Toast.makeText(this, "註冊成功！請登入", Toast.LENGTH_SHORT).show();
                                    finish(); // 雙邊都成功，關閉此頁
                                } else {
                                    btnDoRegister.setEnabled(true);
                                    Toast.makeText(this, "建立資料庫失敗: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            btnDoRegister.setEnabled(true); // 失敗時重新啟用
                            Toast.makeText(this, "註冊失敗: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // 2. 自定義箭頭點擊
        btnArrow.setOnClickListener(v -> {
            finish();
        });
    }
}
