package com.example.prm392_android_app_frontend.data.dto;

import java.util.Map;

// Mẫu generic cho lỗi chuẩn REST (400/401/422/...)
public class ApiError {
    private String message;              // "Invalid credentials"
    private Map<String, String[]> errors; // {"email":["..."], "password":["..."]}

    public String getMessage() { return message; }
    public Map<String, String[]> getErrors() { return errors; }
}