package com.example.save_city_pet;

import com.google.firebase.auth.FirebaseAuth;

public class AdminConfig {
    // 💡 將原本的 UID 定義為 Admin_01
    public static final String ADMIN_01_UID = "kjofM1zN8QfW1wuDUHSxCGbWWg33";

    // 檢查當前用戶是否為 Admin_01
    public static boolean isAdmin() {
        String currentUid = FirebaseAuth.getInstance().getUid();
        return ADMIN_01_UID.equals(currentUid);
    }
}
