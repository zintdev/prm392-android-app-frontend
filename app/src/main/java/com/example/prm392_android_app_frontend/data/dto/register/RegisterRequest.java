package com.example.prm392_android_app_frontend.data.dto.register;

public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String phoneNumber;

    public RegisterRequest(String username, String email, String password, String phoneNumber) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPhoneNumber() { return phoneNumber; }
}


