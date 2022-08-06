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
import com.screen.mirroring.casttotv.tv.cast.screencast.model.MediaFolderData;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ListItemAllFolderBinding;

import java.util.ArrayList;
import java.util.List;

public class MusicAlbumAdapter extends RecyclerView.Adapter<MusicAlbumAdapter.MusicAlbumViewHolder> implements FastScroller.SectionIndexer {

    Context mContext;
    List<MediaFolderData> folderDataList;
    OnItemClickListner onItemClickListner;
    String date;

    public MusicAlbumAdapter(Context mContext) {
        this.mContext = mContext;
        folderDataList = new ArrayList<>();
    }

    public void setFolderDataList(List<MediaFolderData> folderDataList) {
        this.folderDataList = folderDataList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListner(OnItemClickListner onItemClickListner) {
        this.onItemClickListner = onItemClickListner;
    }

    @NonNull
    @Override
    public MusicAlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MusicAlbumViewHolder(ListItemAllFolderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MusicAlbumViewHolder holder, int position) {
        MediaFolderData folderData = folderDataList.get(position);

        holder.binding.folderName.setText(folderData.getFolderName());
        holder.binding.imageCount.setText(folderData.getMediaFileList().size() + " Songs");

        if (folderData.getMediaFileList().size() == 0) {
            return;
        }

        Glide.with(mContext)
                .load(Uri.parse("content://media/external/audio/albumart/" + folderData.getMediaFileList().get(0).getAlbumId()))
                .apply(new RequestOptions().placeholder(R.drawable.ic_music_placeholder).diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(holder.binding.folderImage);

        holder.binding.executePendingBindings();
    }

    @Override
    public CharSequence getSectionText(int position) {
        date = String.valueOf(folderDataList.get(position).getFolderName().charAt(0));
        return TextUtils.isEmpty(date) ? "" : date;
    }

    @Override
    public int getItemCount() {
        return folderDataList.size();
    }

    public class MusicAlbumViewHolder extends RecyclerView.ViewHolder {

        ListItemAllFolderBinding binding;

        public MusicAlbumViewHolder(ListItemAllFolderBinding binding) {
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
