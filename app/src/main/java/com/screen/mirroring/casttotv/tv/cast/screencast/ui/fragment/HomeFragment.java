package com.screen.mirroring.casttotv.tv.cast.screencast.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.screen.mirroring.casttotv.tv.cast.screencast.ads.LoadAds;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.PreferencesUtility;
import com.screen.mirroring.casttotv.tv.cast.screencast.Constant;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.FragmentHomeBinding;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity.MusicAlbumActivity;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity.PhotosActivity;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity.VideosActivity;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity.WebActivity;

import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isMainAdLoad;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    LoadAds loadAds;
    public String showPlateformAd = "admob";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadAds = LoadAds.getInstance(getActivity());
        showPlateformAd = PreferencesUtility.getShowPlateformAd(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_home, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    public void initView() {

        if (showPlateformAd.equalsIgnoreCase(Constant.ADMOB_AD_KEY)) {
            loadAds.loadNativeAd(binding.fram, binding.advertizeLayout, R.layout.ads_unified1);
        } else if (showPlateformAd.equalsIgnoreCase(Constant.FACEBOOK_AD_KEY)) {
            loadAds.loadfacebookNativeAd(binding.fram, binding.advertizeLayout, R.layout.ads_unified_facebook);
        } else if (showPlateformAd.equalsIgnoreCase(Constant.APP_NEXT_AD_KEY)) {
            loadAds.loadAppNextNativeAd(binding.fram, binding.advertizeLayout, R.layout.ads_unified_app_next);
        }

        binding.screenMirroringLayout.setOnClickListener(v -> {
            try {
//                if (!isMainAdLoad) {
//                    loadAds.showFullAd(1);
//                }
                startActivity(new Intent("android.settings.CAST_SETTINGS"));
                return;
            } catch (Exception exception1) {
                exception1.printStackTrace();
                Toast.makeText(getActivity(), "Device not supported", Toast.LENGTH_LONG).show();
            }
        });

        binding.photoLayout.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), PhotosActivity.class));
        });
        binding.videooLayout.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), VideosActivity.class));
        });
        binding.audioLayout.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), MusicAlbumActivity.class));
        });
        binding.browserLayout.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), WebActivity.class));
        });
    }
}
