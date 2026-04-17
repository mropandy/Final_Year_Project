package com.example.save_city_pet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvPhone, tvEmail, tvUserId;
    private ImageButton btnBack;
    private RecyclerView myPetRecyclerView;
    private MyPetAdapter myPetAdapter;
    private ImageView profileImageLarge;

    private Uri imageUri;
    private PetUploadManager uploadManager;

    // 💡 1. PickVisualMedia 必須宣告在全域，不能寫在 onCreate 裡面
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    saveImageToLocal(uri);
                }
            });

    // 💡 2. 選圖 Launcher
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

        // 💡 3. 修正：必須先綁定，再呼叫 loadLocalProfileImage()，否則會閃退！
        profileImageLarge = findViewById(R.id.profileImageLarge);
        tvName = findViewById(R.id.profileName);
        tvPhone = findViewById(R.id.profilePhone);
        tvEmail = findViewById(R.id.profileEmail);
        tvUserId = findViewById(R.id.userId);
        btnBack = findViewById(R.id.btnArrow);
        myPetRecyclerView = findViewById(R.id.myPetRecyclerView);

        loadLocalProfileImage();

        uploadManager = new PetUploadManager(this);

        findViewById(R.id.btnAddMyPet).setOnClickListener(v -> showAddPetDialog());
        myPetRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnBack.setOnClickListener(v -> finish());

        loadUserProfile();

        profileImageLarge.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
    }

    private void saveImageToLocal(Uri sourceUri) {
        // 💡 使用 try-with-resources 安全讀寫，防範記憶體流失
        File directory = getFilesDir();
        File file = new File(directory, "profile_avatar.jpg");

        try (InputStream inputStream = getContentResolver().openInputStream(sourceUri);
             FileOutputStream outputStream = new FileOutputStream(file)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            profileImageLarge.setImageURI(Uri.fromFile(file));
            Toast.makeText(this, "大頭貼已儲存在本地！", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "儲存失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLocalProfileImage() {
        File directory = getFilesDir();
        File file = new File(directory, "profile_avatar.jpg");

        if (file.exists()) {
            profileImageLarge.setImageURI(Uri.fromFile(file));
        } else {
            profileImageLarge.setImageResource(R.drawable.profile);
        }
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

                        // 💡 4. 為您補齊被截斷的 Firebase 迴圈結尾
                        if (petsSnapshot.exists()) {
                            for (DataSnapshot ds : petsSnapshot.getChildren()) {
                                MyPetDomain pet = ds.getValue(MyPetDomain.class);
                                if (pet != null) {
                                    pet.setKey(ds.getKey());
                                    petList.add(pet);
                                }
                            }
                        }

                        // 💡 5. 初始化並設定 Adapter (第二個參數 true 代表在個人頁，要顯示發布按鈕)
                        myPetAdapter = new MyPetAdapter(petList, true);
                        myPetRecyclerView.setAdapter(myPetAdapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "讀取失敗: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
