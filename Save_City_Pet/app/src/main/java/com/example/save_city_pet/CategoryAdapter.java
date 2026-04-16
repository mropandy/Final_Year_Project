package com.example.save_city_pet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private ArrayList<CategoryDomain> items;

    public CategoryAdapter(ArrayList<CategoryDomain> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_category, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryDomain item = items.get(position);
        holder.titleTxt.setText(item.getTitle());

        // 關鍵修正：使用 Glide 載入 JSON 裡的 picUrl 網址
        Glide.with(holder.itemView.getContext())
                .load(item.getPicUrl())
                .placeholder(R.drawable.profile) // 載入中顯示
                .error(R.drawable.profile)       // 失敗顯示
                .into(holder.pic);

        // 點擊事件保持不變
        holder.itemView.setOnClickListener(v -> {
            if (holder.itemView.getContext() instanceof MainActivity) {
                MainActivity activity = (MainActivity) holder.itemView.getContext();

                // 判斷 ID 是否為 00_home
                if (item.getId().equals("00_home")) {
                    activity.initBanner(); // 回到海報
                } else {
                    activity.loadPetsByCategory(item.getId()); // 載入寵物
                }
            }
        });
    }




    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt;
        ImageView pic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.categoryName);
            pic = itemView.findViewById(R.id.categoryPic);
        }
    }
}
