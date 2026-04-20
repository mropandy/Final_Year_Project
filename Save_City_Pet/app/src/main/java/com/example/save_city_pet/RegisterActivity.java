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

        mAuth = FirebaseAuth.getInstance();

        EditText edEmail = findViewById(R.id.regEmail);
        EditText edPassword = findViewById(R.id.regPassword);
        EditText edUsername = findViewById(R.id.regUsername);
        EditText edPhone = findViewById(R.id.regPhone);
        Button btnDoRegister = findViewById(R.id.btnDoRegister);
        ImageButton btnArrow = findViewById(R.id.btnArrow);

        btnDoRegister.setOnClickListener(v -> {
            String email = edEmail.getText().toString().trim();
            String password = edPassword.getText().toString().trim();
            String username = edUsername.getText().toString().trim();
            String phone = edPhone.getText().toString().trim();

            // 1. 完整性檢查
            if (email.isEmpty() || password.isEmpty() || username.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "請填寫完整資訊", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "密碼至少需要 6 位數", Toast.LENGTH_SHORT).show();
                return;
            }

            btnDoRegister.setEnabled(false);

            // 2. Firebase 註冊
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();
                            DatabaseReference userRef = FirebaseDatabase.getInstance()
                                    .getReference("Users").child(uid);

                            // 💡 修正：將輸入的值放入 HashMap，不再寫死
                            HashMap<String, Object> userData = new HashMap<>();
                            userData.put("username", username);
                            userData.put("phone", phone);

                            userRef.setValue(userData).addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful()) {
                                    Toast.makeText(this, "註冊成功！請登入", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    btnDoRegister.setEnabled(true);
                                    Toast.makeText(this, "建立資料庫失敗: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            btnDoRegister.setEnabled(true);
                            Toast.makeText(this, "註冊失敗: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        btnArrow.setOnClickListener(v -> finish());
    }
}
