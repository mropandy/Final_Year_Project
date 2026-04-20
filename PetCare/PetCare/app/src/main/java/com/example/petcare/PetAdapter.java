package com.example.petcare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.ViewHolder> {
    private List<PetCare> catList;
    public PetAdapter(List<PetCare> catList) {
        this.catList = catList;
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PetCare cat = catList.get(position);

        holder.nameText.setText(cat.getName());

        String starInfo = "梳毛需求: " + getLevelStars(cat.getGrooming()) +
                "\n健康指數: " + getLevelStars(cat.getGeneralHealth());

        holder.infoText.setText(starInfo);

        Glide.with(holder.itemView.getContext())
                .load(cat.getImageLink())
                .into(holder.imageView);
    }

    private String getLevelStars(int level) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < level ? "★" : "☆");
        }
        return stars.toString();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, infoText;
        ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.nameText);
            infoText = view.findViewById(R.id.infoText);
            imageView = view.findViewById(R.id.imageView);
        }
    }
    @Override
    public int getItemCount() {
        return catList.size();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }
}
