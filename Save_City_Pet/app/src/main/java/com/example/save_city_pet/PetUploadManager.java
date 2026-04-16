package com.example.save_city_pet;

import android.content.Context;
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

        try {
            // 1. 在手機建立專屬資料夾 (私有目錄)
            File folder = new File(context.getFilesDir(), "my_pets");
            if (!folder.exists()) folder.mkdirs();

            // 2. 建立唯一的檔案名稱
            String fileName = "pet_" + System.currentTimeMillis() + ".jpg";
            File localFile = new File(folder, fileName);

            // 3. 將選取的圖片複製到私有資料夾
            InputStream is = context.getContentResolver().openInputStream(imageUri);
            FileOutputStream fos = new FileOutputStream(localFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.close();
            is.close();

            // 4. 取得本地絕對路徑
            String localPath = localFile.getAbsolutePath();

            // 5. 存入 Firebase
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
