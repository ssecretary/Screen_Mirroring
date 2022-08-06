package com.screen.mirroring.casttotv.tv.cast.screencast.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ListItemAudioListBinding;
import com.screen.mirroring.casttotv.tv.cast.screencast.model.MediaFileModel;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.QueueDataProvider;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.OnItemClickListner;

import java.util.ArrayList;
import java.util.List;

public class QueueListAdapter extends RecyclerView.Adapter<QueueListAdapter.QueueListViewHolder> {

    Context mContext;
    List<MediaFileModel> mediaQueueItemList;
    OnItemClickListner onItemClickListner;
    boolean isMusic;

    public QueueListAdapter(Context mContext, boolean isMusic) {
        this.mContext = mContext;
        this.isMusic = isMusic;
        mediaQueueItemList = new ArrayList<>();
    }

    public void setMediaQueueItemList(List<MediaFileModel> mediaQueueItemList) {
        this.mediaQueueItemList = mediaQueueItemList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListner(OnItemClickListner onItemClickListner) {
        this.onItemClickListner = onItemClickListner;
    }

    @NonNull
    @Override
    public QueueListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new QueueListViewHolder(ListItemAudioListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull QueueListViewHolder holder, int position) {
        MediaFileModel mediaQueueItem = mediaQueueItemList.get(position);

        if (mediaQueueItem != null) {
            if (isMusic){
                Glide.with(mContext)
                        .load(Uri.parse("content://media/external/audio/albumart/" + mediaQueueItem.getAlbumId()))
                        .apply(new RequestOptions().placeholder(R.drawable.ic_music_placeholder).diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(holder.binding.icon);
            }else {
                Glide.with(mContext)
                        .load(mediaQueueItem.getFilePath())
                        .apply(new RequestOptions().placeholder(R.drawable.ic_video_placeholder).diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(holder.binding.icon);
            }

            holder.binding.albumName.setVisibility(View.GONE);
            holder.binding.fileName.setText(mediaQueueItem.getFileName());
            QueueDataProvider provider = QueueDataProvider.getInstance(mContext);
            if (provider.getCurrentItem() != null) {
                if (position == provider.getPositionByItemId(provider.getCurrentItemId())) {
                    holder.binding.getRoot().setBackgroundColor(ContextCompat.getColor(mContext, R.color.color_primary_transparent));
                } else {
                    holder.binding.getRoot().setBackgroundColor(0);
                }
            }
        }
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mediaQueueItemList.size();
    }

    public class QueueListViewHolder extends RecyclerView.ViewHolder {

        ListItemAudioListBinding binding;

        public QueueListViewHolder(ListItemAudioListBinding binding) {
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
