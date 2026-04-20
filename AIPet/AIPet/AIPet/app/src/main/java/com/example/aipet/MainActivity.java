package com.example.aipet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import okhttp3.*;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button btnSelectGallery, btnTakePhoto, btnAnalyze, btnAddToDB;
    private TextView tvResult;

    private String currentPhotoPath;
    private Uri selectedImageUri;

    private final String SERVER_URL = "http://192.168.3.84"; //please input your own ip address
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final OkHttpClient client = new OkHttpClient();

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    currentPhotoPath = null;
                    loadImage(uri);
                    btnAnalyze.setEnabled(true);
                    btnAddToDB.setEnabled(true);
                }
            }
    );

    private final ActivityResultLauncher<Uri> takePhoto = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && currentPhotoPath != null) {
                    File file = new File(currentPhotoPath);
                    selectedImageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                    loadImage(selectedImageUri);
                    btnAnalyze.setEnabled(true);
                    btnAddToDB.setEnabled(true);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        btnSelectGallery = findViewById(R.id.btnSelectGallery);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnAnalyze = findViewById(R.id.btnAnalyze);
        btnAddToDB = findViewById(R.id.btnAddToDB);
        tvResult = findViewById(R.id.tvResult);

        btnSelectGallery.setOnClickListener(v -> pickImage.launch("image/*"));
        btnTakePhoto.setOnClickListener(v -> takePhotoFromCamera());
        btnAnalyze.setOnClickListener(v -> analyzeImage());
        btnAddToDB.setOnClickListener(v -> showAddDialog());

        checkPermissions();
    }

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 100);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void takePhotoFromCamera() {
        try {
            File photoFile = createImageFile();
            Uri photoURI = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            takePhoto.launch(photoURI);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadImage(Uri uri) {
        Glide.with(this).load(uri).into(imageView);
    }

    private void analyzeImage() {
        if (selectedImageUri == null) return;

        tvResult.setText("AI 正在分析...");
        executorService.execute(() -> {
            try {
                File file = getFileFromUri(selectedImageUri);
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "pet.jpg",
                                RequestBody.create(file, MediaType.parse("image/jpeg")))
                        .build();

                Request request = new Request.Builder()
                        .url(SERVER_URL + "/analyze")
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    final String responseData = response.body().string();
                    final JSONObject json = new JSONObject(responseData);
                    runOnUiThread(() -> showAnalyzeResult(json));
                }
            } catch (Exception e) {
                runOnUiThread(() -> tvResult.setText("錯誤：" + e.getMessage()));
            }
        });
    }

    private void showAnalyzeResult(JSONObject json) {
        try {
            String breed = json.getString("breed");
            double confidence = json.getDouble("confidence");
            String species = json.getString("species");
            JSONObject match = json.getJSONObject("match_info");

            String resultText = "物種：" + species + "\n" +
                    "品種：" + breed + " (" + String.format("%.1f", confidence) + "%)\n\n" +
                    "資料庫匹配：\n";

            String matchText;
            if (match.getBoolean("match")) {
                matchText = " 匹配成功！\n名字：" + match.getString("name") +
                        "\n相似度：" + match.getDouble("similarity") + "%";
            } else {
                matchText = "資料庫中未找到高度匹配的寵物";
            }

            tvResult.setText(resultText + matchText);
        } catch (Exception e) {
            tvResult.setText("解析 JSON 失敗");
        }
    }

    private void showAddDialog() {
        EditText inputName = new EditText(this);
        inputName.setHint("寵物名稱");
        EditText inputAge = new EditText(this);
        inputAge.setHint("年齡（歲）");
        EditText inputBreed = new EditText(this);
        inputBreed.setHint("品種（留空自動辨識）");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(inputName);
        layout.addView(inputAge);
        layout.addView(inputBreed);

        new AlertDialog.Builder(this)
                .setTitle("新增寵物到資料庫")
                .setView(layout)
                .setPositiveButton("新增", (dialog, which) -> addToDatabase(
                        inputName.getText().toString(),
                        inputAge.getText().toString(),
                        inputBreed.getText().toString()
                ))
                .setNegativeButton("取消", null)
                .show();
    }

    private void addToDatabase(String name, String age, String breed) {
        if (selectedImageUri == null || name.trim().isEmpty()) {
            Toast.makeText(this, "請輸入名稱並選擇照片", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try {
                File file = getFileFromUri(selectedImageUri);
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "pet.jpg", RequestBody.create(file, MediaType.parse("image/jpeg")))
                        .addFormDataPart("name", name)
                        .addFormDataPart("breed", breed)
                        .build();

                Request request = new Request.Builder()
                        .url(SERVER_URL + "/add")
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    final JSONObject json = new JSONObject(response.body().string());
                    runOnUiThread(() -> {
                        try {
                            Toast.makeText(MainActivity.this, json.getString("message"), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "新增失敗：" + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private File getFileFromUri(Uri uri) throws IOException {
        File file = new File(getCacheDir(), "upload_temp.jpg");
        try (InputStream is = getContentResolver().openInputStream(uri);
             FileOutputStream os = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
        return file;
    }
}