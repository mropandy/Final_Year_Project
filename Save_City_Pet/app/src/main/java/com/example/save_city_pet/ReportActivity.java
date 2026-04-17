package com.example.save_city_pet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import com.example.save_city_pet.BuildConfig;

public class ReportActivity extends AppCompatActivity {

    private ImageView imgReportPet;
    private EditText etName, etBreed, etPhone, etDesc;
    private AutoCompleteTextView etDistrict;
    private Button btnSubmit;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imgReportPet.setImageURI(selectedImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // 初始化 Cloudinary
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
            config.put("secure", true);
            MediaManager.init(this, config);
        } catch (Exception e) {
            // 避免重複初始化報錯
        }

        imgReportPet = findViewById(R.id.imgReportPet);
        etName = findViewById(R.id.etPetName);
        etBreed = findViewById(R.id.etPetBreed);
        etDistrict = findViewById(R.id.etPetDistrict);
        etPhone = findViewById(R.id.etPetPhone);
        etDesc = findViewById(R.id.etPetDesc);
        btnSubmit = findViewById(R.id.btnSubmit);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String[] districts = new String[]{"Tuen Mun", "Sha Tin", "Mong Kok", "Kowloon City", "Central and Western" , "Wan Chai", "Yau Tsim Mong","Southern","Tai Po","Kwai Tsing","Sai Kung","Northern", "Yuen Long","Tsuen Wan","Islands","Kwun Tong","Wong Tai Sin","Sham Shui Po"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, districts);
        etDistrict.setAdapter(adapter);

        imgReportPet.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        btnSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    private void validateAndSubmit() {
        // 💡 1. 補回讀取輸入框的程式碼，否則編譯器找不到 name
        String name = etName.getText().toString().trim();
        String breed = etBreed.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        // 必填檢查
        if (selectedImageUri == null) {
            Toast.makeText(this, "請上傳照片", Toast.LENGTH_SHORT).show();
            return;
        }
        if (breed.isEmpty() || district.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "請填寫品種、地區及電話", Toast.LENGTH_SHORT).show();
            return;
        }

        // 預先生成 Firebase Key 用於命名
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("Items");
        String newCaseKey = itemsRef.push().getKey();

        final String finalName = name.isEmpty() ?
                "Case #" + (newCaseKey != null ? newCaseKey.substring(newCaseKey.length() - 5) : "Unknown") : name;

        btnSubmit.setEnabled(false);
        Toast.makeText(this, "正在發布案件...", Toast.LENGTH_SHORT).show();

        // 💡 2. 使用您新設定的 Unsigned Preset 名稱 [2]
        MediaManager.get().upload(selectedImageUri)
                .unsigned("save_city_pet_presetName")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        // 從 Cloudinary 取得成功後的網路網址 [2]
                        String imageUrl = (String) resultData.get("secure_url");

                        // 將資料寫入 Firebase
                        uploadToPublicItems(newCaseKey, finalName, breed, district, phone, desc, imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        btnSubmit.setEnabled(true);
                        Toast.makeText(ReportActivity.this, "圖片上傳失敗: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void uploadToPublicItems(String newCaseKey, String name, String breed, String district, String phone, String desc, String picUrl) {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("Items");

        if (newCaseKey != null) {
            HashMap<String, Object> publicPet = new HashMap<>();
            publicPet.put("caseID", newCaseKey);
            publicPet.put("title", name);
            publicPet.put("breed", breed);
            publicPet.put("district", district);
            publicPet.put("phone", phone);
            publicPet.put("description", desc);
            publicPet.put("picUrl", picUrl);
            publicPet.put("categoryId", "04_missing_dog");

            itemsRef.child(newCaseKey).setValue(publicPet).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "發布成功！", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                btnSubmit.setEnabled(true);
                Toast.makeText(this, "資料存儲失敗", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
