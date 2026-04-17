package com.example.save_city_pet;

import java.io.Serializable;

// 💡 1. 加上 implements Serializable，確保物件能透過 Intent 安全傳遞
public class UserDomain implements Serializable {
    private String username;
    private String phone;
    private String uid; // 💡 2. 預留 UID 欄位，方便以後比對用戶身分

    // Firebase 必需的空建構子
    public UserDomain() {}

    // --- Getter 和 Setter 區塊 ---

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
