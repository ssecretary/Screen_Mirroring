package com.screen.mirroring.casttotv.tv.cast.screencast.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.OnItemClickListner;
import com.screen.mirroring.casttotv.tv.cast.screencast.fastScrollRecyclerView.FastScroller;
import com.screen.mirroring.casttotv.tv.cast.screencast.model.MediaFileModel;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ListItemAudioListBinding;

import java.util.ArrayList;
import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioListViewHolder> implements FastScroller.SectionIndexer {

    Context mContext;
    List<MediaFileModel> folderDataList;
    OnItemClickListner onItemClickListner;
    String date;

    public AudioAdapter(Context mContext) {
        this.mContext = mContext;
        folderDataList = new ArrayList<>();
    }

    public void setImageDataList(List<MediaFileModel> folderDataList) {
        this.folderDataList = folderDataList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListner(OnItemClickListner onItemClickListner) {
        this.onItemClickListner = onItemClickListner;
    }

    @NonNull
    @Override
    public AudioListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AudioListViewHolder(ListItemAudioListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AudioListViewHolder holder, int position) {
        MediaFileModel filesModel = folderDataList.get(position);

        Glide.with(mContext)
                .load(Uri.parse("content://media/external/audio/albumart/" + filesModel.getAlbumId()))
                .apply(new RequestOptions().placeholder(R.drawable.ic_music_placeholder).diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(holder.binding.icon);

        holder.binding.albumName.setText(filesModel.getSongAlbum());
        holder.binding.fileName.setText(filesModel.getFileName());

        holder.binding.executePendingBindings();
    }

    @Override
    public CharSequence getSectionText(int position) {
        date = String.valueOf(folderDataList.get(position).getFileName().charAt(0));
        return TextUtils.isEmpty(date) ? "" : date;
    }

    @Override
    public int getItemCount() {
        return folderDataList.size();
    }

    public class AudioListViewHolder extends RecyclerView.ViewHolder {

        ListItemAudioListBinding binding;

        public AudioListViewHolder(ListItemAudioListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                if (onItemClickListner != null) {
                    onItemClickListner.onItemClick(getAdapterPosition());
                }
            });
        }
    }
}
