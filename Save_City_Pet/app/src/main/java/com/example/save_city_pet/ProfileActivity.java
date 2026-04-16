package com.example.save_city_pet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvPhone, tvEmail, tvUserId;
    private ImageButton btnBack;
    private RecyclerView myPetRecyclerView;
    private MyPetAdapter myPetAdapter;

    // 💡 確保使用同一個變數名
    private Uri imageUri;
    private PetUploadManager uploadManager;

    // 💡 註冊選圖 Launcher
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            Toast.makeText(this, "照片已選取", Toast.LENGTH_SHORT).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 1. 綁定 UI
        tvName = findViewById(R.id.profileName);
        tvPhone = findViewById(R.id.profilePhone);
        tvEmail = findViewById(R.id.profileEmail);
        tvUserId = findViewById(R.id.userId);
        btnBack = findViewById(R.id.btnArrow);
        myPetRecyclerView = findViewById(R.id.myPetRecyclerView);

        // 💡 初始化上傳管理器
        uploadManager = new PetUploadManager(this);

        findViewById(R.id.btnAddMyPet).setOnClickListener(v -> showAddPetDialog());
        myPetRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnBack.setOnClickListener(v -> finish());

        loadUserProfile();
    }

    private void showAddPetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("新增我的寵物");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputName = new EditText(this);
        inputName.setHint("寵物名字");
        layout.addView(inputName);

        final EditText inputBreed = new EditText(this);
        inputBreed.setHint("品種 (例如：哥基)");
        layout.addView(inputBreed);

        final EditText inputAge = new EditText(this);
        inputAge.setHint("年齡");
        inputAge.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(inputAge);

        // 💡 增加選取照片按鈕
        final Button btnPick = new Button(this);
        btnPick.setText("選取寵物照片");
        btnPick.setOnClickListener(v -> openGallery());
        layout.addView(btnPick);

        builder.setView(layout);

        builder.setPositiveButton("儲存", (dialog, which) -> {
            String name = inputName.getText().toString();
            String breed = inputBreed.getText().toString();
            String ageStr = inputAge.getText().toString();
            int age = ageStr.isEmpty() ? 0 : Integer.parseInt(ageStr);

            // 💡 呼叫新的上傳方法
            if (imageUri != null) {
                uploadManager.savePetLocally(name, breed, age, imageUri);
                imageUri = null; // 上傳後重置
            } else {
                Toast.makeText(this, "請先選擇照片", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void loadUserProfile() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            tvUserId.setText(uid);
            tvEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        tvName.setText(String.valueOf(snapshot.child("username").getValue()));
                        tvPhone.setText(String.valueOf(snapshot.child("phone").getValue()));

                        ArrayList<MyPetDomain> petList = new ArrayList<>();
                        DataSnapshot petsSnapshot = snapshot.child("myPets");
                        if (petsSnapshot.exists()) {
                            for (DataSnapshot ds : petsSnapshot.getChildren()) {
                                MyPetDomain pet = ds.getValue(MyPetDomain.class);
                                if (pet != null) petList.add(pet);
                            }
                        }
                        myPetAdapter = new MyPetAdapter(petList);
                        myPetRecyclerView.setAdapter(myPetAdapter);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }
}
