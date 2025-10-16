package com.example.prm392_android_app_frontend.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStore {
    private static final String PREF = "auth_pref";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    // ✅ Lưu thông tin khi login thành công
    public static void saveLogin(Context context, String token, int id, String username, String email, String role) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_USER_ID, id);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    // ✅ Kiểm tra trạng thái đăng nhập
    public static boolean isLoggedIn(Context context) {
        return getToken(context) != null;
    }

    // ✅ Lấy token
    public static String getToken(Context context) {
        return getPrefs(context).getString(KEY_TOKEN, null);
    }

    // ✅ Lấy các thông tin user
    public static int getUserId(Context context) {
        return getPrefs(context).getInt(KEY_USER_ID, -1);
    }

    public static String getUsername(Context context) {
        return getPrefs(context).getString(KEY_USERNAME, "");
    }

    public static String getEmail(Context context) {
        return getPrefs(context).getString(KEY_EMAIL, "");
    }

    public static String getRole(Context context) {
        return getPrefs(context).getString(KEY_ROLE, "");
    }

    // ✅ Xóa toàn bộ khi logout
    public static void clear(Context context) {
        getPrefs(context).edit().clear().apply();
    }
}
