package com.screen.mirroring.casttotv.tv.cast.screencast.ads;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.screen.mirroring.casttotv.tv.cast.screencast.ScreenMirroringApp;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.PreferencesUtility;

import java.util.Date;

import static androidx.lifecycle.Lifecycle.Event.ON_START;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.OPEN_ADS;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.SHOW_OPEN_ADS;

public class AppOpenManager implements LifecycleObserver, Application.ActivityLifecycleCallbacks {
    private static final String LOG_TAG = "AppOpenManager";
//    public static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";     // Test ID
    public static String AD_UNIT_ID;

    private AppOpenAd appOpenAd = null;

    private AppOpenAd.AppOpenAdLoadCallback loadCallback;

    private final ScreenMirroringApp myApplication;
    private Activity currentActivity;

    private static boolean isShowingAd = false;
    private long loadTime = 0;

    /**
     * Constructor
     */
    public AppOpenManager(ScreenMirroringApp myApplication) {
        this.myApplication = myApplication;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(ON_START)
    public void onStart() {
        Log.e("ads", "open ads start: " + SHOW_OPEN_ADS);
        if (SHOW_OPEN_ADS) {
            if (currentActivity != null && OPEN_ADS) {
                showAdIfAvailable();
            } else {
                fetchAd();
            }
        }
        Log.d(LOG_TAG, "onStart");
    }

    /**
     * Request an ad
     */
    public void fetchAd() {
        if (isAdAvailable()) {
            return;
        }

        loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAppOpenAdLoaded(AppOpenAd ad) {
                AppOpenManager.this.appOpenAd = ad;
            }

            @Override
            public void onAppOpenAdFailedToLoad(LoadAdError loadAdError) {
                Log.e("error ------>", loadAdError.getMessage());
            }

        };
        AdRequest request = getAdRequest();
        if (currentActivity != null) {
            AD_UNIT_ID = PreferencesUtility.getAppOpenId(currentActivity);
            AppOpenAd.load(
                    myApplication, AD_UNIT_ID, request,
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
        }
    }

    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    public boolean isAdAvailable() {
        return appOpenAd != null;
    }

    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    public void showAdIfAvailable() {
        if (!isShowingAd && isAdAvailable()) {
            Log.d(LOG_TAG, "Will show ad.");

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            AppOpenManager.this.appOpenAd = null;
                            isShowingAd = false;
                            fetchAd();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            Log.d(LOG_TAG, "Error display show ad." + adError.getMessage());
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                        }
                    };

            Log.d(LOG_TAG, String.valueOf(currentActivity));
            appOpenAd.show(currentActivity, fullScreenContentCallback);

        } else {
            Log.d(LOG_TAG, "Can not show ad.");
            fetchAd();
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        currentActivity = null;
    }
}
