package com.example.petcare;

import static com.example.petcare.R.*;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jspecify.annotations.NonNull;

import java.util.List;

import retrofit2.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 修正：獲取按鈕視圖
        Button btnGoToList = findViewById(R.id.btnGoToList);

        // 修正：括號閉合問題
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnGoToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳轉到 ListActivity
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                startActivity(intent);
            }
        });

        // 記得執行獲取數據
        fetchData();
    }

    private void fetchData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.api-ninjas.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // 注意：API Key 建議不要直接寫死在代碼中（安全考量）
        apiService.getCatInfo("你的API_KEY", "Siberian")
                .enqueue(new Callback<List<PetCare>>() {
                    @Override
                    public void onResponse(Call<List<PetCare>> call, Response<List<PetCare>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            setupRecyclerView(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PetCare>> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "載入失敗: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupRecyclerView(List<PetCare> petList) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        PetAdapter adapter = new PetAdapter(petList);
        recyclerView.setAdapter(adapter);
    }
}
