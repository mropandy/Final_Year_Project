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

    private ArrayList<PetDomain> petList;

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

        // 1. 處理標題 (Title)
        if (pet.getBreed() == null || pet.getBreed().isEmpty()) {
            holder.tvTitle.setText(pet.getTitle());
        } else {
            holder.tvTitle.setText(pet.getTitle() + " (" + pet.getBreed() + ")");
        }

        // 2. 處理詳細資訊 (Age/Gender/District)
        if (pet.getDistrict() == null || pet.getDistrict().isEmpty() || pet.getAge() == 0) {
            holder.tvDetails.setVisibility(View.GONE);
        } else {
            holder.tvDetails.setVisibility(View.VISIBLE);
            holder.tvDetails.setText("Age: " + pet.getAge() + " | " + pet.getGender() + " | " + pet.getDistrict());
        }

        holder.itemView.setOnClickListener(v -> {
            if (pet.getCaseID() != null) {
                Intent intent = new Intent(holder.itemView.getContext(), DetailActivity.class);
                intent.putExtra("object", pet);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return petList != null ? petList.size() : 0; // 💡 加上 null 安全檢查
    }

    // 💡 修正：加上 static 關鍵字切斷與外部 Adapter 的隱式引用，優化記憶體
    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvTitle, tvDetails;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlider);
            tvTitle = itemView.findViewById(R.id.petInfoTitle);
            tvDetails = itemView.findViewById(R.id.petInfoDetails);
        }
    }
}
