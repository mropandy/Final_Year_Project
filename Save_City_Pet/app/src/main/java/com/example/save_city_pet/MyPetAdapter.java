package com.example.save_city_pet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.File;
import java.util.ArrayList;

public class MyPetAdapter extends RecyclerView.Adapter<MyPetAdapter.ViewHolder> {
    private ArrayList<MyPetDomain> items;
    private boolean isProfile;

    public MyPetAdapter(ArrayList<MyPetDomain> items, boolean isProfile) {
        this.items = items;
        this.isProfile = isProfile;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_all_cases, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyPetDomain pet = items.get(position);

        if (pet.getPicUrl() != null) {
            File imgFile = new File(pet.getPicUrl());
            if (imgFile.exists()) {
                Glide.with(holder.itemView.getContext())
                        .load(imgFile)
                        .placeholder(R.drawable.profile)
                        .into(holder.pic);
            } else {
                holder.pic.setImageResource(R.drawable.profile);
            }
        } else {
            holder.pic.setImageResource(R.drawable.profile);
        }

        holder.titleTxt.setText(pet.getName());
        holder.infoTxt.setText(pet.getBreed() + " | " + pet.getAge() + "歲");

        if (isProfile) {
            holder.btnPublish.setVisibility(View.VISIBLE);

            holder.btnPublish.setOnClickListener(v -> {
                String[] categories = {"尋找猫咪 (03_missing_cat)", "尋找狗狗 (04_missing_dog)"};
                String[] categoryIds = {"03_missing_cat", "04_missing_dog"};

                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("將「" + pet.getName() + "」發布至公開案件")
                        .setItems(categories, (dialog, which) -> {
                            String selectedCategoryId = categoryIds[which];
                            publishToPublicItems(holder.itemView.getContext(), pet, selectedCategoryId);
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });
        } else {
            holder.btnPublish.setVisibility(View.GONE);
        }

        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("刪除寵物")
                    .setMessage("確定要刪除「" + pet.getName() + "」的私人檔案嗎？此操作無法復原。")
                    .setPositiveButton("確定刪除", (dialog, which) -> {
                        // 💡 1. 刪除本地圖片
                        if (pet.getPicUrl() != null) {
                            File file = new File(pet.getPicUrl());
                            if (file.exists()) {
                                file.delete();
                            }
                        }

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

                                        // 💡 2. 安全地更新 Adapter 畫面，防範閃退
                                        int currentPos = holder.getAdapterPosition();
                                        if (currentPos != RecyclerView.NO_POSITION && currentPos < items.size()) {
                                            items.remove(currentPos);
                                            notifyItemRemoved(currentPos);
                                        }
                                    });
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true;
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
            btnPublish = itemView.findViewById(R.id.btnPublish);
        }
    }

    private void publishToPublicItems(android.content.Context context, MyPetDomain pet, String categoryId) {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) return;

        // 💡 3. 先向 Firebase 撈出當前用戶綁定的真實電話
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userPhone = "無聯絡電話";
                if (snapshot.exists() && snapshot.hasChild("phone")) {
                    userPhone = snapshot.child("phone").getValue(String.class);
                }

                DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("Items");
                String newCaseKey = itemsRef.push().getKey();

                if (newCaseKey != null) {
                    java.util.HashMap<String, Object> publicPet = new java.util.HashMap<>();
                    publicPet.put("caseID", newCaseKey);
                    publicPet.put("title", pet.getName());
                    publicPet.put("breed", pet.getBreed());
                    publicPet.put("age", pet.getAge());
                    publicPet.put("categoryId", categoryId);
                    publicPet.put("phone", userPhone);
                    publicPet.put("gender", pet.getGender());
                    publicPet.put("district", pet.getDistrict());

                    // 💡 4. 解決破圖問題：填入對應的圖標網址，代替假網址
                    publicPet.put("picUrl", pet.getPicUrl());

                    publicPet.put("description", "Debug 模式：本地路徑發布。備註：" + (pet.getNotes() != null ? pet.getNotes() : "無"));

                    itemsRef.child(newCaseKey).setValue(publicPet)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Debug：已發布（本地路徑）", Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "無法獲取用戶資料，請稍後再試", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
