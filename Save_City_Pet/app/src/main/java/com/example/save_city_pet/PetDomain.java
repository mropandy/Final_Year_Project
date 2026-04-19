package com.example.save_city_pet;

import java.io.Serializable;

// 加上 Serializable 方便之後用 Intent 把整筆資料傳到詳情頁
public class PetDomain implements Serializable {
    private String title;
    private String breed;      // 品種 (Abyssinian)
    private String gender;     // 性別 (Male)
    private String district;   // 地區 (TM)
    private int age;           // 年齡 (3)
    private String picUrl;     // 圖片網址
    private String description; // 描述
    private String categoryId;  // 屬於哪個分類 (missing_cat_01)
    private String phone;
    private String caseID;
    private String status;

    public PetDomain() {
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getBreed() {
        return breed;
    }

    public String getGender() {
        return gender;
    }

    public String getDistrict() {
        return district;
    }

    public int getAge() {
        return age;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCaseID() {
        return caseID;
    }

    public String getPhone() {
        return phone;
    }

    // Setters (Firebase 讀取時會自動調用)
    public void setTitle(String title) {
        this.title = title;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setCaseID(String caseID) {
        this.caseID = caseID;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    @Override
    public String toString() {
        return "PetDomain{" + "title='" + title + '\'' + ", caseID='" + caseID + '\'' + '}';
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

