package com.example.musicplayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;

import java.util.ArrayList;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {
    private ArrayList<String> songsList;
    private OnClickListener clickListener;
    private OnLongClickListener longClickListener;

    public interface OnClickListener {
        void onClick(View view);
    }

    public interface OnLongClickListener {
        boolean onLongClick(View view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
    }

    public MyRecyclerViewAdapter(ArrayList<String> songsList, OnClickListener clickListener, OnLongClickListener longClickListener) {
        this.songsList = songsList;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.song_row_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String song = songsList.get(position);
        holder.textView.setText(song.split(".mp3")[0]);
        holder.textView.setOnClickListener(view -> clickListener.onClick(view));
        holder.textView.setOnLongClickListener(view -> longClickListener.onLongClick(view));
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }
}
