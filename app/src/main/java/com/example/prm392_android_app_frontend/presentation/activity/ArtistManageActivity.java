package com.example.prm392_android_app_frontend.presentation.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.prm392_android_app_frontend.data.dto.ArtistDto;
import com.example.prm392_android_app_frontend.databinding.ActivityManageArtistBinding;
import com.example.prm392_android_app_frontend.presentation.adapter.ArtistManageAdapter;
import com.example.prm392_android_app_frontend.presentation.fragment.admin.custom.AddArtistFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.admin.custom.EditArtistFragment;
import com.example.prm392_android_app_frontend.presentation.viewmodel.ArtistViewModel;

public class ArtistManageActivity extends AppCompatActivity
        implements ArtistManageAdapter.OnArtistActionListener {

    private ActivityManageArtistBinding binding;
    private ArtistViewModel artistViewModel;
    private ArtistManageAdapter artistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageArtistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        setupFab();

        // Load data
        artistViewModel.fetchAllArtists();
    }

    private void setupToolbar() {
        binding.toolbar.setTitle("Quản lý Nghệ sĩ");
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        artistAdapter = new ArtistManageAdapter(this);
        binding.recyclerViewArtists.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewArtists.setAdapter(artistAdapter);
    }

    private void setupViewModel() {
        artistViewModel = new ViewModelProvider(this).get(ArtistViewModel.class);
        observeViewModel();
    }

    private void setupFab() {
        binding.fabAddArtist.setOnClickListener(v -> {
            AddArtistFragment dialog = new AddArtistFragment();
            dialog.show(getSupportFragmentManager(), "AddArtistDialog");
        });
    }

    private void observeViewModel() {
        // Observe artist list
        artistViewModel.getArtistList().observe(this, artists -> {
            if (artists != null && !artists.isEmpty()) {
                artistAdapter.setArtists(artists);
                binding.recyclerViewArtists.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerViewArtists.setVisibility(View.GONE);
            }
        });

        // Observe loading state
        artistViewModel.isLoading().observe(this, isLoading -> {
            // Show/hide loading indicator if you have one
        });

        // Observe error messages
        artistViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        // Observe success messages
        artistViewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                artistViewModel.fetchAllArtists(); // Refresh list
            }
        });
    }

    @Override
    public void onEditClick(ArtistDto artist) {
        EditArtistFragment dialog = EditArtistFragment.newInstance(artist);
        dialog.show(getSupportFragmentManager(), "EditArtistDialog");
    }

    @Override
    public void onDeleteClick(int artistId, String artistName) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa nghệ sĩ \"" + artistName + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    artistViewModel.deleteArtist(artistId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}