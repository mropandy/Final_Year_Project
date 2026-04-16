package com.example.save_city_pet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

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
        // 💡 複用你之前寫好的 viewholder_all_cases 佈局
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_all_cases, parent, false);
        return new ViewHolder(view);
    }

    // 在 MyPetAdapter.java 的 onBindViewHolder 中
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyPetDomain pet = items.get(position);

        // 💡 讀取本地路徑並轉換成 File 給 Glide
        File imgFile = new File(pet.getPicUrl());
        if (imgFile.exists()) {
            Glide.with(holder.itemView.getContext())
                    .load(imgFile)
                    .placeholder(R.drawable.profile)
                    .into(holder.pic);
        } else {
            holder.pic.setImageResource(R.drawable.profile); // 如果檔案不見了顯示預設圖
        }

        holder.titleTxt.setText(pet.getName());
        holder.infoTxt.setText(pet.getBreed() + " | " + pet.getAge() + "歲");
    }

    @Override
    public int getItemCount() { return items.size(); }

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
