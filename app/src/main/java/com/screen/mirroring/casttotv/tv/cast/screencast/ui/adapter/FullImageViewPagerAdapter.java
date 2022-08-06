package com.screen.mirroring.casttotv.tv.cast.screencast.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.PagerAdapter;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.OnItemClickListner;
import com.screen.mirroring.casttotv.tv.cast.screencast.model.MediaFileModel;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;

import java.io.File;
import java.util.List;

public class FullImageViewPagerAdapter extends PagerAdapter {

    private List<MediaFileModel> imagesData;
    private Context mContext;
    private OnItemClickListner itemClickListner;

    public FullImageViewPagerAdapter(Context context, List<MediaFileModel> imagesData, OnItemClickListner onItemClickListner) {
        mContext = context;
        this.imagesData = imagesData;
        itemClickListner = onItemClickListner;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, final int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.item_full_imageview, collection, false);

        try {

            final SubsamplingScaleImageView imageView = layout.findViewById(R.id.img_full_imageview);

            imageView.setImage(ImageSource.uri(imagesData.get(position).getFilePath()));

            imageView.setOnClickListener(v -> {
                if (itemClickListner != null)
                    itemClickListner.onItemClick(position);
            });
            RelativeLayout img_video = layout.findViewById(R.id.img_video);
            File file = new File(imagesData.get(position).getFilePath());
            if (isValidFile(file)) {
                img_video.setVisibility(View.VISIBLE);
            }

            img_video.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imagesData.get(position).getFilePath()));
                    intent.setDataAndType(Uri.parse(imagesData.get(position).getFilePath()), "video/*");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } catch (Exception e) {
e.printStackTrace();
                }
            });

            Log.d("TAG", "instantiateItem: " + position);
            layout.setTag("myview" + position);
            collection.addView(layout);
        } catch (Exception e) {
e.printStackTrace();
        }
        return layout;
    }

    public void freeMemory() {
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }

    private boolean isValidFile(File file) {

        String ext = file.getName();
        String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);

        if (sub_ext.equalsIgnoreCase("m4v") || sub_ext.equalsIgnoreCase("wmv") || sub_ext.equalsIgnoreCase("3gp") ||
                sub_ext.equalsIgnoreCase("mp4") || sub_ext.equalsIgnoreCase("mkv") || sub_ext.equalsIgnoreCase("flv") || sub_ext.equalsIgnoreCase("avi")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        try {
            if (imagesData != null && imagesData.size() > 0) {
                return imagesData.size();
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

}
