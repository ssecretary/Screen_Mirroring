package com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.screen.mirroring.casttotv.tv.cast.screencast.Constant;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ActivityGiftBinding;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.PreferencesUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.screen.mirroring.casttotv.tv.cast.screencast.utils.Utils.getBottomNavigationHeight;

public class GiftActivity extends AppCompatActivity {

    ActivityGiftBinding binding;
    String firstPackageName = "com.sms.messages.smsme";
    String secondPackageName = "com.calendar.schedule.event";
    String firstAppName = "Messages";
    String secondAppName = "Calendar";
    String firstDescription = "Messages is an SMS and instant messaging application for texting (SMS). Messages, you can communicate with anyone.";
    String secondDescription = "Calendar is a system of organizing days. Calendars – online and print friendly – for any year and month and including public holidays and observances for countries worldwide.";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gift);

        if (getBottomNavigationHeight(this) != 0) {
            binding.loutApp.setPadding(0, 0, 0, getBottomNavigationHeight(this) + 12);
        } else {
            binding.loutApp.setPadding(0, 0, 0, (int) getResources().getDimension(R.dimen._12sdp));
        }

        intView();
    }

    private void intView() {

        String adsArray = PreferencesUtility.getAdData(this);

        if (!TextUtils.isEmpty(adsArray)) {
            try {
                JSONArray array = new JSONArray(adsArray);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    if (i == 0) {
                        firstAppName = object.getString("name");
                        firstPackageName = object.getString("packageName");
                        firstDescription = object.getString("description");
                    } else {
                        secondAppName = object.getString("name");
                        secondPackageName = object.getString("packageName");
                        secondDescription = object.getString("description");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (Constant.IS_APP_FIRST) {
            Constant.IS_APP_FIRST = false;
            binding.animationApp1.setAnimation("first_message.json");
            binding.animationApp2.setAnimation("sec_message.json");
        } else {
            Constant.IS_APP_FIRST = true;
            binding.animationApp1.setAnimation("calendar1.json");
            binding.animationApp2.setAnimation("calendar2.json");
        }

        setAppAnimation();

        binding.icBack.setOnClickListener(view -> onBackPressed());


        if (Constant.IS_APP_FIRST) {
            // call
            binding.txtAppDes.setText(secondDescription);
        } else {
            // message
            binding.txtAppDes.setText(firstDescription);
        }


        Glide.with(this).asGif().load(R.drawable.animation_google_play).into(binding.ivGif);

        binding.animationDownload.setOnClickListener(view -> {
            openAppInPlayStore();
        });

        binding.ivGif.setOnClickListener(v -> {
            openAppInPlayStore();
        });

        binding.animationApp1.setOnClickListener(v -> {
            openAppInPlayStore();
        });

        binding.animationApp2.setOnClickListener(v -> {
            openAppInPlayStore();
        });

        if (Constant.IS_APP_FIRST) {
            // call
            binding.txtAppName.setText(secondAppName);
        } else {
            // message
            binding.txtAppName.setText(firstAppName);
        }

    }

    public void openAppInPlayStore() {
        Intent i = new Intent(Intent.ACTION_VIEW);

        if (Constant.IS_APP_FIRST) {
            i.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + secondPackageName));
        } else {
            i.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + firstPackageName));
        }
        startActivity(i);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 30) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setAppAnimation() {

        binding.loutApp.setVisibility(View.VISIBLE);
        binding.ivBg.setVisibility(View.VISIBLE);

        setFirstAppAnimation();
    }

    private void setFirstAppAnimation() {
        binding.animationApp1.removeAllAnimatorListeners();
        binding.animationApp1.setVisibility(View.VISIBLE);
        binding.animationApp2.setVisibility(View.GONE);


        binding.animationApp1.playAnimation();

//        binding.animationApp1.setRepeatMode(LottieDrawable.RESTART);


        binding.animationApp1.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Log.e("onAnimation first", "end");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                Log.e("onAnimation first", "Repeat");
                binding.animationApp1.pauseAnimation();
                setSecAppAnimation();
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

    private void setSecAppAnimation() {

        binding.animationApp2.removeAllAnimatorListeners();
        binding.animationApp2.setVisibility(View.VISIBLE);
        binding.animationApp1.setVisibility(View.GONE);

        binding.animationApp2.playAnimation();


        binding.animationApp2.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Log.e("onAnimation sec", "end");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                Log.e("onAnimation sec", "Repeat");
                binding.animationApp2.pauseAnimation();
                setFirstAppAnimation();
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

    @Override
    protected void onResume() {
        super.onResume();
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                getWindow().setStatusBarColor(ContextCompat.getColor(GiftActivity.this, R.color.white));// set status background white

                getWindow().setNavigationBarColor(ContextCompat.getColor(GiftActivity.this, R.color.white));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}