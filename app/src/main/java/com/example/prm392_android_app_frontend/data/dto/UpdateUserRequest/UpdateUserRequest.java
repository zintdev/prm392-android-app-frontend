package com.example.prm392_android_app_frontend.data.dto.UpdateUserRequest;

public class UpdateUserRequest  {
    public String username;
    public String email;
    public String password;
    public String oldPassword;
    public String phoneNumber;
    public String role;
    public void setRole(String role) {
        this.role = "CUSTOMER";
    }
}
