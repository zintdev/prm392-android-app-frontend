package com.example.prm392_android_app_frontend.data.dto.UpdateUserRequest;

public class UpdateUserRequest  {
    public String username;
    public String email;
    public String password;     // để null nếu không đổi
    public String phoneNumber;
    public String role;         // nếu backend bắt buộc, điền lại giá trị cũ

    public void setRole(String role) {
        this.role = "CUSTOMER";
    }
}
