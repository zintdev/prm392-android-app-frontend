package com.example.prm392_android_app_frontend.core.util;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

/**
 * Small utility to upload a file Uri to Firebase Storage and get a download URL.
 */
public class FirebaseStorageUploader {

    public interface Callback {
        void onSuccess(String downloadUrl);
        void onError(String message);
    }

    public static void uploadImage(Uri fileUri, Callback cb) {
        if (fileUri == null) {
            cb.onError("File URI is null");
            return;
        }

        StorageReference root = FirebaseStorage.getInstance().getReference();
        String fileName = "products/" + UUID.randomUUID();
        StorageReference dest = root.child(fileName);

        dest.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        dest.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override public void onSuccess(Uri uri) {
                                cb.onSuccess(uri.toString());
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override public void onFailure(@NonNull Exception e) {
                                cb.onError(e.getMessage());
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        cb.onError(e.getMessage());
                    }
                });
    }
}


