package com.example.save_city_pet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class PetUploadManager {
    private Context context;

    public PetUploadManager(Context context) {
        this.context = context;
    }

    public void savePetLocally(String name, String breed, int age, Uri imageUri) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || imageUri == null) return;

        // 💡 1. 使用 try-with-resources 自動關閉流，防範異常時內存洩漏
        try (InputStream is = context.getContentResolver().openInputStream(imageUri)) {

            // 在手機建立專屬資料夾 (私有目錄)
            File folder = new File(context.getFilesDir(), "my_pets");
            if (!folder.exists()) folder.mkdirs();

            // 建立唯一的檔案名稱
            String fileName = "pet_" + System.currentTimeMillis() + ".jpg";
            File localFile = new File(folder, fileName);

            // 💡 2. 進行圖片壓縮：防止原圖過大導致未來 OOM 閃退
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                try (FileOutputStream fos = new FileOutputStream(localFile)) {
                    // 壓縮率設為 80%，在畫質與檔案大小間取得平衡
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                    bitmap.recycle(); // 💡 釋放 Bitmap 記憶體
                }
            } else {
                Toast.makeText(context, "解析圖片失敗", Toast.LENGTH_SHORT).show();
                return;
            }

            // 取得本地絕對路徑
            String localPath = localFile.getAbsolutePath();

            // 存入 Firebase
            saveToFirebase(uid, name, breed, age, localPath);

        } catch (Exception e) {
            Toast.makeText(context, "儲存失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToFirebase(String uid, String name, String breed, int age, String localPath) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                .child(uid).child("myPets");

        MyPetDomain newPet = new MyPetDomain();
        newPet.setName(name);
        newPet.setBreed(breed);
        newPet.setAge(age);
        newPet.setPicUrl(localPath);

        ref.push().setValue(newPet).addOnSuccessListener(aVoid ->
                Toast.makeText(context, "寵物已存至本地", Toast.LENGTH_SHORT).show());
    }
}
