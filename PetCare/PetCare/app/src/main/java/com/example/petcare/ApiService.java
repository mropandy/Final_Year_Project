package com.example.petcare;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ApiService {
    @GET("v1/cats")
    Call<List<PetCare>> getCatInfo(
            @Header("X-Api-Key") String apiKey,
            @Query("name") String catName
    );
}