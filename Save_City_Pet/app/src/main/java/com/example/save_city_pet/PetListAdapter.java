package com.example.save_city_pet;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class PetListAdapter extends RecyclerView.Adapter<PetListAdapter.ViewHolder> {
    private ArrayList<PetDomain> items;

    public PetListAdapter(ArrayList<PetDomain> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_all_cases, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PetDomain pet = items.get(position);

        holder.titleTxt.setText(pet.getTitle());
        holder.infoTxt.setText(pet.getDistrict() + " | " + pet.getBreed());

        Glide.with(holder.itemView.getContext())
                .load(pet.getPicUrl())
                .placeholder(R.drawable.profile)
                .into(holder.pic);

        // 💡 強制隱藏「發布按鈕」
        if (holder.btnPublish != null) {
            holder.btnPublish.setVisibility(View.GONE);
        }

        // 點擊進入詳情頁
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), DetailActivity.class);
            intent.putExtra("object", pet);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return items != null ? items.size() : 0; }

    // 💡 加上 static 關鍵字切斷與外部隱式引用，優化記憶體
    public static class ViewHolder extends RecyclerView.ViewHolder {
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
}
