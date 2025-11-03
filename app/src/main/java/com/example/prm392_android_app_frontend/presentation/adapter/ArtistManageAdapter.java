package com.example.prm392_android_app_frontend.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.ArtistDto;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ArtistManageAdapter extends RecyclerView.Adapter<ArtistManageAdapter.ArtistViewHolder> {

    private List<ArtistDto> artists = new ArrayList<>();
    private OnArtistActionListener listener;

    public interface OnArtistActionListener {
        void onEditClick(ArtistDto artist);
        void onDeleteClick(int artistId, String artistName);
    }

    public ArtistManageAdapter(OnArtistActionListener listener) {
        this.listener = listener;
    }

    public void setArtists(List<ArtistDto> artists) {
        this.artists = artists;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_artist, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        ArtistDto artist = artists.get(position);
        holder.bind(artist, listener);
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    static class ArtistViewHolder extends RecyclerView.ViewHolder {
        private TextView artistName;
        private TextView artistType;
        private TextView artistDebutYear;
        private Button buttonEdit;
        private MaterialButton buttonDelete;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            artistName = itemView.findViewById(R.id.artist_name);
            artistType = itemView.findViewById(R.id.artist_type);
            artistDebutYear = itemView.findViewById(R.id.artist_debut_year);
            buttonEdit = itemView.findViewById(R.id.button_edit_artist);
            buttonDelete = itemView.findViewById(R.id.button_delete_artist);
        }

        public void bind(ArtistDto artist, OnArtistActionListener listener) {
            artistName.setText(artist.getArtistName());

            // Hiển thị loại nghệ sĩ
            if (artist.getArtistType() != null && !artist.getArtistType().isEmpty()) {
                artistType.setText(artist.getArtistType().toUpperCase());
                artistType.setVisibility(View.VISIBLE);
            } else {
                artistType.setVisibility(View.GONE);
            }

            // Hiển thị năm debut
            if (artist.getDebutYear() != null) {
                artistDebutYear.setText("Thành Lập: " + artist.getDebutYear());
                artistDebutYear.setVisibility(View.VISIBLE);
            } else {
                artistDebutYear.setVisibility(View.GONE);
            }

            // Click listeners
            buttonEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(artist);
                }
            });

            buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(artist.getId(), artist.getArtistName());
                }
            });
        }
    }
}