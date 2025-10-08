package com.example.prm392_android_app_frontend.features.auth.data.dto;

public class LoginResponse {
    public String token;
    public UserDto user;
    public static class UserDto {
        public int userId;
        public String username;
        public String email;
        public String role;
    }
}