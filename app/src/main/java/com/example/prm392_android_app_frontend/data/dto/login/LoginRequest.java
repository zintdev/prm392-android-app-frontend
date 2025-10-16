package com.example.prm392_android_app_frontend.data.dto.login;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("usernameOrEmail")
    private final String usernameOrEmail;

    @SerializedName("password")
    private final String password;

            public LoginRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }
}
