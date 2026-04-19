package com.example.save_city_pet;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
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

        // 隱藏原本手冊用的發布按鈕
        if (holder.btnPublish != null) {
            holder.btnPublish.setVisibility(View.GONE);
        }
        // 在 PetListAdapter 的 onBindViewHolder 裡面
        if ("Found".equals(pet.getStatus())) {
            holder.tvFoundBadge.setVisibility(View.VISIBLE); // 💡 顯示綠色標籤
            holder.pic.setAlpha(0.6f); // 圖片變暗一點，讓綠色更明顯
        } else {
            holder.tvFoundBadge.setVisibility(View.GONE);
            holder.pic.setAlpha(1.0f);
        }

        // 💡 管理員權限檢查 (Admin_01)
        if (AdminConfig.isAdmin()) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                String[] options = {"標記為已歸還 (保存數據)", "強制刪除 (永久移除)"};
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Admin_01 管理選單")
                        .setItems(options, (dialog, which) -> {
                            if (which == 0) {
                                archiveItem(pet, position, holder.itemView.getContext());
                            } else {
                                deleteItem(pet.getCaseID(), position, holder.itemView.getContext());
                            }
                        })
                        .show();
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), DetailActivity.class);
            intent.putExtra("object", pet);
            holder.itemView.getContext().startActivity(intent);
        });
        if ("Found".equals(pet.getStatus())) { // 假設你在 archiveCase 時有 setStatus("Found")
            holder.tvFoundBadge.setVisibility(View.VISIBLE);
            holder.pic.setAlpha(0.6f); // 圖片變暗一點，讓文字更明顯
        } else {
            holder.tvFoundBadge.setVisibility(View.GONE);
            holder.pic.setAlpha(1.0f);
        }
    }

    // 💡 保存功能：移動到 FoundHistory 節點
    private void archiveItem(PetDomain pet, int position, Context context) {
        // 💡 必須先在這裡設定狀態，Firebase 才會存入 "Found"
        pet.setStatus("Found");

        FirebaseDatabase.getInstance().getReference("FoundHistory")
                .child(pet.getCaseID())
                .setValue(pet) // 這裡傳上去的 pet 就會帶有 status="Found"
                .addOnSuccessListener(aVoid -> {
                    deleteItem(pet.getCaseID(), position, context);
                    Toast.makeText(context, "已標記為綠色並移至紀錄", Toast.LENGTH_SHORT).show();
                });
    }


    private void deleteItem(String caseId, int position, Context context) {
        FirebaseDatabase.getInstance().getReference("Items")
                .child(caseId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    // 為了安全，檢查 position 是否有效
                    if (position < items.size()) {
                        items.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "案件已移除", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() { return items != null ? items.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt, infoTxt;
        TextView tvFoundBadge;
        ImageView pic;
        LinearLayout btnPublish;
        ImageButton btnDelete; // 💡 修正：必須在 ViewHolder 宣告這個變數

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.petTitle);
            infoTxt = itemView.findViewById(R.id.petInfo);
            pic = itemView.findViewById(R.id.petPic);
            btnPublish = itemView.findViewById(R.id.btnPublish);
            btnDelete = itemView.findViewById(R.id.btnDelete); // 💡 修正：綁定 ID
            tvFoundBadge = itemView.findViewById(R.id.tvFoundBadge);
        }
    }
}
