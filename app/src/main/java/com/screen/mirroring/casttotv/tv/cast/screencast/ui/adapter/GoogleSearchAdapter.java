package com.screen.mirroring.casttotv.tv.cast.screencast.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.screen.mirroring.casttotv.tv.cast.screencast.R;

import java.util.ArrayList;

public class GoogleSearchAdapter extends RecyclerView.Adapter<GoogleSearchAdapter.ViewHolder> {
    FragmentActivity activity;
    ArrayList<String> search_histry;
    LayoutInflater inflater;
    private static ClickListener listener;

    public GoogleSearchAdapter(FragmentActivity activity, ArrayList<String> search_histry) {
        this.activity = activity;
        this.search_histry = search_histry;
        inflater = LayoutInflater.from(activity);
    }

    public interface ClickListener {
        void onItemClick(int position, View v, int type);
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        this.listener = clickListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_google_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.search_url.setText(search_histry.get(position));

        holder.iv_arror.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(position, view,2);
            }
        });
    }

    @Override
    public int getItemCount() {
        return search_histry.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView search_url;
        ImageView iv_arror;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            search_url = (TextView) itemView.findViewById(R.id.search_url);
            iv_arror =  itemView.findViewById(R.id.iv_arror);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(getAdapterPosition(), v,1);
                }
            });
        }
    }
}
