package com.example.save_city_pet;

public class CategoryDomain {
    private String id;      // 用於存儲 cat_01, dog_01 等鍵值
    private String title;   // 必須對應 Firebase 的 "title"
    private String picUrl;  // 必須對應 Firebase 的 "picUrl"

    public CategoryDomain() {
        // Firebase 需要這個空建構子
    }

    // Getter 和 Setter (名稱必須符合 JavaBean 規範)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPicUrl() { return picUrl; }
    public void setPicUrl(String picUrl) { this.picUrl = picUrl; }
}
