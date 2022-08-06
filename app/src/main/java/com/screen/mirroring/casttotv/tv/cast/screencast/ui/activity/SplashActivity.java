package com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.screen.mirroring.casttotv.tv.cast.screencast.ads.LoadAds;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ActivitySplashBinding;

import java.util.Arrays;

public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding binding;
    LoadAds loadAds;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadAds = LoadAds.getInstance(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);

        new Handler(Looper.myLooper()).postDelayed(() -> openMainScreen(), 500);
    }

    public void openMainScreen() {
        startActivity(new Intent(this, MainActivity.class));
        startActivity(new Intent(this, CastDeviceListActivity.class));
        finish();
    }
}
