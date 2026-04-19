package com.example.save_city_pet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import com.example.save_city_pet.ml.PetBreedModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentifyActivity extends AppCompatActivity {

    private ImageView imgPreview;
    private TextView tvResult;
    private Bitmap selectedBitmap;
    private String identifiedBreed = "";
    private Map<String, String> breedTranslation;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    try {
                        // 取得選取圖片的原始 Bitmap
                        selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        imgPreview.setImageBitmap(selectedBitmap);
                        // 自動執行辨識
                        runInference();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "讀取圖片失敗", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify);

        initTranslationMap();

        imgPreview = findViewById(R.id.imgPreview);
        tvResult = findViewById(R.id.tvResult);
        Button btnPick = findViewById(R.id.btnPickImage);
        Button btnNext = findViewById(R.id.btnGoReport);

        btnPick.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        btnNext.setOnClickListener(v -> {
            if (identifiedBreed.isEmpty()) {
                Toast.makeText(this, "請先辨識寵物", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ReportActivity.class);
            intent.putExtra("AI_BREED", identifiedBreed);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void runInference() {
        if (selectedBitmap == null) return;

        try {
            // 1. 初始化模型
            PetBreedModel model = PetBreedModel.newInstance(this);

            // 2. 圖片預處理：手動縮放 Bitmap 到 224x224 (避開 ResizeOp 紅字問題)
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(selectedBitmap, 224, 224, true);
            TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
            tensorImage.load(scaledBitmap);

            // 3. 執行推論
            PetBreedModel.Outputs outputs = model.process(tensorImage.getTensorBuffer());

            // 💡 注意：如果此行報錯，請根據 PetBreedModel 提示改為 getProbabilityAsTensorBuffer()
            float[] confidenceArray = outputs.getOutputFeature0AsTensorBuffer().getFloatArray();

            // 4. 載入標籤檔 (需放在 src/main/assets/labels.txt)
            List<String> labels = FileUtil.loadLabels(this, "labels.txt");

            // 5. 尋找最高信心度結果
            int maxIndex = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidenceArray.length; i++) {
                if (confidenceArray[i] > maxConfidence) {
                    maxConfidence = confidenceArray[i];
                    maxIndex = i;
                }
            }

            // 6. 顯示結果 (包含翻譯)
            if (maxIndex < labels.size()) {
                String rawBreed = labels.get(maxIndex);
                identifiedBreed = breedTranslation.getOrDefault(rawBreed, rawBreed);

                tvResult.setText("辨識結果：" + identifiedBreed +
                        "\n信心度：" + String.format("%.1f%%", maxConfidence * 100));
            }

            model.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "辨識失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initTranslationMap() {
        breedTranslation = new HashMap<>();
        // 💡 在此加入你 labels.txt 裡的品種對應，例如：
        breedTranslation.put("Golden Retriever", "金毛尋回犬");
        breedTranslation.put("Corgi", "威爾斯哥基");
        breedTranslation.put("Poodle", "貴婦犬");
        breedTranslation.put("Bulldog", "鬥牛犬");
        breedTranslation.put("Samoyed", "薩摩耶犬");
        breedTranslation.put("Shiba Inu", "柴犬");
        // 你可以根據 Teachable Machine 的標籤持續增加
    }
}
