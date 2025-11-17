package com.example.prm392_android_app_frontend.presentation.fragment.admin.custom;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.prm392_android_app_frontend.databinding.DialogUpdateArtistBinding;
import com.example.prm392_android_app_frontend.data.dto.ArtistDto;
import com.example.prm392_android_app_frontend.presentation.viewmodel.ArtistViewModel;

public class EditArtistFragment extends DialogFragment {

    private DialogUpdateArtistBinding binding;
    private ArtistViewModel artistViewModel;
    private ArtistDto editingArtist;
    private boolean isChanged = false;

    public static EditArtistFragment newInstance(@Nullable ArtistDto artist) {
        EditArtistFragment fragment = new EditArtistFragment();
        Bundle args = new Bundle();
        if (artist != null) args.putSerializable("artist", artist);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogUpdateArtistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        artistViewModel = new ViewModelProvider(requireActivity()).get(ArtistViewModel.class);

        if (getArguments() != null) {
            editingArtist = (ArtistDto) getArguments().getSerializable("artist");
        }

        if (editingArtist == null) {
            Toast.makeText(requireContext(), "Không tìm thấy nghệ sĩ", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        // Prefill dữ liệu
        binding.etArtistName.setText(editingArtist.getArtistName());
        binding.etArtistType.setText(editingArtist.getArtistType());
        binding.etDebutYear.setText(String.valueOf(editingArtist.getDebutYear()));

        // Nút Lưu mặc định bị disable
        binding.btnSave.setEnabled(false);
        binding.btnSave.setAlpha(0.5f);

        setupChangeListeners();

        // Lưu
        binding.btnSave.setOnClickListener(v -> {
            String name = binding.etArtistName.getText().toString().trim();
            String type = binding.etArtistType.getText().toString().trim();
            String debutYearStr = binding.etDebutYear.getText().toString().trim();

            int debutYear = Integer.parseInt(debutYearStr);

            editingArtist.setArtistName(name);
            editingArtist.setArtistType(type);
            editingArtist.setDebutYear(debutYear);

            artistViewModel.updateArtist(editingArtist.getId(), editingArtist);
            Toast.makeText(requireContext(), "Đã cập nhật nghệ sĩ", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        binding.btnCancel.setOnClickListener(v -> dismiss());
    }

    private void setupChangeListeners() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkIfDataChanged();
            }

            @Override public void afterTextChanged(Editable s) {}
        };

        binding.etArtistName.addTextChangedListener(watcher);
        binding.etArtistType.addTextChangedListener(watcher);
        binding.etDebutYear.addTextChangedListener(watcher);
    }

    private void checkIfDataChanged() {
        if (editingArtist == null) return;

        String name = binding.etArtistName.getText().toString().trim();
        String type = binding.etArtistType.getText().toString().trim();
        String debutYearStr = binding.etDebutYear.getText().toString().trim();

        boolean changed = !name.equals(editingArtist.getArtistName())
                || !type.equals(editingArtist.getArtistType())
                || !debutYearStr.equals(String.valueOf(editingArtist.getDebutYear()));

        binding.btnSave.setEnabled(changed);
        binding.btnSave.setAlpha(changed ? 1f : 0.5f);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
