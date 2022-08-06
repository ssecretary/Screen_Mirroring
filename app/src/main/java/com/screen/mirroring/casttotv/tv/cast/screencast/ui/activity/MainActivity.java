package com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.fragment.HomeFragment;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.DeviceConnectListener;
import com.screen.mirroring.casttotv.tv.cast.screencast.ads.LoadAds;
import com.screen.mirroring.casttotv.tv.cast.screencast.drawerMenu.DrawerAdapter;
import com.screen.mirroring.casttotv.tv.cast.screencast.drawerMenu.DrawerItem;
import com.screen.mirroring.casttotv.tv.cast.screencast.drawerMenu.SimpleItem;
import com.screen.mirroring.casttotv.tv.cast.screencast.rating.ScaleRatingBar;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.PreferencesUtility;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.Utils;
import com.screen.mirroring.casttotv.tv.cast.screencast.Constant;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ActivityMainBinding;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.Arrays;
import java.util.Locale;

import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.OPEN_ADS;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.POS_ABOUT_US;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.POS_CHANGE_LANGUAGE;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.POS_HOW_TO_USE;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.POS_MORE_APPS;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.POS_PRIVACY_POLICY;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.POS_RATE_US;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.POS_SHARE_NOW;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isMainAdLoad;
import static com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity.CastDeviceListActivity.upnpServiceController;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DeviceConnectListener {

    ActivityMainBinding binding;
    float rating_count = 0;
    PreferencesUtility preferencesUtility;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    int selectedLanguage = 4;

    LoadAds loadAds;
    boolean isFirstGift = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        preferencesUtility = PreferencesUtility.getInstance(this);
        selectedLanguage = PreferencesUtility.getSelectedLanguagePosition(this);

        loadAds = LoadAds.getInstance(this);

        if (!isMainAdLoad) {
            loadAds.showFullAd(1);
        }

        changeLanguage();
        initView();
        loadFragment(new HomeFragment());
    }

    public void initView() {

        binding.navView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar, 0, 0) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.loadAnimation.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, GiftActivity.class)));
        binding.loadAnimation2.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, GiftActivity.class)));

        setGiftAnimation();
    }

    private void setGiftAnimation() {
        binding.loadAnimation.removeAllAnimatorListeners();
        if (isFirstGift) {
            isFirstGift = false;
            binding.loadAnimation2.setAnimation("gift_2.json");
            setSecAnimation();

        } else {
            isFirstGift = true;
            binding.loadAnimation.setAnimation("gift_1.json");
            setFirstAnimation();

        }
    }

    private void setSecAnimation() {
        binding.loadAnimation2.removeAllAnimatorListeners();
        binding.loadAnimation2.setVisibility(View.VISIBLE);
        binding.loadAnimation.setVisibility(View.GONE);

        binding.loadAnimation2.playAnimation();
        binding.loadAnimation2.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Log.e("onAnimation", "end");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                Log.e("onAnimation", "Repeat");
                binding.loadAnimation2.pauseAnimation();
                setFirstAnimation();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationPause(Animator animation) {
                super.onAnimationPause(animation);
            }

            @Override
            public void onAnimationResume(Animator animation) {
                super.onAnimationResume(animation);
            }
        });

    }

    private void setFirstAnimation() {
        binding.loadAnimation.removeAllAnimatorListeners();
        binding.loadAnimation2.setVisibility(View.GONE);
        binding.loadAnimation.setVisibility(View.VISIBLE);
        binding.loadAnimation.playAnimation();

        binding.loadAnimation.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Log.e("onAnimation", "end");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                Log.e("onAnimation", "Repeat");
                binding.loadAnimation.pauseAnimation();
                setSecAnimation();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationPause(Animator animation) {
                super.onAnimationPause(animation);
            }

            @Override
            public void onAnimationResume(Animator animation) {
                super.onAnimationResume(animation);
            }
        });
    }

    public void showChangeLanguageDialog() {
        builder = new AlertDialog.Builder(this);

        // Set the alert dialog title
        builder.setTitle("Select Language");

        // Initializing an array of flowers
        final String[] language = new String[]{
                "Arabic", "Croatitan", "Czech", "Dutch", "English", "Filipino", "French", "German", "Indonesian", "Italian", "Korean", "Malay", "Polish", "Portuguese",
                "Russian", "Serbian", "Spanish", "Swedish", "Turkish", "Vietnamese"};

        builder.setSingleChoiceItems(
                language, // Items list
                selectedLanguage, // Index of checked item (-1 = no selection)
                (dialogInterface, i) -> {
                    selectedLanguage = i;
                    PreferencesUtility.setSelectedLanguagePosition(this, selectedLanguage);
                    dialog.dismiss();
                    changeLanguage();
                    Intent intent = getIntent();
                    overridePendingTransition(0, 0);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(intent);
                });

        dialog = builder.create();

        if(!((Activity)this).isFinishing())
        {
            dialog.show();
        }
        else
        {

        }
    }

    public void changeLanguage() {
        final String[] languageCodes = new String[]{
                "ar", "hr", "cs", "nl", "en", "fil", "fr", "de", "in", "it", "ko", "ms", "pl", "pt",
                "ru", "sr", "es", "sv", "tr", "vi"};
        String languageToLoad = languageCodes[selectedLanguage]; // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config,
                getApplicationContext().getResources().getDisplayMetrics());
    }

    public void moreApp() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=VClip")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).addToBackStack("").commit();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (!PreferencesUtility.getRateUS(this)) {
            showRateUsDialog();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        OPEN_ADS = false;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        OPEN_ADS = true;
        if (upnpServiceController != null) {
            upnpServiceController.resume(this);
        }
        invalidateOptionsMenu();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.browse, menu);

        if (isConnected) {
            menu.findItem(R.id.media_route_menu_item).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_cast_connected));
        } else
            menu.findItem(R.id.media_route_menu_item).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_cast));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.media_route_menu_item:
                CastDeviceListActivity castDeviceListActivity = new CastDeviceListActivity();
                castDeviceListActivity.setDeviceConnectListener(this);
                startActivityForResult(new Intent(this, CastDeviceListActivity.class), 100);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showRateUsDialog() {
        final Dialog rateUsDialog = new Dialog(this, R.style.WideDialog);
        rateUsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        rateUsDialog.setContentView(R.layout.dialog_rate_us);
        rateUsDialog.setCancelable(false);

        TextView btn_not_now = rateUsDialog.findViewById(R.id.btn_not_now);
        TextView btn_rate = rateUsDialog.findViewById(R.id.btn_rate);
        ImageView btn_later = rateUsDialog.findViewById(R.id.btn_later);
        ScaleRatingBar rotationratingbar_main = rateUsDialog.findViewById(R.id.rotationratingbar_main);

        rotationratingbar_main.setOnRatingChangeListener((ratingBar, rating, fromUser) -> {
            Log.e("TAG", "onRatingChange: " + rating);
            rating_count = rating;

        });

        btn_not_now.setOnClickListener(v -> {
            rateUsDialog.dismiss();
            finish();
        });

        btn_later.setOnClickListener(v -> {
            rateUsDialog.dismiss();
        });

        btn_rate.setOnClickListener(v -> {
            if (rating_count >= 4) {
                PreferencesUtility.setRateUs(this, true);
                // playStore
                String url = "https://play.google.com/store/apps/details?id=" + getPackageName();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                rateUsDialog.dismiss();
            } else if (rating_count <= 3 && rating_count > 0) {
                PreferencesUtility.setRateUs(this, true);
                // feed back
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("mailto:" + Uri.encode(getResources().getString(R.string.feedback_email))));

                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email via..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    ex.printStackTrace();
                    Toast.makeText(this,
                            "There are no email clients installed.", Toast.LENGTH_SHORT)
                            .show();
                }
                rateUsDialog.dismiss();
            } else {
                Toast.makeText(this, "Please click on 5 Star to give us rating on playstore.", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        rateUsDialog.show();
    }

    @Override
    public void onDeviceConnect(boolean isConnected) {
        Constant.isConnected = isConnected;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        displayNavigationItem(item.getItemId());

        return false;
    }

    public void displayNavigationItem(int itemId) {
        Fragment fragment;
        switch (itemId) {
            case R.id.nav_how_to_use:
                startActivity(new Intent(this, HelpActivity.class));
                break;
            case R.id.nav_change_language:
                showChangeLanguageDialog();
                break;
            case R.id.nav_share:
                Utils.shareApp(this);
                break;
            case R.id.nav_rate:
                Utils.rateusApp(this);
                break;
            case R.id.nav_more_app:
                moreApp();
                break;
            case R.id.nav_privacy_policy:
                startActivity(new Intent(this, PrivacyPolicyActivity.class));
                break;
            case R.id.nav_about_us:
                startActivity(new Intent(this, AboutUsActivity.class));
                break;
            default:
                fragment = new HomeFragment();
                loadFragment(fragment);
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
    }
}
