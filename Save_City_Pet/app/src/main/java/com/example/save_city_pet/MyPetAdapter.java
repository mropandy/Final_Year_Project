package com.example.save_city_pet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.io.File;
import java.util.ArrayList;

public class MyPetAdapter extends RecyclerView.Adapter<MyPetAdapter.ViewHolder> {
    private ArrayList<MyPetDomain> items;

    public MyPetAdapter(ArrayList<MyPetDomain> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 複用你之前寫好的 viewholder_all_cases 佈局
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_all_cases, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyPetDomain pet = items.get(position);

        // 1. 讀取本地路徑並轉換成 File 給 Glide
        if (pet.getPicUrl() != null) {
            File imgFile = new File(pet.getPicUrl());
            if (imgFile.exists()) {
                Glide.with(holder.itemView.getContext())
                        .load(imgFile)
                        .placeholder(R.drawable.profile)
                        .into(holder.pic);
            } else {
                holder.pic.setImageResource(R.drawable.profile); // 檔案不見了顯示預設圖
            }
        } else {
            holder.pic.setImageResource(R.drawable.profile);
        }

        holder.titleTxt.setText(pet.getName());
        holder.infoTxt.setText(pet.getBreed() + " | " + pet.getAge() + "歲");

        // 2. 長按整張卡片：刪除事件
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("刪除寵物")
                    .setMessage("確定要刪除「" + pet.getName() + "」的私人檔案嗎？此操作無法復原。")
                    .setPositiveButton("確定刪除", (dialog, which) -> {

                        // 刪除手機本地的圖片檔案
                        if (pet.getPicUrl() != null) {
                            File file = new File(pet.getPicUrl());
                            if (file.exists()) {
                                file.delete();
                            }
                        }

                        // 刪除 Firebase 上的資料
                        String uid = FirebaseAuth.getInstance().getUid();
                        if (uid != null && pet.getKey() != null) {
                            FirebaseDatabase.getInstance()
                                    .getReference("Users")
                                    .child(uid)
                                    .child("myPets")
                                    .child(pet.getKey())
                                    .removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(holder.itemView.getContext(), "已成功刪除", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true; // 代表長按事件已被消費
        });

        // 💡 3. 修改點擊事件：綁定在 btnPublish 上，而不是 holder.itemView
        holder.btnPublish.setOnClickListener(v -> {
            // 建立一個選擇分類的清單 (這裡使用你的 Firebase Category ID)
            String[] categories = {"撿到貓咪 (01_found_cat)", "撿到狗狗 (02_found_dog)", "尋找貓咪 (03_missing_cat)", "尋找狗狗 (04_missing_dog)"};
            String[] categoryIds = {"01_found_cat", "02_found_dog", "03_missing_cat", "04_missing_dog"};

            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("將「" + pet.getName() + "」發布至公開案件")
                    .setItems(categories, (dialog, which) -> {
                        String selectedCategoryId = categoryIds[which];

                        // 執行發布到 Items
                        publishToPublicItems(holder.itemView.getContext(), pet, selectedCategoryId);
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt, infoTxt;
        ImageView pic;
        LinearLayout btnPublish;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.petTitle);
            infoTxt = itemView.findViewById(R.id.petInfo);
            pic = itemView.findViewById(R.id.petPic);
            btnPublish = itemView.findViewById(R.id.btnPublish); // 💡 綁定 ID
        }
    }

    private void publishToPublicItems(android.content.Context context, MyPetDomain pet, String categoryId) {
        // 1. 指向 Firebase 的公開 Items 節點
        com.google.firebase.database.DatabaseReference itemsRef =
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Items");

        // 2. 建立新案件的唯一 Key (例如 case_05)
        String newCaseKey = itemsRef.push().getKey();

        if (newCaseKey != null) {
            // 3. 準備寫入公開庫的資料
            java.util.HashMap<String, Object> publicPet = new java.util.HashMap<>();
            publicPet.put("caseID", newCaseKey);
            publicPet.put("title", pet.getName());
            publicPet.put("breed", pet.getBreed());
            publicPet.put("age", pet.getAge());
            publicPet.put("categoryId", categoryId);
            publicPet.put("picUrl", pet.getPicUrl()); // 💡 由於照片在本地，其他人換手機會看不到，稍後說明改善
            publicPet.put("description", "這是一筆從個人手冊發布的通報。備註：" + (pet.getNotes() != null ? pet.getNotes() : "無"));

            // 💡 預設填入發布者的電話
            String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
            publicPet.put("phone", "待補充");

            // 4. 上傳至 Firebase
            itemsRef.child(newCaseKey).setValue(publicPet)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "已成功發布至公開平臺！", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "發布失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
