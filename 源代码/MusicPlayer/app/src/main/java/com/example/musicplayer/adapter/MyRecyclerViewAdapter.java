package com.example.musicplayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;

import java.util.ArrayList;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {
    private ArrayList<String> songsList;
    private OnClickItemListener clickItemListener;
    private OnClickButtonListener clickButtonListener;

    public interface OnClickItemListener {
        void onClick(View view);
    }

    public interface OnClickButtonListener {
        void onClick(View view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View songItem;
        private final TextView textView;
        private final Button deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songItem = itemView.findViewById(R.id.song_item);
            textView = itemView.findViewById(R.id.textView);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
        }
    }

    public MyRecyclerViewAdapter(ArrayList<String> songsList, OnClickItemListener clickItemListener, OnClickButtonListener clickButtonListener) {
        this.songsList = songsList;
        this.clickItemListener = clickItemListener;
        this.clickButtonListener = clickButtonListener;
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
        holder.songItem.setOnClickListener(view -> clickItemListener.onClick(view));
        holder.deleteBtn.setOnClickListener(view -> clickButtonListener.onClick(view));
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }
}
