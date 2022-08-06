package com.screen.mirroring.casttotv.tv.cast.screencast.ui.adapter;

import android.content.Context;
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

public class VideosAlbumAdapter extends RecyclerView.Adapter<VideosAlbumAdapter.AllFolderViewHolder> implements FastScroller.SectionIndexer {

    Context mContext;
    List<MediaFolderData> folderDataList;
    OnItemClickListner onItemClickListner;
    String date;

    public VideosAlbumAdapter(Context mContext) {
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
    public AllFolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AllFolderViewHolder(ListItemAllFolderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AllFolderViewHolder holder, int position) {
        MediaFolderData folderData = folderDataList.get(position);

        holder.binding.folderName.setText(folderData.getFolderName());
        holder.binding.imageCount.setText(folderData.getMediaFileList().size() + " Videos");

        if (folderData.getMediaFileList().size() == 0) {
            return;
        }

        Glide.with(mContext).load(folderData.getMediaFileList().get(0).getFilePath()).apply(new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_video_placeholder))
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

    public class AllFolderViewHolder extends RecyclerView.ViewHolder {

        ListItemAllFolderBinding binding;

        public AllFolderViewHolder(ListItemAllFolderBinding binding) {
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
