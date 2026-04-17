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
    private OnCategoryClickListener listener; // 💡 1. 新增點擊監聽介面

    // 💡 2. 定義介面，供 MainActivity 實作
    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryDomain item);
    }

    // 💡 3. 修改構造函數，傳入監聽器
    public CategoryAdapter(ArrayList<CategoryDomain> items, OnCategoryClickListener listener) {
        this.items = items;
        this.listener = listener;
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

        // 使用 Glide 載入
        Glide.with(holder.itemView.getContext())
                .load(item.getPicUrl())
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(holder.pic);

        // 💡 4. 使用介面處理點擊事件，避免強制轉型
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder { // 💡 加上 static 減少記憶體佔用
        TextView titleTxt;
        ImageView pic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.categoryName);
            pic = itemView.findViewById(R.id.categoryPic);
        }
    }
}
