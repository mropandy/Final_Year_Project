package com.example.save_city_pet;

import java.io.Serializable;

// 💡 1. 加上 implements Serializable，確保物件能透過 Intent 安全傳遞
public class MyPetDomain implements Serializable {

    // 💡 2. 將所有變數統一放在最上方，方便閱讀與管理
    private String id;      // 預防未來擴充使用
    private String name;    // 💡 注意：與 Items 的 title 不同，對應 JSON 內的 name
    private String breed;
    private int age;
    private String gender;
    private String district;
    private String picUrl;  // 存放本地路徑
    private String notes;   // 備註
    private String key;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public MyPetDomain() {
    }

    // --- Getter 和 Setter 區塊 ---

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
