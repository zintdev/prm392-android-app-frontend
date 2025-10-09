package com.example.prm392_android_app_frontend.features.auth.data.dto;

public class LoginRequest {
    public String usernameOrEmail;
    public String password;
    public LoginRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail; this.password = password;
    }
}
