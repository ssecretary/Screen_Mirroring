package com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.screen.mirroring.casttotv.tv.cast.screencast.ads.LoadAds;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.PreferencesUtility;
import com.screen.mirroring.casttotv.tv.cast.screencast.Constant;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ActivityHelpBinding;

public class HelpActivity extends AppCompatActivity {

    ActivityHelpBinding binding;

    private int[] layouts;
    private TextView[] dots;

    MyViewPagerAdapter viewPagerAdapter;
    LoadAds loadAds;
    public String showPlateformAd = "admob";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_help);

        viewPagerAdapter = new MyViewPagerAdapter();

        binding.toolbar.setTitle(R.string.title_how_to_use);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadAds = LoadAds.getInstance(this);
        showPlateformAd = PreferencesUtility.getShowPlateformAd(this);

        initView();
    }

    public void initView() {

        if (showPlateformAd.equalsIgnoreCase(Constant.ADMOB_AD_KEY)) {
            loadAds.loadNativeAd(binding.fram, binding.advertizeLayout, R.layout.ads_unified);
        } else if (showPlateformAd.equalsIgnoreCase(Constant.FACEBOOK_AD_KEY)) {
            loadAds.loadfacebookNativeAd(binding.fram, binding.advertizeLayout, R.layout.ads_unified_facebook);
        } else if (showPlateformAd.equalsIgnoreCase(Constant.APP_NEXT_AD_KEY)) {
            loadAds.loadAppNextNativeAd(binding.fram, binding.advertizeLayout, R.layout.ads_unified_app_next);
        }

        layouts = new int[]{
                R.layout.layout_help_1,
                R.layout.layout_help_2,
                R.layout.layout_help_3};

        addPagerDots(0);

        binding.viewPager.setAdapter(viewPagerAdapter);
        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                addPagerDots(position);
                if (position == layouts.length - 1) {
                    binding.skipButton.setText(getString(R.string.action_finish));
                } else {
                    binding.skipButton.setText(getString(R.string.action_next));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        binding.skipButton.setOnClickListener(v -> {
            if (layouts.length - 1 == binding.viewPager.getCurrentItem()) {
                finish();
            } else {
                binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
            }

        });
    }

    public void addPagerDots(int currentPager) {
        dots = new TextView[layouts.length];
        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        binding.dotLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPager]);
            binding.dotLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPager].setTextColor(colorsActive[currentPager]);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
