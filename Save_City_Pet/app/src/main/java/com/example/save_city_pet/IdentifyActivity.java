package com.example.save_city_pet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.privacysandbox.tools.core.proto.ByteString;


import com.example.save_city_pet.ml.PetBreedModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;

import java.io.File;
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
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                    // 這裡定義了一個局部的 uri
                    Uri uri = result.getData().getData();

                    try {
                        // 💡 將局部的 uri 傳入方法，並存入全域的 selectedImageUri
                        selectedImageUri = copyImageToInternalStorage(uri);

                        selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        imgPreview.setImageBitmap(selectedBitmap);
                        runInference();
                    } catch (IOException e) {
                        e.printStackTrace();
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
        imgPreview = findViewById(R.id.imgPreview);
        Button btnPick = findViewById(R.id.btnPickImage);
        Button btnNext = findViewById(R.id.btnGoReport);

        btnPick.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

// 在 IdentifyActivity.java 的 btnNext 點擊事件中
        btnNext.setOnClickListener(v -> {
            if (identifiedBreed.isEmpty() || selectedImageUri == null) {
                Toast.makeText(this, "請先選取並辨識寵物照片", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(IdentifyActivity.this, ReportActivity.class);
            intent.putExtra("AI_BREED", identifiedBreed);
            intent.putExtra("AI_IMAGE_URI", selectedImageUri.toString());
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

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(selectedBitmap, 224, 224, true);
            org.tensorflow.lite.support.image.ImageProcessor imageProcessor =
                    new org.tensorflow.lite.support.image.ImageProcessor.Builder()
                            .add(new org.tensorflow.lite.support.common.ops.NormalizeOp(0.0f, 255.0f))
                            .build();

            TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
            tensorImage.load(scaledBitmap);
            tensorImage = imageProcessor.process(tensorImage);

            PetBreedModel.Outputs outputs = model.process(tensorImage.getTensorBuffer());

            float[] confidenceArray = outputs.getOutputFeature0AsTensorBuffer().getFloatArray();
            List<String> labels = FileUtil.loadLabels(this, "labels.txt");

            int maxIndex = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidenceArray.length; i++) {
                if (confidenceArray[i] > maxConfidence) {
                    maxConfidence = confidenceArray[i];
                    maxIndex = i;
                }
            }

            if (maxIndex < labels.size()) {
                String rawBreed = labels.get(maxIndex);
                identifiedBreed = breedTranslation.getOrDefault(rawBreed, rawBreed);

                if (maxConfidence < 0.4f) {
                    tvResult.setText("辨識結果：不確定 (可能是 " + identifiedBreed + "?)");
                    tvResult.setTextColor(android.graphics.Color.GRAY);
                } else {
                    tvResult.setText("辨識結果：" + identifiedBreed +
                            "\n信心度：" + String.format("%.1f%%", maxConfidence * 100));
                    tvResult.setTextColor(android.graphics.Color.parseColor("#E53935"));
                }
            }

            model.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "辨識失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    private void initTranslationMap() {
        breedTranslation = new HashMap<>();
        breedTranslation.put("0 約瑟爹利yorkshire_terrier", "約瑟爹利");
        breedTranslation.put("1 巴哥pug", "巴哥 (Pug)");
        breedTranslation.put("2 比高狗Beagle", "比高犬 (Beagle)");
        breedTranslation.put("3 布偶貓ragdoll_cat", "布偶貓");
        breedTranslation.put("4 老虎狗Bulldog", "老虎狗 (Bulldog)");
        breedTranslation.put("5 孟買貓Bombay_cat", "孟買貓");
        breedTranslation.put("6 拉布拉多Labrador", "拉布拉多");
        breedTranslation.put("7 松鼠狗pomeranian", "松鼠狗");
        breedTranslation.put("8 波斯貓Persian", "波斯貓");
        breedTranslation.put("9 芝娃娃Chihuahua", "芝娃娃");
        breedTranslation.put("10 金毛尋回犬Golden_Retriever", "金毛尋回犬");
        breedTranslation.put("11 阿比西尼亞貓Abyssinian", "阿比西尼亞貓");
        breedTranslation.put("12 哈士奇husky", "哈士奇 (Husky)");
        breedTranslation.put("13 威爾斯柯基犬Welsh_Corgi", "威爾斯哥基");
        breedTranslation.put("14 美國短毛貓 American_Shorthair", "美國短毛貓");
        breedTranslation.put("15 拳師狗Boxer", "拳師狗 (Boxer)");
        breedTranslation.put("16 柴犬shiba_inu", "柴犬 (Shiba Inu)");
        breedTranslation.put("17 斯芬克斯貓sphynx", "斯芬克斯貓 (無毛貓)");
        breedTranslation.put("18 德國狼狗German_Shepherd", "德國狼狗");
        breedTranslation.put("19 緬因貓Maine_Coon", "緬因貓");
        breedTranslation.put("20 暹羅貓siamese_cat", "暹羅貓");
        breedTranslation.put("21 羅威納犬rottwiler", "羅威納犬");
        breedTranslation.put("22 臘腸狗dachshund", "臘腸狗");
    }

    private Uri copyImageToInternalStorage(Uri uri) throws IOException {
        java.io.InputStream inputStream = getContentResolver().openInputStream(uri);

        // 💡 加上時間戳，確保每次生成的檔名都不同，避免緩存問題
        String fileName = "ai_pet_" + System.currentTimeMillis() + ".jpg";
        File file = new File(getFilesDir(), fileName);

        java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();
        return Uri.fromFile(file);
    }
}
