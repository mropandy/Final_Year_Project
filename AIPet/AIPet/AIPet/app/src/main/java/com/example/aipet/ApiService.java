package com.example.aipet;

import com.example.aipet.model.AnalyzeResponse;
import com.example.aipet.model.AddResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    /**
     * 上傳照片進行 AI 辨識與資料庫匹配
     */
    @Multipart
    @POST("/analyze")
    Call<AnalyzeResponse> analyzeImage(
            @Part MultipartBody.Part file
    );

    /**
     * 上傳照片並新增寵物到資料庫
     */
    @Multipart
    @POST("/add")
    Call<AddResponse> addPet(
            @Part MultipartBody.Part file,
            @Part("name") RequestBody name,
            @Part("age") RequestBody age,
            @Part("breed") RequestBody breed
    );
}