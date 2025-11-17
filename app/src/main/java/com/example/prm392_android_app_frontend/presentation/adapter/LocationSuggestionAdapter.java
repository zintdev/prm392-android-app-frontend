package com.example.prm392_android_app_frontend.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.store.NominatimPlace;

import java.util.ArrayList;
import java.util.List;

public class LocationSuggestionAdapter extends RecyclerView.Adapter<LocationSuggestionAdapter.SuggestionViewHolder> {

    private List<NominatimPlace> suggestions = new ArrayList<>();
    private OnSuggestionClickListener listener;

    public interface OnSuggestionClickListener {
        void onSuggestionClick(NominatimPlace place);
    }

    public LocationSuggestionAdapter(OnSuggestionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        NominatimPlace place = suggestions.get(position);
        holder.tvDisplayName.setText(place.getDisplayName());
        holder.tvCoordinates.setText(String.format("%s, %s", place.getLat(), place.getLon()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Prevent any focus loss handlers from hiding suggestions
                v.requestFocus();
                listener.onSuggestionClick(place);
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public void updateSuggestions(List<NominatimPlace> newSuggestions) {
        this.suggestions = newSuggestions != null ? newSuggestions : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void clearSuggestions() {
        this.suggestions.clear();
        notifyDataSetChanged();
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDisplayName;
        TextView tvCoordinates;

        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDisplayName = itemView.findViewById(R.id.tvDisplayName);
            tvCoordinates = itemView.findViewById(R.id.tvCoordinates);
        }
    }
}
