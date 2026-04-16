package com.example.save_city_pet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        // 2. 長按刪除事件
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
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt, infoTxt;
        ImageView pic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.petTitle);
            infoTxt = itemView.findViewById(R.id.petInfo);
            pic = itemView.findViewById(R.id.petPic);
        }
    }
}
