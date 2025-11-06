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

import com.example.prm392_android_app_frontend.data.dto.ArtistDto;
import com.example.prm392_android_app_frontend.databinding.DialogAddArtistBinding;
import com.example.prm392_android_app_frontend.presentation.viewmodel.ArtistViewModel;

public class AddArtistFragment extends DialogFragment {

    private DialogAddArtistBinding binding;
    private ArtistViewModel artistViewModel;

    public static AddArtistFragment newInstance() {
        return new AddArtistFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogAddArtistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        artistViewModel = new ViewModelProvider(requireActivity()).get(ArtistViewModel.class);

        // Nút Lưu mặc định disable
        binding.btnSave.setEnabled(false);
        binding.btnSave.setAlpha(0.5f);

        setupChangeListeners();

        // Nút Lưu
        binding.btnSave.setOnClickListener(v -> {
            String name = binding.etArtistName.getText().toString().trim();
            String type = binding.etArtistType.getText().toString().trim();
            String debutYearStr = binding.etDebutYear.getText().toString().trim();

            if (name.isEmpty() || type.isEmpty() || debutYearStr.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            int debutYear;
            try {
                debutYear = Integer.parseInt(debutYearStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Năm ra mắt không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            ArtistDto newArtist = new ArtistDto();
            newArtist.setArtistName(name);
            newArtist.setArtistType(type);
            newArtist.setDebutYear(debutYear);

            artistViewModel.createArtist(newArtist);
            Toast.makeText(requireContext(), "Đã thêm nghệ sĩ mới", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        binding.btnCancel.setOnClickListener(v -> dismiss());
    }

    private void setupChangeListeners() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkIfAllFieldsFilled();
            }

            @Override public void afterTextChanged(Editable s) {}
        };

        binding.etArtistName.addTextChangedListener(watcher);
        binding.etArtistType.addTextChangedListener(watcher);
        binding.etDebutYear.addTextChangedListener(watcher);
    }

    private void checkIfAllFieldsFilled() {
        String name = binding.etArtistName.getText().toString().trim();
        String type = binding.etArtistType.getText().toString().trim();
        String debutYear = binding.etDebutYear.getText().toString().trim();

        boolean filled = !name.isEmpty() && !type.isEmpty() && !debutYear.isEmpty();
        binding.btnSave.setEnabled(filled);
        binding.btnSave.setAlpha(filled ? 1f : 0.5f);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
