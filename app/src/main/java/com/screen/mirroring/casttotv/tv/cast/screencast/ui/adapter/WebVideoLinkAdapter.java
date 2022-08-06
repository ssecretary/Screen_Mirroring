package com.screen.mirroring.casttotv.tv.cast.screencast.ui.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.OnItemClickListner;
import com.screen.mirroring.casttotv.tv.cast.screencast.model.WebVideo;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ListItemWebVideoLinkBinding;

import java.util.ArrayList;
import java.util.List;

public class WebVideoLinkAdapter extends RecyclerView.Adapter<WebVideoLinkAdapter.WebVideoLinkViewHolder> {

    Context mContext;
    List<WebVideo> webVideoList;
    OnItemClickListner onItemClickListner;

    public WebVideoLinkAdapter(Context mContext) {
        this.mContext = mContext;
        webVideoList = new ArrayList<>();
    }

    public void setImageDataList(List<WebVideo> webVideoList) {
        this.webVideoList = webVideoList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListner(OnItemClickListner onItemClickListner) {
        this.onItemClickListner = onItemClickListner;
    }

    @NonNull
    @Override
    public WebVideoLinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WebVideoLinkViewHolder(ListItemWebVideoLinkBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull WebVideoLinkViewHolder holder, int position) {
        WebVideo webVideo = webVideoList.get(position);

        if (webVideo.getThumbnail_url() != null) {
            Glide.with(mContext)
                    .load(webVideo.getThumbnail_url())
                    .apply(new RequestOptions().placeholder(R.drawable.ic_video_placeholder).diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(holder.binding.thumbnailView);
        }

        holder.binding.videoName.setText(webVideo.getName());
        holder.binding.videoType.setText(webVideo.getType());

        if (webVideo.getHeight() != null && !webVideo.getHeight().isEmpty()) {
            holder.binding.videoSize.setVisibility(View.VISIBLE);
            if (webVideo.getHeight().contains("p")) {
                holder.binding.videoSize.setText(webVideo.getHeight());
            } else {
                holder.binding.videoSize.setText(webVideo.getHeight() + "p");
            }
        } else holder.binding.videoSize.setVisibility(View.GONE);

        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return webVideoList.size();
    }

    public void addItem(WebVideo webVideo) {

        boolean duplicate = false;
        for (WebVideo v : webVideoList) {
            if (v.getLink().equals(webVideo.getLink())) {
                duplicate = true;
                break;
            }
        }
        if (!duplicate) {
            webVideoList.add(webVideo);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
    }

    public void clearData() {
        webVideoList.clear();
    }

    public class WebVideoLinkViewHolder extends RecyclerView.ViewHolder {

        ListItemWebVideoLinkBinding binding;

        public WebVideoLinkViewHolder(ListItemWebVideoLinkBinding binding) {
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
