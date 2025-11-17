package com.example.prm392_android_app_frontend.data.dto.login;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("token")
    private String token;

    @SerializedName("user")
    private User user;

    public String getToken() { return token; }
    public User getUser() { return user; }

    public static class User {
        @SerializedName("id") private int id;
        @SerializedName("username") private String username;
        @SerializedName("email") private String email;
        @SerializedName("role") private String role;

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }
}
