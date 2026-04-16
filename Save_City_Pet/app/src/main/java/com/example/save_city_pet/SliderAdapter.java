package com.example.save_city_pet;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {

    // 1. 確保這裡宣告的是 PetDomain 的 ArrayList，且名稱叫 petList
    private ArrayList<PetDomain> petList;

    // 2. 建構子的參數類型也要改成 PetDomain
    public SliderAdapter(ArrayList<PetDomain> petList) {
        this.petList = petList;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_item_container, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        PetDomain pet = petList.get(position);

        Glide.with(holder.itemView.getContext())
                .load(pet.getPicUrl())
                .into(holder.imageView);

        // 1. 處理標題 (Title)：如果是 Banner 或沒有品種，就不顯示括號
        if (pet.getBreed() == null || pet.getBreed().isEmpty()) {
            holder.tvTitle.setText(pet.getTitle());
        } else {
            holder.tvTitle.setText(pet.getTitle() + " (" + pet.getBreed() + ")");
        }

        // 2. 處理詳細資訊 (Age/Gender/District)：
        // 如果是 Banner (地區為空) 或者年齡為 0，就直接隱藏整個 TextView
        if (pet.getDistrict() == null || pet.getDistrict().isEmpty() || pet.getAge() == 0) {
            holder.tvDetails.setVisibility(View.GONE);
        } else {
            holder.tvDetails.setVisibility(View.VISIBLE);
            holder.tvDetails.setText("Age: " + pet.getAge() + " | " + pet.getGender() + " | " + pet.getDistrict());
        }
        holder.itemView.setOnClickListener(v -> {
            // 只有是寵物資料（非海報）才跳轉
            if (pet.getCaseID() != null) {
                Intent intent = new Intent(holder.itemView.getContext(), DetailActivity.class);

                // 將整筆寵物物件塞進 Intent，Key 設定為 "object"
                intent.putExtra("object", pet);

                holder.itemView.getContext().startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return petList.size();
    }

    class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvTitle, tvDetails; // 確保這裡有宣告 TextView

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlider);
            // 3. 這裡的 ID 必須對應 slider_item_container.xml 裡的 ID
            tvTitle = itemView.findViewById(R.id.petInfoTitle);
            tvDetails = itemView.findViewById(R.id.petInfoDetails);
        }
    }
}
