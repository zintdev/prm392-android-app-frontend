package com.example.prm392_android_app_frontend.data.dto.changePassword;
public class ChangePasswordRequest {
    public String oldPassword;
    public String newPassword;

    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
