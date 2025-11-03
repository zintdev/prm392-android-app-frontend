package com.example.prm392_android_app_frontend;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            auth.signInAnonymously()
                    .addOnSuccessListener(result ->
                            Log.d("FirebaseAuth", "Đăng nhập ẩn danh thành công: " + result.getUser().getUid())
                    )
                    .addOnFailureListener(e ->
                            Log.e("FirebaseAuth", "Đăng nhập ẩn danh thất bại", e)
                    );
        } else {
            Log.d("FirebaseAuth", "Đã đăng nhập sẵn với UID: " + user.getUid());
        }
    }
}
