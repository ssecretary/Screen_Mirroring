package com.screen.mirroring.casttotv.tv.cast.screencast.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.OnItemClickListner;
import com.screen.mirroring.casttotv.tv.cast.screencast.fastScrollRecyclerView.FastScroller;
import com.screen.mirroring.casttotv.tv.cast.screencast.model.MediaFileModel;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.Utils;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ListItemGridBinding;

import java.util.ArrayList;
import java.util.List;

public class PhotosListAdapter extends RecyclerView.Adapter<PhotosListAdapter.ImageListViewHolder> implements FastScroller.SectionIndexer {

    Context mContext;
    List<MediaFileModel> folderDataList;
    OnItemClickListner onItemClickListner;
    String date;

    public PhotosListAdapter(Context mContext) {
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
    public ImageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageListViewHolder(ListItemGridBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ImageListViewHolder holder, int position) {
        MediaFileModel filesModel = folderDataList.get(position);

        Glide.with(mContext).load(filesModel.getFilePath()).apply(new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_image_placeholder))
                .into(holder.binding.icon);

        if (filesModel.getType() == 2) {
            Glide.with(mContext).load(filesModel.getFilePath()).apply(new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_video_placeholder))
                    .into(holder.binding.icon);

            holder.binding.fileSize.setVisibility(View.VISIBLE);
            if (filesModel.getDuration() != null) {
                if (filesModel.getDuration().contains(":")) {
                    holder.binding.fileSize.setText(filesModel.getDuration());
                } else {
                    String duration = Utils.makeShortTimeString(mContext, Long.parseLong(filesModel.getDuration()) / 1000);
                    holder.binding.fileSize.setText(duration);
                }
            }
        } else {
            Glide.with(mContext).load(filesModel.getFilePath()).apply(new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_image_placeholder))
                    .into(holder.binding.icon);
            holder.binding.fileSize.setVisibility(View.GONE);
        }

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

    public class ImageListViewHolder extends RecyclerView.ViewHolder {

        ListItemGridBinding binding;

        public ImageListViewHolder(ListItemGridBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                if (onItemClickListner != null) {
                    onItemClickListner.onItemClick(getAdapterPosition());
                }
            });

        }

        public ImageView getImageView() {
            return binding.icon;
        }
    }
}
