package com.example.save_city_pet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
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

public class ReportActivity extends AppCompatActivity {

    private ImageView imgReportPet;
    private EditText etName, etBreed, etPhone, etDesc, etAge;
    private AutoCompleteTextView etDistrict;
    private Button btnSubmit;
    private Uri selectedImageUri;
    private RadioGroup rgGender;
    private Spinner spinnerCategory, spinnerPhoneCode;

    private String[] catTitles = {"撿到猫咪", "撿到狗狗", "尋找猫咪", "尋找狗狗"};
    private String[] catIds = {"01_found_cat", "02_found_dog", "03_missing_cat", "04_missing_dog"};
    private String[] phoneCodes = {"852", "86"};

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

        // 1. 初始化 Cloudinary
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
            config.put("secure", true);
            MediaManager.init(this, config);
        } catch (Exception e) {
            // 避免重複初始化報錯
        }

        // 2. 綁定 UI 元件
        imgReportPet = findViewById(R.id.imgReportPet);
        etName = findViewById(R.id.etPetName);
        etBreed = findViewById(R.id.etPetBreed);
        etDistrict = findViewById(R.id.etPetDistrict);
        etPhone = findViewById(R.id.etPetPhone);
        etDesc = findViewById(R.id.etPetDesc);
        etAge = findViewById(R.id.etPetAge);
        btnSubmit = findViewById(R.id.btnSubmit);
        rgGender = findViewById(R.id.rgPetGender);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerPhoneCode = findViewById(R.id.spinnerPhoneCode);
        etBreed = findViewById(R.id.etPetBreed);

        // 3. 設定 下拉選單 (Spinner)
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, catTitles);
        spinnerCategory.setAdapter(catAdapter);

        ArrayAdapter<String> phoneAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, phoneCodes);
        spinnerPhoneCode.setAdapter(phoneAdapter);

        // 4. 設定 地區自動完成
        String[] districts = new String[]{"Tuen Mun", "Sha Tin", "Mong Kok", "Kowloon City", "Central and Western" , "Wan Chai", "Yau Tsim Mong","Southern","Tai Po","Kwai Tsing","Sai Kung","Northern", "Yuen Long","Tsuen Wan","Islands","Kwun Tong","Wong Tai Sin","Sham Shui Po"};
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, districts);
        etDistrict.setAdapter(districtAdapter);

        String aiBreed = getIntent().getStringExtra("AI_BREED");
        String imageUriStr = getIntent().getStringExtra("AI_IMAGE_URI");
        if (imageUriStr != null) {
            selectedImageUri = Uri.parse(imageUriStr);

            // 使用 Glide 載入（推薦，更穩定）
            com.bumptech.glide.Glide.with(this)
                    .load(selectedImageUri)
                    .into(imgReportPet);
        }
        if (aiBreed != null) {
            etBreed.setText(aiBreed);
            Toast.makeText(this, "已自動填入辨識品種：" + aiBreed, Toast.LENGTH_SHORT).show();
        }
        // 5. 事件監聽
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        imgReportPet.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        btnSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    private void validateAndSubmit() {
        String name = etName.getText().toString().trim();
        String breed = etBreed.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();

        // 處理電話：國碼 + 號碼
        String rawPhone = etPhone.getText().toString().trim();
        String selectedCode = spinnerPhoneCode.getSelectedItem().toString();
        String fullPhone = selectedCode + rawPhone;

        // 獲取分類 ID
        int selectedCatIndex = spinnerCategory.getSelectedItemPosition();
        String categoryId = catIds[selectedCatIndex];

        // 獲取性別
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "請選擇性別", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton rbSelected = findViewById(selectedGenderId);
        String gender = rbSelected.getText().toString();

        // 必填檢查
        if (selectedImageUri == null) {
            Toast.makeText(this, "請上傳照片", Toast.LENGTH_SHORT).show();
            return;
        }
        if (breed.isEmpty() || district.isEmpty() || rawPhone.isEmpty() || ageStr.isEmpty()) {
            Toast.makeText(this, "請填寫品種、地區、年齡及電話", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);

        // 預先生成 Firebase Key
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("Items");
        String newCaseKey = itemsRef.push().getKey();

        final String finalName = name.isEmpty() ?
                "Case #" + (newCaseKey != null ? newCaseKey.substring(newCaseKey.length() - 5) : "Unknown") : name;

        btnSubmit.setEnabled(false);
        Toast.makeText(this, "正在發布案件...", Toast.LENGTH_SHORT).show();

        // 上傳圖片到 Cloudinary
        MediaManager.get().upload(selectedImageUri)
                .unsigned("save_city_pet_presetName") // 💡 確保此 Preset Name 正確
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        // 呼叫上傳，注意參數順序需與定義一致
                        uploadToPublicItems(newCaseKey, finalName, breed, district, fullPhone, desc, imageUrl, gender, categoryId, age);
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

    private void uploadToPublicItems(String newCaseKey, String name, String breed, String district,
                                     String phone, String desc, String picUrl,
                                     String gender, String categoryId, int age) {
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
            publicPet.put("categoryId", categoryId);
            publicPet.put("gender", gender);
            publicPet.put("age", age);

            itemsRef.child(newCaseKey).setValue(publicPet).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "發布成功！", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                btnSubmit.setEnabled(true);
                Toast.makeText(this, "資料存儲失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
